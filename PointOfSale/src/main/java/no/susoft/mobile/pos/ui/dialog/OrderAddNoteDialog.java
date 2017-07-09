package no.susoft.mobile.pos.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

public class OrderAddNoteDialog extends DialogFragment {

    EditText noteText;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(900, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View view = (inflater.inflate(R.layout.order_add_note_dialog, null));
        builder.setView(view).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                hideKeyboard();
                dismiss();
            }
        }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Cart.INSTANCE.getOrder().setNote(noteText.getText().toString().trim());
                MainActivity.getInstance().getCartFragment().getCartButtons().updateNoteColour();
                hideKeyboard();
                dismiss();
            }
        }).setNeutralButton(R.string.clear, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Cart.INSTANCE.getOrder().setNote("");
                MainActivity.getInstance().getCartFragment().getCartButtons().updateNoteColour();
            }
        });

        noteText = (EditText) view.findViewById(R.id.noteText);
		if (Cart.INSTANCE.getOrder().getNote() != null && Cart.INSTANCE.getOrder().getNote().length() > 0) {
			noteText.setText(Cart.INSTANCE.getOrder().getNote());
		} else if (AccountManager.INSTANCE.getSavedOrderNote() != null && AccountManager.INSTANCE.getSavedOrderNote().trim().length() > 0) {
			noteText.setText(AccountManager.INSTANCE.getSavedOrderNote());
		}
        noteText.setSelection(noteText.getText().toString().length());

        return builder.create();
    }

    protected void hideKeyboard() {
        try {
            View currentView = this.getDialog().getCurrentFocus();
            if (currentView != null) {
                InputMethodManager imm = (InputMethodManager) MainActivity.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(currentView.getWindowToken(), 0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
