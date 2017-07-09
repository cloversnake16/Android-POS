package no.susoft.mobile.pos.hardware.terminal;

import java.util.StringTokenizer;
import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import no.point.paypoint.*;
import no.susoft.mobile.pos.SusoftPOSApplication;
import no.susoft.mobile.pos.data.PeripheralDevice;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.hardware.printer.Printer;
import no.susoft.mobile.pos.hardware.printer.PrinterFactory;
import no.susoft.mobile.pos.hardware.printer.VerifonePimHelper;
import no.susoft.mobile.pos.hardware.printer.VerifonePrinter;
import no.susoft.mobile.pos.hardware.terminal.events.*;
import no.susoft.mobile.pos.hardware.terminal.exception.CardTerminalException;
import no.susoft.mobile.pos.hardware.terminal.exception.CardTerminalIllegalAmountException;
import no.susoft.mobile.pos.hardware.terminal.exception.CardTerminalInBankModeException;
import no.susoft.mobile.pos.hardware.terminal.exception.CardTerminalNotConnectedException;
import no.susoft.mobile.pos.ui.activity.MainActivity;


/**
 * Created on 3/24/2016.
 */
public class VerifonePim extends CardTerminal implements PayPointListener {

    public static final String IP_SETTING_KEY = "CARD_TERMINAL_IP";

    private static VerifonePim instance = null;
    private static IPayPoint paypoint;
    private static VerifoneLogger logger;
    private static String ipAddress;

    static {
        // Verifone
        errorCodes.put(100, "Com port not found");
        errorCodes.put(101, "Error during open of com port");
        errorCodes.put(102, "Error during setup of com port");
        errorCodes.put(103, "Error during setting up config parameters");
        errorCodes.put(104, "Error during setting config parameters");
        errorCodes.put(105, "Error during setting event mask to be monitored by communication device");
        errorCodes.put(106, "Startup of communication thread failed");
        errorCodes.put(107, "Com port not initialised");
        errorCodes.put(108, "Illegal application version (must be 6 characters)");
        errorCodes.put(109, "Com port initialised before");
        errorCodes.put(110, "Close called when in bank mode");
        errorCodes.put(111, "Illegal transaction type");
        errorCodes.put(112, "Illegal mode value");
        errorCodes.put(113, "Illegal amount");
        errorCodes.put(114, "Trying to start new transaction in bank mode");
        errorCodes.put(115, "Illegal administration code");
        errorCodes.put(116, "Cancel called before for this transaction");
        errorCodes.put(117, "No response received on previous request");
        errorCodes.put(118, "Illegal com port");
        errorCodes.put(119, "Protocol is not running");
        errorCodes.put(120, "Error during sending of data");
        errorCodes.put(121, "Error during receiving of data");
        errorCodes.put(122, "Error during reading of data");
        errorCodes.put(123, "Communication with payment terminal is lost");
        errorCodes.put(124, "Timeout waiting for ACK from payment terminal");
        errorCodes.put(125, "NACK received from terminal, maximum number of retries reached");
        errorCodes.put(126, "General error");
        errorCodes.put(127, "Communication error (f.ex. parity error, framing error etc.)");
        errorCodes.put(128, "Cancel called when not in bank mode");
        errorCodes.put(129, "Error starting Ethernet protocol thread");
        errorCodes.put(130, "Ethernet protocol not running");
        errorCodes.put(144, "For communication via ECR: an IP connection is open in the component (i.e. the terminal is communicating with host), so the terminal is not ready for a new transaction.");
        errorCodes.put(147, "Illegal IP address (wrong format) provided");
        errorCodes.put(150, "The characters in the additional transaction data are not in the legal range");
        errorCodes.put(151, "Data for delete authorisation is not set");
        errorCodes.put(152, "Error in manual card data");
        errorCodes.put(153, "Input parameter has illegal format");
        errorCodes.put(155, "Search for USB connected terminal failed");
        errorCodes.put(156, "Function fails because component is open (i.e. open function has been called)");
    }

    protected UUID currentOperationUUID = UUID.randomUUID();
    private int methodRejectCode = -1;

    private VerifonePim() throws TerminalInBankModeException,
            IllegalAppVersionException, ComNotInitialisedException,
            IllegalIpAddressException, ComAlreadyInitialisedException {

        ErrorReporter.INSTANCE.filelog("VerifonePim", "constructing new VerifonePim()");

        terminalType = TERMINAL_VERIFONE;

        SharedPreferences preferences = SusoftPOSApplication.getContext()
                .getSharedPreferences(PeripheralDevice.class.toString(), Context.MODE_PRIVATE);

        ipAddress = preferences.getString(IP_SETTING_KEY, "");
        ErrorReporter.INSTANCE.filelog("VerifonePim", "ipAddress=" + ipAddress);
    }

    private static void reconnectInternal() throws CardTerminalException {

        try {
            if (paypoint == null) {
                paypoint = PayPointFactory.createPayPoint();
            } else {
                paypoint.close(); // close before reconnect
            }

            paypoint.open(ipAddress, 0, "pda001", PayPoint.PROTOCOL_ETHERNET);
            paypoint.setPayPointListener(VerifonePim.instance);
            paypoint.setEcrLanguage(PayPoint.LANG_NOR);
        } catch (ComAlreadyInitialisedException | IllegalIpAddressException | IllegalAppVersionException e) {
            e.printStackTrace();
            throw new CardTerminalException(e);
        } catch (TerminalInBankModeException e) {
            e.printStackTrace();
            throw new CardTerminalInBankModeException(e);
        } catch (ComNotInitialisedException e) {
            e.printStackTrace();
            throw new CardTerminalNotConnectedException(e);
        }
    }

