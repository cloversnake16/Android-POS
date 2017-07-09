package no.susoft.mobile.pos.ui.slidemenu;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import no.susoft.mobile.pos.R;

/**
 * Sub-classed ImageView that responds to custom states.
 */
public class StateAwareImageView extends ImageView {

    // Drawable state set - checked
    private static final int[] CHECKED_STATE_SET = {
            R.attr.state_item_checked
    };

    // Keeps track of checked state
    private boolean mChecked;

    public StateAwareImageView(Context context) {
        this(context, null);
    }

    public StateAwareImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StateAwareImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Sets the checked state for this view.
     *
     * @param checked current state
     */
    void setItemChecked(boolean checked) {
        if (checked != mChecked) {
            mChecked = checked;
            refreshDrawableState();
        }
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (mChecked) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }

        return drawableState;
    }
}
