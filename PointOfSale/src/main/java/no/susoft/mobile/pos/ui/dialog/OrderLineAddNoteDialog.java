package no.susoft.mobile.pos.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.OrderLine;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class OrderLineAddNoteDialog extends DialogFragment {

    EditText noteText;
    OrderLine orderLine;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(700, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View view = (inflater.inflate(R.layout.order_line_add_note_dialog, null));
        builder.setView(view).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                hideKeyboard();
            }
        }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                orderLine.setNote(noteText.getText().toString().trim());
                MainActivity.getInstance().getCartFragment().refreshCart();
                MainActivity.getInstance().getNumpadEditFragment().updateOptionLineButtons();
                hideKeyboard();
                dismiss();
            }
        }).setNeutralButton(R.string.clear, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                orderLine.setNote("");
                MainActivity.getInstance().getCartFragment().refreshCart();
                MainActivity.getInstance().getNumpadEditFragment().updateOptionLineButtons();
            }
        });



        noteText = (EditText) view.findViewById(R.id.noteText);
        if(orderLine.hasNote()) {
            noteText.setText(orderLine.getNote());
            noteText.setSelection(noteText.getText().toString().length());
        }


        return builder.create();
    }

    public void setOrderLine(OrderLine orderLine) {
        this.orderLine = orderLine;
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
