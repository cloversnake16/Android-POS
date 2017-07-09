package no.susoft.mobile.pos.hardware.printer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.widget.Toast;
import jp.co.casio.vx.framework.device.LinePrinter;
import jp.co.casio.vx.framework.device.lineprintertools.LinePrinterDeviceBase;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Product;
import no.susoft.mobile.pos.data.ReceiptPrintType;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class CasioPrintSupply extends CasioPrint {

    protected static boolean hasPrintedReturns = false;
    protected ArrayList<Product> products = null;
    protected Date from = null;
    protected Date to = null;
    IntentFilter intentFilter = new IntentFilter("jp.co.casio.vx.regdevice.comm.POWER_RECOVERY");
    int boardNo = 0;
    Context context;
    private BroadcastReceiver intentReceiver;

    public CasioPrintSupply() {
        width = casioWidth;
        receiptData = new StringBuilder();
    }

    public void print(Object o, Date from, Date to) {
        context = MainActivity.getInstance();
        initiatePrint((ArrayList<Product>) o, from, to);
    }

    protected void initiatePrint(final ArrayList<Product> products, Date from, Date to) {
        this.products = products;
        this.from = from;
        this.to = to;
        try {
            setupPrinter();
            sendReceiptToPrinter(device, makeReport());
        } catch (Exception ex) {
            ErrorReporter.INSTANCE.filelog(ex);
            Toast.makeText(MainActivity.getInstance(), R.string.error_printing_receipt, Toast.LENGTH_LONG).show();
        }
    }

    protected void printDivisionLine() {
        receiptData.append(dashLine(width)); //division line filled with -----
    }

    protected String makeReport() {
		try {
			receiptData = new StringBuilder();
			receiptData.append("\u001B|N");

			setLargeText();
			addLine(makeCenterizedLineLargeFont(context.getString(R.string.supply_report), width));
			setNormalSizeText();
			feedLines(1);
			addLine(makeCenterizedLine(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(from) + " - " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(to), width));
			printDivisionLine();

			String categoryId = null;
			for (Product product : products) {
				if (categoryId == null || !product.getCategoryId().equals(categoryId)) {
					categoryId = product.getCategoryId();
					receiptData.append(makeLine(product.getCategoryName(), "", width, lm, rm));
				}
				receiptData.append(makeLine(product.getName(), product.getStockQty().toString(), width, lm, rm));
			}

			feedLines(4);
			cutPaper();
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog("Error in makeReceipt", "", e);
		}

        return receiptData.toString();
    }

	@Override
	public int print(Object o, ReceiptPrintType isCopy) {
		return 0;
	}

	@Override
	protected void handleMainBody(ReceiptPrintType printType) {

	}

	protected void sendReceiptToPrinter(final LinePrinterDeviceBase device, final String receiptData) {
        LinePrinter printer;
        device.setMulticharMode(LinePrinterDeviceBase.PAGEMODE_USER);
        try {
            printer = new LinePrinter();
			printer.open(device);
            printer.printNormal(receiptData);
            printer.close();
        } catch (Exception e) {
            ErrorReporter.INSTANCE.filelog("CasioPrintSupply", "sendReceiptToPrinter", e);
        }
	}
}
