package no.susoft.mobile.pos.data;

import no.susoft.mobile.pos.json.JSONSerializable;

public class QuickLaunchMenuCell implements JSONSerializable {

    private long id;
    private long parentGridId;
    private int idx;
    private String text;
    private String productId;
    private String barcode;
    private long childGridId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getParentGridId() {
        return parentGridId;
    }

    public void setParentGridId(long parentGridId) {
        this.parentGridId = parentGridId;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public long getChildGridId() {
        return childGridId;
    }

    public void setChildGridId(long childGridId) {
        this.childGridId = childGridId;
    }
}
