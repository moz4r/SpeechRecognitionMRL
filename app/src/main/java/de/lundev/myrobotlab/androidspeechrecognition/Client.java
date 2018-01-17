package de.lundev.myrobotlab.androidspeechrecognition;

import android.preference.PreferenceManager;
import android.widget.TextView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Timestamp;

/**
 * @author Marvin
 * @author Moz4r
 */
public class Client {

    private final TextView description;
    private final MainActivity mainactivity;
    Socket sock = new Socket();
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public Client(MainActivity mainactivity, TextView description) {
        this.description = description;
        this.mainactivity = mainactivity;
    }

    public boolean startClient(int timeout) {
        //create connection to server
        String ip = PreferenceManager
                .getDefaultSharedPreferences(mainactivity)
                .getString("ip", "127.0.0.1");
        int port = Integer.parseInt(PreferenceManager
                .getDefaultSharedPreferences(mainactivity)
                .getString("port", "5684"));
        description.setText("connecting");
        try {
            sock = new Socket();
            sock.connect(new InetSocketAddress(ip, port), timeout);
            out = new ObjectOutputStream(sock.getOutputStream());
            in = new ObjectInputStream(sock.getInputStream());
            RemoteReader rr = new RemoteReader();
            rr.start();
            System.out.println("Connected to Server!");
            description.setText("Connected to Server!");
            return true;
        } catch (Exception ex) {
            System.out.println(ex);
            description.setText("Server not found");
            stopClient();
            return false;
        }
    }

    public void stopClient() {
        mainactivity.setClientConnected(false);
        try {
            out.close();
            in.close();
            sock.close();
        } catch (Exception ex) {
            System.out.println("sock error");
        }
    }

    public void sendToServer(String mes) {
        if (out == null || !mainactivity.isConnected) {
            System.out.println("Can't send");
            return;
        }
        try {
            out.writeObject(mes);
            System.out.println(mes + " is sent");
        } catch (IOException ex) {
            System.out.println("Sending failed");
        }
    }

    private void process(String mes) {
        System.out.println(mes);
        if (mes.startsWith("serverversion")) {
            String[] split = mes.split("=");
            description.setText("old version, server is @" + split[1]);
            stopClient();
        } else if (mes.startsWith("accepted")) {
            description.setText("Connection verified");
        } else if (mes.startsWith("fromServer")) {
            String[] split = mes.split("=");
            mainactivity.mesFromServer(split[1]);
        } else if (mes.startsWith("startListening")) {
            mainactivity.startListenInvoke();
        } else if (mes.startsWith("stopListening")) {
            mainactivity.stopListenInvoke();
        } else if (mes.startsWith("pauseListening")) {
            mainactivity.pauseListening();
        } else if (mes.startsWith("resumeListening")) {
            mainactivity.resumeListening();
        } else if (mes.startsWith("setAutoListenTrue")) {
            mainactivity.setAutoListen(true);
        } else if (mes.startsWith("setAutoListenFalse")) {
            mainactivity.setAutoListen(false);
        } else if (mes.startsWith("heartBeat")) {
            mainactivity.timestamp = new Timestamp(System.currentTimeMillis());
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
