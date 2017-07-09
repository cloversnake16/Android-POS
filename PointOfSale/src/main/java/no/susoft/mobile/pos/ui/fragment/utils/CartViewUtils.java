package no.susoft.mobile.pos.ui.fragment.utils;

import android.graphics.Color;
import android.view.View;
import android.widget.ListView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.ui.activity.MainActivity;

/**
 * Created by Vilde on 24.01.2016.
 */
public class CartViewUtils {

    public void highlightLineOnResume(ListView list, int selectedLine) {
        if (list.getChildCount() > selectedLine) {
            View view = list.getChildAt(selectedLine);
            doHighlightOnThisRowOnly(view, list);
        } else if (list.getChildCount() > 0){
            highlightLastRow(list);
        }
    }

    private void highlightLastRow(ListView list) {
        doHighlightOnThisRowOnly(list.getChildAt(list.getChildCount()-1), list);
    }

    public void doHighlightOnThisRowOnly(View row, ListView list) {
        if (row != null && list != null) {
            View view;
			for(int i = 0; i < list.getChildCount(); i++) {
                view = list.getChildAt(i);
				updateState(view, view.equals(row), i);
            }
        }
    }

    private void updateState(View view, boolean shouldBeChecked, int i) {
        if (view != null) {
            boolean hasNote = Cart.INSTANCE.getOrder().getLines().get(i).hasNote();
            if(shouldBeChecked) {
                view.setBackgroundColor(MainActivity.getInstance().getResources().getColor(R.color.et_orange));
            } else if (hasNote){
                view.setBackgroundColor(MainActivity.getInstance().getResources().getColor(R.color.light_orange));
            } else {
                view.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }

}
