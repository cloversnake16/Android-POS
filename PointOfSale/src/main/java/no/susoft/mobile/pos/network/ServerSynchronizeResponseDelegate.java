package no.susoft.mobile.pos.network;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import no.susoft.mobile.pos.SusoftPOSApplication;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.*;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.activity.ProtocolMismatchActivity;
import no.susoft.mobile.pos.ui.activity.util.AccountBar;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.client.ResponseHandler;

/**
 * This nested class handles HTTP responses and subsequent JSON serialization.
 */
public final class ServerSynchronizeResponseDelegate implements ResponseHandler<SynchronizeTransport> {
	
	/**
	 *
	 */
	public ServerSynchronizeResponseDelegate() {
		super();
	}
	
	/**
	 * (non-Javadoc)
	 * @see org.apache.http.client.ResponseHandler#handleResponse(org.apache.http.HttpResponse)
	 */
	@Override
	public SynchronizeTransport handleResponse(HttpResponse response) {
		// Drop to the status code returned by the server.
		switch (response.getStatusLine().getStatusCode()) {
			// 200 OK (HTTP/1.0 - RFC 1945)
			case HttpStatus.SC_OK: {
				SynchronizeTransport transport = null;
				try {
					HttpEntity entity = response.getEntity();
					// Empty response?
					ArrayList<Product> products = new ArrayList<>();
					if (entity != null) {
						transport = new SynchronizeTransport();
						Gson gson = JSONFactory.INSTANCE.getFactory();
						InputStreamReader jsonReader = new InputStreamReader(entity.getContent());
						JsonReader reader = new JsonReader(jsonReader);
						
						try {
							reader.beginObject();
							while (reader.hasNext()) {
								String name = reader.nextName();
								if (name.equals("products")) {
									reader.beginArray();
									while (reader.hasNext()) {
										Product p = gson.fromJson(reader, Product.class);
										products.add(p);
										if (products.size() == 5000) {
											storeProducts(products);
											products = new ArrayList<>();
										}
									}
									transport.setProducts(products);
									reader.endArray();
								} else if (name.equals("bundles")) {
									reader.beginArray();
									while (reader.hasNext()) {
										ProductBundle p = gson.fromJson(reader, ProductBundle.class);
										transport.getBundles().add(p);
									}
									reader.endArray();
								} else if (name.equals("accounts")) {
									reader.beginArray();
									while (reader.hasNext()) {
										Account p = gson.fromJson(reader, Account.class);
										transport.getAccounts().add(p);
									}
									reader.endArray();
								} else if (name.equals("customers")) {
									reader.beginArray();
									while (reader.hasNext()) {
										Customer p = gson.fromJson(reader, Customer.class);
										transport.getCustomers().add(p);
									}
									reader.endArray();
								} else if (name.equals("qlmGrids")) {
									reader.beginArray();
									while (reader.hasNext()) {
										QuickLaunchMenuGrid p = gson.fromJson(reader, QuickLaunchMenuGrid.class);
										transport.getQlmGrids().add(p);
									}
									reader.endArray();
								} else if (name.equals("qlmCells")) {
									reader.beginArray();
									while (reader.hasNext()) {
										QuickLaunchMenuCell p = gson.fromJson(reader, QuickLaunchMenuCell.class);
										transport.getQlmCells().add(p);
									}
									reader.endArray();
								} else if (name.equals("areas")) {
									reader.beginArray();
									while (reader.hasNext()) {
										Area p = gson.fromJson(reader, Area.class);
										transport.getAreas().add(p);
									}
									reader.endArray();
								}
								
							}
							reader.endObject();
							reader.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						entity.consumeContent();
						return transport;
					}
					break;
				} catch (NullPointerException e) {
					// The response was empty?
					// TODO Error Reporting
					e.printStackTrace();
					return null;
				} catch (JsonIOException e) {
					// The response was unable to be read.
					// TODO Error Reporting
					e.printStackTrace();
					return null;
				} catch (JsonSyntaxException e) {
					// The response is an invalid JSON document.
					e.printStackTrace();
					return null;
				} catch (UnsupportedEncodingException e) {
					// The response encoding was unexpected.
					// TODO Error Reporting
					e.printStackTrace();
					return null;
				} catch (IllegalStateException e) {
					// The response is not repeatable and the stream has already been previously obtained.
					// TODO Error Reporting
					e.printStackTrace();
					return null;
				} catch (IOException e) {
					// TODO Error Reporting
					e.printStackTrace();
					return null;
				}
			}
			case HttpStatus.SC_NO_CONTENT: {
				return null;
			}
			case HttpStatus.SC_INTERNAL_SERVER_ERROR: {
				return null;
			}
			case HttpStatus.SC_UNAUTHORIZED: {
				ErrorReporter.INSTANCE.filelog("ServerSynchronizeResponseDelegate caught HttpStatus.SC_UNAUTHORIZED..");
				AccountManager.INSTANCE.reauthenticateAccounts();
//				// Invalidate the active account.
//				AccountManager.INSTANCE.doActiveAccountInvalidation();
//				MainActivity.getInstance().runOnUiThread(new Runnable() {
//					@Override
//					public void run() {
//						AccountBar.getInstance().showAccountLoginDialog();
//					}
//				});
				return null;
			}
			case Protocol.PROTOCOL_MISMATCH: {
				SusoftPOSApplication.startActivity(ProtocolMismatchActivity.class);
				// Nothing.
				return null;
			}
			default: {
				MainActivity.getInstance().networkUnavailable();
				return null;
			}
		}
		return null;
	}
	
	private void storeProducts(ArrayList<Product> products) {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT OR REPLACE ");
		sql.append("INTO ");
		sql.append("	PRODUCT ");
		sql.append("( ");
		sql.append("	ID, ");
		sql.append("	NAME, ");
		sql.append("	BARCODE, ");
		sql.append("	DESCRIPTION, ");
		sql.append("	TYPE, ");
		sql.append("	COST, ");
		sql.append("	PRICE, ");
		sql.append("	STOCK, ");
		sql.append("	ABCCODE, ");
		sql.append("	VAT, ");
		sql.append("	USE_ALTERNATIVE, ");
		sql.append("	ALTERNATIVE_PRICE, ");
		sql.append("	ALTERNATIVE_VAT, ");
		sql.append("	MISC ) ");
		sql.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?); ");
		
		SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getWritableDatabase();
		SQLiteStatement statement = db.compileStatement(sql.toString());
		db.beginTransaction();
		
		try {
			for (Product product : products) {
				statement.clearBindings();
				statement.bindString(1, product.getId());
				statement.bindString(2, product.getName());
				statement.bindString(3, product.getBarcode());
				statement.bindString(4, product.getDescription());
				statement.bindString(5, product.getType());
				statement.bindDouble(6, product.getCost().toDouble());
				statement.bindDouble(7, product.getPrice().toDouble());
				statement.bindDouble(8, product.getStockQty().toDouble());
				statement.bindString(9, product.getAbcCode());
				statement.bindDouble(10, product.getVat());
				statement.bindLong(11, product.isUseAlternative() ? 1 : 0);
				statement.bindDouble(12, product.getAlternativePrice().toDouble());
				statement.bindDouble(13, product.getAlternativeVat());
				statement.bindLong(14, product.isMiscellaneous() ? 1 : 0);
				statement.execute();
			}
			
			db.setTransactionSuccessful();
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog("SYNCHRONIZE", "Error on accessing DB", e);
		}
		
		db.endTransaction();
		statement.close();
	}
}