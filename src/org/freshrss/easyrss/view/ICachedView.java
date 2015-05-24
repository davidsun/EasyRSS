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

import android.graphics.Paint;
import android.os.IBinder;
import android.view.View;
import android.view.animation.Animation;

public interface ICachedView {
    void disableCache();

    void enableCache();

    View findViewById(int id);

    int getMeasuredWidth();

    IBinder getWindowToken();

    void setAlpha(float alpha);

    void setAnimation(Animation animation);

    void setLayerType(int layerType, Paint paint);
}
