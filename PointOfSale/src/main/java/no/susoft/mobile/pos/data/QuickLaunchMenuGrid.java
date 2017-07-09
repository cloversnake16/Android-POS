package no.susoft.mobile.pos.data;

import no.susoft.mobile.pos.json.JSONSerializable;

import java.util.List;

public class QuickLaunchMenuGrid implements JSONSerializable {

    private long id;
    private int sizeX;
    private int sizeY;
    private long parentCellId;
    private List<QuickLaunchMenuCell> cells;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getSizeX() {
        return sizeX;
    }

    public void setSizeX(int sizeX) {
        this.sizeX = sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public void setSizeY(int sizeY) {
        this.sizeY = sizeY;
    }

    public long getParentCellId() {
        return parentCellId;
    }

    public void setParentCellId(long parentCellId) {
        this.parentCellId = parentCellId;
    }

    public List<QuickLaunchMenuCell> getCells() {
        return cells;
    }

    public void setCells(List<QuickLaunchMenuCell> cells) {
        this.cells = cells;
    }
}
