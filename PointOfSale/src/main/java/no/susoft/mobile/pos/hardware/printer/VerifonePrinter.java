package no.susoft.mobile.pos.hardware.printer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.text.format.DateFormat;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.*;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.hardware.terminal.CardTerminal;
import no.susoft.mobile.pos.hardware.terminal.CardTerminalFactory;
import no.susoft.mobile.pos.hardware.terminal.VerifonePim;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

import static no.susoft.mobile.pos.hardware.printer.VerifonePimHelper.*;

/**
 * Created on 6/16/2016.
 */
public class VerifonePrinter implements Printer {

    public static final int lm = 2;
    public static final int rm = 2;
    public static final int priceSpace = 9;
    public static final int priceExtraSpace = priceSpace + 5;
    public static final int qtySpace = 9;

    // @formatter:off
    public static final String                    DIVIDER = "------------------------------------------";
    public static final String  DIVIDER_DOUBLE_SMALL_LONG = "==========================================";
    public static final String  DIVIDER_SMALL_RIGHT_SHORT = "                            --------------";
    public static final String DIVIDER_SMALL_RIGHT_LONGER = "                     ---------------------";
    // @formatter:on

    public static final String ORDER_DATE_FORMAT = "dd/MM/yyyy";
    public static final String TIME_FORMAT = "kk:mm:ss";
    public static final String PREPAID_TIMESTAMP_FORMAT = "dd/MM/yyyy HH:mm:ss";

    private static VerifonePrinter instance = null;
    private static CardTerminal cardTerminal = null;
    private MainActivity context;

    private VerifonePrinter() throws Exception {

        // init printer here
        cardTerminal = CardTerminalFactory.getInstance().getCardTerminal();

        if (!(cardTerminal instanceof VerifonePim)) {
            throw new Exception("Printing on VERIFONE printer is available only when VERIFONE card terminal is connected.");
        }
    }

    public static VerifonePrinter getInstance() {

        if (instance == null) {

            ErrorReporter.INSTANCE.filelog("VerifonePrinter", "Init VerifonePrinter instance");

            try {
                instance = new VerifonePrinter();
            } catch (Exception e) {
                ErrorReporter.INSTANCE.filelog("VerifonePrinter", "Error creating VerifonePrinter instance", e);
            }
        }

        return instance;
    }

    @Override
    public void printOrder(Order order, ReceiptPrintType receiptPrintType) {
        if (cardTerminal instanceof VerifonePim) {
            context = MainActivity.getInstance();

            try {
                cardTerminal.printOnTerminal(printOrderFormatted(order, receiptPrintType, true));
            } catch (no.susoft.mobile.pos.hardware.terminal.exception.CardTerminalException e) {
                e.printStackTrace();
            }
        } else {
            ErrorReporter.INSTANCE.filelog("VerifonePrinter", "Printing on VERIFONE printer is available only when VERIFONE card terminal is connected.");
        }
    }

    private String printOrderFormatted(Order order, ReceiptPrintType receiptPrintType, boolean feedLinesAtEnd) {
        StringBuilder result = new StringBuilder();

        if (receiptPrintType.equals(ReceiptPrintType.COPY)) {
            printOrderCopyHeader(result);
            feedLines(result, 1);
        } else if (receiptPrintType.equals(ReceiptPrintType.PRELIM)) {
            printOrderPrelimHeader(result);
            feedLines(result, 1);
        }

        printShopName(result);
        printOrderDateTime(result, order.getDate());
        printShopAndSalesPerson(result, order.getShopId(), order.getSalesPersonId());
		if (!MainActivity.getInstance().isConnected()) {
			printOrderNumber(result, order.getAlternativeId());
		} else {
			printOrderNumber(result, order.getId());
		}
        printCustomer(result, order.getCustomer());
        printDivisionLine(result);
        printOrderMainBody(result, receiptPrintType, order);

        if (feedLinesAtEnd) feedLines(result, 4);

        return result.toString();
    }

