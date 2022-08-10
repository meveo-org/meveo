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

import org.eclipse.jgit.http.server.GitServlet;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.PacketLineOut;
import org.eclipse.jgit.transport.SideBandOutputStream;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.representations.AccessToken;
import org.meveo.admin.exception.BusinessException;
import org.meveo.event.qualifier.git.CommitEvent;
import org.meveo.event.qualifier.git.CommitReceived;
import org.meveo.model.git.GitActionType;
import org.meveo.model.git.GitRepository;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.meveo.service.git.GitClient;
import org.meveo.service.git.GitHelper;
import org.meveo.service.git.GitRepositoryService;
import org.meveo.service.git.MeveoRepository;
import org.slf4j.Logger;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import static org.eclipse.jgit.transport.SideBandOutputStream.CH_ERROR;
import static org.eclipse.jgit.transport.SideBandOutputStream.MAX_BUF;

/**
 * Servlet enabling git through HTTP(s)
 * @author clement.bareth
 * @lastModifiedVersion 6.4.0
 */
@WebServlet(
        urlPatterns = "/git/*"
)
public class MeveoGitServlet extends GitServlet {

    private static Map<String, GitActionType> SERVICE_ROLE_MAPPING = new HashMap<>();

    static {
        SERVICE_ROLE_MAPPING.put("git-upload-pack", GitActionType.READ);
        SERVICE_ROLE_MAPPING.put("git-receive-pack", GitActionType.WRITE);
        SERVICE_ROLE_MAPPING.put("GET", GitActionType.GET);
    }
	
    @Inject
    @CurrentUser
    private MeveoUser currentUser;

    @Inject
    @MeveoRepository
    private GitRepository meveoRepository;

    @Inject
    private GitRepositoryService gitRepositoryService;

    @Inject
    private Logger log;

    @Inject
    @CommitReceived
    private Event<CommitEvent> gitRepositoryCommitedEvent;

    @Inject
    private GitClient gitClient;

    @Override
    public void init(ServletConfig config) throws ServletException {
        Map<String, String> initParameters = new HashMap<>();
        initParameters.put("base-path", GitHelper.getGitDirectory(currentUser));
        initParameters.put("export-all", "1");

        ServletConfig servletConfig = new ServletConfig(){

            @Override
            public String getServletName() {
                return "GitServlet";
            }

            @Override
            public ServletContext getServletContext() {
                return config.getServletContext();
            }

            @Override
            public String getInitParameter(String name) {
                return initParameters.get(name);
            }

            @Override
            public Enumeration<String> getInitParameterNames() {
                return Collections.enumeration(initParameters.keySet());
            }
        };

        setReceivePackFactory(new MeveoReceivePackFactory());

        super.init(servletConfig);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // User should be authenticated
        if(req.getUserPrincipal() == null){
            res.addHeader("WWW-Authenticate", "Basic realm=\"Meveo Git access\", charset=\"UTF-8\"");
            res.getWriter().print("You must be logged to access the Meveo Git server");
            res.setStatus(401);
            return;
        }

        // Extract service and code
        String service = null;
        if(req.getQueryString() != null) {
        	service = req.getQueryString().replaceAll(".*service=([\\w-]+).*", "$1");
        } else if (req.getRequestURI().matches(".*(git-.*-pack).*")){
        	service = req.getRequestURI().replaceAll(".*(git-.*-pack).*", "$1");
        } else {
        	service = req.getMethod();
        }

        String code = req.getRequestURL().toString().replaceAll(".*/git/([^/]+).*", "$1");

        // Check if user is authorized
        boolean authorized;

        final GitActionType gitActionType = SERVICE_ROLE_MAPPING.get(service);
        final GitRepository gitRepository = code.equals(meveoRepository.getCode()) ? meveoRepository : gitRepositoryService.findByCode(code);

        if(gitRepository == null) {
        	res.setStatus(404);
        	return;
        }
        
        if(gitActionType == null) {
        	authorized = false;
        } else {
        
	        switch (gitActionType) {
	        	case GET:
	            case READ: authorized = GitHelper.hasReadRole(currentUser, gitRepository);
	                break;
	
	            case WRITE: authorized = GitHelper.hasWriteRole(currentUser, gitRepository);
	                break;
	
	            default:
	                authorized = false;
	                log.error("Unmapped service type {}", service);
	                break;
	        }
        
        }
        
        if(!authorized) {
            res.setStatus(403);
        	if(gitActionType == GitActionType.GET) {
        		try {
					req.getRequestDispatcher("/errors/403.xhtml")
						.forward(req, res);
					
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
        		
        	} else {
        		sendErrorToClient(res, new Exception("You are not authorized to execute this action"));
        	}
            return;
        }

        // Do not let the git server send directly the response, in case an observer raise an exception
        FakeServletResponse fakeServletResponse = new FakeServletResponse(res);

        try {
        	// Return the file list
        	if(gitActionType == GitActionType.GET) {
        		req.getRequestDispatcher("/pages/admin/files/files.xhtml?folder=git" + req.getPathInfo())
        			.forward(req, res);
        	} else {
        		super.service(req, res);
        	}

        } catch(Exception e) {
            log.error("Git error", e);
            sendErrorToClient(res, e);
            return;
        }

        // Fire commit received event and rollback if an exception was raised
        if(gitActionType == GitActionType.WRITE && req.getMethod().equals("POST")) {
            try {
                RevCommit headCommit = gitClient.getHeadCommit(gitRepository);
                var diffs = gitClient.getDiffs(gitRepository, headCommit);
                Set<String> modifiedFiles = gitClient.getModifiedFiles(diffs);
                gitRepositoryCommitedEvent.fire(new CommitEvent(gitRepository, modifiedFiles, diffs));

            } catch (Exception e) {
                try {
                    log.error("Error raised after commit, rolling back to previous commit", e);
                    RevCommit headCommit = gitClient.getHeadCommit(gitRepository);
                    gitClient.reset(gitRepository, headCommit.getParent(0));
                    
                    sendErrorToClient(res, e);
                    
                } catch (BusinessException ex) {
                    log.error("Error while canceling commit", e);
                    sendErrorToClient(res, e);
                }

            }

        }

        if(res.getStatus() != 500) {
        	try {
	            ServletOutputStream outputStream = res.getOutputStream();
	            for (Integer b : fakeServletResponse.getLines()) {
	                outputStream.write(b);
	            }
	
	            outputStream.flush();
	            outputStream.close();
        	} catch (IllegalStateException e) {
        		
        	}

        } else {
            // Read the output stream and send error to the git client
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            fakeServletResponse.getLines().forEach(byteArrayOutputStream::write);
            String errorMessage = byteArrayOutputStream.toString("UTF-8");
            byteArrayOutputStream.close();

            Exception e = new Exception(errorMessage);
            sendErrorToClient(res, e);
        }
    }

	private void sendErrorToClient(HttpServletResponse res, Exception e) throws IOException {
		res.setContentType("application/x-git-receive-pack-result");
		ServletOutputStream outputStream = res.getOutputStream();

		PacketLineOut packetLineOut = new PacketLineOut(outputStream);
		packetLineOut.setFlushOnEnd(false);

		SideBandOutputStream sideBandOutputStream = new SideBandOutputStream(CH_ERROR, MAX_BUF, outputStream);

		// Find root cause
		Throwable cause = e;
		while(cause.getCause() != null){
		    cause = cause.getCause();
		}

		sideBandOutputStream.write(Constants.encode(cause.getMessage() + "\n"));
		sideBandOutputStream.flush();

		packetLineOut.end();
		sideBandOutputStream.close();
	}
}
