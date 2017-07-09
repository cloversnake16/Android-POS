package no.susoft.mobile.pos.hardware.printer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Prepaid;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class BluetoothPrintPrepaidWide extends BluetoothPrintOrderWide {

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private Prepaid prepaid;

    public BluetoothPrintPrepaidWide(Prepaid prepaid) {
        width = wideWidth;
        context = MainActivity.getInstance();
        this.prepaid = prepaid;
        try {
            output = new ArrayList<>();
            print(makePrepaid(prepaid));
			closeBT();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    protected ArrayList<Object> makePrepaid(final Prepaid prepaid) {
        chain();
        printPrepaidDate(prepaid);                    //DATE
        time(context, prepaid.getIssuedDate().getTime());
        shopAndSalesPerson(context, prepaid.getShopId(), prepaid.getSalespersonId());
        printRightLineOfLength('-', width);
        handleMainBodyPrint(prepaid);
        return output;
    }

    protected void printPrepaidDate(Prepaid prepaid) {
        String date;
        if (context == null) {
            context = MainActivity.getInstance();
        }
        if (prepaid.getLastUsedDate() != null) {
            date = context.getString(R.string.date) + ":" + makeSpace(1) + new SimpleDateFormat("dd/MM/yyyy").format(prepaid.getLastUsedDate());
        } else {
            date = context.getString(R.string.date) + ":" + makeSpace(1) + new SimpleDateFormat("dd/MM/yyyy").format(prepaid.getIssuedDate());
        }
        addLine(makeLine(date, "", width, lm, rm));
    }

    protected void handleMainBodyPrint(Prepaid prepaid) {
        printExtraTitle(prepaid);
        printPrepaidNumber(prepaid);
        printEmptyLine();
        for(byte[] b : printBarcode(getPrepaidBarcodeString(prepaid))) {
            output.add(b);
        }
        printEmptyLine();
        printPrepaidAmount(prepaid);
        printPrepaidValidityDate(prepaid.getDueDate());

    }

    protected void printExtraTitle(Prepaid prepaid) {
        printLargeText();
        if (prepaid.getType().equalsIgnoreCase("C")) {
            addLine(makeCenterizedLineLargeFont(context.getString(R.string.credit_voucher), width));
        } else {
            addLine(makeCenterizedLineLargeFont(context.getString(R.string.gift_card), width));
        }
        printNormalText();

    }

	protected void printExtraHeader() {

	}

	protected void printExtraFooter() {

	}
}
