package no.susoft.mobile.pos.hardware.terminal.events;

/**
 * Created on 3/2/2016.
 */
public class CardTerminalPrintTextEvent extends CardTerminalEvent {

    private String printText = "";
    private String signaturePrint = "";

    public CardTerminalPrintTextEvent(Object eventObj) {
        super(eventObj);
    }

    public String getPrintText() {
        return printText;
    }

    public String getSignaturePrint() {
        return signaturePrint;
    }

    public CardTerminalPrintTextEvent setPrintText(String printText) {
        this.printText = printText;
        return this;
    }

    public CardTerminalPrintTextEvent setSignaturePrint(String signaturePrint) {
        this.signaturePrint = signaturePrint;
        return this;
    }
}
