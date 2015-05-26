package com.walletudo.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.samples.apps.iosched.ui.widget.ScrimInsetsScrollView;
import com.walletudo.DatabaseInitializer;
import com.walletudo.R;
import com.walletudo.Walletudo;
import com.walletudo.model.Profile;
import com.walletudo.service.CashFlowService;
import com.walletudo.service.ProfileService;
import com.walletudo.service.StatisticService;
import com.walletudo.service.TagService;
import com.walletudo.service.WalletService;
import com.walletudo.ui.profile.ProfileActivity;
import com.walletudo.ui.settings.SettingsActivity;
import com.walletudo.ui.statistics.list.StatisticListActivityState;
import com.walletudo.util.AndroidUtils;
import com.walletudo.util.PrefUtils;
import com.walletudo.util.UIUtils;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;


public class BaseActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = BaseActivity.class.getSimpleName();

    // delay to launch nav drawer type, to allow close animation to play
    private static final int NAVDRAWER_LAUNCH_DELAY = 250;
    // fade in and fade out durations for the main content when switching between
    // different Activities of the app through the Nav Drawer
    private static final int MAIN_CONTENT_FADEOUT_DURATION = 150;
    private static final int MAIN_CONTENT_FADEIN_DURATION = 250;
    private static final int ACCOUNT_BOX_EXPAND_ANIM_DURATION = 200;

    private static final int RESOLVE_CONNECTION_REQUEST_CODE = 6969;
    private static final int RC_NEW_PROFILE = 150;

    @Inject
    protected StatisticListActivityState statisticListActivityState;

    @Inject
    protected ProfileService profileService;

    @Inject
    protected StatisticService statisticService;

    @Inject
    protected WalletService walletService;

    @Inject
    protected TagService tagService;

    @Inject
    protected CashFlowService cashFlowService;

    @Inject
    PrefUtils prefUtils;

    @Inject
    Map<DrawerItemType, DrawerItemContent> navigationDrawerMap;

    @Inject
    List<DrawerItemType> navigationDrawerList;

    private Toolbar mActionBarToolbar;
    private DrawerLayout mDrawerLayout;
    // A Runnable that we should execute when the navigation drawer finishes its closing animation
    private Runnable mDeferredOnDrawerClosedRunnable;
    private boolean mAccountBoxExpanded = false;
    private ImageView mExpandAccountBoxIndicator;
    private Handler mHandler;
    // views that correspond to each navigation_drawer type, null if not yet created
    private View[] mNavDrawerItemViews = null;
    // Navigation drawer menu items

    private LinearLayout mAccountListContainer;
    private LinearLayout mAccountListFooter;

    private ViewGroup mNavDrawerListContainer;
    private LinearLayout mNavDrawerListFooter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Walletudo application = (Walletudo) getApplication();
        prefUtils = application.component().prefUtils();
        // Perform one-time bootstrap setup, if needed
        if (!prefUtils.isDataBootstrapDone()) {
            Log.d(TAG, "One-time data bootstrap not done yet. Doing now.");
            performDataBootstrap();
        }

        application.component().inject(this);

        super.onCreate(savedInstanceState);

        mHandler = new Handler();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupNavDrawer();
        setupAccountBox();

        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null) {
            mainContent.setAlpha(0);
            mainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        } else {
            Log.w(TAG, "No view with ID main_content to fade in.");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        startLoginProcess();
    }


    @Override
    public void onBackPressed() {
        if (isNavDrawerOpen()) {
            closeNavDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (isNavDrawerOpen()) {
                closeNavDrawer();
            } else {
                openNavDrawer();
            }
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    protected boolean isNavDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(Gravity.START);
    }

    protected void closeNavDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(Gravity.START);
        }
    }

    protected void openNavDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.openDrawer(Gravity.START);
        }
    }

    private void startLoginProcess() {
        Profile activeProfile = profileService.getActiveProfile();

        if (activeProfile == null) {
            throw new RuntimeException("No profile available. TODO.");
        }
    }

    /**
     * Performs the one-time data bootstrap. This means taking our prepackaged conference data
     * from the R.raw.bootstrap_data resource, and parsing it to populate the database. This
     * data contains the sessions, speakers, etc.
     */
    private void performDataBootstrap() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Log.d(TAG, "Starting data bootstrap process.");

                new DatabaseInitializer(BaseActivity.this).createExampleAccountWithProfile();
                prefUtils.markDataBootstrapDone();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                BaseActivity.this.recreate();
            }
        }.execute();
    }

    private void setupNavDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout == null) {
            return;
        }
        mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.theme_primary_dark));

        final LinearLayout navDrawer = (LinearLayout) mDrawerLayout.findViewById(R.id.navdrawer);

        if (getSelfNavDrawerItem() == DrawerItemType.INVALID) {
            // do not show a nav drawer
            if (navDrawer != null) {
                ((ViewGroup) navDrawer.getParent()).removeView(navDrawer);
            }
            mDrawerLayout = null;
            return;
        }

        final ScrimInsetsScrollView navDrawerContent = (ScrimInsetsScrollView) navDrawer.findViewById(R.id.navdrawer_content);

        if (navDrawerContent != null) {
            final View chosenAccountContentView = findViewById(R.id.chosen_account_content_view);
            final View chosenAccountView = findViewById(R.id.chosen_account_view);
            final int navDrawerChosenAccountHeight = getResources().getDimensionPixelSize(R.dimen.navdrawer_chosen_account_height);
            navDrawerContent.setOnInsetsCallback(new ScrimInsetsScrollView.OnInsetsCallback() {
                @Override
                public void onInsetsChanged(Rect insets) {
                    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) chosenAccountContentView.getLayoutParams();
                    lp.topMargin = insets.top;
                    chosenAccountContentView.setLayoutParams(lp);

                    ViewGroup.LayoutParams lp2 = chosenAccountView.getLayoutParams();
                    lp2.height = navDrawerChosenAccountHeight + insets.top;
                    chosenAccountView.setLayoutParams(lp2);
                }
            });
        }

        if (mActionBarToolbar != null) {
            mActionBarToolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
            mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDrawerLayout.openDrawer(Gravity.START);
                }
            });
        }

        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                onNavDrawerSlide(slideOffset);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                // run deferred action, if we have one
                if (mDeferredOnDrawerClosedRunnable != null) {
                    mDeferredOnDrawerClosedRunnable.run();
                    mDeferredOnDrawerClosedRunnable = null;
                }
                if (mAccountBoxExpanded) {
                    mAccountBoxExpanded = false;
                    setupAccountBoxToggle();
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);

        // populate the nav drawer with the correct items
        populateNavigationDrawer();

        openDrawerOnWelcome();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        getActionBarToolbar();
        setupAdBanner();
    }

    public Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            if (mActionBarToolbar != null) {
                setSupportActionBar(mActionBarToolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
        return mActionBarToolbar;
    }

    private void setupAdBanner() {
//        AdView adView = (AdView) findViewById(R.id.adView);
//        if (adView != null) {
//            adView.loadAd(new AdRequest.Builder().build());
//        }
    }

    private View makeNavDrawerItem(final DrawerItemType type, ViewGroup container) {
        boolean selected = getSelfNavDrawerItem() == type;
        int layoutToInflate;
        if (type == DrawerItemType.SEPARATOR) {
            layoutToInflate = R.layout.item_navigation_drawer_separator;
        } else {
            layoutToInflate = R.layout.item_navigation_drawer;
        }
        View view = getLayoutInflater().inflate(layoutToInflate, container, false);

        if (type == DrawerItemType.SEPARATOR) {
            // we are done
            UIUtils.setAccessibilityIgnore(view);
            return view;
        }

        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        TextView titleView = (TextView) view.findViewById(R.id.title);
        int iconId = navigationDrawerMap.get(type).getIcon();
        String title = navigationDrawerMap.get(type).getTittle();

        // set icon and text
        iconView.setVisibility(iconId > 0 ? View.VISIBLE : View.GONE);
        if (iconId > 0) {
            iconView.setImageResource(iconId);
        }
        titleView.setText(title);

        formatNavDrawerItem(view, type, selected);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNavDrawerItemClicked(type);
            }
        });

        return view;
    }

    protected DrawerItemType getSelfNavDrawerItem() {
        return DrawerItemType.INVALID;
    }

    private void formatNavDrawerItem(View view, DrawerItemType drawerItemType, boolean selected) {
        if (drawerItemType == DrawerItemType.SEPARATOR) {
            // not applicable
            return;
        }

        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        TextView titleView = (TextView) view.findViewById(R.id.title);

        // configure its appearance according to whether or not it's selected
        titleView.setTextColor(selected ?
                getResources().getColor(R.color.navdrawer_text_color_selected) :
                getResources().getColor(R.color.navdrawer_text_color));
        iconView.setColorFilter(selected ?
                getResources().getColor(R.color.navdrawer_icon_tint_selected) :
                getResources().getColor(R.color.navdrawer_icon_tint));
    }

    private void populateNavigationDrawer() {
        setupNavDrawerViewContent();
        setupNavDrawerFooter();
    }

    private void setupNavDrawerViewContent() {
        mNavDrawerListContainer = (ViewGroup) findViewById(R.id.navdrawer_list);

        if (mNavDrawerListContainer == null) {
            return;
        }

        mNavDrawerItemViews = new View[navigationDrawerList.size()];
        mNavDrawerListContainer.removeAllViews();
        int i = 0;
        for (DrawerItemType type : navigationDrawerList) {
            mNavDrawerItemViews[i] = makeNavDrawerItem(type, mNavDrawerListContainer);
            mNavDrawerListContainer.addView(mNavDrawerItemViews[i]);
            ++i;
        }
    }

    private void setupNavDrawerFooter() {
        mNavDrawerListFooter = (LinearLayout) findViewById(R.id.navdrawer_list_footer);
        mNavDrawerListFooter.removeAllViews();
        mNavDrawerListFooter.addView(makeNavDrawerItem(DrawerItemType.SEPARATOR, mNavDrawerListFooter));
        View footerItem = getLayoutInflater().inflate(R.layout.item_navigation_drawer, mNavDrawerListFooter, false);
        TextView title = (TextView) footerItem.findViewById(R.id.title);
        ImageView icon = (ImageView) footerItem.findViewById(R.id.icon);
        icon.setImageResource(R.drawable.ic_menu_settings);
        title.setText(getString(R.string.settingsMenu));
        footerItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BaseActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
        mNavDrawerListFooter.addView(footerItem);
    }

    private void onNavDrawerItemClicked(final DrawerItemType type) {
        if (type == getSelfNavDrawerItem()) {
            mDrawerLayout.closeDrawer(Gravity.START);
            return;
        }

        if (getSelfNavDrawerItem() == DrawerItemType.STATISTIC) {
            statisticListActivityState.clear();
        }

        if (isSpecialItem(type)) {
            goToNavDrawerItem(type);
        } else {
            // launch the target Activity after a short delay, to allow the close animation to play
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    goToNavDrawerItem(type);
                }
            }, NAVDRAWER_LAUNCH_DELAY);

            // change the active type on the list so the user can see the type changed
            setSelectedNavDrawerItem(type);
            // fade out the main content
            View mainContent = findViewById(R.id.main_content);
            if (mainContent != null) {
                mainContent.animate().alpha(0).setDuration(MAIN_CONTENT_FADEOUT_DURATION);
            }
        }

        mDrawerLayout.closeDrawer(Gravity.START);
    }

    private boolean isSpecialItem(DrawerItemType type) {
        return type == DrawerItemType.SETTINGS;
    }

    private void goToNavDrawerItem(DrawerItemType type) {
        Intent intent = new Intent(this, navigationDrawerMap.get(type).getActivityClass());
        startActivity(intent);
        finish();
    }

    /**
     * Sets up the given navigation_drawer type's appearance to the selected state. Note: this could
     * also be accomplished (perhaps more cleanly) with state-based layouts.
     */
    private void setSelectedNavDrawerItem(DrawerItemType drawerItemType) {
        if (mNavDrawerItemViews != null) {
            for (int i = 0; i < mNavDrawerItemViews.length; i++) {
                if (i < navigationDrawerList.size()) {
                    DrawerItemType thisType = navigationDrawerList.get(i);
                    formatNavDrawerItem(mNavDrawerItemViews[i], thisType, drawerItemType == thisType);
                }
            }
        }
    }

    protected void onNavDrawerSlide(float offset) {
    }

    private void openDrawerOnWelcome() {
        // When the user runs the app for the first time, we want to land them with the
        // navigation drawer open. But just the first time.
        if (!prefUtils.isWelcomeDone()) {
            // first run of the app starts with the nav drawer open
            prefUtils.markWelcomeDone();
            mDrawerLayout.openDrawer(Gravity.START);
        }
    }

    /**
     * Sets up the account box. The account box is the area at the top of the nav drawer that
     * shows which account the user is logged in as, and lets them switch accounts. It also
     * shows the user's Google+ cover photo as background.
     */
    private void setupAccountBox() {
        mAccountListContainer = (LinearLayout) findViewById(R.id.account_list);
        mAccountListFooter = (LinearLayout) findViewById(R.id.account_list_footer);

        if (mAccountListContainer == null) {
            //This activity does not have an account box
            return;
        }

        final View chosenAccountView = findViewById(R.id.chosen_account_view);
        Profile activeProfile = profileService.getActiveProfile();
        if (activeProfile == null) {
            // No account logged in; hide account box
            chosenAccountView.setVisibility(View.GONE);
            mAccountListContainer.setVisibility(View.GONE);
            return;
        } else {
            chosenAccountView.setVisibility(View.VISIBLE);
            mAccountListContainer.setVisibility(View.INVISIBLE);
        }

        List<Profile> profiles = profileService.getAll();

        TextView nameView = (TextView) chosenAccountView.findViewById(R.id.profile_name);
        TextView emailView = (TextView) chosenAccountView.findViewById(R.id.account_email);
        mExpandAccountBoxIndicator = (ImageView) findViewById(R.id.expand_account_box_indicator);

//      TODO: Remove this.
//        if (profiles.isEmpty()) {
//            // There's only one account on the device, so no need for a switcher.
//            mExpandAccountBoxIndicator.setVisibility(View.GONE);
//            mAccountListContainer.setVisibility(View.GONE);
//            chosenAccountView.setEnabled(false);
//            return;
//        }

        nameView.setText(activeProfile.getName());
        emailView.setText(activeProfile.getGoogleAccount());

        chosenAccountView.setEnabled(true);
        mExpandAccountBoxIndicator.setVisibility(View.VISIBLE);
        chosenAccountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAccountBoxExpanded = !mAccountBoxExpanded;
                setupAccountBoxToggle();
            }
        });
        setupAccountBoxToggle();

        populateProfileList(profiles);

        setupAccountListFooter();
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, RESOLVE_CONNECTION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appropriately
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_NEW_PROFILE:
                if (resultCode == RESULT_OK) {
                    selectProfileById(profileService.getActiveProfile().getId());
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void populateProfileList(List<Profile> profiles) {
        mAccountListContainer.removeAllViews();

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        for (final Profile profile : profiles) {
            View itemView = layoutInflater.inflate(R.layout.item_navigation_drawer, mAccountListContainer, false);
            TextView profileNameView = (TextView) itemView.findViewById(R.id.title);
            profileNameView.setText(profile.getName());

            if (profile.getId().equals(profileService.getActiveProfile().getId())) {
                profileNameView.setTextColor(getResources().getColor(R.color.navdrawer_text_color_selected));

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mAccountBoxExpanded = false;
                        setupAccountBoxToggle();
                        mDrawerLayout.closeDrawer(Gravity.START);
                    }
                });
            } else {
                profileNameView.setTextColor(getResources().getColor(R.color.navdrawer_text_color));

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectProfileById(profile.getId());
                    }
                });
            }
            mAccountListContainer.addView(itemView);
        }
    }

    private void selectProfileById(Long id) {
        prefUtils.setActiveProfileId(id);
        AndroidUtils.restartApplication(this);
    }


    private void setupAccountListFooter() {
        mAccountListFooter.removeAllViews();

        mAccountListFooter.addView(makeNavDrawerItem(DrawerItemType.SEPARATOR, mAccountListFooter));

        View newProfileView = getLayoutInflater().inflate(R.layout.item_navigation_drawer, mAccountListFooter, false);
        TextView title = (TextView) newProfileView.findViewById(R.id.title);
        title.setText(getString(R.string.addProfileMenu));

        newProfileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BaseActivity.this, ProfileActivity.class);
                startActivityForResult(intent, RC_NEW_PROFILE);
            }
        });
        mAccountListFooter.addView(newProfileView);
    }

    private void setupAccountBoxToggle() {
        DrawerItemType selfDrawerType = getSelfNavDrawerItem();
        if (mDrawerLayout == null || selfDrawerType == DrawerItemType.INVALID) {
            // this Activity does not have a nav drawer
            return;
        }
        mExpandAccountBoxIndicator.setImageResource(mAccountBoxExpanded
                ? R.drawable.ic_drawer_accounts_collapse
                : R.drawable.ic_drawer_accounts_expand);
        int hideTranslateY = -mAccountListContainer.getHeight() / 4; // last 25% of animation
        if (mAccountBoxExpanded && mAccountListContainer.getTranslationY() == 0) {
            // initial setup
            mAccountListContainer.setAlpha(0);
            mAccountListContainer.setTranslationY(hideTranslateY);

            mAccountListFooter.setAlpha(0);
            mAccountListFooter.setTranslationY(-hideTranslateY);
        }

        AnimatorSet set = new AnimatorSet();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                onAnimationEnd(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mNavDrawerListContainer.setVisibility(mAccountBoxExpanded
                        ? View.INVISIBLE : View.VISIBLE);
                mNavDrawerListFooter.setVisibility(mAccountBoxExpanded
                        ? View.INVISIBLE : View.VISIBLE);

                mAccountListContainer.setVisibility(mAccountBoxExpanded
                        ? View.VISIBLE : View.INVISIBLE);
                mAccountListFooter.setVisibility(mAccountBoxExpanded
                        ? View.VISIBLE : View.INVISIBLE);
            }
        });

        if (mAccountBoxExpanded) {
            mAccountListContainer.setVisibility(View.VISIBLE);
            mAccountListFooter.setVisibility(View.VISIBLE);

            AnimatorSet hideNavDrawerList = new AnimatorSet();
            hideNavDrawerList.playTogether(
                    ObjectAnimator.ofFloat(mNavDrawerListContainer, View.ALPHA, 0)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION),
                    ObjectAnimator.ofFloat(mNavDrawerListFooter, View.ALPHA, 0)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION)
            );

            AnimatorSet showAccountList = new AnimatorSet();
            showAccountList.playTogether(
                    ObjectAnimator.ofFloat(mAccountListContainer, View.ALPHA, 1)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION),
                    ObjectAnimator.ofFloat(mAccountListContainer, View.TRANSLATION_Y, 0)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION),

                    ObjectAnimator.ofFloat(mAccountListFooter, View.ALPHA, 1)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION),
                    ObjectAnimator.ofFloat(mAccountListFooter, View.TRANSLATION_Y, 0)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION));

            set.playSequentially(
                    hideNavDrawerList,
                    showAccountList);
        } else {
            mNavDrawerListContainer.setVisibility(View.VISIBLE);
            mNavDrawerListFooter.setVisibility(View.VISIBLE);

            AnimatorSet hideAccountList = new AnimatorSet();
            hideAccountList.playTogether(
                    ObjectAnimator.ofFloat(mAccountListContainer, View.ALPHA, 0)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION),
                    ObjectAnimator.ofFloat(mAccountListContainer, View.TRANSLATION_Y, hideTranslateY)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION),

                    ObjectAnimator.ofFloat(mAccountListFooter, View.ALPHA, 0)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION),
                    ObjectAnimator.ofFloat(mAccountListFooter, View.TRANSLATION_Y, -hideTranslateY)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION));

            AnimatorSet showNavDrawerList = new AnimatorSet();
            showNavDrawerList.playTogether(
                    ObjectAnimator.ofFloat(mNavDrawerListFooter, View.ALPHA, 1)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION),
                    ObjectAnimator.ofFloat(mNavDrawerListContainer, View.ALPHA, 1)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION));

            set.playSequentially(
                    hideAccountList,
                    showNavDrawerList);
        }

        set.start();
    }

    public enum DrawerItemType {
        DASHBOARD,
        CASH_FLOW,
        STATISTIC,
        WALLET,
        TAG,
        INVALID,
        SETTINGS,
        SEPARATOR,
        SYNCHRONIZE
    }

    public static class DrawerItemContent {
        private String tittle;
        private int icon;
        private Class<? extends BaseActivity> activityClass;

        public DrawerItemContent(int icon, String tittle, Class<? extends BaseActivity> activityClass) {
            this.tittle = tittle;
            this.icon = icon;
            this.activityClass = activityClass;
        }

        public String getTittle() {
            return tittle;
        }

        public int getIcon() {
            return icon;
        }

        public Class<? extends BaseActivity> getActivityClass() {
            return activityClass;
        }
    }
}