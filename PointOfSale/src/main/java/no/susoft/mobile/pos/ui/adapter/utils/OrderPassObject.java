package no.susoft.mobile.pos.ui.adapter.utils;

import android.view.View;
import no.susoft.mobile.pos.data.Order;

import java.util.List;

public class OrderPassObject {

    public View view;
    public Order item;
    public List<Order> srcList;

    public OrderPassObject(View v, Order i, List<Order> s) {
        view = v;
        item = i;
        srcList = s;
    }

}
