package org.hibernate.util;

import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.internal.StandardPersisterClassResolver;

/**
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @since
 * @version
 */
public class CustomPersisterClassResolver extends StandardPersisterClassResolver {

	private static final long serialVersionUID = 1619559523252940544L;

	@Override
	public Class<? extends EntityPersister> singleTableEntityPersister() {
		return SingleAutoStaleObjectEvictingPersister.class;
	}

	@Override
	public Class<? extends EntityPersister> joinedSubclassEntityPersister() {
		return JoinedAutoStaleObjectEvictingPersister.class;
	}
}
