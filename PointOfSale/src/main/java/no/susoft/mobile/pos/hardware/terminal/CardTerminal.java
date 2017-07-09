package no.susoft.mobile.pos.hardware.terminal;

import eu.nets.baxi.client.AdministrationArgs;
import eu.nets.baxi.client.TransactionEventArgs;
import eu.nets.baxi.client.TransferAmountArgs;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.hardware.printer.Printer;
import no.susoft.mobile.pos.hardware.printer.PrinterFactory;
import no.susoft.mobile.pos.hardware.printer.VerifonePrinter;
import no.susoft.mobile.pos.hardware.terminal.events.CardTerminalPrintTextEvent;
import no.susoft.mobile.pos.hardware.terminal.events.CardTerminalTransactionCompleteEvent;
import no.susoft.mobile.pos.hardware.terminal.exception.CardTerminalException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 3/1/2016.
 */
public abstract class CardTerminal {

    public static final int RESULT_FINANCIAL_TRANSACTION_OK = 0;        // : Financial transaction OK, accumulator updated
    public static final int RESULT_ADMINISTRATIVE_TRANSACTION_OK = 1;   // : Administrative transaction OK, no update of accumulator
    public static final int RESULT_TRANSACTION_REJECTED = 2;            // : Transaction rejected, no update of accumulator
    public static final int RESULT_TRANSACTION_IS_LOYALTY = 3;          // : Transaction is Loyalty Transaction
    public static final int RESULT_UNKNOWN = 99;                        // : Unknown result. Lost communication with terminal.

    // This is a code for cardholder verification method (CVM). The codes are:
    public static final int VERIFICATION_PIN = 0;       //: Transaction is PIN based (default)
    public static final int VERIFICATION_SIG = 1;       //: Transaction is signature based
    public static final int VERIFICATION_CVM = 2;       //: No CVM. With or without amount confirmation by cardholder.
    public static final int VERIFICATION_LOYALTY = 3;   //: Transaction is Loyalty Transaction

    public static final int SUCCESS = 1;
    public static final int FAILURE = 0;
    public static final int NOT_CONNECTED = -1;

    public static final int TERMINAL_NETS = 1;
    public static final int TERMINAL_VERIFONE = 2;

    public int terminalType;

    protected static final Map<Integer, String> errorCodes = new HashMap<>();

    static {

        // OUR OWN error codes (5 digits, first is 9)
        errorCodes.put(90001, "Amount2 Only used if <TYPE 1> = Purchase with Cashback");
        errorCodes.put(90002, "authCode contains wrong characters. Only [A-Za-z0-9] allowed");
        errorCodes.put(90003, "No listener registered");
    }

    final protected List<CardTerminalEventListener> listeners = new ArrayList<>();

    public abstract int getMethodRejectCode();

    public abstract void testConnectionAndReconnectIfLost(String caller) throws CardTerminalException;

    /**
     * @return 1 on success, CardTerminalTransactionEvent will follow
     * 0 on failure, no CardTerminalTransactionEvent
     */
    abstract public int open() throws CardTerminalException;

    abstract public int close() throws CardTerminalException;

    abstract public boolean isOpen() throws CardTerminalException;

    abstract public int purchase(TerminalRequest terminalRequest) throws CardTerminalException;

    abstract public int reconciliation(String operId) throws CardTerminalException;

    abstract public int sendLastFinancialResult(String operId) throws CardTerminalException;

    abstract public int printLastCardPaymentTransaction(String operId) throws CardTerminalException;

    abstract public int cancel(String operId) throws CardTerminalException;

    abstract public int xreport(String operId) throws CardTerminalException;

    abstract public int zreport(String operId) throws CardTerminalException;

    /**
     * Reversal of the last transaction
     */
    abstract public int reversal(TerminalRequest terminalRequest) throws CardTerminalException;

    /**
     * Return of goods
     */
    abstract public int returnOfGoods(TerminalRequest terminalRequest) throws CardTerminalException;

    abstract public int printOnTerminal(String text) throws CardTerminalException;

    public void addListener(CardTerminalEventListener listener) {
        ErrorReporter.INSTANCE.filelog("CardTerminal", "addListener -> " + listener);

        synchronized (listeners) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }

