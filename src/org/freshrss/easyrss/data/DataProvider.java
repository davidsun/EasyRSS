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

package org.freshrss.easyrss.data;

import java.net.URLDecoder;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

public class DataProvider extends ContentProvider {
    private static final String AUTHORITY = "org.freshrss.easyrss.data";

    private DBOpenHelper mDbHelper;

    private static final String CONTENT_HEAD = "content://";
    public static final String ITEM_CONTENT_URI = CONTENT_HEAD + AUTHORITY + "/" + Item.TABLE_NAME;
    public static final String ITEMTAG_CONTENT_URI = CONTENT_HEAD + AUTHORITY + "/" + ItemTag.TABLE_NAME;
    public static final String SUBSCRIPTION_CONTENT_URI = CONTENT_HEAD + AUTHORITY + "/" + Subscription.TABLE_NAME;
    public static final String SUBSCRIPTIONTAG_CONTENT_URI = CONTENT_HEAD + AUTHORITY + "/"
            + SubscriptionTag.TABLE_NAME;
    public static final String SETTING_CONTENT_URI = CONTENT_HEAD + AUTHORITY + "/" + Setting.TABLE_NAME;
    public static final String TAG_CONTENT_URI = CONTENT_HEAD + AUTHORITY + "/" + Tag.TABLE_NAME;
    public static final String TRANSACTION_CONTENT_URI = CONTENT_HEAD + AUTHORITY + "/" + Transaction.TABLE_NAME;

