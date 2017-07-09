package no.susoft.mobile.pos.discount;

import android.content.Context;
import android.widget.ArrayAdapter;
import no.susoft.mobile.pos.data.DiscountReason;

import java.util.List;

/**
 * This is a list accountAdapter for discount reasons.
 *
 * @author Yesod
 */
public final class DiscountReasonAdapter extends ArrayAdapter<DiscountReason> {

    // The discount reason container.
    private final DiscountReasonContainer container;

    /**
     * @param context
     */
    public DiscountReasonAdapter(final Context context) {
        this(context, DiscountReasonManager.INSTANCE.getDiscountReasons());
    }

    /**
     * This constructor enforces that this accountAdapter is only populated by {@link no.susoft.mobile.discount.DiscountReasonManager#getDiscountReasons()}
     */
    private DiscountReasonAdapter(final Context context, final DiscountReasonContainer container) {
        super(context, android.R.layout.simple_spinner_dropdown_item, container.getReasons());
        // This should never be null.
        this.container = container;
    }

    /**
     * Attempt to return the discount reason at the given index, null otherwise.
     *
     * @return
     */
    public DiscountReason getReason(final int index) {
        try {
            return this.container.getReasons().get(index);
        } catch (Exception x) {
            return null;
        }
    }

    /**
     * Get the index of the first occurrence that matches the given discount reason, -1 otherwise.
     *
     * @param reason
     * @return
     */
    public int find(final DiscountReason reason) {
        final List<DiscountReason> reasons = this.container.getReasons();
        for (int index = 0; index < reasons.size(); ++index)
            if (reasons.get(index).matches(reason))
                return index;
        return -1;
    }
}