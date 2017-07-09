package no.susoft.mobile.pos.hardware.printer;

import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.widget.Toast;
import com.bxl.BXLConst;
import com.bxl.config.editor.BXLConfigLoader;
import jpos.JposConst;
import jpos.JposException;
import jpos.POSPrinter;
import jpos.config.JposEntry;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.data.OrderLine;
import no.susoft.mobile.pos.data.Prepaid;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.activity.util.AppConfig;

import static android.content.Context.MODE_PRIVATE;

public abstract class IPPrint extends BluetoothPrintOrderWide {

    public static final String CONFIG_FILE_NAME = "jpos.xml";

    protected BXLConfigLoader config;
    protected int counter;
    protected Context context;
    protected int qtySpace = 9;
    protected int priceSpace = 9;
    protected int priceExtraSpace = priceSpace + 3;
    protected int lm = 0; //left margin
    protected int rm = 0; //right margin
    protected Order order;
    protected boolean returnsPrinted;
    protected final int slimWidth = 32;
    protected final int wideWidth = 42;
    private String logicalName = "";
    private POSPrinter posPrinter;

    public int openCashDrawer() {
		int result = 0;
        try {
            setupPrinter(AppConfig.getState().getPrinterName(), AppConfig.getState().getPrinterIp());
            openPrinter();
            result = sendOpenCashDrawerCommand();
        } catch (Exception e) {
            e.printStackTrace();
        }
		return result;
    }

    protected int sendOpenCashDrawerCommand() {
        try {
            getPOSPrinter().directIO(1, null, new byte[]{0x1b, 0x70, 0x30, 0x37, 0x79});// String.valueOf((char) 27 + (char) 112 + (char) 48 + (char) 55 + (char) 121).getBytes());
			return 0;
        } catch (JposException e) {
            e.printStackTrace();
        }
		return -1;
    }

    protected void setupPrinter(String productName, String ipAddress) {
        this.logicalName = productName + "_" + ipAddress;

        if (config == null) {
            config = new BXLConfigLoader(context);
            try {
                try {
                    config.openFile();
					if (config.getEntries() == null || config.getEntries().size() == 0) {
						createNewConfigFile();
						config.openFile();
					}
                } catch (Exception e1) {
                    createNewConfigFile();
                    config.openFile();
                }
            } catch (Exception e) {
                ErrorReporter.INSTANCE.filelog(e);
            }

            try {
                if (config.getEntries() != null) {
                    boolean contains = false;
                    for (JposEntry entry : config.getEntries()) {
                        if (entry.getLogicalName().equals(logicalName)) {
                            entry.modifyPropertyValue("address", ipAddress);
                            contains = true;
                        }
                    }
                    if (!contains) {
                        config.addEntry(logicalName, BXLConfigLoader.DEVICE_CATEGORY_POS_PRINTER, productName, BXLConfigLoader.DEVICE_BUS_ETHERNET, ipAddress);
                    }
                }

                config.saveFile();
            } catch (Exception e) {
                ErrorReporter.INSTANCE.filelog(e);
            }
        }
    }

