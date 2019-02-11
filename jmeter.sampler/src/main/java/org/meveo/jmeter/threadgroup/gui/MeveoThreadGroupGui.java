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

import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.AbstractThreadGroupGui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class MeveoThreadGroupGui extends AbstractThreadGroupGui implements ItemListener {

    @Override
    public String getLabelResource() {
        return getClass().getCanonicalName();
    }

    @Override
    public String getStaticLabel() {
        return "Meveo - Function Test";
    }

    private static final long serialVersionUID = 240L;

    public MeveoThreadGroupGui() {
        init();
        initGui();
    }

    @Override
    public TestElement createTestElement() {
        ThreadGroup tg = new ThreadGroup();
        modifyTestElement(tg);
        return tg;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement tg) {
        super.configureTestElement(tg);

    }

    @Override
    public void configure(TestElement tg) {
        super.configure(tg);
    }

    @Override
    public void itemStateChanged(ItemEvent ie) {
    }

    @Override
    public void clearGui(){
        super.clearGui();
        initGui();
    }

    // Initialise the gui field values
    private void initGui(){
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        // THREAD PROPERTIES
    }
}
