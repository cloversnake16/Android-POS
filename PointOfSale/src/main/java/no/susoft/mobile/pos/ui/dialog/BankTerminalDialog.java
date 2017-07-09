package no.susoft.mobile.pos.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.hardware.terminal.CardTerminal;
import no.susoft.mobile.pos.hardware.terminal.CardTerminalFactory;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class BankTerminalDialog extends DialogFragment {

	private Decimal cardPaymentAmount;
	TextView amountTextView;
	TextView textView;
	Button cancelButton;
	String content = "";

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		final View view = (inflater.inflate(R.layout.bank_terminal_dialog, null));

		builder.setView(view);

		Dialog dialog = builder.create();

		textView = (TextView) view.findViewById(R.id.bank_terminal_text_view);
		textView.setText(content);
		cancelButton = (Button) view.findViewById(R.id.bank_terminal_cancel_button);
		amountTextView = (TextView) view.findViewById(R.id.bank_terminal_amount);

		return dialog;
	}

	@Override
	public void onStart() {
		super.onStart();

		try {
			getDialog().getWindow().setLayout(500, 400);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		cancelButton.setOnClickListener(new View.OnClickListener() {

			private BankTerminalDialog parent;

			public View.OnClickListener setParent(BankTerminalDialog parent) {
				this.parent = parent;
				return this;
			}

			@Override
			public void onClick(View v) {
				CardTerminal cardTerminal = null;

				try {
					cardTerminal = CardTerminalFactory.getInstance().getCardTerminal();
					if (cardTerminal.isInBankMode())
						cardTerminal.cancel("0000");
				} catch (Exception e) {
					ErrorReporter.INSTANCE.filelog("appendLine", "cancel error", e);
					e.printStackTrace();
				}

				setContent("");
				parent.dismissAllowingStateLoss();
			}

		}.setParent(this));

		getDialog().setCancelable(false);
		getDialog().setCanceledOnTouchOutside(false);

		textView.setMovementMethod(new ScrollingMovementMethod());

		if (cardPaymentAmount != null) {
			setAmount(cardPaymentAmount.toString());
		}
	}

	public TextView getTextView() {
		return textView;
	}

	public void appendLine(String line) {
		content += line + "\n";
		MainActivity.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {

				try {
					if (textView != null) {
						textView.setText(content.toUpperCase());
					}
				} catch (Exception e) {
					e.printStackTrace();
					ErrorReporter.INSTANCE.filelog("appendLine", "error", e);
				}
			}
		});
	}

	public void setAmount(String amount) {
		if (amount != null) {
			amountTextView.setText(new StringBuilder().append(getString(R.string.amount)).append(": ").append(amount).toString());
		}
	}

	//    @Override
	//    public void onSaveInstanceState(Bundle outState) {
	//No call for super(). Bug on API Level > 11.
	//this is a know bug in the support package
	//    }

	public void setPaymentAmount(final Decimal amount) {
		MainActivity.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				cardPaymentAmount = amount;
				if (cardPaymentAmount != null) {
					setAmount(cardPaymentAmount.toString());
				} else {
					setAmount("");
				}
			}
		});
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
