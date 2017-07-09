package no.susoft.mobile.pos.hardware.printer;

public class PrinterCommands {
	
    public static final byte[] left = new byte[]{0x1b, 0x61, 0x00};
    public static final byte[] center = new byte[]{0x1b, 0x61, 0x01};
    public static final byte[] right = new byte[]{0x1b, 0x61, 0x02};
    public static final byte[] bold = new byte[]{0x1b, 0x45, 0x01};
    public static final byte[] bold_cancel = new byte[]{0x1b, 0x45, 0x00};
    public static final byte[] text_normal_size = new byte[]{0x1d, 0x21, 0x00};
    public static final byte[] text_big_height = new byte[]{0x1b, 0x21, 0x10};
    public static final byte[] text_big_size = new byte[]{0x1d, 0x21, 0x11};
    public static final byte[] reset = new byte[]{0x1b, 0x40};
    public static final byte[] print = new byte[]{0x0a};
    public static final byte[] under_line = new byte[]{0x1b, 0x2d, 2};
    public static final byte[] under_line_cancel = new byte[]{0x1b, 0x2d, 0};
	
    public static byte[] walkPaper(byte n) {
        return new byte[]{0x1b, 0x64, n};
    }
	
    public static byte[] move(byte x, byte y) {
        return new byte[]{0x1d, 0x50, x, y};
    }

}