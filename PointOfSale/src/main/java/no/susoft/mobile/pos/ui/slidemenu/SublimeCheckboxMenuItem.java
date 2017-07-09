package no.susoft.mobile.pos.ui.slidemenu;

/**
 * Checkbox menu item implementation.
 *
 * Created by Vikram.
 */
public class SublimeCheckboxMenuItem extends SublimeBaseMenuItem {

    public SublimeCheckboxMenuItem(SublimeMenu menu, int group, int id,
                                   CharSequence title, CharSequence hint,
                                   boolean valueProvidedAsync,
                                   boolean showsIconSpace) {
        super(menu, group, id, title, hint, ItemType.CHECKBOX, valueProvidedAsync, showsIconSpace);
    }

    // Restores state
    public SublimeCheckboxMenuItem(int group, int id,
                                   CharSequence title, CharSequence hint,
                                   int iconResId,
                                   boolean valueProvidedAsync,
                                   boolean showsIconSpace, int flags) {
        super(group, id, title, hint, iconResId,
                ItemType.CHECKBOX, valueProvidedAsync, showsIconSpace, flags);
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
