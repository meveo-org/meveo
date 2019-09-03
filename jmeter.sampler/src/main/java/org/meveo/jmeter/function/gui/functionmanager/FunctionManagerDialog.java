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

package org.meveo.jmeter.function.gui.functionmanager;

import org.apache.jorphan.gui.ComponentUtil;
import org.meveo.jmeter.utils.SwingUtils;
import org.meveo.jmeter.utils.Waiting;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.util.List;

public class FunctionManagerDialog extends JDialog {

    private static final Border SPACING = BorderFactory.createEmptyBorder(5, 5, 5, 5);

    private final JScrollPane scrollableList = new JScrollPane();
    private final JButton refreshBtn;
    private final JPanel bottomPanel = new JPanel(new BorderLayout());
    private final Waiting waiter = new Waiting(bottomPanel, BorderLayout.WEST);

    private JList<String> stringJList;

    private MouseAdapter listItemClicked;

    public void setListItemClicked(MouseAdapter listItemClicked) {
        this.listItemClicked = listItemClicked;
    }

    public void setRefreshBtnClicked(ActionListener refreshBtnClicked) {
        refreshBtn.addActionListener(refreshBtnClicked);
    }


    public FunctionManagerDialog() {
        super((JFrame) null, "Function Tests Manager", true);

        setLayout(new BorderLayout());
        Dimension size = new Dimension(1024, 768);
        setSize(size);
        setPreferredSize(size);
        ComponentUtil.centerComponentInWindow(this);

        refreshBtn = new JButton("Refresh");
        refreshBtn.setLayout(null);

        bottomPanel.setPreferredSize(new Dimension(1024, 50));
        bottomPanel.add(refreshBtn, BorderLayout.EAST);
        bottomPanel.setBorder(SPACING);

        scrollableList.setBorder(SPACING);
        scrollableList.setBorder(BorderFactory.createTitledBorder("Available functions"));

        add(scrollableList, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

    }

    public void populateList(List<String> functionsCodes) {

        DefaultListModel<String> listModel = new DefaultListModel<>();
        int i = 0;

        for (String e : functionsCodes) {
            listModel.add(i++, e);
        }

        stringJList = new JList<>(listModel);
        stringJList.addMouseListener(listItemClicked);
        scrollableList.setViewportView(stringJList);

    }

    public void enableDialog() {
        waiter.stop();
        refreshBtn.setEnabled(true);
        SwingUtils.setEnable(scrollableList, true);
    }

    public void disableDialog() {
        waiter.start();
        refreshBtn.setEnabled(false);
        SwingUtils.setEnable(scrollableList, false);

        if (stringJList != null) {
            stringJList.removeMouseListener(listItemClicked);
        }
    }

}
