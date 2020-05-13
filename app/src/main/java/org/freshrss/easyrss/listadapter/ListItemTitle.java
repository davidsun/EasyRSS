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

import org.freshrss.easyrss.R;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class ListItemTitle extends AbsListItem {
    private String title;

    public ListItemTitle(final String id, final String title) {
        super(id);

        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public View inflate(View view, final LayoutInflater inflater, final int fontSize) {
        if (view == null || view.getId() != R.id.ListItemTitle) {
            view = inflater.inflate(R.layout.list_item_title, null);
        }
        final TextView txtTitle = (TextView) view.findViewById(R.id.TitleName);
        txtTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize * 4 / 5);
        txtTitle.setText(title);
        return view;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

}
