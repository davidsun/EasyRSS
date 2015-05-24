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

package org.freshrss.easyrss.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.apache.http.HttpStatus;
import org.freshrss.easyrss.account.ReaderAccountMgr;


public class NetworkClient {
    public class NetworkException extends Exception {
        private static final long serialVersionUID = 1L;

        public NetworkException(final String message) {
            super(message);
        }

        public NetworkException(final String message, final Throwable cause) {
            super(message, cause);
        }

        public NetworkException(final Throwable cause) {
            super(cause);
        }
    }

    private static NetworkClient instance = null;

    public synchronized static NetworkClient getInstance() {
        if (instance == null) {
            instance = new NetworkClient();
        }
        return instance;
    }

    private String auth;

    SSLSocketFactory sslSocketFactory = new SSLSocketFactoryCustom();

    private NetworkClient() {
        // TODO empty method
    }

    public byte[] doGetByte(final String url) throws Exception {
        final InputStream stream = doGetStream(url);
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final byte[] data = new byte[8192];
        int len;
        while ((len = stream.read(data, 0, 8192)) != -1) {
            output.write(data, 0, len);
        }
        final byte[] ret = output.toByteArray();
        output.close();
        return ret;
    }

    HttpURLConnection makeConnection(final String url) throws MalformedURLException, IOException {
        final HttpURLConnection httpURLConnection = (HttpURLConnection)(new URL(url).openConnection());
        httpURLConnection.setConnectTimeout(40 * 1000);
        httpURLConnection.setReadTimeout(30 * 1000);
        if (url.toLowerCase(Locale.US).startsWith("https://")) {
            final HttpsURLConnection httpsURLConnection = (HttpsURLConnection)httpURLConnection;
            httpsURLConnection.setSSLSocketFactory(this.sslSocketFactory);
        }
        return httpURLConnection;
    }

    public InputStream doGetStream(final String url) throws Exception {
        final HttpURLConnection conn = makeConnection(url);
        conn.setRequestMethod("GET");
        if (auth != null) {
            conn.setRequestProperty("Authorization", "GoogleLogin auth=" + auth);
        }
        try {
            final int resStatus = conn.getResponseCode();
            if (resStatus == HttpStatus.SC_UNAUTHORIZED) {
                ReaderAccountMgr.getInstance().invalidateAuth();
            }
            if (resStatus != HttpStatus.SC_OK) {
                throw new NetworkException("Invalid HTTP status " + resStatus + ": " + url + ".");
            }
        } catch (final Exception exception) {
            if (exception.getMessage() != null && exception.getMessage().contains("authentication")) {
                ReaderAccountMgr.getInstance().invalidateAuth();
            }
            throw exception;
        }
        return conn.getInputStream();
    }

    public byte[] doPostByte(final String url, final String params) throws Exception {
        final InputStream stream = doPostStream(url, params);
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final byte[] data = new byte[8192];
        int len;
        while ((len = stream.read(data, 0, 8192)) != -1) {
            output.write(data, 0, len);
        }
        final byte[] ret = output.toByteArray();
        output.close();
        return ret;
    }

    public InputStream doPostStream(final String url, final String params) throws IOException, NetworkException {
    	final HttpURLConnection conn = makeConnection(url);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        if (auth != null) {
            conn.setRequestProperty("Authorization", "GoogleLogin auth=" + auth);
        }
        conn.setDoInput(true);
        conn.setDoOutput(true);
        final OutputStream output = conn.getOutputStream();
        output.write(params.getBytes());
        output.flush();
        output.close();

        conn.connect();
        try {
            final int resStatus = conn.getResponseCode();
            if (resStatus == HttpStatus.SC_UNAUTHORIZED) {
                ReaderAccountMgr.getInstance().invalidateAuth();
            }
            if (resStatus != HttpStatus.SC_OK) {
                throw new NetworkException("Invalid HTTP status " + resStatus + ": " + url + ".");
            }
        } catch (final IOException exception) {
            if (exception.getMessage() != null && exception.getMessage().contains("authentication")) {
                ReaderAccountMgr.getInstance().invalidateAuth();
            }
            throw exception;
        }
        return conn.getInputStream();
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(final String auth) {
        this.auth = auth;
    }
}
