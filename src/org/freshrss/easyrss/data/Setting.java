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

import org.freshrss.easyrss.Utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class Setting implements Entity {
    public static final String TABLE_NAME = "settings";
    public static final Uri CONTENT_URI = Uri.parse(DataProvider.SETTING_CONTENT_URI);

    public static final String _NAME = "name";
    public static final String _VALUE = "value";
    public static final String[] COLUMNS = { _NAME, _VALUE };

    public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + _NAME
            + " PRIMARY KEY ," + _VALUE + " TEXT NOT NULL)";
    public static final String[][] INDEX_COLUMNS = { { _NAME } };

    public static final String SETTING_AUTH = "auth";
    public static final String SETTING_TOKEN = "token";
    public static final String SETTING_SERVER_URL = "serverUrl";
    public static final String SETTING_USERNAME = "username";
    public static final String SETTING_PASSWORD = "password";
    public static final String SETTING_SHOW_HELP = "showHelp";
    public static final String SETTING_IS_CLIENT_LOGIN = "isClientLogin";
    public static final String SETTING_FONT_SIZE = "fontSize";
    public static final String SETTING_TOKEN_EXPIRE_TIME = "tokenExpireTime";
    public static final String SETTING_ITEM_LIST_EXPIRE_TIME = "itemListExpireTime";
    public static final String SETTING_SUBSCRIPTION_LIST_EXPIRE_TIME = "subscriptionListExpireTime";
    public static final String SETTING_SYNC_INTERVAL = "syncInterval";
    public static final String SETTING_SYNC_METHOD = "syncMethod";
    public static final String SETTING_IMAGE_PREFETCHING = "imagePrefetching";
    public static final String SETTING_IMAGE_FETCHING = "imageFetching";
    public static final String SETTING_DESCENDING_ITEMS_ORDERING = "decendingItemsOrdering";
    public static final String SETTING_NOTIFICATION_ON = "notificationOn";
    public static final String SETTING_HTTPS_CONECTION = "httpsConnection";
    public static final String SETTING_IMMEDIATE_STATE_SYNCING = "immediateStateSyncing";
    public static final String SETTING_MARK_ALL_AS_READ_CONFIRMATION = "markAllAsReadConfirmation";
    public static final String SETTING_MAX_ITEMS = "maxItems";
    public static final String SETTING_THEME = "theme";
    public static final String SETTING_TAG_LIST_EXPIRE_TIME = "tagListExpireTime";
    public static final String SETTING_GLOBAL_VIEW_TYPE = "globalViewType";
    public static final String SETTING_GLOBAL_NEWEST_ITEM_TIMESTAMP = "globalNewestItemTimestamp";
    public static final String SETTING_GLOBAL_ITEM_UPDATE_TIME = "globalItemUpdateTime";
    public static final String SETTING_GLOBAL_ITEM_UNREAD_COUNT = "globalItemUnreadCount";
    public static final String SETTING_BROWSER_CHOICE = "browserChoice";
    public static final String SETTING_VOLUMN_KEY_SWITCHING = "volumnKeySwitching";

    public static Setting fromCursor(final Cursor cur) {
        return new Setting(Utils.getStringFromCursor(cur, Setting._NAME),
                Utils.getStringFromCursor(cur, Setting._VALUE));
    }

    private String name;
    private String value;

    public Setting() {
        init(null, null);
    }

    public Setting(final String name, final Integer value) {
        init(name, String.valueOf(value));
    }

    public Setting(final String name, final Long value) {
        init(name, String.valueOf(value));
    }

    public Setting(final String name, final String value) {
        init(name, value);
    }

    @Override
    public void clear() {
        init(null, null);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    private void init(final String name, final String value) {
        this.name = name;
        this.value = value;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    @Override
    public ContentValues toContentValues() {
        final ContentValues ret = new ContentValues(2);
        ret.put(_NAME, name);
        ret.put(_VALUE, value);
        return ret;
    }

    @Override
    public ContentValues toUpdateContentValues() {
        final ContentValues ret = new ContentValues(1);
        ret.put(_VALUE, value);
        return ret;
    }
}