    private static final UriMatcher uriMatcher;
    private static final int UM_ITEM_UID = 10;
    private static final int UM_ITEMS = 11;
    private static final int UM_ITEM_TAGS = 12;
    private static final int UM_ITEMS_WITH_LIMIT = 13;
    private static final int UM_ITEMS_WITH_OFFSET = 14;
    private static final int UM_ITEMS_WITH_OFFSET_LIMIT = 15;
    private static final int UM_ITEMTAGS = 20;
    private static final int UM_SUBSCRIPTION_UID = 30;
    private static final int UM_SUBSCRIPTIONS = 31;
    private static final int UM_SUBSCRIPTIONTAGS = 40;
    private static final int UM_SETTING_NAME = 50;
    private static final int UM_SETTINGS = 51;
    private static final int UM_TAG_UID = 60;
    private static final int UM_TAGS = 61;
    private static final int UM_TAG_ITEMS = 62;
    private static final int UM_TRANSACTION_ID = 70;
    private static final int UM_TRANSACTIONS = 71;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, Item.TABLE_NAME + "/id/*", UM_ITEM_UID);
        uriMatcher.addURI(AUTHORITY, Item.TABLE_NAME, UM_ITEMS);
        uriMatcher.addURI(AUTHORITY, Item.TABLE_NAME + "/tags/*", UM_ITEM_TAGS);
        uriMatcher.addURI(AUTHORITY, Item.TABLE_NAME + "/limit/#", UM_ITEMS_WITH_LIMIT);
        uriMatcher.addURI(AUTHORITY, Item.TABLE_NAME + "/offset/#", UM_ITEMS_WITH_OFFSET);
        uriMatcher.addURI(AUTHORITY, Item.TABLE_NAME + "/offset-limit/#/#", UM_ITEMS_WITH_OFFSET_LIMIT);
        uriMatcher.addURI(AUTHORITY, ItemTag.TABLE_NAME, UM_ITEMTAGS);
        uriMatcher.addURI(AUTHORITY, Subscription.TABLE_NAME + "/id/*", UM_SUBSCRIPTION_UID);
        uriMatcher.addURI(AUTHORITY, Subscription.TABLE_NAME, UM_SUBSCRIPTIONS);
        uriMatcher.addURI(AUTHORITY, SubscriptionTag.TABLE_NAME, UM_SUBSCRIPTIONTAGS);
        uriMatcher.addURI(AUTHORITY, Setting.TABLE_NAME + "/id/*", UM_SETTING_NAME);
        uriMatcher.addURI(AUTHORITY, Setting.TABLE_NAME, UM_SETTINGS);
        uriMatcher.addURI(AUTHORITY, Tag.TABLE_NAME + "/id/*", UM_TAG_UID);
        uriMatcher.addURI(AUTHORITY, Tag.TABLE_NAME, UM_TAGS);
        uriMatcher.addURI(AUTHORITY, Tag.TABLE_NAME + "/items/*", UM_TAG_ITEMS);
        uriMatcher.addURI(AUTHORITY, Transaction.TABLE_NAME + "/id/#", UM_TRANSACTION_ID);
        uriMatcher.addURI(AUTHORITY, Transaction.TABLE_NAME, UM_TRANSACTIONS);
    }

    private static String appendProjection(final String projection, final String str) {
        if (projection.length() == 0 || projection.endsWith(",")) {
            return projection + str;
        } else {
            return projection + "," + str;
        }
    }

    private static String toProjectionString(final String[] projection) {
        if (projection == null || projection.length == 0) {
            return "*";
        }
        String ret = "";
        for (int i = 0; i < projection.length; i++) {
            ret = appendProjection(ret, projection[i]);
        }
        return ret;
    }

    private static String appendWhere(final String where, final String key, final String value) {
        final StringBuilder buff = new StringBuilder(128);
        buff.append(key);
        buff.append("=\"");
        buff.append(value);
        buff.append('\"');
        if (!TextUtils.isEmpty(where)) {
            buff.append(" AND ");
            buff.append(where);
        }
        return buff.toString();
    }

    public void close() {
        mDbHelper.close();
    }

    @Override
    public int delete(final Uri uri, final String where, final String[] whereArgs) {
        update(uri, null, where, whereArgs, false);
        return 0;
    }

    @Override
    public String getType(final Uri uri) {
        return null;
    }

    @Override
    public Uri insert(final Uri uri, final ContentValues values) {
        final String tableName;
        final Uri contentUri;
        switch (uriMatcher.match(uri)) {
        case UM_ITEMS:
            tableName = Item.TABLE_NAME;
            contentUri = Item.CONTENT_URI;
            break;
        case UM_ITEMTAGS:
            tableName = ItemTag.TABLE_NAME;
            contentUri = ItemTag.CONTENT_URI;
            break;
        case UM_SUBSCRIPTIONS:
            tableName = Subscription.TABLE_NAME;
            contentUri = Subscription.CONTENT_URI;
            break;
        case UM_SUBSCRIPTIONTAGS:
            tableName = SubscriptionTag.TABLE_NAME;
            contentUri = SubscriptionTag.CONTENT_URI;
            break;
        case UM_SETTINGS:
            tableName = Setting.TABLE_NAME;
            contentUri = Setting.CONTENT_URI;
            break;
        case UM_TAGS:
            tableName = Tag.TABLE_NAME;
            contentUri = Tag.CONTENT_URI;
            break;
        case UM_TRANSACTIONS:
            tableName = Transaction.TABLE_NAME;
            contentUri = Transaction.CONTENT_URI;
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        final SQLiteDatabase database = mDbHelper.getWritableDatabase();
        try {
            final long rowId = database.insertOrThrow(tableName, tableName, values);
            if (rowId > 0) {
                final Uri insertedUri = ContentUris.withAppendedId(contentUri, rowId);
                return insertedUri;
            }
        } catch (final SQLiteConstraintException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean onCreate() throws SQLException {
        DBOpenHelper.init(getContext());
        mDbHelper = DBOpenHelper.getInstance();
        return true;
    }

    @Override
    public Cursor query(final Uri uri, final String[] projection, String selection, final String[] selectionArgs,
            String sortOrder) {
        final String tableName;
        final int match = uriMatcher.match(uri);
        if (match == UM_ITEMS_WITH_OFFSET || match == UM_ITEMS_WITH_LIMIT || match == UM_ITEMS_WITH_OFFSET_LIMIT) {
            final SQLiteDatabase database = mDbHelper.getReadableDatabase();
            final String limit;
            if (match == UM_ITEMS_WITH_OFFSET) {
                limit = " LIMIT 1000000 OFFSET " + uri.getPathSegments().get(2);
            } else if (match == UM_ITEMS_WITH_LIMIT) {
                limit = " LIMIT " + uri.getPathSegments().get(2);
            } else {
                limit = " LIMIT " + uri.getPathSegments().get(3) + " OFFSET " + uri.getPathSegments().get(2);
            }
            if (selection == null) {
                selection = "";
            } else if (!selection.equals("")) {
                selection = " WHERE " + selection;
            }
            if (sortOrder == null) {
                sortOrder = "";
            } else if (!sortOrder.equals("")) {
                sortOrder = " ORDER BY " + sortOrder;
            }
            return database.rawQuery("SELECT " + toProjectionString(projection) + " FROM " + Item.TABLE_NAME
                    + selection + sortOrder + limit, selectionArgs);
        } else if (match == UM_ITEM_TAGS) {
            final SQLiteDatabase database = mDbHelper.getReadableDatabase();
            @SuppressWarnings("deprecation")
            final String uid = URLDecoder.decode(uri.getPathSegments().get(2));
            final String pro = toProjectionString(projection);
            selection = appendWhere(selection, ItemTag._ITEMUID, uid);
            if (sortOrder == null) {
                sortOrder = "";
            } else if (!sortOrder.equals("")) {
                sortOrder = " ORDER BY " + sortOrder;
            }
            return database.rawQuery("SELECT " + pro + " FROM " + Tag.TABLE_NAME + " INNER JOIN " + ItemTag.TABLE_NAME
                    + " ON " + ItemTag._TAGUID + "=" + Tag._UID + " WHERE " + selection + sortOrder, selectionArgs);
        } else if (match == UM_TAG_ITEMS) {
            final SQLiteDatabase database = mDbHelper.getReadableDatabase();
            @SuppressWarnings("deprecation")
            final String uid = URLDecoder.decode(uri.getPathSegments().get(2));
            final String pro = toProjectionString(projection);
            selection = appendWhere(selection, ItemTag._TAGUID, uid);
            if (sortOrder == null) {
                sortOrder = "";
            } else if (!sortOrder.equals("")) {
                sortOrder = " ORDER BY " + sortOrder;
            }
            return database.rawQuery("SELECT " + pro + " FROM " + Item.TABLE_NAME + " INNER JOIN " + ItemTag.TABLE_NAME
                    + " ON " + ItemTag._ITEMUID + "=" + Item._UID + " WHERE " + selection + sortOrder, selectionArgs);
        } else {
            switch (match) {
            case UM_ITEMS:
                tableName = Item.TABLE_NAME;
                break;
            case UM_ITEMTAGS:
                tableName = ItemTag.TABLE_NAME;
                break;
            case UM_SUBSCRIPTIONS:
                tableName = Subscription.TABLE_NAME;
                break;
            case UM_SUBSCRIPTIONTAGS:
                tableName = SubscriptionTag.TABLE_NAME;
                break;
            case UM_SETTINGS:
                tableName = Setting.TABLE_NAME;
                break;
            case UM_TAGS:
                tableName = Tag.TABLE_NAME;
                break;
            case UM_TRANSACTIONS:
                tableName = Transaction.TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
            }
            final SQLiteDatabase database = mDbHelper.getReadableDatabase();
            return database.query(tableName, projection, selection, selectionArgs, null, null, sortOrder);
        }
    }

    @Override
    public int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {
        update(uri, values, selection, selectionArgs, true);
        return 0;
    }

    @SuppressWarnings("deprecation")
    private int update(final Uri uri, final ContentValues values, String where, final String[] whereArgs,
            final boolean update) {
        final String tableName;
        switch (uriMatcher.match(uri)) {
        case UM_ITEM_UID:
            tableName = Item.TABLE_NAME;
            where = appendWhere(where, Item._UID, URLDecoder.decode(uri.getPathSegments().get(2)));
            break;
        case UM_ITEMS:
            tableName = Item.TABLE_NAME;
            break;
        case UM_ITEMTAGS:
            tableName = ItemTag.TABLE_NAME;
            break;
        case UM_SUBSCRIPTION_UID:
            tableName = Subscription.TABLE_NAME;
            where = appendWhere(where, Subscription._UID, URLDecoder.decode(uri.getPathSegments().get(2)));
            break;
        case UM_SUBSCRIPTIONS:
            tableName = Subscription.TABLE_NAME;
            break;
        case UM_SUBSCRIPTIONTAGS:
            tableName = SubscriptionTag.TABLE_NAME;
            break;
        case UM_SETTING_NAME:
            tableName = Setting.TABLE_NAME;
            where = appendWhere(where, Setting._NAME, URLDecoder.decode(uri.getPathSegments().get(2)));
            break;
        case UM_SETTINGS:
            tableName = Setting.TABLE_NAME;
            break;
        case UM_TAG_UID:
            tableName = Tag.TABLE_NAME;
            where = appendWhere(where, Tag._UID, URLDecoder.decode(uri.getPathSegments().get(2)));
            break;
        case UM_TAGS:
            tableName = Tag.TABLE_NAME;
            break;
        case UM_TRANSACTION_ID:
            tableName = Transaction.TABLE_NAME;
            where = appendWhere(where, Transaction._ID, uri.getPathSegments().get(2));
            break;
        case UM_TRANSACTIONS:
            tableName = Transaction.TABLE_NAME;
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        final SQLiteDatabase database = mDbHelper.getWritableDatabase();
        final int count = update ? database.update(tableName, values, where, whereArgs) : database.delete(tableName,
                where, whereArgs);
        return count;
    }
}
