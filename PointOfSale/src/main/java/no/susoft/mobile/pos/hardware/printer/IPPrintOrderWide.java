package no.susoft.mobile.pos.hardware.printer;

import java.util.ArrayList;
import java.util.List;

import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.*;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class IPPrintOrderWide extends IPPrint {

    protected static boolean returnsPrinted = false;
    protected Order order;
    protected boolean isCopy;

    public IPPrintOrderWide(final OrderPaymentResponse response, boolean isCopy) {
        this.order = response.getOrder();
        this.isCopy = isCopy;
        this.context = MainActivity.getInstance();
        width = wideWidth;
    }

	public IPPrintOrderWide() {
        this.context = MainActivity.getInstance();
        width = wideWidth;
	}

	protected void handleOrderLinesPrint() {

        OrderLine thisLine;
        for (int i = 0; i < order.getLines().size(); i++) {
            thisLine = order.getLines().get(i);

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
                                width), "", width, lm, rm));
                    } else {
                        addLine(makeLine(formatQtyIdPriceLine(thisLine.getQuantity().toString(), prodName.get(j), thisLine.getAmount(true).toString(), width - lm - rm), "", width, lm, rm));

                    }
                } else {
                    addLine(makeLine(prodName.get(j), "", width, lm, rm));
                }
            }

			if (thisLine.getComponents() != null) {
				for (OrderLine cLine : thisLine.getComponents()) {
					if (!cLine.getAmount(true).isZero()) {
						if (!cLine.getAmount(true).isEqual(cLine.getAmount(false))) {
							prodName = formatProductName(cLine.getProduct().getName(), width - lm - rm, priceExtraSpace);
						} else {
							prodName = formatProductName(cLine.getProduct().getName(), width - lm - rm, priceSpace);
						}
						for (int j = 0; j < prodName.size(); j++) {
							if (j == 0) {
								addLine(makeLine(formatQtyIdPriceLine("", prodName.get(j), cLine.getAmount(true).toString(), width - lm - rm), "", width, lm, rm));
							} else {
								addLine(makeLine(prodName.get(j), "", width, lm, rm));
							}
						}
					}
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
        }

        printRightLineOfLength('-', width);

		if (order.hasNote()) {
			printEmptyLine();
//			ArrayList<String> notesLines = formatProductName(order.getNote(), width, 0);
//			for (int j = 0; j < notesLines.size(); j++) {
//				addLine(makeLine(notesLines.get(j), "", width, lm, rm));
//			}
			
			addLine(order.getNote());
			
			printEmptyLine();
			printEmptyLine();
		}
    }

    protected void printPaymentMethods() {
		for (Payment pmnt : order.getPayments()) {
            addLine(makeLine(getPaymentTypeString(pmnt.getType(), context), pmnt.getAmount().toString(), width, lm, rm));
        }
    }

    @Override
    protected void handleChangePrint() {
		if (MainActivity.getInstance().getNumpadPayFragment().getChange().isPositive()) {
			addLine(makeLine(context.getString(R.string.change), MainActivity.getInstance().getNumpadPayFragment().getChange().toString(), width, lm, rm));
			printRightLineOfLength('-', priceExtraSpace);
		}
    }

    protected void handleVatPrint(List<OrderLine> orderLines) {
        //VAT
        addLine(makeLine(context.getString(R.string.specify_vat), "", width, lm, rm));
        addLine(formatVatLineNoCode(context.getString(R.string.vat_rate), context.getString(R.string.purchase), context.getString(R.string.vat), width));

        ArrayList<VatGroup> vg = calculateVATForOrderLines(orderLines);
        Decimal orderSumWithoutVat = Decimal.ZERO;
        Decimal orderVat = Decimal.ZERO;

        for (VatGroup v : vg) {
            orderSumWithoutVat = orderSumWithoutVat.add(v.getSumWithoutVat());
            orderVat = orderVat.add(v.getSumVat());
            addLine(formatVatLineNoCode((v.getVatPercent().toString() + "%"), v.getSumWithoutVat().toString(), v.getSumVat().toString(), width));
        }

        printRightLineOfLength('-', width - (width / 3));
        addLine(makeLine(context.getString(R.string.total_vat), "", width, lm, rm));
        addLine(formatVatLineNoCode("", orderSumWithoutVat.toString(), orderVat.toString(), width));
        addLine(makeLine("", rightLineOfCharOfSize('=', width - (width / 3)), width, lm, rm));
    }

    protected void handleExtraBottomPrint() {
        printEmptyLine();
		printExtraFooter();

        for(byte[] b : printBarcode(getOrderBarcodeString())) {
            output.add(b);
        }
		addLine(getOrderBarcodeString());
        boolean isInvoice = false;
        if (order != null && order.getPayments() != null && order.getPayments().size() > 0) {
            for (Payment payment : order.getPayments()) {
                if (payment.getType() == Payment.PaymentType.INVOICE) {
                    isInvoice = true;
                }
            }
        }
        if (isInvoice && !returnsPrinted) {
            printEmptyLine();
            printEmptyLine();
            addLine(makeLine(context.getString(R.string.signature), "", width, lm, rm));
            addLine(makeLine("", rightLineOfCharOfSize(' ', priceExtraSpace), width, lm, rm));
            addLine(makeLine("", rightLineOfCharOfSize('-', width-lm-rm), width, lm, rm));
        }
        
        handleCleanCash();
    }
	
	protected void handleCleanCash() {
		if (order.getReceiptId() > 0) {
			printEmptyLine();
			printEmptyLine();
			printEmptyLine();
			addLine(makeCenterizedLine("" + order.getReceiptId(), width));
		}
		if (order.getDeviceSerialNumber() != null && order.getDeviceSerialNumber().length() > 0) {
			addLine(makeCenterizedLine(order.getDeviceSerialNumber(), width));
		}
		if (order.getControlCode() != null && order.getControlCode().length() > 0) {
			addLine(makeCenterizedLine(order.getControlCode(), width));
		}
	}
	
	@Override
    protected void printExtraTitle() {
		if (AccountManager.INSTANCE.getSavedReceiptHeader() != null && AccountManager.INSTANCE.getSavedReceiptHeader().trim().length() > 0) {
			printEmptyLine();
			addLine(AccountManager.INSTANCE.getSavedReceiptHeader());
			printEmptyLine();
		}
    }

	protected void printExtraFooter() {
		if (AccountManager.INSTANCE.getSavedReceiptFooter() != null && AccountManager.INSTANCE.getSavedReceiptFooter().trim().length() > 0) {
			printEmptyLine();
			addLine(AccountManager.INSTANCE.getSavedReceiptFooter());
			printEmptyLine();
		}
    }

    protected void handleHeaderPrint() {
		if (isCopy) {
			copy(context);
		} else {
			chain();
		}
        datetime(context, order.getDate());
        shopAndSalesPerson(context, order.getShopId(), order.getSalesPersonId());
		if (!MainActivity.getInstance().isConnected()) {
			orderNumber(context, order.getAlternativeId());
		} else {
			orderNumber(context, order.getId());
		}
		customer(context, order.getCustomer());
		printExtraTitle();
        printRightLineOfLength('-', width);
    }

    protected void handlePaymentPrint() {
        printOrderTotalWithDiscount();
        printPaymentMethods();
        printRightLineOfLength('-', priceExtraSpace);
    }


}
