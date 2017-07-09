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

public class DallasKeyListAdapter extends ArrayAdapter<Account> {

    public DallasKeyListAdapter(Context context, int resource, ArrayList<Account> items) {
        super(context, resource, items);
    }

    private class ViewHolder {

        private TextView account_id;
        private TextView account_name;
        private TextView account_key;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.dallas_key_fragment_list_item, parent, false);
        }

        ViewHolder vh = new ViewHolder();
        vh.account_name = (TextView) convertView.findViewById(R.id.dallas_account_name);
        vh.account_key = (TextView) convertView.findViewById(R.id.dallas_account_key);
        vh.account_id = (TextView) convertView.findViewById(R.id.dallas_account_id);

        if (getItem(position).getName() != null) {
            vh.account_name.setText(getItem(position).getName());
        } else {
            vh.account_name.setText("");
        }

        if (getItem(position).getSecurityCode() != null) {
            vh.account_key.setText(getItem(position).getSecurityCode());
        } else {
            vh.account_key.setText("");
        }

        if (getItem(position).getUserId() != null) {
            vh.account_id.setText(getItem(position).getUserId());
        } else {
            vh.account_id.setText("");
        }


        convertView.setTag(getItem(position));

        return convertView;
    }

    @Override
    public Account getItem(int position) {
        return super.getItem(position);
    }
}
