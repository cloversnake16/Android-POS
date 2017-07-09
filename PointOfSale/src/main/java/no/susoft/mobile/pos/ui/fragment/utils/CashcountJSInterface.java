package no.susoft.mobile.pos.ui.fragment.utils;

import java.io.Serializable;

import android.webkit.JavascriptInterface;
import no.susoft.mobile.pos.data.PeripheralProvider;
import no.susoft.mobile.pos.hardware.ConnectionManager;
import no.susoft.mobile.pos.hardware.combi.mPOPPrintCashcount;
import no.susoft.mobile.pos.hardware.printer.*;
import no.susoft.mobile.pos.ui.activity.util.AppConfig;

public class CashcountJSInterface implements Serializable {

	@JavascriptInterface
	public void printCashcountReceipt(final String data) {

        int printerProvider = AppConfig.getState().getPrinterProviderOrdinal();
        if (printerProvider == PeripheralProvider.CASIO.ordinal()) {
            // region CASIO
			if (CasioPrint.hasPrinterConnected()) {
				CasioPrintCashcount po = new CasioPrintCashcount();
				po.print(data);
			}
			// endregion
        } else if (printerProvider == PeripheralProvider.BIXOLON.ordinal()) {
			// region BIXOLON
			if (AppConfig.getState().getPrinterIp().isEmpty()) {
				ConnectionManager.getInstance().execute(new Runnable() {
					@Override
					public void run() {
						BluetoothPrintCashcount bp = new BluetoothPrintCashcount();
						bp.print(bp.makeReport(data));
					}
				});
			} else {
				ConnectionManager.getInstance().execute(new BixolonPrinterJob() {
					@Override
					public int print() {
						IPPrintCashcount pc = new IPPrintCashcount();
						return pc.printIP(AppConfig.getState().getPrinterName(), AppConfig.getState().getPrinterIp(), data);
					}
				});
			}
			// endregion
		} else if (printerProvider == PeripheralProvider.STAR.ordinal()) {
			// region STAR
			ConnectionManager.getInstance().execute(new Runnable() {
				@Override
				public void run() {
					new mPOPPrintCashcount(data);
				}
			});
			// endregion
		}
	}
}
