package no.susoft.mobile.pos.ui.slidemenu;

/**
 * Group header menu item implementation.
 *
 * Created by Vikram.
 */
public class SublimeGroupHeaderMenuItem extends SublimeBaseMenuItem {

    public SublimeGroupHeaderMenuItem(SublimeMenu menu, int group,
                                      int id, CharSequence title,
                                      CharSequence hint,
                                      boolean valueProvidedAsync, boolean showsIconSpace) {
        super(menu, group, id, title, hint, ItemType.GROUP_HEADER, valueProvidedAsync, showsIconSpace);
    }

    public SublimeGroupHeaderMenuItem(int group,
                                      int id, CharSequence title,
                                      CharSequence hint,
                                      int iconResId,
                                      boolean valueProvidedAsync, boolean showsIconSpace,
                                      int flags) {
        super(group, id, title, hint, iconResId, ItemType.GROUP_HEADER,
                valueProvidedAsync, showsIconSpace, flags);
    }

    @Override
    public boolean invoke() {
        return invokeChevron();
    }

    public boolean invokeGroupHeader() {
        return invoke(OnNavigationMenuEventListener.Event.GROUP_HEADER_CLICKED, this);
    }

    public boolean invokeChevron() {
        SublimeGroup group = getMenu().getGroup(getGroupId());

        if (group == null) return false;

        if (group.isCollapsible()) {
            group.setStateCollapsed(!group.isCollapsed());
        }

        return invoke(group.isCollapsed() ?
                OnNavigationMenuEventListener.Event.GROUP_COLLAPSED
                : OnNavigationMenuEventListener.Event.GROUP_EXPANDED, this);
    }
}
