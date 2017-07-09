package no.susoft.mobile.pos.ui.activity;

import java.math.BigDecimal;
import java.util.*;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.*;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.*;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.*;
import android.widget.*;
import butterknife.InjectView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jp.co.casio.vx.framework.device.IButton;
import jp.co.casio.vx.framework.device.LinePrinter;
import jp.co.casio.vx.framework.device.lineprintertools.LinePrinterDeviceBase;
import jp.co.casio.vx.framework.device.lineprintertools.SerialUp400;
import jpos.JposConst;
import jpos.JposException;
import jpos.POSPrinter;
import jpos.POSPrinterConst;
import jpos.events.*;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.SusoftPOSApplication;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.*;
import no.susoft.mobile.pos.db.DBHelper;
import no.susoft.mobile.pos.display.asyncTasks.asyncBT.AcceptBTSocket;
import no.susoft.mobile.pos.display.asyncTasks.asyncWiFi.SendMessageToWFClient;
import no.susoft.mobile.pos.display.asyncTasks.asyncWiFi.SendMessageToWFServer;
import no.susoft.mobile.pos.display.broadcast.WifiDirectBroadcastReceiver;
import no.susoft.mobile.pos.display.initThreads.initWFThread.ClientInit;
import no.susoft.mobile.pos.display.initThreads.initWFThread.ServerInit;
import no.susoft.mobile.pos.display.socketManager.BTSocketManager;
import no.susoft.mobile.pos.display.util.WFDisplayMessageService;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.hardware.ConnectionManager;
import no.susoft.mobile.pos.hardware.combi.mPOPPrintOrderSlim;
import no.susoft.mobile.pos.hardware.combi.mPOPScanner;
import no.susoft.mobile.pos.hardware.printer.*;
import no.susoft.mobile.pos.hardware.scale.IDeviceManagerScale;
import no.susoft.mobile.pos.hardware.scale.IDeviceManagerScaleCallback;
import no.susoft.mobile.pos.hardware.scale.IDeviceManagerScaleCallback.Stub;
import no.susoft.mobile.pos.hardware.scale.ScaleDeviceService;
import no.susoft.mobile.pos.hardware.terminal.*;
import no.susoft.mobile.pos.hardware.terminal.events.*;
import no.susoft.mobile.pos.network.NetworkStateReceiver;
import no.susoft.mobile.pos.network.NetworkStateReceiver.NetworkStateReceiverListener;
import no.susoft.mobile.pos.network.Server;
import no.susoft.mobile.pos.network.ServerPing;
import no.susoft.mobile.pos.server.CustomerSearchFromDisplayAsync;
import no.susoft.mobile.pos.server.DbAPI;
import no.susoft.mobile.pos.server.SendReconciliationAsync;
import no.susoft.mobile.pos.server.SynchronizeDataAsync;
import no.susoft.mobile.pos.ui.activity.util.*;
import no.susoft.mobile.pos.ui.adapter.AccountSpinnerAdapter;
import no.susoft.mobile.pos.ui.adapter.ShopSpinnerAdapter;
import no.susoft.mobile.pos.ui.dialog.AccountSubsequentLoginDialog;
import no.susoft.mobile.pos.ui.dialog.BankTerminalDialog;
import no.susoft.mobile.pos.ui.dialog.GiftcardLookupDialog;
import no.susoft.mobile.pos.ui.dialog.PosChooserDialog;
import no.susoft.mobile.pos.ui.fragment.*;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;
import no.susoft.mobile.pos.ui.fragment.utils.NavigationDrawerOptions;
import no.susoft.mobile.pos.ui.slidemenu.OnNavigationMenuEventListener;
import no.susoft.mobile.pos.ui.slidemenu.SublimeBaseMenuItem;
import no.susoft.mobile.pos.ui.slidemenu.SublimeMenu;
import no.susoft.mobile.pos.ui.slidemenu.SublimeNavigationView;
import no.susoft.mobile.pos.updater.*;
import no.susoft.mobile.pos.usbdisplay.UsbDisplayConstants;
import no.susoft.mobile.pos.usbdisplay.UsbHostService;

@SuppressLint("HandlerLeak")
public class MainActivity extends BaseActivity implements IButton.StatCallback, IDallasKey, NetworkStateReceiverListener, ErrorListener, OutputCompleteListener, StatusUpdateListener, DirectIOListener {
	
	//region VARIABLES
	public static final int LOGIN_REQUEST = 1;
	private static final int ADMIN_REQUEST = 2;
	private static MainActivity instance;
	private static CardTerminalEventListener mainCardTerminalListener = null;
	public AccountSpinnerAdapter accountAdapter;
	public ShopSpinnerAdapter shopAdapter;
	public DallasKey dallasKey;
	boolean doubleBackToExitPressedOnce = false;
	private ProgressDialog progDialog;
	private AccountSubsequentLoginDialog loginDialog;
	private NetworkStateReceiver networkStateReceiver;
	private Timer networkCheckTimer;
	private boolean connected = false;
	private boolean isTimerRunning = false;
	private boolean needToUpdate = false;
	private DBHelper dbHelper;
	private CartFragment cartFragment;
	private NumpadScanFragment numpadScanFragment;
	private NumpadEditFragment numpadEditFragment;
	private NumpadPayFragment numpadPayFragment;
	private QuickLaunchFragment quickLaunchFragment;
	private OrdersFragment ordersFragment;
	private ProductSearchFragment productSearchFragment;
	private ServerCallMethods serverCallMethods;
	private AccountButtons mainAccountNameButtons;
	private MainShell mainShell;
	private ArrayList<OrderPointer> orderResults = new ArrayList<>();
	private ArrayList<DiscountReason> discountReasons = new ArrayList<>();
	private AccountActivity accountActivityContext;
	private boolean accountActivityReturningWithResult = false;
	private boolean startAdmin;
	private AdminDallasKeyFragment adminDallasKeyFragment;
	private boolean paused;
	private PosChooserDialog posChooserDialog;
	private boolean alreadyCallingPosDialog;
	private NavigationDrawerOptions navigationDrawer;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private SublimeMenu menu;
	private SublimeNavigationView snv;
	private GiftcardLookupDialog giftcardLookupDialog;
	private boolean loginDialogIsShowing = false;
	private BankTerminalDialog bankTerminalDialog = null;
	private mPOPScanner starScanner;
	private ServiceConnection weighterConn;
	private IDeviceManagerScale dmScale;
	private POSPrinter posPrinter = null;
	private POSPrinter kitchenPrinter = null;
	private POSPrinter barPrinter = null;
	private WifiP2pManager mManager;
	private WifiP2pManager.Channel mChannel;
	private WifiDirectBroadcastReceiver mReceiver;
	private UsbManager mUsbManager;
	private IntentFilter mIntentFilter;
	public static ServerInit server;
	private BTSocketManager mBtSocketManager;
	private ArrayList<BluetoothSocket> mBtSockets;
	private ArrayList<BluetoothDevice> pairedDevicesArray = new ArrayList<BluetoothDevice>();
	

	//endregion
	
