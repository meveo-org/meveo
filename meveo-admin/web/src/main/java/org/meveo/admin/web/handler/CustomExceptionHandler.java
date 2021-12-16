package org.meveo.admin.web.handler;

import java.util.Iterator;
import java.util.Map;
import javax.faces.FacesException;
import javax.faces.application.NavigationHandler;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Edward P. Legaspi
 * @version 6.15.0
 */
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

    for (Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator(); i.hasNext(); ) {
      ExceptionQueuedEvent event = i.next();
      ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();
      Throwable t = context.getException();

      if (t instanceof ViewExpiredException) {
        ViewExpiredException vee = (ViewExpiredException) t;
        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, Object> requestMap = fc.getExternalContext().getRequestMap();
        NavigationHandler nav =
            fc.getApplication().getNavigationHandler();

        try {
          // Push some stuff to the request scope for later use in the page
          requestMap.put("currentViewId", vee.getViewId());
          nav.handleNavigation(fc, null, "viewExpired");
          fc.renderResponse();

        } finally {
          i.remove();
        }
      }
    }
    // Let the parent handle all the remaining queued exception events.
    getWrapped().handle();
  }

//  @Override
//  public void handle() throws FacesException {
//    final Iterator<ExceptionQueuedEvent> queue = getUnhandledExceptionQueuedEvents().iterator();
//
//    while (queue.hasNext()) {
//      ExceptionQueuedEvent item = queue.next();
//      ExceptionQueuedEventContext exceptionQueuedEventContext = (ExceptionQueuedEventContext) item.getSource();
//
//      try {
//        Throwable throwable = exceptionQueuedEventContext.getException();
//
//        // Reload the page if NonexistentConversationException occurs
//        if (throwable instanceof NonexistentConversationException || throwable instanceof ViewExpiredException) {
//          ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
//          ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());
//        } else {
//          log.error("Exception", throwable);
//        }
//
//      } catch (IOException e) {
//        throw new RuntimeException(e);
//      } finally {
//        queue.remove();
//      }
//    }
//
//    getWrapped().handle();
//  }
}