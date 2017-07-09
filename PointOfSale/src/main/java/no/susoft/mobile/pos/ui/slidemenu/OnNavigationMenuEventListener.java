package no.susoft.mobile.pos.ui.slidemenu;

/**
 * Listener for Menu events
 *
 * GROUP_HEADER_CLICKED added:
 * This is invoked if {@link SublimeMenu#mExpandCollapseGroupOnlyOnChevronClick} is true
 * and the user has clicked the Group Header, but not the expand/collapse chevron.
 */
public interface OnNavigationMenuEventListener {

    // actions

    // GROUP_HEADER_CLICKED added
    // This is invoked if {@link SublimeMenu#mExpandCollapseGroupOnlyOnChevronClick}
    enum Event {
        CLICKED, CHECKED, UNCHECKED, GROUP_EXPANDED, GROUP_COLLAPSED, GROUP_HEADER_CLICKED
    }

    boolean onNavigationMenuEvent(Event event, SublimeBaseMenuItem menuItem);
}
