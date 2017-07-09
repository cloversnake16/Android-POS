package no.susoft.mobile.pos.hardware.scale;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.RemoteCallbackList;
import android.os.RemoteException;
import jp.co.casio.vx.framework.device.DeviceCommon;
import jp.co.casio.vx.framework.device.SerialCom;

public class ScaleTask implements Runnable {
	// =====================================================================================
	// NIC
	// MODEL 6708
	// ECR Serial Communication Protocol
	// ------------------------------------------------------------------------------------
	// Command
	//   W<CR>
	// Scale Response
	//   <LF>xxx.xxxuu<CR>  Returns decimal weight with units
	//   <LF>Shh<CR><EXT>       plus scale status.
	//          or
	//   <LF>Shh<CR><EXT>   Scale status only if wt<0, in motion or out of capacity,
	//                          or zero error.
	// ------------------------------------------------------------------------------------
	// Command
	//   S<CR>
	// Scale Response
	//   <LF>Shh<CR><EXT>   Returns to scale status.
	// ------------------------------------------------------------------------------------
	// Command
	//   Z<CR>
	// Scale Response
	//   <LF>Shh<CR><EXT>   Scale is zeroed, returns status.
	// ------------------------------------------------------------------------------------
	// <ETX> End of Text character (03 hex)
	// <LF>  Line Feed character (0A hex)
	// <CR>  Carriage Return character (0D hex)
	//  x    Weight numeric digit
	//  hh   Two status bytes
	//  uu   Units of measure (LB, KG, OZ, G, etc..., all upper case)
	// ------------------------------------------------------------------------------------
	// Scale Status
	// First Status Byte
	//   Bit=0  1=Scale in motion
	//          0=Stable
	//   Bit=1  1=Scale at zero
	//          0=Not at zere
	//   Bit=2  1=RAM error
	//          0=No RAM error
	//   Bit=3  1=EEPROM error
	//          0=No EEPROM error
	// Second Status Byte
	//   Bit=0  1=Under capacity
	//          0=Not under capacity
	//   Bit=1  1=Over capacity
	//          0=Not over capacity
	//   Bit=2  1=ROM error
	//          0=No ROM error
	//   Bit=3  1=Faulty calibration data
	//          0=calibration data okay
	// =====================================================================================
	@SuppressWarnings("unused")
	private final static String TAG = "ScaleTask";
	private int mId = 0;
	private HashMap<String, String> mProperty = new HashMap<>();
	private int mPort;
	private int mBaudRate;
	private int mBitLen;
	private int mParityBit;
	private int mStopBit;
	private int mFlowControl;
	private long mReadTimeOut = 0;
	private SerialCom mCom = null;
	private Thread mThread = null;
	private RemoteCallbackList<IDeviceManagerScaleCallback> mCallbackList = new RemoteCallbackList<>();

	private boolean mLiveWeight = false;
	private boolean mReadWeight = false;
	private final int CHECKCNT = 2;
	private int mReadCnt = 0;
	private long mWeight = 0;
	private boolean mIsCancel;

	public ScaleTask(int id, int port, int baudRate, int bitLen, int parityBit, int stopBit, int flowControl) {
		mId = id;
		mPort = port;
		mBaudRate = baudRate;
		mBitLen = bitLen;
		mParityBit = parityBit;
		mStopBit = stopBit;
		mFlowControl = flowControl;
		mIsCancel = false;
		mThread = new Thread(this);
	}

	public void setCallback(IDeviceManagerScaleCallback callback) {
		mCallbackList.register(callback);
	}

	public int getId() {
		return mId;
	}

	public int open() {
		if (mCom != null) {
			return ScaleDeviceService.RESULT_ALREADY_OPEN;
		}

		SerialCom com = new SerialCom();
		com.open(mPort, DeviceCommon.DEVICE_MODE_COMMON, DeviceCommon.DEVICE_HOST_LOCALHOST);
		com.connectCom(mBaudRate, mBitLen, mParityBit, mStopBit, mFlowControl);
		com.setControl(SerialCom.SERIAL_IO_DTR | SerialCom.SERIAL_IO_RTS);
		mCom = com;
		mThread.start();

		HashMap<String, String> work = new HashMap<String, String>();
		work.put(ScaleDeviceService.PROPERTY_CapDisplay, ScaleDeviceService.CAP_ENABLE);
		work.put(ScaleDeviceService.PROPERTY_CapDisplayText, ScaleDeviceService.CAP_DISABLE);
		work.put(ScaleDeviceService.PROPERTY_CapPriceCalculating, ScaleDeviceService.CAP_DISABLE);
		work.put(ScaleDeviceService.PROPERTY_CapTareWeight, ScaleDeviceService.CAP_DISABLE);
		work.put(ScaleDeviceService.PROPERTY_CapZeroScale, ScaleDeviceService.CAP_DISABLE);
		work.put(ScaleDeviceService.PROPERTY_WeightUnit, ScaleDeviceService.WEIGHTUNIT_NON);

		mProperty.putAll(work);
		return ScaleDeviceService.RESULT_SUCCESS;
	}

