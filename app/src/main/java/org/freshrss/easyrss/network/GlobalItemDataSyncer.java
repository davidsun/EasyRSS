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

package org.freshrss.easyrss.network;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;

import com.fasterxml.jackson.core.JsonParseException;

import org.freshrss.easyrss.NotificationMgr;
import org.freshrss.easyrss.R;
import org.freshrss.easyrss.data.DataMgr;
import org.freshrss.easyrss.data.DataUtils;
import org.freshrss.easyrss.data.Item;
import org.freshrss.easyrss.data.ItemId;
import org.freshrss.easyrss.data.Setting;
import org.freshrss.easyrss.data.parser.ItemIdJSONParser;
import org.freshrss.easyrss.data.parser.ItemJSONParser;
import org.freshrss.easyrss.data.parser.OnItemIdRetrievedListener;
import org.freshrss.easyrss.data.parser.OnItemRetrievedListener;
import org.freshrss.easyrss.data.readersetting.SettingMaxItems;
import org.freshrss.easyrss.data.readersetting.SettingNotificationOn;
import org.freshrss.easyrss.data.readersetting.SettingSyncInterval;
import org.freshrss.easyrss.data.readersetting.SettingSyncMethod;
import org.freshrss.easyrss.network.url.StreamContentsURL;
import org.freshrss.easyrss.network.url.StreamIdsURL;

public class GlobalItemDataSyncer extends AbsDataSyncer implements DataSyncerListener {
    private class SyncAllItemsItemListener implements OnItemRetrievedListener {
        final private List<Item> items;
        private String continuation;
        private long oldestTimestamp;
        private long newestTimestamp;

        public SyncAllItemsItemListener() {
            this.oldestTimestamp = (1L << 62L);
            this.newestTimestamp = 0;
            this.items = new LinkedList<Item>();
        }

        public String getContinuation() {
            return continuation;
        }

        public List<Item> getItems() {
            return items;
        }

        public long getNewestTimestamp() {
            return newestTimestamp;
        }

        public long getOldestTimestamp() {
            return oldestTimestamp;
        }

        @Override
        public void onItemRetrieved(final Item item) throws IOException {
            DataUtils.writeItemToFile(item);
            items.add(item);
            newestTimestamp = Math.max(item.getTimestamp(), newestTimestamp);
            oldestTimestamp = Math.min(item.getTimestamp(), oldestTimestamp);
        }

        @Override
        public void onListContinuationRetrieved(final String continuation) {
            this.continuation = continuation;
        }
    }

    private static GlobalItemDataSyncer instance;

    private static synchronized void clearInstance() {
        instance = null;
    }

    public static synchronized GlobalItemDataSyncer getInstance(final DataMgr dataMgr, final int networkConfig) {
        if (instance == null) {
            instance = new GlobalItemDataSyncer(dataMgr, networkConfig);
        }
        return instance;
    }

    public static synchronized boolean hasInstance() {
        return instance != null;
    }

    private GlobalItemDataSyncer(final DataMgr dataMgr, final int networkConfig) {
        super(dataMgr, networkConfig);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else {
            return (obj instanceof GlobalItemDataSyncer);
        }
    }

    @Override
    protected void finishSyncing() {
        clearInstance();
    }

    @Override
    public void onProgressChanged(final String text, final int progress, final int maxProgress) {
        notifyProgressChanged(text, progress, maxProgress);
    }

    @Override
    protected void startSyncing() throws DataSyncerException {
        final String sExpTime = dataMgr.getSettingByName(Setting.SETTING_ITEM_LIST_EXPIRE_TIME);
        if (networkConfig != SettingSyncMethod.SYNC_METHOD_MANUAL
                && sExpTime != null
                && Long.valueOf(sExpTime) + new SettingSyncInterval(dataMgr).toSeconds() * 1000 - 10 * 60 * 1000 > System
                        .currentTimeMillis()) {
            return;
        }

        syncReadStatus();
        syncAllItems();
        syncUnreadCount();
        syncUnreadItems();
        NetworkMgr.getInstance().startSyncItemContent();

        final SettingNotificationOn sNotification = new SettingNotificationOn(dataMgr);
        if (sNotification.getData()) {
            final String sSetting = dataMgr.getSettingByName(Setting.SETTING_GLOBAL_ITEM_UNREAD_COUNT);
            final int unreadCount = (sSetting == null) ? 0 : Integer.valueOf(sSetting);
            if (unreadCount > 0) {
                NotificationMgr.getInstance().showNewItemsNotification(unreadCount);
            }
        }

        dataMgr.updateSetting(new Setting(Setting.SETTING_ITEM_LIST_EXPIRE_TIME, System.currentTimeMillis()));
    }

