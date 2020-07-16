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

package org.meveo.jmeter.login.gui;

import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.gui.ComponentUtil;
import org.meveo.jmeter.login.model.Host;
import org.meveo.jmeter.utils.SwingUtils;
import org.meveo.jmeter.utils.Waiting;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class LoginDialog extends JDialog {

    private static final int WIDTH = 500;
    private final JComboBox<String> savedHostsList;
    private final JButton deleteHostBtn;
    private final JButton saveHostBtn;
    private final JButton loginButton;
    private final JPasswordField passwordField;
    private final JTextField usernameInput;
    private final JComboBox<String> protocolInput;
    private final JTextField hostInput;
    private final JTextField portInput;
    private final JTextField authServer;
    private final Waiting waiting;

    public LoginDialog() {
        super((JFrame) null, "Login", true);

        /* Protocol */
        JLabel protocolLabel = new JLabel("Protocol");

        final String[] protocols = {"http", "https"};
        protocolInput = new JComboBox<>(protocols);

        JPanel protocolPanel = new JPanel();
        protocolPanel.setLayout(new GridLayout(2, 1));
        protocolPanel.add(protocolLabel);
        protocolPanel.add(protocolInput);

        /* Host */
        JLabel hostLabel = new JLabel("Host");

        hostInput = new JTextField();

        JPanel hostNamePanel = new JPanel();
        hostNamePanel.setLayout(new GridLayout(2, 1));
        hostNamePanel.add(hostLabel);
        hostNamePanel.add(hostInput);

        /* Port */
        JLabel portLabel = new JLabel("Port");

        portInput = new JTextField();

        JPanel portPanel = new JPanel();
        portPanel.setLayout(new GridLayout(2, 1));
        portPanel.add(portLabel);
        portPanel.add(portInput);

        /* Username */
        JLabel usernameLabel = new JLabel("Username");

        usernameInput = new JTextField();

        JPanel usernamePanel = new JPanel();
        usernamePanel.setLayout(new GridLayout(2, 1));
        usernamePanel.add(usernameLabel);
        usernamePanel.add(usernameInput);

        /* Password */
        JLabel passwordLabel = new JLabel("Password");

        passwordField = new JPasswordField();

        JPanel passwordPanel = new JPanel();
        passwordPanel.setLayout(new GridLayout(2, 1));
        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordField);

        /* Login */
        loginButton = new JButton("Login");
        getRootPane().setDefaultButton(loginButton);

        JPanel loginPanel = new JPanel(new BorderLayout());
        loginPanel.add(loginButton, BorderLayout.EAST);
        loginPanel.setPreferredSize(new Dimension(WIDTH, 40));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 5));

        waiting = new Waiting(loginPanel, BorderLayout.WEST, false);

        /* Host information */
        GridBagConstraints fifthX = new GridBagConstraints();
        fifthX.fill = GridBagConstraints.BOTH;
        fifthX.insets = new Insets(0, 1, 0, 1);
        fifthX.weightx = (double) 1 / 10;

        GridBagConstraints threeFifthX = new GridBagConstraints();
        threeFifthX.fill = fifthX.fill;
        threeFifthX.insets = fifthX.insets;
        threeFifthX.weightx = (double) 8 / 10;

        JPanel hostInformation = new JPanel(new GridBagLayout());
        hostInformation.add(protocolPanel, fifthX);
        hostInformation.add(hostNamePanel, threeFifthX);
        hostInformation.add(portPanel, fifthX);
        hostInformation.setPreferredSize(new Dimension(WIDTH, 75));
        hostInformation.setBorder(BorderFactory.createTitledBorder("Host information"));

        /* Saved hosts */
        savedHostsList = new JComboBox<>();

        deleteHostBtn = new JButton("Delete");

        saveHostBtn = new JButton("Save");

        JPanel savedHostsPanel = new JPanel(new GridBagLayout());
        savedHostsPanel.add(savedHostsList, threeFifthX);
        savedHostsPanel.add(deleteHostBtn, fifthX);
        savedHostsPanel.add(saveHostBtn, fifthX);
        savedHostsPanel.setBorder(BorderFactory.createTitledBorder("Saved hosts"));

        /* Server */
        JPanel hostPanel = new JPanel(new BorderLayout());
        hostPanel.add(savedHostsPanel, BorderLayout.NORTH);
        hostPanel.add(hostInformation, BorderLayout.SOUTH);
        hostPanel.setBorder(BorderFactory.createTitledBorder("Server"));
        
        /* Credentials */
        JPanel credentials = new JPanel(new GridLayout(1, 2));
        credentials.add(usernamePanel);
        credentials.add(passwordPanel);
        credentials.setBorder(BorderFactory.createTitledBorder("Credentials"));
        credentials.setPreferredSize(new Dimension(WIDTH, 75));
        
        /* Auth server */
        JPanel authServerPanel = new JPanel(new BorderLayout());
        authServer = new JTextField();
        authServerPanel.add(authServer);
        authServerPanel.setBorder(BorderFactory.createTitledBorder("Authentication server (leave blank if same)"));
        
        /* Top component */
        JPanel informationPanel = new JPanel(new BorderLayout());
        informationPanel.add(hostPanel, BorderLayout.NORTH);
        informationPanel.add(credentials, BorderLayout.SOUTH);
        informationPanel.add(authServerPanel, BorderLayout.CENTER);
        
        /* Main dialog */
        setLayout(new BorderLayout());
        Dimension size = new Dimension(WIDTH, 350);
        setResizable(false);
        setSize(size);
        setPreferredSize(size);
        ComponentUtil.centerComponentInWindow(this);
        add(informationPanel, BorderLayout.NORTH);
        add(loginPanel, BorderLayout.SOUTH);
    }

    public void setSavedHosts(List<String> savedHosts) {
        List<String> hosts = new ArrayList<>(savedHosts);
        hosts.remove(StringUtils.EMPTY);
        hosts.add(0, StringUtils.EMPTY);
        savedHostsList.setModel(new DefaultComboBoxModel<>(hosts.toArray(new String[]{})));
        savedHostsList.repaint();
    }

    public void loadHost(Host host) {
        protocolInput.setSelectedItem(host.getProtocol());
        hostInput.setText(host.getHostName());
        portInput.setText(host.getPortNumber());
        authServer.setText(host.getAuthServer());
        savedHostsList.setSelectedItem(host.getNickName());
    }

    public void setOnSavedHostChange(ActionListener a) {
        savedHostsList.addActionListener(a);
    }

    public void setOnDeleteClick(ActionListener e) {
        deleteHostBtn.addActionListener(e);
    }

    public void setOnSaveClick(ActionListener e) {
        saveHostBtn.addActionListener(e);
    }

    public void setOnLoginClick(ActionListener e) {
        loginButton.addActionListener(e);
    }

    public String getProtocol() {
        return (String) protocolInput.getSelectedItem();
    }

    public String getHostName() {
        return hostInput.getText();
    }

    public String getPort() {
        return portInput.getText();
    }

    public String getUsername() {
        return usernameInput.getText();
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    public String getSelectedHost() {
        return (String) savedHostsList.getSelectedItem();
    }

    public void enableDialog(){
        waiting.stop();
        SwingUtils.setEnable(this, true);
    }

    public void disableDialog(){
        waiting.start();
        SwingUtils.setEnable(this, false);
    }

	/**
	 * @return
	 */
	public String getAuthServer() {
		return authServer.getText();
	}

}
