package no.susoft.mobile.pos.hardware.printer;

import java.text.SimpleDateFormat;
import java.util.List;

import android.text.format.DateFormat;
import jp.co.casio.vx.framework.device.LinePrinter;
import jp.co.casio.vx.framework.device.lineprintertools.LinePrinterDeviceBase;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.data.Prepaid;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class CasioPrintPrepaid extends CasioPrintOrder {

    private final List<Prepaid> prepaids;
    private Prepaid currentPrepaid;

    public CasioPrintPrepaid(List<Prepaid> prepaids) {
        width = casioWidth;
        this.prepaids = prepaids;
        this.context = MainActivity.getInstance();
        setupPrinter();
    }

    public int print() {
		int ret = LinePrinter.Response.SUCCESS;
        try {
			LinePrinter printer;
			device.setMulticharMode(LinePrinterDeviceBase.PAGEMODE_USER);

			printer = new LinePrinter();
			ret = printer.open(device);
			if (ret == LinePrinter.Response.SUCCESS) {
				for (Prepaid p : prepaids) {
					if (!p.getAmount().equals(Decimal.ZERO)) {
						receiptData = new StringBuilder();
						currentPrepaid = p;
						String output = this.makeReceipt(p, receiptData);
						ret = printer.printNormal(output);
						if (ret != LinePrinter.Response.SUCCESS) {
							return ret;
						}
					}
				}
				ret = printer.close();
				ErrorReporter.INSTANCE.filelog("CasioPrintPrepaid ret = " + ret);
			}

        } catch (Exception ex) {
            ErrorReporter.INSTANCE.filelog("CasioPrintPrepaid", "print()", ex);
        }
		return ret;
    }

    protected String makeReceipt(final Prepaid prepaid, StringBuilder receiptData) {

        chain();      //CHAIN
        feedLines(3);
        printPrepaidDate(prepaid);                    //DATE
        printShopAndSalesPerson(prepaid);
        printDivisionLine();
        handleMainBody(prepaid);
        handleExtraBottomText(width);
        feedLines(4);
        cutPaper();

        return receiptData.toString();
    }

    protected int sendReceiptToPrinter(final LinePrinterDeviceBase device, final String receiptData) {
        LinePrinter printer;

        String errTitle = "";
        int ret = LinePrinter.Response.SUCCESS;
        device.setMulticharMode(LinePrinterDeviceBase.PAGEMODE_USER);

        ErrBlock:
        try {
            errTitle = "construct";
            printer = new LinePrinter();
            errTitle = "open";

            ret = printer.open(device);
            if (ret != LinePrinter.Response.SUCCESS) {
                break ErrBlock;
            }
            errTitle = "printNormal";
            ret = printer.printNormal(receiptData);
            if (ret != LinePrinter.Response.SUCCESS) {
                break ErrBlock;
            }
            errTitle = "close";
            ret = printer.close();

        } catch (Exception e) {
            ErrorReporter.INSTANCE.filelog("CasioPrintPrepaid", "sendReceiptToPrinter", e);
        }

		ErrorReporter.INSTANCE.filelog("CasioPrintPrepaid ret = " + ret);

        if (ret == LinePrinter.Response.ERR_POWER_FAILURE) {
            receiveIntent(device, receiptData, errTitle, ret);
        }

		return ret;
    }


    protected void printShopAndSalesPerson(Prepaid prepaid) {
        receiptData.append(makeLine(
                context.getString(R.string.shop) + ":" + makeSpace(1) + prepaid.getShopId(),
                context.getString(R.string.salesperson) + ":" + makeSpace(1) + prepaid.getSalespersonId(), width, lm, rm));
    }

    protected void handleMainBody(Prepaid prepaid) {
        handleExtraTitle(receiptData, width, prepaid.getType());
        printGiftCardNumber(prepaid);
        printBarCode(prepaid);
        printAmount(prepaid);
        printValidityDate(prepaid);
    }


    protected void printPrepaidDate(Prepaid prepaid) {
        String date;
        if (prepaid.getLastUsedDate() != null) {
            date = context.getString(R.string.date) + ":" + makeSpace(1) + new SimpleDateFormat("dd/MM/yyyy").format(prepaid.getLastUsedDate());
        } else {
            date = context.getString(R.string.date) + ":" + makeSpace(1) + new SimpleDateFormat("dd/MM/yyyy").format(prepaid.getIssuedDate());
        }
        receiptData.append(makeLine(date, context.getString(R.string.time) + ":" + makeSpace(1) + (DateFormat.format("kk:mm:ss", prepaid.getIssuedDate().getTime())), width, lm, rm));
    }


    protected void handleExtraTitle(StringBuilder receiptData, int width, String type) {
        receiptData.append("\u001b|4C");// Vertical x 2, Holizontal x 2
        if (type.equalsIgnoreCase("C")) {
            receiptData.append(makeCenterizedLineLargeFont(context.getString(R.string.credit_voucher).toUpperCase(), width));
        } else {
            receiptData.append(makeCenterizedLineLargeFont(context.getString(R.string.gift_card).toUpperCase(), width));
        }
        receiptData.append("\u001b|N\r\n");// return to normal
        receiptData.append("\u001B|lF");
    }

    protected void printGiftCardNumber(Prepaid prepaid) {
        receiptData.append(makeCenterizedLine(context.getString(R.string.number) + ": " + prepaid.getNumber(), width));
        receiptData.append("\u001B|lF");
    }

    protected void printBarCode(Prepaid prepaid) {
        StringBuilder barcode = new StringBuilder();
        //32 is the size of the string after R. for 1G1 plus 8 numbers, use 32
        //doing 24 + length of number
        barcode.append("\u001B|");
        barcode.append(String.valueOf(24 + prepaid.getNumber().length()));
        barcode.append("Rs109h100w400a-2t-13d1G1");
        barcode.append(prepaid.getNumber());
        barcode.append("e");
        receiptData.append(barcode.toString());
    }

    protected void printAmount(Prepaid prepaid) {
        if (!prepaid.getAmount().equals(prepaid.getIssuedAmount())) {
            receiptData.append("\u001B|lF");
            receiptData.append(makeCenterizedLine(context.getString(R.string.issued_date) + " " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(prepaid.getIssuedDate()), width));
            receiptData.append(makeCenterizedLine(context.getString(R.string.issued_amount) + " " + prepaid.getIssuedAmount(), width));
        }
        receiptData.append("\u001B|lF");
        receiptData.append("\u001b|4C");// Vertical x 2, Holizontal x 2
        receiptData.append(makeCenterizedLineLargeFont(context.getString(R.string.amount) + " " + prepaid.getAmount(), width));
        receiptData.append("\u001b|N\r\n");// return to normal
        receiptData.append("\u001B|lF");
    }

    protected void printValidityDate(Prepaid prepaid) {
        if (prepaid.getDueDate() != null) {
            receiptData.append(makeCenterizedLine(context.getString(R.string.valid_to) + ": " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(prepaid.getDueDate()), width));
        }
    }

    //TODO
    protected String getValidDate() {
        return "31.12.2016";
    }

}
