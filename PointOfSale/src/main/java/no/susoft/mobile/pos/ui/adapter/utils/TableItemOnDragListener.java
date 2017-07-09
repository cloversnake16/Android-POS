package no.susoft.mobile.pos.ui.adapter.utils;

import android.view.DragEvent;
import android.view.View;
import android.widget.Toast;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Table;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

public class TableItemOnDragListener implements View.OnDragListener {

    Table me;
    Table passedItem;
    Table newParent;

    public TableItemOnDragListener(Table t) {
        me = t;
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                break;
            case DragEvent.ACTION_DRAG_ENTERED:
                break;
            case DragEvent.ACTION_DRAG_EXITED:
                break;
            case DragEvent.ACTION_DROP:
                try {
                    TablePassObject passObj = (TablePassObject) event.getLocalState();
                    passedItem = passObj.item;

                    newParent = (Table) v.getTag();

                    if (passedItem != newParent) {
                        if((passedItem.getNumber() == Cart.INSTANCE.getSelectedTable() && passedItem.getAreaId() == Cart.INSTANCE.getSelectedArea()
                        || newParent.getNumber() == Cart.INSTANCE.getSelectedTable() && newParent.getAreaId() == Cart.INSTANCE.getSelectedArea())) {
                            Toast.makeText(MainActivity.getInstance(), R.string.cannot_merge_active_table, Toast.LENGTH_LONG).show();
                        } else {
                            MainActivity.getInstance().getServerCallMethods().updateOrdersTableNumbers(passedItem.getNumber(), passedItem.getAreaId(), newParent.getNumber(), newParent.getAreaId(), this);
                        }
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                break;
            case DragEvent.ACTION_DRAG_ENDED:
            default:
                break;
        }
        return true;
    }

    public void updateTableView() {
        newParent.setOrders(newParent.getOrders() + passedItem.getOrders());
        newParent.setAmount(newParent.getAmount() + passedItem.getAmount());
        passedItem.setOrders(0);
        passedItem.setAmount(0.0);
        MainActivity.getInstance().getCartFragment().getCartButtons().tableViewDialog.updateAdapters();
    }

}
