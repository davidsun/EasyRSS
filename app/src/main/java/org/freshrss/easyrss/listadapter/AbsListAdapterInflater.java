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

package org.freshrss.easyrss.listadapter;

import android.view.View;

public abstract class AbsListAdapterInflater {

    protected int fontSize;

    public AbsListAdapterInflater(final int fontSize) {
        this.fontSize = fontSize;
    }

    public int getFontSize() {
        return fontSize;
    }

    abstract protected View inflateObject(View view, AbsListItem obj);

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }
}
