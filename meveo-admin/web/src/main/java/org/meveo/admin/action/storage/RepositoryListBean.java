package org.meveo.admin.action.storage;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Named;

import org.meveo.model.storage.Repository;

/**
 * Controller for managing the listing of {@link Repository}
 * 
 * @author Edward P. Legaspi
 */
@Named
@ConversationScoped
public class RepositoryListBean extends RepositoryBean {

	private static final long serialVersionUID = 1331175523413094770L;
}
