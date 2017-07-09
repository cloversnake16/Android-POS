package no.susoft.mobile.pos.hardware.printer;

import android.content.Context;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.data.ReceiptPrintType;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class CasioPrintOrdering extends CasioPrintOrder {

    protected Order order = null;
    Context context;

    public CasioPrintOrdering(Order order) {
		this.order = order;
        width = casioWidth;
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
			orderNumber(context, order.getParkedId());
			customer(context, order.getCustomer());
			if (order.getTable() > 0) {
				table(context);
			}
			handleExtraTitle(receiptData, width);
			printDivisionLine();
			handleMainBody(printType);
			handleExtraBottomText(width);

			feedLines(4);
			cutPaper();
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog("Error in makeReceipt", "", e);
		}

        return receiptData.toString();
    }
    
    protected void handleMainBody(ReceiptPrintType printType) {
        handleOrderLines();
		handleExtraBottom();
        feedLines(1);
		printBarCode(String.valueOf(order.getParkedId()), "1T1");
    }
    
	@Override
    protected void printBarCode(String number, String prefix) {
		
		if (MainActivity.getInstance().isConnected()) {
			prefix = "1T1";
		} else {
			prefix = "1Q1";
		}
		
        StringBuilder barcode = new StringBuilder();
        barcode.append("\u001B|");
        barcode.append(String.valueOf(24 + number.length()));
        barcode.append("Rs109h100w400a-2t-13d" + prefix);
        barcode.append(number);
        barcode.append("e");
        receiptData.append(barcode.toString());
    }
}
