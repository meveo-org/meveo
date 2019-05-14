package org.meveo.service.custom;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collections;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.hibernate.Session;
import org.meveo.admin.exception.BusinessException;
import org.meveo.jpa.EntityManagerProvider;
import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.persistence.sql.SQLStorageConfiguration;
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
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.statement.DatabaseFunction;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class CustomTableCreatorService implements Serializable {

    private static final long serialVersionUID = -5858023657669249422L;

    @PersistenceUnit(unitName = "MeveoAdmin")
    private EntityManagerFactory emf;

    @Inject
    @MeveoJpa
    private EntityManagerWrapper emWrapper;

    @Inject
    private EntityManagerProvider entityManagerProvider;

    @Inject
    private Logger log;

    @Inject
    private CustomEntityTemplateService customEntityTemplateService;

    /**
     * Create a table with a single 'id' field. Value is autoincremented for mysql or taken from sequence for Postgress databases.
     * 
     * @param dbTableName DB table name
     */
    public void createTable(String dbTableName) {

        DatabaseChangeLog dbLog = new DatabaseChangeLog("path");

        // Changeset for Postgress
        ChangeSet pgChangeSet = new ChangeSet(dbTableName + "_CT_CP_" + System.currentTimeMillis(), "Meveo", false, false, "meveo", "", "postgresql", dbLog);

        CreateTableChange createPgTableChange = new CreateTableChange();
        createPgTableChange.setTableName(dbTableName);

        ColumnConfig pgUuidColumn = new ColumnConfig();
        pgUuidColumn.setName("uuid");
        pgUuidColumn.setType("varchar(255)");
        pgUuidColumn.setDefaultValueComputed(new DatabaseFunction("uuid_generate_v4()"));

        ConstraintsConfig idConstraints = new ConstraintsConfig();
        idConstraints.setNullable(false);
        idConstraints.setPrimaryKey(true);
        idConstraints.setPrimaryKeyName(dbTableName + "PK");

        pgUuidColumn.setConstraints(idConstraints);
        createPgTableChange.addColumn(pgUuidColumn);

        pgChangeSet.addChange(createPgTableChange);
        dbLog.addChangeSet(pgChangeSet);

        // Changeset for mysql
        ChangeSet mysqlChangeSet = new ChangeSet(dbTableName + "_CT_CM_" + System.currentTimeMillis(), "Meveo", false, false, "meveo", "", "mysql", dbLog);

        CreateTableChange createMsTableChange = new CreateTableChange();
        createMsTableChange.setTableName(dbTableName);

        ColumnConfig msUuidcolumn = new ColumnConfig();
        msUuidcolumn.setName("uuid");
        msUuidcolumn.setType("varchar(255)");
        msUuidcolumn.setDefaultValueComputed(new DatabaseFunction("uuid()"));

        msUuidcolumn.setConstraints(idConstraints);
        createMsTableChange.addColumn(msUuidcolumn);

        mysqlChangeSet.addChange(createMsTableChange);
        dbLog.addChangeSet(mysqlChangeSet);

        EntityManager em = entityManagerProvider.getEntityManagerWoutJoinedTransactions();

        Session hibernateSession = em.unwrap(Session.class);

        hibernateSession.doWork(connection -> {

            Database database;
            try {
                database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

                Liquibase liquibase = new Liquibase(dbLog, new ClassLoaderResourceAccessor(), database);
                liquibase.update(new Contexts(), new LabelExpression());

            } catch (Exception e) {
                log.error("Failed to create a custom table {}", dbTableName, e);
                throw new SQLException(e);
            }

        });
    }

    /**
     * Add a field to a db table. Creates a liquibase changeset to add a field to a table and executes it
     * 
     * @param dbTableName DB Table name
     * @param cft Field definition
     */
    public void addField(String dbTableName, CustomFieldTemplate cft) throws BusinessException {

        String dbFieldname = cft.getDbFieldname();

        DatabaseChangeLog dbLog = new DatabaseChangeLog("path");

        ChangeSet changeSet = new ChangeSet(dbTableName + "_CT_" + dbFieldname + "_AF_" + System.currentTimeMillis(), "Meveo", false, false, "meveo", "", "", dbLog);

        String columnType = getColumnType(cft);

        // Check if column type is handled
        if(columnType != null) {

            AddColumnChange addColumnChange = new AddColumnChange();
            addColumnChange.setTableName(dbTableName);
            
            AddColumnConfig column = new AddColumnConfig();
            column.setName(dbFieldname);
            setDefaultValue(cft, column);
            column.setType(columnType);

            if (cft.isValueRequired()) {
                ConstraintsConfig constraints = new ConstraintsConfig();
                constraints.setNullable(false);
                column.setConstraints(constraints);
            }

            addColumnChange.setColumns(Collections.singletonList(column));
            changeSet.addChange(addColumnChange);

        }

        // Add a foreign key constraint pointing on referenced table if field is an entity reference
        if(cft.getFieldType() == CustomFieldTypeEnum.ENTITY){
            // Abort if referenced cet is not stored as table
            final CustomEntityTemplate referenceCet = customEntityTemplateService.findByCode(cft.getEntityClazzCetCode());
            if(referenceCet.getSqlStorageConfiguration() == null || !referenceCet.getSqlStorageConfiguration().isStoreAsTable()){
                String message = String.format("Referenced entity %s is not configured to be stored in a table, it therefore cannot be stored in %s table", referenceCet.getCode(), dbFieldname);
                throw new BusinessException(message);
            }

            AddForeignKeyConstraintChange foreignKeyConstraint = new AddForeignKeyConstraintChange();
            foreignKeyConstraint.setBaseColumnNames(dbFieldname);
            foreignKeyConstraint.setBaseTableName(dbTableName);
            foreignKeyConstraint.setReferencedColumnNames("uuid");
            foreignKeyConstraint.setReferencedTableName(SQLStorageConfiguration.getDbTablename(referenceCet));
            foreignKeyConstraint.setConstraintName(getFkConstraintName(dbTableName, cft));
            
            

            changeSet.addChange(foreignKeyConstraint);
        }

        if(!changeSet.getChanges().isEmpty()){
            dbLog.addChangeSet(changeSet);

            EntityManager em = entityManagerProvider.getEntityManagerWoutJoinedTransactions();

            Session hibernateSession = em.unwrap(Session.class);

            hibernateSession.doWork(connection -> {
                Database database;
                try {
                    database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

                    Liquibase liquibase = new Liquibase(dbLog, new ClassLoaderResourceAccessor(), database);
                    liquibase.update(new Contexts(), new LabelExpression());

                } catch (Exception e) {
                    log.error("Failed to add a field {} to a custom table {}", dbTableName, dbFieldname, e);
                    throw new SQLException(e);
                }
            });
        }
    }

    /**
     * Update a field of a db table. Creates a liquibase changeset to add a field to a table and executes it
     * 
     * @param dbTableName DB Table name
     * @param cft Field definition
     */
    public void updateField(String dbTableName, CustomFieldTemplate cft) {

        String dbFieldname = cft.getDbFieldname();

        DatabaseChangeLog dbLog = new DatabaseChangeLog("path");

        // Drop not null constraint and add again if needed - a better way would be to check if valueRequired field value was changed
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

        // Drop default value and add it again if needed - a better way would be to check if defaultValue field value was changed
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

        EntityManager em = entityManagerProvider.getEntityManagerWoutJoinedTransactions();

        Session hibernateSession = em.unwrap(Session.class);

        hibernateSession.doWork(connection -> {

            Database database;
            try {
                database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

                Liquibase liquibase = new Liquibase(dbLog, new ClassLoaderResourceAccessor(), database);
                liquibase.update(new Contexts(), new LabelExpression());

            } catch (Exception e) {
                log.error("Failed to update a field {} in a custom table {}", dbTableName, dbFieldname, e);
                throw new SQLException(e);
            }
        });
    }

    /**
     * Remove a field from a table
     * 
     * @param dbTableName Db table name to remove from
     * @param cft Field definition
     */
    public void removeField(String dbTableName, CustomFieldTemplate cft) {

        String dbFieldname = cft.getDbFieldname();

        DatabaseChangeLog dbLog = new DatabaseChangeLog("path");

        // Remove field
        ChangeSet changeSet = new ChangeSet(dbTableName + "_CT_" + dbFieldname + "_RF_" + System.currentTimeMillis(), "Meveo", false, false, "meveo", "", "", dbLog);
        changeSet.setFailOnError(false);

        DropColumnChange dropColumnChange = new DropColumnChange();
        dropColumnChange.setTableName(dbTableName);
        dropColumnChange.setColumnName(dbFieldname);

        // If cft is an entity reference, delete the foreign key constraint first
        if(cft.getFieldType() == CustomFieldTypeEnum.ENTITY){
            DropForeignKeyConstraintChange dropForeignKeyConstraint = new DropForeignKeyConstraintChange();
            dropForeignKeyConstraint.setBaseTableName(dbTableName);
            dropForeignKeyConstraint.setConstraintName(getFkConstraintName(dbTableName, cft));
            changeSet.addChange(dropForeignKeyConstraint);
        }

        changeSet.addChange(dropColumnChange);
        dbLog.addChangeSet(changeSet);

        EntityManager em = entityManagerProvider.getEntityManagerWoutJoinedTransactions();

        Session hibernateSession = em.unwrap(Session.class);

        hibernateSession.doWork(connection -> {

            Database database;
            try {
                database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

                Liquibase liquibase = new Liquibase(dbLog, new ClassLoaderResourceAccessor(), database);
                liquibase.update(new Contexts(), new LabelExpression());

            } catch (Exception e) {
                log.error("Failed to remove a field {} to a custom table {}", dbTableName, dbFieldname, e);
                throw new SQLException(e);
            }
        });
    }

    /**
     * Remove a table from DB
     * 
     * @param dbTableName Db table name to remove from
     */
    public void removeTable(String dbTableName) {

        DatabaseChangeLog dbLog = new DatabaseChangeLog("path");

        // Remove table changeset
        ChangeSet changeSet = new ChangeSet(dbTableName + "_CT_R_" + System.currentTimeMillis(), "Meveo", false, false, "meveo", "", "", dbLog);
        changeSet.setFailOnError(false);

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

        EntityManager em = entityManagerProvider.getEntityManagerWoutJoinedTransactions();

        Session hibernateSession = em.unwrap(Session.class);

        hibernateSession.doWork(connection -> {

            Database database;
            try {
                database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

                Liquibase liquibase = new Liquibase(dbLog, new ClassLoaderResourceAccessor(), database);
                liquibase.update(new Contexts(), new LabelExpression());

            } catch (Exception e) {
                log.error("Failed to drop a custom table {}", dbTableName, e);
                throw new SQLException(e);
            }
        });
    }

    private String getFkConstraintName(String tableName, CustomFieldTemplate cft){
        return String.format("fk_%s_%s", tableName, cft.getDbFieldname());
    }

    private void setDefaultValue(CustomFieldTemplate cft, AddColumnConfig column) {
        if (cft.getDefaultValue() != null) {
            switch (cft.getFieldType()) {
                case DOUBLE:
                case LONG:
                case ENTITY:
                case BOOLEAN:
                    column.setDefaultValueNumeric(cft.getDefaultValue());
                    break;
                case STRING:
                case TEXT_AREA:
                case LIST:
                    column.setDefaultValue(cft.getDefaultValue());
                    break;
            }
        }
    }

    private String getColumnType(CustomFieldTemplate cft) {
        // Serialize the field if it is a list, but not a list of entity references
        if (cft.getStorageType() == CustomFieldStorageTypeEnum.LIST) {
            if (cft.getFieldType() != CustomFieldTypeEnum.ENTITY) {
                return "text";
            }
        }

        switch (cft.getFieldType()) {
            case DATE:
                return "datetime";
            case DOUBLE:
                return "numeric(23, 12)";
            case LONG:
                return "bigint";
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
        }

        return null;
    }
}