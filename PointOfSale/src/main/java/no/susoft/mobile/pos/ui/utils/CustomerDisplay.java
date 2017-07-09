package no.susoft.mobile.pos.ui.utils;

import android.app.AlertDialog;
import android.content.*;
import android.view.View;
import jp.co.casio.vx.framework.device.LineDisplay;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.data.OrderLine;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class CustomerDisplay {

    IntentFilter intentFilter = new IntentFilter("jp.co.casio.vx.regdevice.comm.POWER_RECOVERY");
    private BroadcastReceiver intentReceiver;
    int boardNo = 0;
    Context context;

    private LineDisplay display = new LineDisplay();
    private String dispData1;
    private String dispData2;
    private String dispData3;

    public String getDispData1() {
        return dispData1;
    }

    public void setDispData1(String dispData1) {
        this.dispData1 = dispData1;
    }

    public String getDispData2() {
        return dispData2;
    }

    public void setDispData2(String dispData2) {
        this.dispData2 = dispData2;
    }

    public String getDispData3() {
        return dispData3;
    }

    public void setDispData3(String dispData3) {
        this.dispData3 = dispData3;
    }

    public void setDispDatas(String one, String two, String three) {
        setDispData1(one);
        setDispData2(two);
        setDispData3(three);
    }

    public CustomerDisplay() {
        context = MainActivity.getInstance();
        display = new LineDisplay();
        dispData1 = context.getString(R.string.shopmessage);
        dispData2 = context.getString(R.string.total);
        dispData3 = "5680";
        displayText(display, dispData1, dispData2, dispData3, LineDisplay.LCD_SLEEP_ON);
    }

    public void displayOrderLine(OrderLine ol) {
        if (display != null) {
            setDispDatas(ol.getProduct().getName(), context.getString(R.string.total), ol.getAmount(true).toString());
            displayText(display, dispData1, dispData2, dispData3, LineDisplay.LCD_SLEEP_ON);
        }
    }

    public void displayOrderTotal(Order o) {
        if (display != null) {
            setDispDatas(context.getString(R.string.quantity) + ": " + o.getLines().size(), context.getString(R.string.total), o.getAmount(true).toString());
            displayText(display, dispData1, dispData2, dispData3, LineDisplay.LCD_SLEEP_ON);
        }
    }

    public void onResume() {
        if (display != null) {
            displayText(display, dispData1, dispData2, dispData3, LineDisplay.LCD_SLEEP_ON);
        }
    }

    public void onPause() {
        if (display != null) {
            displayText(display, dispData1, dispData2, dispData3, LineDisplay.LCD_SLEEP_OFF);
        }
    }

    public void onDestroy() {
        context.unregisterReceiver(intentReceiver);
    }

    public void onClick(View view) {
        displaySample();
    }

    private String errMessage(int errno) {
        String text = "";
        switch (errno) {
            case LineDisplay.Response.ERR_PARAMETER:
                text = context.getString(R.string.error_parameter);
                break;
            case LineDisplay.Response.ERR_NOTOPEN:
                text = context.getString(R.string.error_unopened);
                break;
            case LineDisplay.Response.ERR_BOARD_NOTRUNNING:
                text = context.getString(R.string.error_boardnotrun);
                break;
            case LineDisplay.Response.ERR_SERVICE_CONNECTION:
                text = context.getString(R.string.error_serviceconnection);
                break;
            case LineDisplay.Response.ERR_OPENCONFLICT:
                text = context.getString(R.string.error_openconflict);
                break;
            case LineDisplay.Response.ERR_TIMEOUT:
                text = context.getString(R.string.error_timeout);
                break;
            case LineDisplay.Response.ERR_BOARD_CONNECTION:
                text = context.getString(R.string.error_boadconnection);
                break;
            case LineDisplay.Response.ERR_POWER_FAILURE:
                text = context.getString(R.string.error_powerdown);
                break;
            default:
                text = context.getString(R.string.error_generic);
                break;
        }
        return text;
    }

    protected void displaySample() {
        display = new LineDisplay();
        receiveIntent(display, dispData1, dispData2, dispData3);
        displayText(display, dispData1, dispData2, dispData3, LineDisplay.LCD_SLEEP_ON);
    }

    protected void displayText(final LineDisplay display, final String dispData1, final String dispData2, final String dispData3, int sleepmode) {
        String errTitle = "Line display sample";
        int ret = LineDisplay.Response.SUCCESS;

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(errTitle);
        alertDialogBuilder.setCancelable(false);

        ErrBlock:
        try {
            byte[] line1 = dispData1.getBytes("MS932");
            byte[] line2 = dispData2.getBytes("MS932");
            byte[] line3 = dispData3.getBytes("MS932");

            errTitle = "open";
            ret = display.open(LineDisplay.OPENMODE_COMMON, LineDisplay.DEVICE_HOST_LOCALHOST);
            if (ret == LineDisplay.Response.SUCCESS) {
                errTitle = "setText";

                switch (sleepmode) {
                    case LineDisplay.LCD_SLEEP_OFF:
                        ret = display.setSleep(LineDisplay.LCD_SLEEP_OFF);
                        if (ret != LineDisplay.Response.SUCCESS) {
                            errTitle = "setSleep";
                            display.close();
                            break ErrBlock;
                        }
                        return;
                    case LineDisplay.LCD_SLEEP_ON:
                        ret = display.setSleep(LineDisplay.LCD_SLEEP_ON);
                        if (ret != LineDisplay.Response.SUCCESS) {
                            errTitle = "setSleep";
                            display.close();
                            break ErrBlock;
                        }
                        break;
                }
                ret = display.setBacklight(LineDisplay.LCD_BACKLIGHT_GREEN);
                if (ret != LineDisplay.Response.SUCCESS) {
                    errTitle = "setBacklight";
                    display.close();
                    break ErrBlock;
                }

                ret = display.setText(line1, line2, line3, LineDisplay.LCD_CONTROL_SCROLL, LineDisplay.LCD_CONTROL_NORMAL);
                if (ret == LineDisplay.Response.SUCCESS) {
                    errTitle = "setText";
                    ret = display.close();
                    if (ret == LineDisplay.Response.SUCCESS) {
                        return;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            alertDialogBuilder.setMessage("Exception:" + e.toString());
            alertDialogBuilder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            alertDialogBuilder.show();
            return;
        }

        if (ret == LineDisplay.Response.ERR_POWER_FAILURE) {
            return;
        }

        // Error handling procedure
        alertDialogBuilder.setMessage(context.getString(R.string.errormsg_retry) + "(" + errMessage(ret) + ")");
        alertDialogBuilder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //reprint
                displayText(display, dispData1, dispData2, dispData3, LineDisplay.LCD_SLEEP_ON);
            }
        });
        alertDialogBuilder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialogBuilder.show();
    }

    private void receiveIntent(final LineDisplay display, final String dispData1, final String dispData2, final String dispData3) {
        intentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    Thread.sleep(1500);
                } catch (Exception e) {
                }

                if (display != null) {
                    displayText(display, dispData1, dispData2, dispData3, LineDisplay.LCD_SLEEP_ON);
                }
            }
        };
        context.registerReceiver(intentReceiver, intentFilter);
    }

}
