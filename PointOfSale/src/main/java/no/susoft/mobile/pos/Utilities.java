package no.susoft.mobile.pos;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import no.susoft.mobile.pos.data.Decimal;
import org.apache.commons.validator.routines.EmailValidator;

/**
 * This class contains static method routines offered as convenience for various procedures.
 */
public final class Utilities {

    private static int screenWidth = 0;
    private static int screenHeight = 0;

    private final static DecimalFormat CURRENCY_FORMAT = new DecimalFormat("###,##0.00");
    private final static DecimalFormat INTEGER_FORMAT = new DecimalFormat("#0");
    private final static DecimalFormat FRACTION_FORMAT = new DecimalFormat("00");

    // REGIX for no white space.
    private final static Pattern PATTERN_WHITESPACECLEAN = Pattern.compile("\\S+");

    /**
     * Allow this class to remain 'static'
     */
    private Utilities() {
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int getScreenHeight(Context c) {
        if (screenHeight == 0) {
            WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenHeight = size.y;
        }

        return screenHeight;
    }

    public static int getScreenWidth(Context c) {
        if (screenWidth == 0) {
            WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenWidth = size.x;
        }

        return screenWidth;
    }

    public static boolean isAndroid5() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * Return whether this input is clear of all whitespace.
     *
     * @param input
     * @return
     */
    public static boolean isWhiteSpaceClean(final String input) {
        return PATTERN_WHITESPACECLEAN.matcher(input).matches();
    }

    /**
     * Return whether this input is an acceptable email address.
     *
     * @param input
     * @return
     */
    public static boolean isValidEmail(final String input) {
        if (input == null)
            return false;
        //return pattern_email.matcher(input).matches();
        return EmailValidator.getInstance().isValid(input);
    }

    /**
     * Return whether this input is an acceptable phone number.
     *
     * @param input
     * @return
     */
    @Deprecated
    public static boolean isValidPhoneNumber(final String input) {
        return input != null;
    }

    /**
     * Hide the system soft keyboard.
     *
     * @param context
     */
    public static void hideSoftKeyboard(Context context) {
        InputMethodManager input = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        input.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    /**
     * Show the system soft keyboard.
     *
     * @param context
     */
    public static void showSoftKeyboard(Context context) {
        InputMethodManager input = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        input.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    /**
     * Display a simple alert dialog with an "OK" button to dismiss.
     *
     * @param context
     * @param message
     */
    public static void showSimpleDialogNotice(Context context, String message) {
        Builder dialog = new AlertDialog.Builder(context);
        dialog.setMessage(message);
        dialog.setPositiveButton("OK", null);
        dialog.create().show();
    }

    /**
     * Use this function to be explicit that the intention is to use an empty string
     * for the sake of code readability and clarity of intent.
     *
     * @return
     */
    public static String getStringEmpty() {
        return "";
    }

    /**
     * Use this function to be explicit that the intention is to use a null string
     * for the sake of code readability and clarity of intent.
     *
     * @return
     */
    public static String getStringNull() {
        return null;
    }

    /**
     * Invoke a manual unhandled exception. Listed as Deprecated for safety awareness.
     */
    @Deprecated
    public static void invokeManualUnhandledException() {
        System.out.println(1 / 0);
    }

    /**
     * Get the double representation of an integer currency value.
     * Example, an int of 1025 will return 10.25.
     *
     * @param value
     * @return
     */
    public double toCurrencyDouble(final int value) {
        return value / 100;
    }

    /**
     * Return whether the given value is less than zero.
     *
     * @param value
     * @return
     */
    public static boolean isNegative(final BigDecimal value) {
        return value == null || value.signum() == -1;
    }

    /**
     * Return whether the given value is equal to zero.
     *
     * @param value
     * @return
     */
    public static boolean isZero(final BigDecimal value) {
        return value == null || value.signum() == 0;
    }

    /**
     * Return whether the given value is greater than zero.
     *
     * @param value
     * @return
     */
    public static boolean isGreaterThanZero(final BigDecimal value) {
        return value == null || value.signum() == 1;
    }

    /**
     * Return whether the given parameters are equal.
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean isEqual(final BigDecimal a, final BigDecimal b) {
        return a.compareTo(b) == 0;
    }

    /**
     * @param value
     * @return
     */
    public static String toString(final BigDecimal value) {
        return value.toPlainString();
    }

    public static String formatCurrency(double d) {

        if (Double.isNaN(d) || Double.isInfinite(d))
            return "-";

        String res = "";
        try {
            res = CURRENCY_FORMAT.format(d);
        } catch (Exception e) {
        }
        return res;
    }

    public static String formatCurrency(Decimal d) {

        if (d == null)
            return "-";

        String res = "";
        try {
            res = CURRENCY_FORMAT.format(d.toDouble());
        } catch (Exception e) {
        }
        return res;
    }

    public static String formatInteger(double d) {

        if (Double.isNaN(d) || Double.isInfinite(d))
            return "-";

        String res = "";
        try {
            res = INTEGER_FORMAT.format(d);
        } catch (Exception e) {
        }
        return res;
    }

    public static String formatInteger(Decimal d) {

        if (d == null)
            return "-";

        String res = "";
        try {
            res = INTEGER_FORMAT.format(d.toInteger());
        } catch (Exception e) {
        }
        return res;
    }

    public static String formatFraction(Decimal d) {

        if (d == null)
            return "00";

        String res = "";
        try {
            d = d.remainder(Decimal.ONE);
            res = FRACTION_FORMAT.format(d.multiply(Decimal.HUNDRED).toInteger());
        } catch (Exception e) {
        }
        return res;
    }

    public static Object copy(Object orig) {
		Object obj = null;
		try {
			// Write the object out to a byte array
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			out.writeObject(orig);
			out.flush();
			out.close();

			// Make an input stream from the byte array and read
			// a copy of the object back in.
			ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
			obj = in.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj;
	}
}