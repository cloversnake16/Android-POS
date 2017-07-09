package no.susoft.mobile.pos.ui.utils;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class SimpleInfoAlertDialog {

    private AlertDialog alertDialog;
    public AlertDialog getAlertDialog() {
        return alertDialog;
    }

    public SimpleInfoAlertDialog(String message) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.getInstance());
        alertDialogBuilder.setMessage(message).setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        // create alert dialog
        alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }
}