    /**
     * Used by factory to create new instance
     */
    public static CardTerminal getInstance() throws CardTerminalInBankModeException, CardTerminalNotConnectedException {
        ErrorReporter.INSTANCE.filelog("VerifonePim", "getInstance()");

        synchronized (IP_SETTING_KEY) {
            if (instance == null) {
                try {
                    instance = new VerifonePim(); // create yourself

                    ErrorReporter.INSTANCE.filelog("VerifonePim", "Creating instance and connecting...");

                    if (logger == null) logger = new VerifoneLogger();
                    paypoint = PayPointFactory.createPayPoint(logger);

//                    paypoint = PayPointFactory.createPayPoint();

                    paypoint.open(ipAddress, 0, "pda001", PayPoint.PROTOCOL_ETHERNET);
                    paypoint.setPayPointListener(instance);
                    paypoint.setEcrLanguage(PayPoint.LANG_NOR);

                    // startTestCom
                    // To ensure that the communication with the terminal is working properly
                    // it is recommended to make a call immediately after open.
                    paypoint.startTestCom();

                    ErrorReporter.INSTANCE.filelog("VerifonePim", "DONE creating instance and connecting...");

                } catch (NoListenerRegisteredException | IllegalAppVersionException | IllegalIpAddressException | ComAlreadyInitialisedException e) {
                    ErrorReporter.INSTANCE.filelog("VerifonePim", "Error creating VerifonePim instance", e);
                    e.printStackTrace();
                } catch (TerminalInBankModeException e) {
                    ErrorReporter.INSTANCE.filelog("VerifonePim", "Error creating VerifonePim instance", e);
                    e.printStackTrace();
                    throw new CardTerminalInBankModeException(e);
                } catch (ComNotInitialisedException e) {
                    ErrorReporter.INSTANCE.filelog("VerifonePim", "Error creating VerifonePim instance", e);
                    e.printStackTrace();
                    throw new CardTerminalNotConnectedException(e);
                }
            }
        }

        return instance;
    }

    public void testConnectionAndReconnectIfLost(String caller) throws CardTerminalException {
        // check if connection is not lost
        ErrorReporter.INSTANCE.filelog("VerifonePim", "Checking if connection is not lost <- " + caller);

        try {
            paypoint.startTestCom();
        } catch (TerminalInBankModeException e) {
            e.printStackTrace();
            // should wait until back to local mode
            ErrorReporter.INSTANCE.filelog("VerifonePim", "Test communication failed, terminal  is busy", e);
            throw new CardTerminalInBankModeException(e);
        } catch (NoListenerRegisteredException e) {
            e.printStackTrace();
            ErrorReporter.INSTANCE.filelog("VerifonePim", "Test communication failed, terminal has no listener. Trying to fix", e);
            reconnectInternal();
        } catch (ComNotInitialisedException e) {
            e.printStackTrace();
            reconnectInternal();
        }
    }

    public UUID getCurrentOperationUUID() {
        return currentOperationUUID;
    }

    @Override
    public int getMethodRejectCode() {
        return methodRejectCode;
    }

    @Override
    public int open() throws CardTerminalException {
        ErrorReporter.INSTANCE.filelog("VerifonePim", "terminal open()");
        return SUCCESS;
    }

    @Override
    public int close() throws CardTerminalException {
        try {
            paypoint.close();
        } catch (TerminalInBankModeException e) {
            e.printStackTrace();
            return 6003; // Busy
        }

        return SUCCESS;
    }

    @Override
    public boolean isOpen() throws CardTerminalException {
        // Indicates if the communication channel is opened or not. TRUE if opened,
        // FALSE otherwise. Can be read at any time.
        return paypoint.isOpen(); // always
    }

    @Override
    public int purchase(TerminalRequest terminalRequest) throws CardTerminalException {
        currentOperationUUID = UUID.randomUUID();
        terminalRequest.setUuid(currentOperationUUID.toString());

        try {

            long id = -1;
            try {
                id = MainActivity.getInstance().getDbHelper().getWritableDatabase().insert(TerminalRequest.Table.NAME, null,
                        terminalRequest.asContentValues());
            } catch (Exception e) {
                ErrorReporter.INSTANCE.filelog("verifone", "purchase -> Error on accessing DB", e);
            }

            if (id < 0) throw new CardTerminalException("Error saving request to DB");

            int totalPurchaseAmount = terminalRequest.getTotalPurchaseAmount();
            byte mode = PayPoint.TRANS_CARD_PURCHASE;
            if (totalPurchaseAmount > 0) {
                mode = PayPoint.TRANS_CASHBACK;
            }

            paypoint.startTransaction(mode, terminalRequest.getTotalAmount(), totalPurchaseAmount, PayPoint.MODE_NORMAL);

            return SUCCESS;

        } catch (UnknownTransactionCodeException | NoListenerRegisteredException | UnknownModeException e) {
            ErrorReporter.INSTANCE.filelog("verifone", "error calling purchase()", e);
            throw new CardTerminalException(e);
        } catch (TerminalInBankModeException e) {
            ErrorReporter.INSTANCE.filelog("verifone", "error calling purchase()", e);
            throw new CardTerminalInBankModeException(e);
        } catch (ComNotInitialisedException e) {
            ErrorReporter.INSTANCE.filelog("verifone", "error calling purchase()", e);
            throw new CardTerminalNotConnectedException(e);
        } catch (IllegalAmountException e) {
            ErrorReporter.INSTANCE.filelog("verifone", "error calling purchase()", e);
            throw new CardTerminalIllegalAmountException(e);
        }
    }

    protected void shouldBePrintedOnInternalPrinter() {
        final Printer p = PrinterFactory.getInstance().getPrinter();
        if (p instanceof VerifonePrinter) {
            // we can print bank receipt on verifone printer only in this case or if we need customer signature
            this.addListener(new VerboseCardTerminalEventListenerImpl() {
                public void onPrintText(CardTerminalPrintTextEvent event) {
                    super.onPrintText(event);
                    ErrorReporter.INSTANCE.filelog("printLastCardPaymentTransaction", event.getPrintText());
                    p.printLine(event.getPrintText() +
                            VerifonePimHelper.PRINT_NEWLINE +
                            VerifonePimHelper.PRINT_NEWLINE +
                            VerifonePimHelper.PRINT_NEWLINE);

                    removeListener(this);
                }

                @Override
                public void onTransactionComplete(CardTerminalTransactionCompleteEvent event) {
                    super.onTransactionComplete(event);
                    removeListener(this);
                }

                @Override
                public void onDisplayText(CardTerminalDisplayTextEvent event) {
                    // do nothing
                }
            });
        }
    }

