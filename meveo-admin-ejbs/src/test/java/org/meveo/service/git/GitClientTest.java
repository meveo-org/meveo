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

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GitClientTest {

    @Test
    public void computeRelativePathTest(){

        // File is child of repo
        File repoDir = new File("C:/toto/");
        File file = new File("C:/toto/tati/tata/MyClass.java");
        String relativePath = GitHelper.computeRelativePath(repoDir, file).replaceAll("\\\\", "/");
        assertEquals("tati/tata/MyClass.java", relativePath);

        // File is not a child of repo
        repoDir = new File("C:/toto/");
        file = new File("C:/otherRepo/tati/tata/MyClass.java");
        relativePath = GitHelper.computeRelativePath(repoDir, file).replaceAll("\\\\", "/");
        assertEquals("C:/otherRepo/tati/tata/MyClass.java", relativePath);

        // File is the repo
        repoDir = new File("C:/toto/");
        file = new File("C:/toto/");
        relativePath = GitHelper.computeRelativePath(repoDir, file);
        assertEquals("", relativePath);
    }

    @Test
    public void sshKeyGenTest(){
        RSAKeyPair rsaKeyPair = GitHelper.generateRSAKey("test.user@meveo", "meveo-user-pwd");
        assertNotNull(rsaKeyPair.getPrivateKey());
        assertNotNull(rsaKeyPair.getPublicKey());
    }

}