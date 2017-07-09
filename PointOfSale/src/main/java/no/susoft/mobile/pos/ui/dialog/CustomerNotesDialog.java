package no.susoft.mobile.pos.ui.dialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

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
import android.view.ViewGroup;
import android.widget.*;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.CustomerNote;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

public class CustomerNotesDialog extends DialogFragment {

	private ProgressBar progressBar;
	private LinearLayout header;
	private ListView notesList;
	private LinearLayout textHolder;
	private EditText noteField;
	private TextView notFound;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();

		final View view = (inflater.inflate(R.layout.customer_notes_dialog, null));
		builder.setView(view).setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User cancelled the dialog
			}
		});
		builder.setView(view).setNegativeButton(R.string.edit, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			}
		});

		if (MainActivity.getInstance().isConnected()) {
			builder.setNeutralButton(R.string.new_note, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					CustomerNoteEditDialog dialog2 = new CustomerNoteEditDialog();
					dialog2.setCustomerId(Cart.INSTANCE.getOrder().getCustomer().getId());
					dialog2.show(MainActivity.getInstance().getSupportFragmentManager(), "addnote");
				}
			});
		}

		header = (LinearLayout) view.findViewById(R.id.note_list_header);
		notesList = (ListView) view.findViewById(R.id.customer_notes_list);
		notFound = (TextView) view.findViewById(R.id.customer_notes_empty);
		textHolder = (LinearLayout) view.findViewById(R.id.customer_notes_text_holder);
		noteField = (EditText) view.findViewById(R.id.customer_note_text);
		progressBar = (ProgressBar) view.findViewById(R.id.progressBarCustomerNotes);
		progressBar.setVisibility(View.VISIBLE);
		noteField.setKeyListener(null);
		MainActivity.getInstance().getServerCallMethods().loadCustomerNotes(this);

		return builder.create();
	}

	@Override
	public void onStart() {
		super.onStart();
		Button saveButton = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE);
		if (saveButton != null) {
			saveButton.setVisibility(View.GONE);
		}
	}

	public void setupAdapter(ArrayList<CustomerNote> notes) {
		CustomerNotesAdapter adapter = new CustomerNotesAdapter(MainActivity.getInstance(), 0, notes);
        notesList.setAdapter(adapter);
		if (notes.size() > 0) {
			header.setVisibility(View.VISIBLE);
			notFound.setVisibility(View.GONE);
		} else {
			header.setVisibility(View.GONE);
			notFound.setVisibility(View.VISIBLE);
		}

		final CustomerNotesDialog dialog = this;
        notesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final CustomerNote note = (CustomerNote) adapterView.getItemAtPosition(i);
                if (note != null) {
					textHolder.setVisibility(View.VISIBLE);
					noteField.setText(note.getText());

					Button saveButton = ((AlertDialog) dialog.getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE);
					saveButton.setVisibility(View.VISIBLE);
					((AlertDialog) dialog.getDialog()).setButton(AlertDialog.BUTTON_NEGATIVE, MainActivity.getInstance().getResources().getText(R.string.edit), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							CustomerNoteEditDialog dialog2 = new CustomerNoteEditDialog();
							dialog2.setCustomerId(Cart.INSTANCE.getOrder().getCustomer().getId());
							dialog2.setNote(note);
							dialog2.show(MainActivity.getInstance().getSupportFragmentManager(), "addnote");
						}
					});
                }
            }
        });
	}

	public ProgressBar getProgressBar() {
		return progressBar;
	}

	public class CustomerNotesAdapter extends ArrayAdapter<CustomerNote> {

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

		public CustomerNotesAdapter(Context context, int textViewResourceId, ArrayList<CustomerNote> objects) {
			super(context, textViewResourceId, objects);
		}

		private class ViewHolder {

			private TextView note_title;
			private TextView note_created;
			private TextView note_updated;
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.customer_notes_list, parent, false);
			}

			ViewHolder viewHolder = new ViewHolder();
			viewHolder.note_title = (TextView) convertView.findViewById(R.id.list_note_title);
			viewHolder.note_created = (TextView) convertView.findViewById(R.id.list_note_created);
			viewHolder.note_updated = (TextView) convertView.findViewById(R.id.list_note_updated);

			viewHolder.note_title.setText(getItem(position).getTitle());
			viewHolder.note_created.setText(dateFormat.format(getItem(position).getCreated()));
			viewHolder.note_updated.setText(dateFormat.format(getItem(position).getUpdated()));

			convertView.setTag(viewHolder);

			return convertView;
		}

		@Override
		public CustomerNote getItem(int position) {
			return super.getItem(position);
		}

	}
}
