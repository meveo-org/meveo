package org.meveo.admin.action.neo4j;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Named;

import org.meveo.model.neo4j.Neo4JConfiguration;

/**
 * Controller for managing the listing of {@link Neo4JConfiguration}.
 * 
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 */
@Named
@ConversationScoped
public class Neo4jConfigurationListBean extends Neo4jConfigurationBean {

	private static final long serialVersionUID = -475201491959678995L;

}
