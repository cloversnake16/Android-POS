package no.susoft.mobile.pos.hardware.terminal;

import java.util.EventObject;
import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import eu.nets.baxi.client.*;
import eu.nets.baxi.client.ef.BaxiEFEventListener;
import eu.nets.baxi.client.ef.CardInfoAllArgs;
import eu.nets.baxi.pcl.PCLReader;
import no.susoft.mobile.pos.SusoftPOSApplication;
import no.susoft.mobile.pos.data.PeripheralDevice;
import no.susoft.mobile.pos.data.PeripheralProvider;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.hardware.BluetoothDiscovery;
import no.susoft.mobile.pos.hardware.BluetoothDiscoveryEvent;
import no.susoft.mobile.pos.hardware.BluetoothDiscoveryListener;
import no.susoft.mobile.pos.hardware.printer.Printer;
import no.susoft.mobile.pos.hardware.printer.PrinterFactory;
import no.susoft.mobile.pos.hardware.printer.VerifonePrinter;
import no.susoft.mobile.pos.hardware.terminal.events.*;
import no.susoft.mobile.pos.hardware.terminal.exception.CardTerminalException;
import no.susoft.mobile.pos.hardware.terminal.exception.CardTerminalIllegalAmountException;
import no.susoft.mobile.pos.hardware.terminal.exception.CardTerminalInBankModeException;
import no.susoft.mobile.pos.hardware.terminal.exception.CardTerminalNotConnectedException;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import org.apache.commons.validator.routines.InetAddressValidator;

/**
 * Created on 2/10/2016.
 */
public class NetsTerminal extends CardTerminal implements BaxiEFEventListener {

    // region constants
    public static final int RECONCILIATION = 0x3130;// = RECONCILIATION function.

    // Cancellation request. The terminal has the choice to ignore the request.
    // The OnLocalMode result will determine the final status of the transaction.
    public static final int CANCELLATION = 0x3132;

    public static final int X_REPORT = 0x3136;// = X-report.
    public static final int Z_REPORT = 0x3137;// = Z-report.
    public static final int SEND_LATEST_FINANCIAL_TRANSACTION_RESULT = 0x313D;// = Send Latest Financial Transaction Result

    public static final int PRINT_STORED_REPORTS = 0x3030; // Print stored reports, if any
    public static final int TEST_COMMUNICATION_TO_HOST = 0x3031; // Test communication to host
    public static final int START_COPY_OF_LAST_RECONCILIATION = 0x3032; // Start copy of last reconciliation
    public static final int GET_LIST_OF_MERCHANT_IDS = 0x3033; // Get list of merchant IDs
    public static final int SWITCH_MERCHANT = 0x3034; // Switch merchant
    public static final int TOKEN_REQUEST = 0x3035; // Token request
    public static final int DELETE_TOKEN = 0x3036; // Delete token
    public static final int PRINT_ATTACHED_TEXT_ON_TERMINAL = 0x3038; // Print attached text on

    public static final int NOT_USED = 0x3039;// = not used

    public static final int EMPTY_TERMINAL_PRINT_BUFFER = 0x3131;// = Empty terminal print buffer.

    public static final int ANNUL = 0x3134;// = ANNUL from ECR should be mapped by ITU to perform REVERSAL transaction.
    public static final int BALANCE_INQUIRY = 0x3135;// = Balance Inquiry.
    public static final int SEND_OFFLINE_TRANSACTIONS_TO_HOST = 0x3138;// = Send Offline Transactions to HOST.
    public static final int PRINT_OF_STORED_EOT_TRANSACTIONS = 0x313A;// = Print of stored EOT transactions
    public static final int SILENTLY_FINISH_CURRENT_DIALOGUE = 0x313B;// = Finish current dialogue (behaves as Cancel, but silently).
    public static final int PRINT_LATEST_FINANCIAL_TRANSACTION_RECEIPT = 0x313C;// = Print Latest Financial Transaction Receipt
    public static final int START_SOFTWARE_DOWNLOAD = 0x313E;// = Start Software download
    public static final int START_DATASET_DOWNLOAD = 0x313F;// = Start Dataset download

    // endregion

    public static final String AUTHCODE_ALLOWED = "[a-zA-Z0-9]*";
    public static final int TYPE_NOT_IN_USE = 0x30;
    public static final int TYPE_VAT = 0x32;
    public static final String CARD_TERMINAL_NAME = "CARD_TERMINAL_NAME";
    private static NetsTerminal instance;

    protected boolean connected = false;
    protected boolean inBankMode = false;

