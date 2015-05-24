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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.freshrss.easyrss.Utils;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

final public class DataMgr {
    final private static Handler handler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            if (instance != null) {
                switch (msg.what) {
                case MSG_ITEM_UPDATED:
                    for (final OnItemUpdatedListener listener : instance.itemListeners) {
                        listener.onItemUpdated((Item) msg.obj);
                    }
                    break;
                case MSG_SETTING_UPDATED:
                    for (final OnSettingUpdatedListener listener : instance.settingListeners) {
                        listener.onSettingUpdated((String) msg.obj);
                    }
                    break;
                case MSG_SUBSCRIPTION_UPDATED:
                    for (final OnSubscriptionUpdatedListener listener : instance.subscriptionListeners) {
                        listener.onSubscriptionUpdated((Subscription) msg.obj);
                    }
                    break;
                case MSG_TAG_UPDATED:
                    for (final OnTagUpdatedListener listener : instance.tagListeners) {
                        listener.onTagUpdated((Tag) msg.obj);
                    }
                    break;
                default:
                    break;
                }
            }
        }
    };
    static private DataMgr instance = null;
    static private final int MSG_ITEM_UPDATED = 0;
    static private final int MSG_SETTING_UPDATED = 1;
    static private final int MSG_SUBSCRIPTION_UPDATED = 2;
    static private final int MSG_TAG_UPDATED = 3;

    public static DataMgr getInstance() {
        return instance;
    }

    public static synchronized void init(final Context context) {
        if (instance == null) {
            instance = new DataMgr(context);
        }
    }

    final private Context context;
    final private DBOpenHelper dbOpenHelper;
    final private List<OnItemUpdatedListener> itemListeners;
    final private List<OnSettingUpdatedListener> settingListeners;
    final private List<OnSubscriptionUpdatedListener> subscriptionListeners;
    final private List<OnTagUpdatedListener> tagListeners;

    private DataMgr(final Context context) {
        this.context = context;
        this.itemListeners = new LinkedList<OnItemUpdatedListener>();
        this.subscriptionListeners = new LinkedList<OnSubscriptionUpdatedListener>();
        this.settingListeners = new LinkedList<OnSettingUpdatedListener>();
        this.tagListeners = new LinkedList<OnTagUpdatedListener>();

        DBOpenHelper.init(context);
        dbOpenHelper = DBOpenHelper.getInstance();
    }

    public void addItem(final Item item) {
        final ContentResolver resolver = context.getContentResolver();
        final Cursor cur = resolver.query(Item.CONTENT_URI, new String[] { Item._UID }, Item._UID + "=?",
                new String[] { item.getUid() }, null);
        if (cur.getCount() == 0) {
            resolver.insert(Item.CONTENT_URI, item.toContentValues());
        } else {
            resolver.update(Item.CONTENT_URI, item.toUpdateContentValues(), Item._UID + "=?",
                    new String[] { item.getUid() });
        }
        cur.close();

        resolver.delete(ItemTag.CONTENT_URI, ItemTag._ITEMUID + "=?", new String[] { item.getUid() });
        final ItemTag itemTag = new ItemTag();
        itemTag.setItemUid(item.getUid());
        for (final String tag : item.getTags()) {
            itemTag.setTagUid(tag);
            resolver.insert(ItemTag.CONTENT_URI, itemTag.toContentValues());
        }
        notifyItemUpdated(item);
    }

    public void addItems(final List<Item> items) {
        addItems(items, null);
    }

    public void addItems(final List<Item> items, Long lastTimestamp) {
        final SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            for (final Item item : items) {
                final Cursor cur = database.query(Item.TABLE_NAME, new String[] { Item._UID }, Item._UID + "=?",
                        new String[] { item.getUid() }, null, null, null);
                if (cur.getCount() == 0) {
                    database.insertOrThrow(Item.TABLE_NAME, Item.TABLE_NAME, item.toContentValues());
                } else {
                    database.update(Item.TABLE_NAME, item.toUpdateContentValues(), Item._UID + "=?",
                            new String[] { item.getUid() });
                }
                cur.close();

                database.delete(ItemTag.TABLE_NAME, ItemTag._ITEMUID + "=?", new String[] { item.getUid() });
                for (final String tag : item.getTags()) {
                    database.execSQL(SQLConstants.INSERT_ITEM_TAG, new String[] { item.getUid(), tag });
                }
            }
            database.setTransactionSuccessful();
        } catch (final Exception exception) {
            exception.printStackTrace();
        } finally {
            database.endTransaction();
        }
        for (final Item item : items) {
            notifyItemUpdated(item);
        }
    }

    /*
     * This method need to be called in MAIN thread.
     */
    public void addOnItemUpdatedListener(final OnItemUpdatedListener listener) {
        itemListeners.add(listener);
    }

    /*
     * This method need to be called in MAIN thread.
     */
    public void addOnSettingUpdatedListener(final OnSettingUpdatedListener listener) {
        settingListeners.add(listener);
    }

    /*
     * This method need to be called in MAIN thread.
     */
    public void addOnSubscriptionUpdatedListener(final OnSubscriptionUpdatedListener listener) {
        subscriptionListeners.add(listener);
    }

    /*
     * This method need to be called in MAIN thread.
     */
    public void addOnTagUpdatedListener(final OnTagUpdatedListener listener) {
        tagListeners.add(listener);
    }

    private void addSubscription(final SQLiteDatabase database, final Subscription sub) throws Exception {
        final Cursor cur = database.query(Subscription.TABLE_NAME, new String[] { Subscription._UID,
                Subscription._ICON, Subscription._UNREADCOUNT }, Subscription._UID + "=?",
                new String[] { sub.getUid() }, null, null, null);
        if (cur.getCount() == 0) {
            database.insertOrThrow(Subscription.TABLE_NAME, Subscription.TABLE_NAME, sub.toContentValues());
        } else {
            database.update(Subscription.TABLE_NAME, sub.toUpdateContentValues(), Subscription._UID + "=?",
                    new String[] { sub.getUid() });
            cur.moveToFirst();
            sub.setIcon(cur.getBlob(1));
            sub.setUnreadCount(cur.getInt(2));
        }
        cur.close();

        database.delete(SubscriptionTag.TABLE_NAME, SubscriptionTag._SUBSCRIPTIONUID + "=?",
                new String[] { sub.getUid() });
        for (final String tag : sub.getTags()) {
            database.execSQL(SQLConstants.INSERT_SUBSCRIPTION_TAG, new String[] { sub.getUid(), tag });
        }
    }

    public void addSubscription(final Subscription sub) {
        final SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            addSubscription(database, sub);
            database.setTransactionSuccessful();
        } catch (final Exception exception) {
            exception.printStackTrace();
        } finally {
            database.endTransaction();
        }
        notifySubscriptionUpdated(sub);
    }

    public void addSubscriptions(final List<Subscription> subs) {
        final SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            for (final Subscription sub : subs) {
                addSubscription(database, sub);
            }
            database.setTransactionSuccessful();
        } catch (final Exception exception) {
            exception.printStackTrace();
        } finally {
            database.endTransaction();
        }
        for (final Subscription sub : subs) {
            notifySubscriptionUpdated(sub);
        }
    }

    public void addTag(final SQLiteDatabase database, final Tag tag) throws Exception {
        final Cursor cur = database.query(Tag.TABLE_NAME, new String[] { Tag._UID, Tag._UNREADCOUNT }, Tag._UID + "=?",
                new String[] { tag.getUid() }, null, null, null);
        if (cur.getCount() == 0) {
            database.insertOrThrow(Tag.TABLE_NAME, Tag.TABLE_NAME, tag.toContentValues());
        } else {
            database.update(Tag.TABLE_NAME, tag.toUpdateContentValues(), Tag._UID + "=?", new String[] { tag.getUid() });
            cur.moveToFirst();
            tag.setUnreadCount(cur.getInt(1));
        }
        cur.close();
    }

    public void addTag(final Tag tag) {
        final SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            addTag(tag);
            database.setTransactionSuccessful();
        } catch (final Exception exception) {
            exception.printStackTrace();
        } finally {
            database.endTransaction();
        }
        notifyTagUpdated(tag);
    }

    public void addTags(final List<Tag> tags) {
        final SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            for (final Tag tag : tags) {
                addTag(database, tag);
            }
            database.setTransactionSuccessful();
        } catch (final Exception exception) {
            exception.printStackTrace();
        } finally {
            database.endTransaction();
        }
        for (final Tag tag : tags) {
            notifyTagUpdated(tag);
        }
    }

    private void addTransaction(final SQLiteDatabase database, final Transaction transaction) {
        database.insertOrThrow(Transaction.TABLE_NAME, Transaction.TABLE_NAME, transaction.toContentValues());
    }

    public void addTransaction(final Transaction transaction) {
        final SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
        addTransaction(database, transaction);
    }

    public int calcGlobalUnreadItemCount() {
        final ContentResolver resolver = context.getContentResolver();
        final Cursor cur = resolver.query(Item.CONTENT_URI, new String[] { "count(*)" }, ItemState._ISREAD + "=0",
                new String[] {}, null);
        final int ret = (cur.moveToFirst()) ? cur.getInt(0) : 0;
        cur.close();
        return ret;
    }

    public int calcUnreadItemsCountByUid(final String uid) {
        final ContentResolver resolver = context.getContentResolver();
        int ret = 0;
        if (uid.startsWith("feed/")) {
            final Cursor cur = resolver.query(Item.CONTENT_URI, new String[] { "count(*)" }, ItemState._ISREAD
                    + "=0 AND " + Item._SOURCEURI + "=?", new String[] { uid }, null);
            ret = (cur.moveToFirst()) ? cur.getInt(0) : 0;
            cur.close();
        } else if (uid.indexOf("/label/") != -1) {
            Cursor cur;
            try {
                cur = resolver.query(
                        Uri.withAppendedPath(Tag.CONTENT_URI, "/items/" + URLEncoder.encode(uid, "UTF-8")),
                        new String[] { "count(*)" }, ItemState._ISREAD + "=0", new String[] {}, null);
                ret = (cur.moveToFirst()) ? cur.getInt(0) : 0;
                cur.close();
            } catch (final UnsupportedEncodingException exception) {
                exception.printStackTrace();
            }
        } else if (uid.endsWith("/state/com.google/reading-list")) {
            ret = calcGlobalUnreadItemCount();
        }
        return ret;
    }

    public void clearAll() {
        clearItems();
        final SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
        database.execSQL("DELETE FROM " + Subscription.TABLE_NAME);
        database.execSQL("DELETE FROM " + SubscriptionTag.TABLE_NAME);
        database.execSQL("DELETE FROM " + Tag.TABLE_NAME);
        database.execSQL("DELETE FROM " + Setting.TABLE_NAME);
    }

    public void clearItems() {
        final SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
        database.execSQL("DELETE FROM " + Item.TABLE_NAME);
        database.execSQL("DELETE FROM " + ItemTag.TABLE_NAME);
    }

    public Context getContext() {
        return context;
    }

    public int getGlobalUnreadCount() {
        final String ret = getSettingByName(Setting.SETTING_GLOBAL_ITEM_UNREAD_COUNT);
        return (ret == null) ? 0 : Integer.valueOf(ret);
    }

    public Item getItemByUid(final String uid) {
        return getItemByUid(uid, null);
    }

    public Item getItemByUid(final String uid, final String[] projection) {
        final ContentResolver resolver = context.getContentResolver();
        final Cursor cur = resolver.query(Item.CONTENT_URI, projection, Item._UID + "=?", new String[] { uid }, null);
        final Item ret = (cur.moveToFirst()) ? Item.fromCursor(cur) : null;
        cur.close();
        return ret;
    }

    public List<Tag> getItemTagsByUid(final String uid) {
        return getItemTagsByUid(uid, null);
    }

    public List<Tag> getItemTagsByUid(final String uid, final String[] projection) {
        final List<Tag> ret = new ArrayList<Tag>();
        final ContentResolver resolver = context.getContentResolver();
        try {
            final Cursor cur = resolver.query(
                    Uri.withAppendedPath(Item.CONTENT_URI, "/tags/" + URLEncoder.encode(uid, "UTF-8")), projection,
                    null, null, null);
            for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                ret.add(Tag.fromCursor(cur));
            }
            cur.close();
        } catch (final UnsupportedEncodingException exception) {
            exception.printStackTrace();
        }
        return ret;
    }

    public String getSettingByName(final String name) {
        final ContentResolver resolver = context.getContentResolver();
        final Cursor cur = resolver.query(Setting.CONTENT_URI, null, Setting._NAME + "=?", new String[] { name }, null);
        final String ret = (cur.moveToFirst()) ? cur.getString(cur.getColumnIndex(Setting._VALUE)) : null;
        cur.close();
        return ret;
    }

    public Subscription getSubscriptionByUid(final String uid) {
        return getSubscriptionByUid(uid, null);
    }

    public Subscription getSubscriptionByUid(final String uid, final String[] projection) {
        final ContentResolver resolver = context.getContentResolver();
        final Cursor cur = resolver.query(Subscription.CONTENT_URI, projection, Subscription._UID + "=?",
                new String[] { uid }, null);
        final Subscription ret = (cur.moveToFirst()) ? Subscription.fromCursor(cur) : null;
        cur.close();
        return ret;
    }

    public Tag getTagByUid(final String uid) {
        return getTagByUid(uid, null);
    }

    public Tag getTagByUid(final String uid, final String[] projection) {
        final ContentResolver resolver = context.getContentResolver();
        final Cursor cur = resolver.query(Tag.CONTENT_URI, projection, Tag._UID + "=?", new String[] { uid }, null);
        final Tag ret = (cur.moveToFirst()) ? Tag.fromCursor(cur) : null;
        cur.close();
        return ret;
    }

    public int getUnreadCountByUid(final String uid) {
        int ret = 0;
        if (uid.startsWith("feed/")) {
            final Subscription sub = getSubscriptionByUid(uid);
            ret = (sub == null) ? 0 : sub.getUnreadCount();
        } else if (uid.indexOf("/label/") != -1) {
            final Tag tag = getTagByUid(uid);
            ret = (tag == null) ? 0 : tag.getUnreadCount();
        } else if (uid.endsWith("/state/com.google/reading-list")) {
            ret = getGlobalUnreadCount();
        }
        return ret;
    }

    public void markAllItemsAsRead() {
        final ContentResolver resolver = context.getContentResolver();
        final ContentValues values = new ContentValues();
        values.put(ItemState._ISREAD, true);
        resolver.update(Item.CONTENT_URI, values, ItemState._ISREAD + "=0", null);
    }

    public void markAllItemsAsRead(final SQLiteDatabase database) {
        final ContentValues values = new ContentValues();
        values.put(ItemState._ISREAD, true);
        database.update(Item.TABLE_NAME, values, ItemState._ISREAD + "=0", null);
    }

    public void markItemAsReadWithTransactionByUid(final String uid) {
        final Item item = getItemByUid(uid);
        if (item == null || item.getState().isRead()) {
            return;
        }
        item.getState().setRead(true);
        final SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            database.execSQL(SQLConstants.MARK_ITEM_AS_READ, new String[] { uid });
            addTransaction(database, new Transaction(Item.getFullUid(uid), null, Transaction.TYPE_SET_READ));
            database.setTransactionSuccessful();
        } catch (final Exception exception) {
            exception.printStackTrace();
        } finally {
            database.endTransaction();
        }
        notifySettingUpdated(Setting.SETTING_GLOBAL_ITEM_UNREAD_COUNT);
        notifySubscriptionUpdated(getSubscriptionByUid(item.getSourceUri()));
        final Cursor cur = database.rawQuery(SQLConstants.SELECT_ITEM_TAGS, new String[] { uid });
        for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
            notifyTagUpdated(Tag.fromCursor(cur));
        }
        cur.close();
        notifyItemUpdated(item);
    }

    public void markItemAsStarredWithTransactionByUid(final String uid, final boolean isStarred) {
        final ContentResolver resolver = context.getContentResolver();
        final ContentValues values = new ContentValues();
        values.put(ItemState._ISSTARRED, isStarred);
        resolver.update(Item.CONTENT_URI, values, Item._UID + "=?", new String[] { uid });
        addTransaction(new Transaction(Item.getFullUid(uid), null, (isStarred) ? Transaction.TYPE_SET_STARRED
                : Transaction.TYPE_REMOVE_STARRED));
        notifyItemUpdated(getItemByUid(uid));
    }

    public void markItemAsUnreadWithTransactionByUid(final String uid) {
        final Item item = getItemByUid(uid);
        if (item == null || !item.getState().isRead()) {
            return;
        }
        item.getState().setRead(false);
        final SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            database.execSQL(
                    "UPDATE " + Item.TABLE_NAME + " SET " + ItemState._ISREAD + "=0 WHERE " + Item._UID + "=?",
                    new String[] { uid });
            database.execSQL("UPDATE " + Subscription.TABLE_NAME + " SET " + Subscription._UNREADCOUNT + "="
                    + Subscription._UNREADCOUNT + "+1 WHERE " + Subscription._UID + "=?",
                    new String[] { item.getSourceUri() });
            database.execSQL(SQLConstants.INCREASE_TAG_UNREAD_COUNT, new String[] { item.getUid() });
            database.setTransactionSuccessful();
            final String sGlobalUC = getSettingByName(Setting.SETTING_GLOBAL_ITEM_UNREAD_COUNT);
            final int globalUC = ((sGlobalUC == null) ? 0 : Integer.valueOf(sGlobalUC)) + 1;
            updateSetting(database, new Setting(Setting.SETTING_GLOBAL_ITEM_UNREAD_COUNT, globalUC));
            addTransaction(new Transaction(Item.getFullUid(uid), null, Transaction.TYPE_REMOVE_READ));
        } catch (final Exception exception) {
            exception.printStackTrace();
        } finally {
            database.endTransaction();
        }
        notifySettingUpdated(Setting.SETTING_GLOBAL_ITEM_UNREAD_COUNT);
        notifySubscriptionUpdated(getSubscriptionByUid(item.getSourceUri()));
        final Cursor cur = database.rawQuery(SQLConstants.SELECT_ITEM_TAGS, new String[] { uid });
        for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
            notifyTagUpdated(Tag.fromCursor(cur));
        }
        notifyItemUpdated(item);
    }

    public void markItemsAsReadByTimestampRange(final Long tLow, final Long tHigh) {
        if (tLow == null && tHigh == null) {
            markAllItemsAsRead();
            return;
        }
        final ContentResolver resolver = context.getContentResolver();
        final ContentValues values = new ContentValues();
        values.put(ItemState._ISREAD, true);
        final StringBuilder buff = new StringBuilder(128);
        if (tLow == null) {
            buff.append(Item._TIMESTAMP + "<");
            buff.append(tHigh);
        } else if (tHigh == null) {
            buff.append(Item._TIMESTAMP + ">");
            buff.append(tLow);
        } else {
            buff.append(Item._TIMESTAMP + "<");
            buff.append(tHigh);
            buff.append(" AND " + Item._TIMESTAMP + ">");
            buff.append(tLow);
        }
        resolver.update(Item.CONTENT_URI, values, buff.toString(), null);
    }

    public void markItemsAsReadByTimestampRange(final SQLiteDatabase database, final Long tLow, final Long tHigh) {
        if (tLow == null && tHigh == null) {
            markAllItemsAsRead(database);
            return;
        }
        final ContentValues values = new ContentValues();
        values.put(ItemState._ISREAD, true);
        final StringBuilder buff = new StringBuilder(128);
        if (tLow == null) {
            buff.append(Item._TIMESTAMP + "<");
            buff.append(tHigh);
        } else if (tHigh == null) {
            buff.append(Item._TIMESTAMP + ">");
            buff.append(tLow);
        } else {
            buff.append(Item._TIMESTAMP + "<");
            buff.append(tHigh);
            buff.append(" AND " + Item._TIMESTAMP + ">");
            buff.append(tLow);
        }
        database.update(Item.TABLE_NAME, values, buff.toString(), null);
    }

    public void markItemsAsReadItemIds(final List<ItemId> itemIds) {
        markItemsAsReadItemIds(itemIds, 0, itemIds.size(), false);
    }

    public void markItemsAsReadItemIds(final List<ItemId> itemIds, int left, int right) {
        markItemsAsReadItemIds(itemIds, left, right, false);
    }

    public void markItemsAsReadItemIds(final List<ItemId> itemIds, int left, int right, boolean markPreviousAsRead) {
        final SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            Long lastTimestamp = (left > 0) ? (itemIds.get(left - 1).getTimestamp()) : null;
            final ContentValues values = new ContentValues();
            values.put(ItemState._ISREAD, false);
            database.execSQL(SQLConstants.DROP_TRIGGER_MARK_ITEM_AS_READ);
            for (int i = left; i < right; i++) {
                final ItemId curItemId = itemIds.get(i);
                markItemsAsReadByTimestampRange(database, curItemId.getTimestamp(), lastTimestamp);
                database.update(Item.TABLE_NAME, values, Item._UID + "=?", new String[] { curItemId.getUid() });
                lastTimestamp = curItemId.getTimestamp();
            }
            if (markPreviousAsRead) {
                markItemsAsReadByTimestampRange(database, null, lastTimestamp);
            }
            database.execSQL(SQLConstants.CREATE_TRIGGER_MARK_ITEM_AS_READ);
            database.setTransactionSuccessful();
        } catch (final Exception exception) {
            exception.printStackTrace();
        } finally {
            database.endTransaction();
        }
    }

    /*
     * This method will not send any signals like "ItemUpdated".
     */
    public void markItemsAsReadWithTransaction(final List<Item> items) {
        final HashSet<String> tagsApp = new HashSet<String>();
        final HashSet<String> subscriptionsApp = new HashSet<String>();
        final SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            for (final Item item : items) {
                database.execSQL(SQLConstants.MARK_ITEM_AS_READ, new String[] { item.getUid() });
                addTransaction(database, new Transaction(item.getFullUid(), null, Transaction.TYPE_SET_READ));
                subscriptionsApp.add(item.getSourceUri());
                final Cursor cur = database.rawQuery(SQLConstants.SELECT_ITEM_TAGS_UID, new String[] { item.getUid() });
                for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                    tagsApp.add(cur.getString(0));
                }
                cur.close();
            }
            database.setTransactionSuccessful();
        } catch (final Exception exception) {
            exception.printStackTrace();
        } finally {
            database.endTransaction();
        }
        for (final String uid : tagsApp) {
            notifyTagUpdated(getTagByUid(uid));
        }
        for (final String uid : subscriptionsApp) {
            notifySubscriptionUpdated(getSubscriptionByUid(uid));
        }
        notifySettingUpdated(Setting.SETTING_GLOBAL_ITEM_UNREAD_COUNT);
    }

    private void notifyItemUpdated(final Item item) {
        if (item != null) {
            handler.sendMessage(Message.obtain(handler, MSG_ITEM_UPDATED, item));
        }
    }

    private void notifySettingUpdated(final String name) {
        handler.sendMessage(Message.obtain(handler, MSG_SETTING_UPDATED, name));
    }

    private void notifySubscriptionUpdated(final Subscription sub) {
        if (sub != null) {
            handler.sendMessage(Message.obtain(handler, MSG_SUBSCRIPTION_UPDATED, sub));
        }
    }

    private void notifyTagUpdated(final Tag tag) {
        if (tag != null) {
            handler.sendMessage(Message.obtain(handler, MSG_TAG_UPDATED, tag));
        }
    }

    public int removeItemByUid(final SQLiteDatabase database, final String uid) {
        final int ret = database.delete(Item.TABLE_NAME, Item._UID + "=?", new String[] { uid });
        database.delete(ItemTag.TABLE_NAME, ItemTag._ITEMUID + "=?", new String[] { uid });
        DataUtils.deleteFile(new File(Item.getStoragePathByUid(uid)));
        return ret;
    }

    public int removeItemByUid(final String uid) {
        final ContentResolver resolver = context.getContentResolver();
        final int ret = resolver.delete(Item.CONTENT_URI, Item._UID + "=?", new String[] { uid });
        resolver.delete(ItemTag.CONTENT_URI, ItemTag._ITEMUID + "=?", new String[] { uid });
        DataUtils.deleteFile(new File(Item.getStoragePathByUid(uid)));
        return ret;
    }

    /*
     * This method need to be called in MAIN thread.
     */
    public void removeOnItemUpdatedListener(final OnItemUpdatedListener listener) {
        itemListeners.remove(listener);
    }

    /*
     * This method need to be called in MAIN thread.
     */
    public void removeOnSettingUpdatedListener(final OnSettingUpdatedListener listener) {
        settingListeners.remove(listener);
    }

    /*
     * This method need to be called in MAIN thread.
     */
    public void removeOnSubscriptionUpdatedListener(final OnSubscriptionUpdatedListener listener) {
        subscriptionListeners.remove(listener);
    }

    /*
     * This method need to be called in MAIN thread.
     */
    public void removeOnTagUpdatedListener(final OnTagUpdatedListener listener) {
        tagListeners.remove(listener);
    }

    public int removeOutdatedItemsWithLimit(final int limit) {
        int ret = 0;
        final ContentResolver resolver = context.getContentResolver();
        final Cursor cur = resolver.query(Uri.withAppendedPath(Item.CONTENT_URI, "offset/" + limit),
                new String[] { Item._UID }, null, null, Item._UPDATETIME + " DESC");
        for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
            ret += removeItemByUid(Item.fromCursor(cur).getUid());
        }
        cur.close();
        return ret;
    }

    public int removeOutdatedSubscriptions(final long updateTime) {
        int ret = 0;
        final ContentResolver resolver = context.getContentResolver();
        final Cursor cur = resolver.query(Subscription.CONTENT_URI, new String[] { Subscription._UID },
                Subscription._UPDATETIME + "<?", new String[] { String.valueOf(updateTime) }, null);
        final SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                ret += removeSubscriptionByUid(Utils.getStringFromCursor(cur, Subscription._UID));
            }
            database.setTransactionSuccessful();
        } catch (final Exception exception) {
            exception.printStackTrace();
        } finally {
            database.endTransaction();
        }
        cur.close();
        return ret;
    }

    public int removeOutdatedTags(final long updateTime) {
        final ContentResolver resolver = context.getContentResolver();
        return resolver.delete(Tag.CONTENT_URI, Tag._UPDATETIME + "<?", new String[] { String.valueOf(updateTime) });
    }

    public void removeOutdatedUnreadCounts(final long updateTime) {
        final ContentResolver resolver = context.getContentResolver();
        final ContentValues values = new ContentValues();
        values.put(Subscription._UNREADCOUNT, 0);
        resolver.update(Subscription.CONTENT_URI, values, Subscription._UPDATETIME + "<?",
                new String[] { String.valueOf(updateTime) });
        values.clear();
        values.put(Tag._UNREADCOUNT, 0);
        resolver.update(Tag.CONTENT_URI, values, Tag._UPDATETIME + "<?", new String[] { String.valueOf(updateTime) });
    }

    public int removeSettingByName(final String name) {
        final ContentResolver resolver = context.getContentResolver();
        return resolver.delete(Setting.CONTENT_URI, Setting._NAME + "=?", new String[] { name });
    }

    public int removeSubscriptionByUid(final SQLiteDatabase database, final String uid) {
        final int ret = database.delete(Subscription.TABLE_NAME, Subscription._UID + "=?", new String[] { uid });
        database.delete(SubscriptionTag.TABLE_NAME, SubscriptionTag._SUBSCRIPTIONUID + "=?", new String[] { uid });
        return ret;
    }

    public int removeSubscriptionByUid(final String uid) {
        final ContentResolver resolver = context.getContentResolver();
        final int ret = resolver.delete(Subscription.CONTENT_URI, Subscription._UID + "=?", new String[] { uid });
        resolver.delete(SubscriptionTag.CONTENT_URI, SubscriptionTag._SUBSCRIPTIONUID + "=?", new String[] { uid });
        return ret;
    }

    public void removeTransactionById(final long id) {
        final ContentResolver resolver = context.getContentResolver();
        resolver.delete(Transaction.CONTENT_URI, Transaction._ID + "=?", new String[] { String.valueOf(id) });
    }

    public void updateSetting(final Setting setting) {
        updateSetting(dbOpenHelper.getWritableDatabase(), setting);
        notifySettingUpdated(setting.getName());
    }

    /*
     * Remember: the updating of setting is not notified here!
     */
    public void updateSetting(final SQLiteDatabase database, final Setting setting) {
        database.execSQL(SQLConstants.INSERT_OR_REPLACE_SETTING, new String[] { setting.getName(), setting.getValue() });
    }

    public void updateSubscriptionIconByUid(final String uid, final byte[] icon) {
        final ContentResolver resolver = context.getContentResolver();
        final ContentValues values = new ContentValues();
        values.put(Subscription._ICON, icon);
        resolver.update(Subscription.CONTENT_URI, values, Subscription._UID + "=?", new String[] { uid });
        notifySubscriptionUpdated(getSubscriptionByUid(uid));
    }

    public void updateUnreadCount(final UnreadCount unread) {
        final String uid = unread.getUid();
        final ContentResolver resolver = context.getContentResolver();
        final ContentValues values = new ContentValues();
        if (uid.startsWith("feed/")) {
            values.put(Subscription._UNREADCOUNT, unread.getCount());
            values.put(Subscription._UPDATETIME, System.currentTimeMillis());
            resolver.update(Subscription.CONTENT_URI, values, Subscription._UID + "=?", new String[] { uid });
            notifySubscriptionUpdated(getSubscriptionByUid(uid));
        } else if (uid.indexOf("/label/") != -1) {
            values.put(Tag._UNREADCOUNT, unread.getCount());
            values.put(Tag._UPDATETIME, System.currentTimeMillis());
            resolver.update(Tag.CONTENT_URI, values, Tag._UID + "=?", new String[] { uid });
            notifyTagUpdated(getTagByUid(uid));
        } else if (uid.endsWith("/state/com.google/reading-list")) {
            updateSetting(new Setting(Setting.SETTING_GLOBAL_ITEM_UNREAD_COUNT, unread.getCount()));
            updateSetting(new Setting(Setting.SETTING_GLOBAL_ITEM_UPDATE_TIME, System.currentTimeMillis()));
        }
    }

    public void updateUnreadCounts(final List<UnreadCount> unreadCounts) {
        final SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            for (final UnreadCount unread : unreadCounts) {
                final String uid = unread.getUid();
                final ContentValues values = new ContentValues();
                if (uid.startsWith("feed/")) {
                    values.put(Subscription._UNREADCOUNT, unread.getCount());
                    values.put(Subscription._UPDATETIME, System.currentTimeMillis());
                    database.update(Subscription.TABLE_NAME, values, Subscription._UID + "=?", new String[] { uid });
                } else if (uid.indexOf("/label/") != -1) {
                    values.put(Tag._UNREADCOUNT, unread.getCount());
                    values.put(Tag._UPDATETIME, System.currentTimeMillis());
                    database.update(Tag.TABLE_NAME, values, Tag._UID + "=?", new String[] { uid });
                } else if (uid.endsWith("/state/com.google/reading-list")) {
                    updateSetting(database, new Setting(Setting.SETTING_GLOBAL_ITEM_UNREAD_COUNT, unread.getCount()));
                    updateSetting(database,
                            new Setting(Setting.SETTING_GLOBAL_ITEM_UPDATE_TIME, System.currentTimeMillis()));
                }
            }
            database.setTransactionSuccessful();
            notifySettingUpdated(Setting.SETTING_GLOBAL_ITEM_UNREAD_COUNT);
            final ContentResolver resolver = context.getContentResolver();
            {
                final Cursor cur = resolver.query(Tag.CONTENT_URI, null, null, null, null);
                for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                    notifyTagUpdated(Tag.fromCursor(cur));
                }
                cur.close();
            }
            {
                final Cursor cur = resolver.query(Subscription.CONTENT_URI, null, null, null, null);
                for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                    notifySubscriptionUpdated(Subscription.fromCursor(cur));
                }
                cur.close();
            }
        } catch (final Exception exception) {
            exception.printStackTrace();
        } finally {
            database.endTransaction();
        }
    }
}
