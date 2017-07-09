package no.susoft.mobile.pos.ui.slidemenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;
import no.susoft.mobile.pos.R;

/**
 * Sub-classed TextView that responds to custom states &amp; style attrs.
 */
public class StateAwareTextView extends TextView {

    public static final String TAG = StateAwareTextView.class.getSimpleName();

    // Drawable state set - checked
    private static final int[] CHECKED_STATE_SET = {
            R.attr.state_item_checked
    };

    // Keeps track of checked state
    private boolean mChecked;

    public StateAwareTextView(Context context) {
        this(context, null);
    }

    public StateAwareTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StateAwareTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.StateAwareTextView, defStyleAttr,
                0);

        try {

            // If user has provided a valid value
            // for @android:attr/textAppearance, it has already been
            // applied in our parent - TextView. Else, resolve & use the value
            // of @attr/saTextAppearance.
            if (a.hasValue(R.styleable.StateAwareTextView_android_textAppearance)) {
                int textAppearance = a.getResourceId(
                        R.styleable.StateAwareTextView_android_textAppearance, -1);

                if (textAppearance == -1) {
                    if (Config.DEBUG) {
                        Log.i(TAG, "textAppearance is -1");
                    }

                    int saTextAppearance = a.getResourceId(
                            R.styleable.StateAwareTextView_saTextAppearance,
                            R.style.SnvDefaultTextAppearance);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        setTextAppearance(saTextAppearance);
                    } else {
                        setTextAppearance(getContext(), saTextAppearance);
                    }
                } else {
                    if (Config.DEBUG) {
                        Log.i(TAG, "textAppearance is AVAILABLE");
                    }
                }
            }
        } finally {
            a.recycle();
        }
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
    protected int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (mChecked) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }

        return drawableState;
    }
}
