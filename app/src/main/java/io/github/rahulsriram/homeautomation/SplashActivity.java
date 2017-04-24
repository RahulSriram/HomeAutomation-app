package io.github.rahulsriram.homeautomation;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SplashActivity extends AppCompatActivity {
    ProgressBar progressBar;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("HomeAutomation", "create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        sp = getSharedPreferences("HomeAutomation", MODE_PRIVATE);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(0xAAFFFFFF, android.graphics.PorterDuff.Mode.MULTIPLY);
        new DeviceListRetrieverTask().execute();
    }

    protected String sendCommand(String ipAddr, String cmd) {
        try {
            Log.i("HomeAutomation", "send");
            String link = "http://" + ipAddr + ":8000/test";
            String data = "cmd=" + cmd;
            URL url = new URL(link + "?" + data);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String reply = reader.readLine();
            httpURLConnection.disconnect();

            return reply;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    class DeviceListRetrieverTask extends AsyncTask<Void, String, Integer[]> {
        String ipAddr;
        AlertDialog alertDialog;
        boolean finished;

        protected boolean testAddress(String ipAddr) {
            try (Socket socket = new Socket(ipAddr, 80)) {
                Log.i("HomeAutomation", "Connected: " + ((socket.isConnected())? "true" : "false"));
                return socket.isConnected();
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected Integer[] doInBackground(Void... voids) {
            ipAddr = sp.getString("ipAddress", "");

            while (true) {
                if (!testAddress(ipAddr)) {
                    finished = false;
                    publishProgress();

                    while(!finished) {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    String temp = sendCommand(ipAddr, "list");

                    if (temp != null) {
                        String[] temps = temp.split(",");
                        Log.i("HomeAutomation", Arrays.toString(temps));
                        Integer[] list = new Integer[temps.length];
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("ipAddress", ipAddr);
                        editor.apply();

                        for (int i = 0; i < list.length; i++) {
                            list[i] = Integer.parseInt(temps[i]);
                        }

                        return list;
                    } else {
                        ipAddr = null;
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(Integer[] integers) {
            super.onPostExecute(integers);
            new DeviceStatusRetrieverTask().execute(integers);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SplashActivity.this);
            final View dialogView = View.inflate(getApplicationContext(), R.layout.ip_alert_layout, null);
            dialogBuilder.setCancelable(false);
            dialogBuilder.setView(dialogView);
            dialogBuilder.setTitle("IP Address unavailable");
            dialogBuilder.setMessage("The IP Address has changed or was entered incorrectly");
            dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    EditText ipInput = (EditText) dialogView.findViewById(R.id.ip_address_input);
                    ipAddr = ipInput.getText().toString();
                    finished = true;
                }
            });
            alertDialog = dialogBuilder.create();
            alertDialog.show();
        }
    }

    class DeviceStatusRetrieverTask extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... deviceIds) {
            ArrayList<Device> devices = new ArrayList<>();
            String ipAddr = sp.getString("ipAddress", null);

            for (Integer deviceId : deviceIds) {
                String[] reply = sendCommand(ipAddr, "get_" + deviceId).split(",");
                Log.i("HomeAutomation", Arrays.toString(reply));
                devices.add(new Device(reply[0], deviceId, (Integer.parseInt(reply[1]) == 1)));
            }

            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            intent.putParcelableArrayListExtra("Devices", devices);
            startActivity(intent);
            finish();

            return null;
        }
    }
}