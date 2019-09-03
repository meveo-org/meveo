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

package org.meveo.jmeter.sampler.gui;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.meveo.jmeter.sampler.model.MeveoSampler;
import org.meveo.jmeter.utils.MeveoJmeterUtils;

import javax.swing.*;
import java.awt.*;

import static org.meveo.jmeter.utils.MeveoJmeterUtils.getDefaultInputs;
import static org.meveo.jmeter.utils.MeveoJmeterUtils.getTestPlanName;

public class MeveoSamplerGui extends AbstractSamplerGui {

    private final ArgumentsPanel argsPanel;

    public MeveoSamplerGui() {

        setLayout(new BorderLayout());

        argsPanel = new ArgumentsPanel(JMeterUtils.getResString("paramtable"));
        JScrollPane inputScrollPane = new JScrollPane(argsPanel);

        add(inputScrollPane, BorderLayout.CENTER);
    }

    @Override
    public String getLabelResource() {
        return MeveoSamplerGui.class.getName();
    }

    @Override
    public String getStaticLabel() {
        return "Function execution";
    }

    @Override
    public TestElement createTestElement() {
        final MeveoSampler meveoSampler = new MeveoSampler();
        meveoSampler.setArguments(getDefaultInputs());
        meveoSampler.setName("Execute function");
        meveoSampler.setFunction(getTestPlanName());
        meveoSampler.setProperty(TestElement.GUI_CLASS, MeveoSamplerGui.class.getName());
        meveoSampler.setProperty(TestElement.TEST_CLASS, MeveoSampler.class.getName());
        return meveoSampler;
    }

    @Override
    public void modifyTestElement(TestElement element) {
        final MeveoSampler sampler = (MeveoSampler) element;
        sampler.setArguments((Arguments) argsPanel.createTestElement());
    }

    @Override
    public void configure(TestElement element) {
        final MeveoSampler sampler = (MeveoSampler) element;
        argsPanel.configure(sampler.getArguments());
    }

}
