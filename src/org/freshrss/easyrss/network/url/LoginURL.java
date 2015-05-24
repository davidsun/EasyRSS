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

public class LoginURL extends AbsURL {
    private static final String URL_API_LOGIN = "/accounts/ClientLogin";

    private transient String username;
    private transient String password;

    public LoginURL(final String username, final String password) {
        super(true, false, false);

        setUsername(username);
        setPassword(password);
        init();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof LoginURL)) {
            return false;
        }
        final LoginURL url = (LoginURL) obj;
        return (url.username.equals(username) && url.password.equals(password));
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String getBaseURL() {
        return serverUrl + URL_API_LOGIN;
    }

    public String getUsername() {
        return username;
    }

    private void init() {
        addParam("accountType", "GOOGLE");
        addParam("service", "reader");
        addParam("source", "Sun-EasyRSS");
    }

    public void setPassword(final String password) {
        this.password = password;
        addParam("Passwd", password);
    }

    public void setUsername(final String username) {
        this.username = username;
        addParam("Email", username);
    }
}
