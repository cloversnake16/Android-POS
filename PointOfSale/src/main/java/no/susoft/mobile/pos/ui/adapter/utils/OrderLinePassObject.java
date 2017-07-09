package no.susoft.mobile.pos.ui.adapter.utils;

import android.view.View;
import no.susoft.mobile.pos.data.OrderLine;

import java.util.List;

public class OrderLinePassObject {

    public View view;
    public OrderLine item;
    public List<OrderLine> srcList;

    public OrderLinePassObject(View v, OrderLine i, List<OrderLine> s) {
        view = v;
        item = i;
        srcList = s;
    }
}
