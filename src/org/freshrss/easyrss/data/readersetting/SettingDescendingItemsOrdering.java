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

package org.freshrss.easyrss.data.readersetting;

import org.freshrss.easyrss.data.DataMgr;
import org.freshrss.easyrss.data.Setting;

public class SettingDescendingItemsOrdering extends AbsSetting<Boolean> {
    private static Boolean value;

    public SettingDescendingItemsOrdering(final DataMgr dataMgr) {
        super(dataMgr);
    }

    @Override
    protected Boolean getDefault() {
        return true;
    }

    @Override
    protected String getName() {
        return Setting.SETTING_DESCENDING_ITEMS_ORDERING;
    }

    @Override
    protected Boolean getStaticValue() {
        return value;
    }

    @Override
    protected synchronized void setStaticValue(final Boolean value) {
        SettingDescendingItemsOrdering.value = value;
    }

    @Override
    protected void setStaticValue(final String value) {
        SettingDescendingItemsOrdering.value = Boolean.valueOf(value);
    }
}
