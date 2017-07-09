package no.susoft.mobile.pos.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.SusoftPOSApplication;
import no.susoft.mobile.pos.data.PeripheralDevice;
import no.susoft.mobile.pos.data.PeripheralProvider;
import no.susoft.mobile.pos.data.PeripheralType;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.hardware.BluetoothDiscovery;
import no.susoft.mobile.pos.hardware.BluetoothDiscoveryEvent;
import no.susoft.mobile.pos.hardware.BluetoothDiscoveryListener;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.adapter.AdminTypeListAdapter;

public class AdminDevicesFragment extends Fragment {

	@InjectView(R.id.admin_device_listview)
	ListView listView;
	@InjectView(R.id.admin_label_header)
	LinearLayout listHeader;
	@InjectView(R.id.btnSaveDevices)
	Button btnSaveDevices;
	private ProgressDialog progDialog;

	ArrayList<String> deviceNames = new ArrayList<>();
	List<PeripheralDevice> list = new ArrayList<>();
	private AdminTypeListAdapter typeAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		deviceNames = new ArrayList<>();
		list = new ArrayList<>();
		final View rootView = inflater.inflate(R.layout.admin_devices_fragment, container, false);
		ButterKnife.inject(this, rootView);

		try {
			showProgressDialog(getString(R.string.scanning), false);
			SharedPreferences preferences = SusoftPOSApplication.getContext().getSharedPreferences(PeripheralDevice.class.toString(), Context.MODE_PRIVATE);
			loadStoredDevices(preferences);
			discoverDevices();
			addSaveButtonListener();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return rootView;
	}

	private void loadStoredDevices(SharedPreferences preferences) {

		if (preferences.getString("CARD_TERMINAL_NAME", "").length() > 0) {
			final String name = preferences.getString("CARD_TERMINAL_NAME", "");
			int provider = preferences.getInt("CARD_TERMINAL_PROVIDER", PeripheralProvider.NONE.ordinal());
			String ip = preferences.getString("CARD_TERMINAL_IP", "");

			System.out.println("name = " + name);
			System.out.println("provider = " + provider);
			System.out.println("ip = " + ip);


			PeripheralDevice peripheralDevice = new PeripheralDevice(name, true, true);
			peripheralDevice.setType(PeripheralType.CARD_TERMINAL);
			peripheralDevice.setProvider(PeripheralProvider.values()[provider]);
			peripheralDevice.setIp(ip);
			list.add(peripheralDevice);
		}

		if (preferences.getString("SCANNER_NAME", "").length() > 0) {
			final String name = preferences.getString("SCANNER_NAME", "");
			int provider = preferences.getInt("SCANNER_PROVIDER", PeripheralProvider.NONE.ordinal());

			PeripheralDevice peripheralDevice = new PeripheralDevice(name, true, true);
			peripheralDevice.setType(PeripheralType.SCANNER);
			peripheralDevice.setProvider(PeripheralProvider.values()[provider]);
			list.add(peripheralDevice);
		}

		if (preferences.getString("PRINTER_NAME", "").length() > 0) {
			String name = preferences.getString("PRINTER_NAME", "");
			int provider = preferences.getInt("PRINTER_PROVIDER", PeripheralProvider.NONE.ordinal());
			String ip = preferences.getString("PRINTER_IP", "");

			PeripheralDevice peripheralDevice = new PeripheralDevice(name, true, true);
			peripheralDevice.setType(PeripheralType.PRINTER);
			peripheralDevice.setProvider(PeripheralProvider.values()[provider]);
			peripheralDevice.setIp(ip);
			list.add(peripheralDevice);
		}

		if (preferences.getString("DISPLAY_NAME", "").length() > 0) {
			String name = preferences.getString("DISPLAY_NAME", "");
			int provider = preferences.getInt("DISPLAY_PROVIDER", PeripheralProvider.NONE.ordinal());
			String ip = preferences.getString("DISPLAY_IP", "");

			PeripheralDevice peripheralDevice = new PeripheralDevice(name, true, true);
			peripheralDevice.setType(PeripheralType.DISPLAY);
			peripheralDevice.setProvider(PeripheralProvider.values()[provider]);
			peripheralDevice.setIp(ip);
			list.add(peripheralDevice);
		}

		if (preferences.getString("KITCHEN_PRINTER_NAME", "").length() > 0) {
			String name = preferences.getString("KITCHEN_PRINTER_NAME", "");
			int provider = preferences.getInt("KITCHEN_PRINTER_PROVIDER", PeripheralProvider.NONE.ordinal());
			String ip = preferences.getString("KITCHEN_PRINTER_IP", "");

			PeripheralDevice peripheralDevice = new PeripheralDevice(name, true, true);
			peripheralDevice.setType(PeripheralType.KITCHEN_PRINTER);
			peripheralDevice.setProvider(PeripheralProvider.values()[provider]);
			peripheralDevice.setIp(ip);
			list.add(peripheralDevice);
		}

		if (preferences.getString("BAR_PRINTER_NAME", "").length() > 0) {
			String name = preferences.getString("BAR_PRINTER_NAME", "");
			int provider = preferences.getInt("BAR_PRINTER_PROVIDER", PeripheralProvider.NONE.ordinal());
			String ip = preferences.getString("BAR_PRINTER_IP", "");

			PeripheralDevice peripheralDevice = new PeripheralDevice(name, true, true);
			peripheralDevice.setType(PeripheralType.BAR_PRINTER);
			peripheralDevice.setProvider(PeripheralProvider.values()[provider]);
			peripheralDevice.setIp(ip);
			list.add(peripheralDevice);
		}

		if (preferences.getString("CASHDRAWER_NAME", "").length() > 0) {
			String name = preferences.getString("CASHDRAWER_NAME", "");
			int provider = preferences.getInt("CASHDRAWER_PROVIDER", PeripheralProvider.NONE.ordinal());

			PeripheralDevice peripheralDevice = new PeripheralDevice(name, true, true);
			peripheralDevice.setType(PeripheralType.CASHDRAWER);
			peripheralDevice.setProvider(PeripheralProvider.values()[provider]);
			list.add(peripheralDevice);
		}
	}

