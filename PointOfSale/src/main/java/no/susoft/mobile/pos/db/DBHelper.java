package no.susoft.mobile.pos.db;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import no.susoft.mobile.pos.SusoftPOSApplication;
import no.susoft.mobile.pos.data.PeripheralDevice;
import no.susoft.mobile.pos.hardware.terminal.TerminalAdminRequest;
import no.susoft.mobile.pos.hardware.terminal.TerminalRequest;
import no.susoft.mobile.pos.hardware.terminal.TerminalResponse;

public class DBHelper extends SQLiteOpenHelper {

    public static final SimpleDateFormat DB_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
    public static final SimpleDateFormat DB_DATETIME_FORMAT = new SimpleDateFormat("yyyyMMddHHmm");
    public static final SimpleDateFormat DB_TIMESTAMP_FORMAT = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    public static final int DATABASE_VERSION = 14;
	public static final String DATABASE_NAME = "susoft_pos.db";

    private static final String TEXT = " TEXT";
    private static final String INT = " INTEGER";
    private static final String DECIMAL = " REAL";
    private static final String BLOB = " BLOB";

    private static final String COMMA = ",";

    // region create script
    private static final String[] SQL_CREATE_ENTRIES = new String[]{

            "CREATE TABLE PRODUCT (" +
                    "ID TEXT PRIMARY KEY NOT NULL, " +
                    "NAME TEXT, " +
                    "BARCODE TEXT, " +
                    "DESCRIPTION TEXT, " +
                    "TYPE TEXT, " +
                    "COST REAL, " +
                    "PRICE REAL, " +
                    "STOCK REAL, " +
                    "ABCCODE TEXT, " +
                    "VAT REAL, " +
                    "USE_ALTERNATIVE INTEGER DEFAULT 0, " +
                    "ALTERNATIVE_PRICE REAL, " +
                    "ALTERNATIVE_VAT REAL, " +
                    "TARE INTEGER, " +
                    "MISC INTEGER " +
			");",

            "CREATE TABLE ORDERHEADER (" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "SHOP TEXT NOT NULL, " +
                    "TYPE TEXT, " +
                    "POS TEXT, " +
                    "DATE TEXT, " +
                    "SALESPERSON TEXT, " +
                    "CUSTOMERID TEXT, " +
                    "AREAID INTEGER NOT NULL DEFAULT 0, " +
                    "TABLEID INTEGER NOT NULL DEFAULT 0, " +
                    "NOTE TEXT, " +
                    "USE_ALTERNATIVE INTEGER DEFAULT 0, " +
                    "REMOTEID INTEGER NOT NULL DEFAULT 0, " +
                    "ALTERNATIVEID TEXT, " +
                    "IS_SENT INTEGER DEFAULT 0, " +
                    "RECONCILIATION_ID TEXT " +
			");",

            "CREATE TABLE ORDERLINE (" +
					"ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "ORDERID INTEGER NOT NULL, " +
                    "SHOP TEXT NOT NULL, " +
                    "PRODUCT TEXT, " +
                    "TEXT TEXT, " +
                    "PRICE REAL, " +
                    "DISCOUNTREASON INTEGER, " +
                    "DISCOUNT REAL, " +
                    "QTY REAL, " +
                    "DELIVERED REAL, " +
                    "NOTE TEXT, " +
                    "SALESPERSON TEXT " +
			");",

            "CREATE TABLE PAYMENT (" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "ORDERID TEXT NOT NULL, " +
                    "SHOP TEXT NOT NULL, " +
                    "TYPE INTEGER, " +
                    "AMOUNT REAL, " +
                    "NUMBER TEXT, " +
                    "TERMINALTYPE INTEGER, " +
                    "CARDID INTEGER " +
			");",

            "CREATE TABLE TMPORDERHEADER (" +	//Temporary local storage until kitchen server not exists
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "SHOP TEXT NOT NULL, " +
                    "TYPE TEXT, " +
                    "POS TEXT, " +
                    "DATE TEXT, " +
                    "SALESPERSON TEXT, " +
                    "CUSTOMERID TEXT, " +
                    "AREAID INTEGER NOT NULL DEFAULT 0, " +
                    "TABLEID INTEGER NOT NULL DEFAULT 0, " +
                    "NOTE TEXT, " +
					"USE_ALTERNATIVE INTEGER DEFAULT 0, " +
                    "REMOTEID INTEGER NOT NULL DEFAULT 0, " +
					"ALTERNATIVEID TEXT " +
			");",

            "CREATE TABLE TMPORDERLINE (" +
					"ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "ORDERID INTEGER NOT NULL, " +
                    "SHOP TEXT NOT NULL, " +
                    "PRODUCT TEXT, " +
                    "TEXT TEXT, " +
                    "PRICE REAL, " +
                    "DISCOUNTREASON INTEGER, " +
                    "DISCOUNT REAL, " +
                    "QTY REAL, " +
                    "DELIVERED REAL, " +
                    "NOTE TEXT, " +
                    "SALESPERSON TEXT " +
			");",

            "CREATE TABLE QLM_MENU_GRID (" +
					"ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "X_SIZE INTEGER NOT NULL, " +
                    "Y_SIZE INTEGER NOT NULL, " +
                    "PARENT_CELL_ID INTEGER NOT NULL " +
			");",

            "CREATE TABLE QLM_CELL (" +
					"ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "PARENT_GRID_ID INTEGER NOT NULL, " +
                    "IDX INTEGER NOT NULL, " +
                    "TEXT TEXT, " +
                    "PRODUCT_ID TEXT, " +
                    "BARCODE TEXT " +
			");",

            "CREATE TABLE SHOP_AREA (" +
					"ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "NAME TEXT " +
			");",

            "CREATE TABLE SHOP_TABLE (" +
					"ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
					"AREAID INTEGER NOT NULL, " +
                    "NUMBER INTEGER NOT NULL, " +
                    "CAPACITY INTEGER NOT NULL " +
			");",

            "CREATE TABLE ACCOUNT (" +
                    "ID TEXT PRIMARY KEY NOT NULL, " +
                    "SHOP TEXT, " +
                    "NAME TEXT, " +
                    "USERNAME TEXT, " +
                    "PASSWORD TEXT, " +
                    "TYPE TEXT, " +
                    "SECURITYGROUP TEXT, " +
                    "SECURITYCODE TEXT " +
			");",

            "CREATE TABLE CUSTOMER (" +
                    "ID TEXT PRIMARY KEY NOT NULL, " +
                    "FIRSTNAME TEXT, " +
                    "LASTNAME TEXT, " +
                    "EMAIL TEXT, " +
                    "PHONE TEXT, " +
                    "MOBILE TEXT, " +
                    "COMPANY INTEGER NOT NULL DEFAULT 0 " +
			");",

            "CREATE TABLE PRODUCT_BUNDLE (" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "PRODUCT_ID TEXT, " +
                    "REF_ID TEXT, " +
                    "QTY REAL " +
			");",

            "CREATE TABLE TMPORDER_BUNDLE (" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "ORDERID INTEGER, " +
                    "SHOP TEXT, " +
                    "LINEID INTEGER, " +
                    "REFLINEID INTEGER " +
			");",
																	   
			"CREATE TABLE RECONCILIATION (" +
					"ID TEXT PRIMARY KEY NOT NULL, " +
					"SHOP TEXT, " +
					"CONTENT TEXT, " +
					"AMOUNT REAL, " +
					"IS_SENT NOT NULL DEFAULT 0 " +
			");",

            "CREATE TABLE " + TerminalRequest.Table.NAME + " (" +
                    TerminalRequest.Table._ID + INT + " PRIMARY KEY," +
                    TerminalRequest.Table.UUID + TEXT + COMMA +
                    TerminalRequest.Table.OPER_ID + TEXT + COMMA +
                    TerminalRequest.Table.TRANSFER_TYPE + INT + COMMA +
                    TerminalRequest.Table.TOTAL_AMOUNT + INT + COMMA +
                    TerminalRequest.Table.TOTAL_PURCHASE_AMOUNT + INT + COMMA +
                    TerminalRequest.Table.VAT_AMOUNT + INT + COMMA +
                    TerminalRequest.Table.HOST_DATA + TEXT + COMMA +
                    TerminalRequest.Table.ARTICLE_DETAILS + TEXT + COMMA +
                    TerminalRequest.Table.PAYMENT_CONDITION_CODE + TEXT + COMMA +
                    TerminalRequest.Table.OFFLINE_TRANSACTION_AUTH_CODE + TEXT + COMMA +
                    TerminalRequest.Table.OPTIONAL_DATA + TEXT + COMMA +
                    TerminalRequest.Table.CREATE_DATE + TEXT + COMMA +
                    TerminalRequest.Table.SENT_DATE + TEXT + " );",

            "CREATE TABLE " + TerminalResponse.Table.NAME + " (" +
                    TerminalResponse.Table._ID + INT + " PRIMARY KEY," +
                    TerminalResponse.Table.UUID + TEXT + COMMA +
                    TerminalResponse.Table.RESULT_DATA + TEXT + COMMA +
                    TerminalResponse.Table.RESULT + INT + COMMA +
                    TerminalResponse.Table.ISSUER_ID + INT + COMMA +
                    TerminalResponse.Table.TRUNCATED_PAN + TEXT + COMMA +
                    TerminalResponse.Table.TIMESTAMP + TEXT + COMMA +
                    TerminalResponse.Table.VERIFICATION_METHOD + INT + COMMA +
                    TerminalResponse.Table.SESSION_NUMBER + TEXT + COMMA +
                    TerminalResponse.Table.STAN_AUTH + TEXT + COMMA +
                    TerminalResponse.Table.SEQUENCE_NUMBER + TEXT + COMMA +
                    TerminalResponse.Table.TOTAL_AMOUNT + INT + COMMA +
                    TerminalResponse.Table.REJECTION_SOURCE + INT + COMMA +
                    TerminalResponse.Table.REJECTION_REASON + TEXT + COMMA +
                    TerminalResponse.Table.TIP_AMOUNT + INT + COMMA +
                    TerminalResponse.Table.SURCHARGE_AMOUNT + INT + COMMA +
                    TerminalResponse.Table.TERMINAL_ID + TEXT + COMMA +
                    TerminalResponse.Table.ACQUIRER_MERCHANT_ID + TEXT + COMMA +
                    TerminalResponse.Table.CARD_ISSUER_NAME + TEXT + COMMA +
                    TerminalResponse.Table.RESPONSE_CODE + TEXT + COMMA +
                    TerminalResponse.Table.APPLICATION_IDENTIFIER + TEXT + COMMA +
                    TerminalResponse.Table.TERMINAL_VERIFICATION_RESULT + TEXT + COMMA +
                    TerminalResponse.Table.TERMINAL_STATUS_INFORMATION + TEXT + COMMA +
                    TerminalResponse.Table.APPLICATION_TRANSACTION_COUNTER + TEXT + COMMA +
                    TerminalResponse.Table.APPLICATION_EFFECTIVE_DATA + TEXT + COMMA +
                    TerminalResponse.Table.ISSUER_ACTION_CODE + TEXT + COMMA +
                    TerminalResponse.Table.ORGANISATION_NUMBER + TEXT + COMMA +
                    TerminalResponse.Table.BANK_AGENT + TEXT + COMMA +
                    TerminalResponse.Table.ACCOUNT_TYPE + TEXT + COMMA +
                    TerminalResponse.Table.OPTIONAL_DATA + TEXT + COMMA +
                    TerminalResponse.Table.CREATE_DATE + TEXT + COMMA +
                    TerminalResponse.Table.SENT_DATE + TEXT + " );",

            "CREATE TABLE " + TerminalAdminRequest.Table.NAME + " (" +
                    TerminalAdminRequest.Table._ID + INT + " PRIMARY KEY," +
                    TerminalAdminRequest.Table.UUID + TEXT + COMMA +
                    TerminalAdminRequest.Table.ADM_CODE + INT + COMMA +
                    TerminalAdminRequest.Table.OPER_ID + TEXT + COMMA +
                    TerminalAdminRequest.Table.OPTIONAL + TEXT + COMMA +
                    TerminalAdminRequest.Table.CREATE_DATE + TEXT + COMMA +
                    TerminalAdminRequest.Table.SENT_DATE + TEXT + " );",

            "CREATE TABLE PARAMETER (" +
                    "KEY TEXT PRIMARY KEY," +
                    "VALUE TEXT );",

    };
    // endregion

