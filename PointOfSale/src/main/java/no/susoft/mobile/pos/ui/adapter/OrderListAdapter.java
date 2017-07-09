package no.susoft.mobile.pos.ui.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.OrderPointer;

public class OrderListAdapter extends ArrayAdapter<OrderPointer> {

    public OrderListAdapter(Context context, int textViewResourceId, ArrayList<OrderPointer> objects) {
        super(context, textViewResourceId, objects);
    }

    private class ViewHolder {

        private LinearLayout holder;
        private TextView order_id;
        private TextView order_date;
        private TextView order_seller;
        private TextView order_sum;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.orders_list, parent, false);
        }

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.holder = (LinearLayout) convertView.findViewById(R.id.list_order_holder);
        viewHolder.order_id = (TextView) convertView.findViewById(R.id.list_order_id);
        viewHolder.order_date = (TextView) convertView.findViewById(R.id.list_order_date);
        viewHolder.order_seller = (TextView) convertView.findViewById(R.id.list_order_seller);
        viewHolder.order_sum = (TextView) convertView.findViewById(R.id.list_order_sum);
	
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        String date = df.format("dd/MM/yyyy kk:mm", getItem(position).date).toString();

//        if (getItem(position).isLocked()) {
//            viewHolder.holder.setBackgroundColor(Color.parseColor("#FAF8E1"));
//        } else {
            viewHolder.holder.setBackgroundColor(Color.parseColor("#FFFFFF"));
//        }

        viewHolder.order_id.setText(Long.toString(getItem(position).id));
        viewHolder.order_date.setText(date);
        viewHolder.order_seller.setText(getItem(position).salesPersonName);
        viewHolder.order_sum.setText(getItem(position).amount.toString());

        convertView.setTag(viewHolder);

        return convertView;
    }

    @Override
    public OrderPointer getItem(int position) {
        return super.getItem(position);
    }

}






