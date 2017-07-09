package no.susoft.mobile.pos.hardware.printer;

/**
 * Created on 5/26/2016.
 */
public class VerifonePimHelper {

    public static final String PRINT_PREFIX_LARGE_FONT = "\u001B\u001D";
    public static final String PRINT_PREFIX_MEDIUM_FONT = ""; // fake
    public static final String PRINT_PREFIX_SMALL_FONT = "\u001B\u0021";
    public static final String PRINT_PREFIX_ALIGN_CENTER = "\u001B\u0017";
    public static final String PRINT_PREFIX_ALIGN_RIGHT = "\u001B\u0018";
    public static final String PRINT_PREFIX_QR_CODE = "\u001B\u001E";
    public static final String PRINT_PREFIX_BARCODE = "\u001B\u001F";
    public static final String PRINT_NEWLINE = "\n";
    public static final String PRINT_0000 = "\u0000";

    // Large font (21 chars pr line)
    public static final int LARGE_FONT_WIDTH = 21;
    // Medium font (normal size, 24 chars pr line)
    public static final int MEDIUM_FONT_WIDTH = 24;
    // Small font (42 chars pr line)
    public static final int SMALL_FONT_WIDTH = 42;
}
