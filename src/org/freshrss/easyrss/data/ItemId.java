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

import android.content.ContentValues;

public class ItemId implements Entity {
    private String uid;
    private long timestamp;

    public ItemId() {
        init(null, null);
    }

    public ItemId(final String uid, final long timestamp) {
        init(uid, timestamp);
    }

    @Override
    public void clear() {
        init(null, null);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getUid() {
        return uid;
    }

    private void init(final String uid, final Long timestamp) {
        this.uid = uid;
        this.setTimestamp((timestamp == null) ? 0 : timestamp);
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setUid(final String uid) {
        this.uid = uid;
    }

    @Override
    public ContentValues toContentValues() {
        return null;
    }

    @Override
    public ContentValues toUpdateContentValues() {
        return null;
    }
}
