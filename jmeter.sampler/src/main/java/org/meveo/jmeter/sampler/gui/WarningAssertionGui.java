/**
 * 
 */
package org.meveo.jmeter.sampler.gui;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.assertions.gui.AbstractAssertionGui;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledTextField;
import org.meveo.jmeter.sampler.model.WarningAssertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GUI for {@link WarningAssertion}
 * 
 * @author clement.bareth
 * @since 6.10.0
 * @version 6.10.0
 */
public class WarningAssertionGui extends AbstractAssertionGui {
	
	private static final long serialVersionUID = -5109301934453611506L;
	private static Logger log = LoggerFactory.getLogger(WarningAssertionGui.class);
	
	private JLabeledTextField regex = null;
	private JLabeledTextField code = null;
	
	/**
	 * Instantiates a new WarningAssertionGui
	 *
	 */
	public WarningAssertionGui() {
		super();
		
		setLayout(new BorderLayout());
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);
        
        VerticalPanel panel = new VerticalPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        regex = new JLabeledTextField("Regex");
        code = new JLabeledTextField("Status code");
        
        code.addChangeListener(e -> {
        	try {
        		Integer.parseInt(code.getText());
        	} catch (NumberFormatException ex) {
        		code.setText("");
        	}
        });
        
        panel.add(regex);
        panel.add(code);
        
        add(panel, BorderLayout.CENTER);
	}
	
    @Override
    public void clearGui() {
        super.clearGui();
        code.setText("");
        regex.setText("");
    }

    @Override
    public String getLabelResource() {
        return WarningAssertionGui.class.getName();
    }

    @Override
    public String getStaticLabel() {
        return "Warning Assertion";
    }

	@Override
	public TestElement createTestElement() {
		WarningAssertion wa = new WarningAssertion();
		modifyTestElement(wa);
		return wa;
	}

	@Override
	public void modifyTestElement(TestElement element) {
		super.configureTestElement(element);
		
		WarningAssertion wa = (WarningAssertion) element;
		
		if(!StringUtils.isBlank(code.getText())) {
			wa.setStatusCode(Integer.parseInt(code.getText()));
		} else {
			wa.setStatusCode(-1);
		}
		
		wa.setRegex(regex.getText());
	}

	@Override
	public void configure(TestElement element) {
		super.configure(element);
		WarningAssertion wa = (WarningAssertion) element;
		regex.setText(wa.getRegex());
		
		if(wa.getStatusCode() != -1) {
			code.setText(String.valueOf(wa.getStatusCode()));
		} else {
			code.setText("");
		}
	}
	
	

}
