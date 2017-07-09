package no.susoft.mobile.pos.ui.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.ui.activity.util.NavigationMenuItem;

import java.util.ArrayList;

public class AdminMenuAdapter extends ArrayAdapter<NavigationMenuItem> {

    TextView menuItemText;

    public AdminMenuAdapter(Context context, int resource, ArrayList<NavigationMenuItem> items) {
        super(context, resource, items);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.admin_menu_list_item, parent, false);
        }

        menuItemText = (TextView) convertView.findViewById(R.id.admin_detail_list_item);

        menuItemText.setText(getItem(position).toString());

        convertView.setTag(getItem(position));

        if (position == 0) {
            convertView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.orange_border));
        } else {
            convertView.setBackground(null);
        }
        return convertView;
    }

    @Override
    public NavigationMenuItem getItem(int position) {
        return super.getItem(position);
    }

}
