package no.susoft.mobile.pos.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.DiscountReason;

import java.util.List;

public class DiscountReasonSpinnerAdapter extends ArrayAdapter<DiscountReason> {

    public DiscountReasonSpinnerAdapter(Context context, int resource, List<DiscountReason> reasons) {
        super(context, resource, reasons);
    }

    private class ViewHolder {

        private TextView reason_name;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.discount_reason_spinner, parent, false);
        }

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.reason_name = (TextView) convertView.findViewById(R.id.discount_reason_spinner_name);
        viewHolder.reason_name.setText(getItem(position).getName());
        convertView.setTag(viewHolder);

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.discount_reason_spinner, parent, false);
        }
        ((TextView) convertView).setText(getItem(position).getID() + " - " + getItem(position).getName());

        return convertView;
    }

    @Override
    public DiscountReason getItem(int position) {
        return super.getItem(position);
    }

}




