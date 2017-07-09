package no.susoft.mobile.pos.hardware.combi;

import java.util.ArrayList;

import no.susoft.mobile.pos.hardware.printer.BluetoothPrintCashcount;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class mPOPPrintCashcount extends BluetoothPrintCashcount {

    public mPOPPrintCashcount(String json) {
        width = 32;
        this.context = MainActivity.getInstance();
        output = new ArrayList<>();
		Communication.sendCommands(convertArrayListToBytes(makeReport(json)), Star_mPOP.getPort(), MainActivity.getInstance());
    }

    public mPOPPrintCashcount() {
    }

    protected byte[] convertArrayListToBytes(ArrayList<Object> printLines) {
        mPOP_CommandDataList commands = new mPOP_CommandDataList();

        //nordic char set?
        commands.add(nordicCharset());
        commands.add(getLineSpacing());

        for(Object o : printLines) {
            if(o.getClass().equals(String.class)) {
                commands.add(extendedAsciiReplaceNordicChars((String) o));
            } else {
                commands.add((byte[]) o);
            }
        }

        commands.add(feedLines(2));
        commands.add(cutPaper());                    // Cut Paper
        return commands.getByteArray();
    }

    private String arrayToString(byte[] array) {
        String output = "";
        for(byte b : array) {
            output+= String.valueOf(b) + ", ";
        }
        return output;
    }

    protected byte[] feedLines(int i) {
        return new byte[]{0x1b, 0x61, Byte.valueOf(String.valueOf(i)) };
    }

    @Override
    protected byte[] getLineSpacing() {
        return new byte[]{0x1b, 0x30};
    }

    private byte[] nordicCharset() {
//        return new byte[]{0x1b, 0x52, 0x09};
        return new byte[]{0x1b, 0x1d, 0x74, 0x09};
    }

    private byte[] cutPaper() {
        return new byte[]{0x1b, 0x64, 0x03};
    }

    @Override
    protected void printLargeText() {
        addLine(new byte[]{0x09, 0x1b, 0x69, 0x01, 0x01});         // Set DoubleHW
    }

    @Override
    protected void printNormalText() {
        addLine(new byte[]{0x1b, 0x69, 0x00, 0x00});
    }

}
