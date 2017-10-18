package de.lundev.myrobotlab.androidspeechrecognition;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

/**
 *
 * @author Marvin
 */
public class AsyncGetStrFromUrl extends AsyncTask<String, Void, String> {

    ProgressDialog dialog;
    Context context;

    public AsyncGetStrFromUrl(Context ctx) {
        context = ctx;
        dialog = new ProgressDialog(context);
    }

    @Override
    public void onPreExecute() {
        this.dialog.setMessage("Making web-request");
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
        String surl = args[0];

        return new ServiceHelper()
                .getStrFromUrl(context, surl);
    }
}
