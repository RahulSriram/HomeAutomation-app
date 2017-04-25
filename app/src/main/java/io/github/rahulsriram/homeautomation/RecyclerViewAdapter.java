package io.github.rahulsriram.homeautomation;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.text.MessageFormat;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.DeviceViewHolder>{
    Context context;
    List<Device> devices;
    String ipAddr;

    RecyclerViewAdapter(Context c,List<Device> dev, String ip) {
        context = c;
        devices = dev;
        ipAddr = ip;
    }

    @Override
    public DeviceViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_layout, viewGroup, false);
        return new DeviceViewHolder(v);
    }

    @Override
    public void onBindViewHolder(DeviceViewHolder deviceViewHolder, int i) {
        deviceViewHolder.deviceName.setText(devices.get(i).deviceName);
        deviceViewHolder.deviceDescription.setText(MessageFormat.format("#{0}", String.valueOf(devices.get(i).deviceNumber)));
        deviceViewHolder.deviceState.setChecked(devices.get(i).deviceState);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public class DeviceViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView deviceName;
        TextView deviceDescription;
        Switch deviceState;

        DeviceViewHolder(final View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.card_view);
            deviceName = (TextView) itemView.findViewById(R.id.device_name);
            deviceDescription = (TextView) itemView.findViewById(R.id.device_description);
            deviceState = (Switch) itemView.findViewById(R.id.device_state);

            deviceState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (compoundButton.isPressed()) {
                        String cmd = "set_" + String.valueOf(devices.get(getLayoutPosition()).deviceNumber) + "_" + (isChecked ? 1 : 0);
                        new CommandSenderTask().execute(cmd);
                    }
                }
            });
        }

        class CommandSenderTask extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... strings) {
                String reply = null;

                while (reply == null) {
                    reply = util.sendCommand(ipAddr, strings[0]);
                }

                return reply;
            }

            @Override
            protected void onPostExecute(String reply) {
                super.onPostExecute(reply);

                switch (reply) {
                    case "done": //If done, toggle the device state in list and continue
                        devices.get(getLayoutPosition()).deviceState ^= true;
                        break;

                    case "error":
                    case "na": //If error, or device not available, restart app to get latest details from server
                        Intent intent = new Intent(itemView.getContext(), SplashActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(intent);
                }
            }
        }
    }
}