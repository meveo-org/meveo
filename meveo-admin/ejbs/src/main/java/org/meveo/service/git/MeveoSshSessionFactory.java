/*
 * (C) Copyright 2018-2020 Webdrone SAS (https://www.webdrone.fr/) and contributors.
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

package org.meveo.service.git;

import com.jcraft.jsch.*;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.util.FS;

public class MeveoSshSessionFactory extends JschConfigSessionFactory {

    private String sshPrivateKey;
    private String sshPublicKey;
    private String sshPassphrase;

    public MeveoSshSessionFactory(String sshPrivateKey, String sshPublicKey, String sshPassphrase) {
        this.sshPrivateKey = sshPrivateKey;
        this.sshPublicKey = sshPublicKey;
        this.sshPassphrase = sshPassphrase;
    }

    @Override
    protected void configure(OpenSshConfig.Host hc, Session session) {
        session.setConfig("StrictHostKeyChecking", "no");
    }

    @Override
    protected JSch createDefaultJSch(FS fs) throws JSchException {
        JSch jSch = super.createDefaultJSch(fs);
        jSch.removeAllIdentity();
        jSch.addIdentity("identity", this.sshPrivateKey.trim().getBytes(), sshPublicKey.trim().getBytes(), this.sshPassphrase.getBytes());
        return jSch;
    }

}
