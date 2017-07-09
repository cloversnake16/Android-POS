package no.susoft.mobile.pos.hardware.printer;

import java.util.ArrayList;
import java.util.List;

import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.data.OrderLine;
import no.susoft.mobile.pos.data.VatGroup;

class BluetoothPrintReturnsSlim extends BluetoothPrintOrderSlim {

    private ArrayList<OrderLine> orderLines;

    BluetoothPrintReturnsSlim(Order order) {
        width = slimWidth;
        this.order = order;
        output = new ArrayList<>();
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
                output.add(makeLine(formatQtyIdPriceLine(thisLine.getQuantity().toString(), thisLine.getProduct().getId(), thisLine.getAmount(true).toString(), width - lm - rm), "", width, lm, rm));
                ArrayList<String> prodName;

                prodName = formatProductName(thisLine.getProduct().getName(), width - lm - rm, priceSpace);

                for (String aProdName : prodName) {

                    output.add(makeLine(aProdName, "", width, lm, rm));
                }

            }
        }
        output.add(makeLine("", rightLineOfCharOfSize('-', width), width, lm, rm));

		if (order.hasNote()) {
			printEmptyLine();
			addLine(order.getNote());
			printEmptyLine();
			printEmptyLine();
		}
    }

    @Override
    protected void printExtraTitle() {
        //big text
        byte[] cmd = new byte[]{0x1b, 0x21, 0x10};
        output.add(cmd);
        cmd = new byte[]{0x1b, 0x21, 0x20};
        output.add(cmd);

        output.add(makeLine(context.getString(R.string.returns), "", width, lm, rm));
        //back to normal text
        cmd = new byte[]{0x1b, 0x21, 0x00};
        output.add(cmd);
    }

	protected void printExtraFooter() {

	}

    @Override
    protected void handlePaymentPrint() {
        printTotalPurchase();
        printPaymentMethods();
        output.add(makeLine("", rightLineOfCharOfSize('-', priceExtraSpace), width, lm, rm));
    }


    protected void printTotalPurchase() {
        Decimal sum = Decimal.ZERO;
        for (OrderLine ol : orderLines) {
            sum = sum.add(ol.getAmount(true));
        }
        output.add(makeLine(context.getString(R.string.total_return) + ":", sum.toString(), width, lm, rm));
    }


    @Override
    protected void handleChangePrint() {
        //don't print
    }

    @Override
    protected void handleExtraBottomPrint() {
        output.add(makeLine(context.getString(R.string.signature), "", width, lm, rm));
        output.add(makeLine("", rightLineOfCharOfSize(' ', priceExtraSpace), width, lm, rm));
        output.add(makeLine("", rightLineOfCharOfSize('-', priceExtraSpace), width, lm, rm));
    }

    @Override
    protected void handleVatPrint(List<OrderLine> orderLines) {
        ArrayList<VatGroup> vg = calculateVATForOrderLines(orderLines);
        Decimal orderSumWitoutVat = Decimal.ZERO;
        Decimal orderVat = Decimal.ZERO;

        for (VatGroup v : vg) {
            orderSumWitoutVat = orderSumWitoutVat.add(v.getSumWithoutVat());
            orderVat = orderVat.add(v.getSumVat());
            output.add(makeLine(formatVatLine(v.getVatPercent().toString() + "%", "0", (width - lm - rm) / 2), formatVatLine(v.getSumWithoutVat().toString(), v.getSumVat().toString(), (width - lm - rm) / 2), width, lm, rm));
        }
        output.add(makeLine("", rightLineOfCharOfSize('-', (width - lm - rm) - qtySpace - priceSpace), width, lm, rm));
        output.add(makeLine(context.getString(R.string.total_vat), formatVatLine(orderSumWitoutVat.toString(), orderVat.toString(), (width - lm - rm) / 2), width, lm, rm));

        printEmptyLine();
    }
}