    private void printOrderMainBody(StringBuilder result, ReceiptPrintType receiptPrintType, Order order) {

        printOrderLines(result, SMALL_FONT_WIDTH, order);
        printDivisionLine(result);
        printOrderTotalWithDiscount(result, SMALL_FONT_WIDTH, order);
        printOrderPaymentMethods(result, SMALL_FONT_WIDTH, order);

        if (!receiptPrintType.equals(ReceiptPrintType.PRELIM)) {
            Decimal change = context.getNumpadPayFragment().getChange();

            if (change.isPositive()) {
                result.append(PRINT_PREFIX_SMALL_FONT);
                result.append(makeLine(context.getString(R.string.receipt_change_amount), change.toString(), SMALL_FONT_WIDTH, lm, rm));
                result.append(PRINT_PREFIX_SMALL_FONT).append(DIVIDER_SMALL_RIGHT_SHORT).append(PRINT_NEWLINE);
            }

            result.append(PRINT_PREFIX_SMALL_FONT);
            result.append(makeLine(context.getString(R.string.specify_vat), "", SMALL_FONT_WIDTH, lm, rm));

            result.append(PRINT_PREFIX_SMALL_FONT);
            result.append(makeLine(printOrderFormatVatLine(context.getString(R.string.vat_rate), context.getString(R.string.vat_code), (SMALL_FONT_WIDTH - lm - rm) / 2), printOrderFormatVatLine(context.getString(R.string.purchase), context.getString(R.string.vat), (SMALL_FONT_WIDTH - lm - rm) / 2), SMALL_FONT_WIDTH, lm, rm));

            printOrderVatDetails(result, SMALL_FONT_WIDTH, order);
            result.append(PRINT_PREFIX_SMALL_FONT).append(DIVIDER_DOUBLE_SMALL_LONG).append(PRINT_NEWLINE);
        }

		if (order.hasNote()) {
			feedLines(result, 1);
			result.append(order.getNote());
			feedLines(result, 1);
			feedLines(result, 1);
		}

        //feedLines(result, 1);
		
		String barcode = "1O1" + order.getShopId() + String.valueOf(order.getId());
		if (!MainActivity.getInstance().isConnected()) {
			barcode = "1A1" + order.getAlternativeId();
		}
		
        printOrderBarCode(result, barcode);
        printOrderSignature(result, order);
		
		handleCleanCash(result, order);
    }
	
	protected void handleCleanCash(StringBuilder result, Order order) {
		if (order.getReceiptId() > 0) {
			feedLines(result, 1);
			feedLines(result, 1);
			feedLines(result, 1);
			result.append(PRINT_PREFIX_SMALL_FONT).append(PRINT_PREFIX_ALIGN_CENTER).append("" + order.getReceiptId()).append(PRINT_NEWLINE);
		}
		if (order.getDeviceSerialNumber() != null && order.getDeviceSerialNumber().length() > 0) {
			result.append(PRINT_PREFIX_SMALL_FONT).append(PRINT_PREFIX_ALIGN_CENTER).append(order.getDeviceSerialNumber()).append(PRINT_NEWLINE);
		}
		if (order.getControlCode() != null && order.getControlCode().length() > 0) {
			result.append(PRINT_PREFIX_SMALL_FONT).append(PRINT_PREFIX_ALIGN_CENTER).append(order.getControlCode()).append(PRINT_NEWLINE);
		}
	}
	
	private void printOrderSignature(StringBuilder result, Order order) {
        boolean isInvoice = false;
        if (order != null && order.getPayments() != null && order.getPayments().size() > 0) {
            for (Payment payment : order.getPayments()) {
                if (payment.getType() == Payment.PaymentType.INVOICE) {
                    isInvoice = true;
                }
            }
        }

        if (isInvoice) {
            feedLines(result, 1);
            result.append(makeLine(context.getString(R.string.signature), "", SMALL_FONT_WIDTH, lm, rm));
            result.append(PRINT_PREFIX_SMALL_FONT).append(DIVIDER_SMALL_RIGHT_SHORT).append(PRINT_NEWLINE).append(PRINT_NEWLINE);
            result.append(PRINT_PREFIX_SMALL_FONT).append(DIVIDER_SMALL_RIGHT_SHORT);

        }
    }

