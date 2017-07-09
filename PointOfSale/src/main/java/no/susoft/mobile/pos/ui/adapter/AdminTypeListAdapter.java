package no.susoft.mobile.pos.ui.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.PeripheralDevice;
import no.susoft.mobile.pos.data.PeripheralProvider;
import no.susoft.mobile.pos.data.PeripheralType;

public class AdminTypeListAdapter extends ArrayAdapter<PeripheralType> {

	private List<String> deviceNames;
	private List<PeripheralDevice> storedDevices;
	private HashMap<PeripheralType, PeripheralDevice> table;

	public AdminTypeListAdapter(Context context, int textViewResourceId, List<String> deviceNames, List<PeripheralDevice> storedDevices) {
		super(context, textViewResourceId, Arrays.copyOfRange(PeripheralType.values(), 1, PeripheralType.values().length));
		this.deviceNames = deviceNames;
		this.storedDevices = storedDevices;
		table = new HashMap<>();
		for (PeripheralType peripheralType : PeripheralType.values()) {
			if (peripheralType.ordinal() != 0) {
				PeripheralProvider provider = PeripheralProvider.NONE;
				String name = "";
				String ip = "";

				for (PeripheralDevice storedDevice : storedDevices) {
					if (storedDevice.getType() == peripheralType) {
						name = storedDevice.getName();
						ip = storedDevice.getIp();
						provider = storedDevice.getProvider();
						break;
					}
				}

				PeripheralDevice device = new PeripheralDevice(name, true, true);
				device.setType(peripheralType);
				device.setProvider(provider);
				device.setIp(ip);
				table.put(peripheralType, device);
			}
		}
	}

	private class ViewHolder {
		private Spinner spName;
		private TextView tvType;
		private Spinner spProvider;
		private EditText etIP;
		public TextWatcher textWatcher;

	}

	public View getView(final int position, View convertView, ViewGroup parent) {

		try {
			if (convertView == null) {
				convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.type_list_item, parent, false);
				ViewHolder viewHolder = new ViewHolder();
				viewHolder.spName = (Spinner) convertView.findViewById(R.id.admin_device_name);
				viewHolder.tvType = (TextView) convertView.findViewById(R.id.admin_device_type);
				viewHolder.spProvider = (Spinner) convertView.findViewById(R.id.admin_device_provider);
				viewHolder.etIP = (EditText) convertView.findViewById(R.id.admin_device_ip);
				convertView.setTag(viewHolder);
			}

			String typeIp = "";
			final ViewHolder viewHolder = (ViewHolder) convertView.getTag();

			viewHolder.tvType.setText(Arrays.copyOfRange(PeripheralType.values(), 1, PeripheralType.values().length)[position].toString());
			viewHolder.tvType.setTag(Arrays.copyOfRange(PeripheralType.values(), 1, PeripheralType.values().length)[position]);

			viewHolder.spName.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, deviceNames));
			for (PeripheralDevice device : storedDevices) {
				if (device.getType() == viewHolder.tvType.getTag()) {
					int i = 0;
					for (String name : deviceNames) {
						if (name.equals(device.getName())) {
							viewHolder.spName.setSelection(i);
							typeIp = device.getIp();
							break;
						}
						i++;
					}
				}
			}
			viewHolder.spName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int spinnerPosition, long id) {
					PeripheralDevice device = table.get((PeripheralType) viewHolder.tvType.getTag());
					device.setName((String) viewHolder.spName.getSelectedItem());
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {

				}
			});

			fillProviders(viewHolder);

			List<PeripheralProvider> providerList = new ArrayList<>();
			switch((PeripheralType)viewHolder.tvType.getTag()) {
				case PRINTER: providerList = Arrays.asList(PeripheralProvider.getPrinters());	break;
				case CARD_TERMINAL: providerList = Arrays.asList(PeripheralProvider.getCardTerminals());	break;
				case SCANNER: providerList = Arrays.asList(PeripheralProvider.getScanners());	break;
				case DISPLAY: providerList = Arrays.asList(PeripheralProvider.getDisplays());	break;
				case CASHDRAWER: providerList = Arrays.asList(PeripheralProvider.getCashDrawers()); break;
				case KITCHEN_PRINTER:
				case BAR_PRINTER:  providerList = Arrays.asList(PeripheralProvider.getKitchenPrinters());	break;
			}

			if (!providerList.isEmpty()) {
				int ordinal = 0;
				for (PeripheralDevice device : storedDevices) {
					if (device.getType() == viewHolder.tvType.getTag()) {
						for (String name : deviceNames) {
							if (name.equals(device.getName())) {
								ordinal = device.getProvider().ordinal();
								break;
							}
						}
					}
				}
				int index = providerList.indexOf(PeripheralProvider.values()[ordinal]);
				viewHolder.spProvider.setSelection(index);
			}

			final List<PeripheralProvider> finalProviderList = providerList;
			viewHolder.spProvider.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int innerPosition, long id) {
					if (parent != null && parent.getSelectedItem() != null) {
						PeripheralDevice device = table.get((PeripheralType) viewHolder.tvType.getTag());
						device.setProvider((PeripheralProvider) viewHolder.spProvider.getSelectedItem());
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {

				}
			});

			if (viewHolder.textWatcher != null)
				viewHolder.etIP.removeTextChangedListener(viewHolder.textWatcher);

			viewHolder.etIP.setText(typeIp);
			viewHolder.textWatcher = new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					PeripheralDevice device = table.get((PeripheralType) viewHolder.tvType.getTag());
					device.setIp(s.toString());
				}

				@Override
				public void afterTextChanged(Editable s) {
				}
			};
			viewHolder.etIP.addTextChangedListener(viewHolder.textWatcher);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return convertView;
	}

	@Override
	public PeripheralType getItem(int position) {
		return super.getItem(position);
	}

	public PeripheralDevice getDeviceForType(PeripheralType pt) {
		for (PeripheralDevice peripheralDevice : table.values()) {
			if (peripheralDevice.getType() == pt) {
				return peripheralDevice;
			}
		}
		return null;
	}

	public void fillProviders(ViewHolder viewHolder) {
		switch ((PeripheralType)viewHolder.tvType.getTag()) {
			case PRINTER: {
				viewHolder.spProvider.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, PeripheralProvider.getPrinters()));
				break;
			}
			case CARD_TERMINAL: {
				viewHolder.spProvider.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, PeripheralProvider.getCardTerminals()));
				break;
			}
			case DISPLAY: {
				viewHolder.spProvider.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, PeripheralProvider.getDisplays()));
				break;
			}
			case SCANNER: {
				viewHolder.spProvider.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, PeripheralProvider.getScanners()));
				break;
			}
			case CASHDRAWER: {
				viewHolder.spProvider.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, PeripheralProvider.getCashDrawers()));
				break;
			}
			case KITCHEN_PRINTER:
			case BAR_PRINTER: {
				viewHolder.spProvider.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, PeripheralProvider.getKitchenPrinters()));
				break;
			}
		}
	}

	private class DeviceIPTextWatcher implements TextWatcher {

		private View view;

		private DeviceIPTextWatcher(View view) {
			this.view = view;
		}

		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
		}

		public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
		}

		public void afterTextChanged(Editable editable) {
			String text = editable.toString();
			EditText f = (EditText) view.findViewById(R.id.admin_device_ip);
			f.setText(text);
		}
	}

}
