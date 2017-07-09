package no.susoft.mobile.pos.ui.adapter.utils;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.Utilities;
import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.data.OrderLine;
import no.susoft.mobile.pos.data.Product;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.adapter.ProductListAdapter;
import no.susoft.mobile.pos.ui.dialog.OrderLineEditBundleDialog;
import no.susoft.mobile.pos.ui.dialog.OrderLineEditMiscDialog;
import no.susoft.mobile.pos.ui.fragment.ProductSearchFragment;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

public class UpdateProductSearchAdapter {

    ListView productList;
    LinearLayout listHeader;
    Button searchButton;
    ProgressBar progressBar;
    TextView noMatchText;
    ProductSearchFragment productSearchFragment;
    EditText searchTermBox;

    public UpdateProductSearchAdapter(ArrayList<Product> products, ProductSearchFragment productSearchFragment) {

        this.productSearchFragment = productSearchFragment;

        setViews();

        if (products != null && !products.isEmpty()) {

            foundNoMatches(products);

        } else {
            foundNoMatches();
        }

        hideProductSearchProgressBar();
        showProductSearchButton();
    }

    private void setViews() {
        this.productList = productSearchFragment.getProductList();
        this.listHeader = productSearchFragment.getProductListHeader();
        this.searchButton = productSearchFragment.getSearchButton();
        this.progressBar = productSearchFragment.getProgressBar();
        this.noMatchText = productSearchFragment.getNoMatchTextView();
        this.searchTermBox = productSearchFragment.getSearchText();

    }

    private void foundNoMatches(ArrayList<Product> products) {
        showFoundSearchMatchesViews();
        hideNoSearchMatchesViews();
        setupNewAdapter(products);
        clearFocus();
        setListClickListener();
        hideKeyboard();
    }

    private void foundNoMatches() {
        hideFoundSearchMatchesViews();
        showNoSearchMatchesViews();
        selectAllInSearchEditText();
    }

    private void showNoSearchMatchesViews() {
        String searchTerm = searchTermBox.getText().toString();
        noMatchText.setText(MainActivity.getInstance().getString(R.string.foundNoMatchesFor) + " \"" + searchTerm + "\".");
        noMatchText.setVisibility(View.VISIBLE);
    }

    private void hideNoSearchMatchesViews() {
        noMatchText.setVisibility(View.GONE);
    }

    private void hideFoundSearchMatchesViews() {
        productList.setVisibility(View.GONE);
        listHeader.setVisibility(View.GONE);
    }

    private void showFoundSearchMatchesViews() {
        productList.setVisibility(View.VISIBLE);
        listHeader.setVisibility(View.VISIBLE);
    }

    private void setupNewAdapter(ArrayList<Product> products) {
        ProductListAdapter adapter = new ProductListAdapter(MainActivity.getInstance(), 0, products);
        productList.setAdapter(adapter);
    }

    private void clearFocus() {
        View current = MainActivity.getInstance().getCurrentFocus();
        if (current != null) {
            current.clearFocus();
        }
    }

    private void selectAllInSearchEditText() {
        searchTermBox.requestFocus();
        searchTermBox.selectAll();
    }

    private void setListClickListener() {
        productList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                adapterView.setSelection(i);
                Product p = (Product) adapterView.getItemAtPosition(i);
                searchAddProductToOrder(p);
            }
        });
    }

    private void searchAddProductToOrder(Product p) {
		if (p != null && MainActivity.getInstance().getCartFragment() != null && MainActivity.getInstance().getNumpadScanFragment() != null) {
			OrderLine ol;
			if (p.isMiscellaneous()) {
				p = new Product(p);
				p.setPrice(Decimal.ZERO);
				ol = Cart.INSTANCE.addOrderLineFromDialog(p, Decimal.ONE);
			} else {
				ol = Cart.INSTANCE.addOrderLine(p);
			}
			if (p.isMiscellaneous()) {
				Utilities.showSoftKeyboard(MainActivity.getInstance());
				OrderLineEditMiscDialog dialog = new OrderLineEditMiscDialog();
				dialog.setOrderLine(ol);
				dialog.show(MainActivity.getInstance().getFragmentManager(), "misc");
			} else if (p.isBundle() && p.getComponents() != null) {
				OrderLineEditBundleDialog dialog = new OrderLineEditBundleDialog();
				dialog.setOrderLine(ol);
				dialog.show(MainActivity.getInstance().getFragmentManager(), "bundle");
			}
		}
	}

    private void hideKeyboard() {
        View view = MainActivity.getInstance().getProductSearchFragment().getSearchText();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) MainActivity.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void showProductSearchButton() {
        searchButton.setVisibility(View.VISIBLE);
    }

    public void hideProductSearchProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

}
