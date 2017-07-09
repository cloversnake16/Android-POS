package no.susoft.mobile.pos.ui.activity.util;

import android.content.Context;
import android.content.SharedPreferences;
import no.susoft.mobile.pos.SusoftPOSApplication;
import no.susoft.mobile.pos.data.PeripheralDevice;
import no.susoft.mobile.pos.data.PointOfSale;
import no.susoft.mobile.pos.ui.activity.MainActivity;

/**
 * Created by Vilde on 14.12.2015.
 */
public class AppConfig {

    private static AppConfig INSTANCE;
    private boolean usingDallasKey;
    private boolean adminUsingCredentials;
    private boolean restaurant = false;
    private boolean workshop = false;
    private PointOfSale pos;
    private boolean posAcceptPayment = true;
    private String receiptHeader;
    private String receiptFooter;
    private String orderNote;
    SharedPreferences preferences = SusoftPOSApplication.getContext().getSharedPreferences(AppConfig.class.toString(), Context.MODE_PRIVATE);


    public static AppConfig getState() {
        if(INSTANCE == null) {
            INSTANCE = new AppConfig();
        }

        return INSTANCE;
    }


    public void setAdminLoggingInWithCredentials(boolean state) {
        if (isUsingDallasKey()) {
            adminUsingCredentials = state;
        }
    }

    public boolean isAdminLoggingInWithCredentials() {
        if (isUsingDallasKey()) {
            return adminUsingCredentials;
        } else {
            return false;
        }
    }

    public void setPos(PointOfSale pos) {
        this.pos = pos;
    }

    public PointOfSale getPos() {
        return pos;
    }
    public void setUsingDallasKey(boolean usingDallasKey) {
        this.usingDallasKey = usingDallasKey;
    }

    public boolean isUsingDallasKey() {
        return usingDallasKey;
    }

    public boolean isRestaurant() {
        return restaurant;
    }

    public void setRestaurant(boolean restaurant) {
        this.restaurant = restaurant;
        if (restaurant) {
            if (MainActivity.getInstance().getCartFragment() != null && MainActivity.getInstance().getCartFragment().isAdded()) {
                MainActivity.getInstance().getCartFragment().getCartButtons().setupRestaurantSettings(true);
            }
            if (MainActivity.getInstance().getCartFragment() != null && MainActivity.getInstance().getCartFragment().getCartButtons() != null) {
                MainActivity.getInstance().getCartFragment().getCartButtons().setupRestaurantSettings(true);
            }
        } else {
            if (MainActivity.getInstance().getCartFragment() != null && MainActivity.getInstance().getCartFragment().isAdded()) {
                MainActivity.getInstance().getCartFragment().getCartButtons().setupRestaurantSettings(false);
            }
            if (MainActivity.getInstance().getCartFragment() != null && MainActivity.getInstance().getCartFragment().getCartButtons() != null) {
                MainActivity.getInstance().getCartFragment().getCartButtons().setupRestaurantSettings(false);
            }
        }
    }

	public boolean isWorkshop() {
		return workshop;
	}

	public void setWorkshop(boolean workshop) {
		this.workshop = workshop;
	}

	public void setPosAcceptPayment(boolean posAcceptPayment) {
        this.posAcceptPayment = posAcceptPayment;
    }

    public boolean getPosAcceptPayment(){
        return this.posAcceptPayment;
    }

	public String getReceiptHeader() {
		return receiptHeader;
	}

	public void setReceiptHeader(String receiptHeader) {
		this.receiptHeader = receiptHeader;
	}

	public String getReceiptFooter() {
		return receiptFooter;
	}

	public void setReceiptFooter(String receiptFooter) {
		this.receiptFooter = receiptFooter;
	}

	public String getOrderNote() {
		return orderNote;
	}

	public void setOrderNote(String orderNote) {
		this.orderNote = orderNote;
	}

	public int getPrinterTypeOrdinal() {
        return preferences.getInt("PRINTER", -1);
    }

    public int getPrinterProviderOrdinal() {
        SharedPreferences preferences = SusoftPOSApplication.getContext().getSharedPreferences(PeripheralDevice.class.toString(), Context.MODE_PRIVATE);
        return preferences.getInt("PRINTER_PROVIDER", -1);
    }

    public String getPrinterName() {
        SharedPreferences preferences = SusoftPOSApplication.getContext().getSharedPreferences(PeripheralDevice.class.toString(), Context.MODE_PRIVATE);
        return preferences.getString("PRINTER_NAME", "");
    }

    public String getPrinterIp() {
        SharedPreferences preferences = SusoftPOSApplication.getContext().getSharedPreferences(PeripheralDevice.class.toString(), Context.MODE_PRIVATE);
        return preferences.getString("PRINTER_IP", "");
    }

    public String getKitchenPrinterName() {
        SharedPreferences preferences = SusoftPOSApplication.getContext().getSharedPreferences(PeripheralDevice.class.toString(), Context.MODE_PRIVATE);
        return preferences.getString("KITCHEN_PRINTER_NAME", "");
    }

    public String getKitchenPrinterIp() {
        SharedPreferences preferences = SusoftPOSApplication.getContext().getSharedPreferences(PeripheralDevice.class.toString(), Context.MODE_PRIVATE);
        return preferences.getString("KITCHEN_PRINTER_IP", "");
    }

    public String getBarPrinterName() {
        SharedPreferences preferences = SusoftPOSApplication.getContext().getSharedPreferences(PeripheralDevice.class.toString(), Context.MODE_PRIVATE);
        return preferences.getString("BAR_PRINTER_NAME", "");
    }

    public String getBarPrinterIp() {
        SharedPreferences preferences = SusoftPOSApplication.getContext().getSharedPreferences(PeripheralDevice.class.toString(), Context.MODE_PRIVATE);
        return preferences.getString("BAR_PRINTER_IP", "");

    }

    public int getScannerProviderOrdinal() {
        SharedPreferences preferences = SusoftPOSApplication.getContext().getSharedPreferences(PeripheralDevice.class.toString(), Context.MODE_PRIVATE);
        return preferences.getInt("SCANNER_PROVIDER", -1);
    }

    public String getDisplayName() {
        SharedPreferences preferences = SusoftPOSApplication.getContext().getSharedPreferences(PeripheralDevice.class.toString(), Context.MODE_PRIVATE);
        return preferences.getString("DISPLAY_NAME", "");
    }


}
