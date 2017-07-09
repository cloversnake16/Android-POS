package no.susoft.mobile.pos.hardware.printer;

import java.util.ArrayList;

import android.content.Context;
import android.widget.Toast;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jp.co.casio.vx.framework.device.LinePrinter;
import jp.co.casio.vx.framework.device.lineprintertools.LinePrinterDeviceBase;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.data.ReceiptPrintType;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class CasioPrintCashcount extends CasioPrint {

    protected static boolean hasPrintedReturns = false;
    protected JsonObject json = null;
    Context context;

    public CasioPrintCashcount() {
        width = casioWidth;
        receiptData = new StringBuilder();
    }

    public void print(String json) {
        context = MainActivity.getInstance();
        initiatePrint(json);
    }

    protected void initiatePrint(final String jsonData) {
        try {
			JsonParser parser = new JsonParser();
			json = parser.parse(jsonData).getAsJsonObject();
            setupPrinter();
            sendReceiptToPrinter(device, makeReport());
        } catch (Exception ex) {
            ErrorReporter.INSTANCE.filelog(ex);
            Toast.makeText(MainActivity.getInstance(), R.string.error_printing_receipt, Toast.LENGTH_LONG).show();
        }
    }

    protected void printDivisionLine() {
        receiptData.append(dashLine(width)); //division line filled with -----
    }

    protected String makeReport() {
		try {
			receiptData = new StringBuilder();
			receiptData.append("\u001B|N");

			chain(context);
			feedLines(1);
			addLine(makeCenterizedLine(context.getString(R.string.cashcount), width));
			addLine(makeLine(context.getString(R.string.date) + ":" + makeSpace(1) + json.get("date").getAsString() + makeSpace(1) + json.get("time").getAsString(), context.getString(R.string.shop) + ":" + makeSpace(1) + AccountManager.INSTANCE.getAccount().getShop().getID(), width, lm, rm));
        	addLine(makeLine(context.getString(R.string.number) + ":" + makeSpace(1) + Long.parseLong(json.get("number").getAsString()), context.getString(R.string.salesperson) + ":" + makeSpace(1) + AccountManager.INSTANCE.getAccount().getUserId(), width, lm, rm));
			feedLines(1);

			addLine(makeLine(context.getString(R.string.startreserve), Decimal.make(json.get("startreserve").getAsDouble()).toString(), width, lm, rm));
			printDivisionLine();

			double totalCash = 0.0;
			double totalTerminal = 0.0;
			double totalManual = 0.0;
			double totalGiftCard = 0.0;
			double totalPayment = 0.0;
			double calcCash = 0;
			double calcTerminal = 0;
			double calcManual = 0;
			double calcGiftCard = 0;
			JsonArray types = json.getAsJsonArray("payments");
			if (types != null && types.size() > 0) {
				for (JsonElement type : types) {
					JsonObject payment = type.getAsJsonObject();
					String paymentType = payment.get("type").getAsString();
					switch (paymentType) {
						case "1":
							totalCash += payment.get("amount").getAsDouble();
							calcCash += payment.get("calc").getAsDouble();
							break;
						case "2":
							totalTerminal += payment.get("amount").getAsDouble();
							calcTerminal += payment.get("calc").getAsDouble();
							break;
						case "3":
							totalManual += payment.get("amount").getAsDouble();
							calcManual += payment.get("calc").getAsDouble();
							break;
						case "5":
							totalGiftCard += payment.get("amount").getAsDouble();
							calcGiftCard += payment.get("calc").getAsDouble();
							break;
					}

					totalPayment += payment.get("amount").getAsDouble();
				}

				if (totalCash != 0) {
					addLine(makeLine(context.getString(R.string.payment_cash), Decimal.make(totalCash).toString(), width, lm, rm));
				}
				if (totalTerminal != 0) {
					addLine(makeLine(context.getString(R.string.payment_card), Decimal.make(totalTerminal).toString(), width, lm, rm));
				}
				if (totalManual != 0) {
					addLine(makeLine("(M)" + context.getString(R.string.payment_card), Decimal.make(totalManual).toString(), width, lm, rm));
				}
				if (totalGiftCard != 0) {
					addLine(makeLine(context.getString(R.string.payment_giftcard), Decimal.make(totalGiftCard).toString(), width, lm, rm));
				}

				printDivisionLine();
				addLine(makeLine(context.getString(R.string.totalpayment), Decimal.make(totalPayment).toString(), width, lm, rm));
				printDivisionLine();
			}

			addLine(makeLine(context.getString(R.string.totalsales), Decimal.make(json.get("sales").getAsDouble()).toString(), width, lm, rm));
			addLine(makeLine(context.getString(R.string.expenditure), Decimal.make(json.get("expenditure").getAsDouble()).toString(), width, lm, rm));
			addLine(makeLine(context.getString(R.string.orderscount), "" + json.get("orderscount").getAsInt(), width, lm, rm));
			printDivisionLine();

			addLine(makeLine(context.getString(R.string.invoicesales), Decimal.make(json.get("invoicesales").getAsDouble()).toString(), width, lm, rm));
			addLine(makeLine(context.getString(R.string.salesinlcinvoice), Decimal.make(json.get("salesinlcinvoice").getAsDouble()).toString(), width, lm, rm));
			printDivisionLine();

			addLine(makeLine(context.getString(R.string.sumdeposit), Decimal.make(json.get("sumdeposit").getAsDouble()).toString(), width, lm, rm));
			addLine(makeLine(context.getString(R.string.reserve), Decimal.make(json.get("reserve").getAsDouble()).toString(), width, lm, rm));
			addLine(makeLine(context.getString(R.string.depositcash), Decimal.make(json.get("depositcash").getAsDouble()).toString(), width, lm, rm));

			if (calcCash != totalCash) {
				addLine(makeLine(context.getString(R.string.diff) + " (" + context.getString(R.string.payment_cash) + ")", Decimal.make(totalCash - calcCash).toString(), width, lm, rm));
			}
			if (calcTerminal != totalTerminal) {
				addLine(makeLine(context.getString(R.string.diff) + " (" + context.getString(R.string.payment_card) + ")", Decimal.make(totalTerminal - calcTerminal).toString(), width, lm, rm));
			}
			if (calcManual != totalManual) {
				addLine(makeLine(context.getString(R.string.diff) + " ((M) " + context.getString(R.string.payment_card) + ")", Decimal.make(totalManual - calcManual).toString(), width, lm, rm));
			}
			if (calcGiftCard != totalGiftCard) {
				addLine(makeLine(context.getString(R.string.diff) + " (" + context.getString(R.string.payment_giftcard) + ")", Decimal.make(totalGiftCard - calcGiftCard).toString(), width, lm, rm));
			}

			feedLines(1);

			JsonArray taxes = json.getAsJsonArray("taxes");
			if (taxes != null && taxes.size() > 0) {
				double totalVat = 0.0;
				for (JsonElement o : taxes) {
					JsonObject vat = o.getAsJsonObject();
					double vatPercent = vat.get("vat").getAsDouble();
					double vatSaleAmount = vat.get("amount").getAsDouble();
					totalVat += (vatSaleAmount - (vatSaleAmount / (vatPercent / 100 + 1)));
				}

				printDivisionLine();
				addLine(makeLine(context.getString(R.string.vat), Decimal.make(totalVat).toString(), width, lm, rm));
				printDivisionLine();

				for (JsonElement o : taxes) {
					JsonObject vat = o.getAsJsonObject();

					double vatPercent = vat.get("vat").getAsDouble();
					double vatSaleAmount = vat.get("amount").getAsDouble();
					double vatAmount = (vatSaleAmount - (vatSaleAmount / (vatPercent / 100 + 1)));

					addLine(makeLine(Decimal.make(vat.get("vat").getAsDouble()).toString() + "%", Decimal.make(vatAmount).toString(), width, lm, rm));
				}
				printDivisionLine();
				feedLines(1);
			}

			if (json.get("issuedprepaid").getAsDouble()  != 0.0) {
				addLine(makeLine(context.getString(R.string.otheractivities), "", width, lm, rm));
				printDivisionLine();
				addLine(makeLine(context.getString(R.string.issuedprepaid), Decimal.make(json.get("issuedprepaid").getAsDouble()).toString(), width, lm, rm));
				feedLines(1);
			}

			String diff = json.get("diff").getAsString();
			if (diff != null && !diff.isEmpty()) {
				feedLines(1);
				ArrayList<String> diffLines = formatProductName(diff, width, 0);
				for (int j = 0; j < diffLines.size(); j++) {
					addLine(makeLine(diffLines.get(j), "", width, lm, rm));
				}
			}

			feedLines(2);
			addLine(makeLine(context.getString(R.string.signature), "", width, lm, rm));
			addLine(makeLine("", rightLineOfCharOfSize('-', width-lm-rm-context.getString(R.string.signature).length()), width, lm, rm));

			feedLines(4);
			cutPaper();
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog("Error in makeReceipt", "", e);
		}

        return receiptData.toString();
    }

	@Override
	public int print(Object o, ReceiptPrintType isCopy) {
		return 0;
	}

	@Override
	protected void handleMainBody(ReceiptPrintType printType) {

	}

	protected void sendReceiptToPrinter(final LinePrinterDeviceBase device, final String receiptData) {
        LinePrinter printer;
        device.setMulticharMode(LinePrinterDeviceBase.PAGEMODE_USER);
        try {
            printer = new LinePrinter();
			printer.open(device);
            printer.printNormal(receiptData);
            printer.close();
        } catch (Exception e) {
            ErrorReporter.INSTANCE.filelog("CasioPrintSupply", "sendReceiptToPrinter", e);
        }
	}
}
