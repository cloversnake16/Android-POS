package no.susoft.mobile.pos.hardware.terminal.events;

import no.susoft.mobile.pos.hardware.terminal.CardTerminal;
import no.susoft.mobile.pos.hardware.terminal.TerminalResponse;
import no.susoft.mobile.pos.json.JSONSerializable;

/**
 * Created on 3/2/2016.
 */
public class CardTerminalTransactionEvent extends CardTerminalEvent {

    private TerminalResponse terminalResponse;

    /**
     * Constructs a new instance of this class.
     *
     * @param source the object which fired the event.
     */
    public CardTerminalTransactionEvent(Object source) {
        super(source);

        terminalResponse = new TerminalResponse();
    }

    public TerminalResponse getTerminalResponse() {
        return terminalResponse;
    }

    public CardTerminalTransactionEvent setTerminalResponse(TerminalResponse terminalResponse) {
        this.terminalResponse = terminalResponse;return this;
    }
}
