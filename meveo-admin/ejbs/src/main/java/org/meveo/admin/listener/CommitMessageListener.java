package org.meveo.admin.listener;

import org.meveo.service.git.GitClient;
import org.slf4j.Logger;

import javax.enterprise.context.SessionScoped;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named
@SessionScoped
public class CommitMessageListener implements Serializable {

    private static final long serialVersionUID = -8338330543224047209L;

    @Inject
    private transient Logger log;

    @Inject
    private transient GitClient gitClient;

    private String commitMessage;

    public void setCommitMessage(String commitMessage){this.commitMessage = commitMessage;}

    public String getCommitMessage(){ return gitClient.getCommitMessage(); }

    public void ajaxListener(AjaxBehaviorEvent event) {
        log.info("commit message = {}",this.commitMessage);
        gitClient.setCommitMessage(this.commitMessage);
    }
}
