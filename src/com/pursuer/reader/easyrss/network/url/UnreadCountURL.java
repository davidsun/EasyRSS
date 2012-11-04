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

package com.pursuer.reader.easyrss.network.url;

public class UnreadCountURL extends AbsURL {
    private static final String URL_UNREAD_COUNT = URL_GOOGLE_READER_API + "/unread-count";

    public UnreadCountURL(final boolean isHttpsConnection) {
        super(isHttpsConnection, true, true);
    }

    @Override
    public String getBaseURL() {
        return URL_UNREAD_COUNT;
    }
}
