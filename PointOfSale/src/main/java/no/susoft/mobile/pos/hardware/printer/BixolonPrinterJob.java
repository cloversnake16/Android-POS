package no.susoft.mobile.pos.hardware.printer;

import android.widget.Toast;
import jpos.JposConst;
import jpos.POSPrinter;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.SusoftPOSApplication;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public abstract class BixolonPrinterJob extends Thread implements PrinterJob {

	public static int PRINTER = 0;
	public static int KITCHEN_PRINTER = 1;
	public static int BAR_PRINTER = 2;
	private int printerType = PRINTER;

	public BixolonPrinterJob() {
	}

	public BixolonPrinterJob(int printerType) {
		this.printerType = printerType;
	}

	@Override
	public void run() {
		try {
			int result = 0;
			int counter = 0;
			
			POSPrinter printer = null;
			if (printerType == KITCHEN_PRINTER) {
				printer = MainActivity.getInstance().getKitchenPrinter();
			} else if (printerType == BAR_PRINTER) {
				printer = MainActivity.getInstance().getBarPrinter();
			} else {
				printer = MainActivity.getInstance().getPOSPrinter();
			}
			
			do {
				if (printer.getState() != JposConst.JPOS_S_CLOSED) {
					sleep(500);
				} else {
					result = print();
				}
				counter++;
			} while (result != 0 && counter < 20);

			if (counter == 20 && result != 0) {
				MainActivity.getInstance().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(SusoftPOSApplication.getContext(), MainActivity.getInstance().getResources().getString(R.string.no_printer_connected), Toast.LENGTH_LONG).show();
					}
				});
			}

			do {
				sleep(500);
			} while (printer.getState() != JposConst.JPOS_S_IDLE && printer.getState() != JposConst.JPOS_S_CLOSED);
			
			if (printer.getState() == JposConst.JPOS_S_IDLE) {
				printer.release();
			}
			printer.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