    @Override
    public int reconciliation(String operId) throws CardTerminalException {

        try {
            if (paypoint.isInBankMode()) {
                ErrorReporter.INSTANCE.filelog("verifone", "Terminal is in Bank Mode (reconciliation)");
                return FAILURE;
            }

            // set UUID for ongoing operation
            currentOperationUUID = UUID.randomUUID();
            TerminalAdminRequest adminRequest = new TerminalAdminRequest(currentOperationUUID.toString(), PayPoint.ADM_RECONCILIATION, "0000");

            long id = -1;
            try {
                id = MainActivity.getInstance().getDbHelper().getWritableDatabase().insert(TerminalAdminRequest.Table.NAME, null, adminRequest.asContentValues());
            } catch (Exception e) {
                ErrorReporter.INSTANCE.filelog("verifone", "reconciliation -> Error on accessing DB", e);
            }

            if (id < 0) return CardTerminal.FAILURE;

            shouldBePrintedOnInternalPrinter();

            paypoint.startAdmin(PayPoint.ADM_RECONCILIATION);

            return SUCCESS;

        } catch (TerminalInBankModeException e) {
            methodRejectCode = 114; // ERR_IN_BANK_MODE
            ErrorReporter.INSTANCE.filelog("verifone", "error calling reconciliation()", e);
        } catch (NoListenerRegisteredException e) {
            methodRejectCode = 126; // General error
            ErrorReporter.INSTANCE.filelog("verifone", "error calling reconciliation()", e);
        } catch (ComNotInitialisedException e) {
            methodRejectCode = 107; // ERR_COM_NOT_INITED
            ErrorReporter.INSTANCE.filelog("verifone", "error calling reconciliation()", e);
        } catch (TerminalInLocalModeException e) {
            ErrorReporter.INSTANCE.filelog("verifone", "error calling reconciliation()", e);
        } catch (Exception e) {
            methodRejectCode = 126; // General error
            ErrorReporter.INSTANCE.filelog("verifone", "error calling reconciliation()", e);
            e.printStackTrace();
        }

        return FAILURE;
    }

    @Override
    public int sendLastFinancialResult(String operId) throws CardTerminalException {
        return 0;
    }

    @Override
    public int printLastCardPaymentTransaction(String operId) throws CardTerminalException {
        try {
            if (paypoint.isInBankMode()) {
                ErrorReporter.INSTANCE.filelog("verifone", "Terminal is in Bank Mode (printLastCardPaymentTransaction)");
                return FAILURE;
            }

            // set UUID for ongoing operation
            currentOperationUUID = UUID.randomUUID();
            TerminalAdminRequest adminRequest = new TerminalAdminRequest(currentOperationUUID.toString(),
                    PayPoint.ADM_COPY_LAST, "0000");

            long id = -1;
            try {
                id = MainActivity.getInstance().getDbHelper().getWritableDatabase().insert(TerminalAdminRequest.Table.NAME, null,
                        adminRequest.asContentValues());
            } catch (Exception e) {
                ErrorReporter.INSTANCE.filelog("verifone", "printLastCardPaymentTransaction -> Error on accessing DB", e);
            }

            if (id < 0) return CardTerminal.FAILURE;

            shouldBePrintedOnInternalPrinter();
            paypoint.startAdmin(PayPoint.ADM_COPY_LAST); // 0x3830

            return SUCCESS;
        } catch (TerminalInBankModeException e) {
            methodRejectCode = 114; // ERR_IN_BANK_MODE
            ErrorReporter.INSTANCE.filelog("verifone", "error calling printLastCardPaymentTransaction()", e);
        } catch (NoListenerRegisteredException e) {
            methodRejectCode = 126; // General error
            ErrorReporter.INSTANCE.filelog("verifone", "error calling printLastCardPaymentTransaction()", e);
        } catch (ComNotInitialisedException e) {
            methodRejectCode = 107; // ERR_COM_NOT_INITED
            ErrorReporter.INSTANCE.filelog("verifone", "error calling printLastCardPaymentTransaction()", e);
        } catch (TerminalInLocalModeException e) {
            ErrorReporter.INSTANCE.filelog("verifone", "error calling printLastCardPaymentTransaction()", e);
        } catch (Exception e) {
            methodRejectCode = 126; // General error
            ErrorReporter.INSTANCE.filelog("verifone", "error calling printLastCardPaymentTransaction()", e);
            e.printStackTrace();
        }

        return FAILURE;
    }

    @Override
    public int cancel(String operId) throws CardTerminalException {
        try {

            paypoint.cancelRequest();
            return SUCCESS;

        } catch (NoListenerRegisteredException e) {
            methodRejectCode = 126; // General error
            ErrorReporter.INSTANCE.filelog("verifone", "error calling cancel()", e);
        } catch (TerminalInLocalModeException e) {
            methodRejectCode = 128; // Cancel called when not in bank mode
            ErrorReporter.INSTANCE.filelog("verifone", "error calling cancel()", e);
        }

        return FAILURE;
    }

