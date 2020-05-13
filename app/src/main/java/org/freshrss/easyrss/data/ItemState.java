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

public class ItemState implements Entity {
    public static final String _ISCACHED = "isCached";
    public static final String _ISREAD = "isRead";
    public static final String _ISSTARRED = "isStarred";

    public static final String[] OWN_COLUMNS = { _ISCACHED, _ISREAD, _ISSTARRED };
    public static final String[] OWN_COLUMN_TYPE = { "INT NOT NULL DEFAULT 0", "INT NOT NULL DEFAULT 0",
            "INT NOT NULL DEFAULT 0" };

    public static ItemState fromCursor(final Cursor cur) {
        return new ItemState(Utils.toBoolean(Utils.getIntFromCursor(cur, _ISCACHED)), Utils.toBoolean(Utils
                .getIntFromCursor(cur, _ISREAD)), Utils.toBoolean(Utils.getIntFromCursor(cur, _ISSTARRED)));
    }

    private boolean isCached;
    private boolean isRead;
    private boolean isStarred;

    public ItemState() {
        init(null, null, null);
    }

    private ItemState(final boolean isCached, final boolean isRead, final boolean isStarred) {
        init(isCached, isRead, isStarred);
    }

    @Override
    public void clear() {
        init(null, null, null);
    }

    private void init(final Boolean isCached, final Boolean isRead, final Boolean isStarred) {
        this.isCached = (isCached == null) ? false : isCached;
        this.isRead = (isRead == null) ? false : isRead;
        this.isStarred = (isStarred == null) ? false : isStarred;
    }

    public boolean isCached() {
        return isCached;
    }

    public boolean isRead() {
        return isRead;
    }

    public boolean isStarred() {
        return isStarred;
    }

    public void setCached(final boolean isCached) {
        this.isCached = isCached;
    }

    public void setRead(final boolean isRead) {
        this.isRead = isRead;
    }

    public void setStarred(final boolean isStarred) {
        this.isStarred = isStarred;
    }

    @Override
    public ContentValues toContentValues() {
        final ContentValues values = new ContentValues();
        values.put(_ISREAD, isRead);
        values.put(_ISSTARRED, isStarred);
        return values;
    }

    @Override
    public ContentValues toUpdateContentValues() {
        final ContentValues values = new ContentValues();
        values.put(_ISREAD, isRead);
        values.put(_ISSTARRED, isStarred);
        return values;
    }
}
