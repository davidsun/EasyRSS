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

import org.freshrss.easyrss.R;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class PopupMenu extends PopupWindow {
    final private Context context;
    final private ViewGroup viewGroup;
    final private SparseArray<View> itemView;
    private PopupMenuListener listener;

    @SuppressWarnings("deprecation")
    public PopupMenu(final Context context) {
        super(context);

        final FrameLayout layout = new FrameLayout(context);
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.popup_menu, layout);
        setBackgroundDrawable(new BitmapDrawable());
        setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        setTouchable(true);
        setFocusable(true);
        setOutsideTouchable(true);
        setAnimationStyle(R.style.Animations_PopupMenu);

        setContentView(layout);
        this.context = context;
        this.viewGroup = (ViewGroup) layout.findViewById(R.id.ListPopupMenu);
        this.itemView = new SparseArray<View>();
    }

    public void addItem(final PopupMenuItem item) {
        if (itemView.get(item.getId()) != null) {
            return;
        }
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View container = inflater.inflate(R.layout.popup_menu_item, null);
        final ImageView img = (ImageView) container.findViewById(R.id.MenuItemIcon);
        final TextView text = (TextView) container.findViewById(R.id.MenuItemTitle);
        img.setImageResource(item.getResId());
        text.setText(item.getTitle());

        final int id = item.getId();
        container.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (listener != null) {
                    listener.onItemClick(id);
                }
                dismiss();
            }
        });

        container.setFocusable(true);
        container.setClickable(true);
        viewGroup.addView(container);
        viewGroup.invalidate();
        itemView.put(id, container);
    }

    public void clearItems() {
        viewGroup.removeAllViews();
        viewGroup.invalidate();
        itemView.clear();
    }

    public PopupMenuListener getListener() {
        return listener;
    }

    public void removeItem(final int id) {
        final View view = itemView.get(id);
        if (view == null) {
            return;
        }
        view.setOnClickListener(null);
        viewGroup.removeView(view);
        viewGroup.measure(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        itemView.remove(id);
    }

    public void setListener(final PopupMenuListener listener) {
        this.listener = listener;
    }
}
