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

import android.net.Uri;

/*
 * Reference: http://code.google.com/p/google-reader-api/wiki/ApiStreamContents
 */

public class StreamContentsURL extends AbsURL {
    private static final String URL_STREAM_CONTENTS = URL_API + "/stream/contents/";

    private String uid;
    private String continuation;
    private long newestItemTime;
    private int limit;
    private boolean isUnread;

    public StreamContentsURL(final boolean isHttpsConnection, final String uid, final boolean isUnread) {
        super(isHttpsConnection, true, true);
        init(uid, null, 20, 0, isUnread);
    }

    public StreamContentsURL(final boolean isHttpsConnection, final String uid, final String continuation,
            final boolean isUnread) {
        super(isHttpsConnection, true, true);
        init(uid, continuation, 20, 0, isUnread);
    }

    public StreamContentsURL(final boolean isHttpsConnection, final String uid, final String continuation,
            final long newestItemTime, final boolean isUnread) {
        super(isHttpsConnection, true, true);
        init(uid, continuation, newestItemTime, 20, isUnread);
    }

    public StreamContentsURL(final boolean isHttpsConnection, final String uid, final String continuation,
            final long newestItemTime, final int limit, final boolean isUnread) {
        super(isHttpsConnection, true, true);
        init(uid, continuation, newestItemTime, limit, isUnread);
    }

    @Override
    public String getBaseURL() {
        return appendURL(serverUrl + URL_STREAM_CONTENTS, Uri.encode(uid, "/"));
    }

    public String getContinuation() {
        return continuation;
    }

    public int getLimit() {
        return limit;
    }

    public long getNewestItemTime() {
        return newestItemTime;
    }

    public String getUid() {
        return uid;
    }

    private void init(final String uid, final String continuation, final long newestItemTime, final int limit,
            final boolean isUnread) {
        this.setUid(uid);
        this.setContinuation(continuation);
        this.setNewestItemTime(newestItemTime);
        this.setLimit(limit);
        this.setUnread(isUnread);
        addParam("likes", "false");
        addParam("comments", "false");
        addParam("r", "n");
    }

    public boolean isUnread() {
        return isUnread;
    }

    public void setContinuation(final String continuation) {
        this.continuation = continuation;
        if (continuation == null || continuation.length() == 0) {
            removeParam("c");
        } else {
            addParam("c", continuation);
        }
    }

    public void setLimit(final int limit) {
        this.limit = limit;
        if (limit > 0) {
            addParam("n", limit);
        }
    }

    public void setNewestItemTime(final long newestItemTime) {
        this.newestItemTime = newestItemTime;
        if (newestItemTime > 0) {
            addParam("nt", String.valueOf(newestItemTime));
        } else {
            removeParam("nt");
        }
    }

    public void setUid(final String uid) {
        this.uid = uid;
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
