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

public class ItemTag implements Entity {
    public static final String TABLE_NAME = "itemTags";

    public static final Uri CONTENT_URI = Uri.parse(DataProvider.ITEMTAG_CONTENT_URI);

    public static final String _ITEMUID = "itemUid";
    public static final String _TAGUID = "tagUid";
    public static final String[] COLUMNS = { _ITEMUID, _TAGUID };

    public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + _ITEMUID
            + " TEXT, " + _TAGUID + " TEXT, PRIMARY KEY (" + _ITEMUID + "," + _TAGUID + "))";

    public static final String[][] INDEX_COLUMNS = { { _ITEMUID }, { _TAGUID } };

    public static ItemTag fromCursor(final Cursor cur) {
        return new ItemTag(Utils.getStringFromCursor(cur, ItemTag._ITEMUID), Utils.getStringFromCursor(cur,
                ItemTag._TAGUID));
    }

    private String itemUid;
    private String tagUid;

    public ItemTag() {
        init(null, null);
    }

    public ItemTag(final String itemUid, final String tagUid) {
        init(itemUid, tagUid);
    }

    @Override
    public void clear() {
        init(null, null);
    }

    public String getItemUid() {
        return itemUid;
    }

    public String getTagUid() {
        return tagUid;
    }

    private void init(final String itemUid, final String tagUid) {
        this.itemUid = itemUid;
        this.tagUid = tagUid;
    }

    public void setItemUid(final String itemUid) {
        this.itemUid = itemUid;
    }

    public void setTagUid(final String tagUid) {
        this.tagUid = tagUid;
    }

    @Override
    public ContentValues toContentValues() {
        final ContentValues ret = new ContentValues(2);
        ret.put(_ITEMUID, itemUid);
        ret.put(_TAGUID, tagUid);
        return ret;
    }

    @Override
    public ContentValues toUpdateContentValues() {
        return new ContentValues();
    }
}
