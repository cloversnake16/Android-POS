package no.susoft.mobile.pos.hardware.printer;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import android.widget.Toast;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.data.OrderLine;
import no.susoft.mobile.pos.data.Payment;
import no.susoft.mobile.pos.data.Payment.PaymentType;
import no.susoft.mobile.pos.data.VatGroup;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class BluetoothPrintOrderSlim extends BluetoothPrint {

	public boolean isCopy = false;

    public BluetoothPrintOrderSlim() {
        width = slimWidth;
    }

    protected void handleOrderLinesPrint() {

        OrderLine thisLine;
        for (int i = 0; i < order.getLines().size(); i++) {
            thisLine = order.getLines().get(i);

            addLine(makeLine(formatQtyIdPriceLine(thisLine.getQuantity().toString(), thisLine.getProduct().getId(), thisLine.getAmount(true).toString(), width - lm - rm), "", width, lm, rm));
            ArrayList<String> prodName = formatProductName(thisLine.getProduct().getName(), width - lm - rm, priceSpace);

            for (String aProdName : prodName) {
                addLine(makeLine(aProdName, "", width, lm, rm));
            }

			if (thisLine.getComponents() != null) {
				for (OrderLine cLine : thisLine.getComponents()) {
					if (!cLine.getAmount(true).isZero()) {
						addLine(makeLine(formatQtyIdPriceLine("", cLine.getProduct().getId(), cLine.getAmount(true).toString(), width - lm - rm), "", width, lm, rm));
						prodName = formatProductName(cLine.getProduct().getName(), width - lm - rm, priceSpace);
						for (String aProdName : prodName) {
							addLine(makeLine(aProdName, "", width, lm, rm));
						}
					}
				}
			}

            //If if's not the last line
            if (i < order.getLines().size() - 1) {
                addLine(makeLine("", "", width, lm, rm));//add empty line
            }
        }

        printRightLineOfLength('-', width);
    }

    protected void printPaymentMethods() {
        //Print payment methods with amount
        for (Payment p : order.getPayments()) {
            addLine(makeLine(getPaymentTypeString(p.getType(), context), p.getAmount().toString(), width, lm, rm));
        }
    }


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
        printRightLineOfLength('-', width - (width / 3));
    }

    @Override
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
				if (payment.getType() == PaymentType.INVOICE) {
					isInvoice = true;
				}
			}
		}
		if (isInvoice && !returnsPrinted) {
			printEmptyLine();
			addLine(makeLine(context.getString(R.string.signature), "", width, lm, rm));
			addLine(makeLine("", rightLineOfCharOfSize(' ', priceExtraSpace), width, lm, rm));
			addLine(makeLine("", rightLineOfCharOfSize('-', width-lm-rm), width, lm, rm));
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
		if (order.hasNote()) {
			printEmptyLine();
			addLine(order.getNote());
			printEmptyLine();
			printEmptyLine();
		}
		
		if (AccountManager.INSTANCE.getSavedReceiptFooter() != null && AccountManager.INSTANCE.getSavedReceiptFooter().trim().length() > 0) {
			printEmptyLine();
			addLine(AccountManager.INSTANCE.getSavedReceiptFooter());
			printEmptyLine();
		}
    }

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
		if (!MainActivity.getInstance().isConnected()) {
			orderNumber(context, order.getAlternativeId());
		} else {
			orderNumber(context, order.getId());
		}
        customer(context, order.getCustomer());
		printExtraTitle();
        printRightLineOfLength('-', width);
    }

    @Override
    protected void handlePaymentPrint() {
        printOrderTotalWithDiscount();
        printPaymentMethods();
        printRightLineOfLength('-', priceExtraSpace);
    }

    @Override
    protected void bottomOfSendData() {
        try {
            byte[] cmd = new byte[]{0x1b, 0x64, 0xC}; //print and feed 16 lines - only appears like about 3
            mmOutputStream.write(" ".getBytes(Charset.forName("UTF-8")));
            mmOutputStream.write(cmd);
            mmOutputStream.write(new byte[]{0x1b, 0x69});
            mmOutputStream.flush();

            if (order != null && order.hasReturnLines() && !returnsPrinted) {
                BluetoothPrintReturnsSlim returnsPrint = new BluetoothPrintReturnsSlim(order);
                ArrayList<Object> returnsString = returnsPrint.PreparePrint(order);
                returnsPrinted = true;
                returnsPrint.sendData(returnsString);
            } else if (order != null && !order.hasReturnLines()){
                closeBT();
                returnsPrinted = false;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void doExtraAfterSendData() {
        try {
            if (!isCopy && order != null && order.hasReturnLines() && !returnsPrinted) {
                BluetoothPrintReturnsSlim returnsPrint = new BluetoothPrintReturnsSlim(order);
                ArrayList<Object> returnsString = returnsPrint.PreparePrint(order);
                returnsPrinted = true;
                returnsPrint.sendData(returnsString);
            } else {
                closeBT();
            }
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog("BluetoothPrint.doExtraAfterSendData()", "Error", e);
            Toast.makeText(MainActivity.getInstance(), R.string.cannot_connect_bluetooth_printer, Toast.LENGTH_SHORT).show();
        }
    }

}
