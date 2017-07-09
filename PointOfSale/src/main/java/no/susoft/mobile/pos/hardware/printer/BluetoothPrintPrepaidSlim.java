package no.susoft.mobile.pos.hardware.printer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Prepaid;
import no.susoft.mobile.pos.ui.activity.MainActivity;

/**
 * Created by VSB on 18/12/2015.
 */
public class BluetoothPrintPrepaidSlim extends BluetoothPrintOrderSlim {

    private final List<Prepaid> prepaids;

    public BluetoothPrintPrepaidSlim(List<Prepaid> prepaids) {
        this.context = MainActivity.getInstance();
        width = slimWidth;
        this.prepaids = prepaids;
        for(Prepaid p : prepaids) {
            print(makePrepaid(p));
        }
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

    protected void printExtraTitle(Prepaid prepaid) {
        printLargeText();
        if (prepaid.getType().equalsIgnoreCase("C")) {
            addLine(makeCenterizedLineLargeFont(context.getString(R.string.credit_voucher), width));
        } else {
            addLine(makeCenterizedLineLargeFont(context.getString(R.string.gift_card), width));
        }
        printNormalText();

    }

	protected void printExtraTitle() {

	}

	protected void printExtraFooter() {

	}
}
