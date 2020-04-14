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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class CachedFrameLayout extends FrameLayout implements ICachedView {
    final private Paint paint;
    private boolean isCacheEnabled;
    private Bitmap cache;

    public CachedFrameLayout(final Context context) {
        super(context);
        this.paint = new Paint();
        this.isCacheEnabled = false;
    }

    public CachedFrameLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.paint = new Paint();
        this.isCacheEnabled = false;
    }

    public CachedFrameLayout(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        this.paint = new Paint();
        this.isCacheEnabled = false;
    }

    public void disableCache() {
        if (isCacheEnabled) {
            setDrawingCacheEnabled(false);
            if (cache != null && !cache.isRecycled()) {
                cache.recycle();
            }
            isCacheEnabled = false;
            invalidate();
        }
    }

    @Override
    protected void dispatchDraw(final Canvas canvas) {
        if (!isCacheEnabled) {
            super.dispatchDraw(canvas);
        }
    }

    @Override
    public void draw(final Canvas canvas) {
        if (isCacheEnabled) {
            if (cache == null || cache.isRecycled()) {
                updateCache();
            }
            if (cache != null && !cache.isRecycled()) {
                canvas.drawBitmap(cache, null, new Rect(0, 0, getWidth(), getHeight()), paint);
            }
        } else {
            super.draw(canvas);
        }
    }

    public void enableCache() {
        if (!isCacheEnabled && android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            updateCache();
            isCacheEnabled = true;
        }
    }

    @Override
    public void invalidate() {
        if (!isCacheEnabled) {
            super.invalidate();
        }
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        if (!isCacheEnabled) {
            super.onDraw(canvas);
        }
    }

    private void updateCache() {
        setDrawingCacheEnabled(true);
        final Bitmap current = getDrawingCache();
        if (current != null && !current.isRecycled()) {
            cache = current.copy(Bitmap.Config.RGB_565, false);
        }
    }
}
