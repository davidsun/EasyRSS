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

import org.freshrss.easyrss.data.DataMgr;
import org.freshrss.easyrss.data.readersetting.SettingSyncMethod;
import org.freshrss.easyrss.network.NetworkUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SyncingReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        Utils.initManagers(context);
        final int sSync = new SettingSyncMethod(DataMgr.getInstance()).getData();
        if (sSync != SettingSyncMethod.SYNC_METHOD_MANUAL) {
            NetworkUtils.doGlobalSyncing(context, sSync);
        }
    }
}
