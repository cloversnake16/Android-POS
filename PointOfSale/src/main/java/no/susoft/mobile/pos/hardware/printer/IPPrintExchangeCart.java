package no.susoft.mobile.pos.hardware.printer;

import java.util.ArrayList;

import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class IPPrintExchangeCart extends IPPrintOrderWide {

    public IPPrintExchangeCart(Order order) {
		super();
        this.order = order;
        this.isCopy = false;
    }

    @Override
    protected ArrayList<Object> PreparePrint(Order order) {
        this.order = order;
        context = MainActivity.getInstance();
        output = new ArrayList<>();
        try {
			setLargeText();
			addLine(makeCenterizedLine(AccountManager.INSTANCE.getSavedShopName(), width));
			setNormalSizeText();
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
			for(byte[] b : printBarcode("1A1" + order.getAlternativeId())) {
				output.add(b);
			}
			addLine(makeCenterizedLine("1A1" + order.getAlternativeId(), width));
			
        } catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
        }
        return output;
    }
}