    @Override
    public int xreport(String operId) throws CardTerminalException {
        try {
            if (paypoint.isInBankMode()) {
                ErrorReporter.INSTANCE.filelog("verifone", "Terminal is in Bank Mode (xreport)");
                return FAILURE;
            }

            // set UUID for ongoing operation
            currentOperationUUID = UUID.randomUUID();
            TerminalAdminRequest adminRequest = new TerminalAdminRequest(currentOperationUUID.toString(), PayPoint.ADM_X_REPORT, "0000");

            long id = -1;
            try {
                id = MainActivity.getInstance().getDbHelper().getWritableDatabase().insert(TerminalAdminRequest.Table.NAME, null, adminRequest.asContentValues());
            } catch (Exception e) {
                ErrorReporter.INSTANCE.filelog("verifone", "xreport -> Error on accessing DB", e);
            }

            if (id < 0) return CardTerminal.FAILURE;

            shouldBePrintedOnInternalPrinter();

            paypoint.startAdmin(PayPoint.ADM_X_REPORT);

            return SUCCESS;
        } catch (TerminalInBankModeException e) {
            methodRejectCode = 114; // ERR_IN_BANK_MODE
            ErrorReporter.INSTANCE.filelog("verifone", "error calling xreport()", e);
        } catch (NoListenerRegisteredException e) {
            methodRejectCode = 126; // General error
            ErrorReporter.INSTANCE.filelog("verifone", "error calling xreport()", e);
        } catch (ComNotInitialisedException e) {
            methodRejectCode = 107; // ERR_COM_NOT_INITED
            ErrorReporter.INSTANCE.filelog("verifone", "error calling xreport()", e);
        } catch (TerminalInLocalModeException e) {
            ErrorReporter.INSTANCE.filelog("verifone", "error calling xreport()", e);
        } catch (Exception e) {
            methodRejectCode = 126; // General error
            ErrorReporter.INSTANCE.filelog("verifone", "error calling xreport()", e);
            e.printStackTrace();
        }

        return FAILURE;
    }

    @Override
    public int zreport(String operId) throws CardTerminalException {
        try {
            if (paypoint.isInBankMode()) {
                ErrorReporter.INSTANCE.filelog("verifone", "Terminal is in Bank Mode (zreport)");
                return FAILURE;
            }

            currentOperationUUID = UUID.randomUUID();
            TerminalAdminRequest adminRequest = new TerminalAdminRequest(currentOperationUUID.toString(), PayPoint.ADM_Z_REPORT, "0000");

            long id = -1;
            try {
                id = MainActivity.getInstance().getDbHelper().getWritableDatabase().insert(TerminalAdminRequest.Table.NAME, null, adminRequest.asContentValues());
            } catch (Exception e) {
                ErrorReporter.INSTANCE.filelog("verifone", "zreport -> Error on accessing DB", e);
            }

            if (id < 0) return CardTerminal.FAILURE;

            shouldBePrintedOnInternalPrinter();

            paypoint.startAdmin(PayPoint.ADM_Z_REPORT);

            return SUCCESS;
        } catch (TerminalInBankModeException e) {
            methodRejectCode = 114; // ERR_IN_BANK_MODE
            ErrorReporter.INSTANCE.filelog("verifone", "error calling zreport()", e);
        } catch (NoListenerRegisteredException e) {
            methodRejectCode = 126; // General error
            ErrorReporter.INSTANCE.filelog("verifone", "error calling zreport()", e);
        } catch (ComNotInitialisedException e) {
            methodRejectCode = 107; // ERR_COM_NOT_INITED
            ErrorReporter.INSTANCE.filelog("verifone", "error calling zreport()", e);
        } catch (TerminalInLocalModeException e) {
            ErrorReporter.INSTANCE.filelog("verifone", "error calling zreport()", e);
        } catch (Exception e) {
            methodRejectCode = 126; // General error
            ErrorReporter.INSTANCE.filelog("verifone", "error calling zreport()", e);
            e.printStackTrace();
        }

        return FAILURE;
    }

    @Override
    public int reversal(TerminalRequest terminalRequest) throws CardTerminalException {

        currentOperationUUID = UUID.randomUUID();
        terminalRequest.setUuid(currentOperationUUID.toString());
        try {

            if (paypoint.isInBankMode()) {
                ErrorReporter.INSTANCE.filelog("verifone", "Terminal is in Bank Mode (reversal)");
                return FAILURE;
            }

            long id = -1;
            try {
                id = MainActivity.getInstance().getDbHelper().getWritableDatabase().insert(TerminalRequest.Table.NAME, null,
                        terminalRequest.asContentValues());
            } catch (Exception e) {
                ErrorReporter.INSTANCE.filelog("verifone", "reversal -> Error on accessing DB", e);
            }

            if (id < 0) return CardTerminal.FAILURE;

            paypoint.startTransaction(
                    PayPoint.TRANS_REVERSAL,
                    terminalRequest.getTotalAmount(),
                    0,
                    PayPoint.MODE_NORMAL);

            return SUCCESS;

        } catch (UnknownTransactionCodeException e) {
            methodRejectCode = 111; // Illegal transaction type
            ErrorReporter.INSTANCE.filelog("verifone", "error calling reversal()", e);
        } catch (TerminalInBankModeException e) {
            methodRejectCode = 114; // ERR_IN_BANK_MODE
            ErrorReporter.INSTANCE.filelog("verifone", "error calling reversal()", e);
        } catch (NoListenerRegisteredException e) {
            methodRejectCode = 126; // General error
            ErrorReporter.INSTANCE.filelog("verifone", "error calling reversal()", e);
        } catch (IllegalAmountException e) {
            methodRejectCode = 113; // ERR_ILLEGAL_AMOUNT
            ErrorReporter.INSTANCE.filelog("verifone", "error calling reversal()", e);
        } catch (ComNotInitialisedException e) {
            methodRejectCode = 107; // ERR_COM_NOT_INITED
            ErrorReporter.INSTANCE.filelog("verifone", "error calling reversal()", e);
        } catch (UnknownModeException e) {
            methodRejectCode = 112; // Illegal mode value
            ErrorReporter.INSTANCE.filelog("verifone", "error calling reversal()", e);
        } catch (Exception e) {
            methodRejectCode = 126; // General error
            ErrorReporter.INSTANCE.filelog("verifone", "error calling reversal()", e);
            e.printStackTrace();
        }

        return FAILURE;
    }

