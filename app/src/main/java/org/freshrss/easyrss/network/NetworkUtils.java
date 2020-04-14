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

import org.freshrss.easyrss.SyncingReceiver;
import org.freshrss.easyrss.Utils;
import org.freshrss.easyrss.account.ReaderAccountMgr;
import org.freshrss.easyrss.data.DataMgr;
import org.freshrss.easyrss.data.readersetting.SettingImageFetching;
import org.freshrss.easyrss.data.readersetting.SettingSyncMethod;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


final public class NetworkUtils {
    public static boolean checkImageFetchingNetworkStatus(final Context context, final int config) {
        final ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo info = connMgr.getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            return false;
        }
        if (config == SettingImageFetching.FETCH_METHOD_DISABLED) {
            return false;
        } else if (config == SettingImageFetching.FETCH_METHOD_WIFI) {
            final android.net.NetworkInfo.State wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
            return (wifi == NetworkInfo.State.CONNECTED);
        } else if (config == SettingImageFetching.FETCH_METHOD_NETWORK) {
            return true;
        }
        return false;
    }

    public static boolean checkSyncingNetworkStatus(final Context context, final int config) {
        final ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo info = connMgr.getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            return false;
        }
        if (config == SettingSyncMethod.SYNC_METHOD_MANUAL) {
            return true;
        } else if (config == SettingSyncMethod.SYNC_METHOD_WIFI) {
            final android.net.NetworkInfo.State wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
            return (wifi == NetworkInfo.State.CONNECTED);
        } else if (config == SettingSyncMethod.SYNC_METHOD_NETWORK) {
            return true;
        }
        return false;
    }

    public static void doGlobalSyncing(final Context context, final int syncingMethod) {
        Utils.initManagers(context);
        if (ReaderAccountMgr.getInstance().hasAccount() && !GlobalItemDataSyncer.hasInstance()
                && !SubscriptionDataSyncer.hasInstance() && !TagDataSyncer.hasInstance()) {
            final NetworkMgr nMgr = NetworkMgr.getInstance();
            final DataMgr dataMgr = DataMgr.getInstance();
            nMgr.startSync(TagDataSyncer.getInstance(dataMgr, syncingMethod));
            nMgr.startSync(SubscriptionDataSyncer.getInstance(dataMgr, syncingMethod));
            nMgr.startSync(GlobalItemDataSyncer.getInstance(dataMgr, syncingMethod));
        }
    }

    public static void startSyncingTimer(final Context context) {
        final AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        final Intent intent = new Intent(context, SyncingReceiver.class);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmMgr.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(),
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
    }

    private NetworkUtils() {
    }
}
