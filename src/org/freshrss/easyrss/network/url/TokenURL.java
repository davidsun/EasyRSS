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

public class TokenURL extends AbsURL {
    public static final String URL_API_TOKEN = URL_API + "/token";

    public TokenURL(final boolean isHttpsConnection) {
        super(isHttpsConnection, true, false);
    }

    @Override
    public String getBaseURL() {
        return serverUrl + URL_API_TOKEN;
    }
}