    // 12.1.1 Error codes    Error code Description
    static {
        errorCodes.put(2011, "Error when sending message");
        errorCodes.put(6003, "Busy");
        errorCodes.put(6006, "Baxi locked");
        errorCodes.put(7001, "Unknown Error");
        errorCodes.put(7005, "Open rejected");
        errorCodes.put(7007, "Obsolete terminal version");
        errorCodes.put(7008, "No host contact.");
        errorCodes.put(7009, "No response from terminal");
        errorCodes.put(7010, "Create log directory fail");
        errorCodes.put(7011, "Open IO fail");
        errorCodes.put(7012, "Unexpected frame");
        errorCodes.put(7013, "Close rejected");
        errorCodes.put(7014, "No Response from Controller");
        errorCodes.put(7015, "Unknown terminal frame");
        errorCodes.put(7016, "Terminal Reboot Detected");
        errorCodes.put(2111, "Socket General Error");
        errorCodes.put(2112, "Socket Timeout");
        errorCodes.put(2113, "Socket Socket Error");
        errorCodes.put(2114, "Socket Message Length Error");
        errorCodes.put(2115, "Socket Sending of Message Failed");
        errorCodes.put(2116, "Socket Connection Error");

        // 12.1.2 Method reject codes    Method Reject code            Description
        // General cases

        errorCodes.put(7100, "Processing previous command");
        errorCodes.put(7101, "Unable to process");
        errorCodes.put(7102, "Already Open");
        errorCodes.put(7103, "Not Active");
        errorCodes.put(7104, "Terminal busy, administration");

        //Property bad values
        errorCodes.put(7401, "Log file prefix");
        errorCodes.put(7402, "Log file path");
        errorCodes.put(7403, "Host IP address");
        errorCodes.put(7404, "Vendor info extended");
        errorCodes.put(7405, "Trace level");
        errorCodes.put(7406, "Baud rate");

        // Method Reject code            Description
        errorCodes.put(7407, "COM port");
        errorCodes.put(7408, "Host port");
        errorCodes.put(7409, "Indicate EOT transactions");
        errorCodes.put(7410, "Cutter support");
        errorCodes.put(7411, "Printer width");
        errorCodes.put(7412, "Display width");
        errorCodes.put(7413, "Power cycle check");
        errorCodes.put(7414, "TID supervision");
        errorCodes.put(7415, "Auto get customer info");
        errorCodes.put(7416, "DeviceString");
        errorCodes.put(7420, "Terminal ready");
        errorCodes.put(7421, "UseDisplayTextID");
        errorCodes.put(7422, "UseExtendedLocalMode");
        errorCodes.put(7426, "DoNotSplitDisplayText");

        // Function argument bad values
        errorCodes.put(7501, "Only track 2 support");
        errorCodes.put(7502, "Invalid track length");
        errorCodes.put(7503, "Transfer amount invalid type");
        errorCodes.put(7504, "Transfer amount data too long");
        errorCodes.put(7505, "Transfer amount article details too long");
        errorCodes.put(7506, "Invalid operator ID");
        errorCodes.put(7507, "Invalid administration code");
        errorCodes.put(7508, "TLD unknown type");
        errorCodes.put(7509, "TLD bad field value");
        errorCodes.put(7510, "TLD could not build");
        errorCodes.put(7511, "Transfer amount PCC too long");
        errorCodes.put(7512, "Transfer amount PCC not alphanumeric");
        errorCodes.put(7513, "Transfer amount AuthCode too long.");
        errorCodes.put(7514, "Transfer amount AuthCode not alphanumeric.");
        errorCodes.put(7515, "JSON Bad field value");
        errorCodes.put(7516, "Transfer amount Optional Data too long.");

        // EF Cases
        errorCodes.put(7998, "EF Functionality is busy");
        errorCodes.put(7999, "EF Functionality is not activated");
    }

    protected UUID currentOperationUUID = UUID.randomUUID();

    private final BaxiCtrl baxiCtrl;

