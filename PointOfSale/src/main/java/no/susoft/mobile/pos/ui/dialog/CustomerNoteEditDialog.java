package no.susoft.mobile.pos.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.CustomerNote;
import no.susoft.mobile.pos.server.CustomerNoteSaveAsync;

public class CustomerNoteEditDialog extends DialogFragment {

	private String customerId;
	private CustomerNote note;
	private EditText titleField;
	private EditText noteField;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();

		final View view = (inflater.inflate(R.layout.customer_note_edit_dialog, null));
		builder.setView(view).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				getDialog().dismiss();
			}
		});
		builder.setView(view).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				if (note == null) {
					note = new CustomerNote();
				}
				note.setTitle(titleField.getText().toString().trim());
				note.setText(noteField.getText().toString().trim());
				note.setCustomerId(customerId);

				new CustomerNoteSaveAsync().execute(note);
			}
		});

		titleField = (EditText) view.findViewById(R.id.new_customer_note_title);
		noteField = (EditText) view.findViewById(R.id.new_customer_note_text);

		if (note != null) {
			titleField.setText(note.getTitle());
			noteField.setText(note.getText());
		}

		return builder.create();
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public void setNote(CustomerNote note) {
		this.note = note;
	}
}
