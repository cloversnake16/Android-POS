package no.susoft.mobile.pos;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.multidex.MultiDex;
import org.acra.ACRA;

//@ReportsCrashes(
//	               formUri = "https://susoft.cloudant.com/acra-susoft/_design/acra-storage/_update/report",
//	               reportType = HttpSender.Type.JSON,
//	               httpMethod = HttpSender.Method.POST,
//	               formUriBasicAuthLogin = "ainderediermingthetchady",
//	               formUriBasicAuthPassword = "UPSQ5vcPoJ21wBakKOdMxjS1",
//	               formKey = "",
//	               customReportContent = {ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME, ReportField.ANDROID_VERSION, ReportField.PACKAGE_NAME, ReportField.REPORT_ID, ReportField.BUILD, ReportField.STACK_TRACE},
//	               mode = ReportingInteractionMode.TOAST,
//	               resToastText = R.string.toast_crash)

public final class SusoftPOSApplication extends Application {

    private static Context context;
    private static long threadid;

    /**
     * (non-Javadoc)
     *
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // Global access to context.
        SusoftPOSApplication.context = this;
        // Save the id of this UI thread for validation checks later.
        SusoftPOSApplication.threadid = Thread.currentThread().getId();
        // ACRA
        ACRA.init(this);
    }

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}

    /**
     * Get the context for this application.
     */
    public static Context getContext() {
        return context;
    }

    /**
     * Returns whether the given thread id is the same as the UI thread running this app.
     *
     * @param id
     * @return
     */
    public static boolean isUIThread(long id) {
        return threadid == id;
    }

    /**
     * Returns the version name as declared in the manifest XML.
     *
     * @return
     */
    public static String getVersionName() {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            return Utilities.getStringEmpty();
        }
    }

    /**
     * Start the given activity.
     *
     * @param activity
     */
    public static <T extends Activity> void startActivity(Class<T> activity) {
        SusoftPOSApplication.context.startActivity(new Intent(SusoftPOSApplication.context, activity).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}