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

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.freshrss.easyrss.Utils;


import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class Item implements Entity {
    public static final String TABLE_NAME = "items";
    public static final Uri CONTENT_URI = Uri.parse(DataProvider.ITEM_CONTENT_URI);

    public static final String _AUTHOR = "author";
    public static final String _UID = "uid";
    public static final String _HREF = "href";
    public static final String _SOURCEURI = "sourceUri";
    public static final String _SOURCETITLE = "sourceTitle";
    public static final String _TITLE = "title";
    public static final String _TIMESTAMP = "timestamp";
    public static final String _UPDATETIME = "updateTime";

    private static final String[] OWN_COLUMNS = { _UID, _AUTHOR, _HREF, _SOURCEURI, _SOURCETITLE, _TITLE, _TIMESTAMP,
            _UPDATETIME };
    private static final String[] OWN_COLUMNS_TYPE = { "TEXT PRIMARY KEY", "TEXT", "TEXT", "TEXT", "TEXT", "TEXT",
            "INTEGER", "INTEGER" };
    public static final String[] COLUMNS = Utils.arrayMerge(OWN_COLUMNS, ItemState.OWN_COLUMNS);
    public static final String[] COLUMNS_TYPE = Utils.arrayMerge(OWN_COLUMNS_TYPE, ItemState.OWN_COLUMN_TYPE);

    public static final String[][] INDEX_COLUMNS = { { _UID }, { _UPDATETIME }, { _SOURCEURI },
            { _SOURCEURI, ItemState._ISREAD }, { _SOURCEURI, ItemState._ISSTARRED }, { _TIMESTAMP },
            { _TIMESTAMP, ItemState._ISREAD }, { _TIMESTAMP, ItemState._ISSTARRED } };

    private static final String UID_PREFIX = "tag:google.com,2005:reader/item/";

    public static Item fromCursor(final Cursor cur) {
        final ItemState state = ItemState.fromCursor(cur);
        return new Item(Utils.getStringFromCursor(cur, Item._AUTHOR), Utils.getStringFromCursor(cur, Item._UID),
                Utils.getStringFromCursor(cur, Item._HREF), Utils.getStringFromCursor(cur, Item._SOURCEURI),
                Utils.getStringFromCursor(cur, Item._SOURCETITLE), Utils.getStringFromCursor(cur, Item._TITLE),
                Utils.getLongFromCursor(cur, Item._UPDATETIME), Utils.getLongFromCursor(cur, Item._TIMESTAMP), state);
    }

    public static String getFullUid(final String uid) {
        return UID_PREFIX + uid;
    }

    public static String getStoragePathByUid(final String uid) {
        return DataUtils.getAppFolderPath() + File.separator + uid;
    }

    private String author;
    private String uid;
    private String content;
    private String href;
    private String sourceUri;
    private String sourceTitle;
    private String title;
    private List<String> tags;
    private long updateTime;
    private long timestamp;
    private ItemState state;

    public Item() {
        init(null, null, null, null, null, null, null, null, null, null, null);
    }

    private Item(final String author, final String uid, final String href, final String sourceUri,
            final String sourceTitle, final String title, final long updateTime, final long timestamp,
            final ItemState state) {
        init(author, uid, null, href, sourceUri, sourceTitle, title, null, updateTime, timestamp, state);
    }

    public void addTag(final String tag) {
        tags.add(tag);
    }

    @Override
    public void clear() {
        init(null, null, null, null, null, null, null, null, null, null, null);
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public String getFullContentStoragePath() {
        return getStoragePath() + File.separator + uid + ".full";
    }

    public String getFullUid() {
        return getFullUid(uid);
    }

    public String getHref() {
        return href;
    }

    public String getImageStoragePath(final int picId) {
        return getStoragePath() + File.separator + picId + ".erss";
    }

    public String getOriginalContentStoragePath() {
        return getStoragePath() + File.separator + uid + ".original";
    }

    public String getSourceTitle() {
        return sourceTitle;
    }

    public String getSourceUri() {
        return sourceUri;
    }

    public ItemState getState() {
        return state;
    }

    public String getStoragePath() {
        return getStoragePathByUid(uid);
    }

    public String getStrippedContentStoragePath() {
        return getStoragePath() + File.separator + uid + ".stripped";
    }

    public List<String> getTags() {
        return tags;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTitle() {
        return title;
    }

    public String getUid() {
        return uid;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    private void init(final String author, final String uid, final String content, final String href,
            final String sourceUri, final String sourceTitle, final String title, final List<String> tags,
            final Long updateTime, final Long timestamp, final ItemState state) {
        this.author = (author == null) ? "" : author;
        this.uid = uid;
        this.content = (content == null) ? "" : content;
        this.href = (href == null) ? "" : href;
        this.sourceUri = (sourceUri == null) ? "" : sourceUri;
        this.sourceTitle = (sourceTitle == null) ? "" : sourceTitle;
        this.title = (title == null) ? "" : title;
        this.tags = (tags == null) ? new LinkedList<String>() : tags;
        this.updateTime = (updateTime == null) ? System.currentTimeMillis() : updateTime;
        this.timestamp = (timestamp == null) ? 0 : timestamp;
        this.state = (state == null) ? new ItemState() : state;
    }

    public void setAuthor(final String author) {
        this.author = author;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public void setHref(final String href) {
        this.href = href;
    }

    public void setSourceTitle(final String sourceTitle) {
        this.sourceTitle = sourceTitle;
    }

    public void setSourceUri(final String sourceUri) {
        this.sourceUri = sourceUri;
    }

    public void setState(final ItemState state) {
        this.state = state;
    }

    public void setTags(final List<String> tags) {
        this.tags = tags;
    }

    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public void setUid(final String uid) {
        this.uid = uid;
    }

    public void setUpdateTime(final long updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public ContentValues toContentValues() {
        final ContentValues ret = state.toContentValues();
        ret.put(_AUTHOR, author);
        ret.put(_UID, uid);
        ret.put(_HREF, href);
        ret.put(_SOURCEURI, sourceUri);
        ret.put(_SOURCETITLE, sourceTitle);
        ret.put(_TITLE, title);
        ret.put(_UPDATETIME, updateTime);
        ret.put(_TIMESTAMP, timestamp);
        return ret;
    }

    @Override
    public ContentValues toUpdateContentValues() {
        final ContentValues ret = state.toUpdateContentValues();
        ret.put(_UPDATETIME, updateTime);
        return ret;
    }
}