    // region delete script
    private static final String[] SQL_DELETE_ENTRIES = new String[]{
            "DROP TABLE IF EXISTS PRODUCT;",
            "DROP TABLE IF EXISTS ORDERHEADER;",
            "DROP TABLE IF EXISTS ORDERLINE;",
            "DROP TABLE IF EXISTS PAYMENT;",
            "DROP TABLE IF EXISTS QLM_MENU_GRID;",
            "DROP TABLE IF EXISTS QLM_CELL;",
            "DROP TABLE IF EXISTS ACCOUNT;",
            "DROP TABLE IF EXISTS " + TerminalRequest.Table.NAME + ";",
            "DROP TABLE IF EXISTS " + TerminalResponse.Table.NAME + ";",
            "DROP TABLE IF EXISTS " + TerminalAdminRequest.Table.NAME + ";",
            "DROP TABLE IF EXISTS PARAMETER;"
    };
    // endregion

    public DBHelper(Context context) {
        super(context, getDatabasePath(context), null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String createEntry : SQL_CREATE_ENTRIES) {
            db.execSQL(createEntry);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch (oldVersion) {
			case 4:
				String sql = "" +
				"CREATE TABLE CUSTOMER (" +
					"ID TEXT PRIMARY KEY NOT NULL, " +
					"FIRSTNAME TEXT, " +
					"LASTNAME TEXT, " +
					"EMAIL TEXT, " +
					"PHONE TEXT, " +
					"MOBILE TEXT, " +
					"COMPANY INTEGER NOT NULL DEFAULT 0 " +
				");";
				db.execSQL(sql);
				db.execSQL("DELETE FROM PARAMETER WHERE KEY = 'LAST_SYNC_DATE';");

			case 5:
				db.execSQL("ALTER TABLE ORDERLINE ADD COLUMN DELIVERED REAL;");

			case 6:
				sql = "" +
				"CREATE TABLE SHOP_AREA (" +
						"ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
						"NAME TEXT " +
				");";
				db.execSQL(sql);

				sql = "" +
				"CREATE TABLE SHOP_TABLE (" +
						"ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
						"AREAID INTEGER NOT NULL, " +
						"NUMBER INTEGER NOT NULL, " +
						"CAPACITY INTEGER NOT NULL " +
				");";
				db.execSQL(sql);

				sql = "" +
				"CREATE TABLE TMPORDERHEADER (" +
						"ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
						"SHOP TEXT NOT NULL, " +
						"TYPE TEXT, " +
						"POS TEXT, " +
						"DATE TEXT, " +
						"SALESPERSON TEXT, " +
						"CUSTOMERID TEXT, " +
						"AREAID INTEGER NOT NULL DEFAULT 0, " +
						"TABLEID INTEGER NOT NULL DEFAULT 0, " +
						"NOTE TEXT, " +
						"REMOTEID INTEGER NOT NULL DEFAULT 0 " +
				");";
				db.execSQL(sql);

				sql = "" +
				"CREATE TABLE TMPORDERLINE (" +
						"ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
						"ORDERID INTEGER NOT NULL, " +
						"SHOP TEXT NOT NULL, " +
						"PRODUCT TEXT, " +
						"TEXT TEXT, " +
						"PRICE REAL, " +
						"DISCOUNTREASON INTEGER, " +
						"DISCOUNT REAL, " +
						"QTY REAL, " +
						"DELIVERED REAL, " +
						"NOTE TEXT " +
				");";
				db.execSQL(sql);
				db.execSQL("ALTER TABLE ORDERHEADER ADD COLUMN CUSTOMERID TEXT;");

			case 7:
				SharedPreferences preferences = SusoftPOSApplication.getContext().getSharedPreferences(PeripheralDevice.class.toString(), Context.MODE_PRIVATE);
				final SharedPreferences.Editor editor = preferences.edit();
				if (preferences.getInt("PRINTER_PROVIDER", 0) == 0) {
					editor.remove("PRINTER");
					editor.remove("PRINTER_PROVIDER");
					editor.remove("PRINTER_NAME");
					editor.remove("PRINTER_IP");
				}
				if (preferences.getInt("CARD_TERMINAL_PROVIDER", 0) == 0) {
					editor.remove("CARD_TERMINAL");
					editor.remove("CARD_TERMINAL_PROVIDER");
					editor.remove("CARD_TERMINAL_NAME");
					editor.remove("CARD_TERMINAL_IP");
				}
				if (preferences.getInt("CASHDRAWER_PROVIDER", 0) == 0) {
					editor.remove("CASHDRAWER");
					editor.remove("CASHDRAWER_PROVIDER");
					editor.remove("CASHDRAWER_NAME");
				}
				if (preferences.getInt("KITCHEN_PRINTER_PROVIDER", 0) == 0) {
					editor.remove("KITCHEN_PRINTER");
					editor.remove("KITCHEN_PRINTER_PROVIDER");
					editor.remove("KITCHEN_PRINTER_NAME");
					editor.remove("KITCHEN_PRINTER_IP");
				}
				if (preferences.getInt("BAR_PRINTER_PROVIDER", 0) == 0) {
					editor.remove("BAR_PRINTER");
					editor.remove("BAR_PRINTER_PROVIDER");
					editor.remove("BAR_PRINTER_NAME");
					editor.remove("BAR_PRINTER_IP");
				}
				if (preferences.getInt("DISPLAY_PROVIDER", 0) == 0) {
					editor.remove("DISPLAY");
					editor.remove("DISPLAY_PROVIDER");
					editor.remove("DISPLAY_NAME");
					editor.remove("DISPLAY_IP");
				}
				editor.commit();

				db.execSQL("ALTER TABLE ORDERHEADER ADD COLUMN USE_ALTERNATIVE INTEGER DEFAULT 0;");
				db.execSQL("ALTER TABLE TMPORDERHEADER ADD COLUMN USE_ALTERNATIVE INTEGER DEFAULT 0;");
				db.execSQL("ALTER TABLE PRODUCT ADD COLUMN USE_ALTERNATIVE INTEGER DEFAULT 0;");
				db.execSQL("ALTER TABLE PRODUCT ADD COLUMN ALTERNATIVE_PRICE REAL;");
				db.execSQL("ALTER TABLE PRODUCT ADD COLUMN ALTERNATIVE_VAT REAL;");

			case 8:
				db.execSQL("ALTER TABLE PRODUCT ADD COLUMN TARE INTEGER DEFAULT 0;");

			case 9:
				db.execSQL("ALTER TABLE ORDERLINE ADD COLUMN SALESPERSON TEXT;");
				db.execSQL("ALTER TABLE TMPORDERLINE ADD COLUMN SALESPERSON TEXT;");

			case 10:
				sql = "" +
				"CREATE TABLE PRODUCT_BUNDLE (" +
						"ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
						"PRODUCT_ID TEXT, " +
						"REF_ID TEXT, " +
						"QTY REAL " +
				");";
				db.execSQL(sql);

				sql = "" +
				"CREATE TABLE TMPORDER_BUNDLE (" +
						"ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
						"ORDERID INTEGER, " +
						"SHOP TEXT, " +
						"LINEID INTEGER, " +
						"REFLINEID INTEGER " +
				");";
				db.execSQL(sql);

			case 11:
				db.execSQL("ALTER TABLE ORDERHEADER ADD COLUMN ALTERNATIVEID TEXT;");
				db.execSQL("ALTER TABLE TMPORDERHEADER ADD COLUMN ALTERNATIVEID TEXT;");

			case 12:
				db.execSQL("ALTER TABLE ORDERHEADER ADD COLUMN IS_SENT INTEGER DEFAULT 0;");

			case 13:
				sql = "" +
				"CREATE TABLE RECONCILIATION (" +
						"ID TEXT PRIMARY KEY NOT NULL, " +
						"SHOP TEXT, " +
						"CONTENT TEXT, " +
						"AMOUNT REAL, " +
						"IS_SENT NOT NULL DEFAULT 0 " +
				");";
				db.execSQL(sql);
				
				db.execSQL("ALTER TABLE ORDERHEADER ADD COLUMN RECONCILIATION_ID TEXT;");
				db.execSQL("UPDATE ORDERHEADER SET RECONCILIATION_ID = '-1' WHERE RECONCILIATION_ID IS NULL;");

		}
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //onUpgrade(db, oldVersion, newVersion);
    }

	public static String getDatabasePath(Context context) {
		String folderPath = context.getCacheDir().getAbsolutePath() + "/" + DATABASE_NAME;
		if (context.getExternalCacheDir() != null) {
			folderPath = context.getExternalCacheDir().getAbsolutePath() + "/" + DATABASE_NAME;
		}

		Boolean isSDPresent = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		if (isSDPresent) {
			folderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + DATABASE_NAME;
		}

		//folderPath = context.getFilesDir().getAbsolutePath() + "/" + DATABASE_NAME;

		return folderPath;
	}

	public static String md5(String s) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
			digest.update(s.getBytes(Charset.forName("US-ASCII")), 0, s.length());
			byte[] magnitude = digest.digest();
			BigInteger bi = new BigInteger(1, magnitude);
			String hash = String.format("%0" + (magnitude.length << 1) + "x", bi);
			return hash;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
}
