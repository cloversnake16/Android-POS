package no.susoft.mobile.pos.ui.dialog;

import java.util.Date;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.data.Prepaid;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class PayGiftCardDialog extends DialogFragment {

    String giftCardNumber;
    boolean valid;
    EditText amountToUseText;
    TextView fullAmount;
    TextView id;
    AlertDialog dialog;
    TextView shopIssued;
    TextView issuedBy;
    TextView issueDate;
    private Prepaid prepaid;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = (inflater.inflate(R.layout.pay_gift_card_dialog, null));
        builder.setView(view).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try{ MainActivity.getInstance().getNumpadPayFragment().focusOnInputField();} catch (Exception ex) {
                    ex.printStackTrace();
                }
                getDialog().dismiss();
            }
        }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onOkClick();
            }
        });

        TextView title = new TextView(getActivity());
        title.setText(getString(R.string.use_gift_card));
        title.setPadding(10, 20, 10, 10);
        title.setGravity(Gravity.CENTER); // this is required to bring it to center.
        title.setTextSize(20);
        title.setTextColor(Color.BLACK);

        dialog = builder.create();
        dialog.setCustomTitle(title);
        //dialog.setTitle(getString(R.string.use_gift_card));

        return dialog;
    }

    private void onOkClick() {
        MainActivity.getInstance().getNumpadPayFragment().addGiftCardPayment(String.valueOf(prepaid.getNumber()), Decimal.make(amountToUseText.getText().toString()));
        this.dismiss();
    }

    @Override
    public void onStart() {
        super.onStart();

        try {
            getDialog().getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setGravity(Gravity.CENTER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if(prepaid == null) {
            Toast.makeText(MainActivity.getInstance(), R.string.set_prepaid_before_showing, Toast.LENGTH_SHORT).show();
            Log.i("vilde", "Set prepaid object before showing dialog");
            dismiss();
        }

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

        //need to set these        getDialog().getWindow().
        id = (TextView) getDialog().findViewById(R.id.giftCardIdText);
        issuedBy = (TextView) getDialog().findViewById(R.id.etIssuedBySeller);
        issueDate = (TextView) getDialog().findViewById(R.id.tvIssueDate);
        shopIssued = (TextView) getDialog().findViewById(R.id.etShopIssued);
        fullAmount = (TextView) getDialog().findViewById(R.id.giftCardFullAmountText);
        id = (TextView) getDialog().findViewById(R.id.giftCardIdText);

        amountToUseText = (EditText) getDialog().findViewById(R.id.giftCardAmountText);
        amountToUseText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                amountToUseText.selectAll();
                return false;
            }
        });
        amountToUseText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (Decimal.make(amountToUseText.getText().toString()).isGreater(Decimal.make(fullAmount.getText().toString()))) {
                        if(MainActivity.getInstance().getNumpadPayFragment().getRemainingPositiveAmountToPay().isLess(prepaid.getAmount())
                                || MainActivity.getInstance().getNumpadPayFragment().getRemainingPositiveAmountToPay().isEqual(prepaid.getAmount())) {
                            Log.i("vilde", "set to remaining amount ");
                            amountToUseText.setText(MainActivity.getInstance().getNumpadPayFragment().getRemainingPositiveAmountToPay().toString());
                        } else {
                            Log.i("vilde", "set to full amount ");
                            amountToUseText.setText(fullAmount.getText());
                        }
                        Toast.makeText(MainActivity.getInstance(), R.string.cannot_enter_higher_number_than_full_amount, Toast.LENGTH_SHORT).show();
                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                    } else {
                        if(!prepaid.getAmount().isZero())
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(inputEntriesAreValid());
                    }

                    amountToUseText.setSelection(amountToUseText.length());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        populateFields();
        amountToUseText.setSelection(amountToUseText.length());
    }

    private void populateFields() {
        setIssuedShop(prepaid.getShopId());
        setGiftCardNumber(prepaid.getNumber());
        setIssuingSeller(prepaid.getSalespersonId());
        setIssuedDate(prepaid.getIssuedDate());
        setFullAmount(prepaid.getAmount());
        id.setText(String.valueOf(prepaid.getId()));

        if(MainActivity.getInstance().getNumpadPayFragment().getRemainingPositiveAmountToPay().isLess(prepaid.getAmount())) {
            amountToUseText.setText(MainActivity.getInstance().getNumpadPayFragment().getRemainingPositiveAmountToPay().toString());
        } else if(MainActivity.getInstance().getNumpadPayFragment().getRemainingPositiveAmountToPay().isEqual(prepaid.getAmount())) {
            amountToUseText.setText(prepaid.getAmount().toString());
        } else if (MainActivity.getInstance().getNumpadPayFragment().getRemainingPositiveAmountToPay().isGreater(prepaid.getAmount())) {
            amountToUseText.setText(prepaid.getAmount().toString());
        }

    }

    private boolean inputEntriesAreValid() {
        try {
            return (Decimal.make(amountToUseText.getText().toString()) != Decimal.ZERO);
        } catch (NumberFormatException nfex) {
            Log.i("vilde", "invalid numbers");
            return false;
        } catch (Exception ex) {
            return false;
        }
    }

    public String getGiftCardNumber() {
        return giftCardNumber;
    }

    public void setGiftCardNumber(String giftCardNumber) {
        this.id.setText(giftCardNumber);
    }

    public void setFullAmount(Decimal amount) {
        this.fullAmount.setText(amount.toString());
    }

    public void setPrepaid(Prepaid prepaid) {
        this.prepaid = prepaid;
    }

    public void setIssuedShop(String issuedShop) {
        this.shopIssued.setText(issuedShop);
    }

    public void setIssuingSeller(String issuingSeller) {
        this.issuedBy.setText(issuingSeller);
    }

    public void setIssuedDate(Date issuedDate) {
        this.issueDate.setText(issuedDate.toString());
    }
}
