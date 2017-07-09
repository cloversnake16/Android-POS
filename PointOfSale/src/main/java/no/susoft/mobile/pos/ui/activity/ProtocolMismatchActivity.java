package no.susoft.mobile.pos.ui.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import no.susoft.mobile.pos.R;

/**
 * Simple activity to notify a user that they require to get an updated version of the application.
 *
 * @author Yesod
 */
public final class ProtocolMismatchActivity extends Activity implements View.OnClickListener {

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Layout
        this.setContentView(R.layout.protocol_mismatch);
        // Get button.
        View view = this.findViewById(R.id.account_action_authenticate);
        // Attach listener.
        view.setOnClickListener(this);
    }

    /*
     * (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View arg0) {
		Intent restartIntent = MainActivity.getInstance().getPackageManager().getLaunchIntentForPackage(MainActivity.getInstance().getPackageName());
		PendingIntent intent = PendingIntent.getActivity(MainActivity.getInstance(), 0, restartIntent, Intent.FLAG_ACTIVITY_CLEAR_TOP);
		AlarmManager manager = (AlarmManager) MainActivity.getInstance().getSystemService(Context.ALARM_SERVICE);
		manager.set(AlarmManager.RTC, System.currentTimeMillis() + 5, intent);
		System.exit(2);
    }
}