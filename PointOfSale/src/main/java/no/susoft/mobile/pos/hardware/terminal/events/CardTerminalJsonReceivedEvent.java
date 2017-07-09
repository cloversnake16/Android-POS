package no.susoft.mobile.pos.hardware.terminal.events;

/**
 * Created on 3/2/2016.
 */
public class CardTerminalJsonReceivedEvent extends CardTerminalEvent {

    private String jsonData;

    /**
     * Constructs a new instance of this class.
     * @param source the object which fired the event.
     */
    public CardTerminalJsonReceivedEvent(Object source) {
        super(source);
    }

    public String getJsonData() {
        return jsonData;
    }

    public CardTerminalJsonReceivedEvent setJsonData(String jsonData) {
        this.jsonData = jsonData;
        return this;
    }
}
