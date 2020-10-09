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

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.meveo.commons.utils.ParamBean;
import org.meveo.model.git.GitRepository;
import org.meveo.security.MeveoUser;
import org.meveo.security.PasswordUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Helper class for Git usage
 *
 * @author Clément Bareth
 */
public class GitHelper {

    private final static String GIT_DIR = "/git";

    /**
     * @param currentUser Logged user
     * @return the git directory relative to the file explorer directory for the user's provider
     */
    public static String getGitDirectory(MeveoUser currentUser) {
        String rootDir = ParamBean.getInstance().getChrootDir(currentUser != null ? currentUser.getProviderCode() : null);
        new File(rootDir + GIT_DIR).mkdirs();
        return rootDir + GIT_DIR;
    }

    /**
     * @param currentUser Logged user
     * @param code        Code of the git repository
     * @return the {@link File} object linked to the fiven git repository
     */
    public static File getRepositoryDir(MeveoUser currentUser, String code) {
        return new File(getGitDirectory(currentUser), code);
    }

    public static String computeRelativePath(File repositoryDir, File file) {
        // Obtain relative path of file
        String repositoryBasePath = repositoryDir.getAbsolutePath();
        String filePath = file.getAbsolutePath();
        final String relativePath = filePath.replace(repositoryBasePath, "")
                .replaceAll("\\\\", "/");

        if(relativePath.startsWith("/")) {
            return relativePath.substring(1);
        }

        return relativePath;
    }

    public static boolean hasReadRole(MeveoUser user, GitRepository repository) {
        if (CollectionUtils.isEmpty(repository.getReadingRoles())) {
            return true;
        }

        return repository.getReadingRoles().stream().anyMatch(r -> user.getRoles().contains(r));
    }

    public static boolean hasWriteRole(MeveoUser user, GitRepository repository) {
        if (CollectionUtils.isEmpty(repository.getWritingRoles())) {
            return true;
        }

        return repository.getWritingRoles().stream().anyMatch(r -> user.getRoles().contains(r));
    }

    public static CredentialsProvider getCredentialsProvider(GitRepository gitRepository, String username, String password, MeveoUser currentUser) {
        if (!StringUtils.isBlank(username)) {
            // If username and/or password are provided, use them
            if(password == null){
                password = "";
            }
            return new UsernamePasswordCredentialsProvider(username, password);

        } else if (gitRepository.hasCredentials()) {
            // Use configured / default credentials as fallback
            String pwd = PasswordUtils.decrypt(gitRepository.getSalt(), gitRepository.getDefaultRemotePassword());
			return new UsernamePasswordCredentialsProvider(gitRepository.getDefaultRemoteUsername(), pwd);

        } else if(gitRepository.isMeveoRepository()){
            // If repository is hosted in meveo instance, we can use the token the current user is logged in with
            return  new UsernamePasswordCredentialsProvider(currentUser.getToken(), ""); // FIXME: /!\ This may not work, need to test /!\

        }

        return null;
    }

    /**
     * Generate a RSA key pair
     *
     * @param username Username to put on public key
     * @param passphrase Passphrase to encrypt private key
     * @return the generated key pair
     */
    public static RSAKeyPair generateRSAKey(String username, String passphrase) {

        try (ByteArrayOutputStream privateOutputStream = new ByteArrayOutputStream();
             ByteArrayOutputStream publicOutputStream = new ByteArrayOutputStream()) {

            JSch jsch = new JSch();

            KeyPair kpair = KeyPair.genKeyPair(jsch, KeyPair.RSA);
            
            if(passphrase != null) {
            	kpair.writePrivateKey(privateOutputStream, passphrase.getBytes());
            } else {
            	kpair.writePrivateKey(privateOutputStream);
            }
            
            kpair.writePublicKey(publicOutputStream, username);
            kpair.dispose();

            String privateKey = privateOutputStream.toString(StandardCharsets.UTF_8.name());
            String publicKey = publicOutputStream.toString(StandardCharsets.UTF_8.name());

            return new RSAKeyPair(privateKey, publicKey);

        } catch (JSchException | IOException e) {
            throw new RuntimeException(e);
        }


    }
}
