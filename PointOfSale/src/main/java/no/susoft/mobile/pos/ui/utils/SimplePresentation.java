package no.susoft.mobile.pos.ui.utils;

import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.activity.util.SecondaryDisplay;

public class SimplePresentation extends Presentation {

    protected TextView totalQty;
    protected TextView totalSum;
    protected TextView title;
    protected LinearLayout cartFooter;
    protected LinearLayout cartHeader;
    protected ListView orderLines;
    Context context;

    public SimplePresentation(Context context, Display display) {
        super(context, display);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (context.equals(MainActivity.getInstance())) {

            setContentView(R.layout.presentation_cart_fragment);

            try {
                //THESE HAVE TO BE IN HERE AND NOT A SEPARATE METHOD ELSE IT WONT FIND THEM
                totalQty = (TextView) findViewById(R.id.pres_tvTotalQuantity);
                totalSum = (TextView) findViewById(R.id.pres_tvTotalSum);
                title = (TextView) findViewById(R.id.pres_title);
                cartFooter = (LinearLayout) findViewById(R.id.pres_cartFooter);
                cartHeader = (LinearLayout) findViewById(R.id.pres_cartHeader);
                orderLines = (ListView) findViewById(R.id.pres_lvOrderLines);

                orderLines.setAdapter(MainActivity.getInstance().getCartFragment().adapter);

                SecondaryDisplay.getInstance().secondScreenIsSetUp(true);
                refreshSecondaryDisplayCart(false);

            } catch (Exception ex) {
            }

        } else {
            setContentView(R.layout.welcome_screen);
        }
    }

    public void refreshSecondaryDisplayCart(Boolean show) {
        new SimplePresentationUtils(show);
    }

    public void setSecondaryDisplaySelection(int i) {
        if (SecondaryDisplay.getInstance().secondScreenIsSetUp()) {
            getOrderLines().setSelection(i);
        }
    }

    public TextView getTotalQty() {
        return totalQty;
    }

    public TextView getTotalSum() {
        return totalSum;
    }

    public TextView getTitle() {
        return title;
    }

    public LinearLayout getCartFooter() {
        return cartFooter;
    }

    public LinearLayout getCartHeader() {
        return cartHeader;
    }

    public ListView getOrderLines() {
        return orderLines;
    }

}

