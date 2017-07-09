package no.susoft.mobile.pos.ui.dialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.PeripheralProvider;
import no.susoft.mobile.pos.data.Product;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.hardware.combi.mPOPPrintSupply;
import no.susoft.mobile.pos.hardware.printer.*;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.activity.util.AppConfig;

import static no.susoft.mobile.pos.ui.dialog._DialogUtils.*;

public class SupplyReportDialog extends DialogFragment {

	@InjectView(R.id.progress_bar_supply)
	ProgressBar progressBar;
	@InjectView(R.id.print_button)
	Button printButton;
	@InjectView(R.id.from_date)
	DatePicker fromDate;
	@InjectView(R.id.to_date)
	DatePicker toDate;
	@InjectView(R.id.from_time)
	TimePicker fromTime;
	@InjectView(R.id.to_time)
	TimePicker toTime;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final View rootView = inflater.inflate(R.layout.supply_report_dialog, container, false);
		ButterKnife.inject(this, rootView);
		progressBar.setVisibility(View.GONE);
		setPrintOnClickListener();
		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();
		try {
			getDialog().setTitle(R.string.supply_report);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		fromDate.setCalendarViewShown(false);
		toDate.setCalendarViewShown(false);
		fromTime.setIs24HourView(true);
		fromTime.setCurrentHour(0);
		fromTime.setCurrentMinute(0);
		toTime.setIs24HourView(true);
		toTime.setCurrentHour(23);
		toTime.setCurrentMinute(59);
	}

	private void setPrintOnClickListener() {
		printButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				doSearch();
			}
		});
	}

	private void doSearch() {
		showProgressBar();
		if (datesValid(getDateFromDatePicker(fromDate), getDateFromDatePicker(toDate))) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(getDateFromDatePicker(fromDate));
			calendar.set(Calendar.HOUR_OF_DAY, fromTime.getCurrentHour());
			calendar.set(Calendar.MINUTE, fromTime.getCurrentMinute());
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			Date from = calendar.getTime();

			calendar.setTime(getDateFromDatePicker(toDate));
			calendar.set(Calendar.HOUR_OF_DAY, toTime.getCurrentHour());
			calendar.set(Calendar.MINUTE, toTime.getCurrentMinute());
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			Date to = calendar.getTime();

			MainActivity.getInstance().getServerCallMethods().loadSupplyReportFromServer(this, from, to);
		}
	}

	private void showProgressBar() {
		setVisibility(View.VISIBLE, progressBar);
	}

	public void printReport(final ArrayList<Product> products) {
		setVisibility(View.GONE, progressBar);
		if (products != null && products.size() > 0) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(getDateFromDatePicker(fromDate));
			calendar.set(Calendar.HOUR_OF_DAY, fromTime.getCurrentHour());
			calendar.set(Calendar.MINUTE, fromTime.getCurrentMinute());
			final Date from = calendar.getTime();

			calendar.setTime(getDateFromDatePicker(toDate));
			calendar.set(Calendar.HOUR_OF_DAY, toTime.getCurrentHour());
			calendar.set(Calendar.MINUTE, toTime.getCurrentMinute());
			final Date to = calendar.getTime();

			int printerProvider = AppConfig.getState().getPrinterProviderOrdinal();
			
			ErrorReporter.INSTANCE.filelog("printerProvider = " + printerProvider);
			
			if (printerProvider == PeripheralProvider.CASIO.ordinal()) {
				// region CASIO
				
				ErrorReporter.INSTANCE.filelog("CasioPrint.hasPrinterConnected() = " + CasioPrint.hasPrinterConnected());
				
				if (CasioPrint.hasPrinterConnected()) {
					try {
						CasioPrintSupply po = new CasioPrintSupply();
						po.print(products, from, to);
					} catch (Exception e) {
						ErrorReporter.INSTANCE.filelog(e);
					}
				}
				// endregion
			} else if (printerProvider == PeripheralProvider.BIXOLON.ordinal()) {
				// region BIXOLON
				
				ErrorReporter.INSTANCE.filelog("AppConfig.getState().getPrinterIp() = " + AppConfig.getState().getPrinterIp());
				
				if (AppConfig.getState().getPrinterIp().isEmpty()) {
					new BluetoothPrintSupply(products, from, to);
				} else {
					new IPPrintSupply(products, from, to);
				}
				// endregion
			} else if (printerProvider == PeripheralProvider.STAR.ordinal()) {
				// region STAR
				new mPOPPrintSupply(products, from, to);
				// endregion
			} else if (printerProvider == PeripheralProvider.VERIFONE.ordinal()) {
				//region VERIFONE
				Printer printer = PrinterFactory.getInstance().getPrinter();
				if (printer != null && printer instanceof VerifonePrinter) {
					((VerifonePrinter) printer).printSupplyReport(products, from, to);
				}
				//endregion
			} else {
				Toast.makeText(MainActivity.getInstance(), MainActivity.getInstance().getResources().getString(R.string.no_printer_connected), Toast.LENGTH_LONG).show();
			}
		}

		dismiss();
	}

}
