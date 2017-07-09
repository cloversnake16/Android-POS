package no.susoft.mobile.pos.hardware.printer;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.*;
import android.widget.Toast;
import jp.co.casio.vx.framework.device.LinePrinter;
import jp.co.casio.vx.framework.device.lineprintertools.LinePrinterDeviceBase;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.*;
import no.susoft.mobile.pos.data.Payment.PaymentType;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class CasioPrintOrder extends CasioPrint {

    protected Order order = null;
    IntentFilter intentFilter = new IntentFilter("jp.co.casio.vx.regdevice.comm.POWER_RECOVERY");
    int boardNo = 0;
    Context context;
    private BroadcastReceiver intentReceiver;

    public CasioPrintOrder() {
        width = casioWidth;
        receiptData = new StringBuilder();
    }

    @Override
    public int print(Object o, ReceiptPrintType printType) {
        context = MainActivity.getInstance();
        return InitiatePrint((Order) o, printType);
    }

    protected int InitiatePrint(final Order order, ReceiptPrintType isCopy) {
        this.order = order;
        try {
            setupPrinter();
            return sendReceiptToPrinter(device, makeReceipt(order, isCopy, receiptData));
        } catch (Exception ex) {
            ErrorReporter.INSTANCE.filelog("CasioPrintOrder", "InitiatePrint", ex);
            Toast.makeText(MainActivity.getInstance(), R.string.error_printing_receipt, Toast.LENGTH_LONG).show();
        }
		return -1;
    }

    protected void printDivisionLine() {
        receiptData.append(dashLine(width)); //division line filled with -----
    }

    protected void printOrderLines(int width) {
        OrderLine thisLine;
        for (int i = 0; i < order.getLines().size(); i++) {
            thisLine = order.getLines().get(i);

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
                        receiptData.append(makeLine(formatQtyIdPriceLine(thisLine.getQuantity().toString(), prodName.get(j),
                                "-" + thisLine.getDiscount().getPercent().add(Decimal.make(0.5)).toInteger() + "% " + thisLine.getAmount(true).toString(),
                                width - lm - rm), "", width, lm, rm));
                    } else {
                        receiptData.append(makeLine(formatQtyIdPriceLine(thisLine.getQuantity().toString(), prodName.get(j), thisLine.getAmount(true).toString(), width - lm - rm), "", width, lm, rm));

                    }
                } else {
                    receiptData.append(makeLine(prodName.get(j), "", width, lm, rm));
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
								receiptData.append(makeLine(formatQtyIdPriceLine("", prodName.get(j), cLine.getAmount(true).toString(), width - lm - rm), "", width, lm, rm));
							} else {
								receiptData.append(makeLine(prodName.get(j), "", width, lm, rm));
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

            //Space between lines
//            if (i < order.getLines().size() - 1) {
//                receiptData.append("\u001b|lF");//feed
//            }
        }
    }

    protected void printOrderTotalWithDiscount(int width) {
        if (!order.getAmount(true).isEqual(order.getAmount(false))) {
            receiptData.append(makeLine(context.getString(R.string.total_discount) + ":", order.getAmount(false).subtract(order.getAmount(true)).toString(), width, lm, rm));
            printDivisionLine();
        }
        receiptData.append(makeLine(context.getString(R.string.total_purchase) + ":", order.getAmount(true).toString(), width, lm, rm));
        receiptData.append(makeLine("", rightLineOfCharOfSize('-', priceExtraSpace), width, lm, rm));
    }

    protected void printPaymentMethods() {
		for (Payment pmnt : order.getPayments()) {
            receiptData.append(makeLine(getPaymentTypeString(pmnt.getType(), context), pmnt.getAmount().toString(), width, lm, rm));
        }
    }

    protected void printChange() {
        receiptData.append(makeLine(context.getString(R.string.receipt_change_amount), MainActivity.getInstance().getNumpadPayFragment().getChange().toString(), width, lm, rm));
    }

    protected void printSpecifyVat() {
        receiptData.append(makeLine(context.getString(R.string.specify_vat), "", width, lm, rm));

    }

    protected void printVatHeaders() {
        receiptData.append(makeLine(formatVatLine(context.getString(R.string.vat_rate), context.getString(R.string.vat_code), (width - lm - rm) / 2), formatVatLine(context.getString(R.string.purchase), context.getString(R.string.vat), (width - lm - rm) / 2), width, lm, rm));
    }

    protected void printVatDetails(int width) {
        ArrayList<VatGroup> vg = calculateVATForOrderLines(order.getLines());
        Decimal orderSumWithoutVat = Decimal.ZERO;
        Decimal orderVat = Decimal.ZERO;

        for (VatGroup v : vg) {
            orderSumWithoutVat = orderSumWithoutVat.add(v.getSumWithoutVat());
            orderVat = orderVat.add(v.getSumVat());
            receiptData.append(makeLine(formatVatLine(v.getVatPercent().toString() + "%", "0", (width - lm - rm) / 2), formatVatLine(v.getSumWithoutVat().toString(), v.getSumVat().toString(), (width - lm - rm) / 2), width, lm, rm));
        }
        receiptData.append(makeLine("", rightLineOfCharOfSize('-', (width - lm - rm) - qtySpace - priceSpace), width, lm, rm));
        receiptData.append(makeLine(context.getString(R.string.total_vat), formatVatLine(orderSumWithoutVat.toString(), orderVat.toString(), (width - lm - rm) / 2), width, lm, rm));
    }

    protected String makeReceipt(final Order order, ReceiptPrintType printType, StringBuilder receiptData) {
		try {
			this.order = order;
			this.receiptData = receiptData;
			receiptData.append("\u001B|N");

			if (printType.equals(ReceiptPrintType.COPY)) {
				printCopyHeader();
				feedLines(1);
			} else if (printType.equals(ReceiptPrintType.PRELIM)) {
				printPrelimHeader();
				feedLines(1);
			}

			setLargeText();
			chain();
			setNormalSizeText();
			feedLines(2);
			datetime(context, order.getDate());
			shopAndSalesPerson(context, order.getShopId(), order.getSalesPersonId());
			if (!MainActivity.getInstance().isConnected()) {
				orderNumber(context, order.getAlternativeId());
			} else {
				orderNumber(context, order.getId());
			}
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

    private void printPrelimHeader() {
        setLargeText();
        receiptData.append(makeCenterizedLineLargeFont(context.getString(R.string.prelim), width - lm - rm));
        setNormalSizeText();
    }

    protected void handleExtraBottomText(int width) {

    }

    protected void handleMainBody(ReceiptPrintType printType) {
        handleOrderLines();
        handlePaymentMethods();

        if (printType.equals(ReceiptPrintType.ORIGINAL)) {
			Decimal change = MainActivity.getInstance().getNumpadPayFragment().getChange();
			if (change.isPositive()) {
				handleChange();
				feedLines(1);
			}
            handleVat();
        }

		handleExtraBottom();
        feedLines(1);
		if (MainActivity.getInstance().isConnected()) {
			printBarCode(String.valueOf(order.getShopId() + order.getId()), "1O1");
		} else {
			printBarCode(String.valueOf(order.getAlternativeId()), "1A1");
		}

		boolean isInvoice = false;
		if (order != null && order.getPayments() != null && order.getPayments().size() > 0) {
			for (Payment payment : order.getPayments()) {
				if (payment.getType() == PaymentType.INVOICE) {
					isInvoice = true;
				}
			}
		}
		if (isInvoice) {
			feedLines(1);
			addLine(makeLine(context.getString(R.string.signature), "", width, lm, rm));
			addLine(makeLine("", rightLineOfCharOfSize(' ', priceExtraSpace), width, lm, rm));
			addLine(makeLine("", rightLineOfCharOfSize('-', width-lm-rm), width, lm, rm));
		}
		
		handleCleanCash();
    }

    protected void handleExtraTitle(StringBuilder receiptData, int width) {
		if (AccountManager.INSTANCE.getSavedReceiptHeader() != null && AccountManager.INSTANCE.getSavedReceiptHeader().trim().length() > 0) {
			feedLines(1);
			addLine(AccountManager.INSTANCE.getSavedReceiptHeader());
			feedLines(1);
		}
	}

    protected void handleExtraBottom() {
		if (order.hasNote()) {
			feedLines(1);
			addLine(order.getNote());
			feedLines(1);
			feedLines(1);
		}
		
		if (AccountManager.INSTANCE.getSavedReceiptFooter() != null && AccountManager.INSTANCE.getSavedReceiptFooter().trim().length() > 0) {
			feedLines(1);
			addLine(AccountManager.INSTANCE.getSavedReceiptFooter());
			feedLines(1);
		}
	}

    protected void handleOrderLines() {
        printOrderLines(width);              //ORDER LINES
        printDivisionLine();
        printOrderTotalWithDiscount(width);  //ORDER TOTAL
	}

    protected void handleChange() {
        printChange();                  //CHANGE
        receiptData.append(makeLine("", rightLineOfCharOfSize('=', priceExtraSpace), width, lm, rm));
    }

    protected void handlePaymentMethods() {
        printPaymentMethods();          //PAYMENT METHODS
        if (order.getPayments().size() > 0)
            receiptData.append(makeLine("", rightLineOfCharOfSize('-', priceExtraSpace), width, lm, rm));
    }

	protected void handleCleanCash() {
		if (order.getReceiptId() > 0) {
			feedLines(3);
			receiptData.append(makeCenterizedLine("" + order.getReceiptId(), width));
		}
		if (order.getDeviceSerialNumber() != null && order.getDeviceSerialNumber().length() > 0 ) {
			receiptData.append(makeCenterizedLine(order.getDeviceSerialNumber(), width));
		}
		if (order.getControlCode() != null && order.getControlCode().length() > 0 ) {
			receiptData.append(makeCenterizedLine(order.getControlCode(), width));
		}
    }
    
    protected void handleVat() {
        printSpecifyVat();              //VAT START
        printVatHeaders();              //VAT HEADERS
        printVatDetails(width);              //VAT DETAILS
        receiptData.append(makeLine("", rightLineOfCharOfSize('=', (width - lm - rm) - qtySpace - 5), width, lm, rm));

    }
	
	protected void table(Context context) {
		addLine(makeLine((context.getString(R.string.table) + ":" + makeSpace(1) + order.getTable()), context.getString(R.string.area) + ":" + makeSpace(1) + order.getArea(), width, lm, rm));
	}
	
	protected void printBarCode(String number, String prefix) {
        StringBuilder barcode = new StringBuilder();
        //32 is the size of the string after R. for 1O1 plus 8 numbers, use 32
        //for 1O1 plus 6 numbers, use 30
        //doing 24 + length
		
		barcode.append("\u001B|");
		barcode.append(String.valueOf(24 + number.length()));
		barcode.append("Rs109h100w400a-2t-13d" + prefix);
		barcode.append(number);
		barcode.append("e");
        receiptData.append(barcode.toString());
    }

    protected int sendReceiptToPrinter(final LinePrinterDeviceBase device, final String receiptData) {
        LinePrinter printer;
		
		printer = new LinePrinter();

        String errTitle = "";
        int ret = LinePrinter.Response.SUCCESS;

        //Sets the charset locale / language / whatever
        device.setMulticharMode(LinePrinterDeviceBase.PAGEMODE_USER);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.getInstance());
        alertDialogBuilder.setTitle(errTitle);
        alertDialogBuilder.setCancelable(false);

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
            if (ret != LinePrinter.Response.SUCCESS) {
                break ErrBlock;
            }

        } catch (Exception e) {
            ErrorReporter.INSTANCE.filelog("CasioPrintOrder", "sendReceiptToPrinter", e);
            alertDialogBuilder.setMessage("Exception:" + e.toString());
            alertDialogBuilder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            alertDialogBuilder.show();
        }

		ErrorReporter.INSTANCE.filelog("CasioPrintOrder ret = " + ret);

        if (ret == LinePrinter.Response.ERR_POWER_FAILURE) {
            receiveIntent(device, receiptData, errTitle, ret);
        }

		return ret;
    }

    protected void receiveIntent(final LinePrinterDeviceBase device, final String receiptData, final String errTitle, final int ret) {
        intentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setTitle(errTitle);
                alertDialogBuilder.setCancelable(false);

                alertDialogBuilder.setMessage(context.getString(R.string.powererrormsg_retry));
                alertDialogBuilder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //reprint
                        sendReceiptToPrinter(device, receiptData);
                    }
                });
                alertDialogBuilder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                context.unregisterReceiver(intentReceiver);
                alertDialogBuilder.show();
            }
        };
        context.registerReceiver(intentReceiver, intentFilter);
    }
}