	//region INJECT VIEWS
	@InjectView(R.id.account_linear_layout)
	LinearLayout accountNameButtonsLayout;
	@InjectView(R.id.toggle_btn_scan)
	ToggleButton toggle_btn_scan;
	@InjectView(R.id.toggle_btn_browse)
	ToggleButton toggle_btn_browse;
	@InjectView(R.id.toggle_btn_search)
	ToggleButton toggle_btn_search;
	@InjectView(R.id.toggle_btn_edit)
	ToggleButton toggle_btn_edit;
	@InjectView(R.id.toggle_btn_orders)
	ToggleButton toggle_btn_orders;
	@InjectView(R.id.toggleGroup)
	RadioGroup toggleGroup;
	@InjectView(R.id.top_button_delete_order)
	ImageView topIvDeleteOrder;
	@InjectView(R.id.top_iv_park_button)
	ImageView topBtnParkOrder;
	@InjectView(R.id.login_button)
	ImageView loginUserButton;
	@InjectView(R.id.logout_button)
	ImageView logoutUserButton;
	@InjectView(R.id.onlineStatus)
	ImageView onlineStatus;
	//endregion
	
	public static MainActivity getInstance() {
		return instance;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;
		setContentView(R.layout.navigation_drawer_main);
		networkStateReceiver = new NetworkStateReceiver();
		networkStateReceiver.addListener(this);
		this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
		connected = Server.INSTANCE.isNetworkActive();
		dbHelper = new DBHelper(MainActivity.getInstance());
//		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		ErrorReporter.INSTANCE.filelog("======== SUSOFT POS STARTED..");
		ErrorReporter.INSTANCE.filelog("Android OS isNetworkActive = " + connected);
		doOnCreate();
		checkForUpdate();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		//No call for super(). Bug on API Level > 11.
	}
	
	private void doOnCreate() {
		setKeyboardMode();
		initializeClasses();
		setupNavigationDrawer();
		initializeFragments();
		authenticateFirstUser();
	}
	
	public void checkForUpdate() {
		try {
			UpdateRequest.Builder builder = new UpdateRequest.Builder(this);
			builder.setVersionCheckStrategy(buildVersionCheckStrategy()).setPreDownloadConfirmationStrategy(buildPreDownloadConfirmationStrategy()).setDownloadStrategy(buildDownloadStrategy()).setPreInstallConfirmationStrategy(buildPreInstallConfirmationStrategy()).execute();
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
		}
	}
	
