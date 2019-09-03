package org.meveo.admin.web.handler;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.enterprise.context.NonexistentConversationException;
import javax.faces.FacesException;
import javax.faces.application.NavigationHandler;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomExceptionHandler extends ExceptionHandlerWrapper {

    private ExceptionHandler exceptionHandler;
    
    private static final Logger log = LoggerFactory.getLogger(CustomExceptionHandler.class);

    @SuppressWarnings("deprecation")
	public CustomExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public ExceptionHandler getWrapped() {
        return exceptionHandler;
    }

    @Override
    public void handle() throws FacesException {
        final Iterator<ExceptionQueuedEvent> queue = getUnhandledExceptionQueuedEvents().iterator();

        while (queue.hasNext()){
            ExceptionQueuedEvent item = queue.next();
            ExceptionQueuedEventContext exceptionQueuedEventContext = (ExceptionQueuedEventContext)item.getSource();

            try {
                Throwable throwable = exceptionQueuedEventContext.getException();
                
                // Reload the page if NonexistentConversationException occurs
                if(throwable instanceof NonexistentConversationException || throwable instanceof ViewExpiredException) {
                	ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
                    ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());
                }else {
                	log.error("Exception", throwable);
                }

            } catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
                queue.remove();
            }
        }
    }
}