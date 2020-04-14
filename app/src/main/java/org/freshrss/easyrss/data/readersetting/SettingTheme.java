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

public class SettingTheme extends AbsSetting<Integer> {
    public static final int THEME_NORMAL = 0;
    public static final int THEME_DARK = 1;

    private static Integer value;

    public SettingTheme(final DataMgr dataMgr) {
        super(dataMgr);
    }

    @Override
    protected Integer getDefault() {
        return THEME_NORMAL;
    }

    @Override
    protected String getName() {
        return Setting.SETTING_THEME;
    }

    @Override
    public Integer getStaticValue() {
        return value;
    }

    @Override
    public synchronized void setStaticValue(final Integer value) {
        SettingTheme.value = value;
    }

    @Override
    protected void setStaticValue(final String value) {
        SettingTheme.value = Integer.valueOf(value);
    }
}
