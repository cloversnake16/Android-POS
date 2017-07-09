package no.susoft.mobile.pos.ui.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.Utilities;
import no.susoft.mobile.pos.data.Product;

public class ProductListAdapter extends ArrayAdapter<Product> {

    public ProductListAdapter(Context context, int textViewResourceId, ArrayList<Product> objects) {
        super(context, textViewResourceId, objects);
    }

    private class ViewHolder {

        private TextView product_name;
        private TextView product_description;
        private TextView product_stock;
        private TextView product_price;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.product_search_list, parent, false);
        }

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.product_name = (TextView) convertView.findViewById(R.id.list_product_name);
        viewHolder.product_description = (TextView) convertView.findViewById(R.id.list_product_description);
        viewHolder.product_stock = (TextView) convertView.findViewById(R.id.list_product_stock);
        viewHolder.product_price = (TextView) convertView.findViewById(R.id.list_product_price);

        viewHolder.product_name.setText(getItem(position).getName());
        viewHolder.product_description.setText(getItem(position).getDescription());
		if (!getItem(position).isMiscellaneous()) {
			viewHolder.product_stock.setText(String.valueOf(getItem(position).getStockQty().toInteger()));
		} else {
			viewHolder.product_stock.setText("");
		}
        viewHolder.product_price.setText(Utilities.formatCurrency(getItem(position).getPrice()));

        convertView.setTag(viewHolder);

        return convertView;
    }

    @Override
    public Product getItem(int position) {
        return super.getItem(position);
    }

}






