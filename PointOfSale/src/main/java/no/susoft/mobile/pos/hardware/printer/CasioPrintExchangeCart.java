package no.susoft.mobile.pos.hardware.printer;

import android.content.Context;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.data.ReceiptPrintType;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class CasioPrintExchangeCart extends CasioPrintOrder {
	
	protected Order order = null;
	Context context;
	
	public CasioPrintExchangeCart(Order order) {
		this.order = order;
		width = casioWidth;
		context = MainActivity.getInstance();
		receiptData = new StringBuilder();
	}
	
	@Override
    protected String makeReceipt(final Order order, ReceiptPrintType printType, StringBuilder receiptData) {
		try {
			this.order = order;
			this.receiptData = receiptData;
			receiptData.append("\u001B|N");
			setLargeText();
			chain();
			setNormalSizeText();
			feedLines(2);
			datetime(context, order.getDate());
			shopAndSalesPerson(context, order.getShopId(), order.getSalesPersonId());
			orderNumber(context, order.getAlternativeId());
			customer(context, order.getCustomer());
			printDivisionLine();
			feedLines(1);
			addLine(makeCenterizedLine(context.getString(R.string.exchangecertificate), width));
			feedLines(2);
			printBarCode(String.valueOf(order.getAlternativeId()), "1A1");
			feedLines(4);
			cutPaper();
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog("Error in makeReceipt", "", e);
		}

        return receiptData.toString();
    }
	
	
}