    private void syncAllItems() throws DataSyncerException {
        final Context context = dataMgr.getContext();
        if (!NetworkUtils.checkSyncingNetworkStatus(context, networkConfig)) {
            return;
        }
        int count = 0;
        String sSetting = dataMgr.getSettingByName(Setting.SETTING_GLOBAL_NEWEST_ITEM_TIMESTAMP);
        long newestTimestamp = (sSetting == null) ? 0 : Long.valueOf(sSetting);
        long oldestTimestamp = (sSetting == null) ? (1L << 62L) : Long.valueOf(sSetting);
        long newNewestTimestamp = newestTimestamp;
        long newOldestTimestamp = (1L << 62L);
        //long lastTimestamp = (1L << 62L);
        String continuation = null;
        do {
            notifyProgressChanged(context.getString(R.string.TxtSyncingAllItems), count, GLOBAL_ITEMS_LIMIT);
            final int limit = (count == 0) ? 5 : ITEM_LIST_QUERY_LIMIT;
            final InputStream stream = httpGetQueryStream(new StreamContentsURL(isHttpsConnection, "", continuation, 0,
                    limit, false));
            try {
                final ItemJSONParser parser = new ItemJSONParser(stream);
                final SyncAllItemsItemListener listener = new SyncAllItemsItemListener();
                parser.parse(listener);
                newOldestTimestamp = Math.min(newOldestTimestamp, listener.getOldestTimestamp());
                newNewestTimestamp = Math.max(newNewestTimestamp, listener.getNewestTimestamp());
                //lastTimestamp = Math.min(lastTimestamp, listener.getOldestTimestamp());
                continuation = listener.getContinuation();
                final List<Item> items = listener.getItems();
                dataMgr.addItems(items);
                count += items.size();
                if (newOldestTimestamp <= newestTimestamp || items.size() < limit ||
                    continuation == null || continuation.length() <= 0) {
                    break;
                }
            } catch (final JsonParseException exception) {
                exception.printStackTrace();
                throw new DataSyncerException(exception);
            } catch (final IllegalStateException exception) {
                exception.printStackTrace();
                throw new DataSyncerException(exception);
            } catch (final IOException exception) {
                exception.printStackTrace();
                throw new DataSyncerException(exception);
            } finally {
                try {
                    stream.close();
                } catch (final IOException exception) {
                    exception.printStackTrace();
                }
            }
        } while (count < GLOBAL_ITEMS_LIMIT);
        notifyProgressChanged(context.getString(R.string.TxtSyncingAllItems), -1, -1);
        if (newOldestTimestamp <= newestTimestamp) {
            oldestTimestamp = Math.min(oldestTimestamp, newOldestTimestamp);
        }
        newestTimestamp = newNewestTimestamp;

        dataMgr.removeOutdatedItemsWithLimit(new SettingMaxItems(dataMgr).getData());
        dataMgr.updateSetting(new Setting(Setting.SETTING_GLOBAL_NEWEST_ITEM_TIMESTAMP, String.valueOf(newestTimestamp)));
    }

    private void syncReadStatus() throws DataSyncerException {
        final TransactionDataSyncer syncer = TransactionDataSyncer.getInstance(dataMgr, networkConfig);
        syncer.setListener(this);
        syncer.sync();
        syncer.setListener(null);
    }

    private void syncUnreadCount() throws DataSyncerException {
        final UnreadCountDataSyncer syncer = new UnreadCountDataSyncer(dataMgr, networkConfig);
        syncer.setListener(this);
        syncer.sync();
        syncer.setListener(null);
    }

    private void syncUnreadItems() throws DataSyncerException {
        final Context context = dataMgr.getContext();
        if (!NetworkUtils.checkSyncingNetworkStatus(context, networkConfig)) {
            return;
        }
        String sSetting = dataMgr.getSettingByName(Setting.SETTING_GLOBAL_ITEM_UNREAD_COUNT);
        final int unreadCount = (sSetting == null) ? 0 : Integer.valueOf(sSetting);
        if (unreadCount == 0) {
            dataMgr.markAllItemsAsRead();
            return;
        } else if (dataMgr.calcGlobalUnreadItemCount() == unreadCount) {
            return;
        }

        notifyProgressChanged(context.getString(R.string.TxtSyncingUnreadItems), -1, -1);
        final InputStream stream = httpGetQueryStream(new StreamIdsURL(isHttpsConnection,
                "user/-/state/com.google/reading-list", GLOBAL_ITEM_IDS_LIMIT, true));
        try {
            final ItemIdJSONParser parser = new ItemIdJSONParser(stream);
            final List<ItemId> itemIds = new ArrayList<ItemId>();
            parser.parse(new OnItemIdRetrievedListener() {
                @Override
                public void onItemIdRetrieved(final ItemId itemId) throws IOException {
                    itemIds.add(itemId);
                }
            });
            for (int i = 0; i < itemIds.size(); i += 50) {
                if (i + 50 <= itemIds.size()) {
                    dataMgr.markItemsAsReadItemIds(itemIds, i, i + 50);
                } else if (itemIds.size() < GLOBAL_ITEMS_LIMIT) {
                    dataMgr.markItemsAsReadItemIds(itemIds, i, itemIds.size(), true);
                } else {
                    dataMgr.markItemsAsReadItemIds(itemIds, i, itemIds.size());
                }
                if (dataMgr.calcGlobalUnreadItemCount() == unreadCount) {
                    break;
                }
            }
        } catch (final JsonParseException exception) {
            exception.printStackTrace();
            throw new DataSyncerException(exception);
        } catch (final IllegalStateException exception) {
            exception.printStackTrace();
            throw new DataSyncerException(exception);
        } catch (final IOException exception) {
            exception.printStackTrace();
            throw new DataSyncerException(exception);
        } finally {
            try {
                stream.close();
            } catch (final IOException exception) {
                exception.printStackTrace();
            }
        }
    }
}
