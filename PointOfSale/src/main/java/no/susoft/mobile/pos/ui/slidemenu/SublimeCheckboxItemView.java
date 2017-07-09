package no.susoft.mobile.pos.ui.slidemenu;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import no.susoft.mobile.pos.R;

/**
 * View implementation for Checkbox menu item.
 */
public class SublimeCheckboxItemView extends SublimeBaseItemView {

    private CheckBox mCheckbox;

    public SublimeCheckboxItemView(Context context) {
        this(context, null);
    }

    public SublimeCheckboxItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SublimeCheckboxItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.sublime_menu_checkbox_item_content, this, true);
        initializeViews();
    }

    @Override
    protected void initializeViews() {
        super.initializeViews();
        mCheckbox = (CheckBox) findViewById(R.id.checkbox_ctrl);
    }

    @Override
    public void initialize(SublimeBaseMenuItem itemData, SublimeThemer themer) {
        setCheckableItemTintList(themer.getCheckableItemTintList());
        super.initialize(itemData, themer);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mCheckbox.setEnabled(enabled);
    }

    @Override
    public void setItemChecked(boolean checked) {
        super.setItemChecked(checked);
        mCheckbox.setChecked(checked);
    }

    public void setCheckableItemTintList(ColorStateList checkableItemTintList) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCheckbox.setButtonTintList(checkableItemTintList);
        } else {
            Drawable dCheckbox = getResources().getDrawable(R.drawable.checkbox_pre_lollipop);

            if (dCheckbox != null) {
                dCheckbox = DrawableCompat.wrap(dCheckbox);
                DrawableCompat.setTintList(dCheckbox, checkableItemTintList);
                mCheckbox.setButtonDrawable(dCheckbox);
            }
        }
    }
}
