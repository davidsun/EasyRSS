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

import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import org.freshrss.easyrss.R;
import org.freshrss.easyrss.data.DataMgr;
import org.freshrss.easyrss.data.DataUtils;
import org.freshrss.easyrss.data.Item;
import org.freshrss.easyrss.data.ItemState;
import org.freshrss.easyrss.data.Tag;
import org.freshrss.easyrss.data.readersetting.SettingDescendingItemsOrdering;
import org.freshrss.easyrss.data.readersetting.SettingFontSize;
import org.freshrss.easyrss.data.readersetting.SettingMarkAllAsReadConfirmation;
import org.freshrss.easyrss.data.readersetting.SettingSyncMethod;
import org.freshrss.easyrss.listadapter.AbsListItem;
import org.freshrss.easyrss.listadapter.ListAdapter;
import org.freshrss.easyrss.listadapter.ListItemEndEnabled;
import org.freshrss.easyrss.listadapter.ListItemItem;
import org.freshrss.easyrss.listadapter.ListItemTitle;
import org.freshrss.easyrss.listadapter.OnItemTouchListener;
import org.freshrss.easyrss.network.ItemDataSyncer;
import org.freshrss.easyrss.network.NetworkMgr;
import org.freshrss.easyrss.network.NetworkUtils;
import org.freshrss.easyrss.view.AbsViewCtrl;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Toast;

public class FeedViewCtrl extends AbsViewCtrl implements ItemListWrapperListener {
    private class FeedListAdapterListener implements OnItemTouchListener {
        private float lastX;
        private float lastY;
        private int motionSlop;

        public FeedListAdapterListener() {
            final ViewConfiguration configuration = ViewConfiguration.get(FeedViewCtrl.this.context);
            this.lastX = 0f;
            this.lastY = 0f;
            this.motionSlop = configuration.getScaledTouchSlop();
        }

