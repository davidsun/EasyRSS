/*******************************************************************************
 * Copyright (c) 2012 Pursuer (http://pursuer.me).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Pursuer - initial API and implementation
 ******************************************************************************/

package org.freshrss.easyrss;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.freshrss.easyrss.R;
import org.freshrss.easyrss.HomeListWrapper.HomeListWrapperType;
import org.freshrss.easyrss.data.DataMgr;
import org.freshrss.easyrss.data.DataUtils;
import org.freshrss.easyrss.data.OnSettingUpdatedListener;
import org.freshrss.easyrss.data.Setting;
import org.freshrss.easyrss.data.Subscription;
import org.freshrss.easyrss.data.Tag;
import org.freshrss.easyrss.data.readersetting.SettingFontSize;
import org.freshrss.easyrss.data.readersetting.SettingSyncMethod;
import org.freshrss.easyrss.listadapter.AbsListItem;
import org.freshrss.easyrss.listadapter.ListAdapter;
import org.freshrss.easyrss.listadapter.OnItemTouchListener;
import org.freshrss.easyrss.network.GlobalItemDataSyncer;
import org.freshrss.easyrss.network.NetworkMgr;
import org.freshrss.easyrss.network.SubscriptionDataSyncer;
import org.freshrss.easyrss.network.TagDataSyncer;
import org.freshrss.easyrss.view.AbsViewCtrl;
import org.freshrss.easyrss.view.HorizontalPager;
import org.freshrss.easyrss.view.HorizontalPagerListener;
import org.freshrss.easyrss.view.PopupMenu;
import org.freshrss.easyrss.view.PopupMenuItem;
import org.freshrss.easyrss.view.PopupMenuListener;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class HomeViewCtrl extends AbsViewCtrl implements HorizontalPagerListener, PopupMenuListener,
        OnSettingUpdatedListener {
    private class HomeListAdapterListener implements OnItemTouchListener {
        final private int viewType;

        public HomeListAdapterListener(final int viewType) {
            this.viewType = viewType;
        }

        @Override
        public void onItemTouched(final ListAdapter adapter, final AbsListItem item, final MotionEvent event) {
            if (listener == null || !isAvailable || event.getAction() != MotionEvent.ACTION_UP) {
                return;
            }
            final String uid = item.getId();
            if (uid.length() == 0 || DataUtils.isSubscriptionUid(uid) || DataUtils.isUserTagUid(uid)) {
                listener.onItemListSelected(uid, viewType);
            }
        }
    }

    final private static int MENU_ID_SETTINGS = 0;
    final private static int MENU_ID_HELP = 1;
    final private static int MENU_ID_LOGOUT = 2;

    final private static String TAG_PROJECTION[] = new String[] { Tag._UID, Tag._UNREADCOUNT };
    final private static String SUBSCRIPTION_PROJECTION[] = new String[] { Subscription._UID, Subscription._TITLE,
            Subscription._UNREADCOUNT, Subscription._ICON };

    final private HomeListWrapper lstWrapperAll;
    final private HomeListWrapper lstWrapperStarred;
    final private HomeListWrapper lstWrapperUnread;
    final private Button btnStarred;
    final private Button btnAll;
    final private Button btnUnread;
    final private PopupMenu popupMenu;
    private boolean isAvailable;

    public HomeViewCtrl(final DataMgr dataMgr, final Context context) {
        super(dataMgr, R.layout.home, context);

        final int fontSize = new SettingFontSize(dataMgr).getData();

        this.isAvailable = false;
        this.popupMenu = new PopupMenu(context);
        final ListView lstStarred = (ListView) view.findViewById(R.id.HomeListStarred);
        final ListView lstAll = (ListView) view.findViewById(R.id.HomeListAll);
        final ListView lstUnread = (ListView) view.findViewById(R.id.HomeListUnread);
        this.lstWrapperStarred = new HomeListWrapper(dataMgr, lstStarred, HomeListWrapperType.TYPE_STARRED, fontSize);
        this.lstWrapperAll = new HomeListWrapper(dataMgr, lstAll, HomeListWrapperType.TYPE_ALL, fontSize);
        this.lstWrapperUnread = new HomeListWrapper(dataMgr, lstUnread, HomeListWrapperType.TYPE_UNREAD, fontSize);
        lstWrapperStarred.setAdapterListener(new HomeListAdapterListener(Home.VIEW_TYPE_STARRED));
        lstWrapperAll.setAdapterListener(new HomeListAdapterListener(Home.VIEW_TYPE_ALL));
        lstWrapperUnread.setAdapterListener(new HomeListAdapterListener(Home.VIEW_TYPE_UNREAD));

        popupMenu.addItem(new PopupMenuItem(MENU_ID_SETTINGS, R.drawable.popup_menu_item_settings, context
                .getString(R.string.TxtSettings)));
        popupMenu.addItem(new PopupMenuItem(MENU_ID_HELP, R.drawable.popup_menu_item_help, context
                .getString(R.string.TxtHelp)));
        popupMenu.addItem(new PopupMenuItem(MENU_ID_LOGOUT, R.drawable.popup_menu_item_logout, context
                .getString(R.string.TxtLogout)));
        popupMenu.setListener(new PopupMenuListener() {
            @Override
            public void onItemClick(final int id) {
                if (id == MENU_ID_HELP) {
                    showHelp();
                } else if (listener != null) {
                    switch (id) {
                    case MENU_ID_LOGOUT:
                        listener.onLogoutRequired();
                        break;
                    case MENU_ID_SETTINGS:
                        listener.onSettingsSelected();
                        break;
                    default:
                        break;
                    }
                }
            }
        });

        btnStarred = (Button) view.findViewById(R.id.BtnHomeStarred);
        btnAll = (Button) view.findViewById(R.id.BtnHomeAll);
        btnUnread = (Button) view.findViewById(R.id.BtnHomeUnread);

        if (GlobalItemDataSyncer.hasInstance() || SubscriptionDataSyncer.hasInstance() || TagDataSyncer.hasInstance()) {
            setProgressBarVisibility(true);
        } else {
            setProgressBarVisibility(false);
        }
    }

    @Override
    public void handleOnDataSyncerProgressChanged(final String text, final int progress, final int maxProgress) {
        showSyncingProgress(text, progress, maxProgress);
    }

    @Override
    public void onSettingUpdated(final String name) {
        if (name.equals(Setting.SETTING_SYNC_METHOD)) {
            setupHomeButtonSync();
        }
    }

    @Override
    public void handleOnSyncFinished(final String syncerType, final boolean succeeded) {
        setProgressBarVisibility(false);
        if (succeeded) {
            showSyncingProgress();
        }
    }

    @Override
    public void handleOnSyncStarted(final String syncerType) {
        setProgressBarVisibility(true);
    }

    private void initPager() {
        final HorizontalPager pager = (HorizontalPager) view.findViewById(R.id.HomeListPager);
        pager.setListener(this);
        final String sViewType = dataMgr.getSettingByName(Setting.SETTING_GLOBAL_VIEW_TYPE);
        final int viewType = (sViewType == null) ? Home.VIEW_TYPE_ALL : Integer.valueOf(sViewType);
        pager.setCurrentScreen(viewType, false);

        btnAll.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                pager.setCurrentScreen(1, true);
            }
        });
        btnStarred.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                pager.setCurrentScreen(0, true);
            }
        });
        btnUnread.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                pager.setCurrentScreen(2, true);
            }
        });

        final View btnRefresh = view.findViewById(R.id.BtnHomeRefresh);
        btnRefresh.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (listener != null) {
                    listener.onSyncRequired();
                }
            }
        });
    }

    @Override
    public void onActivate() {
        isAvailable = true;
    }

    @Override
    public void onCreate() {
        dataMgr.addOnSettingUpdatedListener(this);
        dataMgr.addOnSettingUpdatedListener(lstWrapperAll);
        dataMgr.addOnSettingUpdatedListener(lstWrapperUnread);
        dataMgr.addOnSubscriptionUpdatedListener(lstWrapperAll);
        dataMgr.addOnSubscriptionUpdatedListener(lstWrapperStarred);
        dataMgr.addOnSubscriptionUpdatedListener(lstWrapperUnread);
        dataMgr.addOnTagUpdatedListener(lstWrapperAll);
        dataMgr.addOnTagUpdatedListener(lstWrapperStarred);
        dataMgr.addOnTagUpdatedListener(lstWrapperUnread);
        NetworkMgr.getInstance().addListener(this);

        initPager();

        final View btnMore = view.findViewById(R.id.BtnHomeMore);
        btnMore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                final int[] location = new int[2];
                view.getLocationOnScreen(location);
                popupMenu.showAtLocation(view, Gravity.TOP | Gravity.LEFT, 0, location[1] + view.getHeight());
            }
        });

        showHomeList();
        showSyncingProgress();
        setupHomeButtonSync();
        final String sHelp = dataMgr.getSettingByName(Setting.SETTING_SHOW_HELP);
        if (sHelp == null || Boolean.valueOf(sHelp)) {
            showHelp();
            dataMgr.updateSetting(new Setting(Setting.SETTING_SHOW_HELP, String.valueOf(false)));
        }
    }

    @Override
    public void onDeactivate() {
        this.isAvailable = false;
    }

    @Override
    public void onDestory() {
        dataMgr.removeOnSettingUpdatedListener(this);
        dataMgr.removeOnSettingUpdatedListener(lstWrapperAll);
        dataMgr.removeOnSettingUpdatedListener(lstWrapperUnread);
        dataMgr.removeOnSubscriptionUpdatedListener(lstWrapperAll);
        dataMgr.removeOnSubscriptionUpdatedListener(lstWrapperStarred);
        dataMgr.removeOnSubscriptionUpdatedListener(lstWrapperUnread);
        dataMgr.removeOnTagUpdatedListener(lstWrapperAll);
        dataMgr.removeOnTagUpdatedListener(lstWrapperStarred);
        dataMgr.removeOnTagUpdatedListener(lstWrapperUnread);
        NetworkMgr.getInstance().removeListener(this);
    }

    @Override
    public void onItemClick(final int id) {
        // TODO empty method
    }

    @Override
    public void onScreenSwitch(final int currentScreen) {
        setHeaderLocation();
        switch (currentScreen) {
        case 0:
            btnStarred.setTextColor(0xFFFFFFFF);
            break;
        case 1:
            btnAll.setTextColor(0xFFFFFFFF);
            break;
        case 2:
            btnUnread.setTextColor(0xFFFFFFFF);
            break;
        default:
            break;
        }
        dataMgr.updateSetting(new Setting(Setting.SETTING_GLOBAL_VIEW_TYPE, String.valueOf(currentScreen)));
    }

    @Override
    public void onScrollChanged(final int l, final int t, final int oldl, final int oldt) {
        btnStarred.setTextColor(0xFF999999);
        btnAll.setTextColor(0xFF999999);
        btnUnread.setTextColor(0xFF999999);
        setHeaderLocation();
    }

    private void setHeaderLocation() {
        final View pager = view.findViewById(R.id.HomeListPager);
        final View homeNav = view.findViewById(R.id.HomeNav);
        final int btnWidth = btnAll.getMeasuredWidth();
        final MarginLayoutParams params = new MarginLayoutParams(homeNav.getLayoutParams());
        params.setMargins(
                (pager.getMeasuredWidth() - btnWidth) / 2 - pager.getScrollX() * btnWidth / pager.getMeasuredWidth(),
                0, 0, 0);
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(params);
        homeNav.setLayoutParams(layoutParams);
    }

    private void setProgressBarVisibility(final boolean isVisible) {
        if (isVisible) {
            view.findViewById(R.id.ProgressBarHomeLoading).setVisibility(View.VISIBLE);
            view.findViewById(R.id.BtnHomeRefresh).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.ProgressBarHomeLoading).setVisibility(View.GONE);
            view.findViewById(R.id.BtnHomeRefresh).setVisibility(View.VISIBLE);
        }
    }

    private void setupHomeButtonSync() {
        final View btnHomeSync = view.findViewById(R.id.BtnHomeSyncMethod);
        if (btnHomeSync == null) {
            return;
        }
        final SettingSyncMethod sSync = new SettingSyncMethod(dataMgr);
        switch (sSync.getData()) {
        case SettingSyncMethod.SYNC_METHOD_WIFI:
            btnHomeSync.setBackgroundResource(R.drawable.button_wifi_xml);
            break;
        case SettingSyncMethod.SYNC_METHOD_NETWORK:
            btnHomeSync.setBackgroundResource(R.drawable.button_network_xml);
            break;
        case SettingSyncMethod.SYNC_METHOD_MANUAL:
            btnHomeSync.setBackgroundResource(R.drawable.button_manual_xml);
            break;
        default:
        }
        btnHomeSync.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                final SettingSyncMethod sSync = new SettingSyncMethod(dataMgr);
                switch (sSync.getData()) {
                case SettingSyncMethod.SYNC_METHOD_WIFI:
                    sSync.setData(dataMgr, SettingSyncMethod.SYNC_METHOD_NETWORK);
                    Toast.makeText(context, context.getString(R.string.MsgSyncOnNetwork), Toast.LENGTH_LONG).show();
                    break;
                case SettingSyncMethod.SYNC_METHOD_NETWORK:
                    sSync.setData(dataMgr, SettingSyncMethod.SYNC_METHOD_MANUAL);
                    Toast.makeText(context, context.getString(R.string.MsgSyncDisabled), Toast.LENGTH_LONG).show();
                    break;
                case SettingSyncMethod.SYNC_METHOD_MANUAL:
                    sSync.setData(dataMgr, SettingSyncMethod.SYNC_METHOD_WIFI);
                    Toast.makeText(context, context.getString(R.string.MsgSyncOnWifi), Toast.LENGTH_LONG).show();
                    break;
                default:
                }
                setupHomeButtonSync();
            }
        });
    }

    private void showHelp() {
        final Dialog dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.setContentView(R.layout.popup_help);
        final ImageView img = (ImageView) dialog.findViewById(R.id.ImgHelp);
        img.setOnClickListener(new OnClickListener() {
            private int current = 0;

            @Override
            public void onClick(final View view) {
                current++;
                switch (current) {
                case 1:
                    img.setImageResource(R.drawable.intro_swiping_small);
                    break;
                case 2:
                    img.setImageResource(R.drawable.intro_switching_small);
                    break;
                default:
                    dialog.dismiss();
                    break;
                }
            }
        });
        dialog.findViewById(R.id.LayoutHelp).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void showHomeList() {
        final ContentResolver resolver = context.getContentResolver();
        {
            final Cursor cur = resolver.query(Tag.CONTENT_URI, TAG_PROJECTION, null, null, null);
            for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                final Tag tag = Tag.fromCursor(cur);
                lstWrapperAll.onTagUpdated(tag);
                lstWrapperStarred.onTagUpdated(tag);
                lstWrapperUnread.onTagUpdated(tag);
            }
            cur.close();
        }

        {
            final Cursor cur = resolver.query(Subscription.CONTENT_URI, SUBSCRIPTION_PROJECTION, null, null, null);
            for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                final Subscription sub = Subscription.fromCursor(cur);
                lstWrapperAll.onSubscriptionUpdated(sub);
                lstWrapperStarred.onSubscriptionUpdated(sub);
                lstWrapperUnread.onSubscriptionUpdated(sub);
            }
            cur.close();
        }
    }

    private void showSyncingProgress() {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm");
        sdf.setTimeZone(TimeZone.getDefault());
        final String sLastSync = dataMgr.getSettingByName(Setting.SETTING_ITEM_LIST_EXPIRE_TIME);
        final String text;
        if (sLastSync == null) {
            text = context.getString(R.string.TxtLastSync) + ": " + context.getString(R.string.TxtUnknown);
        } else {
            text = context.getString(R.string.TxtLastSync) + ": " + sdf.format(Long.valueOf(sLastSync));
        }
        showSyncingProgress(text, -1, -1);
    }

    private void showSyncingProgress(final String text, final int progress, final int maxProgress) {
        final ProgressBar pBar = (ProgressBar) view.findViewById(R.id.ProgressBarProcess);
        final TextView txt = (TextView) view.findViewById(R.id.TxtProgress);
        if (pBar != null && txt != null) {
            if (progress == -1 || maxProgress == -1) {
                pBar.setVisibility(View.GONE);
            } else {
                pBar.setVisibility(View.VISIBLE);
                pBar.setProgress(progress);
                pBar.setMax(maxProgress);
            }
            txt.setText(text);
        }
    }
}
