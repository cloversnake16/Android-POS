package no.susoft.mobile.pos.hardware.terminal.exception;

/**
 * Created on 8/24/2016.
 */
public class CardTerminalIllegalAmountException extends CardTerminalException {
    public CardTerminalIllegalAmountException() {
        super();
    }

    public CardTerminalIllegalAmountException(String detailMessage) {
        super(detailMessage);
    }

    public CardTerminalIllegalAmountException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public CardTerminalIllegalAmountException(Throwable throwable) {
        super(throwable);
    }
}
