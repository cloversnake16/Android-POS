package no.susoft.mobile.pos.usbdisplay;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

import no.susoft.mobile.pos.ui.activity.MainActivity;

/**
 * Created by Sami on 12/15/2016.
 */

public class UsbHostService extends Service {
    private static String TAG = "CommunicationRunnable";
    private final AtomicBoolean keepThreadAlive = new AtomicBoolean(true);
    private static UsbHostService instance;
    private CommunicationRunnable communicationRunnable;

    private UsbDevice device;

    public static UsbHostService getInstance() {
        return instance;
    }

    public class LocalBinder extends Binder {

        UsbHostService getService() {

            return UsbHostService.this;
        }
    }

    @Override
    public void onCreate() {
        instance = this;

        // Register Receiver
        UsbReceiver mUsbReceiver = new UsbReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbDisplayConstants.ACTION_DEVICE_DETACHED);
        filter.setPriority(999);
        registerReceiver(mUsbReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent != null){

            if(device == null){

                device = intent.getParcelableExtra(UsbDisplayConstants.DEVICE_EXTRA_KEY);

                if(device != null){

                    communicationRunnable = new CommunicationRunnable();
                    new Thread(communicationRunnable).start();
                }
            }
        }

        return START_STICKY;
    }


    @Override
    public void onDestroy() {

        super.onDestroy();
        keepThreadAlive.set(false);
        if (MainActivity.getInstance() != null) {
            MainActivity.getInstance().displayUsbConnectionState(false);
        }
        stopHostService();

    }


    @Override
    public IBinder onBind(Intent intent) {

        return mBinder;
    }


    private final IBinder mBinder = new LocalBinder();

    private class CommunicationRunnable implements Runnable {

        private UsbDeviceConnection connection;
        UsbEndpoint endpointIn;
        UsbEndpoint endpointOut;


        @Override
        public void run() {

            final UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

            endpointIn = null;
            endpointOut = null;

            final UsbInterface usbInterface = device.getInterface(0);

            for (int i = 0; i < device.getInterface(0).getEndpointCount(); i++) {

                final UsbEndpoint endpoint = device.getInterface(0).getEndpoint(i);
                if (endpoint.getDirection() == UsbConstants.USB_DIR_IN) {
                    endpointIn = endpoint;
                }
                if (endpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                    endpointOut = endpoint;
                }

            }

            if (endpointIn == null) {
                Log.i(TAG, "Input Endpoint not found");
                return;
            }

            if (endpointOut == null) {
                Log.i(TAG, "Output Endpoint not found");
                return;
            }

            connection = usbManager.openDevice(device);

            if (connection == null) {
                Log.i(TAG, "Could not open device");
                return;
            }

            final boolean claimResult = connection.claimInterface(usbInterface, true);

            if (!claimResult) {

                Log.i(TAG, "Could not claim device");
            }
            else {

                if (MainActivity.getInstance() != null) {
                    MainActivity.getInstance().displayUsbConnectionState(true);
                }


                final byte buff[] = new byte[UsbDisplayConstants.BUFFER_SIZE_IN_BYTES];

                while (keepThreadAlive.get()) {

                    final int bytesTransferred = connection.bulkTransfer(endpointIn, buff, buff.length, UsbDisplayConstants.USB_TIMEOUT_IN_MS);

                    if (bytesTransferred > 0) {
                        if (MainActivity.getInstance() != null) {
                            MainActivity.getInstance().receiveMessageFromSecondaryUsbDisplay(new String(buff, 0, bytesTransferred));
                        }
                    }

                }
            }

            if (MainActivity.getInstance() != null) {
                MainActivity.getInstance().displayUsbConnectionState(false);
            }

            connection.releaseInterface(usbInterface);
            connection.close();
            stopHostService();
        }

        public synchronized void sendMessageToDevice(String msg) {
            final byte[] sendBuff = msg.toString().getBytes();
            connection.bulkTransfer(endpointOut, sendBuff, sendBuff.length, UsbDisplayConstants.USB_TIMEOUT_IN_MS);
        }

    }


    public void sendMessage(final String msg) {
        if (communicationRunnable != null) {
            communicationRunnable.sendMessageToDevice(msg);
        }
    }

    class UsbReceiver extends BroadcastReceiver {

        @Override
        public  void onReceive(Context context, Intent intent){

            String action = intent.getAction();

            if(action.equals(UsbDisplayConstants.ACTION_DEVICE_DETACHED)){

                if (MainActivity.getInstance() != null) {
                    MainActivity.getInstance().displayUsbConnectionState(false);
                }
                stopHostService();
            }
        }
    }


    private void stopHostService(){
        try {
            this.stopSelf();
        } catch (Exception e) {
            try {
                this.stopSelf();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}