    private NetsTerminal() {

        ErrorReporter.INSTANCE.filelog("NetsTerminal", "Creating BaxiCtrl");
        terminalType = TERMINAL_NETS;

        this.baxiCtrl = new BaxiCtrl(MainActivity.getInstance());

        // add myself as listener to baxiCtrl
        this.baxiCtrl.addBaxiCtrlEventListener(this);

        SharedPreferences preferences = SusoftPOSApplication.getContext().getSharedPreferences(PeripheralDevice.class.toString(), Context.MODE_PRIVATE);

        int peripheralProvider = preferences.getInt("CARD_TERMINAL_PROVIDER", PeripheralProvider.NONE.ordinal());
        PCLReader reader = new PCLReader(MainActivity.getInstance());
        String ip = preferences.getString("CARD_TERMINAL_IP", "");
        ErrorReporter.INSTANCE.filelog("NetsTerminal", "ip=" + ip);

        if (peripheralProvider == PeripheralProvider.NETS.ordinal()) {
            if (ip.trim().length() == 0) {

                final BluetoothDiscovery bluetoothDiscovery = BluetoothDiscovery.getInstance();
                ErrorReporter.INSTANCE.filelog("NetsTerminal", "Creating PCLReader");

                final String selectedCardReaderName = preferences.getString(CARD_TERMINAL_NAME, "");
                ErrorReporter.INSTANCE.filelog("NetsTerminal", "selectedCardReaderName : " + selectedCardReaderName);

                // region Listener

                bluetoothDiscovery.addListener(new BluetoothDiscoveryListener() {

                    private PCLReader reader;

                    public PCLReader getReader() {
                        return reader;
                    }

                    public BluetoothDiscoveryListener setReader(PCLReader reader) {
                        this.reader = reader;
                        return this;
                    }

                    @Override
                    public void onBluetoothDiscoveryEvent(BluetoothDiscoveryEvent event) {

                        switch (event.getEventType()) {

                            case BluetoothDevice.ACTION_FOUND:

                                // update list with new device found
                                BluetoothDevice device = event.getDevice();
                                ErrorReporter.INSTANCE.filelog("NetsTerminal", "Found device : " + device.getName());

                                if (device.getBondState() == BluetoothDevice.BOND_BONDED && device.getName().equals(selectedCardReaderName)) {
                                    ErrorReporter.INSTANCE.filelog("NetsTerminal", "Set current reader : " + device.getName());
                                    reader.setCurrentReader(device.getAddress());
                                }

                                break;
                            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                                ErrorReporter.INSTANCE.filelog("NetsTerminal", "ACTION_DISCOVERY_FINISHED");
                                boolean connected = false;

                                if (bluetoothDiscovery.isIdle()) {
                                    List<BluetoothDevice> lst = bluetoothDiscovery.getDiscoveredDevices();

                                    for (BluetoothDevice bluetoothDevice : lst) {

                                        if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED && bluetoothDevice.getName().equals(selectedCardReaderName)) {
                                            ErrorReporter.INSTANCE.filelog("NetsTerminal", "Set current reader : " + bluetoothDevice.getName());
                                            reader.setCurrentReader(bluetoothDevice.getAddress());
                                            connected = true;
                                        }
                                    }

                                    if (!connected) {
                                        List<BluetoothDevice> paired = bluetoothDiscovery.listPairedDevices();

                                        for (BluetoothDevice bluetoothDevice : paired) {
                                            if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED && bluetoothDevice.getName().equals(selectedCardReaderName)) {
                                                ErrorReporter.INSTANCE.filelog("NetsTerminal", "Set current reader : " + bluetoothDevice.getName());
                                                reader.setCurrentReader(bluetoothDevice.getAddress());
                                                connected = true;
                                            }
                                        }
                                    }

                                    bluetoothDiscovery.removeListener(this);
                                }

                                if (!connected) {
                                    ErrorReporter.INSTANCE.filelog("NetsTerminal", "Standalone mode, no card terminal attached...");
                                }

                                break;
                        }
                    }
                }.setReader(reader)).discoverDevices();
                // endregion

            } else {
                ErrorReporter.INSTANCE.filelog("NetsTerminal", "connected with cable -> " + ip);

                // region connected with cable (IP)
                boolean isIpAddress = InetAddressValidator.getInstance().isValidInet4Address(ip);
                if (isIpAddress) {
                    // IP connection
                    Log.i("vilde", "ip: " + ip);
                    //baxiCtrl.setHostIpAddress(ip);
                    //baxiCtrl.setSocketListenerPort(6001);
                    baxiCtrl.setSerialDriver("ip");
                    try {
                        ErrorReporter.INSTANCE.filelog("NetsTerminal", "is open: " + isOpen());
                    } catch (CardTerminalException e) {
                        e.printStackTrace();
                    }
                    try {
                        Log.i("vilde", "is open: " + isOpen());
                    } catch (CardTerminalException e) {
                        e.printStackTrace();
                    }
                } else {
                    // USB
                    baxiCtrl.setDeviceString("SAGEM MONETEL USB Telium");
                }
                // endregion
            }

