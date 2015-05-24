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

public class PopupMenuItem {
    private final int id;
    private final int resId;
    private final String title;

    public PopupMenuItem(final int id, final int resId, final String title) {
        this.id = id;
        this.resId = resId;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public int getResId() {
        return resId;
    }

    public String getTitle() {
        return title;
    }
}
