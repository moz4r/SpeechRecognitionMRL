package de.lundev.myrobotlab.androidspeechrecognition;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

/**
 *
 * @author Marvin
 */
public class AsyncStartClient extends AsyncTask<String, Void, String> {

    ProgressDialog dialog;
    Context context;
    Client client;

    public AsyncStartClient(Context ctx, Client c) {
        context = ctx;
        client = c;
        dialog = new ProgressDialog(context);
    }

    @Override
    public void onPreExecute() {
        this.dialog.setMessage("Starting Socket");
        this.dialog.setCancelable(false);
        this.dialog.show();
    }

    @Override
    public void onPostExecute(final String success) {

        dialog.setMessage("Ready!");

        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public String doInBackground(final String... args) {
        boolean b = client.startClient();
        if (b) {
            return "true";
        } else {
            return "false";
        }
    }
}