	private void initCardTerminal() {
		try {
			final CardTerminal cardTerminal = CardTerminalFactory.getInstance().getCardTerminal();
			if (cardTerminal != null) {
				
				// create if null
				if (mainCardTerminalListener == null) {
					mainCardTerminalListener = new VerboseCardTerminalEventListenerImpl() {
						
						@Override
						public void onError(CardTerminalErrorEvent event) {
							ErrorReporter.INSTANCE.filelog("CardTerminalEventListener", "onError()");
							runOnUiThread(new Runnable() {
								
								CardTerminalErrorEvent event;
								
								public Runnable setEvent(CardTerminalErrorEvent event) {
									this.event = event;
									return this;
								}
								
								@Override
								public void run() {
									
									new AlertDialog.Builder(MainActivity.getInstance()).setTitle("Error").setMessage(event.getErrorString()).setNegativeButton(R.string.OK, new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											dialog.dismiss();
										}
									}).setIcon(android.R.drawable.ic_dialog_alert).show();
								}
							}.setEvent(event));
						}
						
						@Override
						public void onPrintText(CardTerminalPrintTextEvent event) {
							
							Printer p = PrinterFactory.getInstance().getPrinter();
							
							String printText = event.getPrintText();
							String sig = event.getSignaturePrint();
							
							if (printText != null && printText.trim().length() > 0) {
								
								if (sig != null && sig.trim().length() > 0)
									printText = printText + "\n" + sig;
								
								if (printText.toUpperCase().contains("AVSTEMMING")) {
									String id = DbAPI.saveReconciliation(printText);
									Reconciliation reconciliation = DbAPI.getReconciliation(AccountManager.INSTANCE.getAccount().getShop().getID(), id);
									new SendReconciliationAsync().execute(reconciliation);
								}
								
								ErrorReporter.INSTANCE.filelog("CardTerminalEventListener", "onPrintText(" + printText + ")");
								
								// only if offline card and we need customer signature
								if (p instanceof VerifonePrinter && sig != null && sig.trim().length() > 0) {
									ErrorReporter.INSTANCE.filelog("CardTerminalEventListener", "printing on VeriFone");
									
									p.printLine(printText); // contains signature
									
								} else if (AppConfig.getState().getPrinterProviderOrdinal() == PeripheralProvider.CASIO.ordinal() && CasioPrintOrder.hasPrinterConnected()) {
									ErrorReporter.INSTANCE.filelog("CardTerminalEventListener", "printing on Casio");
									
									SerialUp400 device = new SerialUp400(SerialUp400.Port.PORT_COM1, SerialUp400.BaudRate.BAUDRATE_9600, SerialUp400.BitLen.BITLEN_8, SerialUp400.ParityBit.PARITYBIT_NON, SerialUp400.StopBit.STOPBIT_1, SerialUp400.FlowCntl.FLOWCNTL_NON);
									device.setMulticharMode(LinePrinterDeviceBase.PAGEMODE_USER);
									LinePrinter printer = new LinePrinter();
									int ret = printer.open(device);
									if (ret == LinePrinter.Response.SUCCESS) {
										printer.printNormal(printText);
										printer.printNormal(CasioPrint.lineFeedsAndCut(1));
									}
									
									printer.close();
									
								} else if (AppConfig.getState().getPrinterProviderOrdinal() == PeripheralProvider.BIXOLON.ordinal() && AppConfig.getState().getPrinterIp().isEmpty()) {
									
									ErrorReporter.INSTANCE.filelog("CardTerminalEventListener", "printing on BIXOLON (BT)");
									
									final String text = printText;
									ConnectionManager.getInstance().execute(new Runnable() {
										@Override
										public void run() {
											ArrayList<Object> print = new ArrayList<>();
											print.add(new byte[]{0x1b, 0x21, 0x00}); //ensure normal sized text
											print.add(text);
											new BluetoothPrintOrderWide().print(print);
										}
									});
									
								} else if (AppConfig.getState().getPrinterProviderOrdinal() == PeripheralProvider.BIXOLON.ordinal() && !AppConfig.getState().getPrinterIp().isEmpty()) {
									
									ErrorReporter.INSTANCE.filelog("CardTerminalEventListener", "printing on BIXOLON (IP)");
									
									final String text = printText;
									ConnectionManager.getInstance().execute(new BixolonPrinterJob() {
										@Override
										public int print() {
											ArrayList<Object> print = new ArrayList<>();
											print.add(new byte[]{0x1b, 0x21, 0x00}); //ensure normal sized text
											print.add(text);
											IPPrintOrderWide po = new IPPrintOrderWide();
											return po.publicPrintRawData(print);
										}
									});

								} else if (AppConfig.getState().getPrinterProviderOrdinal() == PeripheralProvider.STAR.ordinal()) {
									
									ErrorReporter.INSTANCE.filelog("CardTerminalEventListener", "printing on mPOP");
									
									final String text = printText;
									ConnectionManager.getInstance().execute(new Runnable() {
										@Override
										public void run() {
											String txt = text.substring(0, text.length() - 1);
											ArrayList<Object> print = new ArrayList<>();
											print.add(new byte[]{0x1b, 0x21, 0x00}); //ensure normal sized text
											print.add(txt);
											new mPOPPrintOrderSlim(print);
										}
									});
								}
							}
							
							//    june, 22 2016
							//
							//    [13:39:21] Mihail Meleca: adding print of bank terminal receipt on VeriFone
							//    [13:39:28] Svein A Lindelid: no
							//    [13:39:38] Svein A Lindelid: I don't think we need that
							//    [13:39:57] Svein A Lindelid: let me put it in othere words
							//    [13:40:06] Svein A Lindelid: We should not print that
							//    [13:40:14] Svein A Lindelid: on Verifone bank terminal
						}
						
						@Override
						public void onDisplayText(CardTerminalDisplayTextEvent event) {
							String displayText = event.getDisplayText();
							if (bankTerminalDialog != null && displayText != null && displayText.trim().length() > 0) {
								try {
									bankTerminalDialog.appendLine(displayText);
								} catch (Exception e) {
									ErrorReporter.INSTANCE.filelog("addNumberToPayment", "onDisplayText() error", e);
								}
							}
						}
						
						@Override
						public void onTransactionComplete(CardTerminalTransactionCompleteEvent event) {
							try {
								final TerminalResponse response = event.getTerminalResponse();
								int result = response.getResult();
								switch (result) {
									case CardTerminal.RESULT_FINANCIAL_TRANSACTION_OK:
										
										final int cardId = event.getTerminalResponse().getIssuerId();
										final String cardName = event.getTerminalResponse().getCardIssuerName();
										
										// refresh view
										MainActivity.instance.runOnUiThread(new Runnable() {
											
											@Override
											public void run() {
												MainActivity.instance.hideBankTerminalDialog();
												
												//stuff that updates ui
												BigDecimal HUNDRED = BigDecimal.valueOf(100.00);
												NumpadPayFragment numpadPayFragment = MainActivity.instance.getNumpadPayFragment();
												BigDecimal amount = BigDecimal.valueOf(response.getTotalAmount()).divide(HUNDRED, 2, BigDecimal.ROUND_HALF_UP);

												Decimal am;
												am = numpadPayFragment.isReturnState() ? Decimal.make(amount.negate()) : Decimal.make(amount);

												numpadPayFragment
														.getPaymentList()
														.addCardPayment(response.getApplicationTransactionCounter(),
																am, cardId, cardName, cardTerminal.terminalType);

												numpadPayFragment.refreshPayments();
											}
										});
										
										break;
								}
							} catch (Exception ex) {
								ErrorReporter.INSTANCE.filelog("addNumberToPayment", "onTransactionComplete fired exception ", ex);
							}
							
							hideBankTerminalDialog();
						}
						
						@Override
						public void onTransactionFailed(CardTerminalTransactionFailedEvent event) {
							try {
								int result = event.getTerminalResponse().getResult();
								
								switch (result) {
									case CardTerminal.RESULT_TRANSACTION_REJECTED:
										if (event.getTerminalResponse() != null) {
											ErrorReporter.INSTANCE.filelog("CardTerminalEventListener", "onTransactionFailed(RESULT_TRANSACTION_REJECTED) -> " + cardTerminal.decode(event.getTerminalResponse().getResult()) + "(" + event.getTerminalResponse().getRejectionReason() + ")");
											
											if (bankTerminalDialog != null) {
												bankTerminalDialog.appendLine("TRANSACTION REJECTED -> " + cardTerminal.decode(event.getTerminalResponse().getResult()) + "(" + event.getTerminalResponse().getRejectionReason() + ")");
											}
										}
										
										break;
									
									case CardTerminal.RESULT_UNKNOWN:
										// When BAXI Android has lost communication with the terminal during a transaction,
										// the OnLocalMode event will be triggered, with the local mode result set to an unknown status.
										if (bankTerminalDialog != null) {
											bankTerminalDialog.appendLine("TRANSACTION RESULT UNKNOWN PLEASE USE 'PRINT LAST TRANSACTION RESULT' TO SEE LAST SUCCESSFUL TRANSACTION.");
										}
										break;
								}
								
								if (bankTerminalDialog != null && event.getTerminalResponse() != null) {
									bankTerminalDialog.appendLine("TRANSACTION FAILED: " + event.getTerminalResponse().getRejectionReason());
								}
							} catch (Exception ex) {
								ErrorReporter.INSTANCE.filelog("MainActivity", "onTransactionFailed fired exception ", ex);
							}
							
							//endregion
						}
					};
					cardTerminal.addListener(mainCardTerminalListener);
				}
			}
		} catch (Exception ex) {
			ErrorReporter.INSTANCE.filelog("MainActivity", "Init Card Terminal instance failed ", ex);
		}
	}
	
	private void initPrinter() {
		// ok, so now we have factory to get printers
		// right now it supports VeriFone and mPOP
		PrinterFactory.getInstance().getPrinter();
	}
	
	private void initSecondaryDisplay() {
		SharedPreferences preferences = SusoftPOSApplication.getContext().getSharedPreferences(PeripheralDevice.class.toString(), Context.MODE_PRIVATE);
		int type = preferences.getInt("DISPLAY", PeripheralType.DISPLAY.ordinal());
		int provider = preferences.getInt("DISPLAY_PROVIDER", PeripheralProvider.NONE.ordinal());
		String ip = preferences.getString("DISPLAY_IP", "");
		String name = preferences.getString("DISPLAY_NAME", "");
        if (provider != PeripheralProvider.NONE.ordinal()) {
//            initWiFi2P();
			initBT();
        }
	}
	
	public void initBT() {
		mBtSockets = new ArrayList<>();
		mBtSocketManager = new BTSocketManager(MainActivity.this, true);
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			Toast.makeText(MainActivity.this, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show();
		} else {
			if (bluetoothAdapter.isEnabled()) {
				new AcceptBTSocket(getApplicationContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
			} else {
				Toast.makeText(MainActivity.this, "Bluetooth not enable", Toast.LENGTH_SHORT).show();
			}
		}
	}

	public void manageBTSocket(final BluetoothSocket socket) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mBtSocketManager.startConnection(socket);
				mBtSockets.add(socket);
				byte[] byteArray;
				String name = "Susoft";
				byteArray = mBtSocketManager.buildPacket(
						BTSocketManager.MESSAGE_NAME,
						name,
						name.getBytes()
				);

				Toast.makeText(MainActivity.this, "Connected with a display", Toast.LENGTH_SHORT).show();
				mBtSocketManager.writeChatRoomName(byteArray);
			}
		});

	}
	
	private void initWeighter() {
		try {
			weighterConn = new ServiceConnection() {
				public void onServiceConnected(ComponentName name, IBinder binder) {
					try {
						dmScale = (IDeviceManagerScale) binder;
						IDeviceManagerScaleCallback scaleCallback = new Stub() {
							@Override
							public void notifyWeight(int id, long weight, int status, long price) throws RemoteException {
								try {
									if (status == ScaleDeviceService.RESULT_SUCCESS) {
										if (Cart.selectedLine != null && Cart.selectedLine.getProduct() != null && Cart.selectedLine.getProduct().isWeighted()) {
											if (Cart.selectedLine.getProduct().getTare() > 0) {
												weight -= Cart.selectedLine.getProduct().getTare();
											}
											Cart.selectedLine.getProduct().setWeight(Decimal.make(weight).divide(Decimal.make(1000)));
											Cart.selectedLine.setQuantity(Decimal.make(weight).divide(Decimal.make(1000)));
											runOnUiThread(new Runnable() {
												@Override
												public void run() {
													cartFragment.refreshCart();
												}
											});
										}
									} else {
										// show alert dialog
										runOnUiThread(new Runnable() {
											@Override
											public void run() {
												Toast.makeText(MainActivity.getInstance(), "Coudnt read weight", Toast.LENGTH_LONG).show();
												if (Cart.selectedLine != null) {
													Cart.INSTANCE.removeSelectedOrderLine();
												}
											}
										});
									}
									MainActivity.getInstance().hideProgressDialog();
								} catch (Exception e) {
									ErrorReporter.INSTANCE.filelog(e);
								}
							}
							
							@Override
							public void notifyLiveWeight(int id, long weight, int status) throws RemoteException {

							}
						};
						dmScale.open(scaleCallback);
						//dmScale.liveWeight(2, false);
						
					} catch (Exception e) {
						ErrorReporter.INSTANCE.filelog(e);
					}
				}
				
				public void onServiceDisconnected(ComponentName name) {
				}
			};
			
			Intent intent = new Intent(this, ScaleDeviceService.class);
			bindService(intent, weighterConn, BIND_AUTO_CREATE);
			
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
		}
	}
	
	@Deprecated
	public int doCardTerminalCall(int result) {
		
		ErrorReporter.INSTANCE.filelog("MainActivity", "doCardTerminalCall result = " + result);
		
		switch (result) {
			case CardTerminal.SUCCESS:
				Toast.makeText(MainActivity.getInstance(), "Connected to card terminal. Follow terminal instructions...", Toast.LENGTH_SHORT).show();
				break;
			
			default:
			case CardTerminal.FAILURE:
			case CardTerminal.NOT_CONNECTED:
				try {
					CardTerminal cardTerminal = CardTerminalFactory.getInstance().getCardTerminal();
					ErrorReporter.INSTANCE.filelog("MainActivity", "cardTerminal.getMethodRejectCode() = " + cardTerminal.getMethodRejectCode());
					ErrorReporter.INSTANCE.filelog("Error connecting to card terminal! : " + cardTerminal.decode(cardTerminal.getMethodRejectCode()));
					Toast.makeText(MainActivity.getInstance(), "Error connecting to card terminal! (" + cardTerminal.getMethodRejectCode() + ")", Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					e.printStackTrace();
					ErrorReporter.INSTANCE.filelog("MainActivity", "Error connecting to card terminal! : ", e);
				}
				
				break;
		}
		
		return result;
	}
	
	private void setKeyboardMode() {
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
	}
	
	private void initializeClasses() {
		mainShell = new MainShell(getInstance());
		mainAccountNameButtons = new AccountButtons(accountNameButtonsLayout);
		serverCallMethods = new ServerCallMethods(getInstance());
		new AccountBar(mainAccountNameButtons, loginUserButton, logoutUserButton, accountNameButtonsLayout);
		//new MainTopBarMenu(topBtnPay, topBtnParkOrder, topIvDeleteOrder, toggleGroup, topIvMenu, toggle_btn_scan, toggle_btn_browse, toggle_btn_search, toggle_btn_edit, toggle_btn_orders);
		new MainTopBarMenu(toggleGroup, toggle_btn_scan, toggle_btn_browse, toggle_btn_search, toggle_btn_edit, toggle_btn_orders);
		dallasKey = new DallasKey();
		navigationDrawer = new NavigationDrawerOptions();
	}
	
	private void setupNavigationDrawer() {
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		snv = (SublimeNavigationView) findViewById(R.id.navigation_view);
		snv.setNavigationMenuEventListener(new OnNavigationMenuEventListener() {
			@Override
			public boolean onNavigationMenuEvent(OnNavigationMenuEventListener.Event event, SublimeBaseMenuItem menuItem) {
				switch (event) {
					case CHECKED:
						break;
					case UNCHECKED:
						break;
					case GROUP_EXPANDED:
						break;
					case GROUP_COLLAPSED:
						break;
					default:
						navigationDrawer.doActionForItemInPosition(menuItem.getItemId());
						closeDrawer();
						break;
				}
				return true;
			}
		});
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
		navigationDrawer.doActionForItemInPosition(id);
        return super.onOptionsItemSelected(item);
    }
	
	public void closeDrawer() {
		mDrawerLayout.closeDrawer(Gravity.LEFT);
	}
	
	private void initializeFragments() {
		numpadScanFragment = new NumpadScanFragment();
		numpadEditFragment = new NumpadEditFragment();
		quickLaunchFragment = new QuickLaunchFragment();
		productSearchFragment = new ProductSearchFragment();
		numpadPayFragment = new NumpadPayFragment();
		ordersFragment = new OrdersFragment();
	}
	
	private void authenticateFirstUser() {
		Intent accountIntent = new Intent(this, AccountActivity.class);
		startActivityForResult(accountIntent, LOGIN_REQUEST);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == LOGIN_REQUEST) {
			if (resultCode == RESULT_OK) {
				accountActivityReturningWithResult = true;
			} else {
				finish();
			}
		}
	}
	
    private void initWiFi2P() {
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = WifiDirectBroadcastReceiver.createInstance();
        mReceiver.setmManager(mManager);
        mReceiver.setmChannel(mChannel);
        mReceiver.setmActivity(this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        registerReceiver(mReceiver, mIntentFilter);
    }

    public void initServerAndClientThread(){
        if(mReceiver.isGroupeOwner() ==  WifiDirectBroadcastReceiver.IS_OWNER){
            server = new ServerInit();
            server.start();
        }
        else if(mReceiver.isGroupeOwner() ==  WifiDirectBroadcastReceiver.IS_CLIENT){
            ClientInit client = new ClientInit(mReceiver.getOwnerAddr());
            client.start();
        }

        startService(new Intent(this, WFDisplayMessageService.class));
    }
	
	private void initUsbDisplay() {
		
		if (isUsbHostServiceRunning(no.susoft.mobile.pos.usbdisplay.UsbHostService.class)) {
			displayUsbConnectionState(true);
		} else {
			displayUsbConnectionState(false);
		}
		
		final HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
		if (deviceList == null || deviceList.size() == 0) {
			displayUsbConnectionState(false);
			return;
		}
		
		if (searchForUsbAccessory(deviceList)) {
			return;
		}
		
		for (UsbDevice device : deviceList.values()) {
			initAccessory(device);
		}
	}
	
	private boolean searchForUsbAccessory(final HashMap<String, UsbDevice> deviceList) {
		for (UsbDevice device : deviceList.values()) {
			if (isUsbAccessory(device)) {
				final Intent service = new Intent(this, UsbHostService.class);
				service.putExtra(UsbDisplayConstants.DEVICE_EXTRA_KEY, device);
				startService(service);
				return true;
			}
		}
		
		return false;
	}
	
	private boolean isUsbAccessory(final UsbDevice device) {
		return (device.getProductId() == 0x2d00) || (device.getProductId() == 0x2d01);
	}
	
	private boolean initAccessory(final UsbDevice device) {
		final UsbDeviceConnection connection = mUsbManager.openDevice(device);
		if (connection == null) {
			return false;
		}
		initStringControlTransfer(connection, 0, UsbDisplayConstants.USB_PROTOCOL_MANUFACTURER);
		initStringControlTransfer(connection, 1, UsbDisplayConstants.USB_PROTOCOL_MODEL);
		initStringControlTransfer(connection, 2, UsbDisplayConstants.USB_PROTOCOL_DESCRIPTION);
		initStringControlTransfer(connection, 3, UsbDisplayConstants.USB_PROTOCOL_VERSION);
		initStringControlTransfer(connection, 4, UsbDisplayConstants.USB_PROTOCOL_URI);
		initStringControlTransfer(connection, 5, UsbDisplayConstants.USB_PROTOCOL_SERIAL);
		
		connection.controlTransfer(0x40, 53, 0, 0, new byte[]{}, 0, UsbDisplayConstants.USB_TIMEOUT_IN_MS);
		connection.close();
		return true;
	}
	
	private void initStringControlTransfer(final UsbDeviceConnection deviceConnection, final int index, final String string) {
		deviceConnection.controlTransfer(0x40, 52, 0, index, string.getBytes(), string.length(), UsbDisplayConstants.USB_TIMEOUT_IN_MS);
	}
	
	public void displayUsbConnectionState(boolean isConnected) {
		if (isConnected) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(MainActivity.this, "Connected by a Usb cable.", Toast.LENGTH_SHORT).show();
				}
			});
		} else {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(MainActivity.this, "Disconnected with a Usb cable.", Toast.LENGTH_SHORT).show();
				}
			});
		}
		
	}
	
	private boolean isUsbHostServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
	
	private synchronized void sendMessageToSecondaryUsbDisplay(String msg) {
		if (isUsbHostServiceRunning(no.susoft.mobile.pos.usbdisplay.UsbHostService.class)) {
			UsbHostService.getInstance().sendMessage(msg);
		}
	}
	
	public void receiveMessageFromSecondaryUsbDisplay(String msg) {
		runOnUiThread(new MessageProcessor(msg));
	}

	
	public void receiveMessageFromSecondaryBTDisplay(String msg) {
		runOnUiThread(new MessageProcessor(msg));
	}
	
	class MessageProcessor implements Runnable {
		
		public String m_msg;
		
		public MessageProcessor(String msg) {
			m_msg = msg;
		}
		
		@Override
		public void run() {
			messageManager(m_msg);
		}
	}

	private void sendMessageToSecondaryWFDisplay(String message) {
		if (mReceiver != null) {
			if(mReceiver.isGroupeOwner() == WifiDirectBroadcastReceiver.IS_OWNER){
				new SendMessageToWFClient(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
			}
			else if(mReceiver.isGroupeOwner() == WifiDirectBroadcastReceiver.IS_CLIENT){
				new SendMessageToWFServer(this, mReceiver.getOwnerAddr()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
			}
		}
	}

	private void sendMessageToSecondaryBtDisplay(String msg) {
		byte[] byteArray;

		if (msg.length() == 0) {
			return;
		}

		try {
			byte[] messageBytes = msg.getBytes();
			byteArray = mBtSocketManager.buildPacket(
					mBtSocketManager.MESSAGE_SEND,
					"Susoft",
					messageBytes
			);
		} catch (Exception e) {
			return;
		}

		mBtSocketManager.writeMessage(byteArray);
	}
	
	public void sendMessageToSecondaryDisplay(final String message){
//		if(isBluetooth) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					sendMessageToSecondaryBtDisplay(message);
				}
			});

