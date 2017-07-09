package no.susoft.mobile.pos.hardware.printer;

import java.util.ArrayList;

import android.util.Log;
import jp.co.casio.vx.framework.device.lineprintertools.LinePrinterDeviceBase;
import jp.co.casio.vx.framework.device.lineprintertools.SerialUp400;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.*;
import no.susoft.mobile.pos.ui.activity.MainActivity;

/**
 * Created by Vilde on 16.12.2015.
 */
public class CasioPrintReturns extends CasioPrintOrder {

    ArrayList<OrderLine> orderLines;
    private Order order = null;

    public CasioPrintReturns(Order order) {
        this.order = order;
        receiptData = new StringBuilder();
        width = casioWidth;
    }

    @Override
    protected int InitiatePrint(final Order order, ReceiptPrintType isCopy) {
        return doPrint(order, isCopy);
    }

    protected int doPrint(Order order, ReceiptPrintType isCopy) {
        context = MainActivity.getInstance();
        LinePrinterDeviceBase device = new SerialUp400(SerialUp400.Port.PORT_COM1, SerialUp400.BaudRate.BAUDRATE_9600, SerialUp400.BitLen.BITLEN_8, SerialUp400.ParityBit.PARITYBIT_NON, SerialUp400.StopBit.STOPBIT_1, SerialUp400.FlowCntl.FLOWCNTL_NON);
        int width[];
        receiptData = new StringBuilder();

        try {
            width = device.getLineChars();

            if (width[0] == 0) {
                width[0] = 32;
                return 0;
            }
        } catch (Exception ex) {
            return 0;
        }

        return sendReceiptToPrinter(device, makeReceipt(order, isCopy, new StringBuilder()));
    }

    protected void feedOneLine() {
        receiptData.append("\u001b|lF");//feed
    }

    //Changed to only print lines with negative qty (returned)
    @Override
    protected void printOrderLines(int width) {
        OrderLine thisLine;
        orderLines = new ArrayList<>();

        for (int i = 0; i < order.getLines().size(); i++) {
            thisLine = order.getLines().get(i);

            if (thisLine.getQuantity().isNegative()) {
                orderLines.add(thisLine);
                //Print line with qty, product id and price
                receiptData.append(makeLine(formatQtyIdPriceLine(thisLine.getQuantity().toString(), thisLine.getProduct().getId(), thisLine.getAmount(true).toString(), width - lm - rm), "", width, lm, rm));
                ArrayList<String> prodName;
                Log.i("vilde", "1");
                //TESTING WITH 20
                prodName = formatProductName(thisLine.getProduct().getName(), width - lm - rm, priceExtraSpace);
                Log.i("vilde", "2");
                for (String aProdName : prodName) {
                    Log.i("vilde", "its printing name");
                    receiptData.append(makeLine(aProdName, "", width, lm, rm));
                }

                //If if's not the last line
                if (i < order.getLines().size() - 1) {
                    receiptData.append("\u001b|lF");//feed
                }
            }
        }
        Log.i("vilde", "it printed order lines");
    }

    @Override
    protected void handleExtraTitle(StringBuilder receiptData, int width) {
        receiptData.append("\u001b|4C");// Vertical x 2, Holizontal x 2
        receiptData.append(makeCenterizedLineLargeFont(context.getString(R.string.returns), width - lm - rm));
        receiptData.append("\u001b|N\r\n");// return to normal
        feedOneLine();
    }

	protected void handleExtraBottom() {

	}

    @Override
    protected void printOrderTotalWithDiscount(int width) {
        Decimal sum = Decimal.ZERO;
        for (OrderLine ol : orderLines) {
            sum = sum.add(ol.getAmount(true));
        }
        receiptData.append(makeLine(context.getString(R.string.total_return) + ":", sum.toString(), width, lm, rm));
        receiptData.append(makeLine("", rightLineOfCharOfSize('=', priceExtraSpace), width, lm, rm));
    }

    @Override
    protected void handlePaymentMethods() {
        //don't print
    }

    @Override
    protected void handleChange() {
        //don't print
    }

    @Override
    protected void handleExtraBottomText(int width) {
        feedOneLine();
        feedOneLine();
        receiptData.append(makeLine(context.getString(R.string.signature), "", width, lm, rm));
        feedOneLine();
        feedOneLine();
        receiptData.append(makeLine(dashLine(width - lm - rm), "", width, lm, rm));

    }

    @Override
    protected void handleOrderLines() {
        printOrderLines(width);              //ORDER LINES
        printDivisionLine();
        printOrderTotalWithDiscount(width);  //ORDER TOTAL

		if (order.hasNote()) {
			feedLines(1);
			addLine(order.getNote());
			feedLines(1);
			feedLines(1);
		}
    }

    @Override
    protected void printVatDetails(int width) {
        ArrayList<VatGroup> vg = calculateVATForOrderLines(orderLines);
        Decimal orderSumWitoutVat = Decimal.ZERO;
        Decimal orderVat = Decimal.ZERO;

        for (VatGroup v : vg) {
            orderSumWitoutVat = orderSumWitoutVat.add(v.getSumWithoutVat());
            orderVat = orderVat.add(v.getSumVat());
            receiptData.append(makeLine(formatVatLine(v.getVatPercent().toString() + "%", "0", (width - lm - rm) / 2), formatVatLine(v.getSumWithoutVat().toString(), v.getSumVat().toString(), (width - lm - rm) / 2), width, lm, rm));
        }
        receiptData.append(makeLine("", rightLineOfCharOfSize('-', (width - lm - rm) - qtySpace - priceSpace), width, lm, rm));
        receiptData.append(makeLine(context.getString(R.string.total_vat), formatVatLine(orderSumWitoutVat.toString(), orderVat.toString(), (width - lm - rm) / 2), width, lm, rm));

    }


}
