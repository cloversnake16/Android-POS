package no.susoft.mobile.pos.hardware.combi;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import no.susoft.mobile.pos.data.PeripheralProvider;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.ui.activity.util.AppConfig;

/**
 * Printer information.
 */
@SuppressWarnings("unused")
public class mPOP_PrinterSetting {
    public static final String typeBluetoothIF = "BT:";
    public static final String typeUsbIF       = "USB:";
    public static final String typeEthernetIF  = "TCP:";

    public static final String PREF_KEY_DEVICE_NAME  = "pref_key_device_name";
    public static final String PREF_KEY_PRINTER_TYPE = "pref_key_printertype";
    public static final String PREF_KEY_MAC_ADDRESS  = "pref_key_mac_address";

    private Context mContext;

    mPOP_PrinterSetting(Context context) {
        mContext = context;
    }

    public void write(String portName, String macAddress) {
        write(portName, macAddress, "");
    }

    public void write(String portName, String macAddress, String printerType) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        prefs.edit()
                .putString(PREF_KEY_DEVICE_NAME, portName)
                .putString(PREF_KEY_MAC_ADDRESS, macAddress)
                .putString(PREF_KEY_PRINTER_TYPE, printerType)
                .apply();
    }

    public String getPortName() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String macAddress = prefs.getString(PREF_KEY_MAC_ADDRESS, "");

        if(AppConfig.getState().getPrinterProviderOrdinal() == PeripheralProvider.STAR.ordinal()) {
            macAddress = "BT:" + AppConfig.getState().getPrinterName();
            Log.i("vilde", "It hit STAR printer name!");
        }

        if(!AppConfig.getState().getPrinterIp().trim().isEmpty()) {
            macAddress = "BT:" + AppConfig.getState().getPrinterIp();
            Log.i("vilde", "It hit STAR IP, used as MAC!");
        }

        //Default fallback
        if(macAddress.isEmpty()) {
            Log.i("vilde", "It hit STAR fallback name");
            macAddress = "BT:" + "STAR mPOP-H1004";
        }

		ErrorReporter.INSTANCE.filelog("STAR MPOP macAddress = " + macAddress);

        //FROM DOCS:
        // --- Bluetooth ---
        // It can communication used device name(Ex.BT:Star Micronics) at bluetooth.
        // If android device has paired two same name device, can't choose destination target.
        // If used Mac Address(Ex. BT:00:12:3f:XX:XX:XX) at Bluetooth, can choose destination target.
        if (macAddress.startsWith(typeBluetoothIF)) {
            return macAddress;
        }

        return prefs.getString(PREF_KEY_DEVICE_NAME, "");
    }

    public String getDeviceName() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return prefs.getString(PREF_KEY_DEVICE_NAME, "");
    }

    public String getMacAddress() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return prefs.getString(PREF_KEY_MAC_ADDRESS, "");
    }

    public String getPrinterType() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        if(prefs.getString(PREF_KEY_PRINTER_TYPE, "").isEmpty()) {
            return typeBluetoothIF;
        }
        return prefs.getString(PREF_KEY_PRINTER_TYPE, "");
    }

    public Context getContext() {
        return mContext;
    }
}