        @Override
        public void onItemTouched(final ListAdapter adapter, final AbsListItem item, final MotionEvent event) {
            if (listener == null || !isAvailable || (item instanceof ListItemTitle)) {
                return;
            }
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (item instanceof ListItemItem) {
                    lastX = event.getX();
                    lastY = event.getY();
                    final Message msg = handler.obtainMessage(MSG_ITEM_LONG_CLICK, FeedViewCtrl.this);
                    final Bundle bundle = new Bundle();
                    bundle.putString(INTENT_KEY_ID, item.getId());
                    msg.setData(bundle);
                    handler.sendMessageDelayed(msg, 600);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (item instanceof ListItemItem) {
                    handler.removeMessages(MSG_ITEM_LONG_CLICK);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (item instanceof ListItemItem) {
                    final float curX = event.getX();
                    final float curY = event.getY();
                    if (Math.abs(curY - lastY) > motionSlop || Math.abs(curX - lastX) > motionSlop) {
                        handler.removeMessages(MSG_ITEM_LONG_CLICK);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (item instanceof ListItemItem && handler.hasMessages(MSG_ITEM_LONG_CLICK)) {
                    handler.removeMessages(MSG_ITEM_LONG_CLICK);
                    listener.onItemSelected(item.getId());
                } else if (item instanceof ListItemEndEnabled) {
                    lstWrapper.updateItemEndLoading();
                    NetworkMgr.getInstance().startSync(syncer);
                    Toast.makeText(context, R.string.MsgLoadingMoreItems, Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
            }
        }
    }

    final static private Handler handler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            if (msg.what == MSG_ITEM_LONG_CLICK && msg.obj instanceof FeedViewCtrl) {
                final FeedViewCtrl viewCtrl = (FeedViewCtrl) msg.obj;
                final Integer pos = viewCtrl.lstWrapper.getAdapter().getItemLocationById(
                        msg.getData().getString(INTENT_KEY_ID));
                if (pos == null) {
                    return;
                }
                final AbsListItem absItem = viewCtrl.lstWrapper.getAdapter().getItem(pos);
                if (!(absItem instanceof ListItemItem)) {
                    return;
                }
                final ListItemItem item = (ListItemItem) absItem;
                final AlertDialog.Builder builder = new AlertDialog.Builder(viewCtrl.context);
                final String[] popup = new String[4];
                popup[0] = viewCtrl.context
                        .getString(item.isRead() ? R.string.TxtMarkAsUnread : R.string.TxtMarkAsRead);
                popup[1] = viewCtrl.context.getString(R.string.TxtMarkPreviousAsRead);
                popup[2] = viewCtrl.context.getString(item.isStarred() ? R.string.TxtRemoveStar : R.string.TxtAddStar);
                popup[3] = viewCtrl.context.getString(R.string.TxtSendTo);
                builder.setItems(popup, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int id) {
                        switch (id) {
                        case 0:
                            if (item.isRead()) {
                                viewCtrl.dataMgr.markItemAsUnreadWithTransactionByUid(item.getId());
                            } else {
                                viewCtrl.dataMgr.markItemAsReadWithTransactionByUid(item.getId());
                            }
                            NetworkMgr.getInstance().startImmediateItemStateSyncing();
                            break;
                        case 1:
                            final ProgressDialog pDialog = ProgressDialog.show(new ContextThemeWrapper(
                                    viewCtrl.context, android.R.style.Theme_DeviceDefault_Dialog), viewCtrl.context
                                    .getString(R.string.TxtWorking), viewCtrl.context
                                    .getString(R.string.TxtMarkingPreviousItemsAsRead));
                            final Thread thread = new Thread() {
                                @Override
                                public void run() {
                                    for (int i = 0; i <= pos; i++) {
                                        final AbsListItem item = viewCtrl.lstWrapper.getAdapter().getItem(i);
                                        if (item instanceof ListItemItem) {
                                            final ListItemItem pItem = (ListItemItem) viewCtrl.lstWrapper.getAdapter()
                                                    .getItem(i);
                                            if (!pItem.isRead()) {
                                                viewCtrl.dataMgr.markItemAsReadWithTransactionByUid(pItem.getId());
                                            }
                                        }
                                    }
                                    NetworkMgr.getInstance().startImmediateItemStateSyncing();
                                    handler.sendMessage(handler.obtainMessage(MSG_DISMISS_DIALOG, pDialog));
                                }
                            };
                            thread.setPriority(Thread.MIN_PRIORITY);
                            thread.start();
                            break;
                        case 2:
                            viewCtrl.dataMgr.markItemAsStarredWithTransactionByUid(item.getId(), !item.isStarred());
                            Toast.makeText(viewCtrl.context,
                                    item.isStarred() ? R.string.MsgUnstarred : R.string.MsgStarred, Toast.LENGTH_LONG)
                                    .show();
                            NetworkMgr.getInstance().startImmediateItemStateSyncing();
                            break;
                        case 3:
                            DataUtils.sendTo(viewCtrl.context, viewCtrl.dataMgr.getItemByUid(item.getId()));
                            break;
                        default:
                        }
                    }
                });
                builder.show();
            } else if (msg.what == MSG_DISMISS_DIALOG && msg.obj instanceof ProgressDialog) {
                ((ProgressDialog) msg.obj).dismiss();
            } else if (msg.what == MSG_QUIT && msg.obj instanceof FeedViewCtrl) {
                final FeedViewCtrl viewCtrl = (FeedViewCtrl) msg.obj;
                if (viewCtrl.listener != null) {
                    viewCtrl.listener.onBackNeeded();
                }
            }
        }
    };

    final private static String INTENT_KEY_ID = "id";
    final private static String ITEM_PROJECTION[] = new String[] { Item._UID, Item._TITLE, ItemState._ISREAD,
            ItemState._ISSTARRED, Item._TIMESTAMP, Item._SOURCETITLE };

    final private static int MSG_DISMISS_DIALOG = 1;
    final private static int MSG_ITEM_LONG_CLICK = 0;
    final private static int MSG_QUIT = 2;

    private static String appendCondition(final String condition, final String newCondition) {
        return (condition.length() == 0) ? newCondition : (condition + " AND " + newCondition);
    }

    private static void appendCondition(final StringBuilder builder, final String newCondition) {
        if (builder.length() > 0) {
            builder.append(" AND ");
        }
        builder.append(newCondition);
    }

    private boolean isAvailable;
    final private boolean isDecendingOrdering;
    private boolean isEnd;
    private String lastDateString;
    private long lastTimestamp;
    final private ItemListWrapper lstWrapper;
    private ItemDataSyncer syncer;
    private final String uid;
    private final int viewType;

    public FeedViewCtrl(final DataMgr dataMgr, final Context context, final String uid, final int viewType) {
        super(dataMgr, R.layout.feed, context);

        final int fontSize = new SettingFontSize(dataMgr).getData();
        final ListView feedList = (ListView) view.findViewById(R.id.FeedList);

        this.lstWrapper = new ItemListWrapper(feedList, fontSize);
        this.isDecendingOrdering = new SettingDescendingItemsOrdering(dataMgr).getData();
        this.uid = uid;
        this.viewType = viewType;
        this.isAvailable = false;
        this.isEnd = false;
        this.lastTimestamp = 0;

        lstWrapper.setListener(this);
        lstWrapper.setAdapterListener(new FeedListAdapterListener());
    }

