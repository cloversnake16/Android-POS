package no.susoft.mobile.pos.ui.slidemenu;

/**
 * Separator menu item implementation.
 *
 * Created by Vikram.
 */
public class SublimeSeparatorMenuItem extends SublimeBaseMenuItem {
    private static final String EMPTY_STRING = "";

    public SublimeSeparatorMenuItem(SublimeMenu menu, int group, int id) {
        super(menu, group, id, EMPTY_STRING, EMPTY_STRING, ItemType.SEPARATOR, false, false);
    }

    // Restores state //
    public SublimeSeparatorMenuItem(int group, int id) {
        super(group, id, EMPTY_STRING, EMPTY_STRING, NO_ICON,
                ItemType.SEPARATOR, false, false, ENABLED);
    }

    @Override
    public boolean invoke() {
        return false;
    }
}
