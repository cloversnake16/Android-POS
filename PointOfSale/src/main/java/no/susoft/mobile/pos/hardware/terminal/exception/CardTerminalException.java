package no.susoft.mobile.pos.hardware.terminal.exception;

/**
 * Created on 8/24/2016.
 */
public class CardTerminalException extends Exception {

    public CardTerminalException() {
        super();
    }

    public CardTerminalException(String detailMessage) {
        super(detailMessage);
    }

    public CardTerminalException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public CardTerminalException(Throwable throwable) {
        super(throwable);
    }
}