    private void createConfigFile() {
        FileInputStream fis = null;

        try {
            fis = context.openFileInput(CONFIG_FILE_NAME);
        } catch (FileNotFoundException e) {
            createNewConfigFile();
        }

        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e) {
                ErrorReporter.INSTANCE.filelog(e);
            }
        }
    }

    private void createNewConfigFile() {
        InputStream is = context.getResources().openRawResource(R.raw.jpos);
        FileOutputStream fos = null;

        int available = 0;
        byte[] buffer = null;

        try {
            available = is.available();
            buffer = new byte[available];
            is.read(buffer);

            fos = context.openFileOutput(CONFIG_FILE_NAME, MODE_PRIVATE);
            fos.write(buffer);
        } catch (IOException e) {
            ErrorReporter.INSTANCE.filelog(e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                ErrorReporter.INSTANCE.filelog(e);
            }

            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected POSPrinter getPOSPrinter() {
//        if (posPrinter == null) {
//            posPrinter = new POSPrinter(context);
//        }
        return MainActivity.getInstance().getPOSPrinter();
    }

    protected int openPrinter() {
        try {
			if (getPOSPrinter().getState() == JposConst.JPOS_S_CLOSED) {
				getPOSPrinter().open(logicalName);
				getPOSPrinter().setAsyncMode(true);
			}
			if (!getPOSPrinter().getClaimed()) {
				getPOSPrinter().claim(10000);
				getPOSPrinter().setDeviceEnabled(true);
				getPOSPrinter().setCharacterSet(BXLConst.CS_865_NORDIC);
			}
			return 0;
        } catch (JposException e) {
			ErrorReporter.INSTANCE.filelog("logicalName = " + logicalName + " 	e.getErrorCode() = " + e.getErrorCode());
			if (e.getOrigException() != null) {
				ErrorReporter.INSTANCE.filelog("e.getOrigException() = " + e.getOrigException().toString());
			}
			ErrorReporter.INSTANCE.filelog(e);
			
			try {
				getPOSPrinter().close();
			} catch (Exception e1) {
				ErrorReporter.INSTANCE.filelog(e1);
			}
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
		}
		return -1;
    }

	protected void closePrinter() {
		try {
			if (getPOSPrinter().getState() == JposConst.JPOS_S_IDLE)
				getPOSPrinter().close();
		} catch (JposException e) {
			e.printStackTrace();
		}
	}

    public int printIP(Order order, String printername, String printerip) {
		this.order = order;
        setupPrinter(printername, printerip);

        int result = openPrinter();
		if (result != 0) {
			return result;
		}

		// hack - need to be recoded
		if (logicalName.startsWith(BXLConst.SRP_275III)) {
			width = slimWidth;
		}
		
        PreparePrint(order);
        result = sendData();
		return result;
    }

    public int publicPrintRawData(ArrayList<Object> output) {
        setupPrinter(AppConfig.getState().getPrinterName(), AppConfig.getState().getPrinterIp());
        int result = openPrinter();
		if (result != 0) {
			return result;
		}

        try {
            //reset
            byte[] cmd;
            getPOSPrinter().directIO(1, null, getLineSpacing());
            getPOSPrinter().directIO(1, null, getNordicCharset());

            boolean firstLine = true;
            for (Object line : output) {
                if (firstLine) {
                    //big text
                    cmd = new byte[]{0x1b, 0x21, 0x10};
                    getPOSPrinter().directIO(1, null, cmd);
                    cmd = new byte[]{0x1b, 0x21, 0x20};
                    getPOSPrinter().directIO(1, null, cmd);

                    firstLine = false;
                    //write first line/shop name
                    if (line.getClass().equals(String.class)) {
                        getPOSPrinter().directIO(1, null, extendedAsciiReplaceNordicChars((String) line));
                    } else {
                        getPOSPrinter().directIO(1, null, (byte[]) line);
                    }
                    cmd = new byte[]{0x1b, 0x64, 0x4};
                    getPOSPrinter().directIO(1, null, cmd);

                    //back to normal text
                    cmd = new byte[]{0x1b, 0x21, 0x00};
                    getPOSPrinter().directIO(1, null, cmd);

                } else {
                    if (line.getClass().equals(String.class)) {
                        getPOSPrinter().directIO(1, null, extendedAsciiReplaceNordicChars((String) line));
                    } else {
                        getPOSPrinter().directIO(1, null, (byte[]) line);
                    }
                }
            }

            cmd = new byte[]{0x1b, 0x64, 0xC};
            getPOSPrinter().directIO(1, null, " ".getBytes(Charset.forName("UTF-8")));
            getPOSPrinter().directIO(1, null, cmd);
            getPOSPrinter().directIO(1, null, new byte[]{0x1b, 0x69});
//            getPOSPrinter().release();

			return 0;

        } catch (Exception e) {
            ErrorReporter.INSTANCE.filelog(e);
        }

		return -1;
    }


    protected int sendData() {
        try {
            //reset
            byte[] cmd;
            getPOSPrinter().directIO(1, null, getLineSpacing());
            getPOSPrinter().directIO(1, null, getNordicCharset());

            boolean firstLine = true;
            for (Object line : output) {
                if (firstLine) {
                    //big text
                    cmd = new byte[]{0x1b, 0x21, 0x10};
                    getPOSPrinter().directIO(1, null, cmd);
                    cmd = new byte[]{0x1b, 0x21, 0x20};
                    getPOSPrinter().directIO(1, null, cmd);

                    firstLine = false;
                    //write first line/shop name
                    if (line.getClass().equals(String.class)) {
                        getPOSPrinter().directIO(1, null, extendedAsciiReplaceNordicChars((String) line));
                    } else {
                        getPOSPrinter().directIO(1, null, (byte[]) line);
                    }
                    cmd = new byte[]{0x1b, 0x64, 0x4};
                    getPOSPrinter().directIO(1, null, cmd);

                    //back to normal text
                    cmd = new byte[]{0x1b, 0x21, 0x00};
                    getPOSPrinter().directIO(1, null, cmd);

                } else {
                    if (line.getClass().equals(String.class)) {
                        getPOSPrinter().directIO(1, null, extendedAsciiReplaceNordicChars((String) line));
                    } else {
                        getPOSPrinter().directIO(1, null, (byte[]) line);
                    }
                }
            }
            
            char ESC = (char)27; //Ascii character for ESCAPE
            cmd = new byte[]{0x1b, 0x64, 0xC};
            getPOSPrinter().directIO(1, null, " ".getBytes(Charset.forName("UTF-8")));
            getPOSPrinter().directIO(1, null, cmd);
            getPOSPrinter().directIO(1, null, (ESC+"i").getBytes());
//			getPOSPrinter().directIO(1, null, new byte[]{0x1b, 0x69});
//            getPOSPrinter().release();

			return 0;

        } catch (Exception e) {
            ErrorReporter.INSTANCE.filelog(e);
        }
		return -1;
    }

    protected void doExtraAfterSendData() {
        try {
            if (!isCopy && order != null && order.hasReturnLines()) {
                new IPPrintReturns(order);
            }
        } catch (Exception e) {
            Toast.makeText(MainActivity.getInstance(), R.string.cannot_connect_bluetooth_printer, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
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
            ErrorReporter.INSTANCE.filelog(e);
        }
        return output;
    }

    protected String getOrderBarcodeString() {
		if (MainActivity.getInstance().isConnected()) {
			return "1O1" + order.getShopId() + order.getId();
		}
		return "1A1" + order.getAlternativeId();
	}

    protected String getPrepaidBarcodeString(Prepaid prepaid) {
        return "1G1" + prepaid.getNumber();
    }

    protected void printOrderTotalWithDiscount() {
        if (!order.getAmount(true).isEqual(order.getAmount(false))) {
            addLine(makeLine(context.getString(R.string.total_discount) + ":", order.getAmount(false).subtract(order.getAmount(true)).toString(), width, lm, rm));
            addLine(makeLine("", rightLineOfCharOfSize('-', width), width, lm, rm));
        }
        addLine(makeLine(context.getString(R.string.total_purchase) + ":", order.getAmount(true).toString(), width, lm, rm));
        addLine(makeLine("", rightLineOfCharOfSize('-', priceExtraSpace), width, lm, rm));
    }

    protected abstract void handleHeaderPrint();

    protected abstract void handleExtraBottomPrint();

    protected abstract void printExtraTitle();

    protected abstract void handleOrderLinesPrint();

    protected abstract void handlePaymentPrint();

    protected abstract void handleChangePrint();

    protected abstract void handleVatPrint(List<OrderLine> list);

    protected void handleMainBodyPrint() {
        printExtraTitle();
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

    protected void printLargeText() {
        addLine(new byte[]{0x1b, 0x21, 0x10});
    }

    protected void printNormalText() {
        addLine(new byte[]{0x1b, 0x21, 0x00});
    }

    protected void setLargeText() {
        addLine("\u001b|4C");
    }

    protected void setNormalSizeText() {
        addLine("\u001b|N\r\n");
    }
    
    protected void printEmptyLine() {
        addLine("\r\n");
    }

    protected void printRightLineOfLength(char c, int length) {
        addLine(makeLine("", rightLineOfCharOfSize(c, length), width, lm, rm));
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    String text = "1G120310212";

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