    @Override
    public int returnOfGoods(TerminalRequest terminalRequest) throws CardTerminalException {

        currentOperationUUID = UUID.randomUUID();
        terminalRequest.setUuid(currentOperationUUID.toString());

        try {

            if (paypoint.isInBankMode()) {
                ErrorReporter.INSTANCE.filelog("verifone", "Terminal is in Bank Mode (returnOfGoods)");
                return FAILURE;
            }

            long id = -1;
            try {
                id = MainActivity.getInstance().getDbHelper().getWritableDatabase().insert(TerminalRequest.Table.NAME, null,
                        terminalRequest.asContentValues());
            } catch (Exception e) {
                ErrorReporter.INSTANCE.filelog("verifone", "returnOfGoods -> Error on accessing DB", e);
            }

            if (id < 0) return CardTerminal.FAILURE;

            paypoint.startTransaction(
                    PayPoint.TRANS_RETURN_GOODS,
                    Math.abs(terminalRequest.getTotalAmount()),
                    0,
                    PayPoint.MODE_NORMAL);

            return SUCCESS;

        } catch (UnknownTransactionCodeException e) {
            methodRejectCode = 111; // Illegal transaction type
            ErrorReporter.INSTANCE.filelog("verifone", "error calling returnOfGoods()", e);
        } catch (TerminalInBankModeException e) {
            methodRejectCode = 114; // ERR_IN_BANK_MODE
            ErrorReporter.INSTANCE.filelog("verifone", "error calling returnOfGoods()", e);
        } catch (NoListenerRegisteredException e) {
            methodRejectCode = 126; // General error
            ErrorReporter.INSTANCE.filelog("verifone", "error calling returnOfGoods()", e);
        } catch (IllegalAmountException e) {
            methodRejectCode = 113; // ERR_ILLEGAL_AMOUNT
            ErrorReporter.INSTANCE.filelog("verifone", "error calling returnOfGoods()", e);
        } catch (ComNotInitialisedException e) {
            methodRejectCode = 107; // ERR_COM_NOT_INITED
            ErrorReporter.INSTANCE.filelog("verifone", "error calling returnOfGoods()", e);
        } catch (UnknownModeException e) {
            methodRejectCode = 112; // Illegal mode value
            ErrorReporter.INSTANCE.filelog("verifone", "error calling returnOfGoods()", e);
        } catch (Exception e) {
            methodRejectCode = 126; // General error
            ErrorReporter.INSTANCE.filelog("verifone", "error calling returnOfGoods()", e);
            e.printStackTrace();
        }

        return FAILURE;
    }

    private int printPartial(String text) throws TerminalInLocalModeException, ComNotInitialisedException, IllegalInputFormatException, NoListenerRegisteredException, TerminalInBankModeException {

        try {

            if (paypoint.isInBankMode()) {
                ErrorReporter.INSTANCE.filelog("verifone", "Terminal is in Bank Mode (printPartial)");
                return FAILURE;
            }

            currentOperationUUID = UUID.randomUUID();
            TerminalAdminRequest adminRequest = new TerminalAdminRequest(currentOperationUUID.toString(),
                    PayPoint.ADM_TERM_PRINT, "0000");
            adminRequest.setOptional(text);

            long id = -1;
            try {
                id = MainActivity.getInstance().getDbHelper().getWritableDatabase().insert(TerminalAdminRequest.Table.NAME, null,
                        adminRequest.asContentValues());
            } catch (Exception e) {
                ErrorReporter.INSTANCE.filelog("verifone", "printOnTerminal -> Error on accessing DB", e);
            }

            if (id < 0) return CardTerminal.FAILURE;

            paypoint.setAdminData(text, 0x31);
            paypoint.startAdmin(PayPoint.ADM_TERM_PRINT);

            return SUCCESS;
        } catch (TerminalInBankModeException e) {
            methodRejectCode = 114; // ERR_IN_BANK_MODE
            ErrorReporter.INSTANCE.filelog("verifone", "error calling printPartial(" + text + ")", e);
            throw e;
        } catch (NoListenerRegisteredException | IllegalInputFormatException e) {
            methodRejectCode = 126; // General error
            ErrorReporter.INSTANCE.filelog("verifone", "error calling printPartial(" + text + ")", e);
            throw e;
        } catch (ComNotInitialisedException e) {
            methodRejectCode = 107; // ERR_COM_NOT_INITED
            ErrorReporter.INSTANCE.filelog("verifone", "error calling printPartial(" + text + ")", e);
            throw e;
        } catch (TerminalInLocalModeException e) {
            ErrorReporter.INSTANCE.filelog("verifone", "error calling printPartial(" + text + ")", e);
            throw e;
        } catch (Exception e) {
            methodRejectCode = 126; // General error
            ErrorReporter.INSTANCE.filelog("verifone", "error calling printPartial()", e);
            e.printStackTrace();
        }
        return FAILURE;
    }

    @Override
    public int printOnTerminal(String text) throws CardTerminalException {
        try {

            if (paypoint.isInBankMode()) {
                ErrorReporter.INSTANCE.filelog("verifone", "Terminal is in Bank Mode (printOnTerminal)");
                return FAILURE;
            }

            if (paypoint.isInBankMode()) return FAILURE;

            StringTokenizer st = new StringTokenizer(text, "\n", true);
            // cut by 1500 and print
            StringBuilder partial = new StringBuilder();
            while (st.hasMoreTokens()) {
                partial.append(st.nextToken());

                if (partial.length() >= 1400) {
                    printPartial(partial.toString());
                    partial = new StringBuilder();
                }
            }

            if (partial.length() > 0) {
                printPartial(partial.toString());
            }

            return SUCCESS;
        } catch (TerminalInBankModeException e) {
            methodRejectCode = 114; // ERR_IN_BANK_MODE
            ErrorReporter.INSTANCE.filelog("verifone", "error calling printOnTerminal()", e);
        } catch (NoListenerRegisteredException | IllegalInputFormatException e) {
            methodRejectCode = 126; // General error
            ErrorReporter.INSTANCE.filelog("verifone", "error calling printOnTerminal()", e);
        } catch (ComNotInitialisedException e) {
            methodRejectCode = 107; // ERR_COM_NOT_INITED
            ErrorReporter.INSTANCE.filelog("verifone", "error calling printOnTerminal()", e);
        } catch (TerminalInLocalModeException e) {
            ErrorReporter.INSTANCE.filelog("verifone", "error calling printOnTerminal()", e);
        } catch (Exception e) {
            methodRejectCode = 126; // General error
            ErrorReporter.INSTANCE.filelog("verifone", "error calling printOnTerminal()", e);
            e.printStackTrace();
        }

        return FAILURE;
    }

