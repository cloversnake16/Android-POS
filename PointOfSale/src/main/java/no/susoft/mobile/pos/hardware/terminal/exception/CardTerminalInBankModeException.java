package no.susoft.mobile.pos.hardware.terminal.exception;

/**
 * Created on 8/24/2016.
 */
public class CardTerminalInBankModeException extends CardTerminalException {

    public CardTerminalInBankModeException() {
        super();
    }

    public CardTerminalInBankModeException(String detailMessage) {
        super(detailMessage);
    }

    public CardTerminalInBankModeException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public CardTerminalInBankModeException(Throwable throwable) {
        super(throwable);
    }
}
