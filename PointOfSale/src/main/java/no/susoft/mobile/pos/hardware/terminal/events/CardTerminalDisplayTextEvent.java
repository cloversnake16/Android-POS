package no.susoft.mobile.pos.hardware.terminal.events;

/**
 * Created on 3/2/2016.
 */
public class CardTerminalDisplayTextEvent extends CardTerminalEvent {

    private String displayText;
    private int displayTextSourceID;
    private int displayTextID;

    public CardTerminalDisplayTextEvent(Object eventObj) {
        super(eventObj);
    }

    public String getDisplayText() {
        return displayText;
    }

    public CardTerminalDisplayTextEvent setDisplayText(String displayText) {
        this.displayText = displayText;
        return this;
    }

    public int getDisplayTextSourceID() {
        return displayTextSourceID;
    }

    public CardTerminalDisplayTextEvent setDisplayTextSourceID(int displayTextSourceID) {
        this.displayTextSourceID = displayTextSourceID;
        return this;
    }

    public int getDisplayTextID() {
        return displayTextID;
    }

    public CardTerminalDisplayTextEvent setDisplayTextID(int displayTextID) {
        this.displayTextID = displayTextID;
        return this;
    }
}
