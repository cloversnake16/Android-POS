package no.susoft.mobile.pos.hardware.terminal.events;

import no.susoft.mobile.pos.json.JSONSerializable;

/**
 * Signals the application that a financial or administrative transaction is completed.
 * <p>
 * Created on 3/2/2016.
 */
public class CardTerminalTransactionCompleteEvent extends CardTerminalTransactionEvent implements JSONSerializable {

    /**
     * Constructs a new instance of this class.
     *
     * @param source the object which fired the event.
     */
    public CardTerminalTransactionCompleteEvent(Object source) {
        super(source);
    }

}
