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

public class SettingFontSize extends AbsSetting<Integer> {
    private static Integer value;

    public SettingFontSize(final DataMgr dataMgr) {
        super(dataMgr);
    }

    @Override
    protected Integer getDefault() {
        return 15;
    }

    @Override
    protected String getName() {
        return Setting.SETTING_FONT_SIZE;
    }

    @Override
    protected Integer getStaticValue() {
        return value;
    }

    @Override
    protected synchronized void setStaticValue(final Integer value) {
        SettingFontSize.value = value;
    }

    @Override
    protected void setStaticValue(final String value) {
        SettingFontSize.value = Integer.valueOf(value);
    }
}
