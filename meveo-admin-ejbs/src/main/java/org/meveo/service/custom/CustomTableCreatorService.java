package org.meveo.service.custom;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
import org.meveo.model.customEntities.CustomModelObject;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.persistence.sql.SQLStorageConfiguration;
import org.meveo.model.sql.SqlConfiguration;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.sql.SQLConnectionProvider;
import org.meveo.persistence.sql.SqlConfigurationService;
import org.meveo.service.admin.impl.ModuleInstallationContext;
import org.meveo.util.PersistenceUtils;
import org.slf4j.Logger;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.change.AddColumnConfig;
import liquibase.change.Change;
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
	public boolean createCrtTable(String sqlConnectionCode, CustomRelationshipTemplate crt) throws BusinessException {
		if (crt.getAvailableStorages() == null || !crt.getAvailableStorages().contains(DBStorageType.SQL)) {
			throw new BusinessException("CustomRelationshipTemplate " + crt.getCode() + " is not configured to be stored in a custom table");
		}

		String tableName = SQLStorageConfiguration.getDbTablename(crt);

		DatabaseChangeLog dbLog = new DatabaseChangeLog("path");

		List<Change> changeset = new ArrayList<>();
		
		// Source column
		ColumnConfig sourceColumn = new ColumnConfig();
		sourceColumn.setName(SQLStorageConfiguration.getSourceColumnName(crt));
		sourceColumn.setType("varchar(255)");

		// Target column
		ColumnConfig targetColumn = new ColumnConfig();
		targetColumn.setName(SQLStorageConfiguration.getTargetColumnName(crt));
		targetColumn.setType("varchar(255)");

		// UUID column
		ColumnConfig uuidColumn = new ColumnConfig();
		uuidColumn.setName(UUID);
		uuidColumn.setType("varchar(255)");
		uuidColumn.setDefaultValueComputed(new DatabaseFunction("uuid_generate_v4()"));

		// Table creation
		CreateTableChange createTableChange = new CreateTableChange();
		createTableChange.setTableName(tableName);
		createTableChange.addColumn(sourceColumn);
		createTableChange.addColumn(targetColumn);
		createTableChange.addColumn(uuidColumn);
		changeset.add(createTableChange);

		AtomicBoolean created = new AtomicBoolean();
		created.set(true);

		try (Session hibernateSession = sqlConnectionProvider.getSession(sqlConnectionCode)) {
			hibernateSession.doWork(connection -> {
				DatabaseMetaData meta = connection.getMetaData();
	
				Database database;
				try {
					database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
					setSchemaName(database);
					
					boolean sourceOrTargetColDidNotExist = false;

					// Check table does not exists
					try (var res = meta.getTables(null, database.getDefaultSchemaName(), tableName, new String[] { "TABLE" })) {
						if (res.next()) {
							changeset.remove(createTableChange);
							
							// if uuid field does not exists, add it
							try (ResultSet res2 = meta.getColumns(null, database.getDefaultSchemaName(), tableName, "uuid")) {
								if(!res2.next()) {
									AddColumnConfig addUuidCol = new AddColumnConfig();
									addUuidCol.setName(uuidColumn.getName());
									addUuidCol.setType(uuidColumn.getType());
									
									AddColumnChange pgUuidColChange = new AddColumnChange();
									pgUuidColChange.setTableName(tableName);
									pgUuidColChange.setColumns(List.of((addUuidCol)));
									changeset.add(pgUuidColChange);
								}
							}
							
							
							// if source field does not exists, add it
							try (ResultSet res2 = meta.getColumns(null, database.getDefaultSchemaName(), tableName, sourceColumn.getName())) {
								if(!res2.next()) {
									sourceOrTargetColDidNotExist = true;
									AddColumnConfig addSourceField = new AddColumnConfig();
									addSourceField.setName(sourceColumn.getName());
									addSourceField.setType(sourceColumn.getType());
									
									AddColumnChange addSourceFieldChange = new AddColumnChange();
									addSourceFieldChange.setTableName(tableName);
									addSourceFieldChange.setColumns(List.of((addSourceField)));
									changeset.add(addSourceFieldChange);
								}
							}
							
							// if target field does not exists, add it
							try (ResultSet res2 = meta.getColumns(null, database.getDefaultSchemaName(), tableName, targetColumn.getName())) {
								if(!res2.next()) {
									sourceOrTargetColDidNotExist = true;
									AddColumnConfig addTargetField = new AddColumnConfig();
									addTargetField.setName(targetColumn.getName());
									addTargetField.setType(targetColumn.getType());
									addTargetField.setDefaultValueComputed(targetColumn.getDefaultValueComputed());
									
									AddColumnChange addTargetFieldChange = new AddColumnChange();
									addTargetFieldChange.setTableName(tableName);
									addTargetFieldChange.setColumns(List.of((addTargetField)));
									changeset.add(addTargetFieldChange);
								}
							}
						}
					}
					
					if (sourceOrTargetColDidNotExist) {
						// Primary key constraint addition
						AddPrimaryKeyChange addPrimaryKeyChange = new AddPrimaryKeyChange();
						addPrimaryKeyChange.setColumnNames(uuidColumn.getName());
						addPrimaryKeyChange.setTableName(tableName);
						changeset.add(addPrimaryKeyChange);
					}
					
					// Unique constraint if CRT is unique
					if (crt.isUnique() && sourceOrTargetColDidNotExist) {
						AddUniqueConstraintChange uniqueConstraint = new AddUniqueConstraintChange();
						uniqueConstraint.setColumnNames(sourceColumn.getName() + ", " + targetColumn.getName());
						uniqueConstraint.setTableName(tableName);
						uniqueConstraint.setConstraintName("uk_" + tableName);
						changeset.add(uniqueConstraint);
					}

					// Source foreign key if source cet is a custom table
					if (sourceOrTargetColDidNotExist && crt.getStartNode().getSqlStorageConfiguration() != null && crt.getStartNode().getSqlStorageConfiguration().isStoreAsTable()) {
						AddForeignKeyConstraintChange sourceFkChange = new AddForeignKeyConstraintChange();
						sourceFkChange.setBaseColumnNames(sourceColumn.getName());
						sourceFkChange.setConstraintName(sourceColumn.getName() + "_fk");
						sourceFkChange.setReferencedColumnNames(UUID);
						sourceFkChange.setBaseTableName(tableName);
						sourceFkChange.setReferencedTableName(SQLStorageConfiguration.getDbTablename(crt.getStartNode()));
						changeset.add(sourceFkChange);
					}

					// Target foreign key if target cet is a custom table
					if (sourceOrTargetColDidNotExist && crt.getEndNode().getSqlStorageConfiguration() != null && crt.getEndNode().getSqlStorageConfiguration().isStoreAsTable()) {
						AddForeignKeyConstraintChange targetFkChange = new AddForeignKeyConstraintChange();
						targetFkChange.setConstraintName(targetColumn.getName() + "_fk");
						targetFkChange.setBaseColumnNames(targetColumn.getName());
						targetFkChange.setReferencedColumnNames(UUID);
						targetFkChange.setBaseTableName(tableName);
						targetFkChange.setReferencedTableName(SQLStorageConfiguration.getDbTablename(crt.getEndNode()));
						changeset.add(targetFkChange);
					}
					
					ChangeSet liquibaseChangeset = new ChangeSet(tableName + "_CT_CP_" + System.currentTimeMillis(), "Meveo", false, false, "meveo", "", "", dbLog);
					changeset.forEach(liquibaseChangeset::addChange);
					dbLog.addChangeSet(liquibaseChangeset);

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
		}

		return created.get();
	}
	
	public boolean createCrtTable(CustomRelationshipTemplate crt) throws BusinessException {
		return crt.getRepositories()
			.stream()
			.map(Repository::getSqlConfigurationCode)
			.map(code -> {
				try {
					return createCrtTable(code, crt);
				} catch (BusinessException e) {
					log.error("Failed to create table", e);
					return false;
				}
			})
			.reduce((old, newState) -> old && newState)
			.orElse(false);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void createTable(String sqlConnectionCode, CustomEntityTemplate template) {
		createTable(sqlConnectionCode, template, true);
	}
	
	public void addInheritance(String sqlCode, CustomEntityTemplate template) {
		var dbTableName = SQLStorageConfiguration.getDbTablename(template);
		var parentTableName = SQLStorageConfiguration.getDbTablename(template.getSuperTemplate());

		DatabaseChangeLog dbLog = new DatabaseChangeLog("path");
		var fkChange = new AddForeignKeyConstraintChange();
		fkChange.setBaseTableName(dbTableName);
		fkChange.setBaseColumnNames(UUID);
		fkChange.setReferencedTableName(parentTableName);
		fkChange.setReferencedColumnNames(UUID);
		fkChange.setConstraintName(getInheritanceFK(dbTableName, parentTableName));
		
		ChangeSet pgChangeSet = new ChangeSet(dbTableName + "_CT_CP_" + System.currentTimeMillis(), "Meveo", false, false, "meveo", "", "postgresql", dbLog);
		pgChangeSet.addChange(fkChange);
		dbLog.addChangeSet(pgChangeSet);
		
		try(Session hibernateSession = sqlConnectionProvider.getSession(sqlCode)) {
			hibernateSession.doWork(connection -> {
				Database database = getDatabase(connection);
				try {
					Liquibase liquibase = new Liquibase(dbLog, new ClassLoaderResourceAccessor(), database);
					liquibase.update(new Contexts(), new LabelExpression());
				} catch (Exception e) {
					log.error("Failed to add parent foreign key on table {} on SQL Configuration {}", dbTableName, sqlCode, e);
					throw new SQLException(e);
				}
			});
		}

	}

	public void removeInheritance(String sqlCode, CustomEntityTemplate template) {
		var dbTableName = SQLStorageConfiguration.getDbTablename(template);
		var parentTableName = SQLStorageConfiguration.getDbTablename(template.getSuperTemplate());

		DatabaseChangeLog dbLog = new DatabaseChangeLog("path");
		var fkChange = new DropForeignKeyConstraintChange();
		fkChange.setBaseTableName(dbTableName);
		fkChange.setConstraintName(getInheritanceFK(dbTableName, parentTableName));
		
		ChangeSet pgChangeSet = new ChangeSet(dbTableName + "_CT_CP_" + System.currentTimeMillis(), "Meveo", false, false, "meveo", "", "postgresql", dbLog);
		pgChangeSet.addChange(fkChange);
		dbLog.addChangeSet(pgChangeSet);
		
		try(Session hibernateSession = sqlConnectionProvider.getSession(sqlCode)) {
			hibernateSession.doWork(connection -> {
				Database database = getDatabase(connection);
				try {
					Liquibase liquibase = new Liquibase(dbLog, new ClassLoaderResourceAccessor(), database);
					liquibase.update(new Contexts(), new LabelExpression());
				} catch (Exception e) {
					log.error("Failed to remove parent foreign key on table {} on SQL Configuration {}", dbTableName, sqlCode, e);
					throw new SQLException(e);
				}
			});
		}
	}
	
	/**
	 * @param code Code of the CET or CRT
	 * @return true if the corresponding table already exist
	 */
	public boolean exists(String code) {
		List<SqlConfiguration> sqlConfigs = sqlConfigurationService.listActiveAndInitialized();
		for(var conf : sqlConfigs) {
			
			var dbTableName = SQLStorageConfiguration.getCetDbTablename(code);
			AtomicBoolean exists = new AtomicBoolean(false);
			
			try (Session hibernateSession = sqlConnectionProvider.getSession(conf.getCode())) {
				hibernateSession.doWork(connection -> {
					var meta = connection.getMetaData();
					
					try {
						Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
						setSchemaName(database);
						try (var res = meta.getTables(null, database.getDefaultSchemaName(), dbTableName, new String[] { "TABLE" })) {
							if (res.next()) {
								exists.set(true);
							}
						}
					} catch (DatabaseException e1) {
						// Database does not exists
						exists.set(false);
					}

				});
			}
			
			if(exists.get()) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Create a table with a single 'id' field. Value is autoincremented for mysql
	 * or taken from sequence for Postgress databases.
	 * 
	 * @param sqlConnectionCode Code of the {@link SqlConfiguration}
	 * @param template 			Template used to create the table
	 * @param createSequence 	Whether to create a sequence to generate ids
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void createTable(String sqlConnectionCode, CustomEntityTemplate template, boolean createSequence) {
		executePostgreSqlExtension(sqlConnectionCode);
		
		var dbTableName = SQLStorageConfiguration.getDbTablename(template);
		
		List<Change> pgChanges = new ArrayList<>();
		List<Change> msChanges = new ArrayList<>();
		
		DatabaseChangeLog dbLog = new DatabaseChangeLog("path");

		// Changeset for Postgress
		ChangeSet pgChangeSet = new ChangeSet(dbTableName + "_CT_CP_" + System.currentTimeMillis(), "Meveo", false, false, "meveo", "", "postgresql", dbLog);

		CreateTableChange createPgTableChange = new CreateTableChange();
		createPgTableChange.setTableName(dbTableName);

		AddColumnConfig pgUuidColumn = new AddColumnConfig();
		pgUuidColumn.setName(UUID);
		pgUuidColumn.setType("varchar(255)");
		pgUuidColumn.setDefaultValueComputed(new DatabaseFunction("uuid_generate_v4()"));

		// Primary key constraint
		ConstraintsConfig idConstraints = new ConstraintsConfig();
		idConstraints.setNullable(false);
		idConstraints.setPrimaryKey(true);
		idConstraints.setPrimaryKeyName(dbTableName + "PK");
		
		// If template has a parent template, uuid should have foreign key on parent table
		if(template.getSuperTemplate() != null && template.getSuperTemplate().getAvailableStorages().contains(DBStorageType.SQL)) {
			var parentTableName = SQLStorageConfiguration.getDbTablename(template.getSuperTemplate());
			idConstraints.setForeignKeyName(getInheritanceFK(dbTableName, parentTableName));
			idConstraints.setReferencedTableName(parentTableName);
			idConstraints.setReferencedColumnNames(UUID);
		}
		
		pgUuidColumn.setConstraints(idConstraints);
		createPgTableChange.addColumn(pgUuidColumn);
		pgChanges.add(createPgTableChange);
		
		// Statement generated by liquibase not suitable for postgres < 9.5
		SqlConfiguration sqlConf = sqlConfigurationService.findByCode(sqlConnectionCode);
		String schema = StringUtils.isBlank(sqlConf.getSchema()) ? "public" : sqlConf.getSchema();
		
		final RawSQLChange createPgSequence;
		if (createSequence) {
			createPgSequence = new RawSQLChange("CREATE SEQUENCE " + schema + "." + dbTableName + "_seq;");
			pgChanges.add(createPgSequence);
		} else {
			createPgSequence = null;
		}
		
		dbLog.addChangeSet(pgChangeSet);

		// Changeset for mysql
		ChangeSet mysqlChangeSet = new ChangeSet(dbTableName + "_CT_CM_" + System.currentTimeMillis(), "Meveo", false, false, "meveo", "", "mysql", dbLog);

		CreateTableChange createMsTableChange = new CreateTableChange();
		createMsTableChange.setTableName(dbTableName);

		AddColumnConfig msUuidcolumn = new AddColumnConfig();
		msUuidcolumn.setName(UUID);
		msUuidcolumn.setType("varchar(255)");
		msUuidcolumn.setDefaultValueComputed(new DatabaseFunction("uuid()"));

		msUuidcolumn.setConstraints(idConstraints);
		createMsTableChange.addColumn(msUuidcolumn);

		msChanges.add(createMsTableChange);
		dbLog.addChangeSet(mysqlChangeSet);

		try (Session hibernateSession = sqlConnectionProvider.getSession(sqlConnectionCode)) {

			hibernateSession.doWork(connection -> {
				DatabaseMetaData meta = connection.getMetaData();

				Database database;
				try {
					database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
					setSchemaName(database);

				} catch (DatabaseException e1) {
					log.error("Failed to retrieve database for connection {}", connection);
					throw new SQLException(e1);
				}

				// Check table does not exists
				try (var res = meta.getTables(null, database.getDefaultSchemaName(), dbTableName, new String[] { "TABLE" })) {
					if (res.next()) {
						pgChanges.remove(createPgTableChange);
						msChanges.remove(createMsTableChange);
						
						// if uuid field does not exists, add it
						try (ResultSet res2 = meta.getColumns(null, database.getDefaultSchemaName(), dbTableName, "uuid")) {
							if(!res2.next()) {
								AddColumnChange pgUuidColChange = new AddColumnChange();
								pgUuidColChange.setTableName(dbTableName);
								pgUuidColChange.setColumns(List.of((pgUuidColumn)));
								pgChanges.add(pgUuidColChange);
								
								AddColumnChange msUuidColChange = new AddColumnChange();
								msUuidColChange.setTableName(dbTableName);
								msUuidColChange.setColumns(List.of(msUuidcolumn));
								msChanges.add(msUuidColChange);
							}
						}
					}
				}

				// Check sequence does not exists
				try (var res = meta.getTables(null, database.getDefaultSchemaName(), dbTableName + "_seq", new String[] { "SEQUENCE" })) {
					if (res.next() && createPgSequence != null) {
						pgChanges.remove(createPgSequence);
					}
				}
				
				try {
					
					pgChanges.forEach(pgChangeSet::addChange);
					msChanges.forEach(mysqlChangeSet::addChange);

					Liquibase liquibase = new Liquibase(dbLog, new ClassLoaderResourceAccessor(), database);
					liquibase.update(new Contexts(), new LabelExpression());

				} catch (Exception e) {
					log.error("Failed to create a custom table {} on SQL Configuration {}", dbTableName, sqlConnectionCode, e);
					throw new SQLException(e);
				}

			});
		}
	}

	/**
	 * @param dbTableName
	 * @param parentTableName
	 * @return
	 */
	private String getInheritanceFK(String dbTableName, String parentTableName) {
		return "fk_" + dbTableName + "_" + parentTableName;
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
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void addField(String sqlConnectionCode, String dbTableName, CustomFieldTemplate cft, boolean checkStorage) {

		// Don't add field if not stored in sql
		if (checkStorage && !cft.getStoragesNullSafe().contains(DBStorageType.SQL)) {
			return;
		}

		String dbFieldname = cft.getDbFieldname();

		if ((cft.getFieldType() == CustomFieldTypeEnum.STRING || cft.getFieldType() == CustomFieldTypeEnum.SECRET) && (cft.getMaxValue() == null || cft.getMaxValue() < 1)) {
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
		
		AddColumnConfig column = new AddColumnConfig();

		// Check if column type is handled
		if (columnType != null) {

			AddColumnChange addColumnChange = new AddColumnChange();
			addColumnChange.setTableName(dbTableName);

			
			column.setName(dbFieldname);
			setDefaultValue(cft, column);
			column.setType(columnType);

			ConstraintsConfig constraints = new ConstraintsConfig();
			column.setConstraints(constraints);

			if (cft.isValueRequired()) {
				constraints.setNullable(false);
			}

			addColumnChange.setColumns(Collections.singletonList(column));
			changeSet.addChange(addColumnChange);
			
			
			if (cft.isUnique()) {
				createOrUpdateUniqueField(dbTableName, cft, changeSet);
			}
		}

		// Add a foreign key constraint pointing on referenced table if field is an
		// entity reference
		if (cft.getFieldType() == CustomFieldTypeEnum.ENTITY && cft.getStorageType().equals(CustomFieldStorageTypeEnum.SINGLE)) {
			addForeignKey(sqlConnectionCode, changeSet, cft, dbTableName, dbFieldname);
		}

		if (!changeSet.getChanges().isEmpty()) {

			dbLog.addChangeSet(changeSet);

			try (Session hibernateSession = sqlConnectionProvider.getSession(sqlConnectionCode)) {
				
				try {
					CompletableFuture.runAsync(() -> {
						hibernateSession.doWork(connection -> {
							Database database;
							try {
								database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
								setSchemaName(database);
								
								var meta = connection.getMetaData();
								
								// Check if field already exist in table
								try (ResultSet res = meta.getColumns(null, database.getDefaultSchemaName(), dbTableName, dbFieldname)) {
									if(res.next()) {
										checkTypeMatches(dbTableName, cft, column, res.getString("TYPE_NAME"), res.getInt("COLUMN_SIZE"));
										
									} else {
										// Create the field
										Liquibase liquibase = new Liquibase(dbLog, new ClassLoaderResourceAccessor(), database);
										liquibase.update(new Contexts(), new LabelExpression());
										liquibase.forceReleaseLocks();
									}
								}
								

							} catch (Exception e) {
								log.error("Failed to add field {} to custom table {}", dbFieldname, dbTableName, e);
								throw new SQLException(String.format("Failed to add field %s to custom table %s : ",dbFieldname, dbTableName) + e.getMessage(), e);
							}
						});
					}).get(1, TimeUnit.MINUTES);
					
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					log.error("Failed to add field {} to custom table {} within 1 minute",dbFieldname, dbTableName, e);
					throw new RuntimeException(e);
				}
			}
		}
	}

	/**
	 * @param dbTableName Name of the table
	 * @param cft		  Custom field template to match
	 * @param column      Computed column definition 
	 * @param typeName	  Actual type name of the column
	 * @param typeSize	  Actual size of the column
	 * @throws BusinessException if the computed type and the actual type doesn't match
	 */
	private void checkTypeMatches(String dbTableName, CustomFieldTemplate cft, AddColumnConfig column, String typeName, int typeSize) throws BusinessException {
		String type = typeName;
		switch (type) {
			case "int8" : 
				type = "bigint";
				break;
			case "int4" : 
				type = "int";
				break;
			case "timestamp" :
				type = "datetime";
				break;
			case "numeric":
			case "text" :
				// NOOP
				break;
			default : 
				if(typeSize != 0) {
					type += "(" + typeSize + ")";
				}
		}
		
		if (column.getType().toLowerCase().contains(type)) { 
			return;
		}
		
		// Field definition must match the existing field
		if(!column.getType().toLowerCase().equals(type)) {
			throw new BusinessException("Field defintion for " + cft + " (" + column.getType().toLowerCase() + ") does not match existing column in table " + dbTableName + " (" + type + ")");
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

		try (Session hibernateSession = sqlConnectionProvider.getSession(sqlConnectionCode)) {

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
	
			String type;
			try {
				type = getColumnType(cft);
			} catch (ClassNotFoundException e1) {
				throw new IllegalArgumentException("Cannot get field type for entity with class or code " + cft.getEntityClazzCetCode(), e1);
			}
			
			dropNotNullChange.setColumnDataType(type);

			changeSet.addChange(dropNotNullChange);
			dbLog.addChangeSet(changeSet);

			// Add not null constraint if needed
			if (cft.isValueRequired()) {
				changeSet = new ChangeSet(dbTableName + "_CT_" + dbFieldname + "_ANN_" + System.currentTimeMillis(), "Meveo", false, false, "meveo", "", "", dbLog);
				AddNotNullConstraintChange addNotNullChange = new AddNotNullConstraintChange();
				addNotNullChange.setTableName(dbTableName);
				addNotNullChange.setColumnName(dbFieldname);
				addNotNullChange.setColumnDataType(type);

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
				dropDefaultValueChange.setColumnDataType(type);

				changeSet.addChange(dropDefaultValueChange);
				dbLog.addChangeSet(changeSet);

				// Add default value if needed
				if (cft.getDefaultValue() != null) {
					changeSet = new ChangeSet(dbTableName + "_CT_" + dbFieldname + "_AD_" + System.currentTimeMillis(), "Meveo", false, false, "meveo", "", "", dbLog);
					AddDefaultValueChange addDefaultValueChange = new AddDefaultValueChange();

					addDefaultValueChange.setTableName(dbTableName);
					addDefaultValueChange.setColumnName(dbFieldname);
					addDefaultValueChange.setColumnDataType(type);
					
					if (cft.getFieldType() == CustomFieldTypeEnum.DOUBLE) {
						addDefaultValueChange.setDefaultValueNumeric(cft.getDefaultValue());
					} else if (cft.getFieldType() == CustomFieldTypeEnum.LONG) {
						addDefaultValueChange.setDefaultValueNumeric(cft.getDefaultValue());
					} else if (cft.getFieldType() == CustomFieldTypeEnum.SECRET || cft.getFieldType() == CustomFieldTypeEnum.STRING || cft.getFieldType() == CustomFieldTypeEnum.LIST) {
						addDefaultValueChange.setDefaultValue(cft.getDefaultValue());
					} else if (cft.getFieldType() == CustomFieldTypeEnum.BOOLEAN) {
						addDefaultValueChange.setColumnDataType("int");
						addDefaultValueChange.setDefaultValueNumeric("1".equals(cft.getDefaultValue()) || "true".equalsIgnoreCase(cft.getDefaultValue()) ? "1" : "0");
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
				modifyDataTypeChange.setNewDataType(type);

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
		}
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

		try (Session hibernateSession = sqlConnectionProvider.getSession(sqlConnectionCode)) {

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
		}
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

		try (Session hibernateSession = sqlConnectionProvider.getSession(sqlConnectionCode)) {

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
			case SECRET:
			case STRING:
			case TEXT_AREA:
			case LONG_TEXT:
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
				String pkColumnName = PersistenceUtils.getPKColumnName(jpaEntityClazz);
				String pkColumnType = PersistenceUtils.getPKColumnType(jpaEntityClazz, pkColumnName);
				fieldType = CustomFieldTypeEnum.guessEnum(pkColumnType);
				
				// check the storage as well referenceJPA must be stored in SINGLE storage
				// TODO: Must support different storage types
				if (!cft.getStorageType().equals(CustomFieldStorageTypeEnum.SINGLE)) {
					throw new UnsupportedOperationException("JPA reference CFT must be stored in a SINGLE field storage type.");
				}
			}
		}

		return getColumnType(cft, fieldType);
	}

	private String getColumnType(CustomFieldTemplate cft, CustomFieldTypeEnum fieldType) throws ClassNotFoundException {

		switch (cft.getStorageType()) {
			case LIST:
			case MATRIX:
			case MAP:
				return "text";
			default: 
				break;
		}

		switch (fieldType) {
		
		case DATE:
			return "datetime";
		case DOUBLE:
			return "numeric(23, 12)";
		case LONG:
			return "bigint";
			
		case SECRET:
		case BINARY:
		case EXPRESSION:
		case MULTI_VALUE:
		case ENTITY:
		case STRING:
			return "varchar(" + (cft.getMaxValue() == null ? CustomFieldTemplate.DEFAULT_MAX_LENGTH_STRING : cft.getMaxValue()) + ")";
			
		case LIST:
		case TEXT_AREA:
		case CHILD_ENTITY:
		case EMBEDDED_ENTITY:
		case LONG_TEXT:
			return "text";
			
		case BOOLEAN:
			return "int";


		default:
			break;
		}

		return null;
	}
	
	/**
	 * Try, if possible, to update a table to match a template description.
	 * 
	 * @param code code of the template 
	 * @param fields fields of the template
	 * @return true if the update succeeded, false if a problem occured
	 */
	public boolean tryUpdate(String code, Collection<CustomFieldTemplate> fields) {

		return false;
	}
	
	/**
	 * Create table in all active and initialized Sql datasource.
	 * 
	 * @param dbTablename physical name of the table
	 */
	public void createTable(CustomEntityTemplate template) {
		template.getRepositories().forEach(e -> {
			if (!template.hasReferenceJpaEntity() || (template.hasReferenceJpaEntity() && e.getSqlConfiguration().getCode().equals(SqlConfiguration.DEFAULT_SQL_CONNECTION))) {
				createTable(e.getSqlConfiguration().getCode(), template);
			}
		});
	}

	/**
	 * Adds a field in all active and initialized sql datasource.
	 * 
	 * @param dbTablename physical name of the table
	 * @param cft         the custom field template
	 */
	public void addField(CustomModelObject template, CustomFieldTemplate cft) {
        template.getRepositories().stream()
				.filter(e -> !cft.hasReferenceJpaEntity() || (cft.hasReferenceJpaEntity() && e.getSqlConfiguration().getCode().equals(SqlConfiguration.DEFAULT_SQL_CONNECTION)))
				.forEach(e -> addField(e.getSqlConfiguration().getCode(), SQLStorageConfiguration.getDbTablename(template), cft));
	}

	/**
	 * Updates a field in all active and initialized sql datasource.
	 * 
	 * @param dbTablename physical name of the table
	 * @param cft         the custom field template
	 */
	public void updateField(CustomModelObject template, CustomFieldTemplate cft) {
        template.getRepositories().forEach(e -> {
			// non entity field
			if (!cft.hasReferenceJpaEntity() || (cft.hasReferenceJpaEntity() && e.getSqlConfiguration().getCode().equals(SqlConfiguration.DEFAULT_SQL_CONNECTION))) {
				updateField(e.getSqlConfiguration().getCode(), SQLStorageConfiguration.getDbTablename(template), cft);
			}
		});
	}

	/**
	 * Removes a table in all active and initialized Sql datasource
	 * 
	 * @param dbTablename physical name of the table
	 */
	public void removeTable(CustomModelObject template) {
		List<SqlConfiguration> sqlConfigs = template.getRepositories()
				.stream()
				.map(Repository::getSqlConfiguration)
				.collect(Collectors.toList());
		sqlConfigs.forEach(e -> removeTable(e.getCode(), SQLStorageConfiguration.getDbTablename(template)));
	}

	/**
	 * Removes a field in all active and initialized Sql datasource
	 * 
	 * @param dbTablename physical name of the table
	 * @param cft         the custom field template
	 */
	public void removeField(CustomModelObject template, CustomFieldTemplate cft) {
		template.getRepositories()
			.stream()
			.map(Repository::getSqlConfiguration)
			.forEach(e -> {
			// non entity field
			if (!cft.hasReferenceJpaEntity() || (cft.hasReferenceJpaEntity() && e.getCode().equals(SqlConfiguration.DEFAULT_SQL_CONNECTION))) {
				removeField(e.getCode(), SQLStorageConfiguration.getDbTablename(template), cft);
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

		try (Session hibernateSession = sqlConnectionProvider.getSession(sqlConfigurationCode)) {

			hibernateSession.doWork(connection -> {
				if (!StringUtils.isBlank(sqlConfigurationCode)) {
					if (!sqlConnectionProvider.getSqlConfiguration(sqlConfigurationCode).isXAResource())
						connection.setAutoCommit(false);
				}
				try (PreparedStatement ps = connection.prepareStatement(uuidExtension)) {
					ps.executeUpdate();
					if (!StringUtils.isBlank(sqlConfigurationCode)) {
						if (!sqlConnectionProvider.getSqlConfiguration(sqlConfigurationCode).isXAResource())
							connection.commit();
					}
				} catch (Exception e) {
					if (!sqlConfigurationCode.equals(SqlConfiguration.DEFAULT_SQL_CONNECTION)) {
						if (!sqlConnectionProvider.getSqlConfiguration(sqlConfigurationCode).isXAResource())
							connection.rollback();
					}
					throw e;
				}				
			});
		}
	}
	
	private Database getDatabase(Connection connection) throws SQLException {
		Database database;
		try {
			database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
			setSchemaName(database);

		} catch (DatabaseException e1) {
			log.error("Failed to retrieve database for connection {}", connection);
			throw new SQLException(e1);
		}
		return database;
	}
	
	public boolean isTableExists(String sqlConfigurationCode, String schema, String tableName) {

		return (Boolean) getEntityManager(sqlConfigurationCode)
				.createNativeQuery("SELECT EXISTS(SELECT * FROM information_schema.tables WHERE table_schema = :tableSchema AND table_name = :tableName)")
				.setParameter("tableSchema", schema).setParameter("tableName", tableName).getSingleResult();
	}
}
