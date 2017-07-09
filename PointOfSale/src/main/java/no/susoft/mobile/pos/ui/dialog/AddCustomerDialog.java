package no.susoft.mobile.pos.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.Customer;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class AddCustomerDialog extends DialogFragment {
	
	AlertDialog dialog;
	EditText firstname;
	EditText lastname;
	EditText mobile;
	EditText phone;
	EditText email;
	EditText address1;
	EditText address2;
	EditText zip;
	RadioButton company;
	RadioButton person;
	TextView firstNameLabel;
	TextView lastNameLabel;
	
	@Override
	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		final View view = (inflater.inflate(R.layout.new_customer_dialog, null));
		
		builder.setView(view).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				getDialog().dismiss();
			}
		}).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
		
		dialog = builder.create();
		return dialog;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		final AlertDialog d = (AlertDialog) getDialog();
		if (d != null) {
			Button positiveButton = (Button) d.getButton(Dialog.BUTTON_POSITIVE);
			positiveButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Boolean wantToCloseDialog = onOkClick();
					if (wantToCloseDialog)
						d.dismiss();
				}
			});
		}
		
		firstNameLabel = (TextView) dialog.findViewById(R.id.firstNameLabel);
		lastNameLabel = (TextView) dialog.findViewById(R.id.lastNameLabel);
		person = (RadioButton) dialog.findViewById(R.id.personRadioButton);
		company = (RadioButton) dialog.findViewById(R.id.companyRadioButton);
		firstname = (EditText) dialog.findViewById(R.id.new_customer_firstname);
		lastname = (EditText) dialog.findViewById(R.id.new_customer_lastname);
		mobile = (EditText) dialog.findViewById(R.id.new_customer_mobile);
		phone = (EditText) dialog.findViewById(R.id.new_customer_phone);
		email = (EditText) dialog.findViewById(R.id.new_customer_email);
		address1 = (EditText) dialog.findViewById(R.id.new_customer_address1);
		address2 = (EditText) dialog.findViewById(R.id.new_customer_address2);
		zip = (EditText) dialog.findViewById(R.id.new_customer_zip);
		
		setListeners();
	}
	
	private void setListeners() {
		company.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				firstNameLabel.setText(R.string.company_name);
				lastNameLabel.setText(R.string.contact);
			}
		});
		person.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				firstNameLabel.setText(R.string.new_customer_firstname_text);
				lastNameLabel.setText(R.string.new_customer_lastname_text);
			}
		});
	}
	
	private boolean onOkClick() {
		Customer customer = new Customer();
		customer.setFirstName(getTrimmedText(firstname));
		customer.setLastName(getTrimmedText(lastname));
		customer.setMobile(getTrimmedText(mobile));
		customer.setPhone(getTrimmedText(phone));
		customer.setEmail(getTrimmedText(email));
		customer.setAddress1(getTrimmedText(address1));
		customer.setAddress2(getTrimmedText(address2));
		customer.setZip(getTrimmedText(zip));
		customer.setShopId(AccountManager.INSTANCE.getAccount().getShop().getID());
		if (company.isChecked()) {
			customer.setCompany(true);
		} else {
			customer.setCompany(false);
		}
		
		if (!customer.isCompany() && customer.getFirstName().length() == 0) {
			Toast.makeText(MainActivity.getInstance(), R.string.error_customer_firstname_empty, Toast.LENGTH_LONG).show();
			return false;
		}
		
		if (customer.getLastName().length() == 0) {
			Toast.makeText(MainActivity.getInstance(), R.string.error_customer_lastname_empty, Toast.LENGTH_LONG).show();
			return false;
		}
		
		MainActivity.getInstance().getServerCallMethods().createNewCustomer(customer);
		return true;
	}
	
	private String getTrimmedText(EditText textField) {
		return textField.getText().toString().trim();
	}
	
}