	private void fillDevices(BluetoothDiscovery bluetoothDiscovery) {
		ErrorReporter.INSTANCE.filelog("AdminDevicesFragment", "fillDevices ->");

		deviceNames.add(PeripheralProvider.NONE.toString());
		for (String name : PeripheralDevice.deviceNames) {
			deviceNames.add(name);
		}
		for (String name : PeripheralDevice.bxlDevices) {
			deviceNames.add(name);
		}

		if (bluetoothDiscovery != null) {
			if (bluetoothDiscovery.isIdle()) {
				List<BluetoothDevice> pairedDevices = bluetoothDiscovery.listPairedDevices();
				for (BluetoothDevice pairedDevice : pairedDevices) {
					if (pairedDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
						if (pairedDevice.getName() != null && pairedDevice.getName().trim().length() > 0) {
							if (!deviceNames.contains(pairedDevice.getName())) {
								deviceNames.add(pairedDevice.getName());
							}
						}
					}
				}
			}
		}

		ErrorReporter.INSTANCE.filelog("AdminDevicesFragment", "<- fillDevices");
	}

	private void discoverDevices() {
		try {
			final BluetoothDiscovery bluetoothDiscovery = BluetoothDiscovery.getInstance();
			if (bluetoothDiscovery.getBluetoothAdapter() == null) {
				hideProgressDialog();
				fillDevices(null);
				refreshListView();
			} else {
				bluetoothDiscovery.addListener(new BluetoothDiscoveryListener() {
					@Override
					public void onBluetoothDiscoveryEvent(BluetoothDiscoveryEvent event) {
						switch (event.getEventType()) {
							case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
								ErrorReporter.INSTANCE.filelog("AdminDevicesFragment", "ACTION_DISCOVERY_FINISHED");
								hideProgressDialog();
								fillDevices(bluetoothDiscovery);
								refreshListView();
								if (list.isEmpty()) {
									toast("No devices found. Please retry search");
								}
								bluetoothDiscovery.removeListener(this);
								break;
						}
					}
				}).discoverDevices();
			}
		} catch (Exception e) {
			hideProgressDialog();
			e.printStackTrace();
		}
	}

	private void refreshListView() {
		ErrorReporter.INSTANCE.filelog("AdminDevicesFragment", "refreshListView ->");
		try {
			this.typeAdapter = new AdminTypeListAdapter(getActivity(), 0, deviceNames, list);
			listView.setAdapter(typeAdapter);
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog("AdminDevicesFragment", "refreshListView", e);
		}

		ErrorReporter.INSTANCE.filelog("AdminDevicesFragment", "<- refreshListView");
	}

