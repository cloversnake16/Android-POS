package no.susoft.mobile.pos.hardware.printer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.Customer;
import no.susoft.mobile.pos.data.OrderLine;
import no.susoft.mobile.pos.data.Payment;
import no.susoft.mobile.pos.data.VatGroup;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

/**
 * Created by Vilde on 16.12.2015.
 */
public abstract class _PrinterUtils {

    protected int qtySpace = 9;
    protected int priceSpace = 9;
    protected int priceExtraSpace = priceSpace + 3;
    protected int width = 0;
    protected int lm = 0;
    protected int rm = 0;

    //==================================================================================================================
    //==================================================================================================================
    //Formatting lines and stuff

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

    //Takes a line half the width of the screen and puts the left string left and the right string right. Used in a pair to create vat strings
    protected String formatVatLine(String left, String right, int halfwidthMinusMargin) {
        String str = "";
        str += left;

        //Give quantity 9 spaces, fill rest with space. Length after this should be 9
        for (int i = 0; i < qtySpace - left.length(); i++) {
            str += " ";
        }
        str = addToEndOfString(str, right, halfwidthMinusMargin);

        return str;
    }

    protected String formatVatLineNoCode(String left, String middle, String right, int widthMinusMargin) {
        String str = "";

        str += left;

        //give the left bit a quarter of the space
        while (str.length() < widthMinusMargin / 4) {
            str += " ";
        }

        while (str.length() < ((widthMinusMargin / 8) * 3) + (widthMinusMargin / 4) - middle.length()) {
            str += " ";
        }
        str += middle;

        while (str.length() < ((widthMinusMargin / 8) * 6) + (widthMinusMargin / 4) - right.length()) {
            str += " ";
        }
        str += right;

        while (str.length() < widthMinusMargin) {
            str += " ";
        }

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

    protected ArrayList<String> formatProductName(String pName, int widthMinusMargin, int priceSize) {

        ArrayList<String> strs = new ArrayList<>(Arrays.asList(pName.split(" ")));
        ArrayList<String> returnstring = new ArrayList<>();

        String str = "";
        int firstLineSpace = widthMinusMargin - priceSize - qtySpace;
        int lineSpace = widthMinusMargin;
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
                //while less words have been gone through than there are words,
                // and the length of this string plus the next word plus one space between is less than the linespace
                while (counter < strs.size() && (str.length() + strs.get(counter).length() + 1) < lineSpace) {
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

    protected String addToEndOfString(String startStr, String endStr, int widthMinusMargin) {
        String str = "";
        str += startStr;
        for (int i = 0; i < (widthMinusMargin - startStr.length()); i++) {
            str += " ";
        }
        str = str.substring(0, str.length() - endStr.length() - 1);
        str += endStr;
        return str;
    }

    protected String makeLine(String leftStr, String rightStr, int width, int leftMargin, int rightMargin) {
        String str = "";

        int rightPadding = width - (leftMargin + leftStr.length()) - (rightStr.length() + rightMargin);

        if (leftStr.length() + rightStr.length() >= (width - leftMargin - rightMargin)) {
            rightPadding = width - rightStr.length() - rightMargin;
            str = makeSpace(leftMargin) + leftStr + "\r\n" // 1Line
                    + makeSpace(rightPadding) + rightStr + "\r\n"; //2Line
        } else {
            str = makeSpace(leftMargin) + leftStr + makeSpace(rightPadding) + rightStr + "\r\n";
        }

        return str;
    }

    protected String makeSpace(int spaceSize) {
        String str = "";

        for (int i = 0; i < spaceSize; i++) {
            str += (" ");
        }
        return str;
    }

    protected String makeCenterizedLine(String centerStr, int width) {
        String str = "";
        for (int i = 0; i < ((width - (centerStr.length())) / 2); i++) {
            str += " ";
        }
        str += centerStr;
        while (str.length() < width) {
            str += " ";
        }
        return str;
    }

    protected String makeCenterizedLineLargeFont(String centerStr, int width) {
		if (centerStr != null) {
			width = width / 2;
			String str = "";
			while (str.length() < (((width - centerStr.length()) / 2))) {
				str += " ";
			}
			str += centerStr;
			while (str.length() < width) {
				str += " ";
			}
			return str;
		}
        return "";
    }


    protected String rightLineOfCharOfSize(char lineChar, int sizeFromRight) {
        String str = "";

        for (int i = 0; i < sizeFromRight; i++) {
            str += lineChar;
        }

        return str;
    }

    //==================================================================================================================
    //==================================================================================================================
    //FINSHED LINES

    protected void chain() {
        addLine(makeCenterizedLineLargeFont(AccountManager.INSTANCE.getSavedShopName(), width));
    }

    protected void chain(Context context) {
        addLine(makeCenterizedLine(AccountManager.INSTANCE.getSavedShopName(), width));
		if (AccountManager.INSTANCE.getSavedShopAddress() != null && !AccountManager.INSTANCE.getSavedShopAddress().trim().isEmpty()) {
			addLine(makeCenterizedLine(AccountManager.INSTANCE.getSavedShopAddress(), width));
		}
		if (AccountManager.INSTANCE.getSavedShopCity() != null && !AccountManager.INSTANCE.getSavedShopCity().trim().isEmpty()) {
			addLine(makeCenterizedLine(AccountManager.INSTANCE.getSavedShopZip() + makeSpace(1) + AccountManager.INSTANCE.getSavedShopCity(), width));
		}
		if (AccountManager.INSTANCE.getSavedShopPhone() != null && !AccountManager.INSTANCE.getSavedShopPhone().trim().isEmpty()) {
			addLine(makeCenterizedLine(context.getString(R.string.tlf) + ":" + makeSpace(1) + AccountManager.INSTANCE.getSavedShopPhone(), width));
		}
		if (AccountManager.INSTANCE.getSavedOrgNo() != null && !AccountManager.INSTANCE.getSavedOrgNo().trim().isEmpty()) {
			addLine(makeCenterizedLine(context.getString(R.string.orgno) + ":" + makeSpace(1) + AccountManager.INSTANCE.getSavedOrgNo() + "MVA", width));
		}
    }

	protected void copy(Context context) {
		addLine(makeCenterizedLineLargeFont(context.getString(R.string.copy), width - lm - rm));
	}

    protected void shop(Context context, String shopId) {
        addLine(makeLine(context.getString(R.string.shop) + ":" + makeSpace(1) + shopId + makeSpace(2) + AccountManager.INSTANCE.getSavedShopName(), "", width, lm, rm));
    }

    protected void date(Context context, Date date) {
        addLine(makeLine((context.getString(R.string.date) + ":" + makeSpace(1) + new SimpleDateFormat("dd/MM/yyyy").format(date)), "", width, lm, rm));
    }

    protected void time(Context context, long time) {
        //Print time in hh:mm:ss
        addLine(makeLine(context.getString(R.string.time) + ":" + makeSpace(1) + (DateFormat.format("kk:mm:ss", time)), "", width, lm, rm));
    }

    protected void datetime(Context context, Date date) {
		if (date == null) {
			date = new Date();
		}
		addLine(makeLine((context.getString(R.string.date) + ":" + makeSpace(1) + new SimpleDateFormat("dd/MM/yyyy").format(date)), context.getString(R.string.time) + ":" + makeSpace(1) + (DateFormat.format("kk:mm:ss", date.getTime())), width, lm, rm));
    }

    protected void salesPerson(Context context, String name) {
        addLine(makeLine(context.getString(R.string.tlf) + ":" + makeSpace(1) + AccountManager.INSTANCE.getSavedShopPhone(), context.getString(R.string.salesperson) + ":" + makeSpace(1) + name, width, lm, rm));
    }

    protected void shopAndSalesPerson(Context context, String shopId, String salesPersonId) {
        addLine(makeLine(context.getString(R.string.shop) + ":" + makeSpace(1) + shopId  + makeSpace(2) + AccountManager.INSTANCE.getSavedShopName(), "", width, lm, rm));
        addLine(makeLine(context.getString(R.string.tlf) + ":" + makeSpace(1) + AccountManager.INSTANCE.getSavedShopPhone(), context.getString(R.string.salesperson) + ":" + makeSpace(1) + salesPersonId, width, lm, rm));
    }

    protected void orderNumber(Context context, long id) {
		String orgNo = AccountManager.INSTANCE.getSavedOrgNo();
        addLine(makeLine(context.getString(R.string.number) + ":" + makeSpace(1) + id, (orgNo != null && !orgNo.trim().isEmpty() ? context.getString(R.string.orgno) + ":" + makeSpace(1) + orgNo + "MVA" : ""), width, lm, rm));
    }

    protected void orderNumber(Context context, String id) {
		String orgNo = AccountManager.INSTANCE.getSavedOrgNo();
        addLine(makeLine(context.getString(R.string.number) + ":" + makeSpace(1) + id, (orgNo != null && !orgNo.trim().isEmpty() ? context.getString(R.string.orgno) + ":" + makeSpace(1) + orgNo + "MVA" : ""), width, lm, rm));
    }

    protected void customer(Context context, Customer c) {
		if (c != null && c.getId().length() > 0) {
			String name = c.getFirstName() + " " + c.getLastName();
			if (c.isCompany()) {
				name = c.getLastName();
			}
			addLine(makeLine(context.getString(R.string.customer) + ":" + makeSpace(1) + c.getId() + makeSpace(2) + name, "", width, lm, rm));
		}
    }

    protected String getPaymentTypeString(Payment.PaymentType type, Context context) {
        Log.i("vilde", "Payment type: " + type.toString());
        switch(type) {
            case CASH: return context.getString(R.string.cash);
            case CARD: return context.getString(R.string.card);
            case GIFT_CARD: return context.getString(R.string.gift_card);
            case TIP: return context.getString(R.string.tip);
            case INVOICE: return context.getString(R.string.invoice);
            default: return context.getString(R.string.payment_type);
        }
    }

    protected abstract void addLine(String line);
    protected abstract void addLine(byte[] cmd);
}
