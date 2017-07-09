package no.susoft.mobile.pos.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Discount;
import no.susoft.mobile.pos.data.OrderLine;
import no.susoft.mobile.pos.ui.adapter.utils.OrderLineItemOnDragListener;

import java.util.List;

public class CartOrderLinesListDragDropAdapter extends ArrayAdapter<OrderLine> {

    List<OrderLine> objects;

    public CartOrderLinesListDragDropAdapter(Context context, int textViewResourceId, List<OrderLine> objects) {
        super(context, textViewResourceId, objects);
        this.objects = objects;
    }

    private class ViewHolder {

        private TextView tvName;
        private TextView tvQuantity;
        private TextView tvTotalLinePrice;
        private TextView tvDiscount;
        private TextView tvDiscountPercent;
        private TextView tvDiscountAmount;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.split_order_cart_fragment_orderline, parent, false);
        }

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
        viewHolder.tvQuantity = (TextView) convertView.findViewById(R.id.tvQuantity);
        viewHolder.tvTotalLinePrice = (TextView) convertView.findViewById(R.id.tvTotalLinePrice);
        viewHolder.tvDiscount = (TextView) convertView.findViewById(R.id.tvDiscount);
        viewHolder.tvDiscountPercent = (TextView) convertView.findViewById(R.id.tvDiscountPercent);
        viewHolder.tvDiscountAmount = (TextView) convertView.findViewById(R.id.tvDiscountAmount);

        viewHolder.tvName.setText(getItem(position).getProduct().getName());
        viewHolder.tvQuantity.setText("" + getItem(position).getQuantity().toString());
        viewHolder.tvTotalLinePrice.setText(getItem(position).getAmount(true).toString());

        if (getItem(position).getDiscount() != null) {
            viewHolder.tvDiscount.setVisibility(View.VISIBLE);
            viewHolder.tvDiscountPercent.setText(getItem(position).getDiscount().getPercent().toString() + " %");
            viewHolder.tvDiscountAmount.setText("-" + Discount.calculateDiscount(getItem(position).getAmount(false), getItem(position).getDiscount().getPercent()).toString());
        } else {
            viewHolder.tvDiscount.setVisibility(View.GONE);
            viewHolder.tvDiscountPercent.setText("");
            viewHolder.tvDiscountAmount.setText("");
        }

        convertView.setOnDragListener(new OrderLineItemOnDragListener(getItem(position)));

        convertView.setTag(position);
        return convertView;
    }

    @Override
    public OrderLine getItem(int position) {
        return super.getItem(position);
    }

    public List<OrderLine> getList() {
        return objects;
    }
}
