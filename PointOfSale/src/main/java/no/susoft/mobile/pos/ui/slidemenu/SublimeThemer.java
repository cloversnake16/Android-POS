package no.susoft.mobile.pos.ui.slidemenu;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.util.TypedValue;
import no.susoft.mobile.pos.R;

public class SublimeThemer {

    private static final String TAG = SublimeThemer.class.getSimpleName();
    private static final int[] EMPTY_STATE_SET = new int[]{};
    private static final int[] CHECKED_STATE_SET = new int[]{R.attr.state_item_checked};
    private static final int[] CHECKABLE_ITEM_CHECKED_STATE_SET = new int[]{android.R.attr.state_checked};
    private static final int[] DISABLED_STATE_SET = new int[]{-android.R.attr.state_enabled};

    public enum DefaultTheme {DARK, LIGHT}

    private Context mContext;
    private ColorStateList mIconTintList, mCheckableItemTintList;
    private Drawable mItemBackground, mDrawerBackground;
    private DefaultTheme mDefaultTheme;
    private float mElevation;
    private Drawable mGroupExpandDrawable, mGroupCollapseDrawable;

    private TextViewStyleProfile mItemStyleProfile, mItemHintStyleProfile,
            mSubheaderStyleProfile, mSubheaderHintStyleProfile, mBadgeStyleProfile;

    public SublimeThemer(@NonNull Context context) {
        this(context, DefaultTheme.LIGHT);
    }

    public SublimeThemer(@NonNull Context context, @NonNull DefaultTheme defaultTheme) {
        mContext = context;
        mDefaultTheme = defaultTheme;
    }

    public SublimeThemer setIconTintList(ColorStateList iconTintList) {
        mIconTintList = iconTintList;
        return this;
    }

    public SublimeThemer setCheckableItemTintList(ColorStateList checkableItemTintList) {
        mCheckableItemTintList = checkableItemTintList;
        return this;
    }

    public SublimeThemer setItemStyleProfile(TextViewStyleProfile itemStyleProfile) {
        mItemStyleProfile = itemStyleProfile;
        return this;
    }

    public SublimeThemer setItemHintStyleProfile(TextViewStyleProfile itemHintStyleProfile) {
        mItemHintStyleProfile = itemHintStyleProfile;
        return this;
    }

    public SublimeThemer setSubheaderStyleProfile(TextViewStyleProfile subheaderStyleProfile) {
        mSubheaderStyleProfile = subheaderStyleProfile;
        return this;
    }

    public SublimeThemer setSubheaderHintStyleProfile(TextViewStyleProfile subheaderHintStyleProfile) {
        mSubheaderHintStyleProfile = subheaderHintStyleProfile;
        return this;
    }

    public SublimeThemer setBadgeStyleProfile(TextViewStyleProfile badgeStyleProfile) {
        mBadgeStyleProfile = badgeStyleProfile;
        return this;
    }

    public SublimeThemer setGroupExpandDrawable(Drawable groupExpandDrawable) {
        mGroupExpandDrawable = groupExpandDrawable;
        return this;
    }

    public SublimeThemer setGroupCollapseDrawable(Drawable groupCollapseDrawable) {
        mGroupCollapseDrawable = groupCollapseDrawable;
        return this;
    }

    public SublimeThemer setItemBackground(Drawable itemBackground) {
        mItemBackground = itemBackground;
        return this;
    }

    public SublimeThemer setDrawerBackground(Drawable drawerBackground) {
        mDrawerBackground = drawerBackground;
        return this;
    }

    public SublimeThemer setElevation(float elevation) {
        mElevation = elevation;
        return this;
    }

    public ColorStateList getIconTintList() {
        if (mIconTintList == null) {
            setDefaultIconTintList();
        }

        return mIconTintList;
    }

    public ColorStateList getCheckableItemTintList() {
        if (mCheckableItemTintList == null) {
            setDefaultCheckableItemTintList();
        }

        return mCheckableItemTintList;
    }

