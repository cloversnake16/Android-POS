package no.susoft.mobile.pos.ui.adapter.utils;

import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.ListView;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.data.OrderLine;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.adapter.CartOrderLinesListDragDropAdapter;

import java.util.List;

public class OrderLineItemOnDragListener implements View.OnDragListener {

    OrderLine me;

    public OrderLineItemOnDragListener(OrderLine i) {
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

                try {
                    OrderLinePassObject passObj = (OrderLinePassObject) event.getLocalState();
                    View view = passObj.view;
                    OrderLine passedItem = passObj.item;
                    List<OrderLine> srcList = passObj.srcList;
                    ListView oldParent = (ListView) view.getParent();
                    CartOrderLinesListDragDropAdapter srcAdapter = (CartOrderLinesListDragDropAdapter) (oldParent.getAdapter());

                    ListView newParent = (ListView) v.getParent();
                    CartOrderLinesListDragDropAdapter destAdapter = (CartOrderLinesListDragDropAdapter) (newParent.getAdapter());
                    List<OrderLine> destList = destAdapter.getList();

                    //if moving to another list, make sure it only passes 1 qty and not the whole line
                    if (srcList != destList) {
                        MainActivity.getInstance().getCartFragment().getSplitOrderDialog().moveOneOrderLineQtyFromListToList(oldParent, passedItem);
                    }
                    //else it is reordering in the same list and we want to move the whole item
                    else {
                        int removeLocation = srcList.indexOf(passedItem);
                        int insertLocation = destList.indexOf(me);

                        if (removeLocation != insertLocation) {
                            if (removeItemToList(srcList, passedItem)) {
                                destList.add(insertLocation, passedItem);
                            }
                        }
                    }

                    srcAdapter.notifyDataSetChanged();
                    destAdapter.notifyDataSetChanged();
                } catch (Exception ex) {

                } finally {
                    try {
                        Log.i("vilde", "tried pass whole order");
                        OrderPassObject passObj = (OrderPassObject) event.getLocalState();
                        View view = passObj.view;
                        Order passedItem = passObj.item;

                        ListView oldParent = (ListView) view.getParent();
                        CartOrderLinesListDragDropAdapter srcAdapter = (CartOrderLinesListDragDropAdapter) (oldParent.getAdapter());

                        LinearLayoutListView newParent = (LinearLayoutListView) v;
                        CartOrderLinesListDragDropAdapter destAdapter = (CartOrderLinesListDragDropAdapter) (newParent.listView.getAdapter());

                        MainActivity.getInstance().getCartFragment().getSplitOrderDialog().moveAllLinesFromOrderToMainOrder(passedItem);

                        srcAdapter.notifyDataSetChanged();
                        destAdapter.notifyDataSetChanged();

                        MainActivity.getInstance().getCartFragment().getSplitOrderDialog().updateFootersWithTotals();

                        newParent.listView.smoothScrollToPosition(destAdapter.getCount() - 1);

                    } catch (Exception ex) {
                        Log.i("vilde", "tried pass whole order and failed");
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