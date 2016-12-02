/*
 * The difference between LeFragmentTabHost and FragmentTabHost
 * 1. You can give a FrameLayout object to LeFragmentTabHost besides containerId when calling setup(...)
 * 2. Support click listener on tabs even no fragment transaction happens
 */


package com.letv.wallet.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;

import java.util.ArrayList;

/**
 * Special TabHost that allows the use of {@link Fragment} objects for
 * its tab content.  When placing this in a view hierarchy, after inflating
 * the hierarchy you must call {@link #setup(Context, FragmentManager, int)}
 * to complete the initialization of the tab host.
 *
 * <p>Here is a simple example of using a FragmentTabHost in an Activity:
 *
 * {@sample development/samples/Support4Demos/src/com/example/android/supportv4/app/FragmentTabs.java
 *      complete}
 *
 * <p>This can also be used inside of a fragment through fragment nesting:
 *
 * {@sample development/samples/Support4Demos/src/com/example/android/supportv4/app/FragmentTabsFragmentSupport.java
 *      complete}
 */
public class LeFragmentTabHost extends TabHost
        implements TabHost.OnTabChangeListener {
    private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
    private FrameLayout mRealTabContent;
    private Context mContext;
    private FragmentManager mFragmentManager;
    private int mContainerId;
    private OnTabChangeListener mOnTabChangeListener;
    private TabInfo mLastTab;
    private boolean mAttached;

    static class TabInfo {
        private String tag;
        private Class<?> clss;
        private Bundle args;
        private Fragment fragment;

        TabInfo(String _tag, Class<?> _class, Bundle _args) {
            tag = _tag;
            clss = _class;
            args = _args;
        }

        public String getTag() {
            return tag;
        }
    }

    static class DummyTabFactory implements TabContentFactory {
        private final Context mContext;

        public DummyTabFactory(Context context) {
            mContext = context;
        }

        @Override
        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }
    }

    static class SavedState extends BaseSavedState {
        String curTab;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            curTab = in.readString();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(curTab);
        }

        @Override
        public String toString() {
            return "FragmentTabHost.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " curTab=" + curTab + "}";
        }

        public static final Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    public LeFragmentTabHost(Context context) {
        // Note that we call through to the version that takes an AttributeSet,
        // because the simple Context construct can result in a broken object!
        super(context, null);
        initFragmentTabHost(context, null);
    }

    public LeFragmentTabHost(Context context, AttributeSet attrs) {
        super(context, attrs);
        initFragmentTabHost(context, attrs);
    }

    private void initFragmentTabHost(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs,
                new int[] { android.R.attr.inflatedId }, 0, 0);
        mContainerId = a.getResourceId(0, 0);
        a.recycle();

        super.setOnTabChangedListener(this);
    }

    private void ensureHierarchy(Context context) {
        // If owner hasn't made its own view hierarchy, then as a convenience
        // we will construct a standard one here.
        if (findViewById(android.R.id.tabs) == null) {
            LinearLayout ll = new LinearLayout(context);
            ll.setOrientation(LinearLayout.VERTICAL);
            addView(ll, new LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.FILL_PARENT));

            TabWidget tw = new TabWidget(context);
            tw.setId(android.R.id.tabs);
            tw.setOrientation(TabWidget.HORIZONTAL);
            ll.addView(tw, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 0));

            FrameLayout fl = new FrameLayout(context);
            fl.setId(android.R.id.tabcontent);
            ll.addView(fl, new LinearLayout.LayoutParams(0, 0, 0));

            mRealTabContent = fl = new FrameLayout(context);
            mRealTabContent.setId(mContainerId);
            ll.addView(fl, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.FILL_PARENT, 0, 1));
        }
    }

    /**
     * @deprecated Don't call the original TabHost setup, you must instead
     * call {@link #setup(Context, FragmentManager)} or
     * {@link #setup(Context, FragmentManager, int)}.
     */
    @Override @Deprecated
    public void setup() {
        throw new IllegalStateException(
                "Must call setup() that takes a Context and FragmentManager");
    }

    public void setup(Context context, FragmentManager manager) {
        ensureHierarchy(context);  // Ensure views required by super.setup()
        super.setup();
        mContext = context;
        mFragmentManager = manager;
        ensureContent();
    }

    public void setup(Context context, FragmentManager manager, int containerId) {
        ensureHierarchy(context);  // Ensure views required by super.setup()
        super.setup();
        mContext = context;
        mFragmentManager = manager;
        mContainerId = containerId;
        ensureContent();
        mRealTabContent.setId(containerId);

        // We must have an ID to be able to save/restore our state.  If
        // the owner hasn't set one at this point, we will set it ourself.
        if (getId() == View.NO_ID) {
            setId(android.R.id.tabhost);
        }
    }

    private void ensureContent() {
        if (mRealTabContent == null) {
            mRealTabContent = (FrameLayout)findViewById(mContainerId);
            if (mRealTabContent == null) {
                throw new IllegalStateException(
                        "No tab content FrameLayout found for id " + mContainerId);
            }
        }
    }

    @Override
    public void setOnTabChangedListener(OnTabChangeListener l) {
        mOnTabChangeListener = l;
    }

    public void addTab(TabSpec tabSpec, Class<?> clss, Bundle args) {
        tabSpec.setContent(new DummyTabFactory(mContext));
        String tag = tabSpec.getTag();

        TabInfo info = new TabInfo(tag, clss, args);

        if (mAttached) {
            // If we are already attached to the window, then check to make
            // sure this tab's fragment is inactive if it exists.  This shouldn't
            // normally happen.
            info.fragment = mFragmentManager.findFragmentByTag(tag);
            if (info.fragment != null && !info.fragment.isDetached()) {
                FragmentTransaction ft = mFragmentManager.beginTransaction();
                if (!mHideFragment) {
                    ft.detach(info.fragment);
                } else {
                    ft.hide(info.fragment);
                }

                ft.commitAllowingStateLoss();
            }
        }

        mTabs.add(info);
        addTab(tabSpec);
    }

    public void replaceTab(int position, Class<?> clss, Bundle args) {
        if (clss == null || position < 0 || position >= mTabs.size()) {
            return;
        }

        TabInfo tab = mTabs.get(position);
        if (tab == null) {
            return;
        }
        if (clss.equals(tab.clss) && (args == null) && (tab.args == null)) {
            return;
        }
        if (tab.fragment != null) {
            // remove old fragment
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            ft.remove(tab.fragment);

            // show new fragment if necessary
            if (getCurrentTab() == position) {
                tab.fragment = Fragment.instantiate(mContext,
                        clss.getName(), args);
                ft.add(mContainerId, tab.fragment, tab.tag);
            } else {
                tab.fragment = null;
            }

            ft.commitAllowingStateLoss();
        }

        // store new fragment parameters
        tab.clss = clss;
        tab.args = args;
    }

    public void replaceTab(String tag, Class<?> clss, Bundle args) {
        if (tag == null || clss == null) {
            return;
        }

        for (int i = 0; i < mTabs.size(); i++) {
            if (mTabs.get(i).getTag().equals(tag)) {
                replaceTab(i, clss, args);
                break;
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        String currentTab = getCurrentTabTag();

        // Go through all tabs and make sure their fragments match
        // the correct state.
        FragmentTransaction ft = null;
        for (int i=0; i<mTabs.size(); i++) {
            TabInfo tab = mTabs.get(i);
            tab.fragment = mFragmentManager.findFragmentByTag(tab.tag);

            //EUI: MOBILEP-28096: if fragment not match class, remove fragment
            if (tab.fragment != null && !tab.fragment.isDetached()) {
                if (tab.clss == null || !tab.fragment.getClass().equals(tab.clss)) {
                    // This fragment was restored in the active state,
                    // but is not the current tab.  Deactivate it.
                    if (ft == null) {
                        ft = mFragmentManager.beginTransaction();
                    }

                    //EUI: XIII-5542: for those not match fragment, do detach, no hide
                    // Reproduce: no contacts, dialer, tab_contacts, home, kill acore, add contacts,
                    // dialer, tab_contacts, delete contacts, home, kill acore, add contacts,
                    // dialer, see tab_contacts's fragment in dialer_tab
                    ft.detach(tab.fragment);
                    tab.fragment = null;
                }
            }
            //EUI: MOBILEP-28096: end

            if (tab.fragment != null && !tab.fragment.isDetached()) {
                if (tab.tag.equals(currentTab)) {
                    // The fragment for this tab is already there and
                    // active, and it is what we really want to have
                    // as the current tab.  Nothing to do.
                    mLastTab = tab;
                } else {
                    // This fragment was restored in the active state,
                    // but is not the current tab.  Deactivate it.
                    if (ft == null) {
                        ft = mFragmentManager.beginTransaction();
                    }

                    if (!mHideFragment) {
                        ft.detach(tab.fragment);
                    } else {
                        ft.hide(tab.fragment);
                    }
                }
            }
        }

        // We are now ready to go.  Make sure we are switched to the
        // correct tab.
        mAttached = true;
        ft = doTabChanged(currentTab, ft);
        if (ft != null) {
            ft.commitAllowingStateLoss();
            mFragmentManager.executePendingTransactions();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAttached = false;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.curTab = getCurrentTabTag();
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState)state;
        super.onRestoreInstanceState(ss.getSuperState());
        setCurrentTabByTag(ss.curTab);
    }

    @Override
    public void onTabChanged(String tabId) {
        if (mAttached) {
            FragmentTransaction ft = doTabChanged(tabId, null);
            if (ft != null) {
                ft.commitAllowingStateLoss();
            }
        }
        if (mOnTabChangeListener != null) {
            mOnTabChangeListener.onTabChanged(tabId);
        }
    }

    private FragmentTransaction doTabChanged(String tabId, FragmentTransaction ft) {
        TabInfo newTab = null;
        for (int i=0; i<mTabs.size(); i++) {
            TabInfo tab = mTabs.get(i);
            if (tab.tag.equals(tabId)) {
                newTab = tab;
            }
        }
        if (newTab == null) {
            throw new IllegalStateException("No tab known for tag " + tabId);
        }
        if (mLastTab != newTab) {
            if (ft == null) {
                ft = mFragmentManager.beginTransaction();
            }
            if (mLastTab != null) {
                if (mLastTab.fragment != null) {
                    if (!mHideFragment) {
                        ft.detach(mLastTab.fragment);
                    } else {
                        ft.hide(mLastTab.fragment);
                    }
                }
            }
            if (newTab != null) {
                if (newTab.fragment == null) {
                    newTab.fragment = Fragment.instantiate(mContext,
                            newTab.clss.getName(), newTab.args);
                    ft.add(mContainerId, newTab.fragment, newTab.tag);
                } else {
                    if (!mHideFragment) {
                        ft.attach(newTab.fragment);
                    } else {
                        ft.show(newTab.fragment);
                    }
                }
            }

            mLastTab = newTab;
        }
        return ft;
    }

    // Leui add  begin
    private boolean mHideFragment = false;

    public void setFragmentHiddenEnabled(boolean hideFragment) {
        mHideFragment = hideFragment;
    }

    /**
     * User can gives an tab content container which is not a child of LeFragmentTabHost
     */
    public void setup(Context context, FragmentManager manager, int containerId, FrameLayout realContent) {
        if (realContent == null) {
            setup(context, manager, containerId);
        } else {
            ensureHierarchy(context);  // Ensure views required by super.setup()
            super.setup();
            mContext = context;
            mFragmentManager = manager;
            mContainerId = containerId;

            mRealTabContent = realContent;
            if (mRealTabContent.getId() != containerId) {
                throw new IllegalStateException(
                        "mRealTabContent and containerId mismatch");
            }

            // We must have an ID to be able to save/restore our state.  If
            // the owner hasn't set one at this point, we will set it ourself.
            if (getId() == View.NO_ID) {
                setId(android.R.id.tabhost);
            }
        }
    }

    @Override
    public void setCurrentTab(int index) {

        if (index < 0 || index >= getTabWidget().getChildCount())
            return;

        if (index == getCurrentTab()) {
            if (mOnClickTabNotChangeListener != null) {
                mOnClickTabNotChangeListener.onClickTabNotChanged(getCurrentTabTag());
            }

            long currentTimeMs = System.currentTimeMillis();
            if (mLastClickTime != -1l) {
                if (currentTimeMs - mLastClickTime <= DOUBLE_CLICK_TIME_MS) {
                    mLastClickTime = -1l;
                    if (mOnDoubleClickTabNotChangeListener != null) {
                        mOnDoubleClickTabNotChangeListener.onDoubleClickTabNotChanged(getCurrentTabTag());
                    }
                } else {
                    mLastClickTime = currentTimeMs;
                }
            } else {
                mLastClickTime = currentTimeMs;
            }
        } else {
            mLastClickTime = -1l;
        }

        super.setCurrentTab(index);
    }

    private static final int DOUBLE_CLICK_TIME_MS = 350;
    /**
     * Interface definition for a callback to be invoked when tab changed
     */
    public interface OnClickTabNotChangeListener {
        void onClickTabNotChanged(String tabId);
    }

    public interface OnDoubleClickTabNotChangeListener {
        void onDoubleClickTabNotChanged(String tabId);
    }

    private OnClickTabNotChangeListener mOnClickTabNotChangeListener;
    private OnDoubleClickTabNotChangeListener mOnDoubleClickTabNotChangeListener;
    private long mLastClickTime = -1l;

    public void setOnClickTabNotChangeListener(OnClickTabNotChangeListener l) {
        mOnClickTabNotChangeListener = l;
    }

    public void setOnDoubleClickTabNotChangeListener(OnDoubleClickTabNotChangeListener l) {
        mOnDoubleClickTabNotChangeListener = l;
    }

    // Leui add end

}
