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

public class Transaction implements Entity {
    public static final String TABLE_NAME = "transactions";
    public static final Uri CONTENT_URI = Uri.parse(DataProvider.TRANSACTION_CONTENT_URI);

    public static final String _ID = "id";
    public static final String _UID = "uid";
    public static final String _TYPE = "type";
    public static final String _CONTENT = "note";

    public static final int TYPE_UNKNOWN = -1;
    public static final int TYPE_SET_READ = 0;
    public static final int TYPE_REMOVE_READ = 1;
    public static final int TYPE_SET_STARRED = 2;
    public static final int TYPE_REMOVE_STARRED = 3;

    public static final String[] COLUMNS = { _ID, _UID, _TYPE, _CONTENT };
    public static final String[] COLUMNS_TYPE = { "INTEGER PRIMARY KEY AUTOINCREMENT", "TEXT NOT NULL",
            "INTEGER NOT NULL", "TEXT" };
    public static final String[][] INDEX_COLUMNS = { { _UID }, { _UID, _TYPE } };

    public static Transaction fromCursor(final Cursor cur) {
        return new Transaction(Utils.getLongFromCursor(cur, _ID), Utils.getStringFromCursor(cur, _UID),
                Utils.getStringFromCursor(cur, _CONTENT), Utils.getIntFromCursor(cur, _TYPE));
    }

    private long id;
    private String uid;
    private String content;
    private int type;

    public Transaction() {
        init(null, null, null, null);
    }

    public Transaction(final long id, final String uid, final String content, final int type) {
        init(id, uid, content, type);
    }

    public Transaction(final String uid, final String content, final int type) {
        init(null, uid, content, type);
    }

    @Override
    public void clear() {
        init(null, null, null, null);
    }

    public String getContent() {
        return content;
    }

    public long getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public String getUid() {
        return uid;
    }

    private void init(final Long id, final String uid, final String content, final Integer type) {
        this.id = (id == null) ? 0 : id;
        this.uid = uid;
        this.setContent(content);
        this.type = (type == null) ? TYPE_UNKNOWN : type;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setType(final int type) {
        this.type = type;
    }

    public void setUid(final String uid) {
        this.uid = uid;
    }

    @Override
    public ContentValues toContentValues() {
        final ContentValues ret = new ContentValues(3);
        if (id > 0) {
            ret.put(_ID, id);
        }
        ret.put(_UID, uid);
        ret.put(_TYPE, type);
        ret.put(_CONTENT, content);
        return ret;
    }

    @Override
    public ContentValues toUpdateContentValues() {
        final ContentValues ret = new ContentValues(2);
        ret.put(_UID, uid);
        ret.put(_TYPE, type);
        ret.put(_CONTENT, content);
        return ret;
    }
}
