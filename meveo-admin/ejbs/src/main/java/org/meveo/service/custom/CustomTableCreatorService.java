package org.meveo.service.custom;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.AccessTimeout;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.meveo.admin.exception.BusinessException;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.commons.utils.StringUtils;
import org.meveo.jpa.EntityManagerProvider;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.persistence.sql.SQLStorageConfiguration;
import org.meveo.model.sql.SqlConfiguration;
import org.meveo.model.wf.Workflow;
import org.meveo.persistence.sql.SQLConnectionProvider;
import org.meveo.persistence.sql.SqlConfigurationService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.util.PersistenceUtils;
import org.slf4j.Logger;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.change.AddColumnConfig;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.AddColumnChange;
import liquibase.change.core.AddDefaultValueChange;
import liquibase.change.core.AddForeignKeyConstraintChange;
import liquibase.change.core.AddNotNullConstraintChange;
import liquibase.change.core.AddPrimaryKeyChange;
import liquibase.change.core.AddUniqueConstraintChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.DropColumnChange;
import liquibase.change.core.DropDefaultValueChange;
import liquibase.change.core.DropForeignKeyConstraintChange;
import liquibase.change.core.DropNotNullConstraintChange;
import liquibase.change.core.DropSequenceChange;
import liquibase.change.core.DropTableChange;
import liquibase.change.core.ModifyDataTypeChange;
import liquibase.change.core.RawSQLChange;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.MigrationFailedException;
import liquibase.precondition.core.NotPrecondition;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.precondition.core.PreconditionContainer.ErrorOption;
import liquibase.precondition.core.PreconditionContainer.FailOption;
import liquibase.precondition.core.TableExistsPrecondition;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.statement.DatabaseFunction;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.8.0
 */
@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
@Lock(LockType.WRITE)
public class CustomTableCreatorService implements Serializable {

	private static final String UUID = "uuid";

	private static final long serialVersionUID = -5858023657669249422L;

	@Inject
	private EntityManagerProvider entityManagerProvider;

	@Inject
	private Logger log;

	@Inject
	private CustomEntityTemplateService customEntityTemplateService;

	@Inject
	private SQLConnectionProvider sqlConnectionProvider;

	@Inject
	private SqlConfigurationService sqlConfigurationService;
	
	@Inject
	private CustomFieldTemplateService customFieldTemplateService;
	
	@Inject
	private CustomFieldsCacheContainerProvider cache;

	private EntityManager getEntityManager(String sqlConfigurationCode) {

		if (StringUtils.isBlank(sqlConfigurationCode)) {
			return entityManagerProvider.getEntityManagerWoutJoinedTransactions();

		} else {
			return sqlConnectionProvider.getEntityManager(sqlConfigurationCode);
		}
	}

