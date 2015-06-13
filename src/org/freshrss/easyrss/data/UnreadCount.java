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

public class UnreadCount implements Entity {
    private String uid;
    private int count;
    private long newestItemTime;

    public UnreadCount() {
        init(null, null, null);
    }

    @Override
    public void clear() {
        init(null, null, null);
    }

    public int getCount() {
        return count;
    }

    public long getNewestItemTime() {
        return newestItemTime;
    }

    public String getUid() {
        return uid;
    }

    private void init(final String uid, final Integer count, final Long newestItemTime) {
        this.uid = uid;
        this.count = (count == null) ? 0 : count;
        this.newestItemTime = (newestItemTime == null) ? 0 : newestItemTime;
    }

    public void setCount(final int count) {
        this.count = count;
    }

    public void setNewestItemTime(final long newestItemTime) {
        this.newestItemTime = newestItemTime;
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
