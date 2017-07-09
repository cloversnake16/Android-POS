package no.susoft.mobile.pos.hardware.combi;

import android.content.Context;

import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.stario.StarPrinterStatus;
import no.susoft.mobile.pos.error.ErrorReporter;

@SuppressWarnings({"UnusedParameters", "UnusedAssignment"})
public class Communication {
    public enum Result {
        Success,
        ErrorUnknown,
        ErrorOpenPort,
        ErrorBeginCheckedBlock,
        ErrorEndCheckedBlock,
        ErrorWritePort,
        ErrorReadPort,
    }

    public static Result sendCommands(byte[] commands, StarIOPort port, Context context) {
        Result result = Result.ErrorUnknown;

        try {
            if (port == null) {
                result = Result.ErrorOpenPort;
                return result;
            }

//          // When using an USB interface, you may need to send the following data.
//          byte[] dummy = {0x00};
//          port.writePort(dummy, 0, dummy.length);

            StarPrinterStatus status;

//			ErrorReporter.INSTANCE.filelog("mPOP retreiveStatus started..");

//			result = Result.ErrorBeginCheckedBlock;
//			status = port.retreiveStatus();

//			ErrorReporter.INSTANCE.filelog("mPOP retreiveStatus end");
//			System.out.println("status = " + status);
//			System.out.println("status.etbAvailable = " + status.etbAvailable);

//			status = port.beginCheckedBlock();
//			if (status.offline) {
//				throw new StarIOPortException("A printer is offline");
//			}

			ErrorReporter.INSTANCE.filelog("mPOP writePort started..");

            result = Result.ErrorWritePort;
            port.writePort(commands, 0, commands.length);

			ErrorReporter.INSTANCE.filelog("mPOP writePort end");

//            result = Result.ErrorEndCheckedBlock;
//            port.setEndCheckedBlockTimeoutMillis(30000);     // 30000mS!!!
//            status = port.endCheckedBlock();

//            if (status.coverOpen) {
//                throw new StarIOPortException("Printer cover is open");
//            }
//            else if (status.receiptPaperEmpty) {
//                throw new StarIOPortException("Receipt paper is empty");
//            }
//            else if (status.offline) {
//                throw new StarIOPortException("Printer is offline");
//            }

            result = Result.Success;
        }
        catch (StarIOPortException e) {
			ErrorReporter.INSTANCE.filelog("Communication.sendCommands()", "Error", e);
            // Nothing
        }

        return result;
    }

    public static Result sendCommandsDoNotCheckCondition(byte[] commands, StarIOPort port, Context context) {
        Result result = Result.ErrorUnknown;

        try {
            if (port == null) {
                result = Result.ErrorOpenPort;
                return result;
            }

//          // When using an USB interface, you may need to send the following data.
//          byte[] dummy = {0x00};
//          port.writePort(dummy, 0, dummy.length);

            StarPrinterStatus status;

            result = Result.ErrorWritePort;
            status = port.retreiveStatus();

            if (status.rawLength == 0) {
                throw new StarIOPortException("A printer is offline");
            }

            result = Result.ErrorWritePort;
            port.writePort(commands, 0, commands.length);

            result = Result.ErrorWritePort;
            status = port.retreiveStatus();

            if (status.rawLength == 0) {
                throw new StarIOPortException("A printer is offline");
            }

            result = Result.Success;
        }
        catch (StarIOPortException e) {
            // Nothing
        }

        return result;
    }

    public static Result sendCommands(byte[] commands, String portName, String portSettings, int timeout, Context context) {
        Result result = Result.ErrorUnknown;

        StarIOPort port = null;

        try {
            result = Result.ErrorOpenPort;

            port = StarIOPort.getPort(portName, portSettings, timeout, context);

//          // When using an USB interface, you may need to send the following data.
//          byte[] dummy = {0x00};
//          port.writePort(dummy, 0, dummy.length);

            StarPrinterStatus status;

            result = Result.ErrorBeginCheckedBlock;

            status = port.beginCheckedBlock();

            if (status.offline) {
                throw new StarIOPortException("A printer is offline");
            }

            result = Result.ErrorWritePort;

            port.writePort(commands, 0, commands.length);

            result = Result.ErrorEndCheckedBlock;

            port.setEndCheckedBlockTimeoutMillis(30000);     // 30000mS!!!

            status = port.endCheckedBlock();

            if (status.coverOpen) {
                throw new StarIOPortException("Printer cover is open");
            }
            else if (status.receiptPaperEmpty) {
                throw new StarIOPortException("Receipt paper is empty");
            }
            else if (status.offline) {
                throw new StarIOPortException("Printer is offline");
            }

            result = Result.Success;
        }
        catch (StarIOPortException e) {
            // Nothing
        }
        finally {
            if (port != null) {
                try {
                    StarIOPort.releasePort(port);

                    port = null;
                }
                catch (StarIOPortException e) {
                    // Nothing
                }
            }
        }

        return result;
    }

    public static Result sendCommandsDoNotCheckCondition(byte[] commands, String portName, String portSettings, int timeout, Context context) {
        Result result = Result.ErrorUnknown;

        StarIOPort port = null;

        try {
            result = Result.ErrorOpenPort;

            port = StarIOPort.getPort(portName, portSettings, timeout, context);

//          // When using an USB interface, you may need to send the following data.
//          byte[] dummy = {0x00};
//          port.writePort(dummy, 0, dummy.length);

            StarPrinterStatus status;

            result = Result.ErrorWritePort;
            status = port.retreiveStatus();

            if (status.rawLength == 0) {
                throw new StarIOPortException("A printer is offline");
            }

            result = Result.ErrorWritePort;
            port.writePort(commands, 0, commands.length);

            result = Result.ErrorWritePort;
            status = port.retreiveStatus();

            if (status.rawLength == 0) {
                throw new StarIOPortException("A printer is offline");
            }

            result = Result.Success;
        }
        catch (StarIOPortException e) {
            // Nothing
        }
        finally {
            if (port != null) {
                try {
                    StarIOPort.releasePort(port);

                    port = null;
                }
                catch (StarIOPortException e) {
                    // Nothing
                }
            }
        }

        return result;
    }

}
