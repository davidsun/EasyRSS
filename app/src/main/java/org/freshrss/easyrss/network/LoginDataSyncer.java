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

import java.io.BufferedReader;
import java.io.IOException;

import org.freshrss.easyrss.data.DataMgr;
import org.freshrss.easyrss.data.Setting;
import org.freshrss.easyrss.data.readersetting.SettingSyncMethod;
import org.freshrss.easyrss.network.url.LoginURL;


public class LoginDataSyncer extends AbsDataSyncer {
    final private LoginURL url;

    public LoginDataSyncer(final DataMgr dataMgr, final String username, final String password) {
        super(dataMgr, SettingSyncMethod.SYNC_METHOD_NETWORK);
        url = new LoginURL(username, password);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof LoginDataSyncer)) {
            return false;
        }
        final LoginDataSyncer syncer = (LoginDataSyncer) obj;
        return url.equals(syncer.url);
    }

    @Override
    protected void finishSyncing() {
        /*
         * TODO Empty method: Do nothing here. This class will only be called
         * once.
         */
    }

    @Override
    public void startSyncing() throws DataSyncerException {
        final BufferedReader input = new BufferedReader(httpPostQueryReader(url));
        String auth = null;
        try {
            String line;
            while ((line = input.readLine()) != null) {
                if (line.indexOf("Auth=") == 0) {
                    auth = line.substring("Auth=".length());
                    break;
                }
            }
        } catch (IOException e) {
            throw new DataSyncerException(e);
        } finally {
            try {
                input.close();
            } catch (final IOException exception) {
                exception.printStackTrace();
            }
        }

        if (auth == null) {
            throw new DataSyncerException("Invalid auth");
        } else {
            dataMgr.updateSetting(new Setting(Setting.SETTING_AUTH, auth));
        }
    }
}
