package de.lundev.myrobotlab.androidspeechrecognition;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author LunDev (GitHub), Ma. Vo. (MyRobotLab) based on this ->
 * @author Moz4r
 *         http://www.jameselsey.co.uk/blogs/techblog/android-how-to-implement-voice-recognition-a-nice-easy-tutorial/
 *         could be seen as an outer part of MyRobotLab (myrobotlab.org)
 *         Client temporary build : https://github.com/moz4r/SpeechRecognitionMRL/blob/master/AndroidSpeechRecognition.apk?raw=true
 *         Client temporary sources : https://github.com/moz4r/SpeechRecognitionMRL
 */
public class MainActivity extends Activity implements RecognitionListener {

    private static final int REQUEST_CODE = 1234;
    private static SpeechRecognizer speech = null;
    private static Intent intent;
    private static boolean isListening;
    private static boolean autoListen;
    private static boolean pausedListening = false;
    public boolean isConnected;
    public Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    TextView debug;
    Client client;
    ImageButton speakButton;
    ImageButton disconnected;
    private ListView wordsList;
    private Context context = this;

    /**
     * called when the activity is started, currently when the App starts
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //MainActivity.context = getApplicationContext();

        setContentView(R.layout.activity_main);

        Boolean alwaysOn = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getBoolean("alwaysOn", true);

        if (alwaysOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        String ip = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString("ip", "0");

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        speakButton = (ImageButton) findViewById(R.id.speakButton);

        TextView description = (TextView) findViewById(R.id.description);
        ImageButton speakButton = (ImageButton) findViewById(R.id.speakButton);

        debug = (TextView) findViewById(R.id.debug);
        description.setText("Starting socket");

        client = new Client(this, description);

        class heartBeatTask extends TimerTask {
            public void run() {
                Timestamp now = new Timestamp(System.currentTimeMillis());
                long secondsBetween = (now.getTime() - timestamp.getTime()) / 1000;
                if (isConnected && secondsBetween > 6) {
                    stopClient();
                }
            }
        }

        new AsyncStartClient().execute(10000);

        //hearBeat : reset connection if connection lost
        Timer heartBeatTimer = new Timer();
        TimerTask heartBeat = new heartBeatTask();
        heartBeatTimer.scheduleAtFixedRate(heartBeat, 0, 4000);

        wordsList = (ListView) findViewById(R.id.list);

        wordsList.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sendItemAtPos(position);
            }
        });


    }


    public void disconnectedButtonClicked(View v) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (isConnected) {
                    stopClient();
                } else {
                    timestamp = new Timestamp(System.currentTimeMillis());
                    setClientConnected(client.startClient(2000));

                }
            }
        });
    }

    public void stopClient() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final TextView description = (TextView) findViewById(R.id.description);
                client.stopClient();
                setClientConnected(false);
                description.setText("Disonnected !");
            }
        });
    }

    public void speakButtonClicked(View v) {
        if (!isListening) {
            resumeListening();
        } else {
            pauseListening();
        }
    }

    public void resumeListening() {
        pausedListening = false;
        startListenInvoke();
    }

    public void pauseListening() {
        pausedListening = true;
        stopListenInvoke();
    }

    public void startListenInvoke() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startListening();
            }
        });
    }

    public void stopListenInvoke() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isListening) {
                    speech.stopListening();
                }
            }
        });
    }

    public void setAutoListen(Boolean autoListen) {
        MainActivity.autoListen = autoListen;
    }

    public void setClientConnected(Boolean Connected) {
        final ImageButton disconnected = (ImageButton) findViewById(R.id.disconnected);
        final TextView description = (TextView) findViewById(R.id.description);
        isConnected = Connected;
        if (isConnected) {
            description.setText("Connected !");
            disconnected.setImageResource(R.drawable.connect);
            //disconnected.setVisibility(View.INVISIBLE);
        } else {
            description.setText("Not connected dude !");
            disconnected.setImageResource(R.drawable.disconnect);
        }
    }

    private void startListening() {

        if (!isListening && !pausedListening) {
            if (speech == null) {
                speech = SpeechRecognizer.createSpeechRecognizer(this);
                speech.setRecognitionListener(this);
            }
            intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.intent_speechrecog));

            speech.startListening(intent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        onDestroy();
    }

    public void onBeginningOfSpeech() {
        // TODO Auto-generated method stub
        System.out.println("onbeginningofspeech");
    }

    public void onBufferReceived(byte[] arg0) {
        // TODO Auto-generated method stub
        //Log.i(TAG, "onbufferreceived");
    }

    public void onEndOfSpeech() {
        System.out.println("endofspeech");
    }


    public void onEvent(int arg0, Bundle arg1) {
        // TODO Auto-generated method stub
        System.out.println("onevent");
    }

    public void onPartialResults(Bundle arg0) {
        // TODO Auto-generated method stub
        System.out.println("onpartialresults");
    }

    public void onReadyForSpeech(Bundle arg0) {
        // TODO Auto-generated method stub
        System.out.println("onreadyforspeech");
        listeningStatusChange(true);
        debug.setText("onreadyforspeech");
    }


    public void onResults(Bundle arg0) {
        listeningStatusChange(false);
        String result = arg0.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0);
        System.out.println("onresults:" + result);

        ArrayList<String> matches = arg0.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        wordsList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                matches));

        String mode = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString("mode", "0");
        if (mode.equals("2") && !result.isEmpty()) {
            sendItemAtPos(0);
        }

        if (autoListen) {
            startListening();
        }
    }

    public void onRmsChanged(float arg0) {
        // TODO Auto-generated method stub
        //Log.i(TAG, "onrmschanged");
    }

    @Override
    public void onDestroy() {
        listeningStatusChange(false);
        super.onDestroy();
        if (speech != null) {
            speech.cancel();
            speech.destroy();
            speech = null;
        }
        //client.stopClient();
        debug.setText("onDestroy");
    }

    @Override
    public void onError(int errorCode) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        debug.setText("errorCode" + errorCode + now);

        onDestroy();

        if ((errorCode == SpeechRecognizer.ERROR_NO_MATCH || errorCode == SpeechRecognizer.ERROR_SPEECH_TIMEOUT || errorCode == SpeechRecognizer.ERROR_RECOGNIZER_BUSY)) {
            //Log.d(TAG, "didn't recognize anything");
            if (autoListen && !pausedListening) {
                startListening();
            }

        }
    }

    // here we try to catch microphone status
    public void listeningStatusChange(boolean status) {
        if (status) {
            speakButton.setImageResource(R.drawable.microon);
        } else {
            speakButton.setImageResource(R.drawable.microoff);
        }
        client.sendToServer("isListening=" + status);
        isListening = status;
    }

    public void sendItemAtPos(int position) {
        String mode = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString("mode", "0");
        if (mode.equals("1") || mode.equals("2")) {
            String request = (String) wordsList.getItemAtPosition(position);

            client.sendToServer("recognized=" + request);
            System.out.println(request + " is recognized");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Buttons ActionBar
        getMenuInflater()
                .inflate(R.menu.activity_main_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Button clicked ActionBar
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class
                ));
                return true;
            case R.id.action_about:
                showAbout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showAbout() {
        //about pop-up
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("About");

        final TextView tv = new TextView(this);
        tv.setText(getString(R.string.about1) + getString(R.string.versionname));
        builder.setView(tv);

        builder.show();
    }

    public void mesFromServer(final String mes) {
        final Context ctx = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setMessage(mes)
                        .setTitle("From Server");

                builder.setPositiveButton("OK", null);

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    class AsyncStartClient extends AsyncTask<Integer, Integer, Boolean> {

        final ProgressDialog dialog = new ProgressDialog(context);

        @Override
        public void onPreExecute() {
            Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, "onPreExecute", "onPreExecute");
            TextView description = new TextView(context);
            description.setText("onPreExecute");
            String ip = PreferenceManager
                    .getDefaultSharedPreferences(context)
                    .getString("ip", "127.0.0.1");
            int port = Integer.parseInt(PreferenceManager
                    .getDefaultSharedPreferences(context)
                    .getString("port", "5684"));
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.show();
            dialog.setMessage("Connecting to : " + ip + ":" + port + " ( timeout=10 sec )");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        public void onPostExecute(final Boolean success) {
            Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, "Ready", "Ready");

            if (success) {
                setClientConnected(true);
                client.sendToServer("version=1.0b");
            } else {
                setClientConnected(false);
            }
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }

        @Override
        public Boolean doInBackground(Integer... params) {
            try {
                return client.startClient(params[0]);
            } catch (Exception ex) {
                return false;
            }
        }
    }

}
