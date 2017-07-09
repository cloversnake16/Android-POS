package no.susoft.mobile.pos.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Order;

import java.util.List;

public class OrderIndexSpinnerAdapter extends ArrayAdapter<Order> {

    public OrderIndexSpinnerAdapter(Context context, int resource, List<Order> orders) {
        super(context, resource, orders);
    }

    private class ViewHolder {

        private TextView order_identifier;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.order_id_spinner, parent, false);
        }

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.order_identifier = (TextView) convertView.findViewById(R.id.order_id_spinner_text);
        viewHolder.order_identifier.setText(String.valueOf(position + 1));
        convertView.setTag(getItem(position));

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.order_id_spinner_dropdown, parent, false);
        }
        ((TextView) convertView).setText(getContext().getApplicationContext().getString(R.string.order) + " " + (position + 1));

        return convertView;
    }

    @Override
    public Order getItem(int position) {
        return super.getItem(position);
    }
}
