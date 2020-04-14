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
import org.freshrss.easyrss.network.url.EditItemTagURL;

public class ItemTagDataSyncer extends AbsDataSyncer {
    final private EditItemTagURL url;

    public ItemTagDataSyncer(final DataMgr dataMgr, final int networkConfig, final String itemUid, final String tagUid,
            final boolean isAdd) {
        super(dataMgr, networkConfig);

        this.url = new EditItemTagURL(isHttpsConnection, itemUid, tagUid, isAdd);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof ItemTagDataSyncer)) {
            return false;
        }
        final ItemTagDataSyncer syncer = (ItemTagDataSyncer) obj;
        return (url.equals(syncer.url));
    }

    @Override
    public void startSyncing() throws DataSyncerException {
        final byte[] data = httpPostQueryByte(url);
        if (!"OK".equals(new String(data))) {
            throw new DataSyncerException("Sync failed");
        }
    }

    @Override
    protected void finishSyncing() {
        /*
         * TODO Empty method: Do nothing here. This class will only be called by
         * TransactionDataSyncer.
         */
    }
}
