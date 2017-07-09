package no.susoft.mobile.pos.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.data.Discount;
import no.susoft.mobile.pos.data.DiscountReason;
import no.susoft.mobile.pos.data.OrderLine;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.adapter.DiscountReasonSpinnerAdapter;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

public class OrderDiscountDialog extends DialogFragment {
	
	RadioGroup radioGroup;
	private RadioButton percentRB;
	private RadioButton amountRB;
	private RadioButton totalRB;
	private TextView original_total;
	private Decimal orderTotal = Decimal.ZERO;
	private EditText percent;
	private EditText amount;
	private EditText total;
	private Spinner discountReasonSpinner;
	private final String roundingId = "round";
	
	public static Decimal enteredPercent = null;
	
	private enum DISCOUNT_TYPE {
		PERCENT, AMOUNT, TOTAL
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		keyboard = (InputMethodManager) MainActivity.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
	}
	
	@Override
	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		final View view = (inflater.inflate(R.layout.order_discount_dialog, null));
		
		builder.setView(view).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				applyDiscounts();
				saveEnteredPercent();
				dismiss();
			}
		}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismiss();
			}
		});
		
		Dialog dialog = builder.create();
		
		percentRB = (RadioButton) view.findViewById(R.id.order_discount_percent);
		amountRB = (RadioButton) view.findViewById(R.id.order_discount_amount);
		totalRB = (RadioButton) view.findViewById(R.id.order_discount_total);
		original_total = (TextView) view.findViewById(R.id.original_order_total);
		percent = (EditText) view.findViewById(R.id.percent_input);
		amount = (EditText) view.findViewById(R.id.amount_input);
		total = (EditText) view.findViewById(R.id.total_input);
		discountReasonSpinner = (Spinner) view.findViewById(R.id.order_discount_reason_spinner);
		
		DiscountReasonSpinnerAdapter discountReasonAdapter = new DiscountReasonSpinnerAdapter(MainActivity.getInstance(), 0, MainActivity.getInstance().getDiscountReasons());
		discountReasonAdapter.setDropDownViewResource(R.layout.discount_reason_spinner);
		discountReasonSpinner.setAdapter(discountReasonAdapter);
		
		setRadioGroupOnChangeListener();
		setPercentChangeListener(false);
		setAmountChangeListener(false);
		setTotalChangeListener(false);
		percentRB.toggle();
		
		orderTotal = Cart.INSTANCE.getOrder().getAmountWithoutRoundingLine(false);
		original_total.setText(getString(R.string.original_total) + ": " + orderTotal.toString());
		if (enteredPercent != null) {
			percent.setText(enteredPercent.toString());
			percent.requestFocusFromTouch();
			percent.selectAll();
		}
		
		//        updatePercentDiscount();
		return dialog;
	}
	
	private void saveEnteredPercent() {
		try {
			enteredPercent = Decimal.make(percent.getText().toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void applyDiscounts() {
		try {
			DiscountReason reason = (DiscountReason) discountReasonSpinner.getSelectedItem();
			Discount discount = new Discount(Decimal.make(percent.getText().toString().trim()), reason);
			if (!Decimal.make(percent.getText().toString()).isZero()) {
				for (OrderLine ol : Cart.INSTANCE.getOrder().getLines()) {
					if (!ol.getProduct().getId().equalsIgnoreCase(roundingId)) {
						ol.setDiscount(discount);
						if (ol.getComponents() != null) {
							for (OrderLine line : ol.getComponents()) {
								line.setDiscount(discount);
							}
						}
					}
				}
				correctTotalOrderAmount();
			} else {
				resetDiscount();
			}
			
			MainActivity.getInstance().getCartFragment().refreshCart();
			MainActivity.getInstance().getCartFragment().refreshFragmentView();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void resetDiscount() {
		for (OrderLine ol : Cart.INSTANCE.getOrder().getLines()) {
			ol.setDiscount(null);
			if (ol.getComponents() != null) {
				for (OrderLine line : ol.getComponents()) {
					line.setDiscount(null);
				}
			}
		}
		OrderLine ol = Cart.INSTANCE.getOrder().getLineWithProductId(roundingId);
		if (ol != null) {
			int line = Cart.INSTANCE.getOrder().getLines().indexOf(ol);
			Cart.INSTANCE.getOrder().removeOrderLine(ol);
			MainActivity.getInstance().getCartFragment()._orderLineRemoved(line);
		}
	}
	
	private void correctTotalOrderAmount() {
		try {
			if (!Cart.INSTANCE.getOrder().getAmountWithoutRoundingLine(true).isEqual(Decimal.make(total.getText().toString()))) {
				for (OrderLine ol : Cart.INSTANCE.getOrder().getLines()) {
					if (ol.getProduct().getId().equalsIgnoreCase(roundingId)) {
						
						Decimal price = Decimal.make(total.getText().toString()).subtract(Cart.INSTANCE.getOrder().getAmountWithoutRoundingLine(true));
						if (price.isNegative()) {
							ol.setPrice(price.multiply(Decimal.NEGATIVE_ONE));
							ol.setQuantity(Decimal.NEGATIVE_ONE);
						} else {
							ol.setPrice(Decimal.make(total.getText().toString()).subtract(Cart.INSTANCE.getOrder().getAmountWithoutRoundingLine(true)));
							ol.setQuantity(Decimal.ONE);
						}
						
						MainActivity.getInstance().getCartFragment().refreshCart();
						return;
					}
				}
				Log.i("vilde", "Total: " + Decimal.make(total.getText().toString()).subtract(Cart.INSTANCE.getOrder().getAmountWithoutRoundingLine(true)).toString());
				MainActivity.getInstance().getServerCallMethods().loadProductByID(roundingId, Decimal.make(total.getText().toString()).subtract(Cart.INSTANCE.getOrder().getAmountWithoutRoundingLine(true)));
				
			} else if (Cart.INSTANCE.getOrder().getAmountWithoutRoundingLine(true).isEqual(Decimal.make(total.getText().toString()))) {
				for (OrderLine ol : Cart.INSTANCE.getOrder().getLines()) {
					if (ol.getProduct().getId().equalsIgnoreCase(roundingId)) {
						int line = Cart.INSTANCE.getOrder().getLines().indexOf(ol);
						Cart.INSTANCE.getOrder().removeOrderLine(ol);
						MainActivity.getInstance().getCartFragment()._orderLineRemoved(line);
						return;
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	InputMethodManager keyboard;
	
	private void setRadioGroupOnChangeListener() {
		percentRB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					setToggled(DISCOUNT_TYPE.PERCENT, true);
					amountRB.setChecked(false);
					totalRB.setChecked(false);
				}
				
			}
		});
		
		amountRB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					setToggled(DISCOUNT_TYPE.AMOUNT, true);
					percentRB.setChecked(false);
					totalRB.setChecked(false);
				}
			}
		});
		
		totalRB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					setToggled(DISCOUNT_TYPE.TOTAL, true);
					percentRB.setChecked(false);
					amountRB.setChecked(false);
				}
			}
		});
	}
	
	private void setToggled(DISCOUNT_TYPE type, boolean enabled) {
		switch (type) {
			case PERCENT: {
				percent.setEnabled(enabled);
				setPercentChangeListener(enabled);
				if (enabled) {
					setToggled(DISCOUNT_TYPE.AMOUNT, false);
					setToggled(DISCOUNT_TYPE.TOTAL, false);
					keyboard.showSoftInput(percent, 0);
					setSelectionToEnd(percent);
					selectAll(percent);
				}
				break;
			}
			case AMOUNT: {
				amount.setEnabled(enabled);
				Log.i("vilde", "Amount enabled: " + amount.isEnabled());
				setAmountChangeListener(enabled);
				if (enabled) {
					setToggled(DISCOUNT_TYPE.PERCENT, false);
					setToggled(DISCOUNT_TYPE.TOTAL, false);
					keyboard.showSoftInput(amount, 0);
					setSelectionToEnd(amount);
					selectAll(amount);
				}
				break;
			}
			case TOTAL: {
				total.setEnabled(enabled);
				setTotalChangeListener(enabled);
				if (enabled) {
					setToggled(DISCOUNT_TYPE.AMOUNT, false);
					setToggled(DISCOUNT_TYPE.PERCENT, false);
					keyboard.showSoftInput(total, 0);
					setSelectionToEnd(total);
					selectAll(total);
				}
				break;
			}
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		try {
			getDialog().getWindow().setLayout(600, WindowManager.LayoutParams.WRAP_CONTENT);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		getDialog().setCancelable(false);
		getDialog().setCanceledOnTouchOutside(false);
		
	}
	
	private TextWatcher percentWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
		}
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			try {
				if (Decimal.make(percent.getText().toString()).isGreater(Decimal.HUNDRED)) {
					percent.setText(Decimal.HUNDRED.toString());
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			setSelectionToEnd(percent);
			updatePercentDiscount();
		}
	};
	
	public void setPercentChangeListener(boolean enabled) {
		if (enabled) {
			percent.addTextChangedListener(percentWatcher);
		} else {
			percent.removeTextChangedListener(percentWatcher);
		}
	}
	
	TextWatcher amountWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
		}
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			try {
				if (Decimal.make(amount.getText().toString()).isGreater(orderTotal)) {
					amount.setText(orderTotal.toString());
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			setSelectionToEnd(amount);
			updateAmountDiscount();
		}
	};
	
	public void setAmountChangeListener(boolean enabled) {
		if (enabled) {
			amount.addTextChangedListener(amountWatcher);
		} else {
			amount.removeTextChangedListener(amountWatcher);
		}
	}
	
	TextWatcher totalWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
		}
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			try {
				if (Decimal.make(total.getText().toString()).isGreater(orderTotal)) {
					total.setText(orderTotal.toString());
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			setSelectionToEnd(total);
			updateTotalDiscount();
		}
	};
	
	public void setTotalChangeListener(boolean enabled) {
		if (enabled) {
			total.addTextChangedListener(totalWatcher);
		} else {
			total.removeTextChangedListener(totalWatcher);
		}
	}
	
	private void updatePercentDiscount() {
		try {
			Decimal discountAmount = orderTotal.multiply(Decimal.make(percent.getText().toString()));
			discountAmount = discountAmount.divide(Decimal.HUNDRED);
			amount.setText(discountAmount.toString());
			total.setText(orderTotal.subtract(discountAmount).toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void updateAmountDiscount() {
		try {
			Decimal amountDiscount = Decimal.make(amount.getText().toString());
			total.setText(orderTotal.subtract(amountDiscount).toString());
			percent.setText(amountDiscount.multiply(Decimal.HUNDRED).divide(orderTotal).toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void updateTotalDiscount() {
		try {
			Decimal totalDiscount = Decimal.make(total.getText().toString());
			amount.setText(orderTotal.subtract(totalDiscount).toString());
			percent.setText(orderTotal.subtract(totalDiscount).multiply(Decimal.HUNDRED).divide(orderTotal).toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void setSelectionToEnd(EditText view) {
		view.setSelection(view.getText().toString().length());
	}
	
	private void selectAll(EditText view) {
		view.selectAll();
	}
}
