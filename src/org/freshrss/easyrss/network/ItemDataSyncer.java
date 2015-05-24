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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Pair;

import com.fasterxml.jackson.core.JsonParseException;
import org.freshrss.easyrss.R;
import org.freshrss.easyrss.data.DataMgr;
import org.freshrss.easyrss.data.DataUtils;
import org.freshrss.easyrss.data.Item;
import org.freshrss.easyrss.data.parser.ItemJSONParser;
import org.freshrss.easyrss.data.parser.OnItemRetrievedListener;
import org.freshrss.easyrss.data.readersetting.SettingMaxItems;
import org.freshrss.easyrss.network.url.StreamContentsURL;

public class ItemDataSyncer extends AbsDataSyncer implements DataSyncerListener {
    private class ItemListener implements OnItemRetrievedListener {
        final private List<Item> items;
        private String continuation;

        public ItemListener() {
            this.items = new LinkedList<Item>();
        }

        public String getContinuation() {
            return continuation;
        }

        public List<Item> getItems() {
            return items;
        }

        @Override
        public void onItemRetrieved(final Item item) throws IOException {
            DataUtils.writeItemToFile(item);
            items.add(item);
        }

        @Override
        public void onListContinuationRetrieved(final String continuation) {
            this.continuation = continuation;
        }
    }

    final private long initTime;
    final private long newestItemTime;
    final private boolean isUnread;
    final private String uid;
    private Boolean isEnd;
    private String continuation;

    private static Map<Pair<String, Boolean>, ItemDataSyncer> instances = new HashMap<Pair<String, Boolean>, ItemDataSyncer>();

    public static void clearInstance(final ItemDataSyncer instance) {
        synchronized (instances) {
            instances.remove(Pair.create(instance.getUid(), instance.isUnread()));
        }
    }

    public static ItemDataSyncer getInstance(final DataMgr dataMgr, final int networkConfig, final String uid,
            final long newestItemTime, final boolean isUnread) {
        synchronized (instances) {
            ItemDataSyncer ret = instances.get(Pair.create(uid, isUnread));
            if (ret == null) {
                ret = new ItemDataSyncer(dataMgr, networkConfig, uid, newestItemTime, isUnread);
                instances.put(Pair.create(uid, isUnread), ret);
            }
            return ret;
        }
    }

    private ItemDataSyncer(final DataMgr dataMgr, final int networkConfig, final String uid, final long newestItemTime,
            final boolean isUnread) {
        super(dataMgr, networkConfig);

        this.initTime = System.currentTimeMillis();
        this.uid = uid;
        this.newestItemTime = newestItemTime;
        this.isUnread = isUnread;
        this.isEnd = false;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof ItemDataSyncer)) {
            return false;
        }
        final ItemDataSyncer syncer = (ItemDataSyncer) obj;
        return uid.equals(syncer.uid) && isUnread == syncer.isUnread;
    }

    @Override
    protected void finishSyncing() {
        if (System.currentTimeMillis() - initTime >= 1000 * 3600) {
            clearInstance(this);
        }
    }

    public long getNewestItemTime() {
        return newestItemTime;
    }

    public String getUid() {
        return uid;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public boolean isUnread() {
        return isUnread;
    }

    @Override
    public void onProgressChanged(final String text, final int progress, final int maxProgress) {
        notifyProgressChanged(text, progress, maxProgress);
    }

    private void setEnd(final boolean isEnd) {
        synchronized (this.isEnd) {
            this.isEnd = isEnd;
        }
    }

    @Override
    public void startSyncing() throws DataSyncerException {
        if (isEnd()) {
            return;
        }
        syncReadStatus();
        syncItems();
        syncItemContent();
    }

    private void syncItemContent() {
        NetworkMgr.getInstance().startSyncItemContent();
    }

    private void syncItems() throws DataSyncerException {
        final Context context = dataMgr.getContext();
        if (!NetworkUtils.checkSyncingNetworkStatus(context, networkConfig)) {
            return;
        }
        notifyProgressChanged(context.getString(R.string.TxtSyncingAllItems), -1, -1);

        final InputStream stream = httpGetQueryStream(new StreamContentsURL(isHttpsConnection, uid, continuation,
                newestItemTime, ITEM_LIST_QUERY_LIMIT, isUnread));
        try {
            final ItemJSONParser parser = new ItemJSONParser(stream);
            final ItemListener listener = new ItemListener();
            parser.parse(listener);
            continuation = listener.getContinuation();
            final List<Item> items = listener.getItems();
            dataMgr.addItems(items);
            if (items.size() < ITEM_LIST_QUERY_LIMIT) {
                setEnd(true);
            }
        } catch (final JsonParseException exception) {
            throw new DataSyncerException(exception);
        } catch (final IllegalStateException exception) {
            throw new DataSyncerException(exception);
        } catch (final IOException exception) {
            throw new DataSyncerException(exception);
        } finally {
            try {
                stream.close();
            } catch (final IOException exception) {
                exception.printStackTrace();
            }
        }
        dataMgr.removeOutdatedItemsWithLimit(new SettingMaxItems(dataMgr).getData());
    }

    private void syncReadStatus() throws DataSyncerException {
        final TransactionDataSyncer syncer = TransactionDataSyncer.getInstance(dataMgr, networkConfig);
        syncer.setListener(this);
        syncer.sync();
        syncer.setListener(null);
    }
}
