package no.susoft.mobile.pos.data;

import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public enum PeripheralProvider {

	NONE(MainActivity.getInstance().getString(R.string.please_select)),
	NETS(MainActivity.getInstance().getString(R.string.nets)),
	//    NETS_CABLE(MainActivity.getInstance().getString(R.string.netscable)),
	VERIFONE(MainActivity.getInstance().getString(R.string.verifone)),
	CASIO(MainActivity.getInstance().getString(R.string.casio)),
	BIXOLON(MainActivity.getInstance().getString(R.string.bixolon)),
	STAR(MainActivity.getInstance().getString(R.string.starDevice)),
	BLUETOOTH("BLUETOOTH");

	private String provider;

	PeripheralProvider(String provider) {
		this.provider = provider;
	}

	@Override
	public String toString() {
		return provider;
	}

	public static PeripheralProvider[] getCardTerminals() {
		return new PeripheralProvider[]{NONE, NETS, VERIFONE};
	}

	public static PeripheralProvider[] getPrinters() {
		return new PeripheralProvider[]{NONE, CASIO, BIXOLON, STAR, VERIFONE};
	}

	public static PeripheralProvider[] getScanners() {
		return new PeripheralProvider[]{NONE, CASIO, STAR};
	}

	public static PeripheralProvider[] getDisplays() {
		return new PeripheralProvider[]{NONE, BLUETOOTH};
	}

	public static PeripheralProvider[] getCashDrawers() {
		return new PeripheralProvider[]{NONE, CASIO, STAR, BIXOLON};
	}

	public static PeripheralProvider[] getKitchenPrinters() {
		return new PeripheralProvider[]{NONE, BIXOLON};
	}
}
