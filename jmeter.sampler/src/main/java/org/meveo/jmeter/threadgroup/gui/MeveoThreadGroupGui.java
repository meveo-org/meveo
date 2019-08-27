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
import org.meveo.api.dto.job.TimerEntityDto;
import org.meveo.jmeter.function.FunctionManager;
import org.meveo.jmeter.threadgroup.model.MeveoThreadGroup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class MeveoThreadGroupGui extends AbstractThreadGroupGui implements ItemListener {

    private static final int LABEL_WIDTH = 100;
    private static final int PANEL_HEIGHT = 25;

    private final JComboBox<String> periodicity;

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

        JLabel peridicityLabel = new JLabel("Timer : ");
        peridicityLabel.setPreferredSize(new Dimension(LABEL_WIDTH, PANEL_HEIGHT));
        bottomPanel.add(peridicityLabel);

        final String[] timerCodes = FunctionManager.getTimers()
                .stream()
                .map(TimerEntityDto::getCode)
                .toArray(String[]::new);

        periodicity = new JComboBox<>(timerCodes);
        periodicity.setName("Timer");
        peridicityLabel.setLabelFor(periodicity);
        bottomPanel.add(periodicity);

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
        MeveoThreadGroup tg = (MeveoThreadGroup) element;
        final String selectedItem = (String) periodicity.getSelectedItem();
        tg.setPeriodicity(selectedItem);
    }

    @Override
    public void itemStateChanged(ItemEvent ie) {
        System.out.println(ie);
    }

    @Override
    public void clearGui(){
        super.clearGui();
    }

    @Override
    public void configure(TestElement tg) {
        super.configure(tg);
        MeveoThreadGroup element = (MeveoThreadGroup) tg;
        final String periodicity = element.getPeriodicity();
        this.periodicity.setSelectedItem(periodicity);
    }
}
