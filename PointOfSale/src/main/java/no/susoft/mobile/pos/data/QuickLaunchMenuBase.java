package no.susoft.mobile.pos.data;

import no.susoft.mobile.pos.json.JSONSerializable;

public class QuickLaunchMenuBase implements JSONSerializable {

    private long id;
    private String name;
    private long menuGridId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getMenuGridId() {
        return menuGridId;
    }

    public void setMenuGridId(long menuGridId) {
        this.menuGridId = menuGridId;
    }
}
