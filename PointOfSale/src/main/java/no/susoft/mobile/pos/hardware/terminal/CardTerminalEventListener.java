package no.susoft.mobile.pos.hardware.terminal;

import no.susoft.mobile.pos.hardware.terminal.events.*;

/**
 * Created on 3/1/2016.
 */
public interface CardTerminalEventListener {

    void onConnected(CardTerminalConnectedEvent event);

    void onDisconnected(CardTerminalDisconnectedEvent event);

    void onError(CardTerminalErrorEvent event);

    void onTerminalReady(CardTerminalReadyEvent event);

    void onTransactionComplete(CardTerminalTransactionCompleteEvent event);

    void onTransactionFailed(CardTerminalTransactionFailedEvent event);

    void onLastFinancialResult(CardTerminalLastFinancialResultEvent event);

    void onPrintText(CardTerminalPrintTextEvent event);

    void onDisplayText(CardTerminalDisplayTextEvent event);

    void beforeAny();

    void afterAny();
}