	/**
	 * Create a table with two columns referencing source and target custom tables
	 * 
	 * @param crt {@link CustomRelationshipTemplate} to create table for
	 * @throws BusinessException if the {@link CustomRelationshipTemplate} is not
	 *                           configured to be stored in a custom table
	 */
	public boolean createCrtTable(CustomRelationshipTemplate crt) throws BusinessException {
		if (crt.getAvailableStorages() == null || !crt.getAvailableStorages().contains(DBStorageType.SQL)) {
			throw new BusinessException("CustomRelationshipTemplate " + crt.getCode() + " is not configured to be stored in a custom table");
		}

		String tableName = SQLStorageConfiguration.getDbTablename(crt);

		DatabaseChangeLog dbLog = new DatabaseChangeLog("path");

		ChangeSet changeset = new ChangeSet(tableName + "_CT_CP_" + System.currentTimeMillis(), "Meveo", false, false, "meveo", "", "", dbLog);

		// Make sure table does not exists before creating it
		TableExistsPrecondition tableExistsPrecondition = new TableExistsPrecondition();
		tableExistsPrecondition.setTableName(tableName);

		NotPrecondition notPrecondition = new NotPrecondition();
		notPrecondition.addNestedPrecondition(tableExistsPrecondition);

		PreconditionContainer precondition = new PreconditionContainer();
		precondition.setOnError(ErrorOption.HALT);
		precondition.setOnFail(FailOption.HALT);
		precondition.addNestedPrecondition(notPrecondition);

		changeset.setPreconditions(precondition);

		// Source column
		ColumnConfig sourceColumn = new ColumnConfig();
		sourceColumn.setName(SQLStorageConfiguration.getDbTablename(crt.getStartNode()));
		sourceColumn.setType("varchar(255)");

		// Target column
		ColumnConfig targetColumn = new ColumnConfig();
		targetColumn.setName(SQLStorageConfiguration.getDbTablename(crt.getEndNode()));
		targetColumn.setType("varchar(255)");

		// UUID column
		ColumnConfig uuidColumn = new ColumnConfig();
		uuidColumn.setName(UUID);
		uuidColumn.setType("varchar(255)");
		uuidColumn.setDefaultValueComputed(new DatabaseFunction("uuid_generate_v4()"));

		// Unique constraint if CRT is unique
		if (crt.isUnique()) {
			AddUniqueConstraintChange uniqueConstraint = new AddUniqueConstraintChange();
			uniqueConstraint.setColumnNames(sourceColumn.getName() + ", " + targetColumn.getName());
			uniqueConstraint.setTableName(tableName);
			changeset.addChange(uniqueConstraint);
		}

		// Table creation
		CreateTableChange createTableChange = new CreateTableChange();
		createTableChange.setTableName(tableName);
		createTableChange.addColumn(sourceColumn);
		createTableChange.addColumn(targetColumn);
		createTableChange.addColumn(uuidColumn);
		changeset.addChange(createTableChange);

		// Primary key constraint addition
		AddPrimaryKeyChange addPrimaryKeyChange = new AddPrimaryKeyChange();
		addPrimaryKeyChange.setColumnNames(uuidColumn.getName());
		addPrimaryKeyChange.setTableName(tableName);
		changeset.addChange(addPrimaryKeyChange);

		// Source foreign key if source cet is a custom table
		if (crt.getStartNode().getSqlStorageConfiguration() != null && crt.getStartNode().getSqlStorageConfiguration().isStoreAsTable()) {
			AddForeignKeyConstraintChange sourceFkChange = new AddForeignKeyConstraintChange();
			sourceFkChange.setBaseColumnNames(sourceColumn.getName());
			sourceFkChange.setConstraintName(sourceColumn.getName() + "_fk");
			sourceFkChange.setReferencedColumnNames(UUID);
			sourceFkChange.setBaseTableName(tableName);
			sourceFkChange.setReferencedTableName(sourceColumn.getName());
			changeset.addChange(sourceFkChange);
		}

		// Target foreign key if target cet is a custom table
		if (crt.getEndNode().getSqlStorageConfiguration() != null && crt.getEndNode().getSqlStorageConfiguration().isStoreAsTable()) {
			AddForeignKeyConstraintChange targetFkChange = new AddForeignKeyConstraintChange();
			targetFkChange.setConstraintName(targetColumn.getName() + "_fk");
			targetFkChange.setBaseColumnNames(targetColumn.getName());
			targetFkChange.setReferencedColumnNames(UUID);
			targetFkChange.setBaseTableName(tableName);
			targetFkChange.setReferencedTableName(targetColumn.getName());
			changeset.addChange(targetFkChange);
		}

		dbLog.addChangeSet(changeset);

		EntityManager em = getEntityManager(null);

		Session hibernateSession = em.unwrap(Session.class);

		AtomicBoolean created = new AtomicBoolean();
		created.set(true);

		hibernateSession.doWork(connection -> {

			Database database;
			try {
				database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
				setSchemaName(database);
				Liquibase liquibase = new Liquibase(dbLog, new ClassLoaderResourceAccessor(), database);
				liquibase.update(new Contexts(), new LabelExpression());

			} catch (MigrationFailedException e) {
				if (e.getMessage().toLowerCase().contains("precondition")) {
					created.set(false);
				} else {
					throw new HibernateException(e);
				}
			} catch (Exception e) {
				log.error("Failed to create a custom table {}", tableName, e);
				throw new SQLException(e);
			}

		});

		return created.get();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void createTable(String sqlConnectionCode, String dbTableName) {
		createTable(sqlConnectionCode, dbTableName, true);
	}
	
	/**
	 * Create a table with a single 'id' field. Value is autoincremented for mysql
	 * or taken from sequence for Postgress databases.
	 * 
	 * @param dbTableName DB table name
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void createTable(String sqlConnectionCode, String dbTableName, boolean createSequence) {

		DatabaseChangeLog dbLog = new DatabaseChangeLog("path");

		// Changeset for Postgress
		ChangeSet pgChangeSet = new ChangeSet(dbTableName + "_CT_CP_" + System.currentTimeMillis(), "Meveo", false, false, "meveo", "", "postgresql", dbLog);

		CreateTableChange createPgTableChange = new CreateTableChange();
		createPgTableChange.setTableName(dbTableName);

		ColumnConfig pgUuidColumn = new ColumnConfig();
		pgUuidColumn.setName(UUID);
		pgUuidColumn.setType("varchar(255)");
		pgUuidColumn.setDefaultValueComputed(new DatabaseFunction("uuid_generate_v4()"));

		ConstraintsConfig idConstraints = new ConstraintsConfig();
		idConstraints.setNullable(false);
		idConstraints.setPrimaryKey(true);
		idConstraints.setPrimaryKeyName(dbTableName + "PK");

		pgUuidColumn.setConstraints(idConstraints);
		createPgTableChange.addColumn(pgUuidColumn);
		pgChangeSet.addChange(createPgTableChange);
		
		// Statement generated by liquibase not suitable for postgres < 9.5
		SqlConfiguration sqlConf = sqlConfigurationService.findByCode(sqlConnectionCode);
		String schema = StringUtils.isBlank(sqlConf.getSchema()) ? "public" : sqlConf.getSchema();
		
		if (createSequence) {
			RawSQLChange createPgSequence = new RawSQLChange("CREATE SEQUENCE " + schema + "." + dbTableName + "_seq;");
			pgChangeSet.addChange(createPgSequence);
		}

		dbLog.addChangeSet(pgChangeSet);

		// Changeset for mysql
		ChangeSet mysqlChangeSet = new ChangeSet(dbTableName + "_CT_CM_" + System.currentTimeMillis(), "Meveo", false, false, "meveo", "", "mysql", dbLog);

		CreateTableChange createMsTableChange = new CreateTableChange();
		createMsTableChange.setTableName(dbTableName);

		ColumnConfig msUuidcolumn = new ColumnConfig();
		msUuidcolumn.setName(UUID);
		msUuidcolumn.setType("varchar(255)");
		msUuidcolumn.setDefaultValueComputed(new DatabaseFunction("uuid()"));

		msUuidcolumn.setConstraints(idConstraints);
		createMsTableChange.addColumn(msUuidcolumn);

		mysqlChangeSet.addChange(createMsTableChange);
		dbLog.addChangeSet(mysqlChangeSet);

		Session hibernateSession = sqlConnectionProvider.getSession(sqlConnectionCode);
		
		hibernateSession.doWork(connection -> {
			var meta = connection.getMetaData();
			
			Database database;
			try {
				database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
				setSchemaName(database);

			} catch (DatabaseException e1) {
				log.error("Failed to retrieve database for connection {}", connection);
				throw new SQLException(e1);
			}
			
			// Check table does not exists
			try (var res = meta.getTables(null, database.getDefaultSchemaName(), dbTableName, new String[] {"TABLE"})) {
				if(res.next()) {
					throw new IllegalArgumentException("Table with name " + dbTableName + " in schema " + database.getDefaultSchemaName() + " already exists !");
				}
			}
			
			// Check sequence does not exists
			try (var res = meta.getTables(null, database.getDefaultSchemaName(), dbTableName + "_seq", new String[] {"SEQUENCE"})) {
				if(res.next()) {
					throw new IllegalArgumentException("Sequence with name " + dbTableName + "_seq in schema " + database.getDefaultSchemaName() + " already exists !");
				}
			}
			
			try {

				Liquibase liquibase = new Liquibase(dbLog, new ClassLoaderResourceAccessor(), database);
				liquibase.update(new Contexts(), new LabelExpression());

			} catch (Exception e) {
				log.error("Failed to create a custom table {} on SQL Configuration {}", dbTableName, sqlConnectionCode, e);
				throw new SQLException(e);
			}

		});

		hibernateSession.close();
	}

	private void setSchemaName(Database database) throws DatabaseException {
		String schemaName = null;
		Matcher matcher = Pattern.compile("currentSchema=([^&]*)").matcher(database.getConnection().getURL());
		if (matcher.find()) {
			schemaName = matcher.group(1);
		}
		database.setDefaultSchemaName(schemaName);
		database.setLiquibaseSchemaName(schemaName);
	}

	/**
	 * Add a field to a db table. Creates a liquibase changeset to add a field to a
	 * table and executes it
	 * 
	 * @param dbTableName DB Table name
	 * @param cft         Field definition
	 */
	@AccessTimeout(value = 1L, unit = TimeUnit.MINUTES)
	public void addField(String sqlConnectionCode, String dbTableName, CustomFieldTemplate cft) {
		addField(sqlConnectionCode, dbTableName, cft, true);
	}
	
	@AccessTimeout(value = 1L, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void addField(String sqlConnectionCode, String dbTableName, CustomFieldTemplate cft, boolean checkStorage) {

		// Don't add field if not stored in sql
		if (checkStorage && !cft.getStoragesNullSafe().contains(DBStorageType.SQL)) {
			return;
		}

		String dbFieldname = cft.getDbFieldname();

		if (cft.getFieldType() == CustomFieldTypeEnum.STRING && (cft.getMaxValue() == null || cft.getMaxValue() < 1)) {
			cft.setMaxValue(CustomFieldTemplate.DEFAULT_MAX_LENGTH_STRING);
		}

		DatabaseChangeLog dbLog = new DatabaseChangeLog("path");

		ChangeSet changeSet = new ChangeSet(dbTableName + "_CT_" + dbFieldname + "_AF_" + System.currentTimeMillis(), "Meveo", false, false, "meveo", "", "", dbLog);

		String columnType;
		try {
			columnType = getColumnType(cft);

		} catch (ClassNotFoundException e1) {
			throw new IllegalArgumentException("Cannot get field type for entity with class or code " + cft.getEntityClazzCetCode(), e1);
		}

		// Check if column type is handled
		if (columnType != null) {

			AddColumnChange addColumnChange = new AddColumnChange();
			addColumnChange.setTableName(dbTableName);

			AddColumnConfig column = new AddColumnConfig();
			column.setName(dbFieldname);
			setDefaultValue(cft, column);
			column.setType(columnType);

			ConstraintsConfig constraints = new ConstraintsConfig();
			column.setConstraints(constraints);

			if (cft.isValueRequired()) {
				constraints.setNullable(false);
			}

			if (cft.isUnique()) {
				constraints.setUnique(true);
				constraints.setUniqueConstraintName(getUniqueConstraintName(dbTableName, dbFieldname));
			}

			addColumnChange.setColumns(Collections.singletonList(column));
			changeSet.addChange(addColumnChange);
		}

		// Add a foreign key constraint pointing on referenced table if field is an
		// entity reference
		if (cft.getFieldType() == CustomFieldTypeEnum.ENTITY && cft.getStorageType().equals(CustomFieldStorageTypeEnum.SINGLE)) {

			addForeignKey(sqlConnectionCode, changeSet, cft, dbTableName, dbFieldname);
		}

		if (!changeSet.getChanges().isEmpty()) {

			dbLog.addChangeSet(changeSet);

			Session hibernateSession = sqlConnectionProvider.getSession(sqlConnectionCode);

			try {
				CompletableFuture.runAsync(() -> {
					hibernateSession.doWork(connection -> {
						Database database;
						try {
							database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
							setSchemaName(database);
							Liquibase liquibase = new Liquibase(dbLog, new ClassLoaderResourceAccessor(), database);
							liquibase.update(new Contexts(), new LabelExpression());
							liquibase.forceReleaseLocks();

						} catch (Exception e) {
							log.error("Failed to add field {} to custom table {}", dbFieldname, dbTableName, e);
							throw new SQLException(e);
						}
					});
				}).get(1, TimeUnit.MINUTES);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				log.error("Failed to add field {} to custom table {} within 1 minute", e);
				throw new RuntimeException(e);
			} finally {
				hibernateSession.close();
			}
		}
	}

	/**
	 * Update a field of a db table. Creates a liquibase changeset to add a field to
	 * a table and executes it
	 * 
	 * @param dbTableName DB Table name
	 * @param cft         Field definition
	 */
	public void updateField(String sqlConnectionCode, String dbTableName, CustomFieldTemplate cft) {

		String dbFieldname = cft.getDbFieldname();

		Session hibernateSession = sqlConnectionProvider.getSession(sqlConnectionCode);

		String columnExistsQueryStr = "SELECT EXISTS(\n" + "	SELECT column_name\n" + "	FROM information_schema.columns \n"
				+ "	WHERE table_name=:tableName and column_name=:columnName\n" + ");";

		Query columnExistsQuery = hibernateSession.createNativeQuery(columnExistsQueryStr).setParameter("tableName", dbTableName).setParameter("columnName", dbFieldname);

		boolean columnExists = (boolean) columnExistsQuery.getSingleResult();

		if (!columnExists) {
			addField(sqlConnectionCode, dbTableName, cft);
			return;
		}

		DatabaseChangeLog dbLog = new DatabaseChangeLog("path");

		// Drop not null constraint and add again if needed - a better way would be to
		// check if valueRequired field value was changed
		ChangeSet changeSet = new ChangeSet(dbTableName + "_CT_" + dbFieldname + "_RNN_" + System.currentTimeMillis(), "Meveo", false, false, "meveo", "", "", dbLog);
		changeSet.setFailOnError(false);

		DropNotNullConstraintChange dropNotNullChange = new DropNotNullConstraintChange();
		dropNotNullChange.setTableName(dbTableName);
		dropNotNullChange.setColumnName(dbFieldname);

		if (cft.getFieldType() == CustomFieldTypeEnum.DATE) {
			dropNotNullChange.setColumnDataType("datetime");
		} else if (cft.getFieldType() == CustomFieldTypeEnum.DOUBLE) {
			dropNotNullChange.setColumnDataType("numeric(23, 12)");
		} else if (cft.getFieldType() == CustomFieldTypeEnum.LONG) {
			dropNotNullChange.setColumnDataType("bigInt");
		} else if (cft.getFieldType() == CustomFieldTypeEnum.STRING || cft.getFieldType() == CustomFieldTypeEnum.LIST) {
			dropNotNullChange.setColumnDataType("varchar(" + (cft.getMaxValue() == null ? CustomFieldTemplate.DEFAULT_MAX_LENGTH_STRING : cft.getMaxValue()) + ")");
		}

		changeSet.addChange(dropNotNullChange);
		dbLog.addChangeSet(changeSet);

		// Add not null constraint if needed
		if (cft.isValueRequired()) {
			changeSet = new ChangeSet(dbTableName + "_CT_" + dbFieldname + "_ANN_" + System.currentTimeMillis(), "Meveo", false, false, "meveo", "", "", dbLog);
			AddNotNullConstraintChange addNotNullChange = new AddNotNullConstraintChange();

			addNotNullChange.setTableName(dbTableName);
			addNotNullChange.setColumnName(dbFieldname);

			if (cft.getFieldType() == CustomFieldTypeEnum.DATE) {
				addNotNullChange.setColumnDataType("datetime");
			} else if (cft.getFieldType() == CustomFieldTypeEnum.DOUBLE) {
				addNotNullChange.setColumnDataType("numeric(23, 12)");
			} else if (cft.getFieldType() == CustomFieldTypeEnum.LONG) {
				addNotNullChange.setColumnDataType("bigInt");
			} else if (cft.getFieldType() == CustomFieldTypeEnum.STRING || cft.getFieldType() == CustomFieldTypeEnum.LIST) {
				addNotNullChange.setColumnDataType("varchar(" + (cft.getMaxValue() == null ? CustomFieldTemplate.DEFAULT_MAX_LENGTH_STRING : cft.getMaxValue()) + ")");
			}

			changeSet.addChange(addNotNullChange);
			dbLog.addChangeSet(changeSet);

		}

		// Drop default value and add it again if needed - a better way would be to
		// check if defaultValue field value was changed
		// Default value does not apply to date type field
		if (cft.getFieldType() != CustomFieldTypeEnum.DATE) {
			changeSet = new ChangeSet(dbTableName + "_CT_" + dbFieldname + "_RD_" + System.currentTimeMillis(), "Meveo", false, false, "meveo", "", "", dbLog);
			changeSet.setFailOnError(false);

			DropDefaultValueChange dropDefaultValueChange = new DropDefaultValueChange();
			dropDefaultValueChange.setTableName(dbTableName);
			dropDefaultValueChange.setColumnName(dbFieldname);

			if (cft.getFieldType() == CustomFieldTypeEnum.DOUBLE) {
				dropDefaultValueChange.setColumnDataType("numeric(23, 12)");
			} else if (cft.getFieldType() == CustomFieldTypeEnum.LONG) {
				dropDefaultValueChange.setColumnDataType("bigInt");
			} else if (cft.getFieldType() == CustomFieldTypeEnum.STRING || cft.getFieldType() == CustomFieldTypeEnum.LIST) {
				dropDefaultValueChange.setColumnDataType("varchar(" + (cft.getMaxValue() == null ? CustomFieldTemplate.DEFAULT_MAX_LENGTH_STRING : cft.getMaxValue()) + ")");
			}

			changeSet.addChange(dropDefaultValueChange);
			dbLog.addChangeSet(changeSet);

			// Add default value if needed
			if (cft.getDefaultValue() != null) {
				changeSet = new ChangeSet(dbTableName + "_CT_" + dbFieldname + "_AD_" + System.currentTimeMillis(), "Meveo", false, false, "meveo", "", "", dbLog);
				AddDefaultValueChange addDefaultValueChange = new AddDefaultValueChange();

				addDefaultValueChange.setTableName(dbTableName);
				addDefaultValueChange.setColumnName(dbFieldname);

				if (cft.getFieldType() == CustomFieldTypeEnum.DOUBLE) {
					addDefaultValueChange.setColumnDataType("numeric(23, 12)");
					addDefaultValueChange.setDefaultValueNumeric(cft.getDefaultValue());
				} else if (cft.getFieldType() == CustomFieldTypeEnum.LONG) {
					addDefaultValueChange.setColumnDataType("bigInt");
					addDefaultValueChange.setDefaultValueNumeric(cft.getDefaultValue());
				} else if (cft.getFieldType() == CustomFieldTypeEnum.STRING || cft.getFieldType() == CustomFieldTypeEnum.LIST) {
					addDefaultValueChange.setColumnDataType("varchar(" + (cft.getMaxValue() == null ? CustomFieldTemplate.DEFAULT_MAX_LENGTH_STRING : cft.getMaxValue()) + ")");
					addDefaultValueChange.setDefaultValue(cft.getDefaultValue());
				}

				changeSet.addChange(addDefaultValueChange);
				dbLog.addChangeSet(changeSet);

			}
		}

		// Update field length for String type fields.
		if (cft.getFieldType() == CustomFieldTypeEnum.STRING || cft.getFieldType() == CustomFieldTypeEnum.LIST) {
			changeSet = new ChangeSet(dbTableName + "_CT_" + dbFieldname + "_M_" + System.currentTimeMillis(), "Meveo", false, false, "meveo", "", "", dbLog);
			changeSet.setFailOnError(false);

			ModifyDataTypeChange modifyDataTypeChange = new ModifyDataTypeChange();
			modifyDataTypeChange.setTableName(dbTableName);
			modifyDataTypeChange.setColumnName(dbFieldname);
			modifyDataTypeChange.setNewDataType("varchar(" + (cft.getMaxValue() == null ? CustomFieldTemplate.DEFAULT_MAX_LENGTH_STRING : cft.getMaxValue()) + ")");

			changeSet.addChange(modifyDataTypeChange);
			dbLog.addChangeSet(changeSet);
		}
		createOrUpdateUniqueField(dbTableName, cft, changeSet);

		hibernateSession.doWork(connection -> {

			Database database;
			try {
				database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
				setSchemaName(database);
				Liquibase liquibase = new Liquibase(dbLog, new ClassLoaderResourceAccessor(), database);
				liquibase.update(new Contexts(), new LabelExpression());

			} catch (Exception e) {
				log.error("Failed to update a field {} in a custom table {}", dbTableName, dbFieldname, e);
				throw new SQLException(e);
			}
		});

		hibernateSession.close();
	}

	/**
	 * Add a foreign key
	 * 
	 * @param sqlConnectionCode SQL connection code
	 * @param changeSet         Liquibase changeSet
	 * @param cft               the custom field template
	 * @param dbTableName       SQL table name
	 * @param dbFieldname       SQL field name
	 */
	private void addForeignKey(String sqlConnectionCode, ChangeSet changeSet, CustomFieldTemplate cft, String dbTableName, String dbFieldname) {
		String referenceColumnNames = UUID;

		// Only add foreign key constraint if referenced entity is stored as table
		CustomEntityTemplate referenceCet = customEntityTemplateService.findByCode(cft.getEntityClazzCetCode());
		if(referenceCet == null) {
			referenceCet = cache.getCustomEntityTemplate(cft.getEntityClazzCetCode());
		}
		
		String referenceTableName = null;
		if (referenceCet == null) {
			try {
				if (cft.getEntityClazz().startsWith(CustomEntityTemplate.class.getName())) {
					referenceTableName = SQLStorageConfiguration.getCetDbTablename(cft.getEntityClazzCetCode());
				} else {
					Class<?> jpaEntityClazz = Class.forName(cft.getEntityClazzCetCode());

					// get field type of reference entity
					referenceColumnNames = PersistenceUtils.getPKColumnName(jpaEntityClazz);
					referenceTableName = PersistenceUtils.getTableName(jpaEntityClazz);
				}

			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException("Cannot create foreign key constraint. Referenced cet or jpa entity " + cft.getEntityClazzCetCode() + " does not exists");
			}

		} else {
			referenceTableName = SQLStorageConfiguration.getDbTablename(referenceCet);
		}

		if ((referenceCet != null && referenceCet.getSqlStorageConfiguration() != null && referenceCet.getSqlStorageConfiguration().isStoreAsTable()) || referenceCet == null) {
			AddForeignKeyConstraintChange foreignKeyConstraint = new AddForeignKeyConstraintChange();
			foreignKeyConstraint.setBaseColumnNames(dbFieldname);
			foreignKeyConstraint.setBaseTableName(dbTableName);
			foreignKeyConstraint.setReferencedColumnNames(referenceColumnNames);
			foreignKeyConstraint.setReferencedTableName(referenceTableName);
			foreignKeyConstraint.setConstraintName(getFkConstraintName(dbTableName, cft));

			changeSet.addChange(foreignKeyConstraint);
		}
	}

	/**
	 * Add a change for dropping or creating a unique constraint for a CFT
	 * 
	 * @param dbTableName Table concerned by the changeset
	 * @param cft         Concernced CFT
	 * @param changeSet   Changeset to add the change
	 */
	private void createOrUpdateUniqueField(String dbTableName, CustomFieldTemplate cft, ChangeSet changeSet) {
		String dbFieldname = cft.getDbFieldname();
		if (cft.isSqlStorage()) {
			if (cft.isUnique()) {
				AddUniqueConstraintChange uniqueConstraint = new AddUniqueConstraintChange();
				uniqueConstraint.setColumnNames(dbFieldname);
				uniqueConstraint.setConstraintName(getUniqueConstraintName(dbTableName, dbFieldname));
				uniqueConstraint.setDeferrable(false);
				uniqueConstraint.setDisabled(false);
				uniqueConstraint.setInitiallyDeferred(false);
				uniqueConstraint.setTableName(dbTableName);
				changeSet.addChange(uniqueConstraint);
			} else {
				RawSQLChange sqlChange = new RawSQLChange();
				sqlChange.setSql("ALTER TABLE " + dbTableName + " DROP CONSTRAINT IF EXISTS " + getUniqueConstraintName(dbTableName, dbFieldname));
				changeSet.addChange(sqlChange);
			}
		}
	}

	/**
	 * @param dbTableName Table name
	 * @param dbFieldname Column name
	 * @return Concatenated unique constraint name
	 */
	public String getUniqueConstraintName(String dbTableName, String dbFieldname) {
		return "uk_" + dbTableName + "_" + dbFieldname;
	}

	/**
	 * Remove a field from a table
	 * 
	 * @param dbTableName Db table name to remove from
	 * @param cft         Field definition
	 */
	public void removeField(String sqlConnectionCode, String dbTableName, CustomFieldTemplate cft) {

		String dbFieldname = cft.getDbFieldname();

		DatabaseChangeLog dbLog = new DatabaseChangeLog("path");

		// Remove field
		ChangeSet changeSet = new ChangeSet(dbTableName + "_CT_" + dbFieldname + "_RF_" + System.currentTimeMillis(), "Meveo", false, false, "meveo", "", "", dbLog);
		changeSet.setFailOnError(false);

		DropColumnChange dropColumnChange = new DropColumnChange();
		dropColumnChange.setTableName(dbTableName);
		dropColumnChange.setColumnName(dbFieldname);

		// If cft is an entity reference, delete the foreign key constraint first
		if (cft.getFieldType() == CustomFieldTypeEnum.ENTITY) {
			DropForeignKeyConstraintChange dropForeignKeyConstraint = new DropForeignKeyConstraintChange();
			dropForeignKeyConstraint.setBaseTableName(dbTableName);
			dropForeignKeyConstraint.setConstraintName(getFkConstraintName(dbTableName, cft));
			changeSet.addChange(dropForeignKeyConstraint);
		}

		changeSet.addChange(dropColumnChange);
		dbLog.addChangeSet(changeSet);

		Session hibernateSession = sqlConnectionProvider.getSession(sqlConnectionCode);

		hibernateSession.doWork(connection -> {

			Database database;
			try {
				database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
				setSchemaName(database);
				Liquibase liquibase = new Liquibase(dbLog, new ClassLoaderResourceAccessor(), database);
				liquibase.update(new Contexts(), new LabelExpression());

			} catch (Exception e) {
				log.error("Failed to remove a field {} to a custom table {}", dbTableName, dbFieldname, e);
				throw new SQLException(e);
			}
		});

		hibernateSession.close();
	}

	/**
	 * Remove a table from DB
	 * 
	 * @param dbTableName Db table name to remove from
	 */
	public void removeTable(String sqlConnectionCode, String dbTableName) {
		log.info("Removing table {}", dbTableName);

		DatabaseChangeLog dbLog = new DatabaseChangeLog("path");

		// Remove table changeset
		ChangeSet changeSet = new ChangeSet(dbTableName + "_CT_R_" + System.currentTimeMillis(), "Meveo", false, false, "meveo", "", "", dbLog);
		changeSet.setFailOnError(false);

		// Make sure table exists before dropping it
		TableExistsPrecondition tableExistsPrecondition = new TableExistsPrecondition();
		tableExistsPrecondition.setTableName(dbTableName);

		PreconditionContainer precondition = new PreconditionContainer();
		precondition.setOnError(ErrorOption.HALT);
		precondition.setOnFail(FailOption.HALT);
		precondition.addNestedPrecondition(tableExistsPrecondition);

		changeSet.setPreconditions(precondition);

		DropTableChange dropTableChange = new DropTableChange();
		dropTableChange.setTableName(dbTableName);
		dropTableChange.setCascadeConstraints(true);

		changeSet.addChange(dropTableChange);

		dbLog.addChangeSet(changeSet);

		// Changeset for Postgress
		ChangeSet pgChangeSet = new ChangeSet(dbTableName + "_CT_CRP_" + System.currentTimeMillis(), "Meveo", false, false, "meveo", "", "postgresql", dbLog);
		pgChangeSet.setFailOnError(false);

		DropSequenceChange dropPgSequence = new DropSequenceChange();
		dropPgSequence.setSequenceName(dbTableName + "_seq");
		pgChangeSet.addChange(dropPgSequence);

		dbLog.addChangeSet(pgChangeSet);

		Session hibernateSession = sqlConnectionProvider.getSession(sqlConnectionCode);

		hibernateSession.doWork(connection -> {

			Database database;
			try {
				database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
				setSchemaName(database);
				Liquibase liquibase = new Liquibase(dbLog, new ClassLoaderResourceAccessor(), database);
				liquibase.update(new Contexts(), new LabelExpression());

			} catch (MigrationFailedException e) {
				if (!e.getMessage().toLowerCase().contains("precondition")) {
					throw new HibernateException(e);
				}
			} catch (Exception e) {
				log.error("Failed to drop a custom table {}", dbTableName, e);
				throw new SQLException(e);
			}
		});
	}

	public static String getFkConstraintName(String tableName, CustomFieldTemplate cft) {
		return String.format("fk_%s_%s", tableName, cft.getDbFieldname());
	}

	private void setDefaultValue(CustomFieldTemplate cft, AddColumnConfig column) {
		if (cft.getDefaultValue() != null) {
			switch (cft.getFieldType()) {
			case DOUBLE:
			case LONG:
			case ENTITY:
			case BOOLEAN:
				boolean value = Boolean.parseBoolean(cft.getDefaultValue());
				if (value) {
					column.setDefaultValueNumeric("1");
				} else {
					column.setDefaultValueNumeric("0");
				}
				break;
			case STRING:
			case TEXT_AREA:
			case LIST:
				column.setDefaultValue(cft.getDefaultValue());
				break;
			}
		}
	}

	private String getColumnType(CustomFieldTemplate cft) throws ClassNotFoundException {

		CustomFieldTypeEnum fieldType = cft.getFieldType();
		if (fieldType == CustomFieldTypeEnum.ENTITY) {
			CustomEntityTemplate referenceCet = customEntityTemplateService.findByCode(cft.getEntityClazzCetCode());
			if(referenceCet == null) {
				referenceCet = cache.getCustomEntityTemplate(cft.getEntityClazzCetCode());
			}
			if (referenceCet == null && !cft.getEntityClazz().startsWith(CustomEntityTemplate.class.getName())) {
				Class<?> jpaEntityClazz = Class.forName(cft.getEntityClazzCetCode());
				fieldType = CustomFieldTypeEnum.guessEnum(PersistenceUtils.getPKColumnType(jpaEntityClazz, PersistenceUtils.getPKColumnName(jpaEntityClazz)));
				
				// check the storage as well referenceJPA must be stored in SINGLE storage
				// TODO: Must support different storage types
				if (!cft.getStorageType().equals(CustomFieldStorageTypeEnum.SINGLE) && !cft.getEntityClazz().equals(Workflow.class.getName())) {
					throw new UnsupportedOperationException("JPA reference CFT must be stored in a SINGLE field storage type.");
				}
			}
		}

		return getColumnType(cft, fieldType);
	}

	private String getColumnType(CustomFieldTemplate cft, CustomFieldTypeEnum fieldType) throws ClassNotFoundException {

		// Serialize the field if it is a list, but not a list of entity references
		if (cft.getStorageType() == CustomFieldStorageTypeEnum.LIST) {
			return "text";
		}

		switch (fieldType) {
		case DATE:
			return "datetime";
		case DOUBLE:
			return "numeric(23, 12)";
		case LONG:
			return "bigint";
		case BINARY:
		case EXPRESSION:
		case MULTI_VALUE:
		case STRING:
		case TEXT_AREA:
		case ENTITY:
		case LIST:
			return "varchar(" + (cft.getMaxValue() == null ? CustomFieldTemplate.DEFAULT_MAX_LENGTH_STRING : cft.getMaxValue()) + ")";
		// Store serialized
		case CHILD_ENTITY:
		case EMBEDDED_ENTITY:
			return "text";
		case BOOLEAN:
			return "int";

		default:
			break;
		}

		return null;
	}

	/**
	 * Create table in all active and initialized Sql datasource.
	 * 
	 * @param dbTablename physical name of the table
	 */
	public void createTable(String dbTablename, boolean hasReferenceJpaEntity) {

		List<SqlConfiguration> sqlConfigs = sqlConfigurationService.listActiveAndInitialized();
		sqlConfigs.forEach(e -> {
			if (!hasReferenceJpaEntity || (hasReferenceJpaEntity && e.getCode().equals(SqlConfiguration.DEFAULT_SQL_CONNECTION))) {
				createTable(e.getCode(), dbTablename);
			}
		});
	}

	/**
	 * Adds a field in all active and initialized sql datasource.
	 * 
	 * @param dbTablename physical name of the table
	 * @param cft         the custom field template
	 */
	public void addField(String dbTablename, CustomFieldTemplate cft) {
		sqlConfigurationService.listActiveAndInitialized()
			.stream()
			.filter(e -> !cft.hasReferenceJpaEntity() || (cft.hasReferenceJpaEntity() && e.getCode().equals(SqlConfiguration.DEFAULT_SQL_CONNECTION)))
			.forEach(e -> addField(e.getCode(), dbTablename, cft));
	}

	/**
	 * Updates a field in all active and initialized sql datasource.
	 * 
	 * @param dbTablename physical name of the table
	 * @param cft         the custom field template
	 */
	public void updateField(String dbTablename, CustomFieldTemplate cft) {

		List<SqlConfiguration> sqlConfigs = sqlConfigurationService.listActiveAndInitialized();
		sqlConfigs.forEach(e -> {
			// non entity field
			if (!cft.hasReferenceJpaEntity() || (cft.hasReferenceJpaEntity() && e.getCode().equals(SqlConfiguration.DEFAULT_SQL_CONNECTION))) {
				updateField(e.getCode(), dbTablename, cft);
			}
		});
	}

	/**
	 * Removes a table in all active and initialized Sql datasource
	 * 
	 * @param dbTablename physical name of the table
	 */
	public void removeTable(String dbTablename) {

		List<SqlConfiguration> sqlConfigs = sqlConfigurationService.listActiveAndInitialized();
		sqlConfigs.forEach(e -> removeTable(e.getCode(), dbTablename));
	}

	/**
	 * Removes a field in all active and initialized Sql datasource
	 * 
	 * @param dbTablename physical name of the table
	 * @param cft         the custom field template
	 */
	public void removeField(String dbTablename, CustomFieldTemplate cft) {

		List<SqlConfiguration> sqlConfigs = sqlConfigurationService.listActiveAndInitialized();
		sqlConfigs.forEach(e -> {
			// non entity field
			if (!cft.hasReferenceJpaEntity() || (cft.hasReferenceJpaEntity() && e.getCode().equals(SqlConfiguration.DEFAULT_SQL_CONNECTION))) {
				removeField(e.getCode(), dbTablename, cft);
			}
		});

	}

	/**
	 * Adds uuid-ossp extension to the PostgreSQL database.
	 * 
	 * @param sqlConfigurationCode the sql configuration
	 */
	public void executePostgreSqlExtension(String sqlConfigurationCode) {

		String uuidExtension = "CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\"";

		Session hibernateSession = sqlConnectionProvider.getSession(sqlConfigurationCode);

		hibernateSession.doWork(connection -> {
			try (PreparedStatement ps = connection.prepareStatement(uuidExtension)) {
				ps.executeUpdate();
				if (!StringUtils.isBlank(sqlConfigurationCode)) {
					connection.commit();
				}
			}
		});

		hibernateSession.close();
	}
}