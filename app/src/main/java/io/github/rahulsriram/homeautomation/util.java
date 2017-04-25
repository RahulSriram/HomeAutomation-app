package io.github.rahulsriram.homeautomation;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

class util {
    static boolean testAddress(String ipAddr) {
        try (Socket socket = new Socket(ipAddr, 8000)) {
            Log.i("HomeAutomation", "Connected: " + ((socket.isConnected())? "true" : "false"));
            return socket.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    static String sendCommand(String ipAddr, String cmd) {
        try {
            Log.i("HomeAutomation", "send: " + cmd);
            String link = "http://" + ipAddr + ":8000?cmd=" + cmd;
            URL url = new URL(link);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String reply = reader.readLine();
            httpURLConnection.disconnect();
            Log.i("HomeAutomation", "reply: " + reply);

            return reply;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