    private void printOrderBarCode(StringBuilder result, String barcode) {
        /* june, 22 2016
            [17:21:28] Svein A Lindelid: like:  1O1218305672
            [17:21:39] Svein A Lindelid: and thay need to enter it in manuelly
            [17:21:48] Svein A Lindelid: when it's printed on a verifone
            [17:22:14] Svein A Lindelid: all othere prnter's is handeling code 93
            [17:22:54] Mihail Meleca: so I need to print 1O1+shopid+orderid
            [17:22:58] Mihail Meleca: no barcode
            [17:23:22] Svein A Lindelid: yes
         */
        result.append(PRINT_PREFIX_SMALL_FONT).append(barcode).append(PRINT_NEWLINE);
    }

    protected void printOrderVatDetails(StringBuilder result, int width, Order order) {
        ArrayList<VatGroup> vg = calculateVATForOrderLines(order.getLines());
        Decimal orderSumWithoutVat = Decimal.ZERO;
        Decimal orderVat = Decimal.ZERO;

        for (VatGroup v : vg) {
            orderSumWithoutVat = orderSumWithoutVat.add(v.getSumWithoutVat());
            orderVat = orderVat.add(v.getSumVat());

            result.append(PRINT_PREFIX_SMALL_FONT);
            result.append(makeLine(printOrderFormatVatLine(v.getVatPercent().toString() + "%", "0", (width - lm - rm) / 2),
                    printOrderFormatVatLine(v.getSumWithoutVat().toString(), v.getSumVat().toString(), (width - lm - rm) / 2), width, lm, rm));
        }

        result.append(PRINT_PREFIX_SMALL_FONT).append(DIVIDER_SMALL_RIGHT_LONGER).append(PRINT_NEWLINE);

        result.append(PRINT_PREFIX_SMALL_FONT);
        result.append(makeLine(context.getString(R.string.total_vat), printOrderFormatVatLine(orderSumWithoutVat.toString(),
                orderVat.toString(), (width - lm - rm) / 2), width, lm, rm));
    }

    protected ArrayList<VatGroup> calculateVATForOrderLines(List<OrderLine> orderLines) {

        ArrayList<VatGroup> vatGroups = new ArrayList<>();

        //Add all the different vat groups to the list
        for (OrderLine ol : orderLines) {

            if (vatGroups.size() == 0) {
                vatGroups.add(new VatGroup(Cart.INSTANCE.getProductVat(ol.getProduct())));
            } else {

                boolean add = true;
                for (VatGroup vg : vatGroups) {
                    if (vg.getVatPercent().equals(Cart.INSTANCE.getProductVat(ol.getProduct()))) {
                        add = false;
                    }
                }

                if (add)
                    vatGroups.add(new VatGroup(Cart.INSTANCE.getProductVat(ol.getProduct())));
            }
        }

        //Add sum to the vat objects which will update the rest of its fields automatically
        for (VatGroup vg : vatGroups) {
            for (OrderLine ol : orderLines) {
                if (vg.getVatPercent().equals(Cart.INSTANCE.getProductVat(ol.getProduct()))) {
                    vg.addToPurchaseSumInclVat(ol.getAmount(true));
                }
            }
        }

        return vatGroups;

    }

    protected String printOrderFormatVatLine(String left, String right, int halfwidthMinusMargin) {
        String str = "";
        str += left;

        //Give quantity 9 spaces, fill rest with space. Length after this should be 9
        for (int i = 0; i < qtySpace - left.length(); i++) {
            str += " ";
        }
        str = addToEndOfString(str, right, halfwidthMinusMargin);

        return str;
    }

    protected void printOrderPaymentMethods(StringBuilder result, int width, Order order) {

        for (int i = 0; i < order.getPayments().size(); i++) {
            Payment pmnt = order.getPayments().get(i);

            String type;
            switch (pmnt.getType()) {
                case CARD:
                    type = context.getString(R.string.card);
                    break;
                case CASH:
                    type = context.getString(R.string.cash);
                    break;
                case GIFT_CARD:
                    type = context.getString(R.string.gift_card);
                    break;
                case TIP:
                    type = context.getString(R.string.tip);
                    break;
                case INVOICE:
                    type = context.getString(R.string.invoice);
                    break;
                default:
                    type = "";
                    break;
            }

            result.append(PRINT_PREFIX_SMALL_FONT)
                    .append(makeLine(type, pmnt.getAmount().toString(), width, lm, rm));
        }
    }

