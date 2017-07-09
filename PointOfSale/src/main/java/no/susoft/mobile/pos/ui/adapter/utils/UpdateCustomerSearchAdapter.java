package no.susoft.mobile.pos.ui.adapter.utils;

import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Customer;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.adapter.CustomerListAdapter;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

import java.util.ArrayList;

public class UpdateCustomerSearchAdapter {

    ListView customerList;
    LinearLayout listHeader;
    DialogFragment customerSearchDialog;
    Button searchButton;
    ProgressBar progressBar;
    TextView noMatchText;
    EditText searchTermBox;

    public UpdateCustomerSearchAdapter(ArrayList<Customer> customers, DialogFragment customerSearchDialog) {

        this.customerSearchDialog = customerSearchDialog;

        findViews();

        if (customers != null && !customers.isEmpty()) {
            foundSearchMatches(customers);

        } else {
            foundNoMatches();
        }

        hideCustomerSearchProgressBar();
        showCustomerSearchSearchButton();

    }

    private void findViews() {
        customerList = (ListView) customerSearchDialog.getDialog().findViewById(R.id.customer_list);
        listHeader = (LinearLayout) customerSearchDialog.getDialog().findViewById(R.id.customer_list_header);
        searchButton = (Button) customerSearchDialog.getDialog().findViewById(R.id.customer_search_button);
        progressBar = (ProgressBar) customerSearchDialog.getDialog().findViewById(R.id.progressBarCustomerSearch);
        noMatchText = (TextView) customerSearchDialog.getDialog().getWindow().findViewById(R.id.customer_search_no_match);
        searchTermBox = (EditText) customerSearchDialog.getDialog().getWindow().findViewById(R.id.customer_search_text);
    }

    private void foundNoMatches() {
        hideSearchResultViews();
        showNoMatchesViews();
        selectAllInSearchEditText();
    }

    private void foundSearchMatches(ArrayList<Customer> customers) {
        showFoundSearchMatchesViews();
        hideNoMatchesViews();

        setupNewAdapter(customers);
        setListViewOnItemClickListener();
        hideKeyboard();
    }

    private void showFoundSearchMatchesViews() {
        customerList.setVisibility(View.VISIBLE);
        listHeader.setVisibility(View.VISIBLE);

    }

    private void hideNoMatchesViews() {
        noMatchText.setVisibility(View.GONE);
    }

    private void setupNewAdapter(ArrayList<Customer> customers) {
        CustomerListAdapter adapter = new CustomerListAdapter(MainActivity.getInstance(), 0, customers);
        customerList.setAdapter(adapter);
    }

    private void setListViewOnItemClickListener() {
        customerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Customer c = (Customer) adapterView.getItemAtPosition(i);
                if (c != null && MainActivity.getInstance().getCartFragment() != null && MainActivity.getInstance().getNumpadScanFragment() != null) {
                    Cart.INSTANCE.setOrderCustomer(c);
                    customerSearchDialog.dismiss();
                }
            }
        });
    }

    private void hideKeyboard() {
        View view = customerSearchDialog.getDialog().findViewById(R.id.customer_search_text);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) MainActivity.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showNoMatchesViews() {
        String searchTerm = searchTermBox.getText().toString();
        noMatchText.setText(MainActivity.getInstance().getString(R.string.foundNoMatchesFor) + " \"" + searchTerm + "\".");
        noMatchText.setVisibility(View.VISIBLE);
    }

    private void selectAllInSearchEditText() {
        searchTermBox.requestFocus();
        searchTermBox.selectAll();
    }

    private void hideSearchResultViews() {
        customerList.setVisibility(View.GONE);
        listHeader.setVisibility(View.GONE);
    }

    public void showCustomerSearchSearchButton() {
        if (searchButton != null)
            searchButton.setVisibility(View.VISIBLE);
    }

    public void hideCustomerSearchProgressBar() {
        if (progressBar != null)
            progressBar.setVisibility(View.GONE);
    }

}
