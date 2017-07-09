package no.susoft.mobile.pos.hardware.printer;

import java.util.ArrayList;

import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class BluetoothPrintExchangeCart extends BluetoothPrintOrderWide {
	
	public BluetoothPrintExchangeCart(Order order) {
        width = wideWidth;
        this.context = MainActivity.getInstance();
        output = new ArrayList<>();
		this.order = order;
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
			handleOrderLinesPrint();
        } catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
        }
        return output;
    }
}
