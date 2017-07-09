package no.susoft.mobile.pos.ui.fragment.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.*;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.PeripheralProvider;
import no.susoft.mobile.pos.data.ReceiptPrintType;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.hardware.ConnectionManager;
import no.susoft.mobile.pos.hardware.combi.mPOPPrintExchangeCart;
import no.susoft.mobile.pos.hardware.printer.*;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.activity.util.AppConfig;
import no.susoft.mobile.pos.ui.activity.util.MainTopBarMenu;
import no.susoft.mobile.pos.ui.dialog.OrderAddNoteDialog;
import no.susoft.mobile.pos.ui.dialog.OrderDiscountDialog;
import no.susoft.mobile.pos.ui.dialog.TableViewDialog;

public class CartButtons {

	private final RelativeLayout rlParkOrder;
	private final RelativeLayout rlDeleteOrder;
	private final RelativeLayout rlLoadOrder;
	private RelativeLayout rlAddOrder;
	private RelativeLayout rlSplitOrder;
	private RelativeLayout rlPrelimReceipt;
	private final RelativeLayout rlDeleteLine;
	private RelativeLayout rlTableButton;
	private RelativeLayout rlTakeAwayButton;
	private RelativeLayout rlMoreButton;
	private RelativeLayout rlNoteButton;
	private RelativeLayout rlDiscountOrder;
	private RelativeLayout rlExchangeCart;
	private final LinearLayout parentLayout;
	private boolean isRestaurant;
	private float disabledAlpha = 0.3f;
	PopupWindow popupWindow;
	Drawable noteBackground;

	public CartButtons(LinearLayout rootLayout) {
		this.parentLayout = rootLayout;
		this.rlParkOrder = (RelativeLayout) rootLayout.findViewById(R.id.rlParkOrder);
		this.rlDeleteLine = (RelativeLayout) rootLayout.findViewById(R.id.rlDeleteLineButton);
		this.rlDeleteOrder = (RelativeLayout) rootLayout.findViewById(R.id.rlDeleteOrder);
		this.rlLoadOrder = (RelativeLayout) rootLayout.findViewById(R.id.rlLoadOrderButton);

		this.rlAddOrder = (RelativeLayout) rootLayout.findViewById(R.id.rlAddOrder);
		this.rlTableButton = (RelativeLayout) rootLayout.findViewById(R.id.rlTableButton);
		this.rlTakeAwayButton = (RelativeLayout) rootLayout.findViewById(R.id.rlTakeAwayButton);
		this.rlMoreButton = (RelativeLayout) rootLayout.findViewById(R.id.rlMoreButton);

		setListeners();
	}

	public PopupWindow createDropdownWindow() {

		popupWindow = new PopupWindow(MainActivity.getInstance());

		int minWidth = 60;
		int height = 63;

		LinearLayout dropdown = setupDropdownContent();
		int moreWidth = rlMoreButton.getMeasuredWidth();
		int moreHeight = rlMoreButton.getMeasuredWidth();
		int extraPadding = 6;


        popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		
		popupWindow.setFocusable(true);
		popupWindow.setTouchable(true);
		popupWindow.setOutsideTouchable(true);

//		if (moreWidth >= minWidth)
//			popupWindow.setWindowLayoutMode(moreWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
//		else
//			popupWindow.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//
//		popupWindow.setWidth((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, moreWidth, MainActivity.getInstance().getResources().getDisplayMetrics()));
//		popupWindow.setHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (moreHeight * dropdown.getChildCount()) + extraPadding, MainActivity.getInstance().getResources().getDisplayMetrics()));
		popupWindow.setContentView(dropdown);

		return popupWindow;
	}

