package no.susoft.mobile.pos.hardware.printer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.data.OrderLine;
import no.susoft.mobile.pos.data.Product;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.activity.util.AppConfig;

public class IPPrintSupply extends IPPrint {

    protected Order order;
    protected ArrayList<Product> products = null;
    protected Date from = null;
    protected Date to = null;

    public IPPrintSupply(ArrayList<Product> products, Date from, Date to) {
        this.order = order;
        this.products = products;
        this.from = from;
        this.to = to;
        this.context = MainActivity.getInstance();
		width = wideWidth;
		String name = AppConfig.getState().getPrinterName();
		String ipAddress = AppConfig.getState().getPrinterIp();
		setupPrinter(name, ipAddress);
		openPrinter();
		PreparePrint(products, from, to);
		sendData();
    }

    public IPPrintSupply() {
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
