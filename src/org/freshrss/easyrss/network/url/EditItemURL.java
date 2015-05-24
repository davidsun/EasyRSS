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

public class EditItemURL extends AbsURL {
    private static final String URL_API_EDIT_ITEM = URL_API + "/item/edit?client=scroll";

    private String itemUid;
    private String annotation;
    private boolean isShare;

    public EditItemURL(final boolean isHttpsConnection, final String itemUid, final String annotation,
            final boolean isShare) {
        super(isHttpsConnection, true, false);

        setItemUid(itemUid);
        setAnnotation(annotation);
        setShare(isShare);
        init();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof EditItemURL)) {
            return false;
        }
        final EditItemURL url = (EditItemURL) obj;
        return (itemUid.equals(url.itemUid) && annotation.equals(url.annotation) && isShare == url.isShare);
    }

    public String getAnnotation() {
        return annotation;
    }

    public String getItemUid() {
        return itemUid;
    }

    @Override
    public String getBaseURL() {
        return serverUrl + URL_API_EDIT_ITEM;
    }

    private void init() {
        addParam("T", DataMgr.getInstance().getSettingByName(Setting.SETTING_TOKEN));
    }

    public boolean isShare() {
        return isShare;
    }

    public void setAnnotation(final String annotation) {
        this.annotation = annotation;
        addParam("annotation", annotation);
    }

    public void setItemUid(final String itemUid) {
        this.itemUid = itemUid;
        addParam("srcItemId", itemUid);
    }

    public void setShare(final boolean isShare) {
        this.isShare = isShare;
        addParam("share", (isShare) ? "true" : "false");
    }
}
