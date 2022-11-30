package org.meveo.admin.action.index;

import org.jboss.seam.international.locale.UserLocaleProducer;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Locale;

@Named
@ViewScoped
public class LocaleBean implements Serializable {

    private static final long serialVersionUID = -4534087316489937649L;

    @Inject
    @CurrentUser
    private MeveoUser currentUser;

    @Inject
    private UserLocaleProducer userLocaleProducer;

    private Locale locale;

    public Locale getLocale() {
        if (currentUser.getLocale() != null && !userLocaleProducer.getUserLocale().getLanguage().equals(currentUser.getLocale())) {
            locale = new Locale(currentUser.getLocale());
            userLocaleProducer.init(locale);
        } else {
            locale = userLocaleProducer.getUserLocale();
        }
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}

