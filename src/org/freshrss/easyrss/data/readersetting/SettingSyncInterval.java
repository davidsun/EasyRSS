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

public class SettingSyncInterval extends AbsSetting<Integer> {
    private static final int SYNC_INTERVAL_ONE_HOUR = 0;
    private static final int SYNC_INTERVAL_TWO_HOURS = 1;
    private static final int SYNC_INTERVAL_THREE_HOURS = 2;
    private static final int SYNC_INTERVAL_FOUR_HOURS = 3;
    private static final int SYNC_INTERVAL_SIX_HOURS = 4;

    private static Integer value;

    public SettingSyncInterval(final DataMgr dataMgr) {
        super(dataMgr);
    }

    @Override
    protected Integer getDefault() {
        return SYNC_INTERVAL_ONE_HOUR;
    }

    @Override
    protected String getName() {
        return Setting.SETTING_SYNC_INTERVAL;
    }

    @Override
    protected Integer getStaticValue() {
        return value;
    }

    @Override
    protected void setStaticValue(final Integer value) {
        SettingSyncInterval.value = value;
    }

    @Override
    protected void setStaticValue(final String value) {
        SettingSyncInterval.value = Integer.valueOf(value);
    }

    public long toSeconds() {
        switch (value) {
        case SYNC_INTERVAL_ONE_HOUR:
            return 3600;
        case SYNC_INTERVAL_TWO_HOURS:
            return 2 * 3600;
        case SYNC_INTERVAL_THREE_HOURS:
            return 3 * 3600;
        case SYNC_INTERVAL_FOUR_HOURS:
            return 4 * 3600;
        case SYNC_INTERVAL_SIX_HOURS:
            return 6 * 3600;
        default:
            return 0;
        }
    }
}
