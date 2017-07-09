package no.susoft.mobile.pos.server;

import java.util.ArrayList;

import android.os.AsyncTask;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.entity.mime.content.StringBody;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.data.OrderPaymentResponse;
import no.susoft.mobile.pos.data.PeripheralProvider;
import no.susoft.mobile.pos.data.ReceiptPrintType;
import no.susoft.mobile.pos.hardware.ConnectionManager;
import no.susoft.mobile.pos.hardware.combi.mPOPPrintOrdering;
import no.susoft.mobile.pos.hardware.printer.*;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.network.Protocol;
import no.susoft.mobile.pos.network.Protocol.Message;
import no.susoft.mobile.pos.network.Protocol.Parameters;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.activity.util.AppConfig;

public class ParkOrdersAsync extends AsyncTask<ArrayList<Order>, Void, Message> {

	boolean printReceipt = false;
	private ArrayList<Order> orders;
	
    @Override
    protected Message doInBackground(ArrayList<Order>... o) {

        Message m = Message.ERROR_UNEXPECTED;
        try {
			orders = o[0];
            Request request = Server.INSTANCE.getEncryptedPreparedRequest();
            request.appendState(Protocol.State.AUTHORIZED);
            request.appendOperation(Protocol.OperationCode.REQUEST_ORDER_UPLOAD);
            request.appendParameter(Protocol.Parameters.TOKEN, AccountManager.INSTANCE.getAccount().getToken());
            request.appendParameter(Protocol.Parameters.TYPE, Parameters.TERM.ordinal());
            request.appendParameter(Protocol.Parameters.SHOP, AccountManager.INSTANCE.getAccount().getShop().getID());

			ArrayList<Order> ordersToSent = new ArrayList<>();
			ArrayList<Order> ordersList = o[0];
			for (Order order : ordersList) {
				if (order.hasLines()) {
					ordersToSent.add(order);
				}
			}

            final String json = JSONFactory.INSTANCE.getFactory().toJson(ordersToSent);
            final StringBody file = new StringBody(json, ContentType.APPLICATION_JSON);
            final MultipartEntityBuilder entity = MultipartEntityBuilder.create();
            entity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            entity.addPart("file", file);

            m = Server.INSTANCE.doPost(Message.class, request, entity, JSONFactory.INSTANCE.getFactory());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return m;
    }

    @Override
    protected void onPostExecute(Message result) {
        super.onPostExecute(result);

		//region Print Receipt
		if (result == Message.OK && printReceipt) {
			for (final Order order : orders) {
				int printerProvider = AppConfig.getState().getPrinterProviderOrdinal();
				if (printerProvider == PeripheralProvider.CASIO.ordinal()) {
					// region CASIO
					if (CasioPrint.hasPrinterConnected()) {
						ConnectionManager.getInstance().execute(new Runnable() {
							@Override
							public void run() {
								int result = 0;
								int counter = 0;
								do {
									CasioPrintOrdering po = new CasioPrintOrdering(order);
									result = po.print(order, ReceiptPrintType.ORIGINAL);
									counter++;
								} while (result != 0 && counter < 20);
								if (counter == 20 && result != 0) {
									MainActivity.getInstance().showPrinterNotConnectedToast();
								}
							}
						});
						ConnectionManager.getInstance().execute(new Runnable() {
							@Override
							public void run() {
								int result = 0;
								int counter = 0;
								do {
									CasioPrintOrdering po = new CasioPrintOrdering(order);
									result = po.print(order, ReceiptPrintType.ORIGINAL);
									counter++;
								} while (result != 0 && counter < 20);
								if (counter == 20 && result != 0) {
									MainActivity.getInstance().showPrinterNotConnectedToast();
								}
							}
						});
					}
					// endregion
				} else if (printerProvider == PeripheralProvider.BIXOLON.ordinal()) {
					// region BIXOLON

					if (AppConfig.getState().getPrinterIp().isEmpty()) {
						ConnectionManager.getInstance().execute(new Runnable() {
							@Override
							public void run() {
								OrderPaymentResponse r = new OrderPaymentResponse();
								r.setOrder(order);
								BluetoothPrintOrdering bp = new BluetoothPrintOrdering(r, false);
								bp.print(bp.makeReceipt(order));
							}
						});
						ConnectionManager.getInstance().execute(new Runnable() {
							@Override
							public void run() {
								OrderPaymentResponse r = new OrderPaymentResponse();
								r.setOrder(order);
								BluetoothPrintOrdering bp = new BluetoothPrintOrdering(r, false);
								bp.print(bp.makeReceipt(order));
							}
						});
					} else {
						ConnectionManager.getInstance().execute(new BixolonPrinterJob() {
							@Override
							public int print() {
								OrderPaymentResponse r = new OrderPaymentResponse();
								r.setOrder(order);
								IPPrintOrderingWide po = new IPPrintOrderingWide(r, false);
								return po.printIP(order, AppConfig.getState().getPrinterName(), AppConfig.getState().getPrinterIp());

							}
						});
						ConnectionManager.getInstance().execute(new BixolonPrinterJob() {
							@Override
							public int print() {
								OrderPaymentResponse r = new OrderPaymentResponse();
								r.setOrder(order);
								IPPrintOrderingWide po = new IPPrintOrderingWide(r, false);
								return po.printIP(order, AppConfig.getState().getPrinterName(), AppConfig.getState().getPrinterIp());

							}
						});
					}
					// endregion
				} else if (printerProvider == PeripheralProvider.STAR.ordinal()) {
					// region STAR
					ConnectionManager.getInstance().execute(new Runnable() {
						@Override
						public void run() {
							new mPOPPrintOrdering(order);
						}
					});
					ConnectionManager.getInstance().execute(new Runnable() {
						@Override
						public void run() {
							new mPOPPrintOrdering(order);
						}
					});
					// endregion
				}
			}
		}
		// endregion
		
		MainActivity.getInstance().getServerCallMethods().sendOrdersToServerPostExecute(result, Parameters.TERM);
    }
	
	public void setPrintReceipt(boolean printReceipt) {
		this.printReceipt = printReceipt;
	}
}