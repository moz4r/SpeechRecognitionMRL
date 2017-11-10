package de.lundev.myrobotlab.androidspeechrecognition;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.os.StrictMode;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;



/**
 * @author LunDev (GitHub), Ma. Vo. (MyRobotLab) based on this ->
 * @author Moz4r
 * http://www.jameselsey.co.uk/blogs/techblog/android-how-to-implement-voice-recognition-a-nice-easy-tutorial/
 * could be seen as an outer part of MyRobotLab (myrobotlab.org)
 */
public class MainActivity extends Activity implements RecognitionListener {

    private static final int REQUEST_CODE = 1234;
    private ListView wordsList;
    private static SpeechRecognizer speech = null;
    private static Intent intent;
    private boolean isListening=false;
    TextView debug;
    Client client;
    ImageButton speakButton;

    /**
     * called when the activity is started, currently when the App starts
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        speakButton = (ImageButton) findViewById(R.id.speakButton);

        //Startup-routines
        new ServiceHelper().checkForUpdates(this, false);

        //check if it's in socket-mode
        boolean socketmode = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getBoolean("socketmode", false);

            TextView description = (TextView) findViewById(R.id.description);
            ImageButton speakButton = (ImageButton) findViewById(R.id.speakButton);
            debug = (TextView) findViewById(R.id.debug);
            description.setText("Starting socket");
            client = new Client(this, description);
            String r = null;
            try {
                r = new AsyncStartClient(this, client).execute("start").get();
            } catch (InterruptedException ex) {
                Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (r == null) {
                //ERROR
                Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, "ERR_SOCKETMODECHECK_FAILED");
            } else if (r.equals("true")) {
                client.sendToServer("version=2015.01.01"); // + BuildConfig.VERSION_NAME
            }



        //get references to all important layout-pieces


        wordsList = (ListView) findViewById(R.id.list);

        wordsList.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sendItemAtPos(position);
            }
        });


    }

    /**
     * when the button is clicked
     *
     * @param v - View
     */
    public void speakButtonClicked(View v) {
        startVoiceRecognitionActivity();
    }

    /**
     * use an intent to start the VoiceRecognition-Activity
     */



    private void startVoiceRecognitionActivity() {
        if (!isListening) {
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
    }

    public void onResults(Bundle arg0) {
        // TODO Auto-generated method stub
        listeningStatusChange(false);
        System.out.println("onresults");
        ArrayList<String> matches = arg0.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        wordsList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                matches));

        String mode = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString("mode", "0");
        if (mode.equals("2")) {
            sendItemAtPos(0);
        }
        startVoiceRecognitionActivity();
    }

    public void onRmsChanged(float arg0) {
        // TODO Auto-generated method stub
        //Log.i(TAG, "onrmschanged");
    }

    @Override
    public void onDestroy() {
        listeningStatusChange(false);
        super.onDestroy();
        if(speech != null)
        {
            speech.destroy();
            speech=null;
        }
        System.out.println("destroy");
    }

    @Override
    public void onError(int errorCode)
    {
        listeningStatusChange(false);
        debug.setText("errorCode"+errorCode);
        if ((errorCode == SpeechRecognizer.ERROR_NO_MATCH))
        {
            //Log.d(TAG, "didn't recognize anything");
            startVoiceRecognitionActivity();
        }


    }

    public void listeningStatusChange(boolean status) {
        if (status) {
            speakButton.setImageResource(R.drawable.microon);
        }
        else
        {
            speakButton.setImageResource(R.drawable.microoff);
        }
        client.sendToServer("isListening=" + status);
        isListening=status;
    }

    public void sendItemAtPos(int position) {
        boolean socketmode = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getBoolean("socketmode", false);
        if (socketmode) {
            String mode = PreferenceManager
                    .getDefaultSharedPreferences(this)
                    .getString("mode", "0");
            if (mode.equals("1") || mode.equals("2")) {
                String request = (String) wordsList.getItemAtPosition(position);
                client.sendToServer("recognized=" + request);
            }
        } else {
            String mode = PreferenceManager
                    .getDefaultSharedPreferences(this)
                    .getString("mode", "0");
            if (mode.equals("1") || mode.equals("2")) {
                String request = (String) wordsList.getItemAtPosition(position);
                request = request.replace(" ", "%20");
                String ip = PreferenceManager
                        .getDefaultSharedPreferences(this)
                        .getString("ip", "127.0.0.1");
                String servicename = PreferenceManager
                        .getDefaultSharedPreferences(this)
                        .getString("servicename", "pab");

                String url = "http://" + ip + ":7777/services/" + servicename + "/getResponse/" + request;
                String answer = null;
                try {
                    answer = new AsyncGetStrFromUrl(this).execute(url).get();
                } catch (InterruptedException ex) {
                    Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
                }

                Boolean displayresponse = PreferenceManager
                        .getDefaultSharedPreferences(this)
                        .getBoolean("displayresponse", true);

                if (displayresponse) {
                    String session = null;
                    String msg = null;

                    try {
                        JSONObject obj1 = new JSONObject(answer);
                        session = obj1.getString("session");
                        msg = obj1.getString("msg");
                    } catch (JSONException ex) {
                    }

                    String display = "Session: " + session + "\nMessage: " + msg;

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(display)
                            .setTitle("ANSWER");

                    builder.setPositiveButton("OK", null);

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
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
        tv.setText(getString(R.string.about1) + getString(R.string.versionname) + getString(R.string.about2));
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

    public void startReconition() {
        speakButtonClicked(new View(this));
    }
}