            ErrorReporter.INSTANCE.filelog("CardTerminal", "Total listeners -> " + listeners.size());
        }
    }

    public synchronized void removeListener(CardTerminalEventListener listener) {
        ErrorReporter.INSTANCE.filelog("CardTerminal", "removeListener -> " + listener);

        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    abstract public boolean isConnected();

    abstract public boolean isInBankMode();

    /**
     * Usage: ErrorCode.decode(CardTerminal.getInstance().getMethodRejectCode())
     */
    public String decode(int code) {
        if (!errorCodes.containsKey(code)) return "N/A";
        return errorCodes.get(code);
    }

    public static String asString(TransferAmountArgs a) {
        String str = "";
        final String crlf = "\n";

        try {
            str += "TransferAmountArgs ----------------- " + crlf;

            str += "operID=" + a.getOperID() + crlf;
            str += "type1=" + a.getType1() + crlf;
            str += "amount1=" + a.getAmount1() + crlf;
            str += "type2=" + a.getType2() + crlf;
            str += "amount2=" + a.getAmount2() + crlf;
            str += "type3=" + a.getType3() + crlf;
            str += "amount3=" + a.getAmount3() + crlf;
            str += "hostData=" + a.getHostData() + crlf;
            str += "articleDetails=" + a.getArticleDetails() + crlf;
            str += "paymentConditionCode=" + a.getPaymentConditionCode() + crlf;
            str += "authCode=" + a.getAuthCode() + crlf;
            str += "optionalData=" + a.getOptionalData() + crlf;

        } catch (Exception e) {
            ErrorReporter.INSTANCE.filelog("NETS", "ERROR -> ", e);
        }

        return str;
    }

    public static String asString(TransactionEventArgs args) {
        String str = "";
        final String crlf = "\n";

        try {
            str += "TransactionEventArgs ----------------- " + crlf;
            str += "ResultData: " + args.getResultData() + crlf;
            str += "   Result: " + args.getResult() + crlf;
            str += "   AccumulatorUpdate: 0x" + String.format("%02x", args.getAccumulatorUpdate()) + crlf;
            str += "   IssuerId: " + String.format("%02d", args.getIssuerId()) + crlf;
            str += "   CardData: " + args.getTruncatedPan() + crlf;
            str += "   Timestamp: " + args.getTimestamp() + crlf;
            str += "   VerificationMethod: " + args.getVerificationMethod() + crlf;
            str += "   SessionNumber: " + args.getSessionNumber() + crlf;
            str += "   StanAuth: " + args.getStanAuth() + crlf;
            str += "   SequenceNumber: " + args.getSequenceNumber() + crlf;
            str += "   TotalAmount: " + args.getTotalAmount() + crlf;
            str += "   RejectionSource: " + args.getRejectionSource() + crlf;
            str += "   RejectionReason: " + args.getRejectionReason() + crlf;
            str += "   TipAmount: " + args.getTipAmount() + crlf;
            str += "   SurchargeAmount: " + args.getSurchargeAmount() + crlf;
            str += "   terminalID: " + args.getTerminalID() + crlf;
            str += "   acquirerMerchantID: " + args.getAcquirerMerchantID() + crlf;
            str += "   cardIssuerName: " + args.getCardIssuerName() + crlf;
            str += "   responseCode: " + args.getResponseCode() + crlf;
            str += "   TCC: " + args.getTCC() + crlf;
            str += "   AID: " + args.getAID() + crlf;
            str += "   TVR: " + args.getTVR() + crlf;
            str += "   TSI: " + args.getTSI() + crlf;
            str += "   ATC: " + args.getATC() + crlf;
            str += "   AED: " + args.getAED() + crlf;
            str += "   IAC: " + args.getIAC() + crlf;
            str += "   OrganisationNumber: " + args.getOrganisationNumber() + crlf;
            str += "   BankAgent : " + args.getBankAgent() + crlf;
            str += "   EncryptedPAN : " + args.getEncryptedPAN() + crlf;
            str += "   AccountType : " + args.getAccountType() + crlf;
            str += "   OptionalData : " + args.getOptionalData() + crlf;

        } catch (Exception e) {
            ErrorReporter.INSTANCE.filelog("NETS", "ERROR -> ", e);
        }

        return str;
    }

    public static String asString(AdministrationArgs a) {
        String str = "";
        final String crlf = "\n";

        str += "AdministrationArgs ----------------- " + crlf;

        str += "operID=" + a.OperID + crlf;
        str += "AdmCode=" + a.AdmCode + crlf;
        str += "OptionalData=" + a.OptionalData + crlf;

        return str;
    }


}
