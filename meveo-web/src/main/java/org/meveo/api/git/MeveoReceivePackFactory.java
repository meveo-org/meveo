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

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.http.server.resolver.DefaultReceivePackFactory;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.PostReceiveHook;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory instantiating a post receive web-hook that reset files onto head commit
 * @author clement.bareth
 * @lastModifiedVersion 6.4.0
 */
public class MeveoReceivePackFactory extends DefaultReceivePackFactory {
	
	@Inject 
	private static Logger log = LoggerFactory.getLogger(MeveoReceivePackFactory.class);

    private static final PostReceiveHook updateRefHook = (rp, commands) -> {
        try (Git git = new Git(rp.getRepository())){
            git.reset().setMode(ResetType.HARD).call();
        } catch (Exception e) {
            log.error("Error updating files", e);
        }

    };

    @Override
    public ReceivePack create(HttpServletRequest req, Repository db) throws ServiceNotEnabledException, ServiceNotAuthorizedException {
        ReceivePack receivePack = super.create(req, db);
        receivePack.setPostReceiveHook(updateRefHook);
        return receivePack;
    }
}
