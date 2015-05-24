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

public class SettingBrowserChoice extends AbsSetting<Integer> {
    public static final int BROWSER_CHOICE_UNKNOWN = 0;
    public static final int BROWSER_CHOICE_MOBILIZED = 1;
    public static final int BROWSER_CHOICE_ORIGINAL = 2;
    public static final int BROWSER_CHOICE_EXTERNAL = 3;

    private static Integer value;

    public SettingBrowserChoice(final DataMgr dataMgr) {
        super(dataMgr);
    }

    @Override
    protected Integer getDefault() {
        return BROWSER_CHOICE_UNKNOWN;
    }

    @Override
    protected String getName() {
        return Setting.SETTING_BROWSER_CHOICE;
    }

    @Override
    protected Integer getStaticValue() {
        return value;
    }

    @Override
    protected void setStaticValue(final Integer value) {
        SettingBrowserChoice.value = value;
    }

    @Override
    protected void setStaticValue(final String value) {
        SettingBrowserChoice.value = Integer.valueOf(value);
    }
}
