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

import java.net.MalformedURLException;
import java.net.URL;

public class SubscriptionIconUrl extends AbsURL {
    final private static String URL_BASE = "www.google.com/s2/favicons";

    private String subscriptionUrl;

    public SubscriptionIconUrl(final boolean isHttpsConnection, final String subscriptionUrl) {
        super(isHttpsConnection, false, false);

        setSubscriptionUrl(subscriptionUrl);
        init();
    }

    @Override
    public String getBaseURL() {
        return URL_BASE;
    }

    public String getSubscriptionUrl() {
        return subscriptionUrl;
    }

    private void init() {
        addParam("alt", "feed");
    }

    public void setSubscriptionUrl(final String subscriptionUrl) {
        this.subscriptionUrl = subscriptionUrl;
        try {
            final URL url = new URL(subscriptionUrl);
            addParam("domain", url.getHost());
        } catch (final MalformedURLException exception) {
            exception.printStackTrace();
        }
    }
}
