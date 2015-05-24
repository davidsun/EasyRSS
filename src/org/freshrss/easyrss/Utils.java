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

package org.freshrss.easyrss;

import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import org.freshrss.easyrss.R;
import org.freshrss.easyrss.account.ReaderAccountMgr;
import org.freshrss.easyrss.data.DataMgr;
import org.freshrss.easyrss.network.NetworkMgr;

import android.content.Context;
import android.database.Cursor;


final public class Utils {
    @SuppressWarnings("unchecked")
    public static <T> T[] arrayMerge(final T[]... arrays) {
        int count = 0;
        for (final T[] array : arrays) {
            count += array.length;
        }

        final T[] mergedArray = (T[]) Array.newInstance(arrays[0][0].getClass(), count);
        int start = 0;
        for (final T[] array : arrays) {
            System.arraycopy(array, 0, mergedArray, start, array.length);
            start += array.length;
        }
        return mergedArray;
    }

    @SuppressWarnings("deprecation")
    public static String decodeSubscriptionURL(final String url) {
        if (url.startsWith("feed/")) {
            return "feed/" + URLDecoder.decode(url.substring(5));
        } else {
            return url;
        }
    }

    @SuppressWarnings("deprecation")
    public static String encodeSubscriptionURL(final String url) {
        if (url.startsWith("feed/")) {
            return "feed/" + URLEncoder.encode(url.substring(5));
        } else {
            return url;
        }
    }

    public static byte[] getBolbFromCursor(final Cursor cur, final String column) {
        final int idx = cur.getColumnIndex(column);
        return (idx == -1) ? null : cur.getBlob(idx);
    }

    public static int getIntFromCursor(final Cursor cur, final String column) {
        final int idx = cur.getColumnIndex(column);
        return (idx == -1) ? 0 : cur.getInt(idx);
    }

    public static long getLongFromCursor(final Cursor cur, final String column) {
        final int idx = cur.getColumnIndex(column);
        return (idx == -1) ? 0 : cur.getLong(idx);
    }

    public static String getStringFromCursor(final Cursor cur, final String column) {
        final int idx = cur.getColumnIndex(column);
        return (idx == -1) ? null : cur.getString(idx);
    }

    public static void initManagers(final Context context) {
        if (DataMgr.getInstance() == null) {
            DataMgr.init(context.getApplicationContext());
        }
        if (NetworkMgr.getInstance() == null) {
            NetworkMgr.init(context.getApplicationContext());
        }
        if (ReaderAccountMgr.getInstance() == null) {
            ReaderAccountMgr.init(context.getApplicationContext());
        }
        if (NotificationMgr.getInstance() == null) {
            NotificationMgr.init(context.getApplicationContext());
        }
    }

    public static Date timestampToDate(final long timestamp) {
        return new Date(timestamp / 1000);
    }

    public static String timestampToTimeAgo(final Context context, final long timestamp) {
        final Date date = timestampToDate(timestamp);
        final long delta = new Date().getTime() - date.getTime();
        if (delta < 60 * 60 * 1000) {
            return context.getString(R.string.TxtLessThanOneHourAgo);
        } else if (delta < 40 * 60 * 60 * 1000) {
            return context.getString(R.string.TxtHoursAgo, delta / (60 * 60 * 1000) + 1);
        } else {
            return context.getString(R.string.TxtDaysAgo, delta / (24 * 60 * 60 * 1000) + 1);
        }
    }

    public static boolean toBoolean(final int x) {
        return (x == 0) ? false : true;
    }

    public static int toInt(final boolean x) {
        return (x) ? 1 : 0;
    }

    private Utils() {
    }
}
