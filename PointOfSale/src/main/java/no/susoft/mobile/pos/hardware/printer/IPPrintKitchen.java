package no.susoft.mobile.pos.hardware.printer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import jpos.POSPrinter;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.data.OrderLine;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.activity.util.AppConfig;

public class IPPrintKitchen extends IPPrint {

	public static String DESTINATION_KITCHEN = "K";
	public static String DESTINATION_BAR = "B";

    protected static boolean returnsPrinted = false;
    protected Order order;
    protected String destination;

    public IPPrintKitchen() {
        this.context = MainActivity.getInstance();
		width = wideWidth;
    }

    protected POSPrinter getPOSPrinter() {
		if (destination.equals(DESTINATION_BAR)) {
			return MainActivity.getInstance().getBarPrinter();
		}
        return MainActivity.getInstance().getKitchenPrinter();
    }

	public int print(final Order order, String destination) {
        this.order = order;
        this.destination = destination;
		if (orderNeedToBePrinted(order, destination)) {
			String name = AppConfig.getState().getKitchenPrinterName();
			String ipAddress = AppConfig.getState().getKitchenPrinterIp();
			if (destination.equals(DESTINATION_BAR)) {
				name = AppConfig.getState().getBarPrinterName();
				ipAddress = AppConfig.getState().getBarPrinterIp();
			}
			return printIP(order, name, ipAddress);
		}
		return 0;
	}

    protected void handleHeaderPrint() {
		printNormalText();
        table(context, order.getDate());
        datetime(context, order.getDate());
        shopAndSalesPerson(context, order.getShopId(), order.getSalesPersonId());
		customer(context, order.getCustomer());
		if (order.isUseAlternative()) {
			printEmptyLine();
			addLine(makeCenterizedLine(context.getString(R.string.takeaway), width));
			printEmptyLine();
		}
		printRightLineOfLength('-', width);
    }

    protected void handleMainBodyPrint() {
        handleOrderLinesPrint();
    }

    protected void table(Context context, Date date) {
        addLine(makeLine((context.getString(R.string.table) + ":" + makeSpace(1) + order.getTable()), context.getString(R.string.area) + ":" + makeSpace(1) + order.getArea(), width, lm, rm));
    }

    protected void shopAndSalesPerson(Context context, String shopId, String salesPersonId) {
        addLine(makeLine(context.getString(R.string.salesperson) + ":" + makeSpace(1) + salesPersonId, "", width, lm, rm));
    }

    protected void handleOrderLinesPrint() {
        OrderLine thisLine;
        for (int i = 0; i < order.getLines().size(); i++) {
            thisLine = order.getLines().get(i);

			if (!thisLine.getQuantity().isEqual(thisLine.getDeliveredQty()) && thisLine.getProduct().getAbcCode() != null && thisLine.getProduct().getAbcCode().equals(destination)) {
				ArrayList<String> prodName = formatProductName(thisLine.getProduct().getName(), width - lm - rm, 0);
				Decimal diff = thisLine.getQuantity().subtract(thisLine.getDeliveredQty());

				//PRODUCT NAME LINES
				for (int j = 0; j < prodName.size(); j++) {
					if (j == 0) {
						addLine(makeLine(formatQtyIdPriceLine(diff.toString(), prodName.get(j), "", width - lm - rm), "", width, lm, rm));
					} else {
						addLine(makeLine(prodName.get(j), "", width, lm, rm));
					}
				}

				if (thisLine.getComponents() != null) {
					for (OrderLine cLine : thisLine.getComponents()) {
						prodName = formatProductName(cLine.getProduct().getName(), width - lm - rm, 0);
						for (int j = 0; j < prodName.size(); j++) {
							if (j == 0) {
								addLine(makeLine(formatQtyIdPriceLine("", prodName.get(j), "", width - lm - rm), "", width, lm, rm));
							} else {
								addLine(makeLine(prodName.get(j), "", width, lm, rm));
							}
						}
					}
				}

				if (thisLine.hasNote()) {
					ArrayList<String> notesLines = formatProductName(thisLine.getNote(), width - lm - rm, 0);
					for (int j = 0; j < notesLines.size(); j++) {
						addLine(makeLine(notesLines.get(j), "", width, lm, rm));
					}
				}
			}
		}

        printRightLineOfLength('-', width);

		if (order.hasNote()) {
			addLine(order.getNote());
		}
	}

	private boolean orderNeedToBePrinted(Order order, String destination) {
		if (order != null) {
			for (OrderLine line : order.getLines()) {
				if (!line.getQuantity().isEqual(line.getDeliveredQty()) && line.getProduct().getAbcCode() != null && line.getProduct().getAbcCode().equals(destination)) {
					return true;
				}
			}
		}
		return false;
	}

	protected void doExtraAfterSendData() {

	}

	@Override
	protected void handleExtraBottomPrint() {

	}

	@Override
	protected void printExtraTitle() {

	}

	protected void printExtraFooter() {

	}

	@Override
	protected void handlePaymentPrint() {

	}

	@Override
	protected void handleChangePrint() {

	}

	@Override
	protected void handleVatPrint(List<OrderLine> list) {

	}

}
