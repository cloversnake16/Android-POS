package no.susoft.mobile.pos.hardware.combi;

import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class mPOPPrintOrdering extends mPOPPrintOrderSlim {

    public mPOPPrintOrdering(final Order order) {
		this(order, false);
    }

    public mPOPPrintOrdering(final Order order, boolean isCopy) {
        width = 32;
        this.order = order;
		this.isCopy = isCopy;
		String barcode = "1T1" + order.getShopId() + String.valueOf(order.getParkedId());
		Communication.sendCommands(convertArrayListToBytes(PreparePrint(order), barcode), Star_mPOP.getPort(), MainActivity.getInstance());
    }
    
    @Override
    protected void handleHeaderPrint() {
        printLargeText();
		if (isCopy) {
			copy(context);
		} else {
			chain();
		}
        printNormalText();
		datetime(context, order.getDate());
        shop(context, order.getShopId());
        salesPerson(context, order.getSalesPersonId());
		orderNumber(context, order.getParkedId());
        customer(context, order.getCustomer());
		printExtraTitle();
        printRightLineOfLength('-', width);
    }
}
