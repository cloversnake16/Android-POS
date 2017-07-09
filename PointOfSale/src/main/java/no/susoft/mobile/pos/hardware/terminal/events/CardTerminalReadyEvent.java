package no.susoft.mobile.pos.hardware.terminal.events;

/**
 * This is an optional special purpose event. It triggers each time the terminal determines that
 * it has completed terminal management. Typically, this will occur as the terminal goes into
 * idle after an activity, like a purchase.
 *
 * Created on 3/2/2016.
 */
public class CardTerminalReadyEvent extends CardTerminalEvent {

    /**
     * Constructs a new instance of this class.
     * @param source the object which fired the event.
     */
    public CardTerminalReadyEvent(Object source) {
        super(source);
    }
}
