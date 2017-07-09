package no.susoft.mobile.pos.hardware.terminal.events;

import java.util.EventObject;

/**
 * Created on 3/1/2016.
 */
public abstract class CardTerminalEvent extends EventObject {

    /**
     * Constructs a new instance of this class.
     * @param source the object which fired the event.
     */
    public CardTerminalEvent(Object source) {
        super(source);
    }
}