	public int close() {
		int response = ScaleDeviceService.RESULT_SUCCESS;
		if (mCom != null) {
			cancel();
			mCom.disconnectCom();
			mCom.close();
			mCom = null;
			mCallbackList.kill();
		}
		else {
			response = ScaleDeviceService.RESULT_UNOPENED;
		}
		return response;
	}

	@Override
	public void run() {
		while (true) {
			if (mIsCancel) {
				break;
			}
			sleep(400);
			List<Long> ret = commandW();

			if (ret == null) {
				break;
			}
			long weight = ret.get(0);
			int status = ret.get(1).intValue();

			if (mLiveWeight) {
				invokeLiveWeight(weight, status);
			}
			if (mReadWeight) {
				long now = System.currentTimeMillis();
				if (now > mReadTimeOut) {
					invokeWeight(0L, ScaleDeviceService.RESULT_TIMEOUT, 0L);
					mReadWeight = false;
				}
				else {
					if ((status == ScaleDeviceService.RESULT_SUCCESS) && (weight > 0L)) {
						if (mWeight == weight) {
							mReadCnt++;
						}
						else {
							mReadCnt = 1;
							mWeight = weight;
						}
						if (mReadCnt >= CHECKCNT) {
							long price = 0L;
							invokeWeight(weight, status, price);
							mReadWeight = false;
						}
					}
					else {
						mReadCnt = 0;
						mWeight = 0;
					}
				}
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setProperty(Map property) {
		try {
			HashMap<String, String> work = new HashMap<String, String>();
			Set<String> keys = (Set<String>) property.keySet();
			for (String key : keys) {
				String value = (String) property.get(key);
				work.put(key, value);
			}
			mProperty.putAll(work);
		}
		catch (Exception e) {
		}
	}

	@SuppressWarnings("rawtypes")
	public Map getProperty() {
		return mProperty;
	}

	public void liveWeight(boolean enable) {
		mLiveWeight = enable;
	}

	public int readWeight(int timeout) {
		if (mReadWeight) {
			return ScaleDeviceService.RESULT_BUSY;
		}
		mReadWeight = true;
		mReadCnt = 0;
		mWeight = 0L;
		mReadTimeOut = System.currentTimeMillis() + timeout;
		return ScaleDeviceService.RESULT_SUCCESS;
	}

	public int zeroScale() {
		if (mReadWeight) {
			return ScaleDeviceService.RESULT_BUSY;
		}
		return commandZ();
	}

	public int getStatus() {
		if (mReadWeight) {
			return ScaleDeviceService.RESULT_BUSY;
		}
		return commandS();
	}

	public List<String> command(List<String> cmd) {
		return new ArrayList<String>();
	}

	synchronized private List<Long> commandW() {
		if (mIsCancel) {
			return null;
		}
		ArrayList<Long> response = new ArrayList<>();
		write(new byte[] { 'W', 0x0d });
		sleep(100);
		List<String> s = read();
		if (s != null) {
			if (s.size() > 1) {
				response.add(getWeight(s.get(0)));
				response.add(status(s.get(1)));
			}
			else {
				response.add(0L);
				response.add(status(s.get(0)));
			}
		}
		else {
			response.add(0L);
			response.add((long) ScaleDeviceService.RESULT_TIMEOUT);
		}
		return response;
	}

	synchronized private int commandS() {
		int response = 0;
		if (mIsCancel) {
			return ScaleDeviceService.RESULT_SUCCESS;
		}
		write(new byte[] { 'S', 0x0d });
		sleep(100);
		List<String> s = read();
		if (s != null) {
			String status = s.get(0);
			response = (int) status(status);
		}
		else {
			response = ScaleDeviceService.RESULT_TIMEOUT;
		}
		return response;
	}

	synchronized private int commandZ() {
		int response = 0;
		if (mIsCancel) {
			return ScaleDeviceService.RESULT_SUCCESS;
		}
		write(new byte[] { 'Z', 0x0d });
		sleep(100);
		List<String> s = read();
		if (s != null) {
			String status = s.get(0);
			response = (int) status(status);
		}
		else {
			response = ScaleDeviceService.RESULT_TIMEOUT;
		}
		return response;
	}

	private int write(byte[] data) {
		int ret = mCom.writeData(data, data.length);
		mCom.getEndStatus();
		return ret;
	}

	private List<String> read() {
		ArrayList<String> response = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		long timeout = System.currentTimeMillis() + 500L;
		while (true) {
			if (mIsCancel) {
				break;
			}
			byte[] buffer = new byte[256];
			int readLen = mCom.readData(buffer);
			if (readLen > 0) {
				for (int i = 0; i < readLen; i++) {
					byte b = buffer[i];
					if (b == 0x03) { // EXT
						return response;
					}
					else if (b == 0x0a) { // LF
						sb.setLength(0);
					}
					else if (b == 0x0d) { // CR
						response.add(sb.toString());
					}
					else {
						sb.append(new String(new byte[] { b }));
					}
				}
			}

			long time = System.currentTimeMillis();
			if (time > timeout) {
				break;
			}
			sleep(100);
		}
		return null;
	}

	private long getWeight(String in) {
		Pattern p = Pattern.compile("([0-9]+)\\.([0-9]+)");
		Matcher m = p.matcher(in);
		m.find();
		String v1 = "0";
		String v2 = "";
		int cnt = m.groupCount();
		if (cnt >= 1) {
			v1 = m.group(1);
		}
		if (cnt >= 2) {
			v2 = m.group(2);
		}
		String v = v1 + (v2 + "000").substring(0, 3);
		return Long.valueOf(v);
	}

	private long status(String rcv) {
		int status = ScaleDeviceService.RESULT_ERROR;
		boolean motion = false;
		boolean zero = false;
		boolean errRAM = false;
		boolean errEEPROM = false;
		boolean errROM = false;
		boolean under = false;
		boolean over = false;
		boolean calibrationErr = false;

		try {
			if (rcv.charAt(0) == 'S') {
				char first = rcv.charAt(1);
				char second = rcv.charAt(2);
				if ((first & 0x01) != 0x00) {
					motion = true;
				}
				if ((first & 0x02) != 0x00) {
					zero = true;
				}
				if ((first & 0x04) != 0x00) {
					errRAM = true;
				}
				if ((first & 0x08) != 0x00) {
					errEEPROM = true;
				}
				if ((second & 0x01) != 0x00) {
					under = true;
				}
				if ((second & 0x02) != 0x00) {
					over = true;
				}
				if ((second & 0x04) != 0x00) {
					errROM = true;
				}
				if ((second & 0x08) != 0x00) {
					calibrationErr = true;
				}
				// Log.i(TAG, "errRAM         = " + errRAM);
				// Log.i(TAG, "errEEPROM      = " + errEEPROM);
				// Log.i(TAG, "errROM         = " + errROM);
				// Log.i(TAG, "calibrationErr = " + calibrationErr);
				// Log.i(TAG, "under          = " + under);
				// Log.i(TAG, "over           = " + over);
				// Log.i(TAG, "motion         = " + motion);
				// Log.i(TAG, "zero           = " + zero);
				if ((errRAM) || (errEEPROM) || (errROM)) {
					status = ScaleDeviceService.RESULT_HARDWAREERROR;
				}
				else if (calibrationErr) {
					status = ScaleDeviceService.RESULT_FAULTYCALIBRATION;
				}
				else if (under) {
					status = ScaleDeviceService.RESULT_UNDERZERO;
				}
				else if (over) {
					status = ScaleDeviceService.RESULT_OVERWEIGHT;
				}
				else if (motion) {
					status = ScaleDeviceService.RESULT_UNSTABLE;
				}
				else {
					status = ScaleDeviceService.RESULT_SUCCESS;
				}
			}
		}
		catch (Exception e) {
		}

		return status;
	}

	private void sleep(int time) {
		try {
			Thread.sleep(time);
		}
		catch (InterruptedException e) {
		}
	}

	private void cancel() {
		if (!mIsCancel) {
			mReadWeight = false;
			mLiveWeight = false;
			mIsCancel = true;
			mThread.interrupt();
		}
	}

	private void invokeWeight(long weight, int status, long price) {
		if (mCallbackList != null) {
			try {
				int n = mCallbackList.beginBroadcast();
				for (int i = 0; i < n; i++) {
					try {
						mCallbackList.getBroadcastItem(i).notifyWeight(mId, weight, status, price);
					}
					catch (RemoteException e) {
					}
				}
				mCallbackList.finishBroadcast();
			}
			catch (Exception e) {
			}
		}
	}

	private void invokeLiveWeight(long weight, int status) {
		if (mCallbackList != null) {
			try {
				int n = mCallbackList.beginBroadcast();
				for (int i = 0; i < n; i++) {
					try {
						mCallbackList.getBroadcastItem(i).notifyLiveWeight(mId, weight, status);
					}
					catch (RemoteException e) {
					}
				}
				mCallbackList.finishBroadcast();
			}
			catch (Exception e) {
			}
		}
	}
}
