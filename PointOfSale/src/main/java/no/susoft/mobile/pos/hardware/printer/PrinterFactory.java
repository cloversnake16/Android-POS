package no.susoft.mobile.pos.hardware.printer;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import no.susoft.mobile.pos.SusoftPOSApplication;
import no.susoft.mobile.pos.data.PeripheralDevice;
import no.susoft.mobile.pos.data.PeripheralProvider;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.hardware.combi.Star_mPOP;
import no.susoft.mobile.pos.hardware.terminal.CardTerminal;
import no.susoft.mobile.pos.hardware.terminal.CardTerminalFactory;
import no.susoft.mobile.pos.hardware.terminal.VerifonePim;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class PrinterFactory {

	public static PrinterFactory instance = null;
	private Map<Integer, Printer> printers = new HashMap<>();

	private PrinterFactory() {
	}

	public static PrinterFactory getInstance() {
		if (instance == null)
			instance = new PrinterFactory();

		return instance;
	}

	public Printer getPrinter() {

		SharedPreferences preferences = SusoftPOSApplication.getContext().getSharedPreferences(PeripheralDevice.class.toString(), Context.MODE_PRIVATE);
		int peripheralProvider = preferences.getInt("PRINTER_PROVIDER", PeripheralProvider.NONE.ordinal());

		if (!printers.containsKey(peripheralProvider)) {
			if (peripheralProvider == PeripheralProvider.VERIFONE.ordinal()) {

				CardTerminal cardTerminal = null;
				try {
					cardTerminal = CardTerminalFactory.getInstance().getCardTerminal();
				} catch (Exception e) {
					ErrorReporter.INSTANCE.filelog("PrinterFactory", "adding VERIFONE Printer to cache failed!", e);
					e.printStackTrace();
				}

				if (cardTerminal != null && cardTerminal instanceof VerifonePim) {
					printers.put(peripheralProvider, VerifonePrinter.getInstance());
				} else {
					ErrorReporter.INSTANCE.filelog("CardTerminalFactory", "printing on VERIFONE printer is available only when VERIFONE card terminal is connected");
				}
			} else if (peripheralProvider == PeripheralProvider.STAR.ordinal()) {
				printers.put(peripheralProvider, new Star_mPOP(MainActivity.getInstance()));
			} else if (peripheralProvider == PeripheralProvider.BIXOLON.ordinal()) {
				MainActivity.getInstance().getPOSPrinter();
			}
		}

		return printers.get(peripheralProvider);
	}
}