    @Override
    public boolean isConnected() {
        // Indicates if the communication channel is opened or not. TRUE if opened,
        // FALSE otherwise. Can be read at any time.
        return paypoint.isOpen();
    }

    public boolean isInBankMode() {
        // Indicates if the communication channel is opened or not. TRUE if opened,
        // FALSE otherwise. Can be read at any time.
        return paypoint.isInBankMode();
    }

    private void dispatchEvent(PayPointEvent event) {

        synchronized (listeners) {

            try {
                Looper.prepare();

                switch (event.getEventType()) {

                    case PayPointEvent.STATUS_EVENT:
                        // region STATUS_EVENT
                        PayPointStatusEvent statusEvent = (PayPointStatusEvent) event;
                        if (statusEvent.getStatusType() == PayPointStatusEvent.STATUS_DISPLAY) {

                            for (CardTerminalEventListener listener : listeners) {
                                listener.beforeAny();
                                listener.onDisplayText(new CardTerminalDisplayTextEvent(statusEvent.getSource()).setDisplayText(statusEvent.getStatusData()));
                                listener.afterAny();
                            }
                        } else if (statusEvent.getStatusType() == PayPointStatusEvent.STATUS_CARD_INFO) {
                            ErrorReporter.INSTANCE.filelog("verifone", "dispatchEvent -> cardInfo -> " + statusEvent.getStatusData());

                        } else if (statusEvent.getStatusType() == PayPointStatusEvent.STATUS_READY_FOR_TRANS) {
                            if (statusEvent.getStatusData().compareTo("1") == 0) {

                                for (CardTerminalEventListener listener : listeners) {
                                    listener.beforeAny();
                                    listener.onTerminalReady(new CardTerminalReadyEvent(statusEvent.getSource()));
                                    listener.afterAny();
                                }
                            }
                        } else if (statusEvent.getStatusType() == PayPointStatusEvent.STATUS_INPUT_APPROVAL_CODE) {
                            ErrorReporter.INSTANCE.filelog("verifone", "dispatchEvent -> STATUS_INPUT_APPROVAL_CODE ");
                        } else if (statusEvent.getStatusType() == PayPointStatusEvent.STATUS_INPUT_YES_NO) {
                            ErrorReporter.INSTANCE.filelog("verifone", "dispatchEvent -> STATUS_INPUT_YES_NO ");
                        }

                        break;
                    // endregion
                    case PayPointEvent.RESULT_EVENT:
                        // region RESULT_EVENT
                        PayPointResultEvent resultEvent = (PayPointResultEvent) event;
                        String receipt = resultEvent.getNormalPrint();

                        // not printing empty
                        if (receipt != null && receipt.trim().length() > 0) {

                            for (CardTerminalEventListener listener : listeners) {
                                listener.beforeAny();

                                listener.onPrintText(new CardTerminalPrintTextEvent(resultEvent.getSource())
                                        .setPrintText(receipt)
                                        .setSignaturePrint(resultEvent.getSignaturePrint()));

                                listener.afterAny();
                            }
                        }

                        //    <RESULT> 0x20 = indicates transaction OK.
                        //             0x21 = indicates transaction/operation rejected.
                        //             0x22 = indicates that additional authorisation is needed. (only relevant for adjustment)

                        //    <ACC>    0x20 = indicates standard update of accumulator.
                        //             0x22 = indicates that transaction is approved offline (and sent automatically later)
                        //             0x30 = indicates no update of accumulator.

                        TerminalResponse response = toTerminalResponse(resultEvent);
                        if (currentOperationUUID != null) {
                            response.setUuid(currentOperationUUID.toString());
                        }

                        switch (resultEvent.getResult()) {

                            case PayPointResultEvent.RESULT_OK:
                                CardTerminalTransactionCompleteEvent cardTerminalTransactionCompleteEvent =
                                        new CardTerminalTransactionCompleteEvent(resultEvent.getSource());

                                cardTerminalTransactionCompleteEvent.setTerminalResponse(response);

                                // save terminal response
                                if (currentOperationUUID != null) {
                                    try {
                                        MainActivity.getInstance().getDbHelper().getWritableDatabase().insert(TerminalResponse.Table.NAME, null, response.asContentValues());
                                    } catch (Exception e) {
                                        ErrorReporter.INSTANCE.filelog("BAXI", "LocalMode -> Error on accessing DB", e);
                                    }
                                }

                                for (CardTerminalEventListener listener : listeners) {
                                    listener.beforeAny();
                                    listener.onTransactionComplete(cardTerminalTransactionCompleteEvent);
                                    listener.afterAny();
                                }

                                break;

                            case PayPointResultEvent.RESULT_REJECTED:
                                CardTerminalTransactionFailedEvent cardTerminalTransactionFailedEvent = new CardTerminalTransactionFailedEvent(resultEvent.getSource());
                                cardTerminalTransactionFailedEvent.setTerminalResponse(response);

                                if (currentOperationUUID != null) {
                                    try {
                                        MainActivity.getInstance().getDbHelper().getWritableDatabase().insert(TerminalResponse.Table.NAME, null, response.asContentValues());
                                    } catch (Exception e) {
                                        ErrorReporter.INSTANCE.filelog("BAXI", "LocalMode -> Error on accessing DB", e);
                                    }
                                }

                                for (CardTerminalEventListener listener : listeners) {
                                    listener.beforeAny();
                                    listener.onTransactionFailed(cardTerminalTransactionFailedEvent);
                                    listener.afterAny();
                                }

                                break;
                        }

                        break;
                    // endregion
                }

            } catch (Exception e) {
                ErrorReporter.INSTANCE.filelog("verifone", "dispatchEvent error", e);
            }
        } // sync
    }

