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
import org.meveo.jmeter.function.FunctionManager;
import org.meveo.jmeter.utils.MeveoJmeterUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class UploadFunctionMenu extends JMenuItem implements ActionListener {

    public UploadFunctionMenu() {
        super("Upload current test");
        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final GuiPackage instance = GuiPackage.getInstance();
        final String testPlanFile = instance.getTestPlanFile();
        File tempFile = new File(testPlanFile);
        FunctionManager.upload(MeveoJmeterUtils.getTestPlanName(tempFile), tempFile);
    }

}
