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

package org.freshrss.easyrss.network.url;

import org.freshrss.easyrss.data.DataMgr;
import org.freshrss.easyrss.data.Setting;

public class EditItemTagURL extends AbsURL {
    private static final String URL_API_EDIT_TAG = URL_API + "/edit-tag?client=scroll";

    private String itemUid;
    private String tagUid;
    private boolean isAdd;

    public EditItemTagURL(final boolean isHttpsConnection, final String itemUid, final String tagUid,
            final boolean isAdd) {
        super(isHttpsConnection, true, false);

        setItemUid(itemUid);
        setTagUid(tagUid);
        setAdd(isAdd);
        init();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof EditItemTagURL)) {
            return false;
        }
        final EditItemTagURL url = (EditItemTagURL) obj;
        return (itemUid.equals(url.itemUid) && tagUid.equals(url.tagUid) && isAdd == url.isAdd);
    }

    public String getItemUid() {
        return itemUid;
    }

    public String getTagUid() {
        return tagUid;
    }

    @Override
    public String getBaseURL() {
        return serverUrl + URL_API_EDIT_TAG;
    }

    private void init() {
        addParam("T", DataMgr.getInstance().getSettingByName(Setting.SETTING_TOKEN));
        addParam("async", "true");
        addParam("pos", "0");
    }

    public boolean isAdd() {
        return isAdd;
    }

    public void setAdd(final boolean isAdd) {
        this.isAdd = isAdd;
        if (tagUid != null) {
            if (isAdd) {
                addParam("a", tagUid);
                removeParam("r");
            } else {
                addParam("r", tagUid);
                removeParam("a");
            }
        }
    }

    public void setItemUid(final String itemUid) {
        this.itemUid = itemUid;
        addParam("i", itemUid);
    }

    public void setTagUid(final String tagUid) {
        this.tagUid = tagUid;
        if (isAdd) {
            addParam("a", tagUid);
            removeParam("r");
        } else {
            addParam("r", tagUid);
            removeParam("a");
        }
    }
}
