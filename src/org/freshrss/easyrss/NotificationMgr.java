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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import org.freshrss.easyrss.R;

public class NotificationMgr {
    private static final int NOTIFICATION_ID_NEW_ITEMS = 0;
    private static NotificationMgr instance = null;

    public static NotificationMgr getInstance() {
        return instance;
    }

    public static synchronized void init(final Context context) {
        if (instance == null) {
            instance = new NotificationMgr(context);
        }
    }

    final private Context context;
    final private NotificationManager notificationManager;
    private Boolean showNotification;

    private NotificationMgr(final Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.showNotification = true;
    }

    public void stopNotification() {
        showNotification = false;
        notificationManager.cancel(NOTIFICATION_ID_NEW_ITEMS);
    }

    public void startNotification() {
        showNotification = true;
    }

    @SuppressWarnings("deprecation")
    public void showNewItemsNotification(final int itemCount) {
        if (!showNotification) {
            return;
        }
        final Notification notification = new Notification(R.drawable.logo_notification_smaller,
                context.getString(R.string.MsgTickerTextNewItems), System.currentTimeMillis());
        notification.defaults = Notification.FLAG_AUTO_CANCEL;
        final Intent intent = new Intent(context, Home.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        notification.setLatestEventInfo(context, context.getString(R.string.MsgTitleNewItems1) + ' ' + itemCount + ' '
                + context.getString(R.string.MsgTitleNewItems2), context.getString(R.string.MsgClickToView),
                pendingIntent);
        notificationManager.notify(NOTIFICATION_ID_NEW_ITEMS, notification);
    }
}
