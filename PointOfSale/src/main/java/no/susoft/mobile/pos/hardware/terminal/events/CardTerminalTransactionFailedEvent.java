package no.susoft.mobile.pos.hardware.terminal.events;

import no.susoft.mobile.pos.json.JSONSerializable;

/**
 * Created on 3/2/2016.
 */
public class CardTerminalTransactionFailedEvent extends CardTerminalTransactionEvent implements JSONSerializable {

    /**
     * Constructs a new instance of this class.
     * @param source the object which fired the event.
     */
    public CardTerminalTransactionFailedEvent(Object source) {
        super(source);
    }
}
