package no.susoft.mobile.pos.ui.adapter;

import java.util.*;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.OrderLine;
import no.susoft.mobile.pos.data.Product;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

public class ProductBundleListAdapter extends ArrayAdapter<Product> {

	private static final int TYPE_ITEM = 0;
	private static final int TYPE_SEPARATOR = 1;

	private OrderLine ol;
	private List<OrderLine> linesComponents;
	private List<Product> productComponents;
	private TreeSet<Integer> sectionHeader = new TreeSet<Integer>();

	public ProductBundleListAdapter(Context context, int textViewResourceId, OrderLine ol, List<Product> productComponents, TreeSet<Integer> sectionHeader) {
		super(context, textViewResourceId, ol.getProduct().getComponents());
		linesComponents = ol.getComponents();
		this.productComponents = productComponents;
		this.sectionHeader = sectionHeader;
	}

	private class ViewHolder {

		private TextView tvName;
		private TextView tvPrice;
		private CheckBox check;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		int rowType = getItemViewType(position);

		if (convertView == null) {
			convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.order_line_edit_bundle_dialog_item, parent, false);
		}

		ViewHolder viewHolder = new ViewHolder();
		viewHolder.tvName = (TextView) convertView.findViewById(R.id.bundle_component_name);
		viewHolder.tvPrice = (TextView) convertView.findViewById(R.id.bundle_component_price);
		viewHolder.check = (CheckBox) convertView.findViewById(R.id.bundle_component_check);

		if (rowType == TYPE_ITEM) {
			viewHolder.tvName.setText(productComponents.get(position).getName());
			if (Cart.INSTANCE.isTakeAwayMode() && productComponents.get(position).isUseAlternative()) {
				viewHolder.tvPrice.setText("+" + productComponents.get(position).getAlternativePrice().toString());
			} else if (productComponents.get(position).getPrice().isPositive()) {
				viewHolder.tvPrice.setText("+" + productComponents.get(position).getPrice().toString());
			} else {
				viewHolder.tvPrice.setText("");
			}
			viewHolder.check.setTag(position);

			boolean checked = false;
			if (linesComponents != null) {
				for (OrderLine linesComponent : linesComponents) {
					if (linesComponent.getProduct().getId().equals(productComponents.get(position).getId())) {
						checked = true;
						convertView.setSelected(true);
						break;
					}
				}
			}
			viewHolder.check.setChecked(checked);

			if (Cart.INSTANCE.isTakeAwayMode() && productComponents.get(position).isUseAlternative() && getItem(position).getAlternativePrice().isZero()) {
				viewHolder.tvPrice.setVisibility(View.GONE);
			} else if (getItem(position).getPrice().isZero()) {
				viewHolder.tvPrice.setVisibility(View.GONE);
			} else {
				viewHolder.tvPrice.setVisibility(View.VISIBLE);
			}

		} else {
			viewHolder.tvName.setText(productComponents.get(position).getCategoryName());
			viewHolder.tvPrice.setVisibility(View.GONE);
			viewHolder.check.setVisibility(View.GONE);
			convertView.setBackgroundResource(R.drawable.numpad_gray_button);
			convertView.setEnabled(false);
		}

		convertView.setTag(position);
		return convertView;
	}

	@Override
	public Product getItem(int position) {
		return productComponents.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		return sectionHeader.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
	}

	@Override
	public int getCount() {
		return productComponents.size();
	}

	@Override
	public int getViewTypeCount() {
		return 2;
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
