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
}
