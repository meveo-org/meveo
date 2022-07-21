package org.meveo.model.storage;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.meveo.model.BusinessEntity;
import org.meveo.model.CustomFieldEntity;
import org.meveo.model.ICustomFieldEntity;
import org.meveo.model.crm.custom.CustomFieldValues;
import org.meveo.model.hierarchy.UserHierarchyLevel;
import org.meveo.model.neo4j.Neo4JConfiguration;
import org.meveo.model.persistence.CustomFieldValuesConverter;
import org.meveo.model.sql.SqlConfiguration;

/**
 * Storage for logical repository separation.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.12
 * @since 6.3.0
 */
@Entity
@Table(name = "storage_repository", uniqueConstraints = @UniqueConstraint(columnNames = { "code" }))
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
		@Parameter(name = "sequence_name", value = "storage_repository_seq"), })
@CustomFieldEntity(cftCodePrefix = "REPO")
public class Repository extends BusinessEntity implements ICustomFieldEntity {

	private static final long serialVersionUID = -93688572926121511L;

	public transient static final String DEFAULT_REPOSITORY = "default";
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private Repository parentRepository;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "binary_storage_configuration_id")
	private BinaryStorageConfiguration binaryStorageConfiguration;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "neo4j_configuration_id")
	private Neo4JConfiguration neo4jConfiguration;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "sql_configuration_id")
	private SqlConfiguration sqlConfiguration;

	@Enumerated(EnumType.STRING)
	@Column(name = "data_separation_type", length = 25)
	private DataSeparationTypeEnum dataSeparationType = DataSeparationTypeEnum.PHYSICAL;

	@NotNull
	@Column(name = "path", length = 255)
	private String path;    
	
	@ManyToOne
    @JoinColumn(name="user_hierarchy_level_id")
    private UserHierarchyLevel userHierarchyLevel;
	
    @Convert(converter = CustomFieldValuesConverter.class)
    @Column(name = "cf_values", columnDefinition = "text")
    private CustomFieldValues cfValues;

	public Repository getParentRepository() {
		return parentRepository;
	}

	public void setParentRepository(Repository parentRepository) {
		this.parentRepository = parentRepository;
	}

	public BinaryStorageConfiguration getBinaryStorageConfiguration() {
		return binaryStorageConfiguration;
	}

	public void setBinaryStorageConfiguration(BinaryStorageConfiguration binaryStorageConfiguration) {
		this.binaryStorageConfiguration = binaryStorageConfiguration;
	}

	public Neo4JConfiguration getNeo4jConfiguration() {
		return neo4jConfiguration;
	}

	public void setNeo4jConfiguration(Neo4JConfiguration neo4jConfiguration) {
		this.neo4jConfiguration = neo4jConfiguration;
	}

	public DataSeparationTypeEnum getDataSeparationType() {
		return dataSeparationType;
	}

	public void setDataSeparationType(DataSeparationTypeEnum dataSeparationType) {
		this.dataSeparationType = dataSeparationType;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public SqlConfiguration getSqlConfiguration() {
		return sqlConfiguration;
	}

	public void setSqlConfiguration(SqlConfiguration sqlConfiguration) {
		this.sqlConfiguration = sqlConfiguration;
	}

	public String getSqlConfigurationCode() {

		return sqlConfiguration == null ? SqlConfiguration.DEFAULT_SQL_CONNECTION : sqlConfiguration.getCode();
	}

	public UserHierarchyLevel getUserHierarchyLevel() {
		return userHierarchyLevel;
	}

	public void setUserHierarchyLevel(UserHierarchyLevel userHierarchyLevel) {
		this.userHierarchyLevel = userHierarchyLevel;
	}

	@Override
	public String getUuid() {
		return "REPOSITORY_" + this.code;
	}

	@Override
	public String clearUuid() {
		return getUuid();
	}

	@Override
	public ICustomFieldEntity[] getParentCFEntities() {
		return null;
	}

    @Override
    public CustomFieldValues getCfValues() {
        return cfValues;
    }

    public void setCfValues(CustomFieldValues cfValues) {
        this.cfValues = cfValues;
    }

    @Override
    public CustomFieldValues getCfValuesNullSafe() {
        if (cfValues == null) {
            cfValues = new CustomFieldValues();
        }
        return cfValues;
    }

    @Override
    public void clearCfValues() {
        cfValues = null;
    }

}