    public Drawable getGroupExpandDrawable() {
        if (mGroupExpandDrawable == null) {
            mGroupExpandDrawable = ResourcesCompat.getDrawable(mContext.getResources(),
                    R.drawable.snv_expand, mContext.getTheme());
        }

        // Return a new drawable since this method will be
        // called multiple times
        return mGroupExpandDrawable.getConstantState().newDrawable();
    }

    public Drawable getGroupCollapseDrawable() {
        if (mGroupCollapseDrawable == null) {
            mGroupCollapseDrawable = ResourcesCompat.getDrawable(mContext.getResources(),
                    R.drawable.snv_collapse, mContext.getTheme());
        }

        // Return a new drawable since this method will be
        // called multiple times
        return mGroupCollapseDrawable.getConstantState().newDrawable();
    }

    public Drawable getItemBackground() {
        if (mItemBackground == null) {
            setDefaultItemBackground();
        }

        return mItemBackground.getConstantState().newDrawable();
    }

    public Drawable getDrawerBackground() {
        if (mDrawerBackground == null) {
            setDefaultDrawerBackground();
        }

        return mDrawerBackground;
    }

    public TextViewStyleProfile getItemStyleProfile() {
        if (mItemStyleProfile == null) {
            mItemStyleProfile = new TextViewStyleProfile(mContext, mDefaultTheme);
        }

        return mItemStyleProfile;
    }

    public TextViewStyleProfile getItemHintStyleProfile() {
        if (mItemHintStyleProfile == null) {
            mItemHintStyleProfile = new TextViewStyleProfile(mContext, mDefaultTheme);
        }

        return mItemHintStyleProfile;
    }

    public TextViewStyleProfile getSubheaderStyleProfile() {
        if (mSubheaderStyleProfile == null) {
            mSubheaderStyleProfile = new TextViewStyleProfile(mContext, mDefaultTheme);
        }

        return mSubheaderStyleProfile;
    }

    public TextViewStyleProfile getSubheaderHintStyleProfile() {
        if (mSubheaderHintStyleProfile == null) {
            mSubheaderHintStyleProfile = new TextViewStyleProfile(mContext, mDefaultTheme);
        }

        return mSubheaderHintStyleProfile;
    }

    public TextViewStyleProfile getBadgeStyleProfile() {
        if (mBadgeStyleProfile == null) {
            mBadgeStyleProfile = new TextViewStyleProfile(mContext, mDefaultTheme);
        }

        return mBadgeStyleProfile;
    }

    public DefaultTheme getDefaultTheme() {
        return mDefaultTheme;
    }

    public float getElevation() {
        return mElevation;
    }

    private void setDefaultIconTintList() {
        TypedValue value = new TypedValue();
        if (mContext.getTheme().resolveAttribute(android.R.attr.textColorPrimary, value, true)) {
            ColorStateList baseColor = mContext.getResources().getColorStateList(value.resourceId);
            if (mContext.getTheme().resolveAttribute(R.attr.colorPrimary, value, true)) {
                int colorPrimary = value.data;
                int defaultColor = baseColor.getDefaultColor();
                mIconTintList = new ColorStateList(new int[][]{DISABLED_STATE_SET, CHECKED_STATE_SET, EMPTY_STATE_SET},
                        new int[]{baseColor.getColorForState(DISABLED_STATE_SET, defaultColor), colorPrimary, defaultColor});
            }
        }

        if (mIconTintList == null) {
            // Defaults
            boolean isLightTheme = mDefaultTheme == SublimeThemer.DefaultTheme.LIGHT;

            int defDisabled = isLightTheme ?
                    mContext.getResources().getColor(R.color.snv_primary_text_disabled_material_light)
                    : mContext.getResources().getColor(R.color.snv_primary_text_disabled_material_dark);
            int defChecked = isLightTheme ?
                    mContext.getResources().getColor(R.color.snv_primary_material_light)
                    : mContext.getResources().getColor(R.color.snv_primary_material_dark);
            int defEmptySet = isLightTheme ?
                    mContext.getResources().getColor(R.color.snv_primary_text_default_material_light)
                    : mContext.getResources().getColor(R.color.snv_primary_text_default_material_dark);
            mIconTintList = new ColorStateList(new int[][]{DISABLED_STATE_SET, CHECKED_STATE_SET, EMPTY_STATE_SET},
                    new int[]{defDisabled, defChecked, defEmptySet});
        }
    }

