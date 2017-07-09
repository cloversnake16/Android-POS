package no.susoft.mobile.pos.discount;

import no.susoft.mobile.pos.data.DiscountReason;
import no.susoft.mobile.pos.json.JSONSerializable;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to package discount reasons to be serialized and deserialized to and from a source and destination.
 *
 * @author Yesod
 */
public final class DiscountReasonContainer implements JSONSerializable {

    private final List<DiscountReason> reasons;

    /**
     * Builds with an empty list.
     */
    public DiscountReasonContainer() {
        this.reasons = new ArrayList<DiscountReason>();
    }

    /**
     * Get the discount reasons.
     *
     * @return
     */
    public List<DiscountReason> getReasons() {
        return this.reasons;
    }
}