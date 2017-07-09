package no.susoft.mobile.pos.hardware.combi;

import java.nio.ByteBuffer;
import java.util.ArrayList;

class mPOP_CommandDataList extends ArrayList<Byte> {

    mPOP_CommandDataList add(int... arg) {
        for(int value:arg) {
            add((byte) value);
        }
        return this;
    }

    mPOP_CommandDataList add(byte[] arg) {
        for(byte value:arg) {
            add( value );
        }
        return this;
    }

    mPOP_CommandDataList add(String arg) {
        byte[] argByte = arg.getBytes();

        for(byte value:argByte) {
            add( value );
        }
        return this;
    }

    byte[] getByteArray() {
        ByteBuffer output;

        output = ByteBuffer.allocate(this.size());

        for(Byte value:this) {
            output.put(value);
        }

        return output.array();
    }
}