            baxiCtrl.open();
        }
    }

    public static CardTerminal getInstance() {

        if (instance == null) {
            ErrorReporter.INSTANCE.filelog("Init NetsTerminal instance");
            instance = new NetsTerminal();
        }

        return instance;
    }

    public UUID getCurrentOperationUUID() {
        return currentOperationUUID;
    }

    @Override
    public int getMethodRejectCode() {
        return baxiCtrl.getMethodRejectCode();
    }

    @Override
    public void testConnectionAndReconnectIfLost(String caller) throws CardTerminalException {

        if (!isConnected()) {

            try {
                baxiCtrl.close();
                baxiCtrl.open();
            } catch (Exception e) {
                if (!isConnected()) {
                    throw new CardTerminalNotConnectedException(e);
                }
            }
        }
    }

    @Override
    public int open() throws CardTerminalException {

        currentOperationUUID = UUID.randomUUID();

        int val = CardTerminal.SUCCESS;
        if (!baxiCtrl.isOpen()) {

            ErrorReporter.INSTANCE.filelog("NETS", "open() -> Connecting to card terminal...");

            val = baxiCtrl.open() == CardTerminal.SUCCESS
                    ? CardTerminal.SUCCESS
                    : baxiCtrl.getMethodRejectCode();

            if (val != CardTerminal.SUCCESS) throw getExceptionByCode(val);
        }

        return val;
    }

    private CardTerminalException getExceptionByCode(int val) {

        switch (val) {
            case 7005: //"Open rejected");
            case 7008: //"No host contact.");
            case 7009: //"No response from terminal");
            case 7013: //"Close rejected");
            case 7014: //"No Response from Controller");
            case 7016: //"Terminal Reboot Detected");
            case 2111: //"Socket General Error");
            case 2112: //"Socket Timeout");
            case 2113: //"Socket Socket Error");
            case 2114: //"Socket Message Length Error");
            case 2115: //"Socket Sending of Message Failed");
            case 2116: //"Socket Connection Error");
                return new CardTerminalNotConnectedException(errorCodes.get(val));

            case 7503: // "Transfer amount invalid type");
            case 7504: // "Transfer amount data too long");
            case 7505: // "Transfer amount article details too long");
            case 7511: //"Transfer amount PCC too long");
            case 7512: //"Transfer amount PCC not alphanumeric");
            case 7513: //"Transfer amount AuthCode too long.");
            case 7514: //"Transfer amount AuthCode not alphanumeric.");
            case 7516: //"Transfer amount Optional Data too long.");
                return new CardTerminalIllegalAmountException(errorCodes.get(val));

            default:
                return isInBankMode()
                        ? new CardTerminalInBankModeException(errorCodes.get(val))
                        : new CardTerminalException(errorCodes.get(val));
        }
    }

    @Override
    public int close() throws CardTerminalException {
        if (!isConnected()) throw new CardTerminalNotConnectedException("Not connected");

        if (baxiCtrl.isOpen()) {
            int val = baxiCtrl.close() == SUCCESS ? SUCCESS : baxiCtrl.getMethodRejectCode();

            if(val != CardTerminal.SUCCESS) throw getExceptionByCode(val);
        }

        return SUCCESS; // already closed
    }

    @Override
    public boolean isOpen() throws CardTerminalException {
        return baxiCtrl.isOpen();
    }

    @Override
    public int reversal(TerminalRequest terminalRequest) throws CardTerminalException {

        // set UUID for ongoing operation
        currentOperationUUID = UUID.randomUUID();
        terminalRequest.setUuid(currentOperationUUID.toString());

        try {
            MainActivity.getInstance().getDbHelper().getWritableDatabase().insert(TerminalRequest.Table.NAME, null, terminalRequest.asContentValues());
        } catch (Exception e) {
            ErrorReporter.INSTANCE.filelog("NETS", "reversal->Error on accessing DB", e);
        }

        int val =  doTransferAmount(terminalRequest);
        if(val != CardTerminal.SUCCESS) throw getExceptionByCode(val);

        return CardTerminal.SUCCESS;
    }

    @Override
    public int returnOfGoods(TerminalRequest terminalRequest) throws CardTerminalException {

        terminalRequest.setTransferType(TerminalRequest.TRANSFER_TYPE_RETURN_OF_GOODS);
        terminalRequest.setTotalAmount(Math.abs(terminalRequest.getTotalAmount())); // positive
        terminalRequest.setTotalPurchaseAmount(0);
        terminalRequest.setVatAmount(0);
        terminalRequest.setType3(0x30);

        // set UUID for ongoing operation
        currentOperationUUID = UUID.randomUUID();
        terminalRequest.setUuid(currentOperationUUID.toString());

        try {
            MainActivity.getInstance().getDbHelper().getWritableDatabase().insert(TerminalRequest.Table.NAME, null, terminalRequest.asContentValues());
        } catch (Exception e) {
            ErrorReporter.INSTANCE.filelog("NETS", "returnOfGoods->Error on accessing DB", e);
        }

        int val = doTransferAmount(terminalRequest);
        if(val != CardTerminal.SUCCESS) throw getExceptionByCode(val);

        return CardTerminal.SUCCESS;
    }

    @Override
    public int printOnTerminal(String text) throws CardTerminalException {
        return FAILURE; //not supported
    }

    @Override
    public int purchase(TerminalRequest terminalRequest) throws CardTerminalException {

        // set UUID for ongoing operation
        currentOperationUUID = UUID.randomUUID();
        terminalRequest.setUuid(currentOperationUUID.toString());

        if (!isConnected()) throw new CardTerminalNotConnectedException("Not connected");

        // early checks
        if (terminalRequest.getTotalPurchaseAmount() > 0
                && terminalRequest.getTransferType() != TerminalRequest.TRANSFER_TYPE_PURCHASE_WITH_CASH_BACK)
            return 90001;

        if (!terminalRequest.getOfflineTransactionAuthCode().matches(AUTHCODE_ALLOWED)) return 90002;

        long id = -1;
        try {
            id = MainActivity.getInstance().getDbHelper().getWritableDatabase().insert(TerminalRequest.Table.NAME, null, terminalRequest.asContentValues());
        } catch (Exception e) {
            ErrorReporter.INSTANCE.filelog("NETS", "purchase -> Error on accessing DB", e);
        }

        if (id < 0) return CardTerminal.FAILURE;

        int val =  doTransferAmount(terminalRequest);
        if(val != CardTerminal.SUCCESS) throw getExceptionByCode(val);

        return CardTerminal.SUCCESS;
    }

    public int reconciliation(String operId) throws CardTerminalException {
        int val =  administration(new TerminalAdminRequest(RECONCILIATION, operId));
        if(val != CardTerminal.SUCCESS) throw getExceptionByCode(val);

        return CardTerminal.SUCCESS;
    }

    public int xreport(String operId) throws CardTerminalException {
        int val =  administration(new TerminalAdminRequest(X_REPORT, operId));
        if(val != CardTerminal.SUCCESS) throw getExceptionByCode(val);

        return CardTerminal.SUCCESS;
    }

    public int zreport(String operId) throws CardTerminalException {
        int val =  administration(new TerminalAdminRequest(Z_REPORT, operId));
        if(val != CardTerminal.SUCCESS) throw getExceptionByCode(val);

        return CardTerminal.SUCCESS;
    }

    public int cancel(String operId) throws CardTerminalException {

        int val =  administration(new TerminalAdminRequest(CANCELLATION, operId));
        if(val != CardTerminal.SUCCESS) throw getExceptionByCode(val);

        return CardTerminal.SUCCESS;
    }

    public int sendLastFinancialResult(String operId) throws CardTerminalException {

        int val =  administration(new TerminalAdminRequest(SEND_LATEST_FINANCIAL_TRANSACTION_RESULT, operId));
        if(val != CardTerminal.SUCCESS) throw getExceptionByCode(val);

        return CardTerminal.SUCCESS;
    }

    @Override
    public int printLastCardPaymentTransaction(String operId) throws CardTerminalException {
        final Printer p = PrinterFactory.getInstance().getPrinter();
        currentOperationUUID = UUID.randomUUID();

        // 0x313C = Print Latest Financial Transaction Receipt
        int val =  administration(new TerminalAdminRequest(PRINT_LATEST_FINANCIAL_TRANSACTION_RECEIPT, operId));
        if(val != CardTerminal.SUCCESS) throw getExceptionByCode(val);

        return CardTerminal.SUCCESS;
    }

    private int administration(TerminalAdminRequest adminRequest) {
        inBankMode = true;

        // set UUID for ongoing operation
        currentOperationUUID = UUID.randomUUID();
        adminRequest.setUuid(currentOperationUUID.toString());
        ErrorReporter.INSTANCE.filelog("NETS", "reconciliation -> " + adminRequest);

        try {
            MainActivity.getInstance().getDbHelper().getWritableDatabase().insert(TerminalAdminRequest.Table.NAME, null,
                    adminRequest.asContentValues());
        } catch (Exception e) {
            ErrorReporter.INSTANCE.filelog("NETS", "reconciliation -> Error on accessing DB", e);
        }

        return doAdministration(new AdministrationArgs(
                adminRequest.getAdmCode(),
                adminRequest.getOperID(),
                "")
        );
    }

    private int doAdministration(AdministrationArgs administrationArgs) {
        inBankMode = true;

        ErrorReporter.INSTANCE.filelog("NETS", "-> doAdministration");

        ErrorReporter.INSTANCE.filelog("NETS", asString(administrationArgs));
        int result = baxiCtrl.administration(administrationArgs);

        ErrorReporter.INSTANCE.filelog("NETS", "<- doAdministration");
        return result;
    }

    private TransferAmountArgs asTransferAmountArgs(TerminalRequest terminalRequest) {

        //        TransferAmountArgs a = new TransferAmountArgs();
        //        a.OperID = "0000";
        //        a.Type1 = 0x30;
        //        a.Amount1 = 1000;
        //        a.Type2 = 0x30;
        //        a.Amount2 = 0;
        //        a.Type3 = 0x30;
        //        a.Amount3 = 0;
        //        a.HostData = "";
        //        a.ArticleDetails = "";
        //        a.PaymentConditionCode = "";
        //        a.AuthCode = "";

        TransferAmountArgs a = new TransferAmountArgs();

        String operID = terminalRequest.getOperID();
        if (operID != null && operID.trim().length() > 0) a.setOperID(operID);
        else a.setOperID("0000");

        int transferType = terminalRequest.getTransferType();
        if (transferType != -1) {
            a.setType1(transferType);
        } else {
            a.setType1(TerminalRequest.TRANSFER_TYPE_PURCHASE);
        }

        int totalAmount = terminalRequest.getTotalAmount();
        if (totalAmount > 0) {
            a.setAmount1(totalAmount);
        } else {
            a.setAmount1(0);
        }

        int totalPurchaseAmount = terminalRequest.getTotalPurchaseAmount();
        if (totalPurchaseAmount > 0) {
            a.setType2(TYPE_NOT_IN_USE);
            a.setAmount2(totalPurchaseAmount);
        } else {
            a.setType2(TYPE_NOT_IN_USE);
            a.setAmount2(0);
        }

        int vatAmount = terminalRequest.getVatAmount();
        if (vatAmount > 0) {
            a.setType3(TYPE_VAT);
            a.setAmount3(vatAmount);
        } else {
            a.setType3(TYPE_NOT_IN_USE);
            a.setAmount3(0);
        }

        String hostData = terminalRequest.getHostData();
        if (hostData != null && hostData.trim().length() > 0)
            a.setHostData(hostData);
        else a.setHostData("");

        String articleDetails = terminalRequest.getArticleDetails();
        if (articleDetails != null && articleDetails.trim().length() > 0)
            a.setArticleDetails(articleDetails);
        else a.setArticleDetails("");

        String paymentConditionCode = terminalRequest.getPaymentConditionCode();
        if (paymentConditionCode != null && paymentConditionCode.trim().length() > 0)
            a.setPaymentConditionCode(paymentConditionCode);
        else a.setPaymentConditionCode("");

        String offlineTransactionAuthCode = terminalRequest.getOfflineTransactionAuthCode();
        if (offlineTransactionAuthCode != null && offlineTransactionAuthCode.trim().length() > 0)
            a.setAuthCode(offlineTransactionAuthCode);
        else a.setAuthCode("");

        String optionalData = terminalRequest.getOptionalData();
        if (optionalData != null && optionalData.trim().length() > 0)
            a.setOptionalData(optionalData);
        else a.setOptionalData("");

        return a;
    }

    @Override
    public boolean isConnected() {
        return baxiCtrl != null && connected;
    }

    @Override
    public boolean isInBankMode() {
        return baxiCtrl != null && inBankMode;
    }

    /**
     * @return 1 on success, local mode will follow,
     * 0 or error code on failure, no local mode
     */
    private int doTransferAmount(TerminalRequest terminalRequest) {
        inBankMode = true;

        // build args from TerminalRequest
        TransferAmountArgs a = asTransferAmountArgs(terminalRequest);
        return baxiCtrl.transferAmount(a);
    }

    // region Event listener dispatcher

    @Override
    public void OnCardInfoAll(CardInfoAllArgs cardInfoAllArgs) {
        dispatchEvent(cardInfoAllArgs);
    }

    @Override
    public void OnStdRspReceived(StdRspReceivedEventArgs stdRspReceivedEventArgs) {
        dispatchEvent(stdRspReceivedEventArgs);
    }

    @Override
    public void OnPrintText(PrintTextEventArgs printTextEventArgs) {
        dispatchEvent(printTextEventArgs);
    }

    @Override
    public void OnDisplayText(DisplayTextEventArgs displayTextEventArgs) {
        dispatchEvent(displayTextEventArgs);
    }

    /**
     * When BAXI Android has lost communication with the terminal during a transaction, the OnLocalMode event will be triggered,
     * with the local mode result set to an unknown status. The status of the transaction must then be investigated!
     *
     * @param localModeEventArgs
     */
    @Override
    public void OnLocalMode(LocalModeEventArgs localModeEventArgs) {
        inBankMode = false;
        dispatchEvent(localModeEventArgs);
    }

    /**
     * WARNING: This functionality is only supported on terminal versions 07.1X and higher. It must be turned off for older releases.
     * This is an optional special purpose event. It triggers each time the terminal determines that it has completed terminal management.
     * Typically, this will occur as the terminal goes into idle after an activity, like a purchase.
     *
     * @param terminalReadyEventArgs
     */
    @Override
    public void OnTerminalReady(TerminalReadyEventArgs terminalReadyEventArgs) {
        inBankMode = false;
        dispatchEvent(terminalReadyEventArgs);
    }

    @Override
    public void OnTLDReceived(TLDReceivedEventArgs tldReceivedEventArgs) {
        dispatchEvent(tldReceivedEventArgs);
    }

    @Override
    public void OnLastFinancialResult(LastFinancialResultEventArgs lastFinancialResultEventArgs) {
        dispatchEvent(lastFinancialResultEventArgs);
    }

    @Override
    public void OnJsonReceived(JsonReceivedEventArgs jsonReceivedEventArgs) {
        dispatchEvent(jsonReceivedEventArgs);
    }

    @Override
    public void OnBarcodeReader(BarcodeReaderEventArgs barcodeReaderEventArgs) {

        // BUG found BarcodeReaderEventArgs is not extending EventObject
        //dispatchEvent(barcodeReaderEventArgs);

        ErrorReporter.INSTANCE.filelog("NETS", "Event listener OnBarcodeReader not overridden by underlying implementation!");
    }

    /**
     * This is a general event sent when BAXI.Android sets up a connection with a terminal.
     */
    @Override
    public void OnConnected() {
        connected = true;
        dispatchEvent(new CardTerminalConnectedEvent(this));
    }

    /**
     * This is a general event sent when BAXI.Android loses connection with a terminal.
     */
    @Override
    public void OnDisconnected() {
        connected = false;
        dispatchEvent(new CardTerminalDisconnectedEvent(this));
    }

    @Override
    public void OnBaxiError(BaxiErrorEventArgs baxiErrorEventArgs) {
        dispatchEvent(baxiErrorEventArgs);
    }

    private void dispatchEvent(EventObject event) {

//        ErrorReporter.INSTANCE.filelog("NETS", "dispatchEvent -> " + event.toString());

        synchronized (listeners) {
            try {
                Object eventObj = event.getSource();

                for (CardTerminalEventListener listener : listeners) {

                    listener.beforeAny();

                    if (event instanceof CardTerminalConnectedEvent) {

                        listener.onConnected((CardTerminalConnectedEvent) event);
                    } else if (event instanceof CardTerminalDisconnectedEvent) {

                        listener.onDisconnected((CardTerminalDisconnectedEvent) event);
                    } else if (event instanceof BaxiErrorEventArgs) {

                        listener.onError(new CardTerminalErrorEvent(eventObj)
                                .setErrorCode(((BaxiErrorEventArgs) event).getErrorCode())
                                .setErrorString(((BaxiErrorEventArgs) event).getErrorString()));
                    } else if (event instanceof TerminalReadyEventArgs) {
						inBankMode = false;
                        listener.onTerminalReady(new CardTerminalReadyEvent(eventObj));
                    } else if (event instanceof LocalModeEventArgs) {

                        // every operation ends with Local Mode
                        // region LocalModeEventArgs

                        inBankMode = false;
                        LocalModeEventArgs args = (LocalModeEventArgs) event;
                        int result = args.getResult();

                        switch (result) {
                            case CardTerminal.RESULT_FINANCIAL_TRANSACTION_OK:
                            case CardTerminal.RESULT_ADMINISTRATIVE_TRANSACTION_OK:
                            case CardTerminal.RESULT_TRANSACTION_IS_LOYALTY:

                                CardTerminalTransactionCompleteEvent transactionCompleteEvent =
                                        new CardTerminalTransactionCompleteEvent(eventObj);

                                if (currentOperationUUID == null) {
                                    currentOperationUUID = UUID.randomUUID();
                                }

                                // region set
                                TerminalResponse response = transactionCompleteEvent.getTerminalResponse()
                                        .setUuid(currentOperationUUID.toString())
                                        .setAccountType(args.getAccountType())
                                        .setAcquirerMerchantID(args.getAcquirerMerchantID())
                                        .setApplicationEffectiveData(args.getAED())
                                        .setApplicationIdentifier(args.getAID())
                                        .setApplicationTransactionCounter(args.getATC())
                                        .setBankAgent(args.getBankAgent())
                                        .setCardIssuerName(args.getCardIssuerName())
                                        .setIssuerActionCode(args.getIAC())
                                        .setIssuerId(args.getIssuerId())
                                        .setOptionalData(args.getOptionalData())
                                        .setOrganisationNumber(args.getOrganisationNumber())
                                        .setRejectionReason(args.getRejectionReason())
                                        .setRejectionSource(args.getRejectionSource())
                                        .setResponseCode(args.getResponseCode())
                                        .setResult(args.getResult())
                                        .setResultData(args.getResultData())
                                        .setSequenceNumber(args.getSequenceNumber())
                                        .setSessionNumber(args.getSessionNumber())
                                        .setStanAuth(args.getStanAuth())
                                        .setSurchargeAmount(args.getSurchargeAmount())
                                        .setTerminalID(args.getTerminalID())
                                        .setTimestamp(args.getTimestamp())
                                        .setTipAmount(args.getTipAmount())
                                        .setTotalAmount(args.getTotalAmount())
                                        .setTruncatedPan(args.getTruncatedPan())
                                        .setTerminalStatusInformation(args.getTSI())
                                        .setTerminalVerificationResult(args.getTVR())
                                        .setVerificationMethod(args.getVerificationMethod());
                                // endregion

                                try {
                                    MainActivity.getInstance().getDbHelper().getWritableDatabase().insert(TerminalResponse.Table.NAME, null, response.asContentValues());
                                } catch (Exception e) {
                                    ErrorReporter.INSTANCE.filelog("NETS", "LocalMode -> Error on accessing DB", e);
                                }

                                listener.onTransactionComplete(transactionCompleteEvent);
                                break;

                            default:
                            case CardTerminal.RESULT_TRANSACTION_REJECTED:

                            case CardTerminal.RESULT_UNKNOWN:
                                // When BAXI Android has lost communication with the terminal during a transaction, the OnLocalMode
                                // event will be triggered, with the local mode result set to an unknown status.

                                CardTerminalTransactionFailedEvent transactionFailedEvent = new CardTerminalTransactionFailedEvent(eventObj);

                                // region set

                                response = transactionFailedEvent.getTerminalResponse()
                                        .setUuid(currentOperationUUID.toString())
                                        .setAccountType(args.getAccountType())
                                        .setAcquirerMerchantID(args.getAcquirerMerchantID())
                                        .setApplicationEffectiveData(args.getAED())
                                        .setApplicationIdentifier(args.getAID())
                                        .setApplicationTransactionCounter(args.getATC())
                                        .setBankAgent(args.getBankAgent())
                                        .setCardIssuerName(args.getBankAgent())
                                        .setCardIssuerName(args.getCardIssuerName())
                                        .setIssuerActionCode(args.getIAC())
                                        .setIssuerId(args.getIssuerId())
                                        .setOptionalData(args.getOptionalData())
                                        .setOrganisationNumber(args.getOrganisationNumber())
                                        .setRejectionReason(args.getRejectionReason())
                                        .setRejectionSource(args.getRejectionSource())
                                        .setResponseCode(args.getResponseCode())
                                        .setResult(args.getResult())
                                        .setResultData(args.getResultData())
                                        .setSequenceNumber(args.getSequenceNumber())
                                        .setSessionNumber(args.getSessionNumber())
                                        .setStanAuth(args.getStanAuth())
                                        .setSurchargeAmount(args.getSurchargeAmount())
                                        .setTerminalID(args.getTerminalID())
                                        .setTimestamp(args.getTimestamp())
                                        .setTipAmount(args.getTipAmount())
                                        .setTotalAmount(args.getTotalAmount())
                                        .setTruncatedPan(args.getTruncatedPan())
                                        .setTerminalStatusInformation(args.getTSI())
                                        .setTerminalVerificationResult(args.getTVR())
                                        .setVerificationMethod(args.getVerificationMethod());
                                // endregion

                                try {
                                    MainActivity.getInstance().getDbHelper().getWritableDatabase().insert(TerminalResponse.Table.NAME, null, response.asContentValues());
                                } catch (Exception e) {
                                    ErrorReporter.INSTANCE.filelog("NETS", "RejectedOrUnknown -> Error on accessing DB", e);
                                }

                                listener.onTransactionFailed(transactionFailedEvent);
                                break;
                        }

                        // endregion
                    } else if (event instanceof LastFinancialResultEventArgs) {

                        // region LastFinancialResultEventArgs
                        CardTerminalLastFinancialResultEvent e = new CardTerminalLastFinancialResultEvent(eventObj);

                        LastFinancialResultEventArgs args = (LastFinancialResultEventArgs) event;

                        // region set
                        TerminalResponse response = e.getTerminalResponse()
                                .setUuid(currentOperationUUID.toString())
                                .setAccountType(args.getAccountType())
                                .setAcquirerMerchantID(args.getAcquirerMerchantID())
                                .setApplicationEffectiveData(args.getAED())
                                .setApplicationIdentifier(args.getAID())
                                .setApplicationTransactionCounter(args.getATC())
                                .setBankAgent(args.getBankAgent())
                                .setCardIssuerName(args.getBankAgent())
                                .setCardIssuerName(args.getCardIssuerName())
                                .setIssuerActionCode(args.getIAC())
                                .setIssuerId(args.getIssuerId())
                                .setOptionalData(args.getOptionalData())
                                .setOrganisationNumber(args.getOrganisationNumber())
                                .setRejectionReason(args.getRejectionReason())
                                .setRejectionSource(args.getRejectionSource())
                                .setResponseCode(args.getResponseCode())
                                .setResult(args.getResult())
                                .setResultData(args.getResultData())
                                .setSequenceNumber(args.getSequenceNumber())
                                .setSessionNumber(args.getSessionNumber())
                                .setStanAuth(args.getStanAuth())
                                .setSurchargeAmount(args.getSurchargeAmount())
                                .setTerminalID(args.getTerminalID())
                                .setTimestamp(args.getTimestamp())
                                .setTipAmount(args.getTipAmount())
                                .setTotalAmount(args.getTotalAmount())
                                .setTruncatedPan(args.getTruncatedPan())
                                .setTerminalStatusInformation(args.getTSI())
                                .setTerminalVerificationResult(args.getTVR())
                                .setVerificationMethod(args.getVerificationMethod());
                        // endregion

                        try {
                            MainActivity.getInstance().getDbHelper().getWritableDatabase().insert(TerminalResponse.Table.NAME, null, response.asContentValues());
                        } catch (Exception ex) {
                            ErrorReporter.INSTANCE.filelog("NETS", "Last financial result -> Error on accessing DB", ex);
                        }

                        listener.onLastFinancialResult(new CardTerminalLastFinancialResultEvent(eventObj));

                        // endregion
                    } else if (event instanceof PrintTextEventArgs) {

                        listener.onPrintText(new CardTerminalPrintTextEvent(eventObj)
                                .setPrintText(((PrintTextEventArgs) event).getPrintText()));
                    } else if (event instanceof DisplayTextEventArgs) {

                        listener.onDisplayText(new CardTerminalDisplayTextEvent(eventObj)
                                .setDisplayText(((DisplayTextEventArgs) event).getDisplayText())
                                .setDisplayTextID(((DisplayTextEventArgs) event).getDisplaytextID())
                                .setDisplayTextSourceID(((DisplayTextEventArgs) event).getDisplaytextSourceID()));
                    }

                    listener.afterAny();

                }
            } catch (Exception e) {
                ErrorReporter.INSTANCE.filelog("NETS", "dispatchEvent error", e);
            }
        } // sync
    }

    // endregion
}

