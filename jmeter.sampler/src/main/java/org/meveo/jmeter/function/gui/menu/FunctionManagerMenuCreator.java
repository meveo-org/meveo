/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. This program is
 * not suitable for any direct or indirect application in MILITARY industry See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.meveo.jmeter.function.gui.menu;

import org.apache.jmeter.gui.plugin.MenuCreator;
import org.meveo.jmeter.login.controller.LoginController;

import javax.swing.*;

public class FunctionManagerMenuCreator implements MenuCreator {

    @Override
    public JMenuItem[] getMenuItemsAtLocation(MENU_LOCATION location) {
        if (location == MENU_LOCATION.FILE) {
            try {
                final LoginMenu loginMenu = new LoginMenu();
                final LoadFunctionManagerMenu loadFunctionManagerMenu = new LoadFunctionManagerMenu();
                final UploadFunctionMenu uploadFunctionMenu = new UploadFunctionMenu();
                LoginController.setLoadFunctionManagerMenu(loadFunctionManagerMenu);
                LoginController.setUploadFunctionMenu(uploadFunctionMenu);
                return new JMenuItem[]{loginMenu, loadFunctionManagerMenu, uploadFunctionMenu};
            } catch (Throwable e) {
                return new JMenuItem[0];
            }
        } else {
            return new JMenuItem[0];
        }
    }

    @Override
    public javax.swing.JMenu[] getTopLevelMenus() {
        return new javax.swing.JMenu[0];
    }

    @Override
    public boolean localeChanged(javax.swing.MenuElement menu) {
        return false;
    }

    @Override
    public void localeChanged() {
    }

}
