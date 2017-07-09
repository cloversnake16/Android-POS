package no.susoft.mobile.pos.hardware.terminal.events;

/**
 * Created on 3/2/2016.
 */
public class CardTerminalDisconnectedEvent extends CardTerminalEvent {

    /**
     * Constructs a new instance of this class.
     *
     * @param source the object which fired the event.
     */
    public CardTerminalDisconnectedEvent(Object source) {
        super(source);
    }
}
