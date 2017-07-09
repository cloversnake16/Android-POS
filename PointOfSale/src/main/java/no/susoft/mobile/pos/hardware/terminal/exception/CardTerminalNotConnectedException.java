package no.susoft.mobile.pos.hardware.terminal.exception;

/**
 * Created on 8/24/2016.
 */
public class CardTerminalNotConnectedException extends CardTerminalException {
    public CardTerminalNotConnectedException() {
        super();
    }

    public CardTerminalNotConnectedException(String detailMessage) {
        super(detailMessage);
    }

    public CardTerminalNotConnectedException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public CardTerminalNotConnectedException(Throwable throwable) {
        super(throwable);
    }
}
