package no.susoft.mobile.pos.hardware;

import android.bluetooth.BluetoothDevice;

/**
 * Created on 3/11/2016.
 */
public class BluetoothDiscoveryEvent {

    private String eventType = null;
    private BluetoothDevice device = null;

    public BluetoothDiscoveryEvent(String eventType) {
        this.eventType = eventType;
    }

    public BluetoothDiscoveryEvent(String eventType, BluetoothDevice device) {
        this.eventType = eventType;
        this.device = device;
    }

    public String getEventType() {
        return eventType;
    }

    public BluetoothDevice getDevice() {
        return device;
    }
}
