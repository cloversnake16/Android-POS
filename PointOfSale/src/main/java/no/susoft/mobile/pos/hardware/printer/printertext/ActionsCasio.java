package no.susoft.mobile.pos.hardware.printer.printertext;

/**
 * Created by VSB on 11/08/2016.
 */
public class ActionsCasio implements IPrintActions {

    public String feedLines(int i) {
        return("\u001b|" + i + "lF");   //feed 4line
    }

    public String cutPaper() {
        return("\u001b|fP");            // paper cut
    }

    public String setLargeText() {
        return ("\u001b|4C");           // Vertical x 2, Holizontal x 2
    }

    public String setNormalSizeText() {
        return ("\u001b|N\r\n");        // return to normal
    }

}
