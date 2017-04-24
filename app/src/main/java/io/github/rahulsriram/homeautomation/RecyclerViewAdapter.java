package io.github.rahulsriram.homeautomation;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.DeviceViewHolder>{
    List<Device> devices;

    RecyclerViewAdapter(List<Device> dev) {
        devices = dev;
        devices = dev;
    }

    @Override
    public DeviceViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_layout, viewGroup, false);
        return new DeviceViewHolder(v);
    }

    @Override
    public void onBindViewHolder(DeviceViewHolder deviceViewHolder, int i) {
        deviceViewHolder.deviceName.setText(devices.get(i).deviceName);
        deviceViewHolder.deviceDescription.setText(String.format("#%d", devices.get(i).deviceNumber));
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

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {
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
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    //TODO: Add set_deviceNumber_1 or 0 code
                }
            });
        }
    }

}