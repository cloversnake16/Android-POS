package no.susoft.mobile.pos.hardware.terminal;

import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.hardware.terminal.events.*;

/**
 * Created on 3/22/2016.
 */
public class CardTerminalEventListenerImpl implements CardTerminalEventListener {
    @Override
    public void onConnected(CardTerminalConnectedEvent event) {
    }

    @Override
    public void onDisconnected(CardTerminalDisconnectedEvent event) {
    }

    @Override
    public void onError(CardTerminalErrorEvent event) {
    }

    @Override
    public void onTerminalReady(CardTerminalReadyEvent event) {
    }

    @Override
    public void onTransactionComplete(CardTerminalTransactionCompleteEvent event) {
    }

    @Override
    public void onTransactionFailed(CardTerminalTransactionFailedEvent event) {
    }

    @Override
    public void onLastFinancialResult(CardTerminalLastFinancialResultEvent event) {
    }

    @Override
    public void onPrintText(CardTerminalPrintTextEvent event) {
    }

    @Override
    public void onDisplayText(CardTerminalDisplayTextEvent event) {
    }

    @Override
    public void beforeAny() {

    }

    @Override
    public void afterAny() {

    }
}
