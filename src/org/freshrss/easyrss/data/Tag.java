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

import org.freshrss.easyrss.Utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.text.Html;

public class Tag implements Entity {
    public static final String TABLE_NAME = "tags";
    public static final Uri CONTENT_URI = Uri.parse(DataProvider.TAG_CONTENT_URI);

    public static final String _UID = "uid";
    public static final String _UNREADCOUNT = "unreadCount";
    public static final String _UPDATETIME = "updateTime";
    public static final String _SORTID = "sortId";

    public static final String[] COLUMNS = { _UID, _UNREADCOUNT, _UPDATETIME, _SORTID };
    public static final String[] COLUMNS_TYPE = { "TEXT PRIMARY KEY", "INTEGER NOT NULL DEFAULT 0",
            "INTEGER NOT NULL DEFAULT 0", "TEXT", "INTEGER NOT NULL DEFAULT 0" };
    public static final String[][] INDEX_COLUMNS = { { _UID }, { _UPDATETIME } };

    public static Tag fromCursor(final Cursor cur) {
        return new Tag(Utils.getStringFromCursor(cur, _UID), Utils.getIntFromCursor(cur, _UNREADCOUNT),
                Utils.getLongFromCursor(cur, _UPDATETIME), Utils.getStringFromCursor(cur, _SORTID));
    }

    private String uid;
    private String sortId;
    private int unreadCount;
    private long updateTime;

    public Tag() {
        init(null, null, null, null);
    }

    public Tag(final String uid, final int unreadCount, final long updateTime, final String sortId) {
        init(uid, unreadCount, updateTime, sortId);
    }

    @Override
    public void clear() {
        init(null, null, null, null);
    }

    public String getSortId() {
        return sortId;
    }

    public String getTitle() {
        return Html.fromHtml(uid.substring(uid.lastIndexOf('/') + 1)).toString();
    }

    public String getUid() {
        return uid;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    private void init(final String uid, final Integer unreadCount, final Long updateTime, final String sortId) {
        this.uid = uid;
        this.unreadCount = (unreadCount == null) ? 0 : unreadCount;
        this.updateTime = (updateTime == null) ? System.currentTimeMillis() : updateTime;
        this.sortId = sortId;
    }

    public void setSortId(String sortId) {
        this.sortId = sortId;
    }

    public void setUid(final String uid) {
        this.uid = uid;
    }

    public void setUnreadCount(final int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public void setUpdateTime(final long updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public ContentValues toContentValues() {
        final ContentValues ret = new ContentValues(4);
        ret.put(_UID, uid);
        ret.put(_UPDATETIME, updateTime);
        ret.put(_SORTID, sortId);
        return ret;
    }

    @Override
    public ContentValues toUpdateContentValues() {
        final ContentValues ret = new ContentValues(3);
        ret.put(_UPDATETIME, updateTime);
        ret.put(_SORTID, sortId);
        return ret;
    }
}
