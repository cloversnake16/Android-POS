package no.susoft.mobile.pos.ui.dialog;

import android.app.Dialog;
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
import android.widget.EditText;
import android.widget.Toast;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.data.Product;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

public class NewGiftCardDialog extends DialogFragment {

    EditText amountText;
    EditText qtyText;
    AlertDialog dialog;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View view = (inflater.inflate(R.layout.new_gift_card_dialog, null));

        builder.setView(view).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                getDialog().dismiss();
            }
        }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onOkClick();
            }
        });

        dialog = builder.create();
        dialog.setTitle(getString(R.string.new_gift_card));

        return dialog;
    }


    private void onOkClick() {
        try {
            int qty = Integer.parseInt(qtyText.getText().toString());
            Cart.INSTANCE.addOrderLineGiftCards(getGiftCard(), qty);
        } catch (Exception ex) {
            Toast.makeText(MainActivity.getInstance(), R.string.error_adding_giftcard, Toast.LENGTH_LONG).show();
        }
        this.dismiss();
    }

    private Product getGiftCard() {
        Product giftCard = new Product();
        giftCard.setId("SU_GIFTCARD");
        giftCard.setName(getString(R.string.gift_card));
        giftCard.setPrice(Decimal.make(amountText.getText().toString()));
        return giftCard;
    }

    @Override
    public void onStart() {
        super.onStart();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

        int width = 200;
        int height = 300;
        getDialog().getWindow().setLayout(width, height);
        qtyText = (EditText) getDialog().findViewById(R.id.giftCardQtyText);
        qtyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amountText.selectAll();
            }
        });
        qtyText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(inputEntriesAreValid());
            }
        });
        amountText = (EditText) getDialog().findViewById(R.id.giftCardAmountText);
        amountText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amountText.selectAll();
            }
        });
        amountText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(inputEntriesAreValid());
            }
        });
    }

    private boolean inputEntriesAreValid() {
        try {
            return (Decimal.make(amountText.getText().toString()) != Decimal.ZERO && Integer.parseInt(qtyText.getText().toString()) > 0);
        } catch (NumberFormatException nfex) {
            Log.i("vilde", "invalid numbers");
            return false;
        } catch (Exception ex) {
            return false;
        }
    }
}
