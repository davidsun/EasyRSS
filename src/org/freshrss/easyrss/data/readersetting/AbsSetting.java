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

public abstract class AbsSetting<Data> {
    public AbsSetting(final DataMgr dataMgr) {
        final Data staticData = getStaticValue();
        if (staticData == null) {
            final String settingData = dataMgr.getSettingByName(getName());
            if (settingData == null) {
                setData(dataMgr, getDefault());
            } else {
                setStaticValue(settingData);
            }
        }
    }

    public Data getData() {
        return getStaticValue();
    }

    protected abstract Data getDefault();

    protected abstract String getName();

    protected abstract Data getStaticValue();

    public void setData(final DataMgr dataMgr, final Data data) {
        setStaticValue(data);
        dataMgr.updateSetting(toSetting());
    }

    protected abstract void setStaticValue(Data value);

    protected abstract void setStaticValue(String value);

    public Setting toSetting() {
        return new Setting(getName(), String.valueOf(getStaticValue()));
    }
}
