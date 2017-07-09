package no.susoft.mobile.pos.ui.slidemenu;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Currently inert.
 *
 * Created by Vikram.
 */
public class SublimeNavMenuView extends RecyclerView {
    public SublimeNavMenuView(Context context) {
        this(context, null);
    }

    public SublimeNavMenuView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.listViewStyle);
    }

    public SublimeNavMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
