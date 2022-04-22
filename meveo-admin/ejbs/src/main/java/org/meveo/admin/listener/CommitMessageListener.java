package org.meveo.admin.listener;

import org.meveo.model.storage.Repository;
import org.meveo.service.git.GitClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named
@SessionScoped
public class CommitMessageListener implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private transient Logger log;

    @Inject
    private transient GitClient gitClient;

    private String commitMessage;

    public void setCommitMessage(String commitMessage){this.commitMessage = commitMessage;}

    public CommitMessageListener(){}
    //@Produces @SessionScoped
    public String getCommitMessage(){
        return this.commitMessage;
    }

    public void ajaxListener(AjaxBehaviorEvent event) {
        log.info("commit message = {}",this.commitMessage);
        gitClient.setCommitMessage(this.commitMessage);
    }
}
