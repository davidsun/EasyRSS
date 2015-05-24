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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.fasterxml.jackson.core.JsonParseException;
import org.freshrss.easyrss.R;
import org.freshrss.easyrss.data.DataMgr;
import org.freshrss.easyrss.data.Setting;
import org.freshrss.easyrss.data.Subscription;
import org.freshrss.easyrss.data.parser.OnSubscriptionRetrievedListener;
import org.freshrss.easyrss.data.parser.SubscriptionJSONParser;
import org.freshrss.easyrss.data.readersetting.SettingSyncInterval;
import org.freshrss.easyrss.data.readersetting.SettingSyncMethod;
import org.freshrss.easyrss.network.url.SubscriptionIconUrl;
import org.freshrss.easyrss.network.url.SubscriptionListURL;

public class SubscriptionDataSyncer extends AbsDataSyncer {
    private class SyncerSubscriptionListener implements OnSubscriptionRetrievedListener {
        final private List<Subscription> subscriptions;

        public SyncerSubscriptionListener() {
            this.subscriptions = new LinkedList<Subscription>();
        }

        public List<Subscription> getSubscriptions() {
            return subscriptions;
        }

        public void onSubscriptionRetrieved(final Subscription sub) {
            subscriptions.add(sub);
        }
    }

    private static SubscriptionDataSyncer instance;

    private static synchronized void clearInstance() {
        instance = null;
    }

    public static synchronized SubscriptionDataSyncer getInstance(final DataMgr dataMgr, final int networkConfig) {
        if (instance == null) {
            instance = new SubscriptionDataSyncer(dataMgr, networkConfig);
        }
        return instance;
    }

    public static synchronized boolean hasInstance() {
        return instance != null;
    }

    private SubscriptionDataSyncer(final DataMgr dataMgr, final int networkConfig) {
        super(dataMgr, networkConfig);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else {
            return (obj instanceof SubscriptionDataSyncer);
        }
    }

    @Override
    protected void finishSyncing() {
        clearInstance();
    }

    @Override
    public void startSyncing() throws DataSyncerException {
        final String sExpTime = dataMgr.getSettingByName(Setting.SETTING_SUBSCRIPTION_LIST_EXPIRE_TIME);
        if (networkConfig != SettingSyncMethod.SYNC_METHOD_MANUAL
                && sExpTime != null
                && Long.valueOf(sExpTime) + new SettingSyncInterval(dataMgr).toSeconds() * 1000 - 10 * 60 * 1000 > System
                        .currentTimeMillis()) {
            return;
        }

        syncSubscriptions();
        syncSubscriptionIcons();

        dataMgr.updateSetting(new Setting(Setting.SETTING_SUBSCRIPTION_LIST_EXPIRE_TIME, System.currentTimeMillis()));
    }

    private void syncSubscriptionIcons() throws DataSyncerException {
        final Context context = dataMgr.getContext();
        if (!NetworkUtils.checkSyncingNetworkStatus(context, networkConfig)) {
            return;
        }
        final ContentResolver resolver = context.getContentResolver();
        final Cursor cur = resolver.query(Subscription.CONTENT_URI, new String[] { Subscription._UID,
                Subscription._ICON, Subscription._URL }, null, null, null);
        for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
            final String uid = cur.getString(0);
            final byte[] data = cur.getBlob(1);
            final String subUrl = cur.getString(2);
            if (subUrl != null && data == null) {
                final SubscriptionIconUrl fetchUrl = new SubscriptionIconUrl(isHttpsConnection, subUrl);
                try {
                    final byte[] iconData = httpGetQueryByte(fetchUrl);
                    final Bitmap icon = BitmapFactory.decodeByteArray(iconData, 0, iconData.length);
                    final int size = icon.getWidth() * icon.getHeight() * 2;
                    final ByteArrayOutputStream output = new ByteArrayOutputStream(size);
                    icon.compress(Bitmap.CompressFormat.PNG, 100, output);
                    output.flush();
                    output.close();
                    dataMgr.updateSubscriptionIconByUid(uid, output.toByteArray());
                } catch (final IOException exception) {
                    cur.close();
                    throw new DataSyncerException(exception);
                }
            }
        }
        cur.close();
    }

    private void syncSubscriptions() throws DataSyncerException {
        final Context context = dataMgr.getContext();
        if (!NetworkUtils.checkSyncingNetworkStatus(context, networkConfig)) {
            return;
        }
        notifyProgressChanged(context.getString(R.string.TxtSyncingSubscriptions), -1, -1);

        final InputStream stream = httpGetQueryStream(new SubscriptionListURL(isHttpsConnection));
        final SubscriptionJSONParser parser = new SubscriptionJSONParser(stream);
        final long curTime = System.currentTimeMillis();
        try {
            final SyncerSubscriptionListener listener = new SyncerSubscriptionListener();
            parser.parse(listener);
            dataMgr.addSubscriptions(listener.getSubscriptions());
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
        dataMgr.removeOutdatedSubscriptions(curTime);
    }
}
