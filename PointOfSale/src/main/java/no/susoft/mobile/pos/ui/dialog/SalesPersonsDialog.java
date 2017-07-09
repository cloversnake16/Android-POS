package no.susoft.mobile.pos.ui.dialog;

import java.util.ArrayList;
import java.util.Properties;

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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

public class SalesPersonsDialog extends DialogFragment {

	ListView salesPersonsList;

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

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();

		final View view = (inflater.inflate(R.layout.salespersons_dialog, null));
		builder.setView(view).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User cancelled the dialog
			}
		});

		salesPersonsList = (ListView) view.findViewById(R.id.salespersons_list);
		setupAdapter();

		return builder.create();
	}

	public void setupAdapter() {
		SalesPersonsAdapter adapter = new SalesPersonsAdapter(MainActivity.getInstance(), 0, MainActivity.getInstance().getMainShell().getSalesPersons());
		salesPersonsList.setAdapter(adapter);
		salesPersonsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				final Properties sp = (Properties) adapterView.getItemAtPosition(i);
				if (sp != null) {
					if (!sp.getProperty("id").equals(AccountManager.INSTANCE.getAccount().getUserId())) {
						Cart.selectedLine.setSalesPersonId(sp.getProperty("id"));
					} else {
						Cart.selectedLine.setSalesPersonId(null);
					}
					getDialog().dismiss();
					MainActivity.getInstance().getNumpadEditFragment().updateOptionLineButtons();
					MainActivity.getInstance().getCartFragment().refreshCart();
				}
			}
		});
	}

	public class SalesPersonsAdapter extends ArrayAdapter<Properties> {

		public SalesPersonsAdapter(Context context, int textViewResourceId, ArrayList<Properties> objects) {
			super(context, textViewResourceId, objects);
		}

		private class ViewHolder {

			private TextView itemName;
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.simple_list, parent, false);
			}

			ViewHolder viewHolder = new ViewHolder();
			viewHolder.itemName = (TextView) convertView.findViewById(R.id.itemName);
			viewHolder.itemName.setText(getItem(position).getProperty("name"));
			convertView.setTag(viewHolder);

			return convertView;
		}

		@Override
		public Properties getItem(int position) {
			return super.getItem(position);
		}

	}
}
