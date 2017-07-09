package no.susoft.mobile.pos.hardware.terminal;

import java.util.Date;

import android.content.ContentValues;
import android.provider.BaseColumns;
import no.susoft.mobile.pos.db.DBHelper;
import no.susoft.mobile.pos.json.JSONSerializable;

/**
 * Created on 3/16/2016.
 */
public class TerminalAdminRequest implements JSONSerializable {

    /**
     * Unique operation id to have an asynchronous link between requests and responses
     */
    private String uuid = "";

    /**
     * Admin operation code
     */
    private int admCode;

    /**
     * The parameter operator ID has two different meanings:
     * 1. Single TID terminal - identify the operator initiating a transaction.
     * 2. Multi TID terminal - identify the logical terminal ID that shall be used in the transaction.
     * Legal values 1-10. 1 is the master TID.
     * If the parameter OperID is not used, it should be set to “0000”.
     */
    private String operID = "0000";

    private String optional = "";

    private TerminalAdminRequest(){}

    public TerminalAdminRequest(int admCode, String operID) {
        this.admCode = admCode;
        this.operID = operID;
    }

    public TerminalAdminRequest(String uuid, int admCode, String operID) {
        this.uuid = uuid;
        this.admCode = admCode;
        this.operID = operID;
    }

    public String getUuid() {
        return uuid;
    }

    public TerminalAdminRequest setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public int getAdmCode() {
        return admCode;
    }

    public TerminalAdminRequest setAdmCode(int admCode) {
        this.admCode = admCode;
        return this;
    }

    public String getOperID() {
        return operID;
    }

    public TerminalAdminRequest setOperID(String operID) {
        this.operID = operID;
        return this;
    }

    public String getOptional() {
        return optional;
    }

    public TerminalAdminRequest setOptional(String optional) {
        this.optional = optional;
        return this;
    }

    public ContentValues asContentValues() {

        ContentValues values = new ContentValues();

        values.put(Table.UUID, uuid);
        values.put(Table.OPER_ID, operID);
        values.put(Table.ADM_CODE, admCode);
        values.put(Table.OPTIONAL, optional);
        values.put(Table.CREATE_DATE, DBHelper.DB_TIMESTAMP_FORMAT.format(new Date()));
        values.put(Table.SENT_DATE, "");

        return values;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TerminalAdminRequest{");
        sb.append("uuid='").append(uuid).append('\'');
        sb.append(", admCode=").append(admCode);
        sb.append(", operID='").append(operID).append('\'');
        sb.append(", optional='").append(optional).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public static abstract class Table implements BaseColumns {

        public static final String NAME = "TERMINAL_ADMIN_REQUEST";

        public static final String UUID = "UUID";
        public static final String OPER_ID = "OPER_ID";
        public static final String ADM_CODE = "ADM_CODE";
        public static final String OPTIONAL = "OPTIONAL";
        public static final String CREATE_DATE = "CREATE_DATE";
        public static final String SENT_DATE = "SENT_DATE";

        String[] ALL = {
                _ID,
                UUID,
                OPER_ID,
                ADM_CODE,
                OPTIONAL,
                CREATE_DATE,
                SENT_DATE
        };
    }
}
