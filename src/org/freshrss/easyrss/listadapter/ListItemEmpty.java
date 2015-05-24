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

public class ListItemEmpty extends AbsListItem {
    private String text;

    public ListItemEmpty(final String id, final String text) {
        super(id);

        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public View inflate(View view, final LayoutInflater inflater, final int fontSize) {
        if (view == null || view.getId() != R.id.ListItemEmpty) {
            view = inflater.inflate(R.layout.list_item_empty, null);
        }
        final TextView txtText = (TextView) view.findViewById(R.id.Title);
        txtText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        txtText.setText(text);
        return view;
    }

    public void setText(final String text) {
        this.text = text;
    }
}
