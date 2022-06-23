package org.meveo.model.module;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.*;
import org.meveo.model.BaseEntity;
import org.meveo.model.ObservableEntity;
import org.meveo.model.scripts.ScriptInstance;

/**
 * Meveo module has CETs, CFTs, CEIs, filters, scripts, jobs, notifications, endpoints
 *
 * @author Phu Bach(phu.bach@manaty.net)
 * @lastModifiedVersion 6.3.0
 */
@Entity
@ObservableEntity
@Cacheable
@Table(name = "meveo_module_release")
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {@Parameter(name = "sequence_name", value = "module_release_seq"), })
public class ModuleRelease extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @OneToMany(mappedBy = "moduleRelease", cascade = { CascadeType.ALL }, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ModuleReleaseItem> moduleItems = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "module_release_files", joinColumns = { @JoinColumn(name = "module_release_id") })
    @Column(name = "module_release_file")
    private Set<String> moduleFiles = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "module_license", nullable = false)
    @NotNull
    private ModuleLicenseEnum license = ModuleLicenseEnum.GPL;

    @Column(name = "logo_picture")
    @Size(max = 255)
    private String logoPicture;

    @Type(type="numeric_boolean")
    @Column(name = "installed")
    private boolean installed;

    @Column(name = "module_source", nullable = false, columnDefinition = "TEXT")
    private String moduleSource;

    @Column(name = "current_version")
    @Pattern(regexp = MeveoModule.VERSION_PATTERN)
    private String currentVersion;

    @Column(name = "meveo_version_base")
    @Pattern(regexp = "^(?<major>\\d+\\.)?(?<minor>\\d+\\.)?(?<patch>\\*|\\d+)$")
    private String meveoVersionBase;

    @Column(name = "meveo_version_ceiling")
    @Pattern(regexp = "^(?<major>\\d+\\.)?(?<minor>\\d+\\.)?(?<patch>\\*|\\d+)$")
    private String meveoVersionCeiling;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "script_instance_id")
    private ScriptInstance script;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meveo_module_id", nullable = false, updatable = false)
    @NotNull
    private MeveoModule meveoModule;

    @Column(name = "code", nullable = false, length = 255)
    @Size(max = 255, min = 1)
    protected String code;
    
    @Column(name = "description", nullable = true, length = 255)
    @Size(max = 255)
    protected String description;

    @Type(type="numeric_boolean")
    @Column(name = "is_in_draft")
    private boolean isInDraft = true;

    @OneToMany(mappedBy = "meveoModule", cascade = { CascadeType.ALL }, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<MeveoModuleDependency> moduleDependencies = new ArrayList<>();

    public List<ModuleReleaseItem> getModuleItems() {
        return moduleItems;
    }

    public void setModuleItems(List<ModuleReleaseItem> moduleItems) {
        this.moduleItems = moduleItems;
    }

    public Set<String> getModuleFiles() {
        return moduleFiles;
    }

    public void setModuleFiles(Set<String> moduleFiles) {
        this.moduleFiles = moduleFiles;
    }

    public ModuleLicenseEnum getLicense() {
        return license;
    }

    public void setLicense(ModuleLicenseEnum license) {
        this.license = license;
    }

    public String getLogoPicture() {
        return logoPicture;
    }

    public void setLogoPicture(String logoPicture) {
        this.logoPicture = logoPicture;
    }

    public boolean isInstalled() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public void setScript(ScriptInstance script) {
        this.script = script;
    }

    public ScriptInstance getScript() {
        return script;
    }

    public void setModuleSource(String moduleSource) {
        this.moduleSource = moduleSource;
    }

    public String getModuleSource() {
        return moduleSource;
    }

    public MeveoModule getMeveoModule() {
        return meveoModule;
    }

    public void setMeveoModule(MeveoModule meveoModule) {
        this.meveoModule = meveoModule;
    }

    public boolean isDownloaded() {
        return !StringUtils.isBlank(moduleSource);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMeveoVersionBase() {
        return meveoVersionBase;
    }

    public void setMeveoVersionBase(String meveoVersionBase) {
        this.meveoVersionBase = meveoVersionBase;
    }

    public String getMeveoVersionCeiling() {
        return meveoVersionCeiling;
    }

    public void setMeveoVersionCeiling(String meveoVersionCeiling) {
        this.meveoVersionCeiling = meveoVersionCeiling;
    }

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

    public List<MeveoModuleDependency> getModuleDependencies() {
        return moduleDependencies;
    }

    public void setModuleDependencies(List<MeveoModuleDependency> moduleDependencies) {
        this.moduleDependencies = moduleDependencies;
    }

    public boolean isInDraft() {
        return isInDraft;
    }

    public void setInDraft(boolean inDraft) {
        isInDraft = inDraft;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((currentVersion == null) ? 0 : currentVersion.hashCode());
		result = prime * result + ((meveoModule == null) ? 0 : meveoModule.hashCode());
		result = prime * result + ((meveoVersionBase == null) ? 0 : meveoVersionBase.hashCode());
		result = prime * result + ((meveoVersionCeiling == null) ? 0 : meveoVersionCeiling.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		ModuleRelease other = (ModuleRelease) obj;
		if (other == null) {
			return false;
		}

		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (currentVersion == null) {
			if (other.currentVersion != null)
				return false;
		} else if (!currentVersion.equals(other.currentVersion))
			return false;
		if (meveoModule == null) {
			if (other.meveoModule != null)
				return false;
		} else if (!meveoModule.equals(other.meveoModule))
			return false;
		if (meveoVersionBase == null) {
			if (other.meveoVersionBase != null)
				return false;
		} else if (!meveoVersionBase.equals(other.meveoVersionBase))
			return false;
		if (meveoVersionCeiling == null) {
			if (other.meveoVersionCeiling != null)
				return false;
		} else if (!meveoVersionCeiling.equals(other.meveoVersionCeiling))
			return false;
		return true;
	}
}