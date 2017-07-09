package no.susoft.mobile.pos.display.asyncTasks.asyncBT;

import java.io.IOException;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.display.util.DisplayConstants;

/**
 * Created by Sami on 12/22/2016.
 */


public class AcceptBTSocket extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "AcceptBTSocket";
    private Context mContext;

    private BluetoothServerSocket mServerSocket;
    private boolean isAccepting;

    public AcceptBTSocket(Context context){
        mContext = context;
        isAccepting = true;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        BluetoothServerSocket tmp = null;

        try {
            tmp = bluetoothAdapter.
                    listenUsingRfcommWithServiceRecord(
                            "Susoft-BT-Display", java.util.UUID.fromString(DisplayConstants.UUID)
                    );
        } catch (IOException ie) {
            try {
                tmp = bluetoothAdapter.
                        listenUsingRfcommWithServiceRecord(
                                "Susoft-BT-Display", java.util.UUID.fromString(DisplayConstants.UUID)
                        );
            } catch (IOException ie1) {
                Log.v(TAG, "doInBackground: Failed to set up Accept Thread");
            }
        }

        mServerSocket = tmp;

    }

    @Override
    protected Void doInBackground(Void... params) {
        BluetoothSocket btSocket;

        while (isAccepting) {
            btSocket = null;
            try {
                btSocket = mServerSocket.accept();
            } catch (IOException ie) {
                Log.v(TAG, "doInBackground: server socket accept failed");
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException ite) {
                Log.v(TAG, "doInBackground: thread sleep failed");
            }

            if (btSocket != null) {
                if (MainActivity.getInstance() != null) {
                    MainActivity.getInstance().manageBTSocket(btSocket);
                    try {
                        isAccepting = false;
                        mServerSocket.close();
                    } catch (IOException e) {
                        Log.v(TAG, "doInBackground: server socket close failed");
                    }
                }
            }
        }
        return null;

    }

    @Override
    protected void onPostExecute(Void result) {
        Log.v(TAG, "onPostExecute");
        super.onPostExecute(result);
    }

    @Override
    protected void onCancelled() {
        try {
            isAccepting = false;
            mServerSocket.close();
        } catch (IOException e) {
            Log.v(TAG, "doInBackground: server socket close failed");
        }
        super.onCancelled();
    }

}
