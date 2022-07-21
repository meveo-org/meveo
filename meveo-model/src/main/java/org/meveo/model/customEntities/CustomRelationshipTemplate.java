/*
 * (C) Copyright 2018-2020 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * This program is not suitable for any direct or indirect application in MILITARY industry
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.model.customEntities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.BusinessEntity;
import org.meveo.model.ExportIdentifier;
import org.meveo.model.ModuleItem;
import org.meveo.model.ModuleItemOrder;
import org.meveo.model.ObservableEntity;
import org.meveo.model.billing.RelationshipDirectionEnum;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.persistence.JsonTypes;
import org.meveo.model.persistence.sql.SQLStorageConfiguration;
import org.meveo.model.sql.SqlConfiguration;
import org.meveo.model.storage.Repository;

/**
 * The Class CustomRelationshipTemplate.
 *
 * @author Clément Bareth
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.9.0
 */
@Entity
@ModuleItem(value = "CustomRelationshipTemplate", path = "customRelationshipTemplates")
@ModuleItemOrder(11)
@ExportIdentifier({ "code"})
@Table(name = "CUST_CRT", uniqueConstraints = @UniqueConstraint(columnNames = {"code"}))
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {@org.hibernate.annotations.Parameter(name = "sequence_name", value = "CUST_CRT_SEQ")}
)
@NamedQueries({
        @NamedQuery(name = "CustomRelationshipTemplate.getCRTForCache", query = "SELECT crt from CustomRelationshipTemplate crt JOIN FETCH crt.availableStorages where crt.disabled=false  "),
        @NamedQuery(name = "CustomRelationshipTemplate.findByStartEndAndName", query = "SELECT crt from CustomRelationshipTemplate crt " +
                "WHERE crt.startNode.code = :startCode " +
                "AND crt.endNode.code = :endCode " +
                "AND crt.name = :name")
})
@ObservableEntity
public class CustomRelationshipTemplate extends BusinessEntity implements Comparable<CustomRelationshipTemplate>, CustomModelObject {

    private static final long serialVersionUID = 8281478284763353310L;

    /** The crt prefix. */
    public static String CRT_PREFIX = "CRT";

    @Column(name = "name", length = 100, nullable = false)
    @Size(max = 100)
    @NotNull
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "START_NODE_ID", nullable = false)
    private CustomEntityTemplate startNode;
    
    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "END_NODE_ID", nullable = false)
    private CustomEntityTemplate  endNode;
    
    @Enumerated(EnumType.STRING)
	@Column(name = "DIRECTION", length = 100)
    private RelationshipDirectionEnum direction = RelationshipDirectionEnum.OUTGOING;
    
