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

package org.meveo.jmeter.login.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.MainFrame;
import org.meveo.jmeter.function.FunctionManager;
import org.meveo.jmeter.function.gui.menu.LoadFunctionManagerMenu;
import org.meveo.jmeter.function.gui.menu.UploadFunctionMenu;
import org.meveo.jmeter.login.gui.LoginDialog;
import org.meveo.jmeter.login.model.Host;
import org.meveo.jmeter.login.model.HostConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.text.Collator;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LoginController {

    private static final Logger LOG = LoggerFactory.getLogger(FunctionManager.class);
    private static final String SAVED_HOSTS = "saved-hosts.json";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static List<Host> storedHosts = getStoredHosts();
    private static LoadFunctionManagerMenu loadFunctionManagerMenu;
    private static UploadFunctionMenu uploadFunctionMenu;

    public static void setLoadFunctionManagerMenu(LoadFunctionManagerMenu loadFunctionManagerMenu) {
        LoginController.loadFunctionManagerMenu = loadFunctionManagerMenu;
        loadFunctionManagerMenu.setEnabled(false);
    }

    public static void setUploadFunctionMenu(UploadFunctionMenu uploadFunctionMenu) {
        LoginController.uploadFunctionMenu = uploadFunctionMenu;
        uploadFunctionMenu.setEnabled(false);
    }

    private static List<Host> getStoredHosts() {

        try {

            File storedHostsFile = new File(SAVED_HOSTS);
            if (!storedHostsFile.exists()) {
                PrintWriter writer = new PrintWriter(SAVED_HOSTS, "UTF-8");
                writer.print("[]");
                writer.close();
            }

            return OBJECT_MAPPER.readValue(storedHostsFile, new TypeReference<List<Host>>() {});
        } catch (IOException e) {
            LOG.error("Can't retrieve stored hosts", e);
            return Collections.emptyList();
        }
    }

    private static synchronized void storeHost(Host host) {
        storedHosts.add(host);
        updateStoredHosts();
    }

    private static synchronized void deleteStoredHost(Host host) {
        storedHosts.remove(host);
        updateStoredHosts();
    }

    private static void updateStoredHosts() {
        try {
            PrintWriter writer = new PrintWriter(SAVED_HOSTS, "UTF-8");
            writer.print(OBJECT_MAPPER.writeValueAsString(storedHosts));
            writer.close();
        } catch (JsonProcessingException | FileNotFoundException | UnsupportedEncodingException e) {
            LOG.error("Can't update stored hosts", e);
        }
    }

    private final LoginDialog loginDialog;

    private Host loadedHost;

    public LoginController(LoginDialog loginDialog) {
        this.loginDialog = loginDialog;
        loginDialog.setOnLoginClick(this::onLoginClick);
        loginDialog.setOnSavedHostChange(this::onSavedHostSelected);
        loginDialog.setOnDeleteClick(this::onDeleteBtnClicked);
        loginDialog.setOnSaveClick(this::onSaveBtnClicked);
    }

    public void show() {
        setStoredHosts();
        if(loadedHost != null){
            loadHost(loadedHost);
        }
        final MainFrame currentGui = GuiPackage.getInstance().getMainFrame();
        this.loginDialog.pack();
        loginDialog.setLocationRelativeTo(currentGui);
        this.loginDialog.setVisible(true);
    }

    private void setStoredHosts() {
        List<String> storedHostsNames = storedHosts.stream()
                .map(Host::getNickName)
                .sorted(Collator.getInstance())
                .collect(Collectors.toList());
        loginDialog.setSavedHosts(storedHostsNames);
    }

    @SuppressWarnings("unused")
    private void onLoginClick(ActionEvent e) {

        loginDialog.disableDialog();

        // Retrieve host
        Host host = getHost();

        // Retrieve credentials
        HostConnection hostConnection = new HostConnection();
        hostConnection.setPassword(loginDialog.getPassword());
        hostConnection.setUserName(loginDialog.getUsername());
        hostConnection.setHost(host);

        boolean logged = FunctionManager.login(hostConnection);

        loginDialog.enableDialog();

        // If login succeed
        if(logged) {
            JOptionPane.showMessageDialog(loginDialog, "Successfuly logged to " + host.getHostName());
            loadFunctionManagerMenu.setEnabled(true);
            uploadFunctionMenu.setEnabled(true);
            loginDialog.setVisible(false);
        }else{
            JOptionPane.showMessageDialog(loginDialog, "Cannot log in to " + host.getHostName(), "Login failure", JOptionPane.ERROR_MESSAGE);
            loadFunctionManagerMenu.setEnabled(false);
            uploadFunctionMenu.setEnabled(false);
        }
    }

    private Host getHost() {
        Host host = new Host();
        host.setHostName(loginDialog.getHostName());
        host.setPortNumber(loginDialog.getPort());
        host.setProtocol(loginDialog.getProtocol());
        return host;
    }

    @SuppressWarnings("unused")
    private void onSavedHostSelected(ActionEvent event) {
        loadedHost = null;
        getSelectedHost().ifPresent(this::loadHost);
    }

    @SuppressWarnings("unused")
    private void onDeleteBtnClicked(ActionEvent event) {
        getSelectedHost().ifPresent(LoginController::deleteStoredHost);
        setStoredHosts();
    }

    @SuppressWarnings("unused")
    private void onSaveBtnClicked(ActionEvent event) {
        final Optional<Host> selectedHost = getSelectedHost();
        // If exists, update
        if(selectedHost.isPresent()){
            final Host host = selectedHost.get();
            final Host newHost = getHost();
            host.setHostName(newHost.getHostName());
            host.setPortNumber(newHost.getPortNumber());
            host.setProtocol(newHost.getProtocol());
            updateStoredHosts();
        }else {
            String nickName = JOptionPane.showInputDialog("Please provide a name for this host");
            Host host = getHost();
            host.setNickName(nickName);
            storeHost(host);
            setStoredHosts();
            loadHost(host);
        }

    }

    private void loadHost(Host host) {
        loginDialog.loadHost(host.getNickName(), host.getProtocol(), host.getHostName(), host.getPortNumber());
        loadedHost = host;
    }

    private Optional<Host> getSelectedHost() {
        return storedHosts.stream()
                .filter(h -> h.getNickName().equals(loginDialog.getSelectedHost()))
                .findFirst();
    }

}
