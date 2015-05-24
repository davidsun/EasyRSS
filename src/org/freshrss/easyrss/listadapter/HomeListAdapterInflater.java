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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

final public class HomeListAdapterInflater extends AbsListAdapterInflater {
    public static HomeListAdapterInflater instance;

    public static HomeListAdapterInflater getInstance() {
        return instance;
    }

    public synchronized static void init(final Context context, final int fontSize) {
        if (instance == null) {
            instance = new HomeListAdapterInflater(context, fontSize);
        }
    }

    final LayoutInflater inflater;

    private HomeListAdapterInflater(final Context context, final int fontSize) {
        super(fontSize);

        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View inflateObject(View view, final AbsListItem item) {
        return item.inflate(view, inflater, fontSize);
    }
}
