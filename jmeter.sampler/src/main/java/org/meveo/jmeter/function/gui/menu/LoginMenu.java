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

import org.apache.jmeter.gui.GuiPackage;
import org.meveo.jmeter.login.controller.LoginController;
import org.meveo.jmeter.login.gui.LoginDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginMenu extends JMenuItem implements ActionListener {

    private LoginController loginController;

    public LoginMenu() {
        super("Log in to Meveo");
        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(loginController == null){
            loginController = new LoginController(new LoginDialog());
        }
        loginController.show();
    }
}
