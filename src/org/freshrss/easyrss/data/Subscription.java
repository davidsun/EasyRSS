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

package org.freshrss.easyrss.data;

import java.util.LinkedList;
import java.util.List;

import org.freshrss.easyrss.Utils;


import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

public class Subscription implements Entity {
    public static final String TABLE_NAME = "subscriptions";

    public static final Uri CONTENT_URI = Uri.parse(DataProvider.SUBSCRIPTION_CONTENT_URI);

    public static final String _UID = "uid";
    public static final String _URL = "url";
    public static final String _TITLE = "title";
    public static final String _ICON = "icon";
    public static final String _UNREADCOUNT = "unreadCount";
    public static final String _UPDATETIME = "updateTime";
    public static final String _SORTID = "sortid";
    public static final String _FIRSTITEMMSEC = "firstItemMsec";

    public static final String[] COLUMNS = { _UID, _URL, _TITLE, _ICON, _UNREADCOUNT, _UPDATETIME, _SORTID,
            _FIRSTITEMMSEC };
    public static final String[] COLUMNS_TYPE = { "TEXT PRIMARY KEY", "TEXT", "TEXT NOT NULL", "BOLB",
            "INTEGER NOT NULL DEFAULT 0", "INTEGER NOT NULL DEFAULT 0", "TEXT", "INTEGER NOT NULL DEFAULT 0" };
    public static final String[][] INDEX_COLUMNS = { { _UID }, { _UPDATETIME } };

    public static Subscription fromCursor(final Cursor cur) {
        return new Subscription(Utils.getStringFromCursor(cur, _UID), Utils.getStringFromCursor(cur, _URL),
                Utils.getStringFromCursor(cur, _TITLE), Utils.getBlobFromCursor(cur, _ICON), Utils.getIntFromCursor(
                        cur, _UNREADCOUNT), Utils.getLongFromCursor(cur, _UPDATETIME), Utils.getStringFromCursor(cur,
                        _SORTID), Utils.getLongFromCursor(cur, _FIRSTITEMMSEC));
    }

    private String uid;
    private String title;
    private String url;
    private String sortId;
    private Bitmap icon;
    private List<String> tags;
    private int unreadCount;
    private long updateTime;
    private long firstItemMsec;

    public Subscription() {
        init(null, null, null, null, null, null, null, null, null);
    }

    private Subscription(final String uid, final String url, final String title, final byte[] icon,
            final int unreadCount, final long updateTime, final String sortId, final long firstItemMsec) {
        initFromByteArray(uid, url, title, icon, null, unreadCount, updateTime, sortId, firstItemMsec);
    }

    public void addTag(final String tag) {
        tags.add(tag);
    }

    @Override
    public void clear() {
        init(null, null, null, null, null, null, null, null, null);
    }

    public long getFirstItemMsec() {
        return firstItemMsec;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public String getSortId() {
        return sortId;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getTitle() {
        return title;
    }

    public String getUid() {
        return this.uid;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public long getUpdateTime() {
        return this.updateTime;
    }

    public String getUrl() {
        return url;
    }

    private void init(final String uid, final String url, final String title, final Bitmap icon,
            final List<String> tags, final Integer unreadCount, final Long updateTime, final String sortId,
            final Long firstItemMsec) {
        this.uid = uid;
        this.url = url;
        this.title = title;
        this.icon = icon;
        this.tags = (tags == null) ? new LinkedList<String>() : tags;
        this.unreadCount = (unreadCount == null) ? 0 : unreadCount;
        this.updateTime = (updateTime == null) ? System.currentTimeMillis() : updateTime;
        this.sortId = sortId;
        this.firstItemMsec = (firstItemMsec == null) ? 0 : firstItemMsec;
    }

    private void initFromByteArray(final String uid, final String url, final String title, final byte[] icon,
            final List<String> tags, final Integer unreadCount, final Long updateTime, final String sortId,
            final Long firstItemMsec) {
        final Bitmap bitmapIcon = (icon == null) ? null : BitmapFactory.decodeByteArray(icon, 0, icon.length);
        init(uid, url, title, bitmapIcon, tags, unreadCount, updateTime, sortId, firstItemMsec);
    }

    public void setFirstItemMsec(final long firstItemMsec) {
        this.firstItemMsec = firstItemMsec;
    }

    public void setIcon(final Bitmap icon) {
        this.icon = icon;
    }

    public void setIcon(final byte[] icon) {
        this.icon = (icon == null) ? null : BitmapFactory.decodeByteArray(icon, 0, icon.length);
    }

    public void setSortId(final String sortId) {
        this.sortId = sortId;
    }

    public void setTags(final List<String> tags) {
        this.tags = tags;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public void setUid(final String uid) {
        this.uid = uid;
    }

    public void setUnreadCount(final int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public void setUpdateTime(final long syncTime) {
        this.updateTime = syncTime;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ContentValues toContentValues() {
        final ContentValues ret = new ContentValues(6);
        ret.put(_UID, uid);
        ret.put(_URL, url);
        ret.put(_TITLE, title);
        ret.put(_UPDATETIME, updateTime);
        ret.put(_SORTID, sortId);
        ret.put(_FIRSTITEMMSEC, firstItemMsec);
        return ret;
    }

    @Override
    public ContentValues toUpdateContentValues() {
        final ContentValues ret = new ContentValues(3);
        ret.put(_UPDATETIME, updateTime);
        ret.put(_SORTID, sortId);
        ret.put(_FIRSTITEMMSEC, firstItemMsec);
        return ret;
    }
}
