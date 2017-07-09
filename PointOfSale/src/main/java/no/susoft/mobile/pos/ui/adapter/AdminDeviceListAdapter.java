package no.susoft.mobile.pos.ui.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.PeripheralDevice;
import no.susoft.mobile.pos.data.PeripheralProvider;
import no.susoft.mobile.pos.data.PeripheralType;

public class AdminDeviceListAdapter extends ArrayAdapter<PeripheralDevice> {

	private List<PeripheralDevice> list;

	public AdminDeviceListAdapter(Context context, int textViewResourceId, List<PeripheralDevice> objects) {
		super(context, textViewResourceId, objects);
		list = objects;
	}

	public void setList(List<PeripheralDevice> list) {
		this.list = list;
	}

	public List<PeripheralDevice> getList() {
		return this.list;
	}

	private class ViewHolder {

		private TextView tvName;
		private Spinner spType;
		private Spinner spProvider;
		private EditText etIP;
		private CheckBox cbIsPaired;
		private CheckBox cbIsUsed;
		public TextWatcher textWatcher;

	}

	public View getView(final int position, View convertView, ViewGroup parent) {

		try {
			if (convertView == null) {
				convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.device_list_item, parent, false);
				ViewHolder viewHolder = new ViewHolder();
				viewHolder.tvName = (TextView) convertView.findViewById(R.id.admin_device_name);
				viewHolder.spType = (Spinner) convertView.findViewById(R.id.admin_device_type);
				viewHolder.spProvider = (Spinner) convertView.findViewById(R.id.admin_device_provider);
				viewHolder.etIP = (EditText) convertView.findViewById(R.id.admin_device_ip);
				viewHolder.cbIsPaired = (CheckBox) convertView.findViewById(R.id.admin_device_paired);
				viewHolder.cbIsUsed = (CheckBox) convertView.findViewById(R.id.admin_device_used);
				convertView.setTag(viewHolder);
			}

			final ViewHolder viewHolder = (ViewHolder) convertView.getTag();

			viewHolder.tvName.setText(getItem(position).getName());
			viewHolder.spType.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, PeripheralType.values()));

			List<PeripheralType> typeList = Arrays.asList(PeripheralType.values());

			int typeIndex = typeList.indexOf(getItem(position).getType());

			if(!typeList.isEmpty()) {
				viewHolder.spType.setSelection(typeIndex);
			}

			viewHolder.spType.setSelection(getItem(position).getType().ordinal());
			viewHolder.spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int spinnerPosition, long id) {
					if (spinnerPosition != getItem(position).getType().ordinal()) {
						getItem(position).setType((PeripheralType) viewHolder.spType.getSelectedItem());
						getItem(position).setProvider(PeripheralProvider.NONE);
						getItem(position).setIp("");
						getItem(position).setUsed(false);
						fillProviders(viewHolder);
						notifyDataSetChanged();
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {

				}
			});

			fillProviders(viewHolder);

			List<PeripheralProvider> providerList = new ArrayList<>();
			switch((PeripheralType) viewHolder.spType.getSelectedItem()) {
				case PRINTER:
				case KITCHEN_PRINTER:
				case BAR_PRINTER:  providerList = Arrays.asList(PeripheralProvider.getPrinters());	break;
				case CARD_TERMINAL: providerList = Arrays.asList(PeripheralProvider.getCardTerminals());	break;
				case SCANNER: providerList = Arrays.asList(PeripheralProvider.getScanners());	break;
				case DISPLAY: providerList = Arrays.asList(PeripheralProvider.getDisplays());	break;
			}

			if (!providerList.isEmpty()) {
				int providerIndex = providerList.indexOf(getItem(position).getProvider());
				viewHolder.spProvider.setSelection(providerIndex);
			}
			viewHolder.spProvider.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int innerPosition, long id) {
					if (parent != null && parent.getSelectedItem() != null) {
						if (innerPosition != getItem(position).getProvider().ordinal()) {
							getItem(position).setProvider((PeripheralProvider) parent.getSelectedItem());
						}
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {

				}
			});

			if (viewHolder.textWatcher != null)
				viewHolder.etIP.removeTextChangedListener(viewHolder.textWatcher);

			viewHolder.etIP.setText(getItem(position).getIp());
			viewHolder.textWatcher = new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					getItem(position).setIp(s.toString());
				}

				@Override
				public void afterTextChanged(Editable s) {
				}
			};
			viewHolder.etIP.addTextChangedListener(viewHolder.textWatcher);

			viewHolder.cbIsPaired.setChecked(getItem(position).isPaired());
			viewHolder.cbIsUsed.setChecked(getItem(position).isUsed());
			viewHolder.cbIsUsed.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (viewHolder.cbIsUsed.isChecked()) {
						for (PeripheralDevice device : list) {
							if (getItem(position).getType() == device.getType()) {
								device.setUsed(false);
							}
						}
					}

					getItem(position).setUsed(viewHolder.cbIsUsed.isChecked());
					notifyDataSetChanged();
					try {
						Thread.dumpStack();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return convertView;
	}

	@Override
	public PeripheralDevice getItem(int position) {
		return super.getItem(position);
	}

	public void fillProviders(ViewHolder viewHolder) {
		switch ((PeripheralType) viewHolder.spType.getSelectedItem()) {
			case PRINTER:
			case KITCHEN_PRINTER:
			case BAR_PRINTER: {
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
