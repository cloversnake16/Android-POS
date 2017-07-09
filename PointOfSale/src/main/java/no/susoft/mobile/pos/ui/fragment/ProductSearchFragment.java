package no.susoft.mobile.pos.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.*;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.activity.util.MainTopBarMenu;

public class ProductSearchFragment extends Fragment {

    @InjectView(R.id.product_search_progress_bar)
    ProgressBar progressBar;
    @InjectView(R.id.product_search_text)
    EditText searchText;
    @InjectView(R.id.product_search_button)
    Button searchButton;
    @InjectView(R.id.product_search_list)
    ListView productList;
    @InjectView(R.id.product_search_list_header)
    LinearLayout productListHeader;
    @InjectView(R.id.product_search_no_match)
    TextView product_search_no_match_textview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.getInstance().setProductSearchFragment(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.product_search_fragment, container, false);
        ButterKnife.inject(this, rootView);

        productListHeader.setVisibility(View.GONE);
        searchButton.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchButton.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                MainActivity.getInstance().getServerCallMethods().searchProduct(searchText.getText().toString().trim());
            }
        });

        searchText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            searchButton.callOnClick();
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        final GestureDetector gesture = new GestureDetector(getActivity(),
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true;
                    }

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                           float velocityY) {
                        final int SWIPE_MIN_DISTANCE = 120;
                        final int SWIPE_MAX_OFF_PATH = 250;
                        final int SWIPE_THRESHOLD_VELOCITY = 200;
                        try {
                            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                                return false;
                            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                                // Log.i(Constants.APP_TAG, "Right to Left");
                                getSwipedFragment("right");
                            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                                //Log.i(Constants.APP_TAG, "Left to Right");
                                getSwipedFragment("left");
                            }
                        } catch (Exception e) {
                            // nothing
                        }
                        return super.onFling(e1, e2, velocityX, velocityY);
                    }
                });

        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gesture.onTouchEvent(event);
            }

        });

        return rootView;
    }

    private void getSwipedFragment(String side) {
        if (side.equalsIgnoreCase("left")) {
            MainTopBarMenu.getInstance().toggleBrowseView();
        } else if (side.equalsIgnoreCase("right")) {
            MainTopBarMenu.getInstance().toggleEditView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        searchText.setText("");
        searchText.requestFocus();
        searchText.requestFocusFromTouch();
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public EditText getSearchText() {
        return searchText;
    }

    public Button getSearchButton() {
        return searchButton;
    }

    public ListView getProductList() {
        return productList;
    }

    public LinearLayout getProductListHeader() {
        return productListHeader;
    }

    public TextView getNoMatchTextView() {
        return product_search_no_match_textview;
    }

    public void doEnterClick() {
        searchButton.callOnClick();
    }

    public void setInputFieldToString(String text) {
        searchText.requestFocus();
        searchText.setText(text);
    }
}
