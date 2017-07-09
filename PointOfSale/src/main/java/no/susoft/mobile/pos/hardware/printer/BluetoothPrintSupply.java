package no.susoft.mobile.pos.hardware.printer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.data.OrderLine;
import no.susoft.mobile.pos.data.Product;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class BluetoothPrintSupply extends BluetoothPrintOrderWide {

    protected Order order;
    protected ArrayList<Product> products = null;
    protected Date from = null;
    protected Date to = null;

    public BluetoothPrintSupply(ArrayList<Product> products, Date from, Date to) {
        this.products = products;
        this.from = from;
        this.to = to;
        this.context = MainActivity.getInstance();
        width = wideWidth;
		returnsPrinted = true;
        output = new ArrayList<>();
        print(PreparePrint(products, from, to));
    }

    public BluetoothPrintSupply() {
    }

    protected ArrayList<Object> PreparePrint(ArrayList<Product> products, Date from, Date to) {
		output = new ArrayList<>();
		addLine(makeCenterizedLineLargeFont(context.getString(R.string.supply_report), width));
		printNormalText();
		printEmptyLine();
		addLine(makeCenterizedLine(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(from) + " - " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(to), width));
		printRightLineOfLength('-', width);

		String categoryId = null;
		for (Product product : products) {
			if (categoryId == null || !product.getCategoryId().equals(categoryId)) {
				categoryId = product.getCategoryId();
				addLine(makeLine(product.getCategoryName(), "", width, lm, rm));
			}

			addLine(makeLine(product.getName(), product.getStockQty().toString(), width, lm, rm));
		}
		return output;
    }

	@Override
	protected void doExtraAfterSendData() {
		byte[] cmd = new byte[]{0x1b, 0x64, 0xC};
		try {
			mmOutputStream.write(" ".getBytes(Charset.forName("UTF-8")));
			mmOutputStream.write(cmd);
			mmOutputStream.write(new byte[]{0x1b, 0x69});
			mmOutputStream.flush();
			closeBT();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void handleHeaderPrint() {

	}

	@Override
	protected void handleExtraBottomPrint() {

	}

	@Override
	protected void printExtraTitle() {

	}

	protected void printExtraFooter() {

	}

	@Override
	protected void handleOrderLinesPrint() {

	}

	@Override
	protected void handlePaymentPrint() {

	}

	@Override
	protected void handleChangePrint() {

	}

	@Override
	protected void handleVatPrint(List<OrderLine> list) {

	}
}