	private void addSaveButtonListener() {
		btnSaveDevices.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (listView != null && listView.getAdapter() != null) {
					saveChanges();
				}
			}
		});
	}

	private void saveChanges() {
		SharedPreferences preferences = SusoftPOSApplication.getContext().getSharedPreferences(PeripheralDevice.class.toString(), Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = preferences.edit();
		editor.remove("PRINTER");
		editor.remove("PRINTER_PROVIDER");
		editor.remove("PRINTER_NAME");
		editor.remove("PRINTER_IP");
		editor.remove("CARD_TERMINAL");
		editor.remove("CARD_TERMINAL_PROVIDER");
		editor.remove("CARD_TERMINAL_NAME");
		editor.remove("CARD_TERMINAL_IP");
		editor.remove("SCANNER");
		editor.remove("SCANNER_PROVIDER");
		editor.remove("SCANNER_NAME");
		editor.remove("CASHDRAWER");
		editor.remove("CASHDRAWER_PROVIDER");
		editor.remove("CASHDRAWER_NAME");
		editor.remove("KITCHEN_PRINTER");
		editor.remove("KITCHEN_PRINTER_PROVIDER");
		editor.remove("KITCHEN_PRINTER_NAME");
		editor.remove("KITCHEN_PRINTER_IP");
		editor.remove("BAR_PRINTER");
		editor.remove("BAR_PRINTER_PROVIDER");
		editor.remove("BAR_PRINTER_NAME");
		editor.remove("BAR_PRINTER_IP");
		editor.remove("DISPLAY");
		editor.remove("DISPLAY_PROVIDER");
		editor.remove("DISPLAY_NAME");
		editor.remove("DISPLAY_IP");

		for(PeripheralType pt : PeripheralType.values()) {
			PeripheralDevice device = typeAdapter.getDeviceForType(pt);
			if (device != null && device.getProvider() != PeripheralProvider.NONE) {
				switch (pt) {
					case PRINTER: {
						editor.putInt("PRINTER", PeripheralType.PRINTER.ordinal());
						editor.putInt("PRINTER_PROVIDER", device.getProvider().ordinal());
						editor.putString("PRINTER_NAME", device.getName());
						editor.putString("PRINTER_IP", device.getIp());
						break;
					}
					case CARD_TERMINAL: {
						editor.putInt("CARD_TERMINAL",  PeripheralType.CARD_TERMINAL.ordinal());
						editor.putInt("CARD_TERMINAL_PROVIDER", device.getProvider().ordinal());
						editor.putString("CARD_TERMINAL_NAME", device.getName());
						editor.putString("CARD_TERMINAL_IP", device.getIp());
						break;
					}
					case DISPLAY: {
						editor.putInt("DISPLAY",  PeripheralType.DISPLAY.ordinal());
						editor.putInt("DISPLAY_PROVIDER", device.getProvider().ordinal());
						editor.putString("DISPLAY_NAME", device.getName());
						editor.putString("DISPLAY_IP", device.getIp());
						break;
					}
					case SCANNER: {
						editor.putInt("SCANNER",  PeripheralType.SCANNER.ordinal());
						editor.putInt("SCANNER_PROVIDER", device.getProvider().ordinal());
						editor.putString("SCANNER_NAME", device.getName());
						break;
					}
					case CASHDRAWER: {
						editor.putInt("CASHDRAWER", PeripheralType.CASHDRAWER.ordinal());
						editor.putInt("CASHDRAWER_PROVIDER", device.getProvider().ordinal());
						editor.putString("CASHDRAWER_NAME", device.getName());
						break;
					}
					case KITCHEN_PRINTER: {
						editor.putInt("KITCHEN_PRINTER", PeripheralType.KITCHEN_PRINTER.ordinal());
						editor.putInt("KITCHEN_PRINTER_PROVIDER", device.getProvider().ordinal());
						editor.putString("KITCHEN_PRINTER_NAME", device.getName());
						editor.putString("KITCHEN_PRINTER_IP", device.getIp());
						break;
					}
					case BAR_PRINTER: {
						editor.putInt("BAR_PRINTER", PeripheralType.BAR_PRINTER.ordinal());
						editor.putInt("BAR_PRINTER_PROVIDER", device.getProvider().ordinal());
						editor.putString("BAR_PRINTER_NAME", device.getName());
						editor.putString("BAR_PRINTER_IP", device.getIp());
						break;
					}
				}
			}
		}

		editor.commit();
	}

	public void showProgressDialog(String msg, boolean cancelable) {
		if (progDialog == null || !progDialog.isShowing()) {
			progDialog = new ProgressDialog(getActivity());
			progDialog.setMessage(msg);
			progDialog.setIndeterminate(false);
			progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progDialog.setCancelable(cancelable);
			progDialog.setCanceledOnTouchOutside(false);
			progDialog.show();
		} else {
			progDialog.setMessage(msg);
		}
	}

	public void hideProgressDialog() {
		if (progDialog != null) {
			progDialog.dismiss();
		}
	}

	private void toast(String message) {
		Toast.makeText(MainActivity.getInstance(), message, Toast.LENGTH_LONG).show();
	}

}
