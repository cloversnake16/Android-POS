package no.susoft.mobile.pos.ui.fragment.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.*;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;
import com.bxl.BXLConst;
import com.bxl.config.editor.BXLConfigLoader;
import jp.co.casio.vx.framework.device.Drawer;
import jp.co.casio.vx.framework.device.LinePrinter;
import jp.co.casio.vx.framework.device.Up400DrawerPort;
import jp.co.casio.vx.framework.device.lineprintertools.LinePrinterConvertBase.BarCodeAlignment;
import jp.co.casio.vx.framework.device.lineprintertools.LinePrinterConvertBase.BarCodeSymbology;
import jp.co.casio.vx.framework.device.lineprintertools.LinePrinterConvertBase.BarCodeTextPosition;
import jp.co.casio.vx.framework.device.lineprintertools.LinePrinterDeviceBase;
import jp.co.casio.vx.framework.device.lineprintertools.SerialUp400;
import jpos.JposConst;
import jpos.POSPrinter;
import jpos.POSPrinterConst;
import jpos.config.JposEntry;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.SusoftPOSApplication;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.*;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.hardware.ConnectionManager;
import no.susoft.mobile.pos.hardware.combi.Star_mPOP;
import no.susoft.mobile.pos.hardware.combi.mPOPPrintOrderSlim;
import no.susoft.mobile.pos.hardware.printer.*;
import no.susoft.mobile.pos.hardware.terminal.CardTerminal;
import no.susoft.mobile.pos.hardware.terminal.CardTerminalFactory;
import no.susoft.mobile.pos.network.Server;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.activity.WebViewActivity;
import no.susoft.mobile.pos.ui.activity.util.AppConfig;
import no.susoft.mobile.pos.ui.dialog.GiftcardLookupDialog;
import no.susoft.mobile.pos.ui.dialog.NewGiftCardDialog;
import no.susoft.mobile.pos.ui.dialog.OrderSearchDialog;
import no.susoft.mobile.pos.ui.dialog.SupplyReportDialog;

import static android.content.Context.MODE_PRIVATE;
import static no.susoft.mobile.pos.hardware.printer.IPPrint.CONFIG_FILE_NAME;

public class NavigationDrawerOptions {
	
