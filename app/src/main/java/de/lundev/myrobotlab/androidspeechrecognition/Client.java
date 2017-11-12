package de.lundev.myrobotlab.androidspeechrecognition;

import android.content.Context;
import android.preference.PreferenceManager;
import android.widget.TextView;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marvin
 * @author Moz4r
 */
public class Client {

    private ObjectOutputStream out;
    private ObjectInputStream in;

    private final Context ctx;
    private final TextView description;
    private final MainActivity mainactivity;

    public Client(MainActivity ctxt, TextView descrip) {
        ctx = ctxt;
        description = descrip;
        mainactivity = ctxt;
    }

    public boolean startClient() {
        //create connection to server
        String ip = PreferenceManager
                .getDefaultSharedPreferences(ctx)
                .getString("ip", "127.0.0.1");
        int port = Integer.parseInt(PreferenceManager
                .getDefaultSharedPreferences(ctx)
                .getString("port", "5684"));
        try {
            Socket sock = new Socket(ip, port);
            out = new ObjectOutputStream(sock.getOutputStream());
            in = new ObjectInputStream(sock.getInputStream());
            RemoteReader rr = new RemoteReader();
            rr.start();
            System.out.println("Connected to Server!");
            description.setText("Connected to Server!");
            return true;
        } catch (IOException ex) {
            System.out.println("Server not found");
            description.setText("Server not found");
            return false;
        }
    }

    public void sendToServer(String mes) {
        if (out == null) {
            System.out.println("Can't send");
            return;
        }
        try {
            out.writeObject(mes);
        } catch (IOException ex) {
            System.out.println("Sending failed");
        }
    }

    private void process(String mes) {
        System.out.println(mes);
        if (mes.startsWith("serverversion")) {
            String[] split = mes.split("=");
            description.setText("old version, server is @" + split[1]);
            try {
                out.close();
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (mes.startsWith("accepted")) {
            description.setText("Connection verified");
        } else if (mes.startsWith("fromServer")) {
            String[] split = mes.split("=");
            mainactivity.mesFromServer(split[1]);
        } else if (mes.startsWith("startListening"))
        {
            mainactivity.startListenInvoke();
        } else if (mes.startsWith("stopListening"))
        {
            mainactivity.stopListenInvoke();
        } else if (mes.startsWith("setAutoListenTrue"))
        {
            mainactivity.setAutoListen(true);
        } else if (mes.startsWith("setAutoListenFalse"))
        {
            mainactivity.setAutoListen(false);
        } else if (mes.startsWith("heartBeat"))
        {
        // TODO heartbeat
        } else {
            System.out.println("ERROR: " + mes);
        }
    }

    private class RemoteReader extends Thread {

        @Override
        public void run() {
            try {
                Object obj;
                while ((obj = in.readObject()) != null) {
                   System.out.println("Received message from server");

                    String mes = (String) obj;

                    process(mes);
                }
            } catch (IOException ex) {
                System.out.println("ERROR: Client.class, RemoteReader" + ex);
            } catch (ClassNotFoundException ex) {
                System.out.println("ERROR: Client.class, RemoteReader" + ex);
            }
        }
    }
}
