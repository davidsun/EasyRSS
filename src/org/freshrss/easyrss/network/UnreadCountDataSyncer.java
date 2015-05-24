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
import java.util.LinkedList;
import java.util.List;

import org.freshrss.easyrss.data.DataMgr;
import org.freshrss.easyrss.data.Setting;
import org.freshrss.easyrss.data.UnreadCount;
import org.freshrss.easyrss.data.parser.OnUnreadCountRetrievedListener;
import org.freshrss.easyrss.data.parser.UnreadCountJSONParser;
import org.freshrss.easyrss.network.url.UnreadCountURL;

import android.content.Context;

import com.fasterxml.jackson.core.JsonParseException;

public class UnreadCountDataSyncer extends AbsDataSyncer {
    private class UnreadCountListener implements OnUnreadCountRetrievedListener {
        final private List<UnreadCount> unreadCounts;

        public UnreadCountListener() {
            unreadCounts = new LinkedList<UnreadCount>();
        }

        public List<UnreadCount> getUnreadCounts() {
            return unreadCounts;
        }

        @Override
        public void onUnreadCountRetrieved(final UnreadCount count) {
            unreadCounts.add(count);
        }
    }

    public UnreadCountDataSyncer(final DataMgr dataMgr, final int networkConfig) {
        super(dataMgr, networkConfig);
    }

    @Override
    protected void finishSyncing() {
        /*
         * TODO Empty method: Do nothing here. The instance will be called by
         * other syncers.
         */
    }

    @Override
    public void startSyncing() throws DataSyncerException {
        syncUnreadCount();
    }

    private void syncUnreadCount() throws DataSyncerException {
        final Context context = dataMgr.getContext();
        if (!NetworkUtils.checkSyncingNetworkStatus(context, networkConfig)) {
            return;
        }
        final byte[] content = httpGetQueryByte(new UnreadCountURL(isHttpsConnection));
        final long curTime = System.currentTimeMillis();
        try {
            final UnreadCountJSONParser parser = new UnreadCountJSONParser(content);
            final UnreadCountListener listener = new UnreadCountListener();
            parser.parse(listener);
            dataMgr.updateUnreadCounts(listener.getUnreadCounts());
        } catch (final JsonParseException exception) {
            throw new DataSyncerException(exception);
        } catch (final IllegalStateException exception) {
            throw new DataSyncerException(exception);
        } catch (final IOException exception) {
            throw new DataSyncerException(exception);
        }
        dataMgr.removeOutdatedUnreadCounts(curTime);
        final String sUpdTime = dataMgr.getSettingByName(Setting.SETTING_GLOBAL_ITEM_UPDATE_TIME);
        final long updTime = (sUpdTime == null) ? 0 : Long.valueOf(sUpdTime);
        if (updTime < curTime) {
            dataMgr.updateSetting(new Setting(Setting.SETTING_GLOBAL_ITEM_UNREAD_COUNT, "0"));
        }
    }
}
