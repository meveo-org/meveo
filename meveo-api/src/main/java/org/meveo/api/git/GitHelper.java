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

package org.meveo.api.git;

import org.meveo.commons.utils.ParamBean;
import org.meveo.security.MeveoUser;

/**
 * Helper class to build git constants
 * @author Clément Bareth
 */
public class GitHelper {

    private final static String GIT_DIR = "/git";

    /**
     * @param currentUser Logged user
     * @return the git directory relative to the file explorer directory for the user's provider
     */
    public static String getGitDirectory(MeveoUser currentUser){
        String rootDir = ParamBean.getInstance().getChrootDir(currentUser.getProviderCode());
        return rootDir + GIT_DIR;
    }
}
