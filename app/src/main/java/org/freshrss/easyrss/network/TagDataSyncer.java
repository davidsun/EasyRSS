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
import java.util.LinkedList;
import java.util.List;

import android.content.Context;

import com.fasterxml.jackson.core.JsonParseException;
import org.freshrss.easyrss.R;
import org.freshrss.easyrss.data.DataMgr;
import org.freshrss.easyrss.data.Setting;
import org.freshrss.easyrss.data.Tag;
import org.freshrss.easyrss.data.parser.OnTagRetrievedListener;
import org.freshrss.easyrss.data.parser.TagJSONParser;
import org.freshrss.easyrss.data.readersetting.SettingSyncInterval;
import org.freshrss.easyrss.data.readersetting.SettingSyncMethod;
import org.freshrss.easyrss.network.url.TagListURL;

public class TagDataSyncer extends AbsDataSyncer {
    private class TagListener implements OnTagRetrievedListener {
        final private List<Tag> tags;

        public TagListener() {
            tags = new LinkedList<Tag>();
        }

        public List<Tag> getTags() {
            return tags;
        }

        public void onTagRetrieved(final Tag tag) {
            tags.add(tag);
        }
    }

    private static TagDataSyncer instance;

    private static synchronized void clearInstance() {
        instance = null;
    }

    public static synchronized TagDataSyncer getInstance(final DataMgr dataMgr, final int networkConfig) {
        if (instance == null) {
            instance = new TagDataSyncer(dataMgr, networkConfig);
        }
        return instance;
    }

    public static synchronized boolean hasInstance() {
        return instance != null;
    }

    private TagDataSyncer(final DataMgr dataMgr, final int networkConfig) {
        super(dataMgr, networkConfig);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else {
            return (obj instanceof TagDataSyncer);
        }
    }

    @Override
    protected void finishSyncing() {
        clearInstance();
    }

    @Override
    public void startSyncing() throws DataSyncerException {
        final String sExpTime = dataMgr.getSettingByName(Setting.SETTING_TAG_LIST_EXPIRE_TIME);
        if (networkConfig != SettingSyncMethod.SYNC_METHOD_MANUAL
                && sExpTime != null
                && Long.valueOf(sExpTime) + new SettingSyncInterval(dataMgr).toSeconds() * 1000 - 10 * 60 * 1000 > System
                        .currentTimeMillis()) {
            return;
        }

        syncTags();

        dataMgr.updateSetting(new Setting(Setting.SETTING_TAG_LIST_EXPIRE_TIME, System.currentTimeMillis()));
    }

    private void syncTags() throws DataSyncerException {
        final Context context = dataMgr.getContext();
        if (!NetworkUtils.checkSyncingNetworkStatus(context, networkConfig)) {
            return;
        }
        notifyProgressChanged(context.getString(R.string.TxtSyncingTags), -1, -1);

        final InputStream stream = httpGetQueryStream(new TagListURL(isHttpsConnection));
        final TagJSONParser parser = new TagJSONParser(stream);
        final long curTime = System.currentTimeMillis();
        try {
            final TagListener listener = new TagListener();
            parser.parse(listener);
            dataMgr.addTags(listener.getTags());
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
        dataMgr.removeOutdatedTags(curTime);
    }
}
