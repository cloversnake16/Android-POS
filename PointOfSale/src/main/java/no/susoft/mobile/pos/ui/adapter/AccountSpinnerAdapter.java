package no.susoft.mobile.pos.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Account;

import java.util.ArrayList;

public class AccountSpinnerAdapter extends ArrayAdapter<Account> {

    public AccountSpinnerAdapter(Context context, int resource, ArrayList<Account> accounts) {
        super(context, resource, accounts);
    }

    private class ViewHolder {

        private TextView account_name;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.account_spinner, parent, false);
        }

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.account_name = (TextView) convertView.findViewById(R.id.account_spinner_name);

        if (getItem(position).getName() != null && !getItem(position).getName().isEmpty()) {
            viewHolder.account_name.setText(getItem(position).getName());
        } else {
            viewHolder.account_name.setText(getItem(position).getLogin());
        }

        convertView.setTag(viewHolder);

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.shop_spinner, parent, false);
        }
        TextView label = (TextView) convertView;
        if (getItem(position).getName() != null && !getItem(position).getName().isEmpty()) {
            ((TextView) convertView).setText(getItem(position).getName());
        } else {
            ((TextView) convertView).setText(getItem(position).getLogin());
        }

        return label;
    }

    @Override
    public Account getItem(int position) {
        return super.getItem(position);
    }

}

