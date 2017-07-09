package no.susoft.mobile.pos.hardware.terminal.events;

/**
 * Signals to the application that an error has occurred
 *
 * Created on 3/2/2016.
 */
public class CardTerminalErrorEvent extends CardTerminalEvent {

    /**
     * Error code
     */
    private int errorCode;

    /**
     * A short textual description of the error.
     */
    private String errorString;

    public CardTerminalErrorEvent(Object source) {
        super(source);
    }

    public int getErrorCode() {
        return errorCode;
    }

    public CardTerminalErrorEvent setErrorCode(int errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public String getErrorString() {
        return errorString;
    }

    public CardTerminalErrorEvent setErrorString(String errorString) {
        this.errorString = errorString;
        return this;
    }


}
