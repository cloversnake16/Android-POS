package no.susoft.mobile.pos.hardware.scale;

import java.util.List;
import java.util.Map;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import jp.co.casio.vx.framework.device.SerialCom;

public class ScaleDeviceService extends Service {
	public final static int RESULT_SUCCESS = 0;
	public final static int RESULT_UNBIND = -1;
	public final static int RESULT_UNOPENED = -2;
	public final static int RESULT_ALREADY_OPEN = -3;
	public final static int RESULT_BUSY = -4;
	public final static int RESULT_UNSTABLE = -5;
	public final static int RESULT_TIMEOUT = -6;
	public final static int RESULT_UNDERZERO = -7;
	public final static int RESULT_OVERWEIGHT = -8;
	public final static int RESULT_HARDWAREERROR = -9;
	public final static int RESULT_FAULTYCALIBRATION = -10;
	public final static int RESULT_UNBINDSCALE = -11;
	public final static int RESULT_ERROR = -99;
	public final static int RESULT_INVALID_ID = -100;
	public final static int RESULT_SERVICEERR = -199;

	public final static String PROPERTY_CapDisplay = "R_CapDisplay";
	public final static String PROPERTY_CapDisplayText = "R_CapDisplayText";
	public final static String PROPERTY_CapPriceCalculating = "R_CapPriceCalculating";
	public final static String PROPERTY_CapTareWeight = "R_CapTareWeight";
	public final static String PROPERTY_CapZeroScale = "R_CapZeroScale";
	public final static String PROPERTY_WeightUnit = "R_WeightUnit";
	public final static String PROPERTY_DisplayText = "DisplayText";
	public final static String PROPERTY_TareWeight = "TareWeight";
	public final static String PROPERTY_Price = "Price";

	public final static String CAP_ENABLE = "ENABLE";
	public final static String CAP_DISABLE = "DISABLE";

	public final static String WEIGHTUNIT_NON = "NON";
	public final static String WEIGHTUNIT_GRAM = "GRAM";
	public final static String WEIGHTUNIT_KILOGRAM = "KILOGRAM";
	public final static String WEIGHTUNIT_OUNCE = "OUNCE";
	public final static String WEIGHTUNIT_POUND = "POUND";

	private final static String TAG = "ScaleDeviceService";
	private final static int ID_MAX = 3;
	private ScaleTask[] mTask = new ScaleTask[ID_MAX]; // 0:COM1 1:COM2 2:COM3

	@Override
	public void onCreate() {
		for (int i = 0; i < ID_MAX; i++) {
			mTask[i] = null;
		}
	}

	@Override
	public void onDestroy() {
		for (int i = 0; i < ID_MAX; i++) {
			if (mTask[i] != null) {
				mTask[i].close();
				mTask[i] = null;
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mIService;
	}

    public boolean onUnbind(Intent intent) {
		return false;
    }

	private final IDeviceManagerScale.Stub mIService = new IDeviceManagerScale.Stub() {
		@Override
		public int open(IDeviceManagerScaleCallback cb) throws RemoteException {
			int response = RESULT_SERVICEERR;
			ScaleTask task = ScaleDeviceService.this.createTask();
			if (task != null) {
				int id = task.getId();
				if (mTask[id - 1] == null) {
					task.open();
					task.setCallback(cb);
					mTask[id - 1] = task;
					response = id;
				}
				else {
					response = RESULT_INVALID_ID;
				}
			}
			return response;
		}

		@Override
		public int close(int id) throws RemoteException {
			if ((id < 1) || (ID_MAX < id)) {
				return RESULT_INVALID_ID;
			}
			if (mTask[id - 1] == null) {
				return RESULT_INVALID_ID;
			}

			int response = mTask[id - 1].close();
			mTask[id - 1] = null;
			return response;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public int setProperty(int id, Map property) throws RemoteException {
			if ((id < 1) || (ID_MAX < id)) {
				return RESULT_INVALID_ID;
			}
			if (mTask[id - 1] == null) {
				return RESULT_INVALID_ID;
			}

			mTask[id - 1].setProperty(property);
			return RESULT_SUCCESS;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public Map getProperty(int id) throws RemoteException {
			if ((id < 1) || (ID_MAX < id)) {
				return null;
			}
			if (mTask[id - 1] == null) {
				return null;
			}

			return mTask[id - 1].getProperty();
		}

		@Override
		public int liveWeight(int id, boolean enable) throws RemoteException {
			if ((id < 1) || (ID_MAX < id)) {
				return RESULT_INVALID_ID;
			}
			if (mTask[id - 1] == null) {
				return RESULT_INVALID_ID;
			}

			mTask[id - 1].liveWeight(enable);
			return RESULT_SUCCESS;
		}

		@Override
		public int readWeight(int id, int timeout) throws RemoteException {
			if ((id < 1) || (ID_MAX < id)) {
				return RESULT_INVALID_ID;
			}
			if (mTask[id - 1] == null) {
				return RESULT_INVALID_ID;
			}

			return mTask[id - 1].readWeight(timeout);
		}

		@Override
		public int zeroScale(int id) throws RemoteException {
			if ((id < 1) || (ID_MAX < id)) {
				return RESULT_INVALID_ID;
			}
			if (mTask[id - 1] == null) {
				return RESULT_INVALID_ID;
			}

			return mTask[id - 1].zeroScale();
		}

		@Override
		public int getStatus(int id) throws RemoteException {
			if ((id < 1) || (ID_MAX < id)) {
				return RESULT_INVALID_ID;
			}
			if (mTask[id - 1] == null) {
				return RESULT_INVALID_ID;
			}

			return mTask[id - 1].getStatus();
		}

		@Override
		public List<String> command(int id, List<String> cmd) throws RemoteException {
			if ((id < 1) || (ID_MAX < id)) {
				return null;
			}
			if (mTask[id - 1] == null) {
				return null;
			}

			return mTask[id - 1].command(cmd);
		}
	};

	private ScaleTask createTask() {
		ScaleTask task = new ScaleTask(2, SerialCom.SERIAL_TYPE_COM2, SerialCom.SERIAL_BOUDRATE_9600, SerialCom.SERIAL_BITLEN_7, SerialCom.SERIAL_PARITY_EVEN, SerialCom.SERIAL_STOP_1, SerialCom.SERIAL_FLOW_NON);
		return task;
	}
}
