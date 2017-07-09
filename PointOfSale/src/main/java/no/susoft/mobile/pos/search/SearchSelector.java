package no.susoft.mobile.pos.search;

import java.util.ArrayList;
import java.util.List;

/**
 * This class facilitates the maintaining selection states of a searchable.
 *
 * @author Yesod
 */
public final class SearchSelector {

    private static final long serialVersionUID = 4625383707831751606L;
    // The search selector selection list.
    private final List<Searchable> selection;
    // The search selector limit.
    private final boolean single;

    /**
     * Build an empty selector.
     */
    public SearchSelector(boolean single) {
        this.selection = new ArrayList<Searchable>();
        this.single = single;
    }

    /**
     * Get the currently selected entries.
     *
     * @return
     */
    public List<Searchable> getSelected() {
        return this.selection;
    }

    /**
     * Return how many entries are selected.
     *
     * @return
     */
    public int getSelectionCount() {
        return this.selection.size();
    }

    /**
     * Set whether the 'searchable' is to be added or removed from the selection list.
     *
     * @param searchable
     * @param selected
     */
    public void set(Searchable searchable, boolean selected) {
        if (selected) {
            this.selection.add(searchable);
        } else {
            this.selection.remove(searchable);
        }
    }

    /**
     * Toggle the selection state for product.
     *
     * @param searchable
     */
    public void toggle(Searchable searchable) {
        if (this.single)
            this.selection.clear();
        this.set(searchable, !this.isSelected(searchable));
    }

    /**
     * Return whether 'searchable' is contained in the selection list.
     */
    public boolean isSelected(Searchable searchable) {
        return this.selection.contains(searchable);
    }

    /**
     * Return whether there are currently selected products.
     *
     * @return
     */
    public boolean hasSelection() {
        return this.getSelectionCount() > 0;
    }

}