package no.susoft.mobile.pos.ui.activity.util;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Handler;
import jp.co.casio.vx.framework.device.IButton;

/**
 * Created by Vilde on 14.12.2015.
 */
public interface IDallasKey {

    public IntentFilter getIntentFilter = new IntentFilter("jp.co.casio.vx.regdevice.comm.POWER_RECOVERY");

    void registerDallasKeyReceiver(BroadcastReceiver intentReceiver, IntentFilter intentFilter);


}
