package io.github.rahulsriram.homeautomation;

import android.os.Parcel;
import android.os.Parcelable;

public class Device implements Parcelable {
    String deviceName;
    int deviceNumber;
    boolean deviceState;

    Device(String name, int num, boolean state) {
        deviceName = name;
        deviceNumber = num;
        deviceState = state;
    }

    Device(Parcel in) {
        deviceName = in.readString();
        deviceNumber = in.readInt();
        deviceState = (in.readByte() != 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(deviceName);
        parcel.writeInt(deviceNumber);
        parcel.writeByte((byte) (deviceState ? 1 : 0));
    }

    public static final Parcelable.Creator<Device> CREATOR = new Parcelable.Creator<Device>() {
        public Device createFromParcel(Parcel in) {
            return new Device(in);
        }

        public Device[] newArray(int size) {
            return new Device[size];

        }
    };
}