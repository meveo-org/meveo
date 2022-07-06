package org.meveo.api.module;

import java.io.Serializable;

public class UpdateModulesParameters implements Serializable {
    
    public UpdateModulesParameters() {
    }

    private String id;

    private GitCredentials gitCredentials;

    private String callbackUrl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GitCredentials getGitCredentials() {
        return gitCredentials;
    }

    public void setGitCredentials(GitCredentials gitCredentials) {
        this.gitCredentials = gitCredentials;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public static class GitCredentials {
        
        private String username;

        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
