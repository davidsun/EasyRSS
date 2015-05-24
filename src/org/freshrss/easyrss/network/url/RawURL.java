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

public class RawURL extends AbsURL {
    private String url;

    public RawURL(final String url) {
        super(url.startsWith("https://"), false, false);

        if (url.startsWith("https://")) {
            this.url = url.substring(8);
        } else {
            this.url = url.substring(7);
        }
    }

    @Override
    public String getBaseURL() {
        return url;
    }

    public void setURL(final String url) {
        this.url = url;
    }
}
