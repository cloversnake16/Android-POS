package no.susoft.mobile.pos.hardware.printer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.data.OrderLine;
import no.susoft.mobile.pos.data.Prepaid;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.activity.util.AppConfig;

public abstract class BluetoothPrint extends _PrinterUtils {
    protected static OutputStream mmOutputStream;
    protected static InputStream mmInputStream;
    protected static ArrayList<Object> output;
    // android built in classes for bluetooth operations
    protected BluetoothAdapter mBluetoothAdapter;
    protected BluetoothSocket mmSocket;
    protected BluetoothDevice mmDevice;
    protected Thread workerThread;
    protected byte[] readBuffer;
    protected int readBufferPosition;
    protected int counter;
    protected volatile boolean stopWorker;
    protected Context context;
    protected int qtySpace = 9;
    protected int priceSpace = 9;
    protected int priceExtraSpace = priceSpace + 3;
    protected int lm = 0; //left margin
    protected int rm = 0; //right margin
    protected Order order;
    protected boolean invoicePrinted;
    protected boolean returnsPrinted;
    protected final int slimWidth = 32;
    protected final int wideWidth = 42;


    protected abstract void handleHeaderPrint();

    protected abstract void handleExtraBottomPrint();

    protected abstract void printExtraTitle();

    protected abstract void handleOrderLinesPrint();

    protected abstract void handlePaymentPrint();

    protected abstract void handleChangePrint();

    protected abstract void handleVatPrint(List<OrderLine> list);

    protected abstract void bottomOfSendData();

    @Override
    protected void addLine(String line) {
        output.add(line);
    }

    @Override
    protected void addLine(byte[] cmd) {
        output.add(cmd);
    }

    protected String getOrderBarcodeString() {
		if (MainActivity.getInstance().isConnected()) {
			return "1O1" + order.getShopId() + order.getId();
		}
        return "1A1" + order.getAlternativeId();
    }

    protected String getPrepaidBarcodeString(Prepaid prepaid) {
        return "1G1" + prepaid.getShopId() + prepaid.getId();
    }

    protected void printOrderTotalWithDiscount() {
        if (!order.getAmount(true).isEqual(order.getAmount(false))) {
            addLine(makeLine(context.getString(R.string.total_discount) + ":", order.getAmount(false).subtract(order.getAmount(true)).toString(), width, lm, rm));
            addLine(makeLine("", rightLineOfCharOfSize('-', width), width, lm, rm));
        }
        addLine(makeLine(context.getString(R.string.total_purchase) + ":", order.getAmount(true).toString(), width, lm, rm));
        addLine(makeLine("", rightLineOfCharOfSize('-', priceExtraSpace), width, lm, rm));
    }

    /*
     * This will send data to be printed by the bluetooth printer
     */

    protected byte[] getLineSpacing() {
        return new byte[]{0x1b, 0x33, 0x1e};
    }

    protected byte[] getNordicCharset() {
        return new byte[]{0x1b, 0x74, 0x05};
    }

    protected void sendData(ArrayList<Object> msg) {
        try {
			byte[] cmd;

			mmOutputStream.write(getLineSpacing());
			mmOutputStream.flush();

			mmOutputStream.write(getNordicCharset());
			mmOutputStream.flush();

			boolean firstLine = true;
			for (Object line : msg) {
				if (firstLine) {
					//big text
					cmd = new byte[]{0x1b, 0x21, 0x10};
					mmOutputStream.write(cmd);
					mmOutputStream.flush();
					cmd = new byte[]{0x1b, 0x21, 0x20};
					mmOutputStream.write(cmd);
					mmOutputStream.flush();

					firstLine = false;
					//write first line/shop name
					if (line.getClass().equals(String.class)) {
						mmOutputStream.write(extendedAsciiReplaceNordicChars((String) line));
					} else {
						mmOutputStream.write((byte[]) line);
					}
					cmd = new byte[]{0x1b, 0x64, 0x4};
					mmOutputStream.write(cmd);
					mmOutputStream.flush();

					//back to normal text
					cmd = new byte[]{0x1b, 0x21, 0x00};
					mmOutputStream.write(cmd);

					mmOutputStream.flush();
				} else {
					if (line.getClass().equals(String.class)) {
						mmOutputStream.write(extendedAsciiReplaceNordicChars((String) line));
					} else {
						mmOutputStream.write((byte[]) line);
					}
					mmOutputStream.flush();
				}
			}
            bottomOfSendData();
        } catch (Exception e) {
			ErrorReporter.INSTANCE.filelog("BluetoothPrint.sendData()", "Error", e);
        }
    }

    protected void doExtraAfterSendData() {
		closeBT();
    }

    protected ArrayList<Object> PreparePrint(Order order) {

        this.order = order;
        context = MainActivity.getInstance();
        output = new ArrayList<>();
        try {
            handleHeaderPrint();
            handleMainBodyPrint();
            handleExtraBottomPrint();
        } catch (Exception e) {
			ErrorReporter.INSTANCE.filelog("BluetoothPrint.PreparePrint()", "Error", e);
        }
        return output;
    }