    protected void printOrderTotalWithDiscount(StringBuilder result, int width, Order order) {

        if (!order.getAmount(true).isEqual(order.getAmount(false))) {
            result.append(PRINT_PREFIX_SMALL_FONT);
            result.append(makeLine(context.getString(R.string.total_discount) + ":", order.getAmount(false).subtract(order.getAmount(true)).toString(), width, lm, rm));
            printDivisionLine(result);
        }

        result.append(PRINT_PREFIX_SMALL_FONT);
        result.append(makeLine(context.getString(R.string.total_purchase) + ":", order.getAmount(true).toString(), width, lm, rm));

        result.append(PRINT_PREFIX_SMALL_FONT).append(DIVIDER_SMALL_RIGHT_SHORT).append(PRINT_NEWLINE);
    }

    protected void printOrderLines(StringBuilder result, int width, Order order) {
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
                        result.append(PRINT_PREFIX_SMALL_FONT);
                        result.append(makeLine(formatQtyIdPriceLine(thisLine.getQuantity().toString(), prodName.get(j),
                                "-" + thisLine.getDiscount().getPercent().add(Decimal.make(0.5)).toInteger() + "% " + thisLine.getAmount(true).toString(),
                                width - lm - rm), "", width, lm, rm));
                    } else {
                        result.append(PRINT_PREFIX_SMALL_FONT);
                        result.append(makeLine(formatQtyIdPriceLine(thisLine.getQuantity().toString(), prodName.get(j), thisLine.getAmount(true).toString(), width - lm - rm), "", width, lm, rm));

                    }
                } else {
                    result.append(PRINT_PREFIX_SMALL_FONT);
                    result.append(makeLine(prodName.get(j), "", width, lm, rm));
                }
            }

			if (thisLine.getComponents() != null) {
				for (OrderLine cLine : thisLine.getComponents()) {
					if (!cLine.getAmount(true).isZero()) {
						result.append(PRINT_PREFIX_SMALL_FONT);
						result.append(makeLine(formatQtyIdPriceLine("", cLine.getProduct().getId(), cLine.getAmount(true).toString(), width - lm - rm), "", width, lm, rm));
						prodName = formatProductName(cLine.getProduct().getName(), width - lm - rm, priceSpace);
						for (String aProdName : prodName) {
							result.append(PRINT_PREFIX_SMALL_FONT);
							result.append(makeLine(aProdName, "", width, lm, rm));
						}
					}
				}
			}
        }
    }

    protected String makeLine(String leftStr, String rightStr, int width, int leftMargin, int rightMargin) {
        String str;

        int rightPadding = width - (leftMargin + leftStr.length()) - (rightStr.length() + rightMargin);

        if (leftStr.length() + rightStr.length() >= (width - leftMargin - rightMargin)) {
            rightPadding = width - rightStr.length() - rightMargin;
            str = makeSpace(leftMargin) + leftStr + PRINT_NEWLINE // 1Line
                    + makeSpace(rightPadding) + rightStr + PRINT_NEWLINE; //2Line
        } else {
            str = makeSpace(leftMargin) + leftStr + makeSpace(rightPadding) + rightStr + PRINT_NEWLINE;
        }

        return str;
    }

    protected String makeSpace(int spaceSize) {
        String str = "";
        for (int i = 0; i < spaceSize; i++) str += (" ");

        return str;
    }

    protected String formatQtyIdPriceLine(String qty, String id, String price, int widthMinusMargin) {

        String str = "";
        str += qty;

        //Give quantity 9 spaces, fill rest with space. Length after this should be 9
        for (int i = 0; i < qtySpace - qty.length(); i++) {
            str += " ";
        }

        str += id;
        str = addToEndOfString(str, price, widthMinusMargin);

        return str;
    }

    protected String addToEndOfString(String startStr, String endStr, int widthMinusMargin) {

        String str = "";
        str += startStr;

        for (int i = 0; i < (widthMinusMargin - startStr.length()); i++) str += " ";

        str = str.substring(0, str.length() - endStr.length() - 1);
        str += endStr;

        return str;
    }

    protected ArrayList<String> formatProductName(String pName, int widthMinusMargin, int priceSize) {

        ArrayList<String> strs = new ArrayList<>(Arrays.asList(pName.split(" ")));
        ArrayList<String> returnstring = new ArrayList<>();

        String str = "";
        int firstLineSpace = widthMinusMargin - priceSize - qtySpace;
        //If the product name fits in its allocated first-line space, return the string immediately after adding the spaces in front
        if (str.length() + pName.length() <= firstLineSpace) {
            str += pName;
            returnstring.add(str);
            return returnstring;

            //Else format it so that it fits first line and wraps words
        } else {

            int counter = 0;
            str += strs.get(counter);   //add the first word
            counter++;                  //indicate that a word has been used

            //First fill up the first line
            try {
                while (strs.size() >= counter + 1 && (str.length() + strs.get(counter).length() + 1) < firstLineSpace) {
                    str += " " + strs.get(counter);
                    counter++;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            //add the first line to the list
            returnstring.add(str);

            //while less words have been gone through than there are words
            while (counter < strs.size()) {

                //reset string by putting in the left padding of empty spaces
                str = makeSpace(qtySpace);

                boolean addedString = false;
                // while less words have been gone through than there are words,
                // and the length of this string plus the next word plus one space between is less than the linespace
                while (counter < strs.size() && (str.length() + strs.get(counter).length() + 1) < widthMinusMargin) {
                    str += makeSpace(1) + strs.get(counter);
                    counter++;
                    addedString = true;
                }
                returnstring.add(str);
                if (!addedString) {
                    counter++;
                }
            }
        }

        return returnstring;
    }

    private void printDivisionLine(StringBuilder result) {
        result.append(PRINT_PREFIX_SMALL_FONT)
                .append(DIVIDER) // fast and dirty :)
                .append(PRINT_NEWLINE);
    }

    private void printOrderNumber(StringBuilder result, long orderId) {
        String orgNo = AccountManager.INSTANCE.getAccount().getShop().getOrgNo();
        result.append(PRINT_PREFIX_SMALL_FONT)
                .append(context.getString(R.string.number) + ":" + orderId)
                .append((orgNo != null && !orgNo.trim().isEmpty() ? "    " + context.getString(R.string.orgno) + ":" + makeSpace(1) + orgNo + "MVA" : ""))
                .append(PRINT_NEWLINE);
    }
    
    private void printOrderNumber(StringBuilder result, String orderId) {
        String orgNo = AccountManager.INSTANCE.getAccount().getShop().getOrgNo();
        result.append(PRINT_PREFIX_SMALL_FONT)
                .append(context.getString(R.string.number) + ":" + orderId)
                .append((orgNo != null && !orgNo.trim().isEmpty() ? "    " + context.getString(R.string.orgno) + ":" + makeSpace(1) + orgNo + "MVA" : ""))
                .append(PRINT_NEWLINE);
    }

    private void printCustomer(StringBuilder result, Customer c) {
        if (c != null && c.getId().length() > 0) {
            String name = c.getFirstName() + " " + c.getLastName();
            if (c.isCompany()) {
                name = c.getLastName();
            }

            result.append(PRINT_PREFIX_SMALL_FONT).append(context.getString(R.string.customer) + ": " + c.getId() + "  " + name).append(PRINT_NEWLINE);
        }
    }

    private void printShopAndSalesPerson(StringBuilder result, String shopId, String salesPersonId) {
        result.append(PRINT_PREFIX_SMALL_FONT)
                .append(context.getString(R.string.shop) + ": " + shopId + " " + AccountManager.INSTANCE.getAccount().getShop().getName())
                .append(PRINT_NEWLINE);

        result.append(PRINT_PREFIX_SMALL_FONT)
                .append(context.getString(R.string.tlf) + ": " + AccountManager.INSTANCE.getAccount().getShop().getPhone() + "    ")
                .append(context.getString(R.string.salesperson) + ": " + salesPersonId)
                .append(PRINT_NEWLINE);
    }

    private void printOrderDateTime(StringBuilder result, Date date) {
        result.append(PRINT_PREFIX_SMALL_FONT)
                .append(context.getString(R.string.date) + ": " + new SimpleDateFormat(ORDER_DATE_FORMAT).format(date))
                .append(" " + context.getString(R.string.time) + ": " + new SimpleDateFormat(TIME_FORMAT).format(date))
                .append(PRINT_NEWLINE);
    }

    private void printTime(StringBuilder result, long time) {
        result.append(PRINT_PREFIX_SMALL_FONT)
                .append(context.getString(R.string.time) + ": " + (DateFormat.format(TIME_FORMAT, time)))
                .append(PRINT_NEWLINE);
    }

    private void printOrderDate(StringBuilder result, Date date) {
        result.append(PRINT_PREFIX_SMALL_FONT)
                .append(context.getString(R.string.date) + ": " + new SimpleDateFormat(ORDER_DATE_FORMAT).format(date))
                .append(PRINT_NEWLINE);
    }

    private void printChainName(StringBuilder result) {

        String chainName = AccountManager.INSTANCE.getAccount().getChainName();

        if (chainName.trim().length() <= LARGE_FONT_WIDTH) {
            result.append(PRINT_PREFIX_LARGE_FONT)
                    .append(PRINT_PREFIX_ALIGN_CENTER)
                    .append(chainName)
                    .append(PRINT_NEWLINE);
        } else {
            result.append(PRINT_PREFIX_MEDIUM_FONT)
                    .append(PRINT_PREFIX_ALIGN_CENTER)
                    .append(chainName)
                    .append(PRINT_NEWLINE);
        }
    }

    private void printShopName(StringBuilder result) {

        String shopName = AccountManager.INSTANCE.getAccount().getShop().getName();

        if (shopName.trim().length() <= LARGE_FONT_WIDTH) {
            result.append(PRINT_PREFIX_LARGE_FONT)
                    .append(PRINT_PREFIX_ALIGN_CENTER)
                    .append(shopName)
                    .append(PRINT_NEWLINE);
        } else {
            result.append(PRINT_PREFIX_MEDIUM_FONT)
                    .append(PRINT_PREFIX_ALIGN_CENTER)
                    .append(shopName)
                    .append(PRINT_NEWLINE);
        }
    }

    private void printOrderPrelimHeader(StringBuilder result) {
        result.append(PRINT_PREFIX_ALIGN_CENTER + PRINT_PREFIX_LARGE_FONT)
                .append(context.getString(R.string.prelim)).append(PRINT_NEWLINE);
    }

    private void printOrderCopyHeader(StringBuilder result) {
        result.append(PRINT_PREFIX_ALIGN_CENTER + PRINT_PREFIX_LARGE_FONT)
                .append(context.getString(R.string.copy)).append(PRINT_NEWLINE);
    }

    private void feedLines(StringBuilder result, int num) {
        for (int i = 0; i < num; i++) result.append(PRINT_NEWLINE);
    }

    public void printPrepaid(List<Prepaid> giftCards) {
        if (cardTerminal instanceof VerifonePim) {
            context = MainActivity.getInstance();
            try {
                cardTerminal.printOnTerminal(printPrepaidFormatted(giftCards));
            } catch (no.susoft.mobile.pos.hardware.terminal.exception.CardTerminalException e) {
                e.printStackTrace();
            }
        } else {
            ErrorReporter.INSTANCE.filelog("VerifonePrinter", "Printing on VERIFONE printer is available only when VERIFONE card terminal is connected.");
        }
    }

    @Override
    public void printPrepaidJoined(Order order, List<Prepaid> giftCards) {
        if (cardTerminal instanceof VerifonePim) {
            context = MainActivity.getInstance();
            try {

                StringBuilder result = new StringBuilder(printOrderFormatted(order, ReceiptPrintType.ORIGINAL, false));
                feedLines(result, 1);
                printDivisionLine(result);
                feedLines(result, 1);
                result.append(printPrepaidFormatted(giftCards));

                cardTerminal.printOnTerminal(result.toString());
            } catch (no.susoft.mobile.pos.hardware.terminal.exception.CardTerminalException e) {
                e.printStackTrace();
            }
        } else {
            ErrorReporter.INSTANCE.filelog("VerifonePrinter", "Printing on VERIFONE printer is available only when VERIFONE card terminal is connected.");
        }
    }

    private String printPrepaidFormatted(List<Prepaid> giftCards) {
        StringBuilder result = new StringBuilder();

        for (Prepaid prepaid : giftCards) {
            if (!prepaid.getAmount().equals(Decimal.ZERO)) {
                printShopName(result);
                printPrepaidDateTime(result, prepaid);                    //DATE
                printShopAndSalesPerson(result, prepaid.getShopId(), prepaid.getSalespersonId());
                printDivisionLine(result);
                printPrepaidMainBody(result, prepaid);
                feedLines(result, 4);
                cutPaper();
            }
        }

        return result.toString();
    }

    private void printPrepaidMainBody(StringBuilder result, Prepaid prepaid) {
        printPrepaidExtraTitle(result, prepaid.getType());
        printPrepaidGiftCardNumber(result, prepaid);

        // not working on this printer. Only EAN supported
        //printPrepaidBarcode(result, prepaid);

        printPrepaidAmount(result, prepaid);
        printPrepaidValidityDate(result, prepaid);
    }

    protected void printPrepaidValidityDate(StringBuilder result, Prepaid prepaid) {
        if (prepaid.getDueDate() != null) {
            result.append(PRINT_PREFIX_SMALL_FONT + PRINT_PREFIX_ALIGN_CENTER);
            result.append(context.getString(R.string.valid_to) + ": " +
                    new SimpleDateFormat(PREPAID_TIMESTAMP_FORMAT).format(prepaid.getDueDate()));
            result.append(PRINT_NEWLINE);
        }
    }

    protected void printPrepaidAmount(StringBuilder result, Prepaid prepaid) {

        if (!prepaid.getAmount().equals(prepaid.getIssuedAmount())) {
            result.append(PRINT_PREFIX_SMALL_FONT + PRINT_PREFIX_ALIGN_CENTER);
            result.append(context.getString(R.string.issued_date) + " " + new SimpleDateFormat(PREPAID_TIMESTAMP_FORMAT).format(prepaid.getIssuedDate()));
            result.append(PRINT_NEWLINE);
            result.append(PRINT_PREFIX_SMALL_FONT + PRINT_PREFIX_ALIGN_CENTER);
            result.append(context.getString(R.string.issued_amount) + " " + prepaid.getIssuedAmount());
            result.append(PRINT_NEWLINE);
        }

        result.append(PRINT_PREFIX_LARGE_FONT + PRINT_PREFIX_ALIGN_CENTER);
        result.append(context.getString(R.string.amount) + " " + prepaid.getAmount());
        result.append(PRINT_NEWLINE);
    }

    protected void printPrepaidBarcode(StringBuilder result, Prepaid prepaid) {
        //32 is the size of the string after R. for 1G1 plus 8 numbers, use 32
        //doing 24 + length of number

        result.append(PRINT_PREFIX_SMALL_FONT +
                String.valueOf(24 + prepaid.getNumber().length()) +
                PRINT_PREFIX_BARCODE +
                prepaid.getNumber() +
                PRINT_NEWLINE);
    }

    protected void printPrepaidGiftCardNumber(StringBuilder result, Prepaid prepaid) {
        result.append(PRINT_PREFIX_LARGE_FONT + PRINT_PREFIX_ALIGN_CENTER);
        result.append(context.getString(R.string.number) + ": 1G1" + prepaid.getNumber());
        result.append(PRINT_NEWLINE);
    }

    protected void printPrepaidExtraTitle(StringBuilder result, String type) {
        result.append(PRINT_PREFIX_LARGE_FONT + PRINT_PREFIX_ALIGN_CENTER);
        ErrorReporter.INSTANCE.filelog("VerifonePrinter", "printPrepaidExtraTitle -> type = " + type);

        if (type.equalsIgnoreCase("C")) {
            result.append(context.getString(R.string.credit_voucher).toUpperCase());
        } else {
            result.append(context.getString(R.string.gift_card).toUpperCase());
        }

        result.append(PRINT_NEWLINE);
    }

    private void printPrepaidDateTime(StringBuilder result, Prepaid prepaid) {

        Date date = prepaid.getLastUsedDate() != null
                ? prepaid.getLastUsedDate()
                : prepaid.getIssuedDate();

        result.append(PRINT_PREFIX_SMALL_FONT)
                .append(context.getString(R.string.date) + ": " + new SimpleDateFormat(ORDER_DATE_FORMAT).format(date))
                .append(" " + context.getString(R.string.time) + ": " + new SimpleDateFormat(TIME_FORMAT).format(date))
                .append(PRINT_NEWLINE);
    }

    protected void printPrepaidDate(StringBuilder result, Prepaid prepaid) {

        String date = prepaid.getLastUsedDate() != null
                ? context.getString(R.string.date) + ":" + makeSpace(1) + new SimpleDateFormat("dd/MM/yyyy").format(prepaid.getLastUsedDate())
                : context.getString(R.string.date) + ":" + makeSpace(1) + new SimpleDateFormat("dd/MM/yyyy").format(prepaid.getIssuedDate());

        result.append(PRINT_PREFIX_SMALL_FONT)
                .append(makeLine(date, "", SMALL_FONT_WIDTH, lm, rm))
                .append(PRINT_NEWLINE);
    }

    @Override
    public void printReturn(Order order) {
        if (cardTerminal instanceof VerifonePim) {
            context = MainActivity.getInstance();
            try {
                cardTerminal.printOnTerminal(printOrderFormatted(order, ReceiptPrintType.ORIGINAL, true));
            } catch (no.susoft.mobile.pos.hardware.terminal.exception.CardTerminalException e) {
                e.printStackTrace();
            }
        } else {
            ErrorReporter.INSTANCE.filelog("VerifonePrinter", "Printing on VERIFONE printer is available only when VERIFONE card terminal is connected.");
        }
    }

    public void printSupplyReport(ArrayList<Product> products, Date from, Date to) {
        if (cardTerminal instanceof VerifonePim) {
            context = MainActivity.getInstance();
            try {
                cardTerminal.printOnTerminal(printSupplyReportFormatted(products, from, to));
            } catch (no.susoft.mobile.pos.hardware.terminal.exception.CardTerminalException e) {
                e.printStackTrace();
            }
        } else {
            ErrorReporter.INSTANCE.filelog("VerifonePrinter", "Printing on VERIFONE printer is available only when VERIFONE card terminal is connected.");
        }
    }

    private String printSupplyReportFormatted(ArrayList<Product> products, Date from, Date to) {
        StringBuilder result = new StringBuilder();

        result.append(PRINT_PREFIX_MEDIUM_FONT).append(PRINT_PREFIX_ALIGN_CENTER)
                .append(context.getString(R.string.supply_report))
                .append(PRINT_NEWLINE);

        feedLines(result, 1);

        result.append(PRINT_PREFIX_SMALL_FONT).append(PRINT_PREFIX_ALIGN_CENTER)
                .append(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(from) + " - " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(to))
                .append(PRINT_NEWLINE);
        printDivisionLine(result);

        String categoryId = null;
        for (Product product : products) {
            if (categoryId == null || !product.getCategoryId().equals(categoryId)) {
                categoryId = product.getCategoryId();
                result.append(PRINT_PREFIX_SMALL_FONT);
                result.append(makeLine(product.getCategoryName(), "", SMALL_FONT_WIDTH, lm, rm));
            }
            result.append(PRINT_PREFIX_SMALL_FONT);
            result.append(makeLine(product.getName(), product.getStockQty().toString(), SMALL_FONT_WIDTH, lm, rm));
        }

        return result.toString();
    }


    @Override
    public void cutPaper() {
        // not possible on this printer
    }

    public void printLine(String s) {
        try {
            cardTerminal.printOnTerminal(new StringBuilder().append(PRINT_PREFIX_SMALL_FONT).append(s).append(PRINT_NEWLINE).toString());
        } catch (no.susoft.mobile.pos.hardware.terminal.exception.CardTerminalException e) {
            e.printStackTrace();
        }
    }
}

/*

// supported on Verifone
cardTerminal.printOnTerminal(VerifonePimHelper.PRINT_PREFIX_LARGE_FONT + "Large text" + VerifonePimHelper.PRINT_NEWLINE +
        VerifonePimHelper.PRINT_PREFIX_SMALL_FONT + "small text,small text" + VerifonePimHelper.PRINT_NEWLINE +
        VerifonePimHelper.PRINT_PREFIX_ALIGN_CENTER + "Center" + VerifonePimHelper.PRINT_NEWLINE +
        VerifonePimHelper.PRINT_PREFIX_ALIGN_RIGHT + "to the right" + VerifonePimHelper.PRINT_NEWLINE
        );

*/