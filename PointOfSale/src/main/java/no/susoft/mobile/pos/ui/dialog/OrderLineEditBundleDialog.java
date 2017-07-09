package no.susoft.mobile.pos.ui.dialog;

import java.util.*;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.Utilities;
import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.data.OrderLine;
import no.susoft.mobile.pos.data.Product;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.adapter.ProductBundleListAdapter;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

public class OrderLineEditBundleDialog extends DialogFragment {

    private ListView components;
    private Product product;
    private OrderLine ol;
	private List<Product> componentsWithHeaders;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(800, 700);
    }

	@Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View view = (inflater.inflate(R.layout.order_line_edit_bundle_dialog, null));
        builder.setView(view).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (ol != null && ol.getProduct().isBundle()) {
					ArrayList<OrderLine> lines = new ArrayList<OrderLine>();
					ArrayList<OrderLine> oldLines = ol.getComponents();
					SparseBooleanArray sbArray = components.getCheckedItemPositions();

					for (int j = 0; j < componentsWithHeaders.size(); j++) {
						if (sbArray.get(j) && componentsWithHeaders.size() > j) {
							Order order = Cart.INSTANCE.getOrder();
							Product product = componentsWithHeaders.get(j);
							if (product.getId() != null && product.hasID()) {
								OrderLine line = new OrderLine();
								line.setOrderId(order.getId());
								line.setShopId(order.getShopId());
								line.setProduct(product);
								line.setText(product.getName());
								if (ol.getDiscount() != null) {
									line.setDiscount(ol.getDiscount());
								}
								if (order.isUseAlternative() && product.isUseAlternative()) {
									line.setPrice(product.getAlternativePrice());
								} else {
									line.setPrice(product.getPrice());
								}
								line.setQuantity(ol.getQuantity());
								lines.add(line);
							}
						}
					}

					boolean needToPrintCancel = false;
					if (oldLines != null) {
						if (oldLines.size() != lines.size()) {
							needToPrintCancel = true;
						} else {
							for (OrderLine oldLine : oldLines) {
								boolean contains = false;
								for (OrderLine line : lines) {
									if (oldLine.getProduct().getId().equals(line.getProduct().getId())) {
										contains = true;
									}
								}
								if (!contains) {
									needToPrintCancel = true;
								}
							}
						}
					}

					if (needToPrintCancel) {
						final OrderLine orderLineForPrint = (OrderLine) Utilities.copy(ol);
						MainActivity.getInstance().getServerCallMethods().printCancelLineOnKitchen(orderLineForPrint);
					}

					ol.setDeliveredQty(Decimal.ZERO);
					ol.setComponents(lines);
					MainActivity.getInstance().getCartFragment().refreshCart();
					if (Cart.INSTANCE.getOrder().getLines().size() == 1) {
						MainActivity.getInstance().getCartFragment().selectLastRow();
					}
					getDialog().dismiss();
				}
            }
        });

		componentsWithHeaders = new ArrayList<>();
		TreeSet<Integer> sectionHeader = new TreeSet<Integer>();

		List<Product> list = ol.getProduct().getComponents();
		Collections.sort(list, new Comparator<Product>() {
			@Override
			public int compare(Product p1, Product p2) {
				if (p1.getCategoryName() != null && p2.getCategoryName() != null) {
					int i = p1.getCategoryName().compareTo(p2.getCategoryName());
					if (i != 0)
						return i;
				}

				int i = p1.getName().compareTo(p2.getName());
				if (i != 0)
					return i;

				return p1.getId().compareTo(p2.getId());
			}
		});

		String sectionName = "";
		for (Product product : list) {
			if (product.getCategoryName() != null) {
				if ((sectionName.length() == 0 && sectionHeader.size() == 0) || !product.getCategoryName().equals(sectionName)) {
					sectionName = product.getCategoryName();
					Product p = new Product();
					p.setCategoryName(sectionName);
					componentsWithHeaders.add(p);
					sectionHeader.add(componentsWithHeaders.size() - 1);
					
				}
			}
			componentsWithHeaders.add(product);
		}

        components = (ListView) view.findViewById(R.id.bundle_component_list);
		ProductBundleListAdapter adapter = new ProductBundleListAdapter(MainActivity.getInstance(), 0, ol, componentsWithHeaders, sectionHeader);
		components.setAdapter(adapter);
		if (ol.getComponents() != null) {
			for (int j = 0; j < componentsWithHeaders.size(); j++) {
				for (OrderLine linesComponent : ol.getComponents()) {
					if (componentsWithHeaders.get(j).getId() != null && componentsWithHeaders.get(j).hasID()) {
						if (linesComponent.getProduct().getId().equals(componentsWithHeaders.get(j).getId())) {
							components.setItemChecked(j, true);
							break;
						}
					}
				}
			}
		}

		components.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				SparseBooleanArray sbArray = components.getCheckedItemPositions();
				CheckBox chk = (CheckBox) view.findViewById(R.id.bundle_component_check);
				if (sbArray.get(position)) {
					chk.setChecked(true);
				} else {
					chk.setChecked(false);
				}
			}
		});

        return builder.create();
    }

	public void setOrderLine(OrderLine ol) {
		this.ol = ol;
	}
}