    private void setDefaultCheckableItemTintList() {
        TypedValue value = new TypedValue();
        if (mContext.getTheme().resolveAttribute(android.R.attr.textColorPrimary, value, true)) {
            ColorStateList baseColor = mContext.getResources().getColorStateList(value.resourceId);
            if (mContext.getTheme().resolveAttribute(R.attr.colorPrimary, value, true)) {
                int colorPrimary = value.data;
                int defaultColor = baseColor.getDefaultColor();
                mCheckableItemTintList = new ColorStateList(new int[][]{DISABLED_STATE_SET,
                        CHECKABLE_ITEM_CHECKED_STATE_SET, EMPTY_STATE_SET},
                        new int[]{baseColor.getColorForState(DISABLED_STATE_SET, defaultColor),
                                colorPrimary, defaultColor});
            }
        }

        if (mCheckableItemTintList == null) {
            // Defaults
            boolean isLightTheme = mDefaultTheme == SublimeThemer.DefaultTheme.LIGHT;

            int defDisabled = isLightTheme ?
                    mContext.getResources().getColor(R.color.snv_primary_text_disabled_material_light)
                    : mContext.getResources().getColor(R.color.snv_primary_text_disabled_material_dark);
            int defChecked = isLightTheme ?
                    mContext.getResources().getColor(R.color.snv_primary_material_light)
                    : mContext.getResources().getColor(R.color.snv_primary_material_dark);
            int defEmptySet = isLightTheme ?
                    mContext.getResources().getColor(R.color.snv_primary_text_default_material_light)
                    : mContext.getResources().getColor(R.color.snv_primary_text_default_material_dark);
            mCheckableItemTintList = new ColorStateList(new int[][]{DISABLED_STATE_SET, CHECKABLE_ITEM_CHECKED_STATE_SET, EMPTY_STATE_SET},
                    new int[]{defDisabled, defChecked, defEmptySet});
        }
    }

    private void setDefaultItemBackground() {
        TypedValue value = new TypedValue();
        int colorControlHighlight = 0;
        if (mContext.getTheme().resolveAttribute(R.attr.colorControlHighlight, value, true)) {
            colorControlHighlight = value.data;
        } else {
            colorControlHighlight = mDefaultTheme == DefaultTheme.LIGHT ?
                    mContext.getResources().getColor(R.color.snv_ripple_material_light)
                    : mContext.getResources().getColor(R.color.snv_ripple_material_dark);
        }

        StateListDrawable drawable = new StateListDrawable();
        LayerDrawable checked = new LayerDrawable(new Drawable[]{
                new ColorDrawable(colorControlHighlight),
                obtainSelectableItemBackground()
        });
        drawable.addState(CHECKED_STATE_SET, checked);
        drawable.addState(EMPTY_STATE_SET, obtainSelectableItemBackground());
        mItemBackground = drawable;
    }

    private Drawable obtainSelectableItemBackground() {
        // Create an array of the attributes we want to resolve
        int[] attrs = new int[]{R.attr.selectableItemBackground}; // index 0

        // Obtain the styled attributes
        TypedArray ta = mContext.obtainStyledAttributes(attrs);

        Drawable drawableFromTheme = null;

        if (ta.hasValue(0)) {
            // Get the value of the 'selectableItemBackground' attribute that was
            // set in the theme. The parameter is the index
            // of the attribute in the 'attrs' array.
            drawableFromTheme = ta.getDrawable(0); // index
        }

        // Finally free resources used by TypedArray
        ta.recycle();

        return drawableFromTheme;
    }

    private void setDefaultDrawerBackground() {
        TypedValue value = new TypedValue();
        if (mContext.getTheme().resolveAttribute(android.R.attr.colorBackground, value, true)) {
            mDrawerBackground = new ColorDrawable(value.data);
        } else {
            mDrawerBackground = new ColorDrawable(mDefaultTheme == DefaultTheme.LIGHT ?
                    mContext.getResources().getColor(R.color.snv_background_material_light)
                    : mContext.getResources().getColor(R.color.snv_background_material_dark));
        }
    }
}
