package no.susoft.mobile.pos.ui.adapter.utils;

import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.ListView;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.data.OrderLine;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.adapter.SplitOrderOrderListAdapter;

import java.util.List;

public class OrderItemOnDragListener implements View.OnDragListener {

    Order me;

    public OrderItemOnDragListener(Order i) {
        me = i;
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                Log.i("vilde", "Item ACTION_DRAG_STARTED: " + "\n");
                break;
            case DragEvent.ACTION_DRAG_ENTERED:
                Log.i("vilde", "Item ACTION_DRAG_ENTERED: " + "\n");
                break;
            case DragEvent.ACTION_DRAG_EXITED:
                Log.i("vilde", "Item ACTION_DRAG_EXITED: " + "\n");
                break;
            case DragEvent.ACTION_DROP:
                Log.i("vilde", "Item ACTION_DROP: " + "\n");

                Log.i("vilde", "dropping whole order here");

                if (event.getLocalState().getClass().equals(OrderPassObject.class)) {
                    try {
                        OrderPassObject passObj = (OrderPassObject) event.getLocalState();
                        View view = passObj.view;

                        if (!v.equals(view)) {

                            Order passedItem = passObj.item;

                            ListView newParent = (ListView) v.getParent();
                            SplitOrderOrderListAdapter destAdapter = (SplitOrderOrderListAdapter) (newParent.getAdapter());
                            List<Order> destList = destAdapter.getList();

                            MainActivity.getInstance().getCartFragment().getSplitOrderDialog().moveAllLinesFromOrderToOrder(passedItem, destList.get(destList.indexOf(me)));

                            destAdapter.notifyDataSetChanged();
                        }
                    } catch (Exception ex) {

                    }
                } else if (event.getLocalState().getClass().equals(OrderLinePassObject.class)) {
                    try {
                        OrderLinePassObject passObj = (OrderLinePassObject) event.getLocalState();
                        View view = passObj.view;

                        if (!v.equals(view)) {
                            OrderLine passedItem = passObj.item;
                            //ListView oldParent = (ListView) view.getParent();
                            List<OrderLine> destList = me.getLines();

                            //moveWholeLineFromMainToOrderInOrderList(oldParent, destList, passedItem);
                            MainActivity.getInstance().getCartFragment().getSplitOrderDialog().moveOneFromMainOrderToOrderListOrder(destList, passedItem);

                            MainActivity.getInstance().getCartFragment().getSplitOrderDialog().updateAllAdaptersAndFooters();
                        }
                    } catch (Exception ex) {
                    }
                }

                break;
            case DragEvent.ACTION_DRAG_ENDED:
                Log.i("vilde", "Item ACTION_DRAG_ENDED: " + "\n");
            default:
                break;
        }
        return true;
    }

    private boolean removeItemToList(List<OrderLine> l, OrderLine it) {
        boolean result = l.remove(it);
        return result;
    }

}