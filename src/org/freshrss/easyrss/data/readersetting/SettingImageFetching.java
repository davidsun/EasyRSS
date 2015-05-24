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

public class SettingImageFetching extends AbsSetting<Integer> {
    public static final int FETCH_METHOD_WIFI = 0;
    public static final int FETCH_METHOD_NETWORK = 1;
    public static final int FETCH_METHOD_DISABLED = 2;

    private static Integer value;

    public SettingImageFetching(final DataMgr dataMgr) {
        super(dataMgr);
    }

    @Override
    protected Integer getDefault() {
        return FETCH_METHOD_NETWORK;
    }

    @Override
    protected String getName() {
        return Setting.SETTING_IMAGE_FETCHING;
    }

    @Override
    protected Integer getStaticValue() {
        return value;
    }

    @Override
    protected void setStaticValue(final Integer value) {
        SettingImageFetching.value = value;
    }

    @Override
    protected void setStaticValue(final String value) {
        SettingImageFetching.value = Integer.valueOf(value);
    }
}
