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

public class TagListURL extends AbsURL {
    private static final String URL_TAG_LIST = URL_API + "/tag/list";

    public TagListURL(final boolean isHttpsConnection) {
        super(isHttpsConnection, true, true);
    }

    @Override
    public String getBaseURL() {
        return serverUrl + URL_TAG_LIST;
    }
}