    protected void handleMainBodyPrint() {
        handleOrderLinesPrint();
        handlePaymentPrint();
        handleChangePrint();
        handleVatPrint(order.getLines());
    }

    protected byte[] extendedAsciiReplaceNordicChars(String input) {
        int length = input.length();
        byte[] retVal = new byte[length];

        for (int i = 0; i < length; i++) {
            char c = input.charAt(i);

            if (c < 127) {
                retVal[i] = (byte) c;
            } else {
                switch ((byte) (c - 256)) {
                    case -27:   retVal[i] = (byte) 0x86;    break; //å
                    case -59:   retVal[i] = (byte) 0x8F;    break; //Å
                    case -26:   retVal[i] = (byte) 0x91;    break; //æ
                    case -58:   retVal[i] = (byte) 0x92;    break; //Æ
                    case -8:    retVal[i] = (byte) 0x9b;    break; //ø to ö
                    case -40:   retVal[i] = (byte) 0x9d;    break; //Ø to Ö
                    case -28:   retVal[i] = (byte) 0x84;    break; //ä
                    default:    retVal[i] = (byte) (c - 256);    break;
                }
            }
        }
        return retVal;
    }

    // This will find a bluetooth printer device
    protected synchronized void findBT(ArrayList<Object> msg) {
        try {
            if (mmDevice == null) {
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                if (!mBluetoothAdapter.isEnabled()) {
					mBluetoothAdapter.enable();
                    Toast.makeText(MainActivity.getInstance(), MainActivity.getInstance().getString(R.string.enable_bluetooth), Toast.LENGTH_SHORT).show();
                }

                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
				ErrorReporter.INSTANCE.filelog("pairedDevices.size() = " + pairedDevices.size());
                if (pairedDevices.size() > 0) {
                    for (BluetoothDevice device : pairedDevices) {
                        if (device.getName().equals(AppConfig.getState().getPrinterName())) {
							ErrorReporter.INSTANCE.filelog("device = " + device.getName());
                            mmDevice = device;
                            openBT(msg);
                            break;
                        }
                    }
                } else {
                    Log.i("vilde", "no device..");
                }
            } else {
                openBT(msg);
            }

        } catch (Exception e) {
			ErrorReporter.INSTANCE.filelog("BluetoothPrint.findBT()", "Error", e);
        }
    }

    // After opening a connection to bluetooth printer device,
    // we have to listen and check if a data were sent to be printed.
    protected synchronized void beginListenForData() {
        try {

            // This is the ASCII code for a newline character
            final byte delimiter = 10;

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];

            workerThread = new Thread(new Runnable() {
                public void run() {
                    while (!Thread.currentThread().isInterrupted()
                            && !stopWorker) {

                        try {
                            int bytesAvailable = mmInputStream.available();
                            if (bytesAvailable > 0) {
                                byte[] packetBytes = new byte[bytesAvailable];
                                mmInputStream.read(packetBytes);
                                for (int i = 0; i < bytesAvailable; i++) {
                                    byte b = packetBytes[i];
                                    if (b == delimiter) {
                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(readBuffer, 0,
                                                encodedBytes, 0,
                                                encodedBytes.length);
                                        final String data = new String(
                                                encodedBytes, "UTF-8");
                                        readBufferPosition = 0;

                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }

                        } catch (IOException ex) {
                            stopWorker = true;
                        }

                    }
                }
            });

            workerThread.start();

        } catch (Exception e) {
			ErrorReporter.INSTANCE.filelog("BluetoothPrint.beginListenForData()", "Error", e);
        }
    }


    // Close the connection to bluetooth printer.
    public void closeBT() {
        try {
			ErrorReporter.INSTANCE.filelog("closeBT..");
            stopWorker = true;
            mmOutputStream.close();
            mmInputStream.close();
            mmSocket.close();
        } catch (Exception e) {
			ErrorReporter.INSTANCE.filelog("BluetoothPrint.closeBT()", "Error", e);
        }
    }

    // Tries to open a connection to the bluetooth printer device
    protected synchronized void openBT(ArrayList<Object> msg) {
        try {
            // Standard SerialPortService ID
            //UUID uuid = UUID.fromString("00001000-0000-1000-8000-00805f9b34fb");

			if (mBluetoothAdapter.isDiscovering()) {
				mBluetoothAdapter.cancelDiscovery();
			}

			ParcelUuid selectedUuid = null;
			ParcelUuid[] supportedUuids = mmDevice.getUuids();
			try {
				Class cl = Class.forName("android.bluetooth.BluetoothDevice");
				Class[] params = {};
				Method method = cl.getMethod("getUuids", params);
				Object[] args = {};
				supportedUuids = (ParcelUuid[]) method.invoke(mmDevice, args);
				if (supportedUuids != null && supportedUuids.length > 0) {
					selectedUuid = supportedUuids[0];
				}
			} catch (Exception e) {
				ErrorReporter.INSTANCE.filelog("Error UUID", "", e);
			}

			if (selectedUuid != null) {
                try {
                    mmSocket = mmDevice.createRfcommSocketToServiceRecord(selectedUuid.getUuid());
                    mmSocket.connect();
                } catch (IOException ex) {
                    mmSocket = mmDevice.createInsecureRfcommSocketToServiceRecord(selectedUuid.getUuid());
                    mmSocket.connect();
                }
			} else {
				Class<?> device = mmDevice.getClass();
				Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};
				Method m = device.getMethod("createRfcommSocket", paramTypes);
				Object[] params = new Object[]{Integer.valueOf(1)};

				mmSocket = (BluetoothSocket) m.invoke(mmDevice, params);
                mmSocket.connect();
			}


            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();

            beginListenForData();
            sendData(msg);

            doExtraAfterSendData();

        } catch (Exception e) {
			ErrorReporter.INSTANCE.filelog("openBT", "", e);
			Toast.makeText(MainActivity.getInstance(), R.string.cannot_connect_bluetooth_printer, Toast.LENGTH_SHORT).show();
        }
    }

