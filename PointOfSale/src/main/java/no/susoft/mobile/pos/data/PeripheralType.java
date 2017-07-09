package no.susoft.mobile.pos.data;

import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public enum PeripheralType {

    NONE(MainActivity.getInstance().getString(R.string.please_select)),
    CARD_TERMINAL(MainActivity.getInstance().getString(R.string.card_terminal)),
    PRINTER(MainActivity.getInstance().getString(R.string.printer)),
    SCANNER(MainActivity.getInstance().getString(R.string.scanner)),
    DISPLAY(MainActivity.getInstance().getString(R.string.display)),
    CASHDRAWER(MainActivity.getInstance().getString(R.string.cashdrawer)),
    KITCHEN_PRINTER(MainActivity.getInstance().getString(R.string.kitchen_printer)),
    BAR_PRINTER(MainActivity.getInstance().getString(R.string.bar_printer));

    private String type;

    PeripheralType(String type) {
        this.type = type;
    }

    public static String[] getStrings() {
        return new String[]{CARD_TERMINAL.toString(), PRINTER.toString(), SCANNER.toString(), DISPLAY.toString(), CASHDRAWER.toString(), KITCHEN_PRINTER.toString(), BAR_PRINTER.toString()};
    }

    @Override public String toString() {
        return type;
    }
}
