package no.susoft.mobile.pos.hardware.combi;

import java.util.ArrayList;
import java.util.List;

import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Prepaid;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class mPOPPrintPrepaidSlim extends mPOPPrintOrderSlim {
    private final List<Prepaid> prepaids;
    private Prepaid prepaid;

    public mPOPPrintPrepaidSlim(List<Prepaid> prepaids) {
		width = slimWidth;
		this.prepaids = prepaids;
		try {
			for (Prepaid p : prepaids) {
				this.prepaid = p;
				String barcode = "1G1" + prepaid.getNumber();
				Communication.sendCommands(convertArrayListToBytes(PreparePrepaidPrint(p), barcode), Star_mPOP.getPort(), MainActivity.getInstance());
			}
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog("mPOPPrintPrepaidSlim", "Error", e);
			e.printStackTrace();
		}
	}

    protected ArrayList<Object> PreparePrepaidPrint(Prepaid p) {
        context = MainActivity.getInstance();

        output = new ArrayList<>();
        try {
            handleHeaderPrint(p);
            handleMainBodyPrint();
            handleExtraBottomPrint();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;

    }

    private void handleHeaderPrint(Prepaid p) {
        printLargeText();
        chain();
        printNormalText();
        datetime(context, p.getIssuedDate());
		shop(context, p.getShopId());
        salesPerson(context, p.getSalespersonId());
        orderNumber(context, p.getId());
        printRightLineOfLength('-', width);
    }

    @Override
    protected void handleMainBodyPrint() {
        printExtraTitle();
        printRightLineOfLength(' ', width);
        printGiftCardNumber();
        //printBarCode();
        //printBarcodeTest();
        printAmount();
        printValidityDate();
        printRightLineOfLength(' ', width);
        printRightLineOfLength(' ', width);

    }

    @Override
    protected void printExtraTitle() {
        printLargeText();
        if (prepaid.getType().equalsIgnoreCase("C")) {
            addLine(makeCenterizedLineLargeFont(context.getString(R.string.credit_voucher), width));
        } else {
            addLine(makeCenterizedLineLargeFont(context.getString(R.string.gift_card), width));
        }
        printNormalText();

    }

	protected void printExtraFooter() {

    }

    protected void printGiftCardNumber() {
//        output.add(makeCenterizedLine(
//                context.getString(R.string.gift_card) + context.getString(R.string.number) + ": " + giftcard.getId(), width));
        addLine(makeCenterizedLine(context.getString(R.string.number) + ": " + prepaid.getNumber(), width));
    }


    protected void printAmount() {
        printLargeText();
        addLine(makeCenterizedLineLargeFont(context.getString(R.string.amount) + " " + prepaid.getAmount(), width));
        printNormalText();
    }

    protected void printValidityDate() {
        if(prepaid.getDueDate() != null) {
            addLine(makeCenterizedLine(context.getString(R.string.valid_to) + ": " + prepaid.getDueDate().toString(), width));
        }
    }

}
