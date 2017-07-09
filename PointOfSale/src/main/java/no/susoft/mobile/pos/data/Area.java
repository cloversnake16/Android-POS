package no.susoft.mobile.pos.data;

import no.susoft.mobile.pos.json.JSONSerializable;

import java.util.ArrayList;
import java.util.List;

public class Area implements JSONSerializable {

    String shopId;
    int id;
    String name;
    List<Table> tables = new ArrayList<>();

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }
}
