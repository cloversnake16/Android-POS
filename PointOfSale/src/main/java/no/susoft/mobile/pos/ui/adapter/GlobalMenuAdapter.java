package no.susoft.mobile.pos.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import no.susoft.mobile.pos.R;

import java.util.ArrayList;
import java.util.List;

public class GlobalMenuAdapter extends ArrayAdapter<GlobalMenuAdapter.GlobalMenuItem> {

    private static final int TYPE_MENU_ITEM = 0;
    private static final int TYPE_DIVIDER = 1;

    private final LayoutInflater inflater;
    private final List<GlobalMenuItem> menuItems = new ArrayList<>();

    public GlobalMenuAdapter(Context context) {
        super(context, 0);
        this.inflater = LayoutInflater.from(context);
        setupMenuItems();
    }

    private void setupMenuItems() {
        menuItems.add(new GlobalMenuItem(R.drawable.ic_qlm_menu, "Transactions"));
        menuItems.add(new GlobalMenuItem(R.drawable.ic_qlm_menu, "Reports"));
        menuItems.add(new GlobalMenuItem(R.drawable.ic_navigation_menu, "Table Arrangement"));
        menuItems.add(GlobalMenuItem.dividerMenuItem());
        menuItems.add(new GlobalMenuItem(0, "Settings"));
        menuItems.add(new GlobalMenuItem(0, "About"));
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return menuItems.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public GlobalMenuItem getItem(int position) {
        return menuItems.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        return menuItems.get(position).isDivider ? TYPE_DIVIDER : TYPE_MENU_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (getItemViewType(position) == TYPE_MENU_ITEM) {
            MenuItemViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.global_menu_item, parent, false);
                holder = new MenuItemViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (MenuItemViewHolder) convertView.getTag();
            }

            GlobalMenuItem item = getItem(position);
            holder.tvLabel.setText(item.label);
            holder.ivIcon.setImageResource(item.iconResId);
            holder.ivIcon.setVisibility(item.iconResId == 0 ? View.GONE : View.VISIBLE);

            return convertView;
        } else {
            return inflater.inflate(R.layout.global_menu_item_divider, parent, false);
        }
    }

    @Override
    public boolean isEnabled(int position) {
        return !menuItems.get(position).isDivider;
    }

    public static class MenuItemViewHolder {

        @InjectView(R.id.ivIcon)
        ImageView ivIcon;
        @InjectView(R.id.tvLabel)
        TextView tvLabel;

        public MenuItemViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    public static class GlobalMenuItem {

        public int iconResId;
        public String label;
        public boolean isDivider;

        private GlobalMenuItem() {

        }

        public GlobalMenuItem(int iconResId, String label) {
            this.iconResId = iconResId;
            this.label = label;
            this.isDivider = false;
        }

        public static GlobalMenuItem dividerMenuItem() {
            GlobalMenuItem globalMenuItem = new GlobalMenuItem();
            globalMenuItem.isDivider = true;
            return globalMenuItem;
        }
    }
}