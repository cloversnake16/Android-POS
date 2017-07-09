package no.susoft.mobile.pos.usbdisplay;

/**
 * Created by Sami on 12/16/2016.
 */

public class UsbDisplayConstants {
    public static final int USB_TIMEOUT_IN_MS                 = 500;
    public static final int BUFFER_SIZE_IN_BYTES              = 57600;
    public static final String DEVICE_EXTRA_KEY               = "usb_device";
    public static final String ACTION_DEVICE_DETACHED         = "android.hardware.usb.action.USB_DEVICE_DETACHED";

    public static final String USB_PROTOCOL_MANUFACTURER      = "Susoft";
    public static final String USB_PROTOCOL_MODEL             = "Usb-Display";
    public static final String USB_PROTOCOL_DESCRIPTION       = "Communication with a display";
    public static final String USB_PROTOCOL_VERSION           = "0.1";
    public static final String USB_PROTOCOL_URI               = "http://susoft.com";
    public static final String USB_PROTOCOL_SERIAL            = "42";

}
