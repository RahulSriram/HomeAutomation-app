package io.github.rahulsriram.homeautomation;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.util.ArrayList;

public class SplashActivity extends AppCompatActivity {
    ProgressBar progressBar;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        sp = getSharedPreferences("HomeAutomation", MODE_PRIVATE);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(0xAAFFFFFF, android.graphics.PorterDuff.Mode.MULTIPLY);
        new DeviceListRetrieverTask().execute();
    }

    class DeviceListRetrieverTask extends AsyncTask<Void, String, Integer[]> {
        String ipAddr;
        AlertDialog alertDialog;
        boolean finished;

        @Override
        protected Integer[] doInBackground(Void... voids) {
            ipAddr = sp.getString("ipAddress", "");

            while (true) {
                if (!util.testAddress(ipAddr)) {
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
                    String temp = util.sendCommand(ipAddr, "list");

                    if (temp != null) {
                        String[] temps = temp.split(",");
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
                String reply = null;
                while (reply == null) {
                    reply = util.sendCommand(ipAddr, "get_" + deviceId);
                }

                String[] data = reply.split(",");
                devices.add(new Device(data[0], deviceId, (Integer.parseInt(data[1]) == 1)));

            }

            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            intent.putParcelableArrayListExtra("Devices", devices);
            startActivity(intent);
            finish();

            return null;
        }
    }
}