package no.susoft.mobile.pos.ui.activity.util;

public class NavigationMenuItem {

    NavigationEnum id;
    String title;

    public NavigationMenuItem(NavigationEnum id, String title) {
        this.id = id;
        this.title = title;
    }

    public String toString() {
        return title;
    }

    public NavigationEnum getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
