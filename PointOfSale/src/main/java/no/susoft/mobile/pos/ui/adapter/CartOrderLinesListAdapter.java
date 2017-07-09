package no.susoft.mobile.pos.ui.adapter;

import java.util.List;
import java.util.Properties;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.data.Discount;
import no.susoft.mobile.pos.data.OrderLine;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class CartOrderLinesListAdapter extends ArrayAdapter<OrderLine> {

    private List<OrderLine> list;

    public CartOrderLinesListAdapter(Context context, int textViewResourceId, List<OrderLine> objects) {
        super(context, textViewResourceId, objects);
        list = objects;
    }

    public void setList(List<OrderLine> list) {
        this.list = list;
    }

    public List<OrderLine> getList() {
        return this.list;
    }

    private class ViewHolder {

        private TextView tvName;
        private TextView tvQuantity;
        private TextView tvPrice;
        private TextView tvTotalLinePrice;
        private LinearLayout llLineDiscount;
        private TextView tvDiscount;
        private TextView tvDiscountPercent;
        private TextView tvDiscountAmount;
        private LinearLayout llLineSeller;
        private TextView tvSellerName;
        private LinearLayout llLineComponents;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.cart_fragment_orderline, parent, false);
        }

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
        viewHolder.tvQuantity = (TextView) convertView.findViewById(R.id.tvQuantity);
        viewHolder.tvPrice = (TextView) convertView.findViewById(R.id.tvPrice);
        viewHolder.tvTotalLinePrice = (TextView) convertView.findViewById(R.id.tvTotalLinePrice);
        viewHolder.llLineDiscount = (LinearLayout) convertView.findViewById(R.id.llLineDiscount);
        viewHolder.tvDiscount = (TextView) convertView.findViewById(R.id.tvDiscount);
        viewHolder.tvDiscountPercent = (TextView) convertView.findViewById(R.id.tvDiscountPercent);
        viewHolder.tvDiscountAmount = (TextView) convertView.findViewById(R.id.tvDiscountAmount);
        viewHolder.llLineSeller = (LinearLayout) convertView.findViewById(R.id.llLineSeller);
        viewHolder.tvSellerName = (TextView) convertView.findViewById(R.id.tvSellerName);
        viewHolder.llLineComponents = (LinearLayout) convertView.findViewById(R.id.llLineComponents);
	
        viewHolder.tvName.setText(getItem(position).getProduct().getName());
        viewHolder.tvQuantity.setText(getItem(position).getQuantity().toString());
        viewHolder.tvPrice.setText(getItem(position).getPrice().toString());
        viewHolder.tvTotalLinePrice.setText(getItem(position).getAmount(true).toString());
	
		if (getItem(position).getSalesPersonId() != null && !getItem(position).getSalesPersonId().isEmpty()) {
			viewHolder.llLineSeller.setVisibility(View.VISIBLE);
			for (Properties sp : MainActivity.getInstance().getMainShell().getSalesPersons()) {
				if (sp.getProperty("id").equals(getItem(position).getSalesPersonId())) {
					viewHolder.tvSellerName.setText(sp.getProperty("name"));
					if (getItem(position).getDiscount() != null) {
						viewHolder.llLineDiscount.setVisibility(View.VISIBLE);
					} else {
						viewHolder.llLineDiscount.setVisibility(View.GONE);
					}
				}
			}
		} else {
			viewHolder.llLineDiscount.setVisibility(View.VISIBLE);
			viewHolder.llLineSeller.setVisibility(View.GONE);
			viewHolder.tvSellerName.setText("");
		}

		if (getItem(position).getComponents() != null && getItem(position).getComponents().size() > 0) {
			viewHolder.llLineComponents.setVisibility(View.VISIBLE);
			viewHolder.llLineComponents.removeAllViews();
			if (getItem(position).getDiscount() != null) {
				viewHolder.llLineDiscount.setVisibility(View.VISIBLE);
			} else {
				viewHolder.llLineDiscount.setVisibility(View.GONE);
			}
			for (OrderLine line : getItem(position).getComponents()) {
				View vi = LayoutInflater.from(this.getContext()).inflate(R.layout.order_line_bundle_item, null);
				TextView tvName = (TextView) vi.findViewById(R.id.bundle_component_name);
				TextView tvPrice = (TextView) vi.findViewById(R.id.bundle_component_price);

				tvName.setText(line.getText());
				if (line.getAmount(true).isPositive()) {
					tvPrice.setText(line.getAmount(true).toString());
				} else {
					tvPrice.setText("");
				}

				if (line.getPrice().isZero()) {
					tvPrice.setVisibility(View.GONE);
				} else {
					tvPrice.setVisibility(View.VISIBLE);
				}

				viewHolder.llLineComponents.addView(vi);
			}
		} else {
			viewHolder.llLineComponents.setVisibility(View.GONE);
		}

		if (getItem(position).hasNote()) {
			convertView.setBackgroundColor(MainActivity.getInstance().getResources().getColor(R.color.light_orange));
		}

//        else if(convertView.isActivated()) {
//            convertView.setBackgroundColor(MainActivity.getInstance().getResources().getColor(R.color.et_orange));
//        } else {
//            convertView.setBackgroundColor(Color.TRANSPARENT);
//        }

        if (getItem(position).getDiscount() != null) {
            viewHolder.tvDiscount.setVisibility(View.VISIBLE);
            viewHolder.tvDiscountPercent.setText(getItem(position).getDiscount().getPercent().toString() + " %");

			Decimal amount = getItem(position).getAmount(false);
			if (getItem(position).getComponents() != null) {
				for (OrderLine line : getItem(position).getComponents()) {
					amount = amount.add(line.getAmount(false));
				}
			}

            viewHolder.tvDiscountAmount.setText("-" + Discount.calculateDiscount(amount, getItem(position).getDiscount().getPercent()).toString());
        } else {
            viewHolder.tvDiscount.setVisibility(View.GONE);
            viewHolder.tvDiscountPercent.setText("");
            viewHolder.tvDiscountAmount.setText("");
        }

        convertView.setTag(position);
        return convertView;
    }

    @Override
    public OrderLine getItem(int position) {
        return super.getItem(position);
    }

}