	private LinearLayout setupDropdownContent() {
		// inflate your layout or dynamically add view
		LayoutInflater inflater = (LayoutInflater) MainActivity.getInstance().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View splitView = inflater.inflate(R.layout.cart_top_menu_bar_split_button, null);
		View prelimView = inflater.inflate(R.layout.cart_top_menu_bar_prelim_receipt_button, null);

		rlSplitOrder = (RelativeLayout) splitView.findViewById(R.id.rlSplitOrder);
		rlPrelimReceipt = (RelativeLayout) prelimView.findViewById(R.id.rlPrelimReceipt);

		View popupView = inflater.inflate(R.layout.cart_top_menu_dropdown, null);
		LinearLayout ll = (LinearLayout) popupView.findViewById(R.id.rlCartDropdown);
		ll.addView(rlSplitOrder);
		ll.addView(rlPrelimReceipt);

		if (isRestaurant) {
			addNoteView(ll);
			addDiscountView(ll);
		}

		return ll;
	}

	public void setupRestaurantSettings(boolean isRestaurant) {
		this.isRestaurant = isRestaurant;

		if (!isRestaurant) {
			try {
				parentLayout.removeView(rlAddOrder);
				parentLayout.removeView(rlTableButton);
				parentLayout.removeView(rlMoreButton);
				parentLayout.removeView(rlTakeAwayButton);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				if (parentLayout.indexOfChild(rlNoteButton) < 0) {
					addNoteView(parentLayout);
				}
				if (parentLayout.indexOfChild(rlDiscountOrder) < 0) {
					addDiscountView(parentLayout);
				}
				if (parentLayout.indexOfChild(rlExchangeCart) < 0) {
					addExchangeCartView(parentLayout);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		setupPopupStuff();

		if (isRestaurant) {
			setTableListener();
			setTakeAwayListener();
			enableView(rlMoreButton);
			setMoreListener();
		} else {
			removeView(rlMoreButton);
		}
		adjustButtonSizing();
		refreshCartButtonStates();
	}

	private void adjustButtonSizing() {
		parentLayout.invalidate();
		int counter = 0;
		for (int i = 0; i < parentLayout.getChildCount(); i++) {
			View v = parentLayout.getChildAt(i);
			if (v.getVisibility() == View.VISIBLE) {
				counter++;
			}
			LinearLayout.LayoutParams loparams = (LinearLayout.LayoutParams) v.getLayoutParams();
			loparams.height = LayoutParams.MATCH_PARENT;
			loparams.width = 0;
			loparams.weight = 1;
			v.setLayoutParams(loparams);
		}
		parentLayout.setWeightSum(counter);
	}

	private void addNoteView(LinearLayout parent) {
		LayoutInflater inflater = (LayoutInflater) MainActivity.getInstance().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View noteView = inflater.inflate(R.layout.cart_top_menu_bar_note_button, null);
		rlNoteButton = (RelativeLayout) noteView.findViewById(R.id.rlAddNote);
		parent.addView(rlNoteButton);
		setNoteOnClickListener();
		noteBackground = rlNoteButton.getBackground();
	}

	private void addDiscountView(LinearLayout parent) {
		LayoutInflater inflater = (LayoutInflater) MainActivity.getInstance().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View discountView = inflater.inflate(R.layout.cart_top_menu_bar_discount_button, null);
		rlDiscountOrder = (RelativeLayout) discountView.findViewById(R.id.rlDiscountOrder);
		parent.addView(rlDiscountOrder);
		setDiscountOrderListener();
	}

	private void addExchangeCartView(LinearLayout parent) {
		LayoutInflater inflater = (LayoutInflater) MainActivity.getInstance().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View exchangeCartView = inflater.inflate(R.layout.cart_top_menu_bar_exchangecart_button, null);
		rlExchangeCart = (RelativeLayout) exchangeCartView.findViewById(R.id.rlExchangeCart);
		parent.addView(rlExchangeCart);
		rlExchangeCart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
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
									CasioPrintExchangeCart po = new CasioPrintExchangeCart(Cart.INSTANCE.getOrder());
									result = po.print(Cart.INSTANCE.getOrder(), ReceiptPrintType.ORIGINAL);
									counter++;
								} while (result != 0 && counter < 20);
								if (counter == 20 && result != 0) {
									MainActivity.getInstance().showPrinterNotConnectedToast();
								}
							}
						});
					} else {
						MainActivity.getInstance().showPrinterNotConnectedToast();
					}
					// endregion
				} else if (printerProvider == PeripheralProvider.BIXOLON.ordinal()) {
					// region BIXOLON
					if (AppConfig.getState().getPrinterIp().isEmpty()) {
						ConnectionManager.getInstance().execute(new Runnable() {
							@Override
							public void run() {
								BluetoothPrintExchangeCart bp = new BluetoothPrintExchangeCart(Cart.INSTANCE.getOrder());
								bp.print(bp.makeReceipt(Cart.INSTANCE.getOrder()));
							}
						});
					} else {
						ConnectionManager.getInstance().execute(new BixolonPrinterJob() {
							@Override
							public int print() {
								IPPrintExchangeCart po = new IPPrintExchangeCart(Cart.INSTANCE.getOrder());
								return po.printIP(Cart.INSTANCE.getOrder(), AppConfig.getState().getPrinterName(), AppConfig.getState().getPrinterIp());
							}
						});
					}
					// endregion
				} else if (printerProvider == PeripheralProvider.STAR.ordinal()) {
					// region STAR
					ConnectionManager.getInstance().execute(new Runnable() {
						@Override
						public void run() {
							new mPOPPrintExchangeCart(Cart.INSTANCE.getOrder());
						}
					});
					// endregion
				} else {
					MainActivity.getInstance().showPrinterNotConnectedToast();
				}

			}
		});
	}

	//LISTENERS ========================================================================================================
	private void setListeners() {
		setLoadCartOnClickListener();
		setDeleteOrderListener();
		setParkOrderListener();
		setDeleteLineOnClickListener();
		if (rlAddOrder != null) {
			setAddOrderOnClickListener();
		}
		if (rlMoreButton != null) {
			setMoreListener();
		}
	}

	private void setupPopupStuff() {
		createDropdownWindow();
		setSplitOrderListener();
		setPrelimPrintListener();
		setDiscountOrderListener();
		setPopupListener();
	}

	private void setPrelimPrintListener() {
		rlPrelimReceipt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				doPrelimPrintOnClick();
			}
		});
	}

	private void setParkOrderListener() {
		rlParkOrder.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				doParkOnClick();
			}
		});
	}

	private void setAddOrderOnClickListener() {
		rlAddOrder.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				doAddOrderClick();
			}
		});
	}

	private void setLoadCartOnClickListener() {
		rlLoadOrder.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				doLoadCartClick();
			}
		});
	}

	private void setDeleteOrderListener() {
		rlDeleteOrder.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				doDeleteOrderClick();
			}
		});
	}

	private void setDeleteLineOnClickListener() {
		rlDeleteLine.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				doDeleteLineClick();
			}
		});
	}

	private void setSplitOrderListener() {
		rlSplitOrder.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doSplitOrderClick();
			}
		});
	}

	private void setDiscountOrderListener() {
		rlDiscountOrder.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doDiscountClick();
			}
		});
	}

	private Handler handler = new Handler();
	private Runnable showPopup = new Runnable() {
		@Override
		public void run() {
			popupWindow.showAsDropDown(rlMoreButton, 0, 0);
		}
	};

	private void setMoreListener() {
		rlMoreButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (popupWindow == null) {
					try {
						createDropdownWindow();
					} catch (Exception e) {
						ErrorReporter.INSTANCE.filelog("createDropdownWindow", "", e);
					}
				}

				if (popupWindow.isShowing()) {
					popupWindow.dismiss();
				} else {
					try {
						if (rlMoreButton != null) {
							int buttonWidth = rlMoreButton.getWidth() - 4;
							int buttonHeight = rlMoreButton.getHeight();
							
							if (popupWindow != null) {
								LinearLayout dropdown = (LinearLayout) popupWindow.getContentView();
								if (dropdown != null) {
									dropdown.invalidate();
									for (int i = 0; i < dropdown.getChildCount(); i++) {
										View v = dropdown.getChildAt(i);
										LinearLayout.LayoutParams loparams = (LinearLayout.LayoutParams) v.getLayoutParams();
										loparams.height = buttonHeight;
										loparams.width = buttonWidth;
										v.setLayoutParams(loparams);
									}
								}
							}
						}

						popupWindow.showAsDropDown(rlMoreButton, -2, 0);
						popupWindow.update();
					} catch (Exception e) {
						ErrorReporter.INSTANCE.filelog("showAsDropDown", "", e);
					}
					//handler.postAtFrontOfQueue(showPopup);
				}
			}
		});
	}

	private void setPopupListener() {
		popupWindow.getContentView().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				popupWindow.dismiss();
			}
		});
	}

	private void setTableListener() {
		rlTableButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doTableClick();
			}
		});
	}

	private void setTakeAwayListener() {
		rlTakeAwayButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doTakeAwayClick();
			}
		});
	}

	private void setNoteOnClickListener() {
		rlNoteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				doNoteClick();
			}
		});
	}

	//LISTENER clicks===================================================================================================
	private void doParkOnClick() {
		Cart.INSTANCE.doParkClick();
	}

	private void doAddOrderClick() {
		Cart.INSTANCE.doAddOrderClick();
	}

	private void doLoadCartClick() {
		Cart.INSTANCE.doLoadCartClick();
	}

	private void doDeleteOrderClick() {
		Cart.INSTANCE.doDeleteOrderClick();
	}

	private void doDeleteLineClick() {
		Cart.INSTANCE.doDeleteLineClick();
	}

	public TableViewDialog tableViewDialog;

	private void doTableClick() {
		tableViewDialog = new TableViewDialog();
		tableViewDialog.show(MainActivity.getInstance().getSupportFragmentManager(), "TableViewDialog");
	}

	private void doTakeAwayClick() {
		Cart.INSTANCE.switchTakeAwayMode();
		updateTakeAwayColour();
	}

	private void doSplitOrderClick() {
		Cart.INSTANCE.doSplitOrderClick();
		hideDropdownMenu();
	}

	private void doPrelimPrintOnClick() {
		try {
			if (Cart.INSTANCE.getOrder() != null && CasioPrint.hasPrinterConnected()) {
				CasioPrintOrder printOrder = new CasioPrintOrder();
				printOrder.print(Cart.INSTANCE.getOrder(), ReceiptPrintType.PRELIM);
			}
			hideDropdownMenu();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void doNoteClick() {
		OrderAddNoteDialog oadDialog = new OrderAddNoteDialog();
		if (Cart.INSTANCE.getOrder() != null) {
			oadDialog.show(MainActivity.getInstance().getSupportFragmentManager(), "addordernote");
		}
		hideDropdownMenu();
	}

	private void doDiscountClick() {
		OrderDiscountDialog odDialog = new OrderDiscountDialog();
		if (Cart.INSTANCE.getOrder() != null) {
			odDialog.show(MainActivity.getInstance().getSupportFragmentManager(), "discountdialog");
		}
		hideDropdownMenu();
	}

	private void hideDropdownMenu() {
		if (popupWindow != null) {
			popupWindow.dismiss();
		}
	}

	//VISUALS ==========================================================================================================
	public void togglePayView() {
		if (!Cart.INSTANCE.hasZeroPriceLines()) {
			MainTopBarMenu.getInstance().untoggleViews();
			if (Cart.INSTANCE.getOrder().hasLines()) {
				MainActivity.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.item_detail_container, MainActivity.getInstance().getNumpadPayFragment(), "Pay").commit();
			}
		} else {
			Toast.makeText(MainActivity.getInstance(), MainActivity.getInstance().getText(R.string.cannot_have_zero_price_orderlines), Toast.LENGTH_LONG).show();
		}
	}

	public void setParkSuccessfulMessage() {
		Toast.makeText(MainActivity.getInstance(), MainActivity.getInstance().getString(R.string.park_successful), Toast.LENGTH_SHORT).show();
	}

	public void resetParkSuccessful() {
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				refreshCartButtonStates();
			}
		}, 2000);
	}

	public void refreshCartButtonStates() {
		if (MainActivity.getInstance().getNumpadPayFragment() != null && MainActivity.getInstance().getNumpadPayFragment().hasPayments()) {
			disableAllActions();
		} else {
			setDeleteOrderButtonState();
			setParkOrderButtonState();
			setDeleteLineButtonState();
			setLoadOrderButtonState();
			setNoteButtonState();
			setDiscountButtonState();
			if (AppConfig.getState().isRestaurant()) {
				setAddOrderButtonState();
				setTableButtonState();
				setTakeAwayButtonState();
				setSplitOrderButtonState();
				setPrelimPrintButtonState();
				updateMoreButtonState();
			} else {
				if (Cart.INSTANCE.hasActiveOrder() && Cart.INSTANCE.getOrder().hasLines()) {
					enableView(rlExchangeCart);
				} else {
					disableView(rlExchangeCart);
				}
			}
		}
	}

	private void updateMoreButtonState() {
		try {
			if (!isRestaurant) {
				removeView(rlMoreButton);
			} else if (!rlSplitOrder.isEnabled() && !rlPrelimReceipt.isEnabled() && !rlNoteButton.isEnabled()) {
				disableView(rlMoreButton);
			} else if (!rlMoreButton.isEnabled() && isRestaurant) {
				enableView(rlMoreButton);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void setDeleteOrderButtonState() {
		if ((Cart.INSTANCE.hasActiveOrder() && Cart.INSTANCE.getOrder().hasLines()) || Cart.INSTANCE.getOrders().size() > 1) {
			enableView(rlDeleteOrder);
		} else {
			disableView(rlDeleteOrder);
		}
	}

	private void setParkOrderButtonState() {
		if ((Cart.INSTANCE.hasActiveOrder() && Cart.INSTANCE.getOrder().hasLines()) || Cart.INSTANCE.hasOrdersWithLines()) {
			rlParkOrder.setVisibility(View.VISIBLE);
			if (AppConfig.getState().isWorkshop() && !MainActivity.getInstance().isConnected()) {
				disableView(rlParkOrder);
			} else {
				enableView(rlParkOrder);
			}
		} else {
			rlParkOrder.setVisibility(View.GONE);
			disableView(rlParkOrder);
		}
	}

	private void setDeleteLineButtonState() {
		if ((Cart.INSTANCE.hasActiveOrder() && Cart.INSTANCE.getOrder().hasLines()) && Cart.INSTANCE.hasSelectedLine()) {
			enableView(rlDeleteLine);
		} else {
			disableView(rlDeleteLine);
		}
	}

	private void setNoteButtonState() {
		if (Cart.INSTANCE.hasActiveOrder() && Cart.INSTANCE.getOrder().hasLines()) {
			enableView(rlNoteButton);
		} else {
			disableView(rlNoteButton);
		}
	}

	private void setAddOrderButtonState() {
		if (isRestaurant) {
			enableView(rlAddOrder);
		} else {
			removeView(rlAddOrder);
		}
	}

	private void setTableButtonState() {
		if (isRestaurant) {
			enableView(rlTableButton);
		} else {
			removeView(rlTableButton);
		}
	}

	private void setTakeAwayButtonState() {
		if (isRestaurant) {
			enableView(rlTakeAwayButton);
			updateTakeAwayColour();
		} else {
			removeView(rlTakeAwayButton);
		}
	}

	private void setSplitOrderButtonState() {
		if (isRestaurant) {
			if (Cart.INSTANCE.hasOrdersWithLines()) {
				enableView(rlSplitOrder);
			} else {
				disableView(rlSplitOrder);
			}
		} else {
			removeView(rlSplitOrder);
		}
	}

	private void setPrelimPrintButtonState() {
		if (isRestaurant) {
			if (Cart.INSTANCE.hasOrdersWithLines()) {
				enableView(rlPrelimReceipt);
			} else {
				disableView(rlPrelimReceipt);
			}
		} else {
			removeView(rlPrelimReceipt);
		}
	}

	private void setLoadOrderButtonState() {
		if ((Cart.INSTANCE.hasActiveOrder() && Cart.INSTANCE.getOrder().hasLines()) || Cart.INSTANCE.hasOrdersWithLines()) {
			rlLoadOrder.setVisibility(View.GONE);
		} else {
			rlLoadOrder.setVisibility(View.VISIBLE);
		}
		if (AppConfig.getState().isWorkshop() && !MainActivity.getInstance().isConnected()) {
			disableView(rlLoadOrder);
		} else {
			enableView(rlLoadOrder);
		}
	}

	private void setDiscountButtonState() {
		if (Cart.INSTANCE.hasActiveOrder() && Cart.INSTANCE.getOrder().hasLines()) {
			enableView(rlDiscountOrder);
		} else {
			disableView(rlDiscountOrder);
		}
	}

	//==========================================================================================

	private void removeView(RelativeLayout view) {
		try {
			if (view != null) {
				view.setVisibility(View.GONE);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void disableView(RelativeLayout rl) {
		try {
			if (rl != null) {
				rl.setEnabled(false);
				rl.setClickable(false);
				for (int i = 0; i < rl.getChildCount(); i++) {
					if (rl.getChildAt(i).getClass().equals(ImageView.class)) {
						rl.getChildAt(i).setAlpha(disabledAlpha);
					} else if (rl.getChildAt(i).getClass().equals(AppCompatTextView.class)) {
						((TextView) rl.getChildAt(i)).setTextColor(MainActivity.getInstance().getResources().getColor(R.color.disabledViewGray));
					}
				}
				rl.setBackgroundColor(MainActivity.getInstance().getResources().getColor(R.color.eee));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void enableView(RelativeLayout rl) {
		try {
			if (rl != null) {
				rl.setEnabled(true);
				rl.setClickable(true);
				for (int i = 0; i < rl.getChildCount(); i++) {
					if (rl.getChildAt(i).getClass().equals(ImageView.class)) {
						rl.getChildAt(i).setAlpha(1f);
						//TODO
						//((ImageView)rl.getChildAt(i)).setColorFilter(Color.parseColor("#FFFFFFFF"), PorterDuff.Mode.SRC_IN);
					} else if (rl.getChildAt(i).getClass().equals(AppCompatTextView.class)) {
						((TextView) rl.getChildAt(i)).setTextColor(MainActivity.getInstance().getResources().getColor(android.R.color.black));
						//TODO
						//((TextView) rl.getChildAt(i)).setTextColor(MainActivity.getInstance().getResources().getColor(android.R.color.white));
					}
				}
				rl.setBackground(MainActivity.getInstance().getResources().getDrawable(R.drawable.edit_panel_borders));
				//TODO
				//rl.setBackgroundColor(MainActivity.getInstance().getResources().getColor(R.color.nav));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void disableAllActions() {
		disableView(rlParkOrder);
		disableView(rlDeleteOrder);
		disableView(rlLoadOrder);
		if (isRestaurant) {
			disableView(rlAddOrder);
			disableView(rlTableButton);
			disableView(rlSplitOrder);
			disableView(rlTakeAwayButton);
			updateMoreButtonState();
		}
	}

	public void enableAllActions() {
		enableView(rlParkOrder);
		enableView(rlDeleteOrder);
		enableView(rlLoadOrder);
		if (isRestaurant) {
			enableView(rlAddOrder);
			enableView(rlTableButton);
			enableView(rlSplitOrder);
			enableView(rlMoreButton);
		}
	}

	public void updateNoteColour() {
		if (Cart.INSTANCE.getOrder() != null && Cart.INSTANCE.getOrder().hasNote()) {
			rlNoteButton.setBackground(MainActivity.getInstance().getResources().getDrawable(R.drawable.light_orange_edit_panel_borders));
		} else {
			if (Cart.INSTANCE.getOrder() != null) {
				rlNoteButton.setBackground(MainActivity.getInstance().getResources().getDrawable(R.drawable.edit_panel_borders));
			}
		}
	}

	public void updateTakeAwayColour() {
		if (Cart.INSTANCE.getOrder() != null && Cart.INSTANCE.getOrder().isUseAlternative()) {
			rlTakeAwayButton.setBackground(MainActivity.getInstance().getResources().getDrawable(R.drawable.light_orange_edit_panel_borders));
		} else {
			if (Cart.INSTANCE.getOrder() != null) {
				rlTakeAwayButton.setBackground(MainActivity.getInstance().getResources().getDrawable(R.drawable.edit_panel_borders));
			}
		}
	}
}
