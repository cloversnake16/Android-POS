package no.susoft.mobile.pos.hardware.printer.printertext;

/**
 * Created by VSB on 11/08/2016.
 */
public class ActionsBixolon implements IPrintActions {

    char ESC = (char)27; //Ascii character for ESCAPE

    @Override
    public String cutPaper() {
        return (ESC+"i");
    }

    @Override
    public String feedLines(int i) {
        return (ESC+"J"+i);
    }

    @Override
    public String setLargeText() {
        return null;
    }

    @Override
    public String setNormalSizeText() {
        return null;
    }
}
