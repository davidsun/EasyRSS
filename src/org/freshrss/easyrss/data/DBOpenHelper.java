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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper {
    final private static int DB_VERSION = 44;
    final private static Map<Integer, String[]> UPGRADE_SQL;

    static {
        UPGRADE_SQL = new HashMap<Integer, String[]>();
        UPGRADE_SQL.put(43, new String[] { SQLConstants.UPGRADE_ITEM_TAGS_ITEM_UID, SQLConstants.UPGRADE_ITEMS_UID });
    }

    private static DBOpenHelper instance;

    private static boolean checkDropTable(final SQLiteDatabase database, final String tableName, final String[] columns) {
        final List<String> cols = getTableColumns(database, tableName);
        if (cols == null) {
            return false;
        }
        if (cols.size() == columns.length) {
            for (final String col : cols) {
                int i = 0;
                for (; i < columns.length; i++) {
                    if (columns[i].equals(col)) {
                        break;
                    }
                }
                if (i == columns.length) {
                    return true;
                }
            }
        } else {
            return true;
        }
        return false;
    }

    private static void checkTableColumns(final SQLiteDatabase database, final String tableName,
            final String[] columns, final String[] columnTypes) {
        final List<String> cols = getTableColumns(database, tableName);
        for (int i = 0; i < columns.length; i++) {
            boolean found = false;
            for (final String col : cols) {
                if (col.equals(columns[i])) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                database.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + columns[i] + " " + columnTypes[i]);
            }
        }
    }

    public static DBOpenHelper getInstance() {
        return instance;
    }

    private static List<String> getTableColumns(final SQLiteDatabase database, final String tableName) {
        List<String> ret = null;
        try {
            final Cursor cur = database.rawQuery("SELECT * FROM " + tableName + " LIMIT 1", null);
            if (cur != null) {
                ret = new ArrayList<String>(Arrays.asList(cur.getColumnNames()));
            }
            cur.close();
        } catch (final Exception exception) {
            exception.printStackTrace();
        }
        return ret;
    }

    public static synchronized void init(final Context context) {
        if (instance == null) {
            instance = new DBOpenHelper(context);
        }
    }

    private static String sqlCreateIndex(final String tableName, final String[] columnNames) {
        final StringBuilder buff = new StringBuilder(128);
        buff.append("CREATE INDEX IF NOT EXISTS idx_");
        buff.append(tableName);
        for (int i = 0; i < columnNames.length; i++) {
            buff.append('_');
            buff.append(columnNames[i]);
        }
        buff.append(" ON ");
        buff.append(tableName);
        buff.append('(');
        for (int i = 0; i < columnNames.length; i++) {
            if (i > 0) {
                buff.append(',');
            }
            buff.append(columnNames[i]);
        }
        buff.append(')');
        return buff.toString();
    }

    private DBOpenHelper(final Context context) {
        super(context, "EasyRSSDB", null, DB_VERSION);
    }

    private String createTable(final String tableName, final String[] columns, final String[] columnsCreate) {
        final StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS ");
        builder.append(tableName);
        builder.append('(');
        for (int i = 0; i < columns.length; i++) {
            builder.append(columns[i]);
            builder.append(' ');
            builder.append(columnsCreate[i]);
            if (i + 1 < columns.length) {
                builder.append(',');
            }
        }
        builder.append(')');
        return builder.toString();
    }

    private void createTables(final SQLiteDatabase database) {
        database.execSQL(createTable(Item.TABLE_NAME, Item.COLUMNS, Item.COLUMNS_TYPE));
        database.execSQL(ItemTag.SQL_CREATE_TABLE);
        database.execSQL(createTable(Subscription.TABLE_NAME, Subscription.COLUMNS, Subscription.COLUMNS_TYPE));
        database.execSQL(SubscriptionTag.SQL_CREATE_TABLE);
        database.execSQL(Setting.SQL_CREATE_TABLE);
        database.execSQL(createTable(Tag.TABLE_NAME, Tag.COLUMNS, Tag.COLUMNS_TYPE));
        database.execSQL(createTable(Transaction.TABLE_NAME, Transaction.COLUMNS, Transaction.COLUMNS_TYPE));

        for (final String[] columns : Item.INDEX_COLUMNS) {
            database.execSQL(sqlCreateIndex(Item.TABLE_NAME, columns));
        }
        for (final String[] columns : SubscriptionTag.INDEX_COLUMNS) {
            database.execSQL(sqlCreateIndex(SubscriptionTag.TABLE_NAME, columns));
        }
        for (final String[] columns : Setting.INDEX_COLUMNS) {
            database.execSQL(sqlCreateIndex(Setting.TABLE_NAME, columns));
        }
        for (final String[] columns : Subscription.INDEX_COLUMNS) {
            database.execSQL(sqlCreateIndex(Subscription.TABLE_NAME, columns));
        }
        for (final String[] columns : ItemTag.INDEX_COLUMNS) {
            database.execSQL(sqlCreateIndex(ItemTag.TABLE_NAME, columns));
        }
        for (final String[] columns : Tag.INDEX_COLUMNS) {
            database.execSQL(sqlCreateIndex(Tag.TABLE_NAME, columns));
        }
        for (final String[] columns : Transaction.INDEX_COLUMNS) {
            database.execSQL(sqlCreateIndex(Transaction.TABLE_NAME, columns));
        }
    }

    private void createTriggers(final SQLiteDatabase database) {
        database.execSQL(SQLConstants.CREATE_TRIGGER_DELETE_REDUNDENT_TRANSACTION);
        database.execSQL(SQLConstants.CREATE_TRIGGER_MARK_ITEM_AS_READ);
    }

    @Override
    public void onCreate(final SQLiteDatabase database) {
        createTables(database);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase database, final int oldVersion, final int newVersion) {
        if (checkDropTable(database, ItemTag.TABLE_NAME, ItemTag.COLUMNS)) {
            database.execSQL("DROP TABLE IF EXISTS " + ItemTag.TABLE_NAME);
        }
        if (checkDropTable(database, SubscriptionTag.TABLE_NAME, SubscriptionTag.COLUMNS)) {
            database.execSQL("DROP TABLE IF EXISTS " + SubscriptionTag.TABLE_NAME);
        }
        if (checkDropTable(database, Setting.TABLE_NAME, Setting.COLUMNS)) {
            database.execSQL("DROP TABLE IF EXISTS " + Setting.TABLE_NAME);
        }
        checkTableColumns(database, Item.TABLE_NAME, Item.COLUMNS, Item.COLUMNS_TYPE);
        checkTableColumns(database, Subscription.TABLE_NAME, Subscription.COLUMNS, Subscription.COLUMNS_TYPE);
        checkTableColumns(database, Tag.TABLE_NAME, Tag.COLUMNS, Tag.COLUMNS_TYPE);
        checkTableColumns(database, Transaction.TABLE_NAME, Transaction.COLUMNS, Transaction.COLUMNS_TYPE);
        createTables(database);
        createTriggers(database);
        for (int i = oldVersion + 1; i <= newVersion; i++) {
            final String[] sqls = UPGRADE_SQL.get(i);
            if (sqls != null) {
                for (final String sql : sqls) {
                    database.execSQL(sql);
                }
            }
        }
    }
}
