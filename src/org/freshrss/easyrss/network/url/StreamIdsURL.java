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

package org.freshrss.easyrss.network.url;

public class StreamIdsURL extends AbsURL {
    private static final String URL_STREAM_IDS = URL_API + "/stream/items/ids";

    private String uid;
    private int limit;
    private boolean isUnread;

    public StreamIdsURL(final boolean isHttpsConnection, final String uid) {
        super(isHttpsConnection, true, true);
        init(uid, 100, false);
    }

    public StreamIdsURL(final boolean isHttpsConnection, final String uid, final int limit) {
        super(isHttpsConnection, true, true);
        init(uid, limit, false);
    }

    public StreamIdsURL(final boolean isHttpsConnection, final String uid, final int limit, final boolean isUnread) {
        super(isHttpsConnection, true, true);
        init(uid, limit, isUnread);
    }

    @Override
    public String getBaseURL() {
        return serverUrl + URL_STREAM_IDS;
    }

    public int getLimit() {
        return limit;
    }

    public String getUid() {
        return uid;
    }

    private void init(final String uid, final int limit, final boolean isUnread) {
        this.setUid(uid);
        this.setLimit(limit);
        this.setUnread(isUnread);
    }

    public boolean isUnread() {
        return isUnread;
    }

    public void setLimit(final int limit) {
        this.limit = limit;
        if (limit > 0) {
            addParam("n", limit);
        }
    }

    public void setUid(final String uid) {
        this.uid = uid;
        addParam("s", uid);
    }

    public void setUnread(final boolean isUnread) {
        this.isUnread = isUnread;
        if (isUnread) {
            addParam("xt", "user/-/state/com.google/read");
        } else {
            removeParam("xt");
        }
    }
}
