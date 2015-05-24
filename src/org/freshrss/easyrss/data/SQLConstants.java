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

final public class SQLConstants {
    final public static String CREATE_TRIGGER_DELETE_REDUNDENT_TRANSACTION;
    final public static String CREATE_TRIGGER_MARK_ITEM_AS_READ;
    final public static String DROP_TRIGGER_MARK_ITEM_AS_READ;
    final public static String INCREASE_TAG_UNREAD_COUNT;
    final public static String INSERT_ITEM_TAG;
    final public static String INSERT_OR_REPLACE_SETTING;
    final public static String INSERT_SUBSCRIPTION_TAG;
    final public static String MARK_ITEM_AS_READ;
    final public static String SELECT_ITEM_TAGS;
    final public static String SELECT_ITEM_TAGS_UID;
    final public static String UPGRADE_ITEM_TAGS_ITEM_UID;
    final public static String UPGRADE_ITEMS_UID;

    // CREATE TRIGGER IF NOT EXISTS delete_redundent_transactions INSERT ON
    // transactions BEGIN
    // DELETE FROM transactions WHERE uid=new.uid AND type=new.type;
    // END;
    static {
        final StringBuffer buffer = new StringBuffer();
        buffer.append("CREATE TRIGGER IF NOT EXISTS delete_redundent_transactions INSERT ON ");
        buffer.append(Transaction.TABLE_NAME);
        buffer.append(" BEGIN DELETE FROM ");
        buffer.append(Transaction.TABLE_NAME);
        buffer.append(" WHERE ");
        buffer.append(Transaction._UID);
        buffer.append("=new.");
        buffer.append(Transaction._UID);
        buffer.append(" AND ");
        buffer.append(Transaction._TYPE);
        buffer.append("=new.");
        buffer.append(Transaction._TYPE);
        buffer.append(";END;");
        CREATE_TRIGGER_DELETE_REDUNDENT_TRANSACTION = buffer.toString();
    }

    // CREATE TRIGGER IF NOT EXISTS mark_item_as_read UPDATE OF isRead ON items
    // FOR EACH ROW WHEN new.isRead=1 AND old.isRead=0 BEGIN
    // UPDATE tags SET unreadCount=unreadCount-1 WHERE uid IN (SELECT tagUid
    // FROM itemTags WHERE itemUid=new.uid) AND unreadCount<1000 AND
    // unreadCount>0;
    // UPDATE subscriptions SET unreadCount=unreadCount-1 WHERE
    // uid=new.sourceUri AND unreadCount<1000 AND unreadCount>0;
    // UPDATE settings SET value=value-1 WHERE name='globalItemUnreadCount' AND
    // CAST(value AS INT)>0 AND CAST(value AS INT)<1000;
    // INSERT INTO transactions (uid,type) VALUES (new.uid,0);
    // END;
    static {
        final StringBuffer buffer = new StringBuffer();
        // Declare the trigger
        buffer.append("CREATE TRIGGER IF NOT EXISTS mark_item_as_read UPDATE OF ");
        buffer.append(ItemState._ISREAD);
        buffer.append(" ON ");
        buffer.append(Item.TABLE_NAME);
        buffer.append(" FOR EACH ROW WHEN new.");
        buffer.append(ItemState._ISREAD);
        buffer.append("=1 AND old.");
        buffer.append(ItemState._ISREAD);
        buffer.append("=0 BEGIN\n");
        // Update unreadCount of tags
        buffer.append("UPDATE ");
        buffer.append(Tag.TABLE_NAME);
        buffer.append(" SET ");
        buffer.append(Tag._UNREADCOUNT);
        buffer.append("=");
        buffer.append(Tag._UNREADCOUNT);
        buffer.append("-1 WHERE ");
        buffer.append(Tag._UID);
        buffer.append(" IN (SELECT ");
        buffer.append(ItemTag._TAGUID);
        buffer.append(" FROM ");
        buffer.append(ItemTag.TABLE_NAME);
        buffer.append(" WHERE ");
        buffer.append(ItemTag._ITEMUID);
        buffer.append("=new.");
        buffer.append(Item._UID);
        buffer.append(") AND ");
        buffer.append(Tag._UNREADCOUNT);
        buffer.append("<1000 AND ");
        buffer.append(Tag._UNREADCOUNT);
        buffer.append(">0;\n");
        // Update unreadCount of subscription
        buffer.append("UPDATE ");
        buffer.append(Subscription.TABLE_NAME);
        buffer.append(" SET ");
        buffer.append(Subscription._UNREADCOUNT);
        buffer.append("=");
        buffer.append(Subscription._UNREADCOUNT);
        buffer.append("-1 WHERE ");
        buffer.append(Subscription._UID);
        buffer.append("=new.");
        buffer.append(Item._SOURCEURI);
        buffer.append(" AND ");
        buffer.append(Subscription._UNREADCOUNT);
        buffer.append("<1000 AND ");
        buffer.append(Subscription._UNREADCOUNT);
        buffer.append(">0;\n");
        // Update Setting globalItemUnreadCount
        buffer.append("UPDATE ");
        buffer.append(Setting.TABLE_NAME);
        buffer.append(" SET ");
        buffer.append(Setting._VALUE);
        buffer.append("=");
        buffer.append(Setting._VALUE);
        buffer.append("-1 WHERE ");
        buffer.append(Setting._NAME);
        buffer.append("='");
        buffer.append(Setting.SETTING_GLOBAL_ITEM_UNREAD_COUNT);
        buffer.append("' AND CAST(");
        buffer.append(Setting._VALUE);
        buffer.append(" AS INT)>0 AND CAST(");
        buffer.append(Setting._VALUE);
        buffer.append(" AS INT)<1000;\n");
        // End
        buffer.append("END;");
        CREATE_TRIGGER_MARK_ITEM_AS_READ = buffer.toString();
    }

