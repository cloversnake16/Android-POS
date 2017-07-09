package no.susoft.mobile.pos.network;

import java.util.Locale;

import no.susoft.mobile.pos.json.JSONSerializable;

/**
 * This class defines client/server communication protocol.
 * @author Yesod
 */
public final class Protocol {

	public final static String CHARSET = "UTF-8";
	public final static int PROTOCOL_MISMATCH = 600;
	public final static int PROTOCOL_INSECURE_CONNECTION = 601;

	/**
	 * static class.
	 */
	private Protocol() {
	}
	
	/**
	 * Supported client states.
	 * @author Yesod
	 */
	public enum State {
		BOOTSTRAP,
		NOTAUTHORIZED,
		AUTHORIZED
	}

	/**
	 * Supported server-side parameters.
	 * @author Yesod
	 */
	public enum Parameters {
		STATE,
		OPCODE,
		TYPE,
		TOKEN,
		BARCODE,
		TERM,
		ID,
		DEFAULT,
		ORIENTATION,
		LANDSCAPE,
		PORTRAIT,
		IMAGE,
		CHALLENGE,
		PROTOCOL,
		SHOP,
		CONSTRAINT,
		QUALITY,
		ENTITY,
		PARK,
		OFFER,
		INVOICE,
		TERMINAL,
		CASH,
		COMMIT,
		SUSPEND,
		MODE,
		QLM_GRID,
		QLM_CELL,
		SECURITYCODE,
		CHAIN,
		LICENSE,
		AREA,
		TABLE,
		TO_AREA,
		TO_TABLE,
		FROM_DATE,
		TO_DATE,
		KITCHEN,
		COMPLETE,
		FROM_AMOUNT,
		TO_AMOUNT,
		ALTERNATIVE,
		VERSION,
		CUSTOMER
	}
	
	/**
	 * Supported server-side operation codes.
	 * @author Yesod
	 */
	public enum OperationCode {
		REQUEST_SETTING,
		REQUEST_POINTS_OF_SALES,
		REQUEST_SEARCH,
		REQUEST_PRODUCT_SEARCH,
		REQUEST_IMAGE_ADD,
		REQUEST_IMAGE,
		REQUEST_IMAGE_SET,
		REQUEST_AUTHORIZATION,
		REQUEST_RESOLVE_BARCODE,
		REQUEST_INVENTORY_UPLOAD,
		REQUEST_DISCOUNT_REASONS,
		REQUEST_ORDER_LOAD,
		REQUEST_ORDER_UPLOAD,
		REQUEST_ORDER_DELETE,
		REQUEST_ORDER_LOCK,
		REQUEST_ORDER_MERGE,
		REQUEST_CUSTOMER_SUBMIT,
		REQUEST_EPROCUREMENT_UPLOAD,
		REQUEST_QUICK_LAUNCH_MENU,
		REQUEST_ACCOUNT_LIST,
		REQUEST_APPLY_SECURITYCODE,
		REQUEST_TABLE_FORMATION,
		REQUEST_PREPAID,
		REQUEST_FIND_PREPAID,
		REQUEST_SYNCHRONIZE,
		REQUEST_SYNCHRONIZE_ORDERS,
		REQUEST_SUPPLY_REPORT,
		REQUEST_CUSTOMER_NOTES_LOAD,
		REQUEST_CUSTOMER_NOTE_SAVE,
		REQUEST_RECEIPT_TEMPLATE,
		REQUEST_ORDERNOTE_TEMPLATE,
		REQUEST_REAUTHORIZATION,
		REQUEST_GENERATE_NUMBER,
		REQUEST_SYNCHRONIZE_RECONCILIATIONS,
		REQUEST_RELOAD_PRODUCTS
	}
	
	/**
	 * Supported client/server search entity types.
	 * @author Yesod
	 */
	public enum SearchEntity implements JSONSerializable {
		PRODUCT,
		CUSTOMER,
		ORDER,
		QLM_GRID,
		QLM_CELL
	}
	
	/**
	 * @author Yesod
	 */
	public enum SearchType implements JSONSerializable {
		ANY,
		PARKED,
		OFFERED,
		ORDER
	}
	
	/**
	 * Supported client/server search constraint types.
	 * @author Yesod
	 */
	public enum SearchConstraint implements JSONSerializable {
		NONE,
		ID,
		TERM,
		BARCODE,
		TABLE,
		SUSPEND,
		ALTERNATIVE,
		COMPLETE
	}
	
	/**
	 * Supported client/server quality types.
	 * @author Yesod
	 */
	public enum Quality implements JSONSerializable {
		THUMBNAIL,
		SMALL,
		DEFAULT,
		MEDIUM,
		LARGE
	}
	
	/**
	 * Supported client/server image types
	 * @author Yesod
	 */
	public enum ImageType implements JSONSerializable {
		PRODUCT,
		WEBSHOP,
		QLM_CELL
	}

	/**
	 * Supported client/server post response messages
	 */
	public enum Message implements JSONSerializable {
		OK,
		ERROR_ROUTED_RACE_CONDITION,
		ERROR_DUPLICATE_ATTEMPT,
		ERROR_UNRESOLVED_BARCODE,
		ERROR_UNEXPECTED
	}

	/**
	 * Format the string for parameterization.
	 * @param string
	 * @return
	 */
	public static String format(String string) {
		return string.toLowerCase(Locale.US);
	}
}