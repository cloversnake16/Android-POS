package no.susoft.mobile.pos.ui.adapter.utils;

import android.view.View;
import no.susoft.mobile.pos.data.Area;
import no.susoft.mobile.pos.data.Table;

public class TablePassObject {

    public View view;
    public Table item;
    public Area area;

    public TablePassObject(View v, Table t, Area a) {
        view = v;
        item = t;
        area = a;
    }

}
