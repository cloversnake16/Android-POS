package no.susoft.mobile.pos.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.PointOfSale;

import java.util.List;

/**
 * Created by Vilde on 11.12.2015.
 */
public class PosListAdapter extends ArrayAdapter<PointOfSale> {

    public PosListAdapter(Context context, int resource, List<PointOfSale> objects) {
        super(context, resource, objects);
    }

    private class ViewHolder {

        private TextView posId;
        private TextView posName;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.pos_line, parent, false);
        }

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.posId = (TextView) convertView.findViewById(R.id.posId);
        viewHolder.posName = (TextView) convertView.findViewById(R.id.posName);

        viewHolder.posId.setText(getItem(position).getId());
        viewHolder.posName.setText(getItem(position).getName());

        convertView.setTag(getItem(position));

        return convertView;
    }

    @Override
    public PointOfSale getItem(int position) {
        return super.getItem(position);
    }
}
