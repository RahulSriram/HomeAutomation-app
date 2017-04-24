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
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SplashActivity extends AppCompatActivity {
    TextView text;
    ProgressBar progressBar;
    SharedPreferences sp;
    TextView log;
    ScrollView scrollView;
    Integer[] deviceIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        deviceIds = null;
        sp = getSharedPreferences("HomeAutomation", MODE_PRIVATE);
        log = (TextView) findViewById(R.id.text_log);
        scrollView = (ScrollView) findViewById(R.id.log_scroll_view);
        scrollView.setVisibility(View.INVISIBLE);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(0xAAFFFFFF, android.graphics.PorterDuff.Mode.MULTIPLY);
        progressBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollView.setVisibility(((scrollView.getVisibility() == View.VISIBLE) ? View.INVISIBLE : View.VISIBLE));
            }
        });

        DeviceListRetrieverTask deviceListRetrieverTask = new DeviceListRetrieverTask();
        deviceListRetrieverTask.execute();
        DeviceStatusRetrieverTask deviceStatusRetrieverTask = new DeviceStatusRetrieverTask();
        deviceStatusRetrieverTask.execute();
    }

    protected String sendCommand(String ipAddr, String cmd) {
        try {
            String link = "http://" + ipAddr;
            String data = "cmd=" + cmd;
            URL url = new URL(link);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            OutputStreamWriter writer = new OutputStreamWriter(httpURLConnection.getOutputStream());
            writer.write(data);
            writer.flush();
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String reply = reader.readLine();
            httpURLConnection.disconnect();

            return reply;
        } catch (Exception e) {
            return e.getClass().toString();
        }
    }

    class DeviceListRetrieverTask extends AsyncTask<Void, Void, Void> {
        String ipAddr;

        protected boolean testAddress(String ipAddr) {
            try (Socket socket = new Socket(ipAddr, 80)) {
                return socket.isConnected();
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ipAddr = sp.getString("ipAddress", null);

            while (true) {
                while (!testAddress(ipAddr)) {
                    Toast.makeText(getApplicationContext(), ":/", Toast.LENGTH_SHORT).show();
                    publishProgress();
                }

                String[] temp = sendCommand(ipAddr, "list").split(",");
                if (temp.length > 0) {
                    Integer[] list = new Integer[temp.length];
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("ipAddress", ipAddr);
                    editor.apply();

                    for (int i = 0; i < list.length; i++) {
                        list[i] = Integer.parseInt(temp[i]);
                    }

                    deviceIds = list;
                    break;
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getApplicationContext());
            LayoutInflater inflater = getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.ip_alert_layout, null);
            dialogBuilder.setCancelable(false);
            dialogBuilder.setView(dialogView);
            dialogBuilder.setTitle("IP Address unavailable");
            dialogBuilder.setMessage("The IP Address has changed or was entered incorrectly");
            dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    EditText ipInput = (EditText) dialogView.findViewById(R.id.ip_address_input);
                    ipAddr = ipInput.getText().toString();
                }
            });
            AlertDialog alertDialog = dialogBuilder.create();
            alertDialog.show();
        }
    }

    class DeviceStatusRetrieverTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            while(deviceIds == null); //Waiting for DeviceListRetrieverTask to get device ID list
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ArrayList<Device> devices = new ArrayList<>();
            String ipAddr = sp.getString("ipAddress", null);

            for (Integer deviceId : deviceIds) {
                String[] reply = sendCommand(ipAddr, "status_" + deviceId).split(",");
                Log.i("HomeAutomation", Arrays.toString(reply));
                devices.add(new Device(reply[0], deviceId, (Integer.parseInt(reply[1]) == 1)));
            }

            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            intent.putParcelableArrayListExtra("Devices", devices);
            startActivity(intent);

            return null;
        }
    }
}
