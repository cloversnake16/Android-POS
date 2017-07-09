package no.susoft.mobile.pos.hardware.combi;

import java.util.ArrayList;

import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class mPOPPrintExchangeCart extends mPOPPrintOrderSlim {

    public mPOPPrintExchangeCart(final Order order) {
		this(order, false);
    }

    public mPOPPrintExchangeCart(final Order order, boolean isCopy) {
        width = 32;
        this.order = order;
		this.isCopy = isCopy;
		String barcode = "1A1" + order.getAlternativeId();
		Communication.sendCommands(convertArrayListToBytes(PreparePrint(order), barcode), Star_mPOP.getPort(), MainActivity.getInstance());
    }
    
    @Override
    protected ArrayList<Object> PreparePrint(Order order) {
        this.order = order;
        context = MainActivity.getInstance();
        output = new ArrayList<>();
        try {
			printLargeText();
			chain();
			printNormalText();
			datetime(context, order.getDate());
			shop(context, order.getShopId());
			salesPerson(context, order.getSalesPersonId());
			orderNumber(context, order.getAlternativeId());
			customer(context, order.getCustomer());
			printRightLineOfLength('-', width);
			printEmptyLine();
			addLine(makeCenterizedLine(context.getString(R.string.exchangecertificate), width));
			printEmptyLine();
			printEmptyLine();
        } catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
        }
        return output;
    }
}
