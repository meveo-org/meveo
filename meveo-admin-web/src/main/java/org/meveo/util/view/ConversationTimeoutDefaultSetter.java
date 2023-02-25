package org.meveo.util.view;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.servlet.ServletRequest;

public class ConversationTimeoutDefaultSetter {

    @Inject
    private Conversation conversation;

    public void conversationInitialized(
            @Observes @Initialized(ConversationScoped.class)
            ServletRequest payload) {
        conversation.setTimeout(43200000L); // 12 hours
    }

}