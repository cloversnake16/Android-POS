package no.susoft.mobile.pos.ui.dialog;

import java.util.Calendar;
import java.util.Date;

import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class _DialogUtils {

    protected static java.util.Date getDateFromDatePicker(DatePicker datePicker){
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year =  datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        return calendar.getTime();
    }

    protected static void setVisibility(int state, View... views) {
        for(View v : views) {
            v.setVisibility(state);
        }
    }

    protected static String getDateErrorMessage(Date from, Date to) {
        try {
            if (to.before(from)) {
                return MainActivity.getInstance().getString(R.string.found_no_match_for_dates) + " " + MainActivity.getInstance().getString(R.string.hint) + ": " + MainActivity.getInstance().getString(R.string.to_date_earlier_than_from_date);
            } else if (new Date().before(from)) {
                return MainActivity.getInstance().getString(R.string.found_no_match_for_dates) + " " + MainActivity.getInstance().getString(R.string.hint) + ": " + MainActivity.getInstance().getString(R.string.from_date_is_in_future);
            } else {
                return MainActivity.getInstance().getString(R.string.found_no_match_for_dates);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return MainActivity.getInstance().getString(R.string.found_no_match_for_dates);
        }
    }

    protected static boolean datesValid(Date fromDate, Date toDate) {
        if (toDate.before(fromDate) || new Date().before(fromDate)) {
            String message = getDateErrorMessage(fromDate, toDate);
            if (message != null) {
                Toast.makeText(MainActivity.getInstance(), message, Toast.LENGTH_LONG).show();
            }
            return false;
        } else {
            return true;
        }
    }

}
