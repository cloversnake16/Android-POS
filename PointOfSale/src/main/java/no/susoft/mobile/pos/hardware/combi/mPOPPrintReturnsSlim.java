package no.susoft.mobile.pos.hardware.combi;

import java.util.ArrayList;
import java.util.List;

import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.data.OrderLine;
import no.susoft.mobile.pos.data.VatGroup;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class mPOPPrintReturnsSlim extends mPOPPrintOrderSlim {

    private ArrayList<OrderLine> orderLines;

    public mPOPPrintReturnsSlim(Order order) {
        width = slimWidth;
        this.order = order;
		output = new ArrayList<>();
		String barcode = "1O1" + order.getShopId() + String.valueOf(order.getId());
		if (!MainActivity.getInstance().isConnected()) {
			barcode = "1A1" + order.getAlternativeId();
		}
		Communication.sendCommands(convertArrayListToBytes(PreparePrint(order), barcode), Star_mPOP.getPort(), MainActivity.getInstance());
    }

    //Changed to only print lines with negative qty (returned)
    @Override
    protected void handleOrderLinesPrint() {
//        OrderLine thisLine;
//        orderLines = new ArrayList<>();
//
//        for (int i = 0; i < order.getLines().size(); i++) {
//            thisLine = order.getLines().get(i);
//
//            if (thisLine.getQuantity().isNegative()) {
//                orderLines.add(thisLine);
//                //Print line with qty, product id and price
//                output.add(makeLine(formatQtyIdPriceLine(thisLine.getQuantity().toString(), thisLine.getProduct().getId(), thisLine.getAmount(true).toString(), width - lm - rm), "", width, lm, rm));
//                ArrayList<String> prodName;
//
//                prodName = formatProductName(thisLine.getProduct().getName(), width - lm - rm, priceSpace);
//
//                for (String aProdName : prodName) {
//
//                    output.add(makeLine(aProdName, "", width, lm, rm));
//                }
//
//            }
//        }
//        output.add(makeLine("", rightLineOfCharOfSize('-', width), width, lm, rm));

        orderLines = new ArrayList<>();
        OrderLine thisLine;
        for (int i = 0; i < order.getLines().size(); i++) {
            thisLine = order.getLines().get(i);
            if (thisLine.getQuantity().isNegative()) {
                orderLines.add(thisLine);

                //Print line with qty, product id and price
                ArrayList<String> prodName;

                if (!thisLine.getAmount(true).isEqual(thisLine.getAmount(false))) {
                    prodName = formatProductName(thisLine.getProduct().getName(), width - lm - rm, priceExtraSpace);
                } else {
                    prodName = formatProductName(thisLine.getProduct().getName(), width - lm - rm, priceSpace);
                }

                //PRODUCT NAME LINES
                for (int j = 0; j < prodName.size(); j++) {
                    if (j == 0) {
                        if (!thisLine.getAmount(true).isEqual(thisLine.getAmount(false))) {
                            addLine(makeLine(formatQtyIdPriceLine(thisLine.getQuantity().toString(), prodName.get(j),
                                    "-" + thisLine.getDiscount().getPercent().add(Decimal.make(0.5)).toInteger() + "% " + thisLine.getAmount(true).toString(),
                                    width - lm - rm), "", width, lm, rm));
                        } else {
                            addLine(makeLine(formatQtyIdPriceLine(thisLine.getQuantity().toString(), prodName.get(j), thisLine.getAmount(true).toString(), width - lm - rm), "", width, lm, rm));

                        }
                    } else {
                        addLine(makeLine(prodName.get(j), "", width, lm, rm));
                    }
                }

                //DISCOUNT
                if (!thisLine.getAmount(true).isEqual(thisLine.getAmount(false))) {
                    Decimal discAmount = thisLine.getAmount(false).subtract(thisLine.getAmount(true));
//                receiptData.append(makeLine("",
//                        "(" + thisLine.getAmount(false).toString() +
//                                " - " + thisLine.getDiscount().getPercent().toString() +
//                                "%/" + thisLine.getDiscountAmount().toString() + ")",
//                        width, lm, rm+1));
//                receiptData.append(makeLine("", context.getString(R.string.original_price) + " " + thisLine.getAmount(false).toString(), width, lm, rm+1));
//                receiptData.append(makeLine("", context.getString(R.string.discount) + " " + thisLine.getDiscount().getPercent() + "% (" + discAmount.toString() + ")", width, lm, rm+1));
                }

                //Space between lines
//            if (i < order.getLines().size() - 1) {
//                receiptData.append("\u001b|lF");//feed
//            }
            }
        }
        addLine(makeLine("", rightLineOfCharOfSize('-', width-lm-rm), width, lm, rm));

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
        addLine(new byte[]{0x1b, 0x69, 0x01, 0x01});

        addLine(makeCenterizedLineLargeFont(context.getString(R.string.returns), width-lm-rm));
        //back to normal text
        addLine(new byte[]{0x1b, 0x69, 0x00, 0x00});
        addLine(makeLine("", rightLineOfCharOfSize(' ', priceExtraSpace), width, lm, rm));
    }

	protected void printExtraFooter() {

    }

    @Override
    protected void handlePaymentPrint() {
        printTotalPurchase();
        printPaymentMethods();
        addLine(makeLine("", rightLineOfCharOfSize('-', priceExtraSpace), width, lm, rm));
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
        addLine(makeLine("", rightLineOfCharOfSize(' ', priceExtraSpace), width, lm, rm));
        addLine(makeLine("", rightLineOfCharOfSize('-', width-lm-rm), width, lm, rm));
    }

    @Override
    protected void handleVatPrint(List<OrderLine> orderLines) {
        ArrayList<VatGroup> vg = calculateVATForOrderLines(orderLines);
        Decimal orderSumWitoutVat = Decimal.ZERO;
        Decimal orderVat = Decimal.ZERO;

        for (VatGroup v : vg) {
            orderSumWitoutVat = orderSumWitoutVat.add(v.getSumWithoutVat());
            orderVat = orderVat.add(v.getSumVat());
            addLine(makeLine(formatVatLine(v.getVatPercent().toString() + "%", "0", (width - lm - rm) / 2), formatVatLine(v.getSumWithoutVat().toString(), v.getSumVat().toString(), (width - lm - rm) / 2), width, lm, rm));
        }
        addLine(makeLine("", rightLineOfCharOfSize('-', (width - lm - rm) - qtySpace - priceSpace), width, lm, rm));
        addLine(makeLine(context.getString(R.string.total_vat), formatVatLine(orderSumWitoutVat.toString(), orderVat.toString(), (width - lm - rm) / 2), width, lm, rm));

        printEmptyLine();
    }
}
