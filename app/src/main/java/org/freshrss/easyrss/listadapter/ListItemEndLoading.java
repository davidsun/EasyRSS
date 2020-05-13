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

public class ListItemEndLoading extends AbsListItem {
    public ListItemEndLoading(final String id) {
        super(id);
    }

    @Override
    public View inflate(View view, final LayoutInflater inflater, final int fontSize) {
        if (view == null || view.getId() != R.id.ListItemEndLoading) {
            view = inflater.inflate(R.layout.list_item_end_loading, null);
        }
        final TextView txt = (TextView) view.findViewById(R.id.TxtLoading);
        txt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        return view;
    }
}
