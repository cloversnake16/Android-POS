package no.susoft.mobile.pos.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.data.Prepaid;

import java.util.ArrayList;
import java.util.List;

public class GiftcardListAdapter extends ArrayAdapter<Prepaid> {

    public GiftcardListAdapter(Context context, int textViewResourceId, List<Prepaid> objects) {
        super(context, textViewResourceId, objects);
    }

    private class ViewHolder {
        private LinearLayout holder;
        private TextView giftcard_number;
        private TextView giftcard_date;
        private TextView giftcard_issued_amount;
        private TextView giftcard_current_amount;
        private TextView giftcard_shop;
        private TextView giftcard_seller;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.giftcard_lookup_list, parent, false);
        }

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.holder = (LinearLayout) convertView.findViewById(R.id.list_giftcard_lookup_holder);
        viewHolder.giftcard_number = (TextView) convertView.findViewById(R.id.list_giftcard_number);
        viewHolder.giftcard_date = (TextView) convertView.findViewById(R.id.list_giftcard_date);
        viewHolder.giftcard_shop = (TextView) convertView.findViewById(R.id.list_giftcard_shop);
        viewHolder.giftcard_seller = (TextView) convertView.findViewById(R.id.list_giftcard_seller);
        viewHolder.giftcard_issued_amount = (TextView) convertView.findViewById(R.id.list_giftcard_issued_amount);
        viewHolder.giftcard_current_amount = (TextView) convertView.findViewById(R.id.list_giftcard_current_amount);

        android.text.format.DateFormat df = new android.text.format.DateFormat();
        String date = df.format("dd/MM/yyyy", getItem(position).getIssuedDate()).toString();

        viewHolder.giftcard_number.setText(getItem(position).getNumber());
        viewHolder.giftcard_date.setText(date);
        viewHolder.giftcard_shop.setText(getItem(position).getShopId());
        viewHolder.giftcard_seller.setText(getItem(position).getSalespersonId());
        viewHolder.giftcard_issued_amount.setText(getItem(position).getIssuedAmount().toString());
        viewHolder.giftcard_current_amount.setText(String.valueOf(getItem(position).getAmount().toString()));

        if(getItem(position).getAmount().isZero()) {
            convertView.setBackgroundColor(Color.parseColor("#ffb3b3"));
        }

        convertView.setTag(viewHolder);

        return convertView;
    }

    @Override
    public Prepaid getItem(int position) {
        return super.getItem(position);
    }

}






