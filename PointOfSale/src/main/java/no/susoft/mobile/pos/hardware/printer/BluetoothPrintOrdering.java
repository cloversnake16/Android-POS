package no.susoft.mobile.pos.hardware.printer;

import java.util.ArrayList;

import no.susoft.mobile.pos.data.OrderPaymentResponse;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class BluetoothPrintOrdering extends BluetoothPrintOrderWide {

    public BluetoothPrintOrdering(final OrderPaymentResponse response, boolean isCopy) {
        width = wideWidth;
        this.order = response.getOrder();
		this.isCopy = isCopy;
        this.context = MainActivity.getInstance();
        output = new ArrayList<>();
    }
    
	@Override
    protected void handleHeaderPrint() {
        chain();
        datetime(context, order.getDate());
		shopAndSalesPerson(context, order.getShopId(), order.getSalesPersonId());
		orderNumber(context, order.getParkedId());
		customer(context, order.getCustomer());
		printExtraTitle();
        printRightLineOfLength('-', width);
    }
    
	@Override
	protected String getOrderBarcodeString() {
		if (MainActivity.getInstance().isConnected()) {
			return "1T1" + order.getShopId() + order.getId();
		}
		return "1Q1" + order.getAlternativeId();
	}

}
