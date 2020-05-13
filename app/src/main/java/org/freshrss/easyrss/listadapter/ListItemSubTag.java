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

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ListItemSubTag extends AbsListItem {
    private Bitmap icon;
    private int number;
    private String title;

    public ListItemSubTag(final String id, final String title, final int number, final Bitmap icon) {
        super(id);

        this.title = title;
        this.icon = icon;
        this.number = number;
    }

    public ListItemSubTag(final String id, final String title, final int number, final Resources res,
            final int iconResId) {
        super(id);

        this.title = title;
        this.icon = BitmapFactory.decodeResource(res, iconResId);
        this.number = number;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public int getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public View inflate(View view, final LayoutInflater inflater, final int fontSize) {
        if (view == null || view.getId() != R.id.ListItemSubTag) {
            view = inflater.inflate(R.layout.list_item_sub_tag, null);
        }
        final TextView txtNumber = (TextView) view.findViewById(R.id.number);
        final TextView txtTitle = (TextView) view.findViewById(R.id.TagTitle);
        final ImageView imgIcon = ((ImageView) view.findViewById(R.id.icon));
        txtTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        txtTitle.setText(title);
        txtNumber.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        if (number >= 1000) {
            txtNumber.setText("1000+");
        } else if (number > 0) {
            txtNumber.setText(String.valueOf(number));
            txtNumber.setVisibility(View.VISIBLE);
        } else {
            txtNumber.setVisibility(View.INVISIBLE);
        }
        imgIcon.setImageBitmap(icon);
        return view;
    }

    public void setIcon(final Bitmap icon) {
        this.icon = icon;
    }

    public void setNumber(final int number) {
        this.number = number;
    }

    public void setTitle(final String title) {
        this.title = title;
    }
}
