package no.susoft.mobile.pos.hardware.terminal.events;

/**
 * CardTerminalLastFinancialResultEvent is identical to the contents of a CardTerminalTransactionCompleteEvent,
 * but it contains the results of the last/latest financial operation. This data can be used to determine
 * what happened to a transaction in case of communications errors or other causes of a loss of transaction result.
 *
 * Created on 3/2/2016.
 */
public class CardTerminalLastFinancialResultEvent extends CardTerminalTransactionEvent {

    /**
     * Constructs a new instance of this class.
     *
     * @param source the object which fired the event.
     */
    public CardTerminalLastFinancialResultEvent(Object source) {
        super(source);
    }
}
