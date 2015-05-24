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

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.net.Uri;

public abstract class AbsURL {
    protected static final String URL_API = "/reader/api/0";

    protected static String serverUrl = "";
    
    protected static String appendParams(final String str, final String param) {
        if (str.length() == 0 || str.endsWith("&")) {
            return str + param;
        } else {
            return str + "&" + param;
        }
    }

    protected static String appendURL(String str, String param) {
        if (param.startsWith("/")) {
            param = param.substring(1);
        }
        if (!str.endsWith("/")) {
            str += "/";
        }
        return str + param;
    }

    private static String paramsToString(final List<NameValuePair> params) {
        String ret = "";
        for (final NameValuePair p : params) {
            ret = appendParams(ret, p.getName() + "=" + p.getValue());
        }
        return ret;
    }

    final private transient List<NameValuePair> params;
    final private boolean authNeeded;
    final private boolean isListQuery;
    final private boolean isHttpsConnection;

    public AbsURL(final boolean isHttpsConnection, final boolean authNeeded, final boolean isListQuery) {
        this.params = new ArrayList<NameValuePair>();
        this.authNeeded = authNeeded;
        this.isListQuery = isListQuery;
        this.isHttpsConnection = isHttpsConnection;
    }

    protected void addParam(final String key, final int value) {
        addParam(key, String.valueOf(value));
    }

    protected void addParam(final String key, final long value) {
        addParam(key, String.valueOf(value));
    }

    protected void addParam(final String key, final String value) {
        for (final NameValuePair p : params) {
            if (p.getName().equals(key)) {
                params.remove(p);
                break;
            }
        }
        params.add(new BasicNameValuePair(key, Uri.encode(value)));
    }

    protected abstract String getBaseURL();

    public List<NameValuePair> getParams() {
        if (isListQuery) {
            final List<NameValuePair> ret = new ArrayList<NameValuePair>(params);
            ret.add(new BasicNameValuePair("client", "android"));
            ret.add(new BasicNameValuePair("output", "json"));
            ret.add(new BasicNameValuePair("ck", String.valueOf(System.currentTimeMillis())));
            return ret;
        } else {
            return params;
        }
    }

    public String getParamsString() {
        return paramsToString(getParams());
    }

    public String getURL() {
    	String baseUrl = getBaseURL();
    	if (baseUrl.startsWith("http")) {
    		return baseUrl;
    	}
        return (isHttpsConnection ? "https://" : "http://") + baseUrl;
    }

    public boolean isAuthNeeded() {
        return authNeeded;
    }

    public boolean isListQuery() {
        return isListQuery;
    }

    protected void removeParam(final String key) {
        for (final NameValuePair p : params) {
            if (p.getName().equals(key)) {
                params.remove(p);
                return;
            }
        }
    }
    
    public static void setServerUrl(final String serverUrl) {
        AbsURL.serverUrl = serverUrl;
    }
}
