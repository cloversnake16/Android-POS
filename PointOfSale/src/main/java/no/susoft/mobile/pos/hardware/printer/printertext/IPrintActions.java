package no.susoft.mobile.pos.hardware.printer.printertext;

/**
 * Created by VSB on 11/08/2016.
 */
public interface IPrintActions {
    String cutPaper();
    String feedLines(int i);
    String setLargeText();
    String setNormalSizeText();

}
