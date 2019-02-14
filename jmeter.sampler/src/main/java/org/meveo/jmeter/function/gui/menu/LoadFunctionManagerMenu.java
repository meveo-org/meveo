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

import org.meveo.jmeter.function.controller.FunctionManagerController;
import org.meveo.jmeter.function.gui.functionmanager.FunctionManagerDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoadFunctionManagerMenu extends JMenuItem implements ActionListener {

    private FunctionManagerController functionManagerController;

    public LoadFunctionManagerMenu() {
        super("Load or create function test");
        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (functionManagerController == null) {
            functionManagerController = new FunctionManagerController(new FunctionManagerDialog());
        }
        functionManagerController.show();
    }

}
