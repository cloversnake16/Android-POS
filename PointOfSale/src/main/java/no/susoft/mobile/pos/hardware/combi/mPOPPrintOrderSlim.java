package no.susoft.mobile.pos.hardware.combi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import android.util.Log;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.data.OrderLine;
import no.susoft.mobile.pos.data.Payment;
import no.susoft.mobile.pos.data.Payment.PaymentType;
import no.susoft.mobile.pos.hardware.printer.BluetoothPrintOrderSlim;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class mPOPPrintOrderSlim extends BluetoothPrintOrderSlim {

    protected static boolean returnsPrinted = false;

    public mPOPPrintOrderSlim(final Order order) {
		this(order, false);
    }

    public mPOPPrintOrderSlim(final Order order, boolean isCopy) {
        width = 32;
        this.order = order;
		this.isCopy = isCopy;
		String barcode = "1O1" + order.getShopId() + String.valueOf(order.getId());
		if (!MainActivity.getInstance().isConnected()) {
			barcode = "1A1" + order.getAlternativeId();
		}
		Communication.sendCommands(convertArrayListToBytes(PreparePrint(order), barcode), Star_mPOP.getPort(), MainActivity.getInstance());
    }

    public mPOPPrintOrderSlim(final ArrayList<Object> printLines) {
        width = 32;
		Communication.sendCommands(convertArrayListToBytes(printLines, null), Star_mPOP.getPort(), MainActivity.getInstance());
    }

    public mPOPPrintOrderSlim() {
    }

    @Override
    protected void handleExtraBottomPrint(){
		printExtraFooter();

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
		
		handleCleanCash();
    }
    
	protected void handleCleanCash() {
		if (order.getReceiptId() > 0) {
			feedLines(3);
			addLine(makeCenterizedLine("" + order.getReceiptId(), width));
		}
		if (order.getDeviceSerialNumber() != null && order.getDeviceSerialNumber().length() > 0 ) {
			addLine(makeCenterizedLine(order.getDeviceSerialNumber(), width));
		}
		if (order.getControlCode() != null && order.getControlCode().length() > 0 ) {
			addLine(makeCenterizedLine(order.getControlCode(), width));
		}
    }

    protected byte[] convertArrayListToBytes(ArrayList<Object> printLines, String barcode) {
        mPOP_CommandDataList commands = new mPOP_CommandDataList();

        //nordic char set?
        commands.add(nordicCharset());
        commands.add(getLineSpacing());

        for(Object o : printLines) {
            if(o.getClass().equals(String.class)) {
                commands.add(extendedAsciiReplaceNordicChars((String) o));
            } else {
                commands.add((byte[]) o);
            }
        }

        commands.add(feedLines(2));

		if (barcode != null && !barcode.isEmpty()) {
			ArrayList<byte[]> barcodes = getCode93(barcode, (byte) 50, (byte) 80, (byte) 49);
			for (byte[] b : barcodes) {
				commands.add(b);
			}
			commands.add(feedLines(2));
		}

        commands.add(cutPaper());                    // Cut Paper
        return commands.getByteArray();
    }

    public static ArrayList<byte[]> getCode93(String barcodeData, byte barCodeOption, byte height, byte width) {
        ArrayList<byte[]> commands = new ArrayList<>();

//        byte n1 = 0x37;
//        byte n2 = 0;
//        switch (option) {
//            case No_Added_Characters_With_Line_Feed:
//                n2 = 49;
//                break;
//            case Adds_Characters_With_Line_Feed:
//                n2 = 50;
//                break;
//            case No_Added_Characters_Without_Line_Feed:
//                n2 = 51;
//                break;
//            case Adds_Characters_Without_Line_Feed:
//                n2 = 52;
//                break;
//        }
//        byte n3 = 0;
//        switch (width) {
//            case _2_dots:
//                n3 = 49;
//                break;
//            case _3_dots:
//                n3 = 50;
//                break;
//            case _4_dots:
//                n3 = 51;
//                break;
//        }
//        byte n4 = height;
        byte[] command = new byte[6 + barcodeData.getBytes().length + 1];
        command[0] = 0x1b;
        command[1] = 0x62;
        command[2] = 0x37;              //n1
        command[3] = barCodeOption;     //n2
        command[4] = width;             //n3
        command[5] = height;            //n4
        for (int index = 0; index < barcodeData.getBytes().length; index++) {
            command[index + 6] = barcodeData.getBytes()[index];
        }
        command[command.length - 1] = 0x1e;

        commands.add(command);
        return commands;
    }

    protected byte[] getBarcodeBytes(String number){
        //number = 0x1b + "1O1" + 0x1b + number;
        byte[] one = new byte[]{0x1b, 0x62, 0x37, 0x02, 0x01, 0x3C}; //x, x, barcodetype code93, text under position and feed, 2 dots, height
        byte[] two = new byte[0];
        try {
            two = number.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] finalBytes;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            outputStream.write(one);
            //outputStream.write(37);
            outputStream.write("1O1".getBytes("UTF-8"));
            //outputStream.write(0x25);
            outputStream.write(two);
//            outputStream.write(String.valueOf(length).getBytes());
            outputStream.write(0x1e); //end
        } catch (IOException e) {
            e.printStackTrace();
        }

        finalBytes = outputStream.toByteArray();

        Log.i("vilde", arrayToString(finalBytes));
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return finalBytes;
    }

    private String arrayToString(byte[] array) {
        String output = "";
        for(byte b : array) {
            output+= String.valueOf(b) + ", ";
        }
        return output;
    }

    protected byte[] feedLines(int i) {
        return new byte[]{0x1b, 0x61, Byte.valueOf(String.valueOf(i)) };
    }

    @Override
    protected byte[] getLineSpacing() {
        return new byte[]{0x1b, 0x30};
    }

    private byte[] nordicCharset() {
//        return new byte[]{0x1b, 0x52, 0x09};
        return new byte[]{0x1b, 0x1d, 0x74, 0x09};
    }

    private byte[] cutPaper() {
        return new byte[]{0x1b, 0x64, 0x03};
    }

    @Override
    protected void printLargeText() {
        addLine(new byte[]{0x09, 0x1b, 0x69, 0x01, 0x01});         // Set DoubleHW
    }

    @Override
    protected void printNormalText() {
        addLine(new byte[]{0x1b, 0x69, 0x00, 0x00});
    }

    protected void handleOrderLinesPrint() {
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

			if (thisLine.getComponents() != null) {
				for (OrderLine cLine : thisLine.getComponents()) {
					if (!cLine.getAmount(true).isZero()) {
						prodName = formatProductName(cLine.getProduct().getName(), width - lm - rm, priceSpace);
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
//            if (!thisLine.getAmount(true).isEqual(thisLine.getAmount(false))) {
//                Decimal discAmount = thisLine.getAmount(false).subtract(thisLine.getAmount(true));
//                receiptData.append(makeLine("",
//                        "(" + thisLine.getAmount(false).toString() +
//                                " - " + thisLine.getDiscount().getPercent().toString() +
//                                "%/" + thisLine.getDiscountAmount().toString() + ")",
//                        width, lm, rm+1));
//                receiptData.append(makeLine("", context.getString(R.string.original_price) + " " + thisLine.getAmount(false).toString(), width, lm, rm+1));
//                receiptData.append(makeLine("", context.getString(R.string.discount) + " " + thisLine.getDiscount().getPercent() + "% (" + discAmount.toString() + ")", width, lm, rm+1));
        }

        printRightLineOfLength('-', width);

        //Space between lines
//            if (i < order.getLines().size() - 1) {
//                receiptData.append("\u001b|lF");//feed
//            }
//        }
    }

}
