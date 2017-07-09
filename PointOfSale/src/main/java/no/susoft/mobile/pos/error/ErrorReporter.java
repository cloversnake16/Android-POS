package no.susoft.mobile.pos.error;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;
import android.util.Log;
import no.susoft.mobile.pos.SusoftPOSApplication;

/**
 * This class handles error reporting routines. It is highly encouraged to route /all/ exceptions
 * messages to this class.
 *
 * @author Yesod
 */
public enum ErrorReporter {
    INSTANCE;

    /*
     * A temporary location where I will place error codes. Keeping error codes in one accessible
     * location expedites finding sources of an error by its message.
     */
    public final static String ResponceReadError = "";

    private static final boolean debug = true;

	private static String LOG_NAME = "susoft.log";
    private static File logFile = null;
    private static SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	static {
		try {
			String folderPath = SusoftPOSApplication.getContext().getCacheDir().getAbsolutePath() + "/" + LOG_NAME;
			if (SusoftPOSApplication.getContext().getExternalCacheDir() != null) {
				folderPath = SusoftPOSApplication.getContext().getExternalCacheDir().getAbsolutePath() + "/" + LOG_NAME;
			}

			Boolean isSDPresent = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
			if (isSDPresent) {
				folderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + LOG_NAME;
			}

			logFile = new File(folderPath);
			if (!logFile.exists()) {
				logFile.createNewFile();
			}
		} catch (IOException ignore) {
		}
	}

    /**
     * Singleton instance.
     */
    private ErrorReporter() {
    }

    public <T> void reportError(Class<T> source, String message, Exception x) {
        Log.e(source.toString(), message, x);
    }

    public void filelog(String msg) {
        if (!debug) return;

        System.out.println(msg);
		writeFile(msg);
    }

    public void filelog(String tag, String msg) {
        if (!debug) return;

        Log.d(tag, msg);
        writeFile("<" + tag + "> " + msg);
    }

    public void filelog(String tag, String msg, Throwable ex) {
        if (!debug) return;

        try {
            StringWriter sw = new StringWriter();

            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            writeFile("<" + tag + "> " + msg + " " + ex.getMessage());
            writeFile(sw.toString());

            pw.close();

        } catch (Exception ignore) {
        }
    }

    public void filelog(Throwable ex) {
        if (!debug) return;

        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            writeFile(sw.toString());
            pw.close();
        } catch (Exception ignore) {
        }
    }

    public void writeFile(String text) {

        if (!debug || logFile == null) return;

        BufferedWriter buf = null;

        try {
            buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(logDateFormat.format(new Date())).append(" - ").append(text);
            buf.newLine();
        } catch (IOException ignore) {
        } finally {
            if (buf != null) {
                try {
                    buf.flush();
                    buf.close();
                } catch (IOException ignore) {
                }
            }
        }
    }
}