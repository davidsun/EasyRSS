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

package org.freshrss.easyrss.view;

import android.view.animation.Interpolator;

public class StaticInterpolator implements Interpolator {
    float staticValue;

    public StaticInterpolator() {
        staticValue = 0;
    }

    public StaticInterpolator(final float staticValue) {
        this.staticValue = staticValue;
    }

    @Override
    public float getInterpolation(final float duration) {
        return staticValue;
    }

    public float getStaticValue() {
        return staticValue;
    }

    public void setStaticValue(final float staticValue) {
        this.staticValue = staticValue;
    }
}
