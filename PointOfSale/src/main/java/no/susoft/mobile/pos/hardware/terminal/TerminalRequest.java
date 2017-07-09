package no.susoft.mobile.pos.hardware.terminal;

import java.util.Date;

import android.content.ContentValues;
import android.provider.BaseColumns;
import no.susoft.mobile.pos.db.DBHelper;
import no.susoft.mobile.pos.json.JSONSerializable;

/**
 * Created on 3/4/2016.
 */
public class TerminalRequest implements JSONSerializable {

    // region constants
    public static final int TRANSFER_TYPE_PURCHASE = 0x30;
    public static final int TRANSFER_TYPE_RETURN_OF_GOODS = 0x31;
    public static final int TRANSFER_TYPE_REVERSAL = 0x32;
    public static final int TRANSFER_TYPE_PURCHASE_WITH_CASH_BACK = 0x33;
    public static final int TRANSFER_TYPE_AUTHORISATION = 0x34;

    public static final int TRANSFER_TYPE_ADJUSTMENT = 0x35; // verifone

    public static final int TRANSFER_TYPE_BALANCE_INQUIRY = 0x36;
    public static final int TRANSFER_TYPE_DEPOSIT = 0x38;
    public static final int TRANSFER_TYPE_CASH_WITHDRAWAL = 0x39;
    public static final int TRANSFER_TYPE_FORCE_OFFLINE = 0x40;
    public static final int TRANSFER_TYPE_INCREMENT_PRE_AUTHORISATION = 0x41;
    public static final int TRANSFER_TYPE_REVERSAL_PRE_AUTHORISATION = 0x42;
    public static final int TRANSFER_TYPE_SALE_COMPLETION_PRE_AUTHORISATION = 0x43;

    // endregion

    /**
     * Unique operation id to have an asynchronous link between requests and responses
     */
    private String uuid = "";

    /**
     * The parameter operator ID has two different meanings:
     * 1. Single TID terminal - identify the operator initiating a transaction.
     * 2. Multi TID terminal - identify the logical terminal ID that shall be used in the transaction.
     * Legal values 1-10. 1 is the master TID.
     * If the parameter OperID is not used, it should be set to “0000”.
     */
    private String operID = "0000";

    /**
     * See TRANSFER_TYPE_* constants
     */
    private int transferType = TRANSFER_TYPE_PURCHASE;

    /**
     * Total amount
     */
    private int totalAmount = 0;

    /**
     * Total purchase amount
     * if transferType = TRANSFER_TYPE_PURCHASE_WITH_CASH_BACK
     */
    private int totalPurchaseAmount = 0;

    /**
     * VAT (Value added tax) amount.
     */
    private int type3 = 0x32;

    private int vatAmount = 0;

    private String hostData = "";
    private String articleDetails = "";

    /**
     * Variable field length, max 3 alphanumeric char.
     */
    private String paymentConditionCode = "";

    /**
     * Authorisation Code field. Variable field length, max 6 characters.
     * The field is used for Force Offline transactions
     * Lower and upper case is allowed. Special characters like ‘@#£¤%&’ are NOT allowed.
     */
    private String offlineTransactionAuthCode = "";

    /**
     * Variable field, alphanumeric data. Each record is Optional Data field. Variable field length, max 1024 ASCII characters.
     */
    private String optionalData = "";

    public TerminalRequest() {
    }

    // region boilerplate

    public String getUuid() {
        return uuid;
    }

    public TerminalRequest setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getOperID() {
        return operID;
    }

    public TerminalRequest setOperID(String operID) {
        this.operID = operID;
        return this;
    }

    public int getTransferType() {
        return transferType;
    }

    public TerminalRequest setTransferType(int transferType) {
        this.transferType = transferType;
        return this;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public TerminalRequest setTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
        return this;
    }

    public int getTotalPurchaseAmount() {
        return totalPurchaseAmount;
    }

    public TerminalRequest setTotalPurchaseAmount(int totalPurchaseAmount) {
        this.totalPurchaseAmount = totalPurchaseAmount;
        return this;
    }

    public int getType3() {
        return type3;
    }

    public TerminalRequest setType3(int type3) {
        this.type3 = type3;
        return this;
    }

    public int getVatAmount() {
        return vatAmount;
    }

    public TerminalRequest setVatAmount(int vatAmount) {
        this.vatAmount = vatAmount;
        return this;
    }

    public String getHostData() {
        return hostData;
    }

