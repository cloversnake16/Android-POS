package no.susoft.mobile.pos.ui.activity.util;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.media.MediaRouter;
import android.util.Log;
import android.view.Display;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.utils.SimplePresentation;
import no.susoft.mobile.pos.ui.utils.SimplePresentationUtils;

public class SecondaryDisplay {

    //For secondary display
    MediaRouter router = null;
    SimplePresentation preso = null;
    MediaRouter.SimpleCallback routeCallback = null;
    private SimplePresentationUtils simplePresentationUtils;
    private boolean secondScreenIsSetUp = false;
    private static SecondaryDisplay INSTANCE;

    public static SecondaryDisplay getInstance() {
        return INSTANCE;
    }

    public SecondaryDisplay() {
        INSTANCE = this;
    }

    public void initializeSecondaryDisplay(Context context) {

        if (routeCallback == null) {
            routeCallback = new RouteCallback();
            router = (MediaRouter) context.getSystemService(context.getApplicationContext().MEDIA_ROUTER_SERVICE);
        }
        handleRoute(router.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO), context);
        router.addCallback(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, routeCallback);
    }

    public boolean secondScreenIsSetUp() {
        return secondScreenIsSetUp;
    }

    public void secondScreenIsSetUp(boolean b) {
        secondScreenIsSetUp = b;
    }

    private class RouteCallback extends MediaRouter.SimpleCallback {

        @Override
        public void onRoutePresentationDisplayChanged(MediaRouter router, MediaRouter.RouteInfo route) {
            handleRoute(route, MainActivity.getInstance());
        }
    }

    public void refreshSecondaryDisplayCart(Boolean show) {
        simplePresentationUtils = new SimplePresentationUtils(show);

    }

    public void handleRoute(MediaRouter.RouteInfo route, Context context) {
        if (route == null) {
            clearPreso();
        } else {
            Display display = route.getPresentationDisplay();

            if (route.isEnabled() && display != null) {
                if (preso == null) {
                    showPreso(route, context);
                } else if (preso.getDisplay().getDisplayId() != display.getDisplayId()) {
                    clearPreso();
                    showPreso(route, context);
                }

                Log.i("vilde", "display: " + display.getName());
                printDisplays();
            } else {
                clearPreso();
            }
        }
    }

    private void printDisplays() {
        DisplayManager dm = (DisplayManager) MainActivity.getInstance().getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = dm.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
        for (Display display : displays) {
            Log.i("vilde", "displays: " + display.getName());
        }

    }

    public SimplePresentation getSimplePresentation() {
        return preso;
    }

    public void removeSecondaryDisplayPresentation() {
        clearPreso();
        if (router != null) {
            router.removeCallback(routeCallback);
        }
    }

    public void setSimplePresentation(SimplePresentation simplePresentation) {
        this.preso = simplePresentation;
    }

    public void showPreso(MediaRouter.RouteInfo route, Context context) {
        if (preso == null) {
            setSimplePresentation(new SimplePresentation(context, route.getPresentationDisplay()));
        }
        preso.show();
    }

    public void clearPreso() {
        if (preso != null) {
            preso.dismiss();
            setSimplePresentation(null);
        }
    }
}
