package no.susoft.mobile.pos.hardware.printer;

import no.susoft.mobile.pos.data.OrderPaymentResponse;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class IPPrintOrderingWide extends IPPrintOrderWide {

	public IPPrintOrderingWide(final OrderPaymentResponse response , boolean isCopy) {
        this.order = response.getOrder();
        this.isCopy = isCopy;
        this.context = MainActivity.getInstance();
        width = wideWidth;
    }

    @Override
    protected void handleHeaderPrint() {
		if (isCopy) {
			copy(context);
		} else {
			chain();
		}
        datetime(context, order.getDate());
        shopAndSalesPerson(context, order.getShopId(), order.getSalesPersonId());
		orderNumber(context, order.getParkedId());
		customer(context, order.getCustomer());
		printExtraTitle();
        printRightLineOfLength('-', width);
    }
    
	@Override
    protected String getOrderBarcodeString() {
		return "1T1" + order.getShopId() + order.getParkedId();
    }

}