//    @Column(name = "available_storages", columnDefinition = "TEXT")
//    @Type(type = JsonTypes.JSON_LIST)
	@ManyToMany
	@JoinTable(name = "crt_db_storage", inverseJoinColumns = @JoinColumn(name = "db_storage_code"), joinColumns = @JoinColumn(name = "crt_id"))
    private List<DBStorageType> availableStorages;

    /**
     * Json list type. ex : ["firstName","lastName","birthDate"]
     */
    @Column(name = "START_NODE_KEYS", length = 100)
    private String startNodeKeys; 

    /**
     * Json list type. ex : ["firstName","lastName","birthDate"]
     */
    @Column(name = "END_NODE_KEYS", length = 100)
    private String endNodeKeys;

    /**
     * Whether the relation is used for unicity of nodes
     */
    @Column(name = "is_unique")
    @Type(type="numeric_boolean")
    @ColumnDefault("0")
    private boolean unique;

    /**
     * Name of the field that will be added to the source entity to refer the most recent target entity
     */
    @Column(name = "source_name_singular")
    private String sourceNameSingular;

    /**
     * Name of the field that will be added to the source entity to refer every target entities
     */
    @Column(name = "source_name_plural")
    private String sourceNamePlural;

    /**
     * Name of the field that will be added to the target entity to refer the most recent source entity
     */
    @Column(name = "target_name_singular")
    private String targetNameSingular;

    /**
     * Name of the field that will be added to the target entity to refer every source entities
     */
    @Column(name = "target_name_plural")
    private String targetNamePlural;

    /**
     * Name of the graphql type corresponding to the relationship
     */
    @Column(name = "graphql_type")
    private String graphQlTypeName;
    
	@Type(type = "numeric_boolean")
	@Column(name = "audited")
	private boolean audited = false;
	
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "crt_storage_repository",
        joinColumns = @JoinColumn(name = "crt_id"),
        inverseJoinColumns = @JoinColumn(name = "repo_id")
    )
    private List<Repository> repositories = new ArrayList<>();
    
	
	/**
	 * @return the {@link #repositories}
	 */
	public List<Repository> getRepositories() {
		if (repositories == null) {
			this.repositories = new ArrayList<>();
		}
		return repositories;
	}

	/**
	 * @param repositories the repositories to set
	 */
	public void setRepositories(List<Repository> repositories) {
		this.repositories = repositories;
	}

    /**
	 * Name of the field that will be added to the target entity and that refer to the incoming relationships of this type.
	 *
	 * @return the relationships field target
	 */
    public String getRelationshipsFieldTarget() {
        if(targetNameSingular != null){
            return targetNameSingular + "Relations";
        }

        if(graphQlTypeName != null){
            return Character.toLowerCase(graphQlTypeName.charAt(0)) + graphQlTypeName.substring(1) + "s";
        }

        return null;
    }

    /**
	 * Gets the available storages.
	 *
	 * @return the available storages
	 */
    public List<DBStorageType> getAvailableStorages() {
		return availableStorages != null ? availableStorages : new ArrayList<>();
	}
    
    public void addStorage(DBStorageType e) {
    	availableStorages = getAvailableStorages();
    	availableStorages.add(e);
    }

	/**
	 * Sets the available storages.
	 *
	 * @param availableStorages the new available storages
	 */
	public void setAvailableStorages(List<DBStorageType> availableStorages) {
		this.availableStorages = availableStorages;
	}

	/**
	 * Name of the field that will be added to the source entity and that refer to the outgoing relationships of this type.
	 *
	 * @return the relationships field source
	 */
    public String getRelationshipsFieldSource() {
        if(sourceNameSingular != null){
            return sourceNameSingular + "Relations";
        }

        if(graphQlTypeName != null){
            return Character.toLowerCase(graphQlTypeName.charAt(0)) + graphQlTypeName.substring(1) + "s";
        }

        return null;
    }

    /**
	 * Gets the name of the graphql type corresponding to the relationship.
	 *
	 * @return the name of the graphql type corresponding to the relationship
	 */
    public String getGraphQlTypeName() {
        return graphQlTypeName;
    }

    /**
	 * Sets the name of the graphql type corresponding to the relationship.
	 *
	 * @param graphQlTypeName the new name of the graphql type corresponding to the relationship
	 */
    public void setGraphQlTypeName(String graphQlTypeName) {
        this.graphQlTypeName = graphQlTypeName;
    }

    /**
	 * Gets the name of the field that will be added to the target entity to refer the most recent source entity.
	 *
	 * @return the name of the field that will be added to the target entity to refer the most recent source entity
	 */
    public String getTargetNameSingular() {
        return targetNameSingular;
    }

    /**
	 * Sets the name of the field that will be added to the target entity to refer the most recent source entity.
	 *
	 * @param targetNameSingular the new name of the field that will be added to the target entity to refer the most recent source entity
	 */
    public void setTargetNameSingular(String targetNameSingular) {
        this.targetNameSingular = targetNameSingular;
    }

    /**
	 * Gets the name of the field that will be added to the target entity to refer every source entities.
	 *
	 * @return the name of the field that will be added to the target entity to refer every source entities
	 */
    public String getTargetNamePlural() {
        return targetNamePlural;
    }

    /**
	 * Sets the name of the field that will be added to the target entity to refer every source entities.
	 *
	 * @param targetNamePlural the new name of the field that will be added to the target entity to refer every source entities
	 */
    public void setTargetNamePlural(String targetNamePlural) {
        this.targetNamePlural = targetNamePlural;
    }

    /**
	 * Gets the name of the field that will be added to the source entity to refer the most recent target entity.
	 *
	 * @return the name of the field that will be added to the source entity to refer the most recent target entity
	 */
    public String getSourceNameSingular() {
        return sourceNameSingular;
    }

    /**
	 * Sets the name of the field that will be added to the source entity to refer the most recent target entity.
	 *
	 * @param sourceNameSingular the new name of the field that will be added to the source entity to refer the most recent target entity
	 */
    public void setSourceNameSingular(String sourceNameSingular) {
        this.sourceNameSingular = sourceNameSingular;
    }

    /**
	 * Gets the name of the field that will be added to the source entity to refer every target entities.
	 *
	 * @return the name of the field that will be added to the source entity to refer every target entities
	 */
    public String getSourceNamePlural() {
        return sourceNamePlural;
    }

    /**
	 * Sets the name of the field that will be added to the source entity to refer every target entities.
	 *
	 * @param sourceNamePlural the new name of the field that will be added to the source entity to refer every target entities
	 */
    public void setSourceNamePlural(String sourceNamePlural) {
        this.sourceNamePlural = sourceNamePlural;
    }

    /**
	 * Checks if is whether the relation is used for unicity of nodes.
	 *
	 * @return the whether the relation is used for unicity of nodes
	 */
    public boolean isUnique() {
        return unique;
    }

    /**
	 * Sets the whether the relation is used for unicity of nodes.
	 *
	 * @param unique the new whether the relation is used for unicity of nodes
	 */
    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    /**
	 * Gets the name.
	 *
	 * @return the name
	 */
    public String getName() {
        return name;
    }

    /**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
    public void setName(String name) {
        this.name = name;
    }

    /**
	 * Gets the start node.
	 *
	 * @return the start node
	 */
    public CustomEntityTemplate getStartNode() {
		return startNode;
	}

	/**
	 * Sets the start node.
	 *
	 * @param startNode the new start node
	 */
	public void setStartNode(CustomEntityTemplate startNode) {
		this.startNode = startNode;
	}

	/**
	 * Gets the end node.
	 *
	 * @return the end node
	 */
	public CustomEntityTemplate getEndNode() {
		return endNode;
	}

	/**
	 * Sets the end node.
	 *
	 * @param endNode the new end node
	 */
	public void setEndNode(CustomEntityTemplate endNode) {
		this.endNode = endNode;
	}

	/**
	 * Sets the end entity.
	 *
	 * @param endNode the new end entity
	 */
	public void setEndEntity(CustomEntityTemplate endNode) {
        this.endNode = endNode;
    }

    /**
	 * Sets the start entity.
	 *
	 * @param startEntity the new start entity
	 */
    public void setStartEntity(CustomEntityTemplate startEntity){
        this.startNode = startEntity;
    }

	/**
	 * Gets the direction.
	 *
	 * @return the direction
	 */
	public RelationshipDirectionEnum getDirection() {
		return direction;
	}

	/**
	 * Sets the direction.
	 *
	 * @param direction the new direction
	 */
	public void setDirection(RelationshipDirectionEnum direction) {
		if(direction!=null){
			this.direction = direction;
		}
		
	}

	/**
	 * Gets the json list type.
	 *
	 * @return the json list type
	 */
	public String getStartNodeKeys() {
		return startNodeKeys;
	}

	/**
	 * Sets the start node key.
	 *
	 * @param startNodeKeys the new start node key
	 */
	public void setStartNodeKey(String startNodeKeys) {
		this.startNodeKeys = startNodeKeys;
	}

	/**
	 * Gets the json list type.
	 *
	 * @return the json list type
	 */
	public String getEndNodeKeys() {
		return endNodeKeys;
	}

	/**
	 * Sets the end node key.
	 *
	 * @param endNodeKeys the new end node key
	 */
	public void setEndNodeKey(String endNodeKeys) {
		this.endNodeKeys = endNodeKeys;
	}

	@Override
    public String getAppliesTo() {
        return CRT_PREFIX + "_" + getCode();
    }
    
    /**
	 * Gets the applies to.
	 *
	 * @param code the code
	 * @return the applies to
	 */
    public static String getAppliesTo(String code) {
        return CRT_PREFIX + "_" + code;
    }

    /**
	 * Gets the permission resource name.
	 *
	 * @return the permission resource name
	 */
    public String getPermissionResourceName() {
        return CustomRelationshipTemplate.getPermissionResourceName(code);
    }

	/**
	 * Gets the read permission.
	 *
	 * @return the read permission
	 */
	public String getReadPermission() {
		return CustomRelationshipTemplate.getReadPermission(code);
	}

	/**
	 * Gets the modify permission.
	 *
	 * @return the modify permission
	 */
	public String getModifyPermission() {
		return CustomRelationshipTemplate.getModifyPermission(code);
	}

	/**
	 * Gets the decrypt permission.
	 *
	 * @return the decrypt permission
	 */
	public String getDecrpytPermission() {
		return CustomRelationshipTemplate.getDecryptPermission(code);
	}

    @Override
    public int compareTo(CustomRelationshipTemplate cet1) {
        return StringUtils.compare(name, cet1.getName());
    }

    /**
	 * Gets the permission resource name.
	 *
	 * @param code the code
	 * @return the permission resource name
	 */
    public static String getPermissionResourceName(String code) {
        return "CRT_" + code;
    }

	/**
	 * Gets the read permission.
	 *
	 * @param code the code
	 * @return the read permission
	 */
	public static String getReadPermission(String code) {
		return "CRT_" + code + "-read";
	}

	/**
	 * Gets the modify permission.
	 *
	 * @param code the code
	 * @return the modify permission
	 */
	public static String getModifyPermission(String code) {
		return "CRT_" + code + "-modify";
	}

	/**
	 * Gets the decrypt permission.
	 *
	 * @param code the code
	 * @return the decrypt permission
	 */
	public static String getDecryptPermission(String code) {
		return "CRT_" + code + "-decrypt";
	}

	@Override
	public String toString() {
		return "CustomRelationshipTemplate [code=" + code + "]";
	}
    
	/**
	 * Gets the code from applies to.
	 *
	 * @param appliesTo the applies to
	 * @return the code from applies to
	 */
	public static String getCodeFromAppliesTo(String appliesTo) {
		if (appliesTo == null)
			return null;

		if (!appliesTo.startsWith("CRT_")) {
			return null;
		}

		return appliesTo.substring(4);
	}

	public boolean isAudited() {
		return audited;
	}

	public void setAudited(boolean audited) {
		this.audited = audited;
	}

	@Override
	public String getDbTableName() {
		return SQLStorageConfiguration.getDbTablename(this);
	}

}