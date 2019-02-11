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

package org.meveo.jmeter.function.gui;

import org.apache.jorphan.gui.ComponentUtil;
import org.meveo.api.dto.function.FunctionDto;
import org.meveo.jmeter.function.FunctionManager;

import javax.swing.*;
import java.awt.*;

public class FunctionManagerDialog extends JDialog {

    FunctionManager manager;

    public FunctionManagerDialog(){
        super((JFrame) null, "Function Tests Manager", true);
        setLayout(new BorderLayout());
        Dimension size = new Dimension(1024, 768);
        setSize(size);
        setPreferredSize(size);
        ComponentUtil.centerComponentInWindow(this);
        manager = new FunctionManager();
        init();
    }

    public void init(){
        JList list = new JList();

        for(FunctionDto functionDto : manager.getFunctions()) {
            JLabel jLabel = new JLabel(functionDto.getCode());
            list.add(jLabel);
        }

        JScrollPane scrollableList = new JScrollPane(list);
        add(scrollableList);

    }




}
