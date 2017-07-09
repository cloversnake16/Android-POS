package no.susoft.mobile.pos.ui.fragment.utils;

import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.ui.fragment.CartFragment;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

/**
 * Created by Vilde on 29.02.2016.
 */
public class CartPersistingOrders {

    // PERSISTING ORDER STUFF
    private Order previousOrder;
    private HashMap<String, ArrayList<Order>> accountOrderMap = new HashMap<>();
    private HashMap<String, Integer> accountOrderTableMap = new HashMap<>();
    private HashMap<String, Integer> accountOrderAreaMap = new HashMap<>();



    public void deleteOrderForThisAccount(String accountId) {
        accountOrderMap.remove(accountId);
        accountOrderTableMap.remove(accountId);
        accountOrderAreaMap.remove(accountId);
    }


    public Order getPreviousOrder() {
        return previousOrder;
    }

    public void setPreviousOrder(Order previousOrder) {
        this.previousOrder = previousOrder;
    }

    public void removeOrderFromMaps(String userId) {
        accountOrderAreaMap.remove(userId);
        accountOrderTableMap.remove(userId);
        accountOrderMap.remove(userId);
    }

    public void addOrders(String userId, ArrayList<Order> orders) {
        accountOrderMap.put(userId, orders);
    }

    public void addArea(String userId, int selectedArea) {
        accountOrderAreaMap.put(userId, selectedArea);
    }

    public void addTable(String userId, int selectedTable) {
        accountOrderTableMap.put(userId, selectedTable);
    }

    public ArrayList<Order> getOrderAt(String userId) {
        return accountOrderMap.get(userId);
    }

    public Integer getTableFor(String userId) {
        if(accountOrderTableMap.get(userId) != null) {
            return accountOrderTableMap.get(userId);
        }
        return null;
    }

    public Integer getAreaFor(String userId) {
        if(accountOrderAreaMap.get(userId) != null) {
            return accountOrderAreaMap.get(userId);
        }
        return null;
    }
}