    private String getCondition(final long timestamp) {
        final StringBuilder builder = new StringBuilder();
        if (viewType == Home.VIEW_TYPE_UNREAD) {
            appendCondition(builder, ItemState._ISREAD + "=0");
        } else if (viewType == Home.VIEW_TYPE_STARRED) {
            appendCondition(builder, ItemState._ISSTARRED + "=1");
        }
        if (uid.length() > 0 && !DataUtils.isTagUid(uid)) {
            appendCondition(builder, Item._SOURCEURI + "=\"" + uid + "\"");
        }
        if (timestamp > 0) {
            if (isDecendingOrdering) {
                appendCondition(builder, Item._TIMESTAMP + "<" + timestamp);
            } else {
                appendCondition(builder, Item._TIMESTAMP + ">" + timestamp);
            }
        }
        return builder.toString();
    }

    public ListItemItem getLastItem(final String uid) {
        final ListAdapter adapter = lstWrapper.getAdapter();
        int location = adapter.getItemLocationById(uid) - 1;
        while (location >= 0 && !(adapter.getItem(location) instanceof ListItemItem)) {
            location--;
        }
        return (location < 0) ? null : (ListItemItem) adapter.getItem(location);
    }

    public ListItemItem getNextItem(final String uid) {
        final ListAdapter adapter = lstWrapper.getAdapter();
        int location = adapter.getItemLocationById(uid) + 1;
        if (location + 10 >= adapter.getCount()) {
            showItemList();
        }
        while (location < adapter.getCount() && !(adapter.getItem(location) instanceof ListItemItem)) {
            location++;
            if (location >= adapter.getCount()) {
                showItemList();
            }
        }
        return (location >= adapter.getCount()) ? null : (ListItemItem) adapter.getItem(location);
    }

    public String getUid() {
        return uid;
    }

    public int getViewType() {
        return viewType;
    }

    @Override
    public void handleOnSyncFinished(final String syncerType, final boolean succeeded) {
        if (syncerType.equals(ItemDataSyncer.class.getName())) {
            isEnd = false;
            showItemList();
        }
    }

    private void markAllAsRead() {
        dataMgr.removeOnItemUpdatedListener(lstWrapper);
        final ProgressDialog pDialog = ProgressDialog.show(new ContextThemeWrapper(context,
                android.R.style.Theme_DeviceDefault_Dialog), context.getString(R.string.TxtWorking),
                context.getString(R.string.TxtMarkingAllItemsAsRead));
        final Thread thread = new Thread() {
            @Override
            public void run() {
                final ContentResolver resolver = context.getContentResolver();
                String condition = getCondition(0);
                condition = appendCondition(condition, ItemState._ISREAD + "=0");
                @SuppressWarnings("deprecation")
                final Uri uri = DataUtils.isTagUid(uid) ? Uri.withAppendedPath(Tag.CONTENT_URI,
                        "items/" + URLEncoder.encode(uid)) : Item.CONTENT_URI;
                final Cursor cur = resolver.query(uri, null, condition, null, null);
                final List<Item> items = new LinkedList<Item>();
                for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                    items.add(Item.fromCursor(cur));
                }
                cur.close();
                dataMgr.markItemsAsReadWithTransaction(items);
                NetworkMgr.getInstance().startImmediateItemStateSyncing();
                handler.sendMessage(handler.obtainMessage(MSG_DISMISS_DIALOG, pDialog));
                handler.sendMessage(handler.obtainMessage(MSG_QUIT, FeedViewCtrl.this));
            }
        };
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    @Override
    public void onActivate() {
        this.isAvailable = true;
    }

