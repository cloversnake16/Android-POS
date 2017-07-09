package no.susoft.mobile.pos.ui.slidemenu;

/**
 * Text menu item implementation.
 *
 * Created by Vikram.
 */
public class SublimeTextMenuItem extends SublimeBaseMenuItem {

    private static final String TAG = SublimeTextMenuItem.class.getSimpleName();

    public SublimeTextMenuItem(SublimeMenu menu, int group, int id,
                               CharSequence title, CharSequence hint,
                               boolean valueProvidedAsync, boolean showsIconSpace) {
        super(menu, group, id, title, hint, ItemType.TEXT, valueProvidedAsync, showsIconSpace);
    }

    public SublimeTextMenuItem(int group, int id,
                               CharSequence title, CharSequence hint,
                               int iconResId,
                               boolean valueProvidedAsync, boolean showsIconSpace,
                               int flags) {
        super(group, id, title, hint, iconResId, ItemType.TEXT,
                valueProvidedAsync, showsIconSpace, flags);
    }

    @Override
    public boolean invoke() {
        return invoke(OnNavigationMenuEventListener.Event.CLICKED, this);
    }
}