	public void doActionForItemInPosition(int id) {
		CardTerminal cardTerminal = null;
		try {
			cardTerminal = CardTerminalFactory.getInstance().getCardTerminal();
		} catch (Exception e) {
			e.printStackTrace();
			ErrorReporter.INSTANCE.filelog("NavigationDrawerOptions", "doActionForItemInPosition error ", e);
		}
		
		switch (id) {
			case R.id.lm_key_perfomance:
				kprIntent();
				break;
			
			case R.id.lm_giftcards:
				showAllGiftCardsDialog();
				break;
			
			case R.id.lm_reprint_receipt:
				if (Cart.persistingOrders.getPreviousOrder() != null) {
					int printerProvider = AppConfig.getState().getPrinterProviderOrdinal();
					if (printerProvider == PeripheralProvider.CASIO.ordinal()) {
						if (CasioPrintOrder.hasPrinterConnected()) {
							new CasioPrintOrder().print(Cart.persistingOrders.getPreviousOrder(), ReceiptPrintType.COPY);
						}
					} else if (printerProvider == PeripheralProvider.BIXOLON.ordinal()) {
						if (AppConfig.getState().getPrinterIp().isEmpty()) {
							BluetoothPrintOrderWide bp = new BluetoothPrintOrderWide(null, false);
							bp.print(bp.makeReceipt(Cart.persistingOrders.getPreviousOrder()));
						} else {
							OrderPaymentResponse response = new OrderPaymentResponse();
							response.setOrder(Cart.persistingOrders.getPreviousOrder());
							IPPrintOrderWide po = new IPPrintOrderWide(response, false);
							po.printIP(Cart.persistingOrders.getPreviousOrder(), AppConfig.getState().getPrinterName(), AppConfig.getState().getPrinterIp());
						}
					} else if (printerProvider == PeripheralProvider.STAR.ordinal()) {
						new mPOPPrintOrderSlim(Cart.persistingOrders.getPreviousOrder());
					} else if (printerProvider == PeripheralProvider.VERIFONE.ordinal()) {
						Printer printer = PrinterFactory.getInstance().getPrinter();
						if (printer != null) {
							printer.printOrder(Cart.persistingOrders.getPreviousOrder(), ReceiptPrintType.ORIGINAL);
						}
					}
				} else {
					Toast.makeText(MainActivity.getInstance(), MainActivity.getInstance().getString(R.string.no_order_in_memory), Toast.LENGTH_SHORT).show();
				}
				break;
			
			case R.id.lm_x_report:
				if (cardTerminal != null) {
					try {
						switch (cardTerminal.open()) {
							case CardTerminal.SUCCESS:
								try {
									cardTerminal.xreport("0000");
								} catch (no.susoft.mobile.pos.hardware.terminal.exception.CardTerminalException e) {
									e.printStackTrace();
								}
								break;
							case CardTerminal.FAILURE:
								Toast.makeText(MainActivity.getInstance(), "No connection to card terminal. Can't process xreport call." + cardTerminal.decode(cardTerminal.getMethodRejectCode()), Toast.LENGTH_LONG).show();
								break;
						}
					} catch (no.susoft.mobile.pos.hardware.terminal.exception.CardTerminalException e) {
						e.printStackTrace();
					}
				} else {
					Toast.makeText(MainActivity.getInstance(), "Card terminal not connected.", Toast.LENGTH_SHORT).show();
				}
				break;
			
			case R.id.lm_z_report:
				if (cardTerminal != null) {
					try {
						switch (cardTerminal.open()) {
							case CardTerminal.SUCCESS:
								try {
									cardTerminal.zreport("0000");
								} catch (no.susoft.mobile.pos.hardware.terminal.exception.CardTerminalException e) {
									e.printStackTrace();
								}
								break;
							case CardTerminal.FAILURE:
								Toast.makeText(MainActivity.getInstance(), "No connection to card terminal. Can't process zreport call." + cardTerminal.decode(cardTerminal.getMethodRejectCode()), Toast.LENGTH_LONG).show();
								break;
						}
					} catch (no.susoft.mobile.pos.hardware.terminal.exception.CardTerminalException e) {
						e.printStackTrace();
					}
				} else {
					Toast.makeText(MainActivity.getInstance(), "Card terminal not connected.", Toast.LENGTH_SHORT).show();
				}
				break;
			
			case R.id.lm_reconciliation:
				if (cardTerminal != null) {
					try {
						switch (cardTerminal.open()) {
							case CardTerminal.SUCCESS:
								try {
									cardTerminal.reconciliation("0000");
								} catch (no.susoft.mobile.pos.hardware.terminal.exception.CardTerminalException e) {
									e.printStackTrace();
								}
								break;
							case CardTerminal.FAILURE:
								Toast.makeText(MainActivity.getInstance(), "No connection to card terminal. Can't process reconciliation call." + cardTerminal.decode(cardTerminal.getMethodRejectCode()), Toast.LENGTH_LONG).show();
								break;
						}
					} catch (no.susoft.mobile.pos.hardware.terminal.exception.CardTerminalException e) {
						e.printStackTrace();
					}
				} else {
					Toast.makeText(MainActivity.getInstance(), "Card terminal not connected.", Toast.LENGTH_SHORT).show();
				}
				
				break;
			
			case R.id.lm_print_last_trans_result:
				if (cardTerminal != null) {
					try {
						switch (cardTerminal.open()) {
							case CardTerminal.SUCCESS:
								
								try {
									cardTerminal.printLastCardPaymentTransaction("0000");
								} catch (no.susoft.mobile.pos.hardware.terminal.exception.CardTerminalException e) {
									e.printStackTrace();
								}
								break;
							case CardTerminal.FAILURE:
								Toast.makeText(MainActivity.getInstance(), "No connection to card terminal. Can't print last result." + cardTerminal.decode(cardTerminal.getMethodRejectCode()), Toast.LENGTH_LONG).show();
								break;
						}
					} catch (no.susoft.mobile.pos.hardware.terminal.exception.CardTerminalException e) {
						e.printStackTrace();
					}
				} else {
					Toast.makeText(MainActivity.getInstance(), "Card terminal not connected.", Toast.LENGTH_SHORT).show();
				}
				break;
			
			case R.id.lm_cashcount:
				cashCountIntent();
				break;
			
			case R.id.lm_voucher:
				voucherIntent();
				break;
			
			case R.id.lm_open_cashdraw:
				try {
					doOpenDrawer();
				} catch (Exception ex) {
					ex.printStackTrace();
					ErrorReporter.INSTANCE.filelog("NavigationDrawerOptions", "OPEN_CASHDRAW error", ex);
				}
				break;
			
			case R.id.lm_order_search:
				OrderSearchDialog orderSearchDialog = new OrderSearchDialog();
				orderSearchDialog.show(MainActivity.getInstance().getSupportFragmentManager(), "ordersearch");
				break;
			
			case R.id.lm_supply_report:
				SupplyReportDialog supplyReportDialog = new SupplyReportDialog();
				supplyReportDialog.show(MainActivity.getInstance().getSupportFragmentManager(), "supplyreport");
				break;
			
			case R.id.lm_stock_order:
				Intent i = new Intent(MainActivity.getInstance().getApplicationContext(), WebViewActivity.class);
				Account account = AccountManager.INSTANCE.getAccount();
				i.putExtra("url", "http://" + Server.authority + "/" + Server.path1 + "/no.susoft.sucom.servlets.AndroidAccessServlet?chain=" + account.getChain().getID() + "&user=" + account.getLogin() + "&pwd=" + account.getPassword() + "&exec=230&android=1&shop_id=" + account.getShop().getID() + "&sktab=2");
				MainActivity.getInstance().startActivity(i);
				break;
			
			case R.id.lm_stock_movement:
				i = new Intent(MainActivity.getInstance().getApplicationContext(), WebViewActivity.class);
				account = AccountManager.INSTANCE.getAccount();
				i.putExtra("url", "http://" + Server.authority + "/" + Server.path1 + "/no.susoft.sucom.servlets.AndroidAccessServlet?chain=" + account.getChain().getID() + "&user=" + account.getLogin() + "&pwd=" + account.getPassword() + "&exec=230&android=1&shop_id=" + account.getShop().getID() + "&sktab=1&skfilter=2");
				MainActivity.getInstance().startActivity(i);
				break;
			
			case R.id.lm_stock_correction:
				i = new Intent(MainActivity.getInstance().getApplicationContext(), WebViewActivity.class);
				account = AccountManager.INSTANCE.getAccount();
				i.putExtra("url", "http://" + Server.authority + "/" + Server.path1 + "/no.susoft.sucom.servlets.AndroidAccessServlet?chain=" + account.getChain().getID() + "&user=" + account.getLogin() + "&pwd=" + account.getPassword() + "&exec=230&android=1&shop_id=" + account.getShop().getID() + "&sktab=1&skfilter=1");
				MainActivity.getInstance().startActivity(i);
				break;
			
			case R.id.lm_stock_return:
				i = new Intent(MainActivity.getInstance().getApplicationContext(), WebViewActivity.class);
				account = AccountManager.INSTANCE.getAccount();
				i.putExtra("url", "http://" + Server.authority + "/" + Server.path1 + "/no.susoft.sucom.servlets.AndroidAccessServlet?chain=" + account.getChain().getID() + "&user=" + account.getLogin() + "&pwd=" + account.getPassword() + "&exec=230&android=1&shop_id=" + account.getShop().getID() + "&sktab=1&skfilter=3");
				MainActivity.getInstance().startActivity(i);
				break;
			
			case R.id.lm_key_shopindicators:
				i = new Intent(MainActivity.getInstance().getApplicationContext(), WebViewActivity.class);
				account = AccountManager.INSTANCE.getAccount();
				i.putExtra("url", "http://" + Server.authority + "/" + Server.path1 + "/no.susoft.sucom.servlets.AndroidAccessServlet?chain=" + account.getChain().getID() + "&user=" + account.getLogin() + "&pwd=" + account.getPassword() + "&exec=281&android=1&shop_id=" + account.getShop().getID());
				MainActivity.getInstance().startActivity(i);
				break;
			
			case R.id.lm_chat:
				PackageManager pm = SusoftPOSApplication.getContext().getPackageManager();
				try {
					pm.getPackageInfo("com.example.sami_leino.susoft_chat", PackageManager.GET_ACTIVITIES);
					Intent intent = new Intent();
					intent.setComponent(new ComponentName("com.example.sami_leino.susoft_chat", "com.example.sami_leino.susoft_chat.ui.activity.main.SplashActivity"));
					intent.setAction(Intent.ACTION_MAIN);
					intent.addCategory(Intent.CATEGORY_LAUNCHER);
					intent.putExtra("AUTO_LOGIN", true);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					MainActivity.getInstance().startActivity(intent);
				} catch (PackageManager.NameNotFoundException e) {
					Toast.makeText(MainActivity.getInstance(), "Chat app is not installed", Toast.LENGTH_LONG).show();
				}
				break;
			
			case R.id.lm_issent_fix:
				SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getWritableDatabase();
				try {
					ContentValues cv = new ContentValues();
					cv.put("IS_SENT", 0);
					db.update("ORDERHEADER", cv, null, null);
					
					MainActivity.getInstance().networkAvailable();
				} catch (Exception e) {
					ErrorReporter.INSTANCE.filelog(e);
					throw e;
				}

				break;
			
//			case R.id.lm_test:
//				doTest();
//				break;
//			case R.id.lm_test2:
//				doTest2();
//				break;
//			case R.id.lm_test3:
//				doTest3();
//				break;
			
		}
	}
	
