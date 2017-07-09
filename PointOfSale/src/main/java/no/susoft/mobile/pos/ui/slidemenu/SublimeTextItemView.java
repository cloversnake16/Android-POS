package no.susoft.mobile.pos.ui.slidemenu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import no.susoft.mobile.pos.R;

/**
 * View implementation for Text menu item.
 *
 * Created by Vikram.
 */
public class SublimeTextItemView extends SublimeBaseItemView {

    public SublimeTextItemView(Context context) {
        this(context, null);
    }

    public SublimeTextItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SublimeTextItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.sublime_menu_text_item_content, this, true);
        initializeViews();
    }

    @Override
    protected void initializeViews() {
        super.initializeViews();
    }

    @Override
    public void initialize(SublimeBaseMenuItem itemData, SublimeThemer themer) {
        super.initialize(itemData, themer);
    }
}
