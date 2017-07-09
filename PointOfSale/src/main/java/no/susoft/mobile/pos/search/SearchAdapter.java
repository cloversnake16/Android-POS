package no.susoft.mobile.pos.search;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * @author Yesod
 */
public abstract class SearchAdapter extends BaseAdapter {

    // The searchable accountAdapter source;
    private SearchableAdapterSource source;

    /**
     * @author Yesod
     */
    public interface SearchableAdapterSource {

        /**
         * Get the item count.
         *
         * @return
         */
        public int getCount();

        /**
         * Get the item at 'position'.
         *
         * @param position
         * @return
         */
        public Searchable getItem(int position);
    }

    /**
     * Set the searchable accountAdapter source.
     *
     * @param source
     */
    public void setSearchableAdapterSource(SearchableAdapterSource source) {
        this.source = source;
    }

    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        if (this.source != null)
            return this.source.getCount();
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public Object getItem(int position) {
        if (this.source != null)
            return this.source.getItem(position);
        return null;
    }

    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View row, ViewGroup parent) {
        throw new UnsupportedOperationException("BaseAdapter:getView has not been implemented for this Searchable type.");
    }
}