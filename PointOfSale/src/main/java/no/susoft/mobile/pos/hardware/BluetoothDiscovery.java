package no.susoft.mobile.pos.hardware;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import eu.nets.baxi.pcl.PCLDevice;
import eu.nets.baxi.pcl.PCLReader;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.ui.activity.MainActivity;

/**
 * Created on 3/11/2016.
 */
public class BluetoothDiscovery {

    private static BluetoothDiscovery instance;
    private BroadcastReceiver discoverUnpairedDevicesReceiver;
    private BroadcastReceiver discoveryFinishedReceiver;
    private List<BluetoothDevice> discoveredDevices = new ArrayList<>();
    private List<BluetoothDevice> pairedDevices = new ArrayList<>();
    private BluetoothAdapter bluetoothAdapter = null;
    private PCLReader pclReader = null;
    private List<BluetoothDiscoveryListener> listeners = new ArrayList<>();

    private BluetoothDiscovery() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        pclReader = new PCLReader(MainActivity.getInstance());
    }

    public static BluetoothDiscovery getInstance() {
        if (instance == null) instance = new BluetoothDiscovery();
        return instance;
    }

    public boolean isIdle() {
        if (bluetoothAdapter == null) return false;
        return !bluetoothAdapter.isDiscovering();
    }

    public void discoverDevices() {

        if (bluetoothAdapter == null) return;
        if (bluetoothAdapter.isDiscovering()) return;

        if (discoverUnpairedDevicesReceiver == null) {
            discoverUnpairedDevicesReceiver = new BroadcastReceiver() {

                //---fired when a new device is discovered---
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();

                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                        if (!discoveredDevices.contains(device)) {
                            discoveredDevices.add(device);
                            dispathEvent(new BluetoothDiscoveryEvent(BluetoothDevice.ACTION_FOUND, device));
                        }
                    }
                }
            };
        }

        if (discoveryFinishedReceiver == null) {
            discoveryFinishedReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                        cancelAnyDeviceSearch(); //does some clean up
                        dispathEvent(new BluetoothDiscoveryEvent(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
                    }
                }
            };
        }

        //---register the broadcast receivers---
        IntentFilter filterFound = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter filterDiscoveryFinished = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        MainActivity.getInstance().registerReceiver(discoverUnpairedDevicesReceiver, filterFound);
        MainActivity.getInstance().registerReceiver(discoveryFinishedReceiver, filterDiscoveryFinished);
        bluetoothAdapter.startDiscovery();
    }

    public List<BluetoothDevice> getDiscoveredDevices() {
        return discoveredDevices;
    }

    public List<BluetoothDevice> listPairedDevices() {
        ErrorReporter.INSTANCE.filelog("BluetoothDiscovery", "listPairedDevices ->");

        try {
            List<PCLDevice> devices = pclReader.getPairedReaders();

            ErrorReporter.INSTANCE.filelog("BluetoothDiscovery", "listPairedDevices -> " + devices.size() + " devices found.");

            for (PCLDevice dev : devices) {
                if (pairedDevices != null && !pairedDevices.contains(dev.getDevice())) {
                    pairedDevices.add(dev.getDevice());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Set<BluetoothDevice> bluetoothDevices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice b : bluetoothDevices) {
                if (pairedDevices != null && !pairedDevices.contains(b)) {
                    pairedDevices.add(b);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        ErrorReporter.INSTANCE.filelog("BluetoothDiscovery", "<- listPairedDevices");

        return pairedDevices;
    }

    private void cancelAnyDeviceSearch() {
        if (bluetoothAdapter == null) return;

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        if (discoverUnpairedDevicesReceiver != null) cleanReceiver(discoverUnpairedDevicesReceiver);
        if (discoveryFinishedReceiver != null) cleanReceiver(discoveryFinishedReceiver);
    }

    private void cleanReceiver(BroadcastReceiver receiver) {
        if (receiver != null) {
            try {
                MainActivity.getInstance().unregisterReceiver(receiver);
            } catch (Exception e) {
                ErrorReporter.INSTANCE.filelog("bluetooth", "Failed to unregister a Bluetooth receiver.", e);
            }
        }
    }

    private void dispathEvent(BluetoothDiscoveryEvent event) {

        for (BluetoothDiscoveryListener listener : listeners) {
            listener.onBluetoothDiscoveryEvent(event);
        }
    }

    public BluetoothDiscovery addListener(BluetoothDiscoveryListener listener) {
        listeners.add(listener);
        if (isIdle())
            listener.onBluetoothDiscoveryEvent(new BluetoothDiscoveryEvent(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        return this;
    }

    public void removeListener(BluetoothDiscoveryListener listener) {
        listeners.remove(listener);
    }

	public BluetoothAdapter getBluetoothAdapter() {
		return bluetoothAdapter;
	}
}
