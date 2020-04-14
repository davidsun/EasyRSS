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

import android.view.LayoutInflater;
import android.view.View;

abstract public class AbsListItem {
    public static final String ID_TITLE_ALL = "TITLE_ALL";
    public static final String ID_TITLE_TAGS = "TITLE_ALL_TAGS";
    public static final String ID_TITLE_SUBSCRIPTIONS = "TITLE_ALL_SUBS";
    public static final String ID_TAGS_EMPTY = "TAGS_EMPTY";
    public static final String ID_SUBSCRIPTIONS_EMPTY = "SUBS_EMPTY";
    public static final String ID_END = "END";
    public static final String ITEM_TITLE_TYPE_ALL = "ALL";
    public static final String ITEM_TITLE_TYPE_STARRED = "STARRED";
    public static final String ITEM_TITLE_TYPE_UNREAD = "UNREAD";

    private String id;

    public AbsListItem(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public abstract View inflate(View view, LayoutInflater inflater, int fontSize);

    public void setId(final String id) {
        this.id = id;
    }
}
