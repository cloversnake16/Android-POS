package no.susoft.mobile.pos.hardware.terminal;

import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.hardware.terminal.events.*;

/**
 * Created on 8/23/2016.
 */
public class VerboseCardTerminalEventListenerImpl implements CardTerminalEventListener {

    @Override
    public void onConnected(CardTerminalConnectedEvent event) {
        ErrorReporter.INSTANCE.filelog("onConnected");
    }

    @Override
    public void onDisconnected(CardTerminalDisconnectedEvent event) {
        ErrorReporter.INSTANCE.filelog("VerboseCardTerminalEventListenerImpl", "onDisconnected");
    }

    @Override
    public void onError(CardTerminalErrorEvent event) {
        ErrorReporter.INSTANCE.filelog("VerboseCardTerminalEventListenerImpl","onError");
    }

    @Override
    public void onTerminalReady(CardTerminalReadyEvent event) {
        ErrorReporter.INSTANCE.filelog("VerboseCardTerminalEventListenerImpl","onTerminalReady");
    }

    @Override
    public void onTransactionComplete(CardTerminalTransactionCompleteEvent event) {
        ErrorReporter.INSTANCE.filelog("VerboseCardTerminalEventListenerImpl","onTransactionComplete");
    }

    @Override
    public void onTransactionFailed(CardTerminalTransactionFailedEvent event) {
        ErrorReporter.INSTANCE.filelog("VerboseCardTerminalEventListenerImpl","onTransactionFailed");
    }

    @Override
    public void onLastFinancialResult(CardTerminalLastFinancialResultEvent event) {
        ErrorReporter.INSTANCE.filelog("VerboseCardTerminalEventListenerImpl","onLastFinancialResult");
    }

    @Override
    public void onPrintText(CardTerminalPrintTextEvent event) {
        ErrorReporter.INSTANCE.filelog("VerboseCardTerminalEventListenerImpl","onPrintText: \n" + event.getPrintText() + event.getSignaturePrint());
    }

    @Override
    public void onDisplayText(CardTerminalDisplayTextEvent event) {
        ErrorReporter.INSTANCE.filelog("VerboseCardTerminalEventListenerImpl","onDisplayText: \n" + event.getDisplayText());
    }

    @Override
    public void beforeAny() {
    }

    @Override
    public void afterAny() {
    }
}
