package no.susoft.mobile.pos.ui.dialog;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.*;
import no.susoft.mobile.pos.hardware.ConnectionManager;
import no.susoft.mobile.pos.hardware.combi.mPOPPrintOrderSlim;
import no.susoft.mobile.pos.hardware.printer.*;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.activity.util.AppConfig;
import no.susoft.mobile.pos.ui.activity.util.MainTopBarMenu;
import no.susoft.mobile.pos.ui.adapter.CartOrderLinesListAdapter;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

public class OrderSearchSelectionDialog extends DialogFragment {

    @InjectView(R.id.details_button_print) Button printButton;
    @InjectView(R.id.details_button_cancel) Button cancelButton;
    @InjectView(R.id.details_button_return) Button returnButton;
    @InjectView(R.id.details_button_select) Button selectButton;
    @InjectView(R.id.order_details_list)    ListView list;
    private Order order;
    private OrderSearchDialog parentDialog;
    private CartOrderLinesListAdapter adapter;
    private ArrayList<OrderLine> selectedLines;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.orders_search__selection_fragment, container, false);
        ButterKnife.inject(this, rootView);

        setButtonListeners();
        setOrderLineAdapter(order);
        setListOnClickListener();
        selectedLines = new ArrayList<>();

        return rootView;
    }
	
	private void setOrderLineAdapter(Order order) {
		if (order != null && order.hasLines()) {
			adapter = new CartOrderLinesListAdapter(MainActivity.getInstance(), 0, order.getLines());
			list.setAdapter(adapter);
		}
	}

    private void setButtonListeners() {
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeDialog();
            }
        });

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnSelectedLines();
                closeDialog();
            }
        });

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
				selectAllLines();
            }
        });

        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
				int printerProvider = AppConfig.getState().getPrinterProviderOrdinal();
				if (printerProvider == PeripheralProvider.CASIO.ordinal()) {
					if (CasioPrintOrder.hasPrinterConnected()) {
						new CasioPrintOrder().print(order, ReceiptPrintType.COPY);
					}
				} else if (printerProvider == PeripheralProvider.BIXOLON.ordinal()) {
					if (AppConfig.getState().getPrinterIp().isEmpty()) {
						BluetoothPrintOrderWide bp = new BluetoothPrintOrderWide(null, true);
						bp.print(bp.makeReceipt(order));
					} else {
						ConnectionManager.getInstance().execute(new BixolonPrinterJob() {
							@Override
							public int print() {
								OrderPaymentResponse response = new OrderPaymentResponse();
								response.setOrder(order);
								IPPrintOrderWide po = new IPPrintOrderWide(response, true);
								return po.printIP(order, AppConfig.getState().getPrinterName(), AppConfig.getState().getPrinterIp());
							}
						});
					}
				} else if (printerProvider == PeripheralProvider.STAR.ordinal()) {
					new mPOPPrintOrderSlim(order, true);
				} else if (printerProvider == PeripheralProvider.VERIFONE.ordinal()) {
					Printer printer = PrinterFactory.getInstance().getPrinter();
					if (printer != null) {
						printer.printOrder(order, ReceiptPrintType.COPY);
					}
				}
			}
        });
    }

    private void closeDialog() {
        if(getDialog() != null) {
            getDialog().dismiss();
        }
    }

    private void setListOnClickListener() {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(selectedLines.contains(order.getLines().get(i))) {
                    selectedLines.remove(order.getLines().get(i));
                } else {
                    selectedLines.add(order.getLines().get(i));
                }
//                if(list.getChildAt(i).isSelected()) {
//                    list.getChildAt(i).setSelected(false);
//                    selectedLines.remove(order.getLines().get(i));
//                    Log.i("vilde", "Unselect");
//                } else {
//                    list.getChildAt(i).setSelected(true);
//                    selectedLines.add(order.getLines().get(i));
//                    Log.i("vilde", "Select");
//                }

            }
        });
    }

    private void setCartSelectedLines() {
        order.setLines(selectedLines);
        displayOrder(order);
    }

    void displayOrder(Order o) {
        if (o != null && MainActivity.getInstance().getCartFragment() != null && MainActivity.getInstance().getNumpadScanFragment() != null) {
            Cart.INSTANCE.setOrder(o);
            MainTopBarMenu.getInstance().toggleScanView();
        }
        parentDialog.dismiss();
    }

    private void returnSelectedLines() {
        for(OrderLine ol : selectedLines) {
            ol.setQuantity(ol.getQuantity().multiply(Decimal.NEGATIVE_ONE));
        }
        order.setLines(selectedLines);
        displayOrder(order);
    }

    public void setup(OrderSearchDialog osd, Order o) {
        this.parentDialog = osd;
        this.order = o;
    }

    private void selectAllLines() {
        if(list == null) {
            Log.i("vilde", "List was null...");
            return;
        }
		selectedLines = new ArrayList<>();
        for(int i = 0; i < list.getChildCount(); i++) {
            list.getChildAt(i).callOnClick();
            list.getChildAt(i).setSelected(true);
			selectedLines.add(order.getLines().get(i));
        }
    }
}
