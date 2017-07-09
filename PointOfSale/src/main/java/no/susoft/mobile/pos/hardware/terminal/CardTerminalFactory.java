package no.susoft.mobile.pos.hardware.terminal;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import no.susoft.mobile.pos.SusoftPOSApplication;
import no.susoft.mobile.pos.data.PeripheralDevice;
import no.susoft.mobile.pos.data.PeripheralProvider;
import no.susoft.mobile.pos.data.PeripheralType;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.hardware.terminal.exception.CardTerminalException;
import no.susoft.mobile.pos.hardware.terminal.exception.CardTerminalInBankModeException;
import no.susoft.mobile.pos.hardware.terminal.exception.CardTerminalNotConnectedException;
import org.apache.commons.validator.routines.InetAddressValidator;

/**
 * Created on 3/1/2016.
 */
public class CardTerminalFactory {

    public static final String IP_SETTING_KEY = "CARD_TERMINAL_IP";
    public static final String CARD_TERMINAL_NAME = "CARD_TERMINAL_NAME";

    public static CardTerminalFactory instance = null;

    private Map<Integer, CardTerminal> cardTerminals = null;

    private CardTerminalFactory() {

    }

    public static CardTerminalFactory getInstance() {
        if (instance == null) instance = new CardTerminalFactory();

        return instance;
    }

    public CardTerminal getCardTerminal() throws CardTerminalInBankModeException, CardTerminalNotConnectedException {
        if (cardTerminals == null)
            cardTerminals = new HashMap<>();

        SharedPreferences preferences = SusoftPOSApplication.getContext().getSharedPreferences(PeripheralDevice.class.toString(), Context.MODE_PRIVATE);
        String ipAddress = preferences.getString(IP_SETTING_KEY, ""); // for IP devices

        int peripheralProvider = preferences.getInt("CARD_TERMINAL_PROVIDER", PeripheralProvider.NONE.ordinal());

        if (!cardTerminals.containsKey(peripheralProvider)) {
            if (peripheralProvider == PeripheralProvider.NETS.ordinal()) {
                cardTerminals.put(peripheralProvider, NetsTerminal.getInstance());
            } else if (peripheralProvider == PeripheralProvider.VERIFONE.ordinal()) {
                if (InetAddressValidator.getInstance().isValidInet4Address(ipAddress)) {
                    cardTerminals.put(peripheralProvider, VerifonePim.getInstance());
                }
            }
        }

        return cardTerminals.get(peripheralProvider);
    }

    public CardTerminal getCardTerminal(int terminalId) throws Exception {

        if (cardTerminals == null)
            cardTerminals = new HashMap<>();

        if (!cardTerminals.containsKey(terminalId)) {
            if (terminalId == CardTerminal.TERMINAL_NETS)
                cardTerminals.put(CardTerminal.TERMINAL_NETS, NetsTerminal.getInstance());
            if (terminalId == CardTerminal.TERMINAL_VERIFONE)
                cardTerminals.put(CardTerminal.TERMINAL_VERIFONE, VerifonePim.getInstance());
        }

        return cardTerminals.get(terminalId);
    }
}
