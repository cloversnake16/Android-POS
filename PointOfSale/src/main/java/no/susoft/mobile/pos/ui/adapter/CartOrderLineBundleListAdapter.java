package no.susoft.mobile.pos.ui.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.OrderLine;

public class CartOrderLineBundleListAdapter extends ArrayAdapter<OrderLine> {

    private OrderLine ol;
    private List<OrderLine> linesComponents;

    public CartOrderLineBundleListAdapter(Context context, int textViewResourceId, OrderLine ol) {
        super(context, textViewResourceId, ol.getComponents());
		linesComponents = ol.getComponents();
	}

    private class ViewHolder {

        private TextView tvName;
        private TextView tvPrice;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.order_line_bundle_item, parent, false);
        }

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.tvName = (TextView) convertView.findViewById(R.id.bundle_component_name);
        viewHolder.tvPrice = (TextView) convertView.findViewById(R.id.bundle_component_price);

        viewHolder.tvName.setText(linesComponents.get(position).getText());
		if (linesComponents.get(position).getPrice().isPositive()) {
			viewHolder.tvPrice.setText(linesComponents.get(position).getPrice().toString());
		} else {
			viewHolder.tvPrice.setText("");
		}

		if (getItem(position).getPrice().isZero()) {
			viewHolder.tvPrice.setVisibility(View.GONE);
		} else {
			viewHolder.tvPrice.setVisibility(View.VISIBLE);
		}

        convertView.setTag(position);
        return convertView;
    }

    @Override
    public OrderLine getItem(int position) {
        return super.getItem(position);
    }

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int arg0) {
		return true;
	}

}
