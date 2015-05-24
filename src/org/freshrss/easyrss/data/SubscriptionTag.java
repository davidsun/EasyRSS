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

public class SubscriptionTag implements Entity {
    public static final String TABLE_NAME = "subscriptionTags";
    public static final Uri CONTENT_URI = Uri.parse(DataProvider.SUBSCRIPTIONTAG_CONTENT_URI);

    public static final String _SUBSCRIPTIONUID = "subscriptionUid";
    public static final String _TAGUID = "tagUid";
    public static final String[] COLUMNS = { _SUBSCRIPTIONUID, _TAGUID };

    public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + _SUBSCRIPTIONUID
            + " TEXT," + _TAGUID + " TEXT, PRIMARY KEY (" + _SUBSCRIPTIONUID + "," + _TAGUID + "))";
    public static final String[][] INDEX_COLUMNS = { { _SUBSCRIPTIONUID }, { _TAGUID } };

    public static SubscriptionTag fromCursor(final Cursor cur) {
        return new SubscriptionTag(Utils.getStringFromCursor(cur, SubscriptionTag._SUBSCRIPTIONUID),
                Utils.getStringFromCursor(cur, SubscriptionTag._TAGUID));
    }

    private String subscriptionUid;
    private String tagUid;

    public SubscriptionTag() {
        init(null, null);
    }

    public SubscriptionTag(final String subscriptionUid, final String tagUid) {
        init(subscriptionUid, tagUid);
    }

    @Override
    public void clear() {
        init(null, null);
    }

    public String getSubscriptionUid() {
        return subscriptionUid;
    }

    public String getTagUid() {
        return tagUid;
    }

    private void init(final String subscriptionUid, final String tagUid) {
        this.subscriptionUid = subscriptionUid;
        this.tagUid = tagUid;
    }

    public void setSubscriptionUid(final String subscriptionUid) {
        this.subscriptionUid = subscriptionUid;
    }

    public void setTagUid(final String tagUid) {
        this.tagUid = tagUid;
    }

    @Override
    public ContentValues toContentValues() {
        final ContentValues ret = new ContentValues(2);
        ret.put(_SUBSCRIPTIONUID, subscriptionUid);
        ret.put(_TAGUID, tagUid);
        return ret;
    }

    @Override
    public ContentValues toUpdateContentValues() {
        final ContentValues ret = new ContentValues();
        return ret;
    }
}