    @Override
    public void onCreate() {
        NetworkMgr.getInstance().addListener(this);
        dataMgr.addOnItemUpdatedListener(lstWrapper);

        showItemList();

        final View markAll = this.view.findViewById(R.id.BtnMarkAllAsRead);
        markAll.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                final LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context,
                        android.R.style.Theme_DeviceDefault_Dialog));
                final SettingMarkAllAsReadConfirmation sMark = new SettingMarkAllAsReadConfirmation(dataMgr);
                if (sMark.getData()) {
                    final View popupView = inflater.inflate(R.layout.mark_all_as_read_popup, null);
                    final CheckBox checkBox = (CheckBox) popupView.findViewById(R.id.CheckBoxDontShowAgain);
                    checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                            popupView.findViewById(R.id.Hint).setVisibility(View.VISIBLE);
                            sMark.setData(dataMgr, !isChecked);
                            dataMgr.updateSetting(sMark.toSetting());
                        }
                    });
                    builder.setIcon(android.R.drawable.ic_dialog_info);
                    builder.setTitle(R.string.TxtConfirmation);
                    builder.setView(popupView);
                    builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            markAllAsRead();
                        }
                    });
                    builder.setNegativeButton(android.R.string.no, null);
                    builder.show();
                } else {
                    markAllAsRead();
                }
            }
        });

        final View btnBack = this.view.findViewById(R.id.BtnBackHome);
        btnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (listener != null) {
                    listener.onBackNeeded();
                }
            }
        });
    }

    @Override
    public void onDeactivate() {
        this.isAvailable = false;
    }

    @Override
    public void onDestory() {
        NetworkMgr.getInstance().removeListener(this);
        dataMgr.removeOnItemUpdatedListener(lstWrapper);
    }

    @Override
    public void onNeedMoreItems() {
        showItemList();
    }

    private void showItemList() {
        if (isEnd) {
            return;
        }
        lstWrapper.removeItemEnd();
        final ContentResolver resolver = context.getContentResolver();
        @SuppressWarnings("deprecation")
        final Uri uri = DataUtils.isTagUid(uid) ? Uri.withAppendedPath(Tag.CONTENT_URI,
                "items/" + URLEncoder.encode(uid)) : Item.CONTENT_URI;
        final Cursor cur = resolver.query(uri, ITEM_PROJECTION, getCondition(lastTimestamp), null, Item._TIMESTAMP
                + ((isDecendingOrdering) ? " DESC LIMIT 20" : " LIMIT 20"));
        int count = 0;
        for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
            final Item item = Item.fromCursor(cur);
            count++;
            lastTimestamp = item.getTimestamp();
            final String curDateString = Utils.timestampToTimeAgo(context, item.getTimestamp());
            if (!curDateString.equals(lastDateString)) {
                String s;
                switch (viewType) {
                case Home.VIEW_TYPE_ALL:
                    s = ListItemItem.ITEM_TITLE_TYPE_ALL;
                    break;
                case Home.VIEW_TYPE_STARRED:
                    s = ListItemItem.ITEM_TITLE_TYPE_STARRED;
                    break;
                case Home.VIEW_TYPE_UNREAD:
                    s = ListItemItem.ITEM_TITLE_TYPE_UNREAD;
                    break;
                default:
                    s = "";
                }
                s += curDateString;
                lstWrapper.updateTitle(s, curDateString);
                lastDateString = curDateString;
            }
            lstWrapper.updateItem(item);
        }
        cur.close();
        if (count < 20) {
            if (!isDecendingOrdering || (viewType == Home.VIEW_TYPE_STARRED && uid.length() > 0)) {
                lstWrapper.updateItemEndDisabled();
            } else {
                updateLoadMore();
            }
            isEnd = true;
        }
    }

    private void updateLoadMore() {
        if (syncer == null) {
            final SettingSyncMethod sSync = new SettingSyncMethod(dataMgr);
            final int syncingMethod = NetworkUtils.checkSyncingNetworkStatus(context, sSync.getData()) ? sSync
                    .getData() : SettingSyncMethod.SYNC_METHOD_MANUAL;
            final int count = lstWrapper.getAdapter().getCount();
            final long time = (count > 0) ? ((ListItemItem) lstWrapper.getAdapter().getItem(count - 1)).getTimestamp() / 1000000 - 1
                    : 0;
            if (viewType == Home.VIEW_TYPE_STARRED) {
                syncer = ItemDataSyncer.getInstance(dataMgr, syncingMethod, "user/-/state/com.google/starred", time,
                        false);
            } else {
                syncer = ItemDataSyncer.getInstance(dataMgr, syncingMethod, uid, time,
                        (viewType == Home.VIEW_TYPE_UNREAD));
            }
        }
        if (syncer.isEnd()) {
            lstWrapper.updateItemEndDisabled();
        } else if (syncer.isRunning()) {
            lstWrapper.updateItemEndLoading();
        } else {
            final SettingSyncMethod sSync = new SettingSyncMethod(dataMgr);
            final int syncingMethod = NetworkUtils.checkSyncingNetworkStatus(context, sSync.getData()) ? sSync
                    .getData() : SettingSyncMethod.SYNC_METHOD_MANUAL;
            if (syncingMethod != SettingSyncMethod.SYNC_METHOD_MANUAL) {
                NetworkMgr.getInstance().startSync(syncer);
                lstWrapper.updateItemEndLoading();
            } else {
                lstWrapper.updateItemEndEnabled();
            }
        }
    }
}