	private void kprIntent() {
		Intent i = new Intent(MainActivity.getInstance().getApplicationContext(), WebViewActivity.class);
		Account account = AccountManager.INSTANCE.getAccount();
		i.putExtra("url", "http://" + Server.authority + "/" + Server.path1 + "/no.susoft.sucom.servlets.AndroidAccessServlet?chain=" + account.getChain().getID() + "&user=" + account.getLogin() + "&pwd=" + account.getPassword() + "&exec=85&android=1&shop_id=" + account.getShop().getID());
		MainActivity.getInstance().startActivity(i);
	}
	
	private void showCreateGiftCardDialog() {
		NewGiftCardDialog gcDialog = new NewGiftCardDialog();
		gcDialog.show(MainActivity.getInstance().getSupportFragmentManager(), "newgiftcard");
	}
	
	private void showAllGiftCardsDialog() {
		GiftcardLookupDialog gcDialog = new GiftcardLookupDialog();
		gcDialog.show(MainActivity.getInstance().getSupportFragmentManager(), "giftcardlookup");
	}
	
	private void cashCountIntent() {
		
		Cursor rs = null;
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT ");
			sql.append("    ID, ");
			sql.append("    SHOP, ");
			sql.append("    TYPE, ");
			sql.append("    POS, ");
			sql.append("    DATE, ");
			sql.append("    SALESPERSON, ");
			sql.append("    CUSTOMERID, ");
			sql.append("    AREAID, ");
			sql.append("    TABLEID, ");
			sql.append("    NOTE, ");
			sql.append("    REMOTEID, ");
			sql.append("    ALTERNATIVEID ");
			sql.append("FROM ");
			sql.append("	ORDERHEADER ");
			sql.append("WHERE ");
			sql.append("	TYPE = 'O' ");
			sql.append("AND IS_SENT = 0;");
			
			SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getWritableDatabase();
			rs = db.rawQuery(sql.toString(), null);
			if (rs != null && rs.getCount() > 0) {
				if (Server.INSTANCE.isNetworkActive()) {
					Builder builder = new AlertDialog.Builder(MainActivity.getInstance());
					builder.setTitle("");
					builder.setMessage("There are orders which was not sent to the server.");
					builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
					builder.setPositiveButton(R.string.send, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							MainActivity.getInstance().networkAvailable();
						}
					});
					AlertDialog dialog = builder.create();
					dialog.show();
				}
			} else {
				Intent i = new Intent(MainActivity.getInstance().getApplicationContext(), WebViewActivity.class);
				Account account = AccountManager.INSTANCE.getAccount();
				i.putExtra("url", "http://" + Server.authority + "/" + Server.path1 + "/no.susoft.sucom.servlets.AndroidAccessServlet?chain=" + account.getChain().getID() + "&user=" + account.getLogin() + "&pwd=" + account.getPassword() + "&exec=254&android=1&shop_id=" + account.getShop().getID());
				i.putExtra("jsInterface", new CashcountJSInterface());
				MainActivity.getInstance().startActivity(i);
			}
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
	}
	
	private void voucherIntent() {
		Intent i = new Intent(MainActivity.getInstance().getApplicationContext(), WebViewActivity.class);
		Account account = AccountManager.INSTANCE.getAccount();
		i.putExtra("url", "http://" + Server.authority + "/" + Server.path1 + "/no.susoft.sucom.servlets.AndroidAccessServlet?chain=" + account.getChain().getID() + "&user=" + account.getLogin() + "&pwd=" + account.getPassword() + "&exec=269&android=1&shop_id=" + account.getShop().getID());
		MainActivity.getInstance().startActivity(i);
	}
	
	private void doOpenDrawer() {
		try {
			Thread drawerThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						SharedPreferences preferences = SusoftPOSApplication.getContext().getSharedPreferences(PeripheralDevice.class.toString(), Context.MODE_PRIVATE);
						int cashdrawerProvider = preferences.getInt("CASHDRAWER_PROVIDER", PeripheralProvider.CASIO.ordinal());
						if (cashdrawerProvider == PeripheralProvider.STAR.ordinal()) {
							Star_mPOP.openDrawer(MainActivity.getInstance());
						} else if (cashdrawerProvider == PeripheralProvider.CASIO.ordinal()) {
							if (CasioPrint.hasPrinterConnected()) {
								doCasioOpenDrawer();
							} else {
								doDirectOpenDrawer();
							}
						} else if (cashdrawerProvider == PeripheralProvider.BIXOLON.ordinal() && AppConfig.getState().getPrinterIp() != null && AppConfig.getState().getPrinterIp().length() > 0) {
							ConnectionManager.getInstance().execute(new BixolonPrinterJob() {
								@Override
								public int print() {
									IPPrintOrderWide ip = new IPPrintOrderWide();
									return ip.openCashDrawer();
								}
							});
						} else if (cashdrawerProvider == PeripheralProvider.BIXOLON.ordinal()) {
							ConnectionManager.getInstance().execute(new Runnable() {
								@Override
								public void run() {
									BluetoothPrintOrderWide bp = new BluetoothPrintOrderWide();
									bp.openCashDrawer();
								}
							});
						}
					} catch (Exception ex) {
						ErrorReporter.INSTANCE.filelog("openCashDrawer in thread", "ERROR Open Drawer ", ex);
					}
				}
			});
			drawerThread.start();
			
		} catch (Exception ex) {
			ErrorReporter.INSTANCE.filelog("openCashDrawer", "ERROR Open Drawer ", ex);
			ex.printStackTrace();
		}
	}
	
	private void doDirectOpenDrawer() {
		try {
			Drawer drawer = new Drawer();
			int ret = drawer.open(Drawer.OPENMODE_COMMON, Drawer.DEVICE_HOST_LOCALHOST);
			ErrorReporter.INSTANCE.filelog("Direct drawer open status -> ", "" + ret);
		} catch (Exception ex) {
			ErrorReporter.INSTANCE.filelog("Direct drawer exception -> ", "" + ex.getMessage());
			ex.printStackTrace();
		}
	}
	
	private void doCasioOpenDrawer() {
		SerialUp400 printerDevice;
		try {
			
			printerDevice = new SerialUp400(SerialUp400.Port.PORT_COM1, SerialUp400.BaudRate.BAUDRATE_9600, SerialUp400.BitLen.BITLEN_8, SerialUp400.ParityBit.PARITYBIT_NON, SerialUp400.StopBit.STOPBIT_1, SerialUp400.FlowCntl.FLOWCNTL_NON);
			
			printerDevice.setMulticharMode(LinePrinterDeviceBase.MULTICHARMODE_JIS);
			
			Up400DrawerPort drawerPort = new Up400DrawerPort(printerDevice);
			Drawer drawer = new Drawer();
			int ret = drawer.open(drawerPort);
			
			ErrorReporter.INSTANCE.filelog("NavigationDrawerOptions", "Printer drawer open status 1 -> " + ret);
			
			ret = drawer.open(Drawer.OPENMODE_COMMON, Drawer.DEVICE_HOST_LOCALHOST);
			ErrorReporter.INSTANCE.filelog("NavigationDrawerOptions", "Printer drawer open status 2 -> " + ret);
			
			ret = drawer.setOpen(Drawer.DEVICE_DRAWER_NO1);
			ErrorReporter.INSTANCE.filelog("NavigationDrawerOptions", "Printer drawer open status 3 -> " + ret);
			
			drawer.close();
			ErrorReporter.INSTANCE.filelog("NavigationDrawerOptions", "Printer drawer close status -> " + ret);
			
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog("NavigationDrawerOptions", "Printer drawer error ", e);
		}
		
	}
	
	private void doTest() {
		try {
            BXLConfigLoader config = new BXLConfigLoader(MainActivity.getInstance());
            try {
                try {
                    config.openFile();
	
					System.out.println("config.getEntries() = " + config.getEntries());
					
					if (config.getEntries() == null || config.getEntries().size() == 0) {
						createNewConfigFile();
						config.openFile();
					}
                } catch (Exception e1) {
					e1.printStackTrace();
                    createNewConfigFile();
                    config.openFile();
                }
            } catch (Exception e) {
				e.printStackTrace();
                ErrorReporter.INSTANCE.filelog(e);
            }
            
            String productName = BXLConst.SRP_275III;
            String ipAddress = "192.168.1.171";
			
            String logicalName = productName + "_" + ipAddress;
            try {
                if (config.getEntries() != null) {
                    boolean contains = false;
                    for (JposEntry entry : config.getEntries()) {
                        if (entry.getLogicalName().equals(logicalName)) {
                            entry.modifyPropertyValue("address", ipAddress);
                            contains = true;
                        }
                    }
                    if (!contains) {
                        config.addEntry(logicalName, BXLConfigLoader.DEVICE_CATEGORY_POS_PRINTER, productName, BXLConfigLoader.DEVICE_BUS_ETHERNET, ipAddress);
                    }
                }

                config.saveFile();
            } catch (Exception e) {
				e.printStackTrace();
                ErrorReporter.INSTANCE.filelog(e);
            }
			
			POSPrinter posPrinter = new POSPrinter(MainActivity.getInstance());
			posPrinter.addErrorListener(MainActivity.getInstance());
			posPrinter.addOutputCompleteListener(MainActivity.getInstance());
			posPrinter.addStatusUpdateListener(MainActivity.getInstance());
			posPrinter.addDirectIOListener(MainActivity.getInstance());
            
			if (posPrinter.getState() == JposConst.JPOS_S_CLOSED) {
				posPrinter.open(logicalName);
				posPrinter.setAsyncMode(true);
			}
			if (!posPrinter.getClaimed()) {
				posPrinter.claim(10000);
				posPrinter.setDeviceEnabled(true);
				posPrinter.setCharacterSet(BXLConst.CS_865_NORDIC);
			}
            
			posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, "TEST KITCHEN PRINTER SRP-275IIIC");
            
            
		} catch (Exception e) {
			e.printStackTrace();
			ErrorReporter.INSTANCE.filelog(e);
		}
		
	}
	
	private void doTest2() {
		try {
			
			String barcode = "Rs109h100w400a-2t-13d1A110020161122132606889e";
		
			LinePrinterDeviceBase device = new SerialUp400(SerialUp400.Port.PORT_COM1, SerialUp400.BaudRate.BAUDRATE_9600, SerialUp400.BitLen.BITLEN_8, SerialUp400.ParityBit.PARITYBIT_NON, SerialUp400.StopBit.STOPBIT_1, SerialUp400.FlowCntl.FLOWCNTL_NON);

			StringBuilder s = new StringBuilder();
			s.append("\u001B|N");
			s.append("\u001b|" + 1 + "lF");
			s.append("1O1100501917");
			s.append("\u001B|");
			s.append(String.valueOf(24 + 20));
			s.append(barcode);
			s.append("\u001b|" + 4 + "lF");
			s.append("\u001b|fP");

			device.setMulticharMode(LinePrinterDeviceBase.PAGEMODE_USER);
			LinePrinter printer = new LinePrinter();
			printer.open(device);
			printer.printNormal(s.toString());
			printer.close();
			
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
		}
	}
	
	private void doTest3() {
		try {
			String barcode = "1A110020161122132606889";
			List<byte[]> commands = new ArrayList<>();
			LinePrinterDeviceBase device = new SerialUp400(SerialUp400.Port.PORT_COM1, SerialUp400.BaudRate.BAUDRATE_9600, SerialUp400.BitLen.BITLEN_8, SerialUp400.ParityBit.PARITYBIT_NON, SerialUp400.StopBit.STOPBIT_1, SerialUp400.FlowCntl.FLOWCNTL_NON);
			commands = device.getConverter().barCode(commands, BarCodeSymbology.BAR_CODE128, 400, 100, BarCodeAlignment.BAR_CENTER, BarCodeTextPosition.BAR_ABOVE, barcode);
			
//			StringBuilder s = new StringBuilder();
//			s.append("\u001B|N");
//			s.append("\u001b|" + 1 + "lF");
//			s.append("1A110020161122132606889");
//			s.append("\u001B|");
//			for (byte[] command : commands) {
//				s.append(new String(command));
//			}
//			s.append("\u001b|" + 4 + "lF");
//			s.append("\u001b|fP");
			
//			device.setMulticharMode(LinePrinterDeviceBase.PAGEMODE_USER);
//			device.write(commands);
//			LinePrinter printer = new LinePrinter();
//			printer.open(device);
//			printer.printNormal(s.toString());
//			printer.close();

		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
		}
	}

    private void createNewConfigFile() {
        InputStream is = MainActivity.getInstance().getResources().openRawResource(R.raw.jpos);
        FileOutputStream fos = null;

        int available = 0;
        byte[] buffer = null;

        try {
            available = is.available();
            buffer = new byte[available];
            is.read(buffer);

            fos = MainActivity.getInstance().openFileOutput(CONFIG_FILE_NAME, MODE_PRIVATE);
            fos.write(buffer);
        } catch (IOException e) {
			e.printStackTrace();
            ErrorReporter.INSTANCE.filelog(e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                ErrorReporter.INSTANCE.filelog(e);
            }

            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
	
	
}
