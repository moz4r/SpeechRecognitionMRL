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


/**
 * @author LunDev (GitHub), Ma. Vo. (MyRobotLab) based on this ->
 * @author Moz4r
 * http://www.jameselsey.co.uk/blogs/techblog/android-how-to-implement-voice-recognition-a-nice-easy-tutorial/
 * could be seen as an outer part of MyRobotLab (myrobotlab.org)
 * Client temporary build :
 * Client temporary sources :
 */
public class MainActivity extends Activity implements RecognitionListener {

    private static final int REQUEST_CODE = 1234;
    private ListView wordsList;
    private static SpeechRecognizer speech = null;
    private static Intent intent;
    private boolean isListening=false;
    private boolean autoListen=false;
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
                client.sendToServer("version=20171112"); // + BuildConfig.VERSION_NAME
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
        startListening();
    }

    /**
     * use an intent to start the VoiceRecognition-Activity
     */

    public void startListenInvoke(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startListening();
            }
        });
    }

    public void stopListenInvoke(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isListening) {
                    speech.stopListening();
                }
            }
        });
    }

    public void setAutoListen(Boolean autoListen){
    this.autoListen=autoListen;
    }

    private void startListening() {
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
            if (autoListen) {
                startListening();
            }
        }
        if ((errorCode == SpeechRecognizer.ERROR_SPEECH_TIMEOUT))
        {
            //Log.d(TAG, "didn't recognize anything");
            if (autoListen) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                startListening();
            }
        }
    }

    // here we try to catch microphone status
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
            String mode = PreferenceManager
                    .getDefaultSharedPreferences(this)
                    .getString("mode", "0");
            if (mode.equals("1") || mode.equals("2")) {
                String request = (String) wordsList.getItemAtPosition(position);
                client.sendToServer("recognized=" + request);
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
}
