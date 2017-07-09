package no.susoft.mobile.pos.hardware.combi;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import com.starmicronics.starioextension.starioextmanager.StarIoExtManager;
import com.starmicronics.starioextension.starioextmanager.StarIoExtManagerListener;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class mPOPScanner {

    private StarIoExtManager mStarIoExtManager;
    private android.content.Context context;

    public mPOPScanner(Context context) {
        this.context = context;

        mPOP_PrinterSetting setting = new mPOP_PrinterSetting(context);

        mStarIoExtManager = new StarIoExtManager(StarIoExtManager.Type.OnlyBarcodeReader, setting.getPortName(), setting.getPrinterType(), 3000, context);     // 10000mS!!!
        mStarIoExtManager.setListener(mStarIoExtManagerListener);

        starIoExtManagerConnect();
    }

    private void starIoExtManagerConnect() {
        AsyncTask<Void, Void, Boolean> asyncTask = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                Log.i("vilde", "onPreExecute");
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                //mStarIoExtManager.disconnect();
                return mStarIoExtManager.connect();
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (!result) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

                    dialogBuilder.setTitle("Communication Result");
                    dialogBuilder.setMessage("failure.\nScanner is offline.");
                    dialogBuilder.setPositiveButton("OK", null);

                    dialogBuilder.show();
                } else {
                    Log.i("vilde", "Connected mpop!");
                }
            }
        };

        asyncTask.execute();
    }

    private final StarIoExtManagerListener mStarIoExtManagerListener = new StarIoExtManagerListener() {
        @Override
        public void didBarcodeDataReceive(byte[] data) {
            Log.i("vilde", "Received data from barcode scanner");
            Log.i("vilde", "Length: " + data.length);
            Log.i("vilde", "Bytes: " + Arrays.toString(data));

            try {

                byte[] newBytes = new byte[data.length-2];

                for(int i = 0; i < newBytes.length; i++) {
                    newBytes[i] = data[i];
                }
                String decoded = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    decoded = new String(newBytes, StandardCharsets.UTF_8);
                } else {
                    decoded = new String(newBytes, "UTF-8");
                }

                Log.i("vilde", "String: " + decoded);
                Log.i("vilde", "String length: " + decoded.length());
                MainActivity.getInstance().writeToCurrentInput(decoded);
                MainActivity.getInstance().dispatchEnterDown();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void didBarcodeReaderImpossible() {
//            Log.i("vilde", "Barcode Reader Impossible.");
        }

        @Override
        public void didBarcodeReaderConnect() {
//            Log.i("vilde", "Barcode Reader Connect.");
        }

        @Override
        public void didBarcodeReaderDisconnect() {
            Log.i("vilde", "Barcode Reader Disconnect.");

        }

        @Override
        public void didAccessoryConnectSuccess() {
            Log.i("vilde", "Accessory Connect.");
        }

        @Override
        public void didAccessoryConnectFailure() {
            Log.i("vilde", "Check the device.(Power and Bluetooth pairing)\nThen touch up the Refresh button.");
        }

        @Override
        public void didAccessoryDisconnect() {
            Log.i("vilde", "Accessory Disconnect.");
        }
    };
}