    static {
        DROP_TRIGGER_MARK_ITEM_AS_READ = "DROP TRIGGER IF EXISTS mark_item_as_read";
    }

    static {
        final StringBuffer buffer = new StringBuffer();
        buffer.append("UPDATE ");
        buffer.append(Tag.TABLE_NAME);
        buffer.append(" SET ");
        buffer.append(Tag._UNREADCOUNT);
        buffer.append("=");
        buffer.append(Tag._UNREADCOUNT);
        buffer.append("+1 WHERE ");
        buffer.append(Tag._UID);
        buffer.append(" IN(SELECT ");
        buffer.append(ItemTag._TAGUID);
        buffer.append(" FROM ");
        buffer.append(ItemTag.TABLE_NAME);
        buffer.append(" WHERE ");
        buffer.append(ItemTag._ITEMUID);
        buffer.append("=?) AND ");
        buffer.append(Tag._UNREADCOUNT);
        buffer.append("<1000");
        INCREASE_TAG_UNREAD_COUNT = buffer.toString();
    }

    static {
        final StringBuffer buffer = new StringBuffer();
        buffer.append("INSERT INTO ");
        buffer.append(ItemTag.TABLE_NAME);
        buffer.append("(");
        buffer.append(ItemTag._ITEMUID);
        buffer.append(",");
        buffer.append(ItemTag._TAGUID);
        buffer.append(")VALUES(?,?)");
        INSERT_ITEM_TAG = buffer.toString();
    }

    static {
        final StringBuilder builder = new StringBuilder();
        builder.append("INSERT OR REPLACE INTO ");
        builder.append(Setting.TABLE_NAME);
        builder.append('(');
        builder.append(Setting._NAME);
        builder.append(',');
        builder.append(Setting._VALUE);
        builder.append(") VALUES (?,?)");
        INSERT_OR_REPLACE_SETTING = builder.toString();
    }

    static {
        final StringBuffer buffer = new StringBuffer();
        buffer.append("INSERT INTO ");
        buffer.append(SubscriptionTag.TABLE_NAME);
        buffer.append("(");
        buffer.append(SubscriptionTag._SUBSCRIPTIONUID);
        buffer.append(",");
        buffer.append(SubscriptionTag._TAGUID);
        buffer.append(")VALUES(?,?)");
        INSERT_SUBSCRIPTION_TAG = buffer.toString();
    }

    static {
        final StringBuffer buffer = new StringBuffer();
        buffer.append("UPDATE ");
        buffer.append(Item.TABLE_NAME);
        buffer.append(" SET ");
        buffer.append(ItemState._ISREAD);
        buffer.append("=1 WHERE ");
        buffer.append(Item._UID);
        buffer.append("=?");
        MARK_ITEM_AS_READ = buffer.toString();
    }

    static {
        final StringBuffer buffer = new StringBuffer();
        buffer.append("SELECT * FROM ");
        buffer.append(Tag.TABLE_NAME);
        buffer.append(" WHERE ");
        buffer.append(Tag._UID);
        buffer.append(" IN(SELECT ");
        buffer.append(ItemTag._TAGUID);
        buffer.append(" FROM ");
        buffer.append(ItemTag.TABLE_NAME);
        buffer.append(" WHERE ");
        buffer.append(ItemTag._ITEMUID);
        buffer.append("=?)");
        SELECT_ITEM_TAGS = buffer.toString();
    }

    static {
        final StringBuffer buffer = new StringBuffer();
        buffer.append("SELECT ");
        buffer.append(ItemTag._TAGUID);
        buffer.append(" FROM ");
        buffer.append(ItemTag.TABLE_NAME);
        buffer.append(" WHERE ");
        buffer.append(ItemTag._ITEMUID);
        buffer.append("=?");
        SELECT_ITEM_TAGS_UID = buffer.toString();
    }

    /*
     * UPDATE itemTags SET itemUid = SUBSTR(itemUid, LENGTH(RTRIM(itemUid,
     * '0123456789abcdef')) + 1);
     */
    static {
        final StringBuffer buffer = new StringBuffer();
        buffer.append("UPDATE ");
        buffer.append(ItemTag.TABLE_NAME);
        buffer.append(" SET ");
        buffer.append(ItemTag._ITEMUID);
        buffer.append("=SUBSTR(");
        buffer.append(ItemTag._ITEMUID);
        buffer.append(",LENGTH(RTRIM(");
        buffer.append(ItemTag._ITEMUID);
        buffer.append(",'0123456789abcdef'))+1)");
        UPGRADE_ITEM_TAGS_ITEM_UID = buffer.toString();
    }

    /*
     * UPDATE items SET uid = SUBSTR(uid, LENGTH(RTRIM(uid, '0123456789abcdef'))
     * + 1);
     */
    static {
        final StringBuffer buffer = new StringBuffer();
        buffer.append("UPDATE ");
        buffer.append(Item.TABLE_NAME);
        buffer.append(" SET ");
        buffer.append(Item._UID);
        buffer.append("=SUBSTR(");
        buffer.append(Item._UID);
        buffer.append(",LENGTH(RTRIM(");
        buffer.append(Item._UID);
        buffer.append(",'0123456789abcdef'))+1)");
        UPGRADE_ITEMS_UID = buffer.toString();
    }

    private SQLConstants() {
    }
}
