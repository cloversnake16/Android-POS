package no.susoft.mobile.pos.ui.slidemenu;

/**
 * Switch menu item implementation.
 */
public class SublimeSwitchMenuItem extends SublimeBaseMenuItem {

    public SublimeSwitchMenuItem(SublimeMenu menu, int group, int id,
                                 CharSequence title, CharSequence hint,
                                 boolean valueProvidedAsync, boolean showsIconSpace) {
        super(menu, group, id, title, hint, ItemType.SWITCH, valueProvidedAsync, showsIconSpace);
    }

    // Restores state
    public SublimeSwitchMenuItem(int group, int id,
                                 CharSequence title, CharSequence hint,
                                 int iconResId,
                                 boolean valueProvidedAsync, boolean showsIconSpace,
                                 int flags) {
        super(group, id, title, hint, iconResId, ItemType.SWITCH, valueProvidedAsync, showsIconSpace, flags);
    }

    @Override
    public boolean invoke() {
        if (isCheckable()) {
            setChecked(!isChecked());
            return invoke(isChecked() ?
                    OnNavigationMenuEventListener.Event.CHECKED
                    : OnNavigationMenuEventListener.Event.UNCHECKED, this);
        } else {
            return invoke(OnNavigationMenuEventListener.Event.CLICKED, this);
        }
    }
}
