package no.susoft.mobile.pos.ui.slidemenu;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ProgressBar;
import no.susoft.mobile.pos.R;

/**
 * View implementation for Text with Badge menu item.
 *
 * Created by Vikram.
 */
public class SublimeTextWithBadgeItemView extends SublimeBaseItemView {
    private StateAwareTextView mBadgeView;
    private ProgressBar mProgress;

    public SublimeTextWithBadgeItemView(Context context) {
        this(context, null);
    }

    public SublimeTextWithBadgeItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SublimeTextWithBadgeItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context)
                .inflate(R.layout.sublime_menu_text_with_badge_item_content, this, true);
        initializeViews();
    }

    @Override
    protected void initializeViews() {
        super.initializeViews();
        mBadgeView = (StateAwareTextView) findViewById(R.id.badge);
        mProgress = (ProgressBar) findViewById(R.id.progress);
    }

    @Override
    public void initialize(SublimeBaseMenuItem itemData, SublimeThemer themer) {
        super.initialize(itemData, themer);

        TextViewStyleProfile badgeStyleProfile = themer.getBadgeStyleProfile();
        setBadgeTextColor(badgeStyleProfile.getTextColor());
        if (badgeStyleProfile.getTypeface() != null) {
            setBadgeTypeface(badgeStyleProfile.getTypeface(),
                    badgeStyleProfile.getTypefaceStyle());
        } else {
            setBadgeTypefaceStyle(badgeStyleProfile.getTypefaceStyle());
        }

        if (itemData.providesValueAsync()) {
            mBadgeView.setVisibility(GONE);
            mProgress.setVisibility(VISIBLE);
        } else {
            mProgress.setVisibility(GONE);
            mBadgeView.setVisibility(VISIBLE);
            mBadgeView.setText(((SublimeTextWithBadgeMenuItem) itemData).getBadgeText());
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mBadgeView.setEnabled(enabled);
        mProgress.setEnabled(enabled);
    }

    public void setBadgeTextColor(ColorStateList textColor) {
        mBadgeView.setTextColor(textColor);
    }

    public void setBadgeTypeface(Typeface typeface, int typefaceStyle) {
        mBadgeView.setTypeface(typeface, typefaceStyle);
    }

    public void setBadgeTypefaceStyle(int typefaceStyle) {
        mBadgeView.setTypeface(mBadgeView.getTypeface(), typefaceStyle);
    }

    @Override
    public void setItemChecked(boolean checked) {
        super.setItemChecked(checked);
        mBadgeView.setItemChecked(checked);
    }
}
