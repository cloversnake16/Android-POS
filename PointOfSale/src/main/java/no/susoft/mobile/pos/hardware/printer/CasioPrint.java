package no.susoft.mobile.pos.hardware.printer;

import java.util.Arrays;

import android.content.Context;
import jp.co.casio.vx.framework.device.LinePrinter;
import jp.co.casio.vx.framework.device.lineprintertools.LinePrinterDeviceBase;
import jp.co.casio.vx.framework.device.lineprintertools.SerialUp400;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.ReceiptPrintType;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public abstract class CasioPrint extends _PrinterUtils {

    protected int lm = 2;
    protected int rm = 2;
    protected Context context;
    protected StringBuilder receiptData = null;
    protected LinePrinterDeviceBase device;
    protected int qtySpace = 9;
    protected int priceSpace = 9;
    protected int priceExtraSpace = priceSpace + 5;
    protected final int casioWidth = 32;
	public static boolean isBusy;

    @Override
    protected void addLine(String line) {
		receiptData.append(line);
    }

    @Override
    protected void addLine(byte[] cmd) {
        //TODO test
        addLine(Arrays.toString(cmd));
    }

    public synchronized static boolean hasPrinterConnected() {
        try {
            LinePrinter printer = new LinePrinter();
            LinePrinterDeviceBase test = new SerialUp400(SerialUp400.Port.PORT_COM1, SerialUp400.BaudRate.BAUDRATE_9600, SerialUp400.BitLen.BITLEN_8, SerialUp400.ParityBit.PARITYBIT_NON, SerialUp400.StopBit.STOPBIT_1, SerialUp400.FlowCntl.FLOWCNTL_NON);
            int ret = printer.open(test);
            printer.close();
            return ret >= 0;
        } catch (Exception ex) {
            ErrorReporter.INSTANCE.filelog("CasioPrint", "hasPrinterConnected()", ex);
            return false;
        }
    }

    public abstract int print(Object o, ReceiptPrintType isCopy);

    protected abstract void handleMainBody(ReceiptPrintType printType);

    protected void printDivisionLine() {
        addLine(dashLine(width)); //division line filled with -----
    }

    protected String dashLine(int width) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < (width); i++) {
            line.append("-");
        }

        return line.toString() + "\r\n";
    }

    protected void setupPrinter() {
        context = MainActivity.getInstance();
        device = new SerialUp400(SerialUp400.Port.PORT_COM1, SerialUp400.BaudRate.BAUDRATE_9600, SerialUp400.BitLen.BITLEN_8, SerialUp400.ParityBit.PARITYBIT_NON, SerialUp400.StopBit.STOPBIT_1, SerialUp400.FlowCntl.FLOWCNTL_NON);

        int widthArray[];

        try {
            widthArray = device.getLineChars();
            width = widthArray[0];
        } catch (Exception e) {
            ErrorReporter.INSTANCE.filelog("CasioPrint", "setupPrinter() ", e);
            e.printStackTrace();
        }
    }

    protected void feedLines(int i) {
        addLine("\u001b|" + i + "lF");//feed 4line
    }

    protected void cutPaper() {
        receiptData.append("\u001b|fP");// paper cut
    }


    protected void printChainName() {
        addLine(makeCenterizedLineLargeFont(AccountManager.INSTANCE.getAccount().getChain().getName().trim(), width - lm - rm));
    }

    protected void setLargeText() {
        addLine("\u001b|4C");        // Vertical x 2, Holizontal x 2
    }

    protected void setNormalSizeText() {
        addLine("\u001b|N\r\n");     // return to normal
    }

    protected void printCopyHeader() {
        setLargeText();
        addLine(makeCenterizedLineLargeFont(context.getString(R.string.copy), width - lm - rm));
        setNormalSizeText();
    }

    public static String lineFeedsAndCut(int i) {
        return "\u001b|" + i + "lF" + " " + "\u001b|fP";
    }

    public static synchronized boolean isBusy() {
        return isBusy;
    }

    public static synchronized void setBusy(boolean busy) {
        isBusy = busy;
    }
}
