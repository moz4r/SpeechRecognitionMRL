package de.lundev.myrobotlab.androidspeechrecognition;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 *
 * @author Marvin
 */
public class ServiceHelper {

    //make a HTTP-call & get the response
    public String getStrFromUrl(Context ctx, String surl) {
        if (!(internetavailable(ctx))) {
            return null;
        }
        String resp = "";
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(surl);
        HttpResponse response = null;
        try {
            response = client.execute(request);
        } catch (IOException ex) {
        }
        BufferedReader rd;
        try {
            rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line;
            while ((line = rd.readLine()) != null) {
                resp = resp + line + "\n";
            }
        } catch (IOException ex) {
        } catch (IllegalStateException ex) {
        }
        return resp;
    }

    //check if internet is available
    public boolean internetavailable(Context ctx) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI")) {
                if (ni.isConnected()) {
                    haveConnectedWifi = true;
                }
            }
            if (ni.getTypeName().equalsIgnoreCase("MOBILE")) {
                if (ni.isConnected()) {
                    haveConnectedMobile = true;
                }
            }
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    //check for Updates (Startup & Settings)
    public void checkForUpdates(final Context ctx, boolean now) {

        boolean checkforupdates = false;

        if (now) {
            checkforupdates = true;
        }
        
        String modus = PreferenceManager.getDefaultSharedPreferences(ctx).getString("updatemode", "0");
        //ceck for Updates on ...
        if (modus.equals("0")) {
            //Startup
            checkforupdates = true;
        } else if (modus.equals("1")) {
            //manually
        } else if (modus.equals("2")) {
            //1x a day
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            String dateakt = new SimpleDateFormat("dd.MM.yyyy")
                    .format(calendar.getTime());
            String datesav = PreferenceManager
                    .getDefaultSharedPreferences(ctx)
                    .getString("dateupdate", "01.01.2014");
            if (!(dateakt.equals(datesav))) {
                checkforupdates = true;
                PreferenceManager
                        .getDefaultSharedPreferences(ctx)
                        .edit()
                        .putString("dateupdate", dateakt)
                        .commit();
            }
        }

        if (checkforupdates) {
            String[] aktversionurl = {"https://raw.githubusercontent.com/LunDev/database/master/VoiceRecognitionMRLApp/version"};
            String aktversion = null;
            try {
                aktversion = new AsyncGetStrFromUrl(ctx).execute(aktversionurl).get();
            } catch (InterruptedException ex) {
            } catch (ExecutionException ex) {
            }
            if (aktversion == null) {
                return;
            }
            aktversion = aktversion.substring(0, aktversion.length() - 1);

            String runversion = BuildConfig.VERSION_NAME;

            boolean versionneuer = false;

            String aktversion2 = aktversion.replace(".", "~");
            String[] aktversionsplit = aktversion2.split("~");
            int[] aktversionsplitint = new int[aktversionsplit.length];
            for (int i = 0; i < aktversionsplit.length; i++) {
                aktversionsplitint[i] = Integer.parseInt(aktversionsplit[i]);
            }

            String runversion2 = runversion.replace(".", "~");
            String[] runversionsplit = runversion2.split("~");
            int[] runversionsplitint = new int[runversionsplit.length];
            for (int i = 0; i < runversionsplit.length; i++) {
                runversionsplitint[i] = Integer.parseInt(runversionsplit[i]);
            }

            for (int i = 0; i < 3; i++) {
                if (aktversionsplitint[i] < runversionsplitint[i]) {
                    //eigener Versions-Teil ist NEUER wie der aktuelleste Versions-Teil
                    break;
                } else if (aktversionsplitint[i] > runversionsplitint[i]) {
                    //eigener Versions-Teil ist ÄLTER wie der aktuelleste Versions-Teil
                    versionneuer = true;
                    break;
                } else if (aktversionsplitint[i] > runversionsplitint[i]) {
                    //eigener Versions-Teil ist GLEICH wie der aktuelleste Versions-Teil
                }
            }

            if (versionneuer) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                String[] releasemessageurl = {"https://raw.githubusercontent.com/LunDev/database/master/VoiceRecognitionMRLApp/releasemessage"};
                String releasemessage = null;
                try {
                    releasemessage = new AsyncGetStrFromUrl(ctx).execute(releasemessageurl).get();
                } catch (InterruptedException ex) {
                } catch (ExecutionException ex) {
                }
                String text = "From " + runversion + " updaten?\nDownload now?!";
                if (releasemessage == null) {
                } else {
                    text = text + "\n" + releasemessage;
                }
                builder.setMessage(text)
                        .setTitle(aktversion + " is out now!");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String[] ur1url = {"https://raw.githubusercontent.com/LunDev/database/master/VoiceRecognitionMRLApp/downloadlink"};
                        String ur1 = null;
                        try {
                            ur1 = new AsyncGetStrFromUrl(ctx).execute(ur1url).get();
                        } catch (InterruptedException ex) {
                        } catch (ExecutionException ex) {
                        }
                        if (ur1 == null) {
                        } else {
                            ctx.startActivity(new Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(ur1)));
                        }
                    }
                });
                builder.setNegativeButton("Later", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }
}
