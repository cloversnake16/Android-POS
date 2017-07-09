package no.susoft.mobile.pos.ui.slidemenu;

import android.os.Bundle;

/**
 * Text with Badge menu item implementation.
 *
 * Created by Vikram.
 */
public class SublimeTextWithBadgeMenuItem extends SublimeBaseMenuItem {

    private static final String SS_BADGE_TEXT = "ss.badge.text";

    private CharSequence mBadgeText;

    public SublimeTextWithBadgeMenuItem(SublimeMenu menu, int group, int id,
                                        CharSequence title, CharSequence hint,
                                        boolean valueProvidedAsync,
                                        CharSequence badgeText, boolean showsIconSpace) {
        super(menu, group, id, title, hint, ItemType.BADGE, valueProvidedAsync, showsIconSpace);
        mBadgeText = badgeText;
    }

    // Restores state
    public SublimeTextWithBadgeMenuItem(int group, int id,
                                        CharSequence title, CharSequence hint,
                                        int iconResId,
                                        boolean valueProvidedAsync,
                                        CharSequence badgeText, boolean showsIconSpace,
                                        int flags) {
        super(group, id, title, hint, iconResId, ItemType.BADGE, valueProvidedAsync,
                showsIconSpace, flags);
        mBadgeText = badgeText;
    }

    static SublimeTextWithBadgeMenuItem createFromBundle(Bundle bundle, int group, int id,
                                                         CharSequence title, CharSequence hint,
                                                         int iconResId,
                                                         boolean valueProvidedAsync,
                                                         boolean showsIconSpace, int flags) {
        String badgeText = bundle.getString(SS_BADGE_TEXT);
        return new SublimeTextWithBadgeMenuItem(group, id, title, hint, iconResId,
                valueProvidedAsync, badgeText, showsIconSpace, flags);
    }

    @Override
    public boolean invoke() {
        return invoke(OnNavigationMenuEventListener.Event.CLICKED, this);
    }

    /**
     * Set/change badge text.
     *
     * @param badgeText The text that should be displayed as the badge.
     * @return This {@link SublimeTextWithBadgeMenuItem} for chaining.
     */
    public SublimeTextWithBadgeMenuItem setBadgeText(CharSequence badgeText) {
        mBadgeText = badgeText;
        attemptItemUpdate();

        return this;
    }

    /**
     * Returns the text that should be displayed as the badge.
     *
     * @return text to display as the badge.
     */
    public CharSequence getBadgeText() {
        return mBadgeText;
    }
}