    public TerminalRequest setHostData(String hostData) {
        this.hostData = hostData;
        return this;
    }

    public String getArticleDetails() {
        return articleDetails;
    }

    public TerminalRequest setArticleDetails(String articleDetails) {
        this.articleDetails = articleDetails;
        return this;
    }

    public String getPaymentConditionCode() {
        return paymentConditionCode;
    }

    public TerminalRequest setPaymentConditionCode(String paymentConditionCode) {
        this.paymentConditionCode = paymentConditionCode;
        return this;
    }

    public String getOfflineTransactionAuthCode() {
        return offlineTransactionAuthCode;
    }

    public TerminalRequest setOfflineTransactionAuthCode(String offlineTransactionAuthCode) {
        this.offlineTransactionAuthCode = offlineTransactionAuthCode;
        return this;
    }

    public String getOptionalData() {
        return optionalData;
    }

    public TerminalRequest setOptionalData(String optionalData) {
        this.optionalData = optionalData;
        return this;
    }

    // endregion

    public ContentValues asContentValues() {

        ContentValues values = new ContentValues();

        values.put(Table.UUID, uuid);
        values.put(Table.OPER_ID, operID);
        values.put(Table.TRANSFER_TYPE, transferType);
        values.put(Table.TOTAL_AMOUNT, totalAmount);
        values.put(Table.TOTAL_PURCHASE_AMOUNT, totalPurchaseAmount);
        values.put(Table.VAT_AMOUNT, vatAmount);
        values.put(Table.HOST_DATA, hostData);
        values.put(Table.ARTICLE_DETAILS, articleDetails);
        values.put(Table.PAYMENT_CONDITION_CODE, paymentConditionCode);
        values.put(Table.OFFLINE_TRANSACTION_AUTH_CODE, offlineTransactionAuthCode);
        values.put(Table.OPTIONAL_DATA, optionalData);

        values.put(Table.CREATE_DATE, DBHelper.DB_TIMESTAMP_FORMAT.format(new Date()));
        values.put(Table.SENT_DATE, "");

        return values;
    }

    public static abstract class Table implements BaseColumns {

        public static final String NAME = "TERMINAL_REQUEST";

        public static final String UUID = "UUID";
        public static final String OPER_ID = "OPER_ID";
        public static final String TRANSFER_TYPE = "TRANSFER_TYPE";
        public static final String TOTAL_AMOUNT = "TOTAL_AMOUNT";
        public static final String TOTAL_PURCHASE_AMOUNT = "TOTAL_PURCHASE_AMOUNT";
        public static final String VAT_AMOUNT = "VAT_AMOUNT";
        public static final String HOST_DATA = "HOST_DATA";
        public static final String ARTICLE_DETAILS = "ARTICLE_DETAILS";
        public static final String PAYMENT_CONDITION_CODE = "PAYMENT_CONDITION_CODE";
        public static final String OFFLINE_TRANSACTION_AUTH_CODE = "OFFLINE_TRANSACTION_AUTH_CODE";
        public static final String OPTIONAL_DATA = "OPTIONAL_DATA";
        public static final String CREATE_DATE = "CREATE_DATE";
        public static final String SENT_DATE = "SENT_DATE";

        String[] ALL = {
                _ID,
                UUID,
                OPER_ID,
                TRANSFER_TYPE,
                TOTAL_AMOUNT,
                TOTAL_PURCHASE_AMOUNT,
                VAT_AMOUNT,
                HOST_DATA,
                ARTICLE_DETAILS,
                PAYMENT_CONDITION_CODE,
                OFFLINE_TRANSACTION_AUTH_CODE,
                OPTIONAL_DATA,
                CREATE_DATE,
                SENT_DATE
        };
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TerminalRequest{");
        sb.append("uuid='").append(uuid).append('\'');
        sb.append(", operID='").append(operID).append('\'');
        sb.append(", transferType=").append(transferType);
        sb.append(", totalAmount=").append(totalAmount);
        sb.append(", totalPurchaseAmount=").append(totalPurchaseAmount);
        sb.append(", type3=").append(type3);
        sb.append(", vatAmount=").append(vatAmount);
        sb.append(", hostData='").append(hostData).append('\'');
        sb.append(", articleDetails='").append(articleDetails).append('\'');
        sb.append(", paymentConditionCode='").append(paymentConditionCode).append('\'');
        sb.append(", offlineTransactionAuthCode='").append(offlineTransactionAuthCode).append('\'');
        sb.append(", optionalData='").append(optionalData).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
