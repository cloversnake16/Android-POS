package no.susoft.mobile.pos.ui.utils;

import android.view.View;
import no.susoft.mobile.pos.ui.activity.util.SecondaryDisplay;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

public class SimplePresentationUtils {

    SecondaryDisplay sd;
    SimplePresentation preso;

    public SimplePresentationUtils(Boolean show) {
        updateReferences();
        refreshSecondaryDisplayCart(show);
    }

    private void updateReferences() {
        sd = SecondaryDisplay.getInstance();
        preso = sd.getSimplePresentation();
    }

    public void refreshSecondaryDisplayCart(Boolean show) {
        if (sd.secondScreenIsSetUp()) {
            if (show) {
                refreshPresentationCartTextFields();
                showPresentationCartDetails();
            } else {
                hidePresentationCartDetails();
            }
        }
    }

    private void refreshPresentationCartTextFields() {
        preso.getTotalQty().setText(String.valueOf(Cart.INSTANCE.getOrder().getLines().size()));
        preso.getTotalSum().setText(String.valueOf(Cart.INSTANCE.getOrder().getAmount(true)));
    }

    private void showPresentationCartDetails() {
        preso.getCartFooter().setVisibility(View.VISIBLE);
        preso.getCartHeader().setVisibility(View.VISIBLE);
        preso.getOrderLines().setVisibility(View.VISIBLE);
    }

    private void hidePresentationCartDetails() {
        preso.getCartFooter().setVisibility(View.GONE);
        preso.getCartHeader().setVisibility(View.GONE);
        preso.getOrderLines().setVisibility(View.GONE);
    }

}
