package no.susoft.mobile.pos.hardware.combi;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.starioextension.starioextmanager.StarIoExtManager;
import com.starmicronics.starioextension.starioextmanager.StarIoExtManagerListener;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.data.Prepaid;
import no.susoft.mobile.pos.data.ReceiptPrintType;
import no.susoft.mobile.pos.hardware.printer.Printer;

import static no.susoft.mobile.pos.hardware.combi.mPOP_PrinterFunctions.createCommandsOpenCashDrawer;

public class Star_mPOP implements Printer {

    static StarIoExtManagerListener mStarListener;
    static StarIoExtManager mStarIoExtManager;

    public Star_mPOP(final Activity activity) {
        mPOP_PrinterSetting setting = new mPOP_PrinterSetting(activity);
        mStarIoExtManager = new StarIoExtManager(StarIoExtManager.Type.Standard, setting.getPortName(), setting.getPrinterType(), 3000, activity); // 10000mS!!!
        mStarIoExtManager.setListener(mStarListener = new StarIoExtManagerListener() {
            @Override
            public void didPrinterImpossible() {
                super.didPrinterImpossible();
            }

            @Override
            public void didPrinterOnline() {
                super.didPrinterOnline();
            }

            @Override
            public void didPrinterOffline() {
                super.didPrinterOffline();
            }

            @Override
            public void didPrinterPaperReady() {
                super.didPrinterPaperReady();
            }

            @Override
            public void didPrinterPaperNearEmpty() {
                super.didPrinterPaperNearEmpty();
            }

            @Override
            public void didPrinterPaperEmpty() {
                super.didPrinterPaperEmpty();
            }

            @Override
            public void didPrinterCoverOpen() {
                super.didPrinterCoverOpen();
            }

            @Override
            public void didPrinterCoverClose() {
                super.didPrinterCoverClose();
            }

            @Override
            public void didCashDrawerOpen() {
                super.didCashDrawerOpen();
            }

            @Override
            public void didCashDrawerClose() {
                super.didCashDrawerClose();
            }

            @Override
            public void didBarcodeReaderImpossible() {
//                Log.i("vilde", "Barcode Reader Impossible.");
            }

            @Override
            public void didBarcodeReaderConnect() {
//                Log.i("vilde", "Barcode Reader Connect.");
            }

            @Override
            public void didBarcodeReaderDisconnect() {
//                Log.i("vilde", "Barcode Reader Disconnect.");
            }

            @Override
            public void didBarcodeDataReceive(byte[] bytes) {
                super.didBarcodeDataReceive(bytes);
            }

            @Override
            public void didAccessoryConnectSuccess() {
                super.didAccessoryConnectSuccess();
            }

            @Override
            public void didAccessoryConnectFailure() {
                super.didAccessoryConnectFailure();
            }

            @Override
            public void didAccessoryDisconnect() {
                super.didAccessoryDisconnect();
            }
        });
        mStarListener.didAccessoryDisconnect();
        AsyncTask<Void, Void, Boolean> asyncTask = new AsyncTask<Void, Void, Boolean>() {
            @Override protected void onPreExecute() {  }
            @Override protected Boolean doInBackground(Void... params) { return mStarIoExtManager.connect(); }
            @Override protected void onPostExecute(Boolean result) {
                if (! result) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                    dialogBuilder.setTitle("Communication Result");
                    dialogBuilder.setMessage("Fail to Open Port.");
                    dialogBuilder.setPositiveButton("OK", null);
                    dialogBuilder.show(); }
                else {
                }

            } };
        asyncTask.execute();
    }


    public static StarIOPort getPort() {
        return mStarIoExtManager.getPort();
    }

    public static void openDrawer(Context context) {
		Communication.sendCommands(createCommandsOpenCashDrawer(), mStarIoExtManager.getPort(), context);
    }

    // todo Need to be implemented
    // adding placeholders here to make it possible to use this class in PrinterFactory

    @Override
    public void printOrder(Order order, ReceiptPrintType original) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printPrepaid(List<Prepaid> giftCards) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printPrepaidJoined(Order order, List<Prepaid> giftCards) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printReturn(Order order) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cutPaper() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printLine(String s) {
        throw new UnsupportedOperationException();
    }
}
