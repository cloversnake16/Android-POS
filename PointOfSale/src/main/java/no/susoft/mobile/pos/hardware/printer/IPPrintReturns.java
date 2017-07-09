package no.susoft.mobile.pos.hardware.printer;

import java.util.ArrayList;
import java.util.List;

import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.data.OrderLine;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class IPPrintReturns extends IPPrintOrderWide {

    private ArrayList<OrderLine> orderLines;

	public IPPrintReturns(Order order) {
		this.order = order;
        this.context = MainActivity.getInstance();
        width = wideWidth;
	}

	//Changed to only print lines with negative qty (returned)
    @Override
    protected void handleOrderLinesPrint() {
        OrderLine thisLine;
        orderLines = new ArrayList<>();

        for (int i = 0; i < order.getLines().size(); i++) {
            thisLine = order.getLines().get(i);

            if (thisLine.getQuantity().isNegative()) {
                orderLines.add(thisLine);
                //Print line with qty, product id and price
                addLine(makeLine(formatQtyIdPriceLine(thisLine.getQuantity().toString(), thisLine.getProduct().getId(), thisLine.getAmount(true).toString(), width - lm - rm), "", width, lm, rm));
                ArrayList<String> prodName;

                prodName = formatProductName(thisLine.getProduct().getName(), width - lm - rm, priceSpace);

                for (String aProdName : prodName) {
                    addLine(makeLine(aProdName, "", width, lm, rm));
                }

            }
        }
        addLine(makeLine("", rightLineOfCharOfSize('-', width), width, lm, rm));

		if (order.hasNote()) {
			printEmptyLine();
			addLine(order.getNote());
			printEmptyLine();
			printEmptyLine();
		}
    }

    @Override
    protected void printExtraTitle() {
        addLine(makeLine(context.getString(R.string.returns), "", width, lm, rm));
    }

	protected void printExtraFooter() {

	}

    @Override
    protected void handlePaymentPrint() {
        printTotalPurchase();
    }

    protected void printTotalPurchase() {
        Decimal sum = Decimal.ZERO;
        for (OrderLine ol : orderLines) {
            sum = sum.add(ol.getAmount(true));
        }
        addLine(makeLine(context.getString(R.string.total_return) + ":", sum.toString(), width, lm, rm));
    }


    @Override
    protected void handleChangePrint() {
        //don't print
    }

    @Override
    protected void handleExtraBottomPrint() {
        addLine(makeLine(context.getString(R.string.signature), "", width, lm, rm));
        addLine(makeLine("", rightLineOfCharOfSize(' ', priceExtraSpace), width, lm, rm));
        addLine(makeLine("", rightLineOfCharOfSize('-', (width - lm - rm) - qtySpace - priceSpace), width, lm, rm));
    }

    @Override
    protected void handleVatPrint(List<OrderLine> orderLines) {
        printEmptyLine();
    }

	protected void doExtraAfterSendData() {

	}

}
