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

import org.freshrss.easyrss.data.DataMgr;
import org.freshrss.easyrss.data.Setting;
import org.freshrss.easyrss.network.url.TokenURL;

public class TokenDataSyncer extends AbsDataSyncer {
    public TokenDataSyncer(final DataMgr dataMgr, final int networkConfig) {
        super(dataMgr, networkConfig);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else {
            return (obj instanceof TokenDataSyncer);
        }
    }

    @Override
    public void startSyncing() throws DataSyncerException {
        final String sExpTime = dataMgr.getSettingByName(Setting.SETTING_TOKEN_EXPIRE_TIME);
        final String token = dataMgr.getSettingByName(Setting.SETTING_TOKEN);
        if (token != null && sExpTime != null && Long.valueOf(sExpTime) >= System.currentTimeMillis()) {
            return;
        }

        syncToken();

        dataMgr.updateSetting(new Setting(Setting.SETTING_TOKEN_EXPIRE_TIME, String.valueOf(System.currentTimeMillis()
                + TOKEN_EXPIRE_TIME)));
    }

    private void syncToken() throws DataSyncerException {
        final byte[] data = httpGetQueryByte(new TokenURL(isHttpsConnection));
        dataMgr.updateSetting(new Setting(Setting.SETTING_TOKEN, new String(data)));
    }

    @Override
    protected void finishSyncing() {
        /*
         * TODO Empty method: Do nothing here. This class will only be called by
         * TransactionDataSyncer.
         */
    }
}