    public synchronized void print(ArrayList<Object> s) {
		if ((mmDevice == null && mmSocket == null) || (mmSocket != null && !mmSocket.isConnected())) {
			findBT(s);
		} else if (mmSocket != null && mmSocket.isConnected()) {
            beginListenForData();
            sendData(s);
            doExtraAfterSendData();
        }
    }

    public int openCashDrawer() {
        if ((mmDevice == null && mmSocket == null) || (mmSocket != null && !mmSocket.isConnected())) {
            findBT(null);
        } else if (mmSocket != null && mmSocket.isConnected()) {
            beginListenForData();
            try {
                mmOutputStream.write(new byte[]{0x1b, 0x70, 0x30, 0x37, 0x79});
                mmOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            closeBT();
        }
		return 0;
    }

    protected void printLargeText() {
        byte[] cmd = new byte[]{0x1b, 0x21, 0x10};
        addLine(cmd);
        cmd = new byte[]{0x1b, 0x21, 0x20};
        addLine(cmd);
    }

    protected void printNormalText() {
        byte[] cmd = new byte[]{0x1b, 0x21, 0x00};
        addLine(cmd);
    }

    protected void printEmptyLine() {
//        String emptyline = "";
//        for (int i = 0; i < (width - lm - rm); i++) {
//            emptyline = emptyline + " ";
//        }
//        addLine(emptyline);
        addLine("\r\n");
    }

    protected void printRightLineOfLength(char c, int length) {
        addLine(makeLine("", rightLineOfCharOfSize(c, length), width, lm, rm));
    }

    protected ArrayList<byte[]> printBarcode(String barcode) {
//		if (barcode.length() >= 15) {
//			return printBarcodeCode128(barcode);
//		}
		return printBarcodeCode93(barcode);
    }
    
    protected ArrayList<byte[]> printBarcodeCode93(String barcode) {
        ArrayList<byte[]> commands = new ArrayList<>();
        commands.add(new byte[]{0x1b, 0x61, 0x01}); //center align
        commands.add(new byte[]{0x1d, 0x68, 0x50}); //height 80 dots = 0x50
        commands.add(new byte[]{0x1d, 0x6b, 0x48});
        commands.add(new byte[]{(byte)barcode.length()});
        commands.add(barcode.getBytes());
        commands.add(new byte[]{0x1b, 0x61, 0x00}); //left align
		
        return commands;
    }

    protected ArrayList<byte[]> printBarcodeCode128(String barcode) {
        ArrayList<byte[]> commands = new ArrayList<>();
        commands.add(new byte[]{0x1b, 0x61, 0x01}); //center align
        commands.add(new byte[]{0x1d, 0x68, 0x50}); //height 80 dots = 0x50
        commands.add(new byte[]{0x1d, 0x6b, 0x49}); // Code128
        commands.add(new byte[]{(byte)barcode.length()});
        commands.add(barcode.getBytes());
        commands.add(new byte[]{0x1b, 0x61, 0x00}); //left align

        return commands;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    protected void printPrepaidValidityDate(Date dueDate) {
        if (dueDate != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-mm-yyyy");
            addLine(makeCenterizedLine(context.getString(R.string.valid_to) + ": " + dateFormat.format(dueDate), width));
        }
    }

    protected void printPrepaidAmount(Prepaid prepaid) {
        printLargeText();
        addLine(makeCenterizedLineLargeFont(context.getString(R.string.amount) + " " + prepaid.getAmount(), width));
        printNormalText();
    }

    protected void printPrepaidNumber(Prepaid prepaid) {
        String title;
        if (prepaid.getType().equalsIgnoreCase("C")) {
            title = context.getString(R.string.credit_voucher);
        } else {
            title = context.getString(R.string.gift_card);
        }
        addLine(makeCenterizedLine(title + context.getString(R.string.number) + ": " + prepaid.getNumber(), width));
    }

}