    private TerminalResponse toTerminalResponse(PayPointResultEvent event) {

        byte result = event.getResult();
        byte accumulator = event.getAccumulator();
        byte issuerId = event.getIssuerId();

        TerminalResponse terminalResponse = new TerminalResponse();
        terminalResponse.setResultData(event.getLocalModeData());

        // region data
        // ...
        // 3// 01************00198-1;
        // 4// 20160404140955;
        // 5// 0;
        // 6// 081;
        // 7// 001456017271;
        // 8// 0000;
        // 9// 00000049600;
        //10// ;
        //11// ;
        //12// ;
        //13// 111010;
        //14// 71408677;
        //15// ;
        //16// ;
        //17// ;
        //18// ;
        //19// ;
        //20// ;
        //21// ;
        //22// ;
        //23// 000000;
        //24// 0;
        //25// BankAxept;
        //26// D5780000021010;
        //27// 017271;
        //28// ;
        //29// ;
        //30// ;
        //31// ;
        //32// DSAFE99999999999997;
        // endregion

        switch (result) {

            case 0x20: // indicates transaction OK

                terminalResponse.setResult(CardTerminal.RESULT_ADMINISTRATIVE_TRANSACTION_OK);

                // 0x20 indicates standard update of accumulator
                // 0x22 indicates that transaction is approved offline (and sent automatically later)
                if (accumulator == 0x20 || accumulator == 0x22) {

                    terminalResponse.setResult(CardTerminal.RESULT_FINANCIAL_TRANSACTION_OK);

                    // Field id 0 is reserved for Result code
                    // Field id 1 is reserved for Accumulator
                    // For approved transactions field 2 will contain Issuer id: 2 digit issuer number

                    // Approved financial transaction (Result code is RESULT_OK and Accumulator is ACCU_APPROVED_ONLINE or ACCU_APPROVED_OFFLINE)

                    // Issuer id: 2 digit issuer number
                    terminalResponse.setIssuerId(issuerId);

                    // 3 -> Primary account number. Variable field length, max 19 digits. Will be masked.
                    // 4 -> Transaction timestamp in format YYYYMMDDhhmmss
                    terminalResponse.setTimestamp(event.getLocalModeFieldById(4));

                    // 5 -> Verification indicator
                    //    0x30: no signature panel
                    //    0x31: signature panel
                    //    0x32: loyalty
                    terminalResponse.setVerificationMethod(Integer.parseInt(event.getLocalModeFieldById(5)));

                    // 6 -> Session number, 3 digits
                    terminalResponse.setSessionNumber(event.getLocalModeFieldById(6));

                    // 7 -> Retrieval reference number, 12 digits
                    // 8 -> 0000 (4 bytes, not used)

                    // 9 ->  Total amount including tip entered by the customer on the terminal, 11 digits
                    terminalResponse.setTotalAmount(Integer.parseInt(event.getLocalModeFieldById(9)));

                    // 10 -> ****, 4 bytes. Present only for approved pre-authorisations, else empty
                    // 11 -> ***, 3 bytes. Present only for approved pre-authorisations, else empty
                    // 12 ->  Authorisation flag. Present only for approved pre-authorisations, else empty
                    //    0x30: online authorisation
                    //    0x31: manual authorisation

                    // 13 ->  Retailer id, 6 numeric digits 111010
                    terminalResponse.setAcquirerMerchantID(event.getLocalModeFieldById(13));
                    // 14 ->  Terminal id, 8 bytes 71408677
                    terminalResponse.setTerminalID(event.getLocalModeFieldById(14));

                    // 15 -> Authorisation status. Present only for approved pre-authorisations, else empty
                    //    0x30: signature on adjustment
                    //    0x31: additional authorisation performed, signature on adjustment
                    //    0x32: online pin entered, no signature on adjustment
                    //    0x33: offline pin entered, no signature on adjustment

                    // 16 -> Encrypted card data. Present only for approved pre-authorisations, else empty
                    // 17 -> EMV data. Present only for approved pre-authorisations, else empty
                    // 18 -> Checksum, 4 digits. Present only for approved pre-authorisations, else empty
                    // 19 -> Card product. Possible content: ICA, NK, BANK, AMX or DINERS (1)
                    // 20 -> Financial institution. Possible content: ICA, AMX, DIN, SWE, SEB, SHB, NOR, Ã–EB, FRI, SVB (1)
                    // 21 -> ECR transaction id. Range "1"-"255" (1)

                    // 22 ->  Encrypted card number (1)
                    //    Length of encrypted PAN, 2 numeric digits
                    //    Encrypted PAN, variable size
                    //    Key serial number, 20 bytes

                    // 23 -> Processing code, 6 numeric digits (1)  000000
                    // 24 -> Transaction type, the same value that was used as input to startTransaction when the transaction was started (1)
                    // 25 -> Application Label or Application Preferred name for chip, card/issuer name for magnetic stripe (1)  BankAxept
                    terminalResponse.setCardIssuerName(event.getLocalModeFieldById(25));

                    // 26 -> Application Identifier (only present for chip transactions) (1) D5780000021010
                    terminalResponse.setApplicationIdentifier(event.getLocalModeFieldById(26));

                    // 27 -> Authorisation code, 6 digits
                    terminalResponse.setStanAuth(event.getLocalModeFieldById(27));

                    // 28 -> Character string containing, 3 bytes. Containing POS entry mode, verification method and card authorisation channel. Used in Sweden
                    // 29 -> VAT amount, 11 digits

                    // 30 -> Encrypted bonus number (1)
                    //    Length of encrypted bonus number, 2 numeric digits
                    //    Encrypted bonus number, variable size
                    //    Key serial number, 20 bytes

                    // 31 -> Bonus amount, 11 digits

                    // 32 -> Card holder ID, 19 alphanumeric. Only present when received from NETS, else empty. DSAFE99999999999997
                    terminalResponse.setOptionalData("Card holder ID:" + event.getLocalModeFieldById(32));
                }

                break;
            case 0x21: // indicates transaction/operation rejected
                terminalResponse.setResult(CardTerminal.RESULT_TRANSACTION_REJECTED);

                // 3 -> Error type
                //    0x30: Unknown
                //    0x31: General terminal error
                //    0x32: Communication error
                //    0x33: Host rejected
                //    0x34: Cancelled by customer or operator
                //    0x35: Card removed
                //    0x36: Terminal busy
                //    0x37: Integration component error
                String errType = event.getLocalModeFieldById(3);

                if (errType != null) {
                    try {
                        terminalResponse.setRejectionSource(Integer.parseInt(errType));
                    } catch (Exception numex) {
                        ErrorReporter.INSTANCE.filelog("verifone", "error ", numex);
                        terminalResponse.setRejectionSource(0x30);
                    }
                }

                // todo check 4 -> Detailed error code. Variable size
                //terminalResponse.setRejectionReason(event.getLocalModeFieldById(4));

                break;
            case 0x22: // indicates that additional authorisation is needed (only relevant for adjustment)

                break;
        }

        // region docs
        //            String containing the additional result data. If present the first two bytes will always be:
        //
        //            ;	073	0x3B	0x3B		Semicolon
        //
        //            Result code
        //                0x20 indicates transaction OK
        //                0x21 indicates transaction/operation rejected
        //                0x22 indicates that additional authorisation is needed (only relevant for adjustment)
        //            Accumulator
        //                0x20 indicates standard update of accumulator
        //                0x22 indicates that transaction is approved offline (and sent automatically later)
        //                0x30 indicates no update of accumulator
        //
        //            The remaining content depends on transaction type and if it is approved or not:
        //
        //            Approved financial transaction (Result code is RESULT_OK and Accumulator is ACCU_APPROVED_ONLINE or ACCU_APPROVED_OFFLINE):
        //                Issuer id: 2 digit issuer number
        //                Primary account number. Variable field length, max 19 digits. Will be masked.
        //                0x3B separator
        //                Transaction timestamp in format YYYYMMDDhhmmss
        //                0x3B separator
        //                Verification indicator
        //                    0x30: no signature panel
        //                    0x31: signature panel
        //                    0x32: loyalty
        //                0x3B separator
        //                Session number, 3 digits
        //                0x3B separator
        //                Retrieval reference number, 12 digits
        //                0x3B separator
        //                0000 (4 bytes, not used)
        //                0x3B separator
        //                Total amount including tip entered by the customer on the terminal, 11 digits
        //                0x3B separator
        //                ****, 4 bytes. Present only for approved pre-authorisations, else empty
        //                0x3B separator
        //                ***, 3 bytes. Present only for approved pre-authorisations, else empty
        //                0x3B separator
        //                Authorisation flag. Present only for approved pre-authorisations, else empty
        //                    0x30: online authorisation
        //                    0x31: manual authorisation
        //                0x3B separator
        //                Retailer id, 6 numeric digits
        //                0x3B separator
        //                Terminal id, 8 bytes.
        //                0x3B separator
        //                Authorisation status. Present only for approved pre-authorisations, else empty
        //                    0x30: signature on adjustment
        //                    0x31: additional authorisation performed, signature on adjustment
        //                    0x32: online pin entered, no signature on adjustment
        //                    0x33: offline pin entered, no signature on adjustment
        //                0x3B separator
        //                Encrypted card data. Present only for approved pre-authorisations, else empty
        //                0x3B separator
        //                EMV data. Present only for approved pre-authorisations, else empty
        //                0x3B separator
        //                Checksum, 4 digits. Present only for approved pre-authorisations, else empty
        //                0x3B separator
        //                Card product. Possible content: ICA, NK, BANK, AMX or DINERS (1)
        //                0x3B separator
        //                Financial institution. Possible content: ICA, AMX, DIN, SWE, SEB, SHB, NOR, Ã–EB, FRI, SVB (1)
        //                0x3B separator
        //                ECR transaction id. Range "1"-"255" (1)
        //                0x3B separator
        //                Encrypted card number (1)
        //                    Length of encrypted PAN, 2 numeric digits
        //                    Encrypted PAN, variable size
        //                    Key serial number, 20 bytes
        //                0x3B separator
        //                Processing code, 6 numeric digits (1)
        //                0x3B separator
        //                Transaction type, the same value that was used as input to startTransaction when the transaction was started (1)
        //                0x3B separator
        //                Application Label or Application Preferred name for chip, card/issuer name for magnetic stripe (1)
        //                0x3B separator
        //                Application Identifier (only present for chip transactions) (1)
        //                0x3B separator
        //                Authorisation code, 6 digits
        //                0x3B separator
        //                Character string containing, 3 bytes. Containing POS entry mode, verification method and card authorisation channel. Used in Sweden
        //                0x3B separator
        //                VAT amount, 11 digits
        //                0x3B separator
        //                Encrypted bonus number (1)
        //                    Length of encrypted bonus number, 2 numeric digits
        //                    Encrypted bonus number, variable size
        //                    Key serial number, 20 bytes
        //                0x3B separator
        //                Bonus amount, 11 digits
        //                0x3B separator
        //                Card holder ID, 19 alphanumeric. Only present when received from NETS, else empty.
        //                0x3B separator
        //            Rejected financial transaction/administrative command, i.e. Result code is RESULT_REJECTED (1):
        //                Error type
        //                    0x30: Unknown
        //                    0x31: General terminal error
        //                    0x32: Communication error
        //                    0x33: Host rejected
        //                    0x34: Cancelled by customer or operator
        //                    0x35: Card removed
        //                    0x36: Terminal busy
        //                    0x37: Integration component error
        //                0x3B separator
        //                Detailed error code. Variable size
        //                0x3B separator
        //
        //            (1) ONLY RETURNED WHEN SPECIAL SW IN TERMINAL
        // endregion

        return terminalResponse;
    }

    /**
     * Listener method
     */
    @Override
    public void getPayPointEvent(PayPointEvent event) {
        dispatchEvent(event);
    }
}