//		} else if (isWiFi) {
//			runOnUiThread(new Runnable() {
//				@Override
//				public void run() {
//					sendMessageToSecondaryWFDisplay(message);
//				}
//			});
//		}

	}
	
	public void sendResetViewToSecondaryDisplay() {
		JsonObject result = new JsonObject();
		result.addProperty("action", 0);
		sendMessageToSecondaryDisplay(result.toString());
	}
	
	public void sendCustomerToSecondaryDisplay(Customer c) {
		JsonObject result = new JsonObject();
		result.addProperty("action", 3);
		if (c != null) {
			result.addProperty("customer", c.getName());
			result.addProperty("email", c.getEmail());
			result.addProperty("phone", c.getMobile());
		} else {
			result.addProperty("customer", "");
			result.addProperty("email", "");
			result.addProperty("phone", "");
		}
		sendMessageToSecondaryDisplay(result.toString());
	}
	
	public void sendOrderLinesToSecondaryDisplay() {
		sendMessageToSecondaryDisplay(serializeOrderLines());
	}
	
	public String serializeOrderLines() {
		JsonObject result = new JsonObject();
		result.addProperty("action", 2);
		JsonArray lines = new JsonArray();
		if (Cart.INSTANCE.hasActiveOrder()) {
			int i = 1;
			for (OrderLine line : Cart.INSTANCE.getOrder().getLines()) {
				if (line.getProduct() != null) {
					JsonObject lineObj = new JsonObject();
					lineObj.addProperty("id", i);
					lineObj.addProperty("text", line.getText());
					lineObj.addProperty("qty", line.getQuantity().toDouble());
					lineObj.addProperty("price", line.getPrice().toDouble());
					
					double discountAmount = 0.0;
					double discountPercent = 0.0;
					if (line.getDiscount() != null) {
						discountAmount = line.getDiscountAmount().toDouble();
						discountPercent = line.getDiscount().getPercent().toDouble();
					}
					
					lineObj.addProperty("discount_amount", discountAmount);
					lineObj.addProperty("discount_percent", discountPercent);
					lineObj.addProperty("vat_percent", Cart.INSTANCE.getProductVat(line.getProduct()));
					
					String sellerName = "";
					if (line.getSalesPersonId() != null && !line.getSalesPersonId().isEmpty() && MainActivity.getInstance().getMainShell().getSalesPersons() != null) {
						for (Properties sp : MainActivity.getInstance().getMainShell().getSalesPersons()) {
							if (sp.getProperty("id").equals(line.getSalesPersonId())) {
								sellerName = sp.getProperty("name");
							}
						}
					}
					
					lineObj.addProperty("seller_name", sellerName);
					lineObj.addProperty("note", line.getNote());
					
					JsonArray components = new JsonArray();
					if (line.getComponents() != null) {
						for (OrderLine cLine : line.getComponents()) {
							JsonObject c = new JsonObject();
							c.addProperty("text", cLine.getText());
							c.addProperty("qty", cLine.getQuantity().toDouble());
							c.addProperty("price", cLine.getPrice().toDouble());
							components.add(c);
						}
					}
					
					lineObj.add("components", components);
					lines.add(lineObj);
					i++;
				}
			}
		}
		result.add("lines", lines);
		
		System.out.println("result.toString() = " + result.toString());
		
		return result.toString();
	}
	
	@Override
	public void onPostResume() {
		super.onPostResume();
		doOnPostResume();
		paused = false;
	}
	
	private void doOnPostResume() {
		if (accountActivityReturningWithResult) {
			if (!AccountManager.INSTANCE.getLoggedInAccounts().isEmpty() && !needToUpdate) {
				setupInitialVisualElements();
				//To make sure the cart gets a shop id, else the app will crash.
				Cart.INSTANCE.resetCart();
				initialize();
			} else {
				goToInactiveState();
			}
		}
		
		accountActivityReturningWithResult = false;
		
		if (startAdmin) {
			dallasKey.removeReceiverIfExists(MainActivity.getInstance());
			startAdminActivityForResult();
		} else {
			if (AppConfig.getState().isUsingDallasKey()) {
				dallasKey.startIButton(this);
			}
		}
		
		if (AppConfig.getState().getScannerProviderOrdinal() == PeripheralProvider.STAR.ordinal() && starScanner == null) {
			starScanner = new mPOPScanner(this);
		}
	}
	
	private void startPosChooserDialog() {
		if (getPosChooserDialog() == null || !alreadyCallingPosDialog) {
			PosChooserDialog pcd = new PosChooserDialog();
			setPosChooserDialog(pcd);
			showProgressDialog(getString(R.string.loading_pos_list), false);
			loadPosObjects();
			setAlreadyCallingPosDialog(true);
		} else {
			Log.i("vilde", "Tried call another POS chooser dialog but it's being done already.");
		}
		//dialog is being shown in postexecute
	}
	
	public void setAlreadyCallingPosDialog(boolean state) {
		alreadyCallingPosDialog = state;
	}
	
	public void showProgressDialog(String msg, boolean cancelable) {
		if (progDialog == null || !progDialog.isShowing()) {
			progDialog = new ProgressDialog(MainActivity.getInstance());
			progDialog.setMessage(msg);
			progDialog.setIndeterminate(false);
			progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progDialog.setCancelable(cancelable);
			progDialog.setCanceledOnTouchOutside(false);
			progDialog.show();
		} else {
			progDialog.setMessage(msg);
		}
	}
	
	//    @Override
	//    public boolean dispatchKeyEvent(KeyEvent e) {
	//        if(toggle_btn_scan.isChecked()) {
	//            getNumpadScanFragment().performNumpadNumberClick(e.getKeyCode(), getNumpadScanFragment().getInputField());
	//            return true;
	//        } else if (toggle_btn_edit.isChecked()) {
	//            getNumpadEditFragment().performNumpadNumberClick(e.getKeyCode(), getNumpadEditFragment().getInputField());
	//        }
	//        return false;
	//    }
	
	public void hideProgressDialog() {
		if (progDialog != null) {
			progDialog.dismiss();
		}
	}
	
	private void loadPosObjects() {
		MainActivity.getInstance().getServerCallMethods().getPosForShopAsync();
	}
	
	public void initialize() {
		if (MainActivity.getInstance().isConnected()) {
			showProgressDialog(getString(R.string.synchronization), false);
			new SynchronizeDataAsync().execute();
		} else {
			initializePos();
		}
		initCardTerminal();
		initPrinter();
		initSecondaryDisplay();
		initWeighter();
	}
	
	public void initializePos() {
		if (AccountManager.INSTANCE.getSavedPos() == null) {
			startPosChooserDialog();
		} else {
			hideProgressDialog();
			AppConfig.getState().setPos(AccountManager.INSTANCE.getSavedPos());
		}
		mainShell.loadSalesPersons();
	}
	
	public void showPosChooserDialog() {
		if (getPosChooserDialog() != null) {
			hideProgressDialog();
			getPosChooserDialog().show(getSupportFragmentManager(), "poschooser");
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		dallasKey.removeReceiverIfExists(MainActivity.getInstance());
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
		}
		paused = true;
	}
	
    @Override
    protected void onResume() {
        super.onResume();
		if (getCartFragment() != null) {
			//getCartFragment().getCartButtons().setupRestaurantSettings(AppConfig.getState().isRestaurant());
		}
        if (mManager != null && mReceiver != null) {
            registerReceiver(mReceiver, mIntentFilter);
        }
//		initUsbDisplay();
    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		System.out.println("onDestroy");
		if (dbHelper != null) {
			dbHelper.close();
		}
		AccountManager.INSTANCE.doCompleteInvalidation();
		
		if (networkStateReceiver != null) {
			networkStateReceiver.removeListener(this);
			unregisterReceiver(networkStateReceiver);
		}
		if (weighterConn != null) {
			unbindService(weighterConn);
		}
		try {
			if (posPrinter != null) {
				posPrinter.close();
			}
			if (kitchenPrinter != null) {
				kitchenPrinter.close();
			}
			if (barPrinter != null) {
				barPrinter.close();
			}
		} catch (JposException e) {
			e.printStackTrace();
		}
		System.exit(2);
	}
	
	public void networkAvailable() {
		ErrorReporter.INSTANCE.filelog("Network available. Android OS isNetworkActive() = " + Server.INSTANCE.isNetworkActive());
		if (Server.INSTANCE.isNetworkActive()) {
			connected = true;
			isTimerRunning = false;
			
			if (networkCheckTimer != null) {
				networkCheckTimer.cancel();
				networkCheckTimer.purge();
				networkCheckTimer = null;
			}
			
			if (AccountManager.INSTANCE.getAccount() != null && AccountManager.INSTANCE.getAccount().getShop() != null) {
				ErrorReporter.INSTANCE.filelog("Synchronization start..");
				new SynchronizeDataAsync().execute();
			}
			
			ErrorReporter.INSTANCE.filelog("Switch Online");
			switchOnline();
		} else {
			ErrorReporter.INSTANCE.filelog("Switch Offline");
			switchOffline();
		}
	}
	
	@Override
	public void networkUnavailable() {
		ErrorReporter.INSTANCE.filelog("There's no network connectivity. Android OS isNetworkActive() = " + Server.INSTANCE.isNetworkActive());
		connected = false;
		switchOffline();
		
		if (Server.INSTANCE.isNetworkActive() && AccountManager.INSTANCE.getAccount() != null) {
			AccountManager.INSTANCE.getAccount().setToken("");
			
			if (!isTimerRunning || networkCheckTimer == null) {
				networkCheckTimer = new Timer("SusoftNetworkCheckTimer", true);
				networkCheckTimer.scheduleAtFixedRate(new TimerTask() {
					@Override
					public void run() {
						isTimerRunning = true;
						new ServerPing().execute();
					}
				}, 300000, 300000);
			}
		}
	}
	
	public void switchOnline() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				onlineStatus.setVisibility(View.GONE);
				if (cartFragment != null && cartFragment.getCartButtons() != null) {
					cartFragment.getCartButtons().refreshCartButtonStates();
				}
				if (numpadPayFragment != null) {
					numpadPayFragment.setOnlineMode();
				}
			}
		});
	}
	
	public void switchOffline() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				onlineStatus.setVisibility(View.VISIBLE);
				if (cartFragment != null && cartFragment.getCartButtons() != null) {
					cartFragment.getCartButtons().refreshCartButtonStates();
				}
				if (numpadPayFragment != null) {
					numpadPayFragment.setOfflineMode();
				}
			}
		});
	}
	
	//region GETTERS AND SETTERS
	
	public void startAdminActivity() {
		startAdmin = true;
		
		if (getAccountActivity() == null) {
			startAdminActivityForResult();
		}
	}
	
	private void startAdminActivityForResult() {
		Intent intent = new Intent(MainActivity.getInstance(), AdminActivity.class);
		MainActivity.getInstance().startActivityForResult(intent, MainActivity.ADMIN_REQUEST);
		startAdmin = false;
	}
	
	private void setupInitialVisualElements() {
		getMainShell().setupUIElements();
		MainTopBarMenu.getInstance().toggleInitialNumpadFragment();
		getCartFragment().getCartButtons().refreshCartButtonStates();
	}
	
	//For the toggle buttons in the top menu bar. Needs to be in this class.
	public void onToggle(View view) {
		MainTopBarMenu.getInstance().onToggle(view);
	}
	
	public MainShell getMainShell() {
		return mainShell;
	}
	
	public AccountActivity getAccountActivity() {
		return accountActivityContext;
	}
	
	public ServerCallMethods getServerCallMethods() {
		return serverCallMethods;
	}
	
	public CartFragment getCartFragment() {
		return cartFragment;
	}
	
	public void setCartFragment(CartFragment cartFragment) {
		this.cartFragment = cartFragment;
	}
	
	public NumpadScanFragment getNumpadScanFragment() {
		return numpadScanFragment;
	}
	
	public void setNumpadScanFragment(NumpadScanFragment numpadScanFragment) {
		this.numpadScanFragment = numpadScanFragment;
	}
	
	public NumpadEditFragment getNumpadEditFragment() {
		return this.numpadEditFragment;
	}
	
	public void setNumpadEditFragment(NumpadEditFragment numpadEditFragment) {
		this.numpadEditFragment = numpadEditFragment;
	}
	
	public QuickLaunchFragment getQuickLaunchFragment() {
		return this.quickLaunchFragment;
	}
	
	public void setQuickLaunchFragment(QuickLaunchFragment quickLaunchFragment) {
		this.quickLaunchFragment = quickLaunchFragment;
	}
	
	public ProductSearchFragment getProductSearchFragment() {
		return this.productSearchFragment;
	}
	
	public void setProductSearchFragment(ProductSearchFragment productSearchFragment) {
		this.productSearchFragment = productSearchFragment;
	}
	
	public NumpadPayFragment getNumpadPayFragment() {
		return numpadPayFragment;
	}
	
	public void setNumpadPayFragment(NumpadPayFragment numpadPayFragment) {
		this.numpadPayFragment = numpadPayFragment;
	}
	
	public OrdersFragment getOrdersFragment() {
		return ordersFragment;
	}
	
	public void setOrdersFragment(OrdersFragment ordersFragment) {
		this.ordersFragment = ordersFragment;
	}
	
	public ArrayList<DiscountReason> getDiscountReasons() {
		return discountReasons;
	}
	
	public void setDiscountReasons(ArrayList<DiscountReason> discountReasons) {
		this.discountReasons = discountReasons;
	}
	
	public ArrayList<OrderPointer> getOrderResults() {
		return orderResults;
	}
	
	public void setOrderResults(ArrayList<OrderPointer> orderResults) {
		this.orderResults = orderResults;
	}
	
	public void setAccountActivityContext(AccountActivity accountActivityContext) {
		this.accountActivityContext = accountActivityContext;
	}
	
	//endregion
	
	public AdminDallasKeyFragment getAdminDallasKeyFragment() {
		return adminDallasKeyFragment;
	}
	
	public void setAdminDallasKeyFragment(AdminDallasKeyFragment adminDallasKeyFragment) {
		this.adminDallasKeyFragment = adminDallasKeyFragment;
	}
	
	@Override
	public void onChangeIbutton(boolean b, byte[] bytes) {
		dallasKey.onChangeIButton(this, "MainActivity", b, bytes);
	}
	
	public void goToInactiveState() {
		new Handler().post(new Runnable() {
			public void run() {
				dallasKey.registerReceiverIfNotDoneAlready(MainActivity.getInstance());
				if (loginDialog == null || loginDialog.getDialog() == null) {
					loginDialog = new AccountSubsequentLoginDialog();
				}
				
				if (!loginDialogIsShowing) {
					loginDialog.show(getSupportFragmentManager(), "nodismiss");
					loginDialogIsShowing = true;
				}
				
				setAlreadyCallingPosDialog(false);
			}
		});
	}
	
	public boolean handleIfIsInInactiveState() {
		if (loginDialog != null) {
			loginDialog.dismissAllowingStateLoss();
			loginDialogIsShowing = false;
			MainActivity.getInstance().initializePos();
			return true;
		}
		return false;
	}
	
	public AccountSubsequentLoginDialog getLoginDialog() {
		return loginDialog;
	}
	
	public boolean isPaused() {
		return paused;
	}
	
	public PosChooserDialog getPosChooserDialog() {
		return posChooserDialog;
	}
	
	public void setPosChooserDialog(PosChooserDialog posChooserDialog) {
		this.posChooserDialog = posChooserDialog;
	}
	
	@Override
	public void onBackPressed() {
		doubleClickBackToExit();
	}
	
	private void doubleClickBackToExit() {
		if (doubleBackToExitPressedOnce) {
			super.onBackPressed();
			MainActivity.getInstance().finish();
		} else {
			this.doubleBackToExitPressedOnce = true;
			Toast.makeText(this, getString(R.string.press_back_again_to_exit), Toast.LENGTH_SHORT).show();
			new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					doubleBackToExitPressedOnce = false;
				}
			}, 2000);
		}
	}
	
	@Override
	public void registerDallasKeyReceiver(BroadcastReceiver intentReceiver, IntentFilter intentFilter) {
		this.registerReceiver(intentReceiver, intentFilter);
	}
	
	public void writeToCurrentInput(String text) {
		if (toggle_btn_scan.isChecked()) {
			getNumpadScanFragment().setInputFieldToString(text);
		} else if (toggle_btn_edit.isChecked()) {
			getNumpadEditFragment().setInputFieldToString(text);
		} else if (toggle_btn_search.isChecked()) {
			getProductSearchFragment().setInputFieldToString(text);
		} else if (toggle_btn_browse.isChecked()) {
			getQuickLaunchFragment().setInputFieldToString(text);
		} else if (noButtonsAreToggled()) {
			//Should mean that it's on the pay view
			getNumpadPayFragment().doEnterClickInMainActivity();
		}
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				return dispatchEnterDown();
			}
		} else if (noButtonsAreToggled()) {
			try {
				getNumpadPayFragment().dispatchKeyToInputField(event);
				return true;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return super.dispatchKeyEvent(event);
	}
	
	public boolean dispatchEnterDown() {
		if (toggle_btn_scan.isChecked()) {
			getNumpadScanFragment().doEnterClick();
		} else if (toggle_btn_edit.isChecked()) {
			getNumpadEditFragment().doEnterClick();
		} else if (toggle_btn_search.isChecked()) {
			getProductSearchFragment().doEnterClick();
		} else if (toggle_btn_browse.isChecked()) {
			getQuickLaunchFragment().doEnterClick();
		} else if (noButtonsAreToggled()) {
			//Should mean that it's on the pay view
			getNumpadPayFragment().doEnterClickInMainActivity();
		} else {
			return false;
		}
		return true;
	}
	
	public void hideBankTerminalDialog() {
		MainActivity.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					if (bankTerminalDialog != null) {
						bankTerminalDialog.dismissAllowingStateLoss();
						bankTerminalDialog = null;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public synchronized void showBankTerminalDialog() {
		MainActivity.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					bankTerminalDialog = new BankTerminalDialog();
					bankTerminalDialog.show(getSupportFragmentManager(), "Card terminal");
				} catch (Exception e) {
					ErrorReporter.INSTANCE.filelog("showBankTerminalDialog", "Error", e);
				}
			}
		});
	}
	
	public synchronized void addLineToBankTerminalDialog(String line) {
		bankTerminalDialog.appendLine(line);
	}
	
	public BankTerminalDialog getBankTerminalDialog() {
		return bankTerminalDialog;
	}
	
	private boolean noButtonsAreToggled() {
		//EQUALS PAYMENT FRAGMENT IS TOGGLED, or error
		return !toggle_btn_edit.isChecked() && !toggle_btn_scan.isChecked() && !toggle_btn_browse.isChecked() && !toggle_btn_orders.isChecked() && !toggle_btn_search.isChecked();
	}
	
	public void focusOnActiveFragmentInputField() {
		if (toggle_btn_scan.isChecked()) {
			getNumpadScanFragment().inputFieldRequestFocus();
			getNumpadScanFragment().hideKeyboard();
		} else if (toggle_btn_edit.isChecked()) {
			getNumpadEditFragment().inputFieldRequestFocus();
			getNumpadEditFragment().hideKeyboard();
		}
	}
	
	public boolean editFragmentIsToggled() {
		return toggle_btn_edit.isChecked();
	}
	
	public void refreshActiveFragment() {
		if (editFragmentIsToggled()) {
			getNumpadEditFragment().refreshView();
		} else if (payFragmentIsToggled()) {
			getNumpadPayFragment().refreshView();
		}
	}
	
	private boolean payFragmentIsToggled() {
		return noButtonsAreToggled();
	}
	
	public GiftcardLookupDialog getGiftcardLookupDialog() {
		return giftcardLookupDialog;
	}
	
	public void setGiftcardLookupDialog(GiftcardLookupDialog giftcardLookupDialog) {
		this.giftcardLookupDialog = giftcardLookupDialog;
	}
	
	VersionCheckStrategy buildVersionCheckStrategy() {
		return (new SimpleHttpVersionCheckStrategy(String.format("http://%1$s/sutemplates/PointOfSale.json", Server.authority)));
	}
	
	ConfirmationStrategy buildPreDownloadConfirmationStrategy() {
		return (new ImmediateConfirmationStrategy());
	}
	
	DownloadStrategy buildDownloadStrategy() {
		return (new InternalHttpDownloadStrategy());
	}
	
	ConfirmationStrategy buildPreInstallConfirmationStrategy() {
		return (new ImmediateConfirmationStrategy());
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	public boolean isNeedToUpdate() {
		return needToUpdate;
	}
	
	public void forceUpdate() {
		needToUpdate = true;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (accountActivityContext != null) {
					accountActivityContext.setVisualStateForUpdate();
				}
			}
		});
	}
	
	public DBHelper getDbHelper() {
		return dbHelper;
	}
	
	public IDeviceManagerScale getDmScale() {
		return dmScale;
	}
	
	public POSPrinter getPOSPrinter() {
		if (posPrinter == null) {
			posPrinter = new POSPrinter(this);
			posPrinter.addErrorListener(this);
			posPrinter.addOutputCompleteListener(this);
			posPrinter.addStatusUpdateListener(this);
			posPrinter.addDirectIOListener(this);
		}
		return posPrinter;
	}
	
	public POSPrinter getKitchenPrinter() {
		if (kitchenPrinter == null) {
			kitchenPrinter = new POSPrinter(this);
			kitchenPrinter.addErrorListener(this);
			kitchenPrinter.addOutputCompleteListener(this);
			kitchenPrinter.addStatusUpdateListener(this);
			kitchenPrinter.addDirectIOListener(this);
		}
		return kitchenPrinter;
	}
	
	public POSPrinter getBarPrinter() {
		if (barPrinter == null) {
			barPrinter = new POSPrinter(this);
			barPrinter.addErrorListener(this);
			barPrinter.addOutputCompleteListener(this);
			barPrinter.addStatusUpdateListener(this);
			barPrinter.addDirectIOListener(this);
		}
		return barPrinter;
	}
	
	static String getStatusString(int state) {
		switch (state) {
			case JposConst.JPOS_S_BUSY:
				return "JPOS_S_BUSY";
			
			case JposConst.JPOS_S_CLOSED:
				return "JPOS_S_CLOSED";
			
			case JposConst.JPOS_S_ERROR:
				return "JPOS_S_ERROR";
			
			case JposConst.JPOS_S_IDLE:
				return "JPOS_S_IDLE";
			
			default:
				return "Unknown State";
		}
	}
	
	public void showPrinterNotConnectedToast() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(SusoftPOSApplication.getContext(), MainActivity.getInstance().getResources().getString(R.string.no_printer_connected), Toast.LENGTH_LONG).show();
			}
		});
	}
	
	@Override
	public void directIOOccurred(DirectIOEvent e) {
		ErrorReporter.INSTANCE.filelog("DirectIO: " + e + "(" + e.getData() + ")");
		if (e.getObject() != null) {
			ErrorReporter.INSTANCE.filelog(new String((byte[]) e.getObject()));
		}
	}
	
	@Override
	public void errorOccurred(ErrorEvent e) {
		ErrorReporter.INSTANCE.filelog("Error: " + e);
	}
	
	@Override
	public void outputCompleteOccurred(OutputCompleteEvent e) {
		ErrorReporter.INSTANCE.filelog("OCE: " + e.getOutputID() + "\n");
		try {
			getPOSPrinter().release();
		} catch (Exception e1) {
			ErrorReporter.INSTANCE.filelog(e1);
		}
	}
	
	@Override
	public void statusUpdateOccurred(StatusUpdateEvent e) {
		ErrorReporter.INSTANCE.filelog("SUE: (" + e.getStatus() + ") " + getSUEMessage(e.getStatus()) + "\n");
	}
	
	private String getSUEMessage(int status) {
		switch (status) {
			case JposConst.JPOS_SUE_POWER_OFF_OFFLINE:
				return "Power off";
			
			case POSPrinterConst.PTR_SUE_COVER_OPEN:
				return "Cover Open";
			
			case POSPrinterConst.PTR_SUE_COVER_OK:
				return "Cover OK";
			
			case POSPrinterConst.PTR_SUE_REC_EMPTY:
				return "Receipt Paper Empty";
			
			case POSPrinterConst.PTR_SUE_REC_NEAREMPTY:
				return "Receipt Paper Near Empty";
			
			case POSPrinterConst.PTR_SUE_REC_PAPEROK:
				return "Receipt Paper OK";
			
			case POSPrinterConst.PTR_SUE_IDLE:
				return "Printer Idle";
			
			default:
				return "Unknown";
		}
	}
	
    public synchronized void messageManager(final String msg) {
        JsonParser parser = new JsonParser();
        JsonObject object = parser.parse(msg).getAsJsonObject();
        int action = object.get("action").getAsInt();

        switch (action) {
            case 1:
                String salesPersonName = "";
                if (AccountManager.INSTANCE.getAccount() != null) {
                    salesPersonName = AccountManager.INSTANCE.getAccount().getName();
                }

                JsonObject result = new JsonObject();
                result.addProperty("action", action);
                result.addProperty("shop_name", AccountManager.INSTANCE.getSavedShopName());
                result.addProperty("salesperson", salesPersonName);
                sendMessageToSecondaryDisplay(result.toString());
                break;

            case 2:
                ErrorReporter.INSTANCE.filelog("result = " + serializeOrderLines());
                sendMessageToSecondaryDisplay(serializeOrderLines());
                break;

            case 3:
                String phone = object.get("phone").getAsString();
                String email = object.get("email").getAsString();
                ErrorReporter.INSTANCE.filelog("phone = " + phone);
                ErrorReporter.INSTANCE.filelog("email = " + email);
                new CustomerSearchFromDisplayAsync().execute(phone, email);
                break;
        }
    }
}