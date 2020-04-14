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

public interface ViewCtrlListener {
    void onBackNeeded();

    void onImageViewRequired(String imgPath);

    void onItemListSelected(String uid, int viewType);

    void onItemSelected(String uid);

    void onLogin(boolean succeeded);

    void onLogoutRequired();

    void onWebsiteViewSelected(String uid, boolean isMobilized);

    void onReloadRequired(boolean showSettings);

    void onSettingsSelected();

    void onSyncRequired();
}
