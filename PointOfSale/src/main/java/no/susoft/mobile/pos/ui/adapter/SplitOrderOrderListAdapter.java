package no.susoft.mobile.pos.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.ui.adapter.utils.OrderItemOnDragListener;

import java.util.ArrayList;
import java.util.List;

public class SplitOrderOrderListAdapter extends ArrayAdapter<Order> {

    List<Order> list;

    public SplitOrderOrderListAdapter(Context context, int textViewResourceId, ArrayList<Order> objects) {
        super(context, textViewResourceId, objects);
        list = objects;
    }

    private class ViewHolder {

        private TextView order_identifier;
        private TextView order_no_of_items;
        private TextView order_sum;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.split_order_orders_list_item, parent, false);
        }

        ViewHolder viewHolder = new ViewHolder();

        viewHolder.order_identifier = (TextView) convertView.findViewById(R.id.list_order_customer);
        viewHolder.order_no_of_items = (TextView) convertView.findViewById(R.id.list_order_no_of_items);
        viewHolder.order_sum = (TextView) convertView.findViewById(R.id.list_order_sum);

        if (getItem(position).getCustomer() != null) {
            viewHolder.order_identifier.setText(getItem(position).getCustomer().getName());
        } else {
            if (getItem(position).getLocalOrderIdentifier() == null || getItem(position).getLocalOrderIdentifier().isEmpty()) {
                getItem(position).setLocalOrderIdentifier("" + (position + 1));
            }
            viewHolder.order_identifier.setText(getItem(position).getLocalOrderIdentifier());
        }
        viewHolder.order_no_of_items.setText(String.valueOf(getItem(position).getNumberOfItems()));
        viewHolder.order_sum.setText(getItem(position).getAmount(true).toString());

        convertView.setTag(getItem(position));
        convertView.setOnDragListener(new OrderItemOnDragListener(getItem(position)));
        //convertView.setOnDragListener(MainActivity.getInstance().getCartFragment().getSplitOrderDialog().getOrderListListOnDragListener(position, getdList()));

        return convertView;
    }

    @Override
    public Order getItem(int position) {
        return super.getItem(position);
    }

    public List<Order> getList() {
        return list;
    }

}






