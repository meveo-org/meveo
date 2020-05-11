package org.hibernate.util;

import java.io.Serializable;
import java.util.Objects;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.Type;

/**
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @since
 * @version
 */
public final class CacheKey implements Serializable {

	private static final long serialVersionUID = -3416131764216489395L;

	private final Object id;
	private final Type type;
	private final String entityOrRoleName;
	private final String tenantId;
	private final int hashCode;

	/**
	 * Construct a new key for a collection or entity instance. Note that an entity
	 * name should always be the root entity name, not a subclass entity name.
	 *
	 * @param id               The identifier associated with the cached data
	 * @param type             The Hibernate type mapping
	 * @param entityOrRoleName The entity or collection-role name.
	 * @param tenantId         The tenant identifier associated with this data.
	 * @param factory          The session factory for which we are caching
	 */
	CacheKey(final Object id, final Type type, final String entityOrRoleName, final String tenantId, final SessionFactoryImplementor factory) {
		this.id = id;
		this.type = type;
		this.entityOrRoleName = entityOrRoleName;
		this.tenantId = tenantId;
		this.hashCode = calculateHashCode(type, factory);
	}

	private int calculateHashCode(Type type, SessionFactoryImplementor factory) {
		int result = type.getHashCode(id, factory);
		result = 31 * result + (tenantId != null ? tenantId.hashCode() : 0);
		return result;
	}

	public Object getId() {
		return id;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (this == other) {
			return true;
		}
		if (hashCode != other.hashCode() || !(other instanceof CacheKey)) {
			// hashCode is part of this check since it is pre-calculated and hash must match
			// for equals to be true
			return false;
		}
		final CacheKey that = (CacheKey) other;
		return Objects.equals(entityOrRoleName, that.entityOrRoleName) && type.isEqual(id, that.id) && Objects.equals(tenantId, that.tenantId);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public String toString() {
		// Used to be required for OSCache
		return entityOrRoleName + '#' + id.toString();
	}
}
