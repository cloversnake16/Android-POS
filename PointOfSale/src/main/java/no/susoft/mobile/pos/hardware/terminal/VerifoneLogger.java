package no.susoft.mobile.pos.hardware.terminal;

import android.os.Environment;
import no.point.paypoint.ILogger;
import no.susoft.mobile.pos.SusoftPOSApplication;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created on 4/13/2016.
 */
public class VerifoneLogger implements ILogger {

    private static final String LOG_NAME = "pimpoint.log";
    private static File logFile = null;
    private static final SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final boolean debug = true;

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

    @Override
    public void debug(Object o) {
        filelog(o.toString(), null);
    }

    @Override
    public void debug(Object o, Throwable throwable) {
        filelog(o.toString(), throwable);
    }

    @Override
    public void warn(Object o) {
        filelog(o.toString(), null);
    }

    @Override
    public void warn(Object o, Throwable throwable) {
        filelog(o.toString(), throwable);
    }

    @Override
    public void error(Object o) {
        filelog(o.toString(), null);
    }

    @Override
    public void error(Object o, Throwable throwable) {
        filelog(o.toString(), throwable);
    }

    @Override
    public void info(Object o) {
        filelog(o.toString(), null);
    }

    @Override
    public void info(Object o, Throwable throwable) {
        filelog(o.toString(), throwable);
    }

    public void filelog(String msg, Throwable ex) {
        if (debug) {
            try {
                writeFile(msg);

                if (ex != null) {

                    writeFile(ex.getMessage());

                    StringWriter sw = new StringWriter();

                    PrintWriter pw = new PrintWriter(sw);
                    ex.printStackTrace(pw);
                    writeFile(sw.toString());
                    pw.close();
                }
            } catch (Exception ignore) {
            }
        }
    }

    public void writeFile(String text) {

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

    @Override
    public boolean isDebugEnabled() {
        return true;
    }
}
