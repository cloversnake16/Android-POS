package no.susoft.mobile.pos.hardware.printer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.data.Prepaid;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class IPPrintPrepaid extends IPPrintOrderWide {

    private Prepaid prepaid;

    public IPPrintPrepaid(Prepaid prepaid) {
        this.prepaid = prepaid;
        this.context = MainActivity.getInstance();
        width = wideWidth;
    }

    @Override
	protected ArrayList<Object> PreparePrint(Order order) {
		output = new ArrayList<>();
        chain();
        printPrepaidDate(prepaid);
        time(context, prepaid.getIssuedDate().getTime());
        shopAndSalesPerson(context, prepaid.getShopId(), prepaid.getSalespersonId());
        printRightLineOfLength('-', width);

        handleMainBodyPrint(prepaid);
		return output;
    }

    protected void printPrepaidDate(Prepaid prepaid) {
        String date;
        if (prepaid.getLastUsedDate() != null) {
            date = context.getString(R.string.date) + ":" + makeSpace(1) + new SimpleDateFormat("dd/MM/yyyy").format(prepaid.getLastUsedDate());
        } else {
            date = context.getString(R.string.date) + ":" + makeSpace(1) + new SimpleDateFormat("dd/MM/yyyy").format(prepaid.getIssuedDate());
        }
        addLine(makeLine(date, "", width, lm, rm));
    }

    protected void handleMainBodyPrint(Prepaid prepaid) {
        printExtraTitle(prepaid);
		printEmptyLine();
        printPrepaidNumber(prepaid);
        printEmptyLine();

        for(byte[] b : printBarcode(getPrepaidBarcodeString(prepaid))) {
            output.add(b);
        }

        printEmptyLine();
        printPrepaidAmount(prepaid);
        printPrepaidValidityDate(prepaid.getDueDate());

    }

    protected void handleExtraBottomPrint() {
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

	protected void printExtraFooter() {

	}

	protected void doExtraAfterSendData() {

	}

}
