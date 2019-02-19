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

package org.meveo.jmeter.threadgroup.gui;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.gui.AbstractThreadGroupGui;
import org.meveo.jmeter.threadgroup.model.MeveoThreadGroup;
import org.meveo.jmeter.utils.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.concurrent.TimeUnit;

public class MeveoThreadGroupGui extends AbstractThreadGroupGui implements ItemListener {

    private static final int LABEL_WIDTH = 100;
    private static final int PANEL_HEIGHT = 25;

    private final JTextField peridicity;
    private final JComboBox<TimeUnit> timeUnit;

    public MeveoThreadGroupGui() {

        super();

        removeAll();

        Box box = Box.createVerticalBox();

        VerticalPanel titlePanel = new VerticalPanel();
        titlePanel.add(createTitleLabel());

        box.add(titlePanel);
        add(box, BorderLayout.NORTH);

        /* Periodcity */
        JPanel bottomPanel = new HorizontalPanel();
        bottomPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, PANEL_HEIGHT));

        JLabel peridicityLabel = new JLabel("Periodicity : ");
        peridicityLabel.setPreferredSize(new Dimension(LABEL_WIDTH, PANEL_HEIGHT));
        bottomPanel.add(peridicityLabel);

        peridicity = new JTextField();
        peridicity.setName("Periodicity");
        peridicity.setText("1");
        peridicity.setEnabled(false);
        peridicityLabel.setLabelFor(peridicity);
        bottomPanel.add(peridicity);

        TimeUnit[] availableTimeUnits = {TimeUnit.DAYS};
        timeUnit = new JComboBox<>(availableTimeUnits);
        timeUnit.setSelectedItem(TimeUnit.DAYS);
        bottomPanel.add(timeUnit);
        SwingUtils.setEnable(timeUnit, false);

        /* Test properties */
        VerticalPanel panel = new VerticalPanel();
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Test properties"));
        panel.add(bottomPanel);

        add(panel, BorderLayout.CENTER);

    }

    @Override
    public String getLabelResource() {
        return getClass().getCanonicalName();
    }

    @Override
    public String getStaticLabel() {
        return "Meveo - Function Test";
    }

    @Override
    public TestElement createTestElement() {
        MeveoThreadGroup tg = new MeveoThreadGroup();
        modifyTestElement(tg);
        return tg;
    }

    @Override
    public void modifyTestElement(TestElement element) {

    }

    @Override
    public void itemStateChanged(ItemEvent ie) {
        System.out.println(ie);
    }

    @Override
    public void clearGui(){
        super.clearGui();
    }

}
