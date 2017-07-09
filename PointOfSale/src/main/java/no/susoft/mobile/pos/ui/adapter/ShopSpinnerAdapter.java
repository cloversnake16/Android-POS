package no.susoft.mobile.pos.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Shop;

import java.util.List;

public class ShopSpinnerAdapter extends ArrayAdapter<Shop> {

    public ShopSpinnerAdapter(Context context, int resource, List<Shop> shops) {
        super(context, resource, shops);
    }

    private class ViewHolder {

        private TextView shop_name;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.shop_spinner, parent, false);
        }

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.shop_name = (TextView) convertView.findViewById(R.id.shop_spinner_name);
        viewHolder.shop_name.setText(getItem(position).getName());
        convertView.setTag(viewHolder);

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.shop_spinner, parent, false);
        }
        ((TextView) convertView).setText(getItem(position).getID() + " - " + getItem(position).getName());

        return convertView;
    }

    @Override
    public Shop getItem(int position) {
        return super.getItem(position);
    }

}




