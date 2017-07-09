package no.susoft.mobile.pos.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Customer;

import java.util.ArrayList;

public class CustomerListAdapter extends ArrayAdapter<Customer> {

    public CustomerListAdapter(Context context, int textViewResourceId, ArrayList<Customer> objects) {
        super(context, textViewResourceId, objects);
    }

    private class ViewHolder {

        private TextView customer_id;
        private TextView customer_firstname;
        private TextView customer_lastname;
        private TextView customer_email;
        private TextView customer_phone;
        private TextView customer_mobile;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.customer_search_list, parent, false);
        }

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.customer_id = (TextView) convertView.findViewById(R.id.list_customer_id);
        viewHolder.customer_firstname = (TextView) convertView.findViewById(R.id.list_customer_firstname);
        viewHolder.customer_lastname = (TextView) convertView.findViewById(R.id.list_customer_lastname);
        viewHolder.customer_email = (TextView) convertView.findViewById(R.id.list_customer_email);
        viewHolder.customer_phone = (TextView) convertView.findViewById(R.id.list_customer_phone);
        viewHolder.customer_mobile = (TextView) convertView.findViewById(R.id.list_customer_mobile);

        viewHolder.customer_id.setText(getItem(position).getId());
        viewHolder.customer_firstname.setText(getItem(position).getFirstName());
        viewHolder.customer_lastname.setText(getItem(position).getLastName());
        viewHolder.customer_email.setText(getItem(position).getEmail());
        viewHolder.customer_phone.setText(getItem(position).getPhone());
        viewHolder.customer_mobile.setText(getItem(position).getMobile());

        convertView.setTag(viewHolder);

/*		convertView.setOnClickListener(new View.OnClickListener() {
            @Override
			public void onClick(View view) {
				MainActivity.getInstance().getCartFragment()._orderCustomerUpdated(getItem(position));
			}
		});*/

        return convertView;
    }

    @Override
    public Customer getItem(int position) {
        return super.getItem(position);
    }

}






