package org.meveo.api.dto.module;

import org.apache.commons.lang3.StringUtils;
import org.meveo.api.dto.*;
import org.meveo.api.dto.catalog.BusinessProductModelDto;
import org.meveo.api.dto.catalog.BusinessServiceModelDto;
import org.meveo.api.dto.dwh.BarChartDto;
import org.meveo.api.dto.dwh.LineChartDto;
import org.meveo.api.dto.dwh.MeasurableQuantityDto;
import org.meveo.api.dto.dwh.PieChartDto;
import org.meveo.api.dto.job.JobInstanceDto;
import org.meveo.api.dto.job.TimerEntityDto;
import org.meveo.api.dto.notification.EmailNotificationDto;
import org.meveo.api.dto.notification.JobTriggerDto;
import org.meveo.api.dto.notification.NotificationDto;
import org.meveo.api.dto.notification.WebHookDto;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.ModuleLicenseEnum;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class MeveoModuleDto.
 * 
 * @author andrius
 */
@XmlRootElement(name = "Module")
@XmlAccessorType(XmlAccessType.FIELD)
public class MeveoModuleDto extends BaseDataModelDto {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The license. */
    @XmlAttribute(required = true)
    private ModuleLicenseEnum license;

    /** The logo picture. */
    private String logoPicture;
    
    /** The logo picture file. */
    private byte[] logoPictureFile;

    /** The script. */
    private ScriptInstanceDto script;

    /** The module items. */
    @XmlElementWrapper(name = "moduleItems")
	@XmlElements({ @XmlElement(name = "customEntityTemplate", type = CustomEntityTemplateDto.class),
			@XmlElement(name = "customFieldTemplate", type = CustomFieldTemplateDto.class),
			@XmlElement(name = "filter", type = FilterDto.class),
			@XmlElement(name = "jobInstance", type = JobInstanceDto.class),
			@XmlElement(name = "script", type = ScriptInstanceDto.class),
			@XmlElement(name = "notification", type = NotificationDto.class),
			@XmlElement(name = "timerEntity", type = TimerEntityDto.class),
			@XmlElement(name = "emailNotif", type = EmailNotificationDto.class),
			@XmlElement(name = "jobTrigger", type = JobTriggerDto.class),
			@XmlElement(name = "webhookNotif", type = WebHookDto.class),
			@XmlElement(name = "businessServiceModel", type = BusinessServiceModelDto.class),
			@XmlElement(name = "businessProductModel", type = BusinessProductModelDto.class),
			@XmlElement(name = "subModule", type = MeveoModuleDto.class),
			@XmlElement(name = "measurableQuantity", type = MeasurableQuantityDto.class),
			@XmlElement(name = "pieChart", type = PieChartDto.class),
			@XmlElement(name = "lineChart", type = LineChartDto.class),
			@XmlElement(name = "barChart", type = BarChartDto.class),
            })
    private List<BaseDto> moduleItems;

    /**
     * Instantiates a new meveo module dto.
     */
    public MeveoModuleDto() {
    }

    /**
     * Instantiates a new meveo module dto.
     *
     * @param meveoModule the meveo module
     */
    public MeveoModuleDto(MeveoModule meveoModule) {
        super(meveoModule);
        this.license = meveoModule.getLicense();
        this.logoPicture = meveoModule.getLogoPicture();
        this.moduleItems = new ArrayList<BaseDto>();
        if (meveoModule.getScript() != null) {
            this.setScript(new ScriptInstanceDto(meveoModule.getScript()));
        }
    }

    /**
     * Gets the license.
     *
     * @return the license
     */
    public ModuleLicenseEnum getLicense() {
        return license;
    }

    /**
     * Sets the license.
     *
     * @param license the new license
     */
    public void setLicense(ModuleLicenseEnum license) {
        this.license = license;
    }

    /**
     * Gets the logo picture.
     *
     * @return the logo picture
     */
    public String getLogoPicture() {
        return logoPicture;
    }

    /**
     * Sets the logo picture.
     *
     * @param logoPicture the new logo picture
     */
    public void setLogoPicture(String logoPicture) {
        this.logoPicture = logoPicture;
    }

    /**
     * Gets the logo picture file.
     *
     * @return the logo picture file
     */
    public byte[] getLogoPictureFile() {
        return logoPictureFile;
    }

    /**
     * Sets the logo picture file.
     *
     * @param logoPictureFile the new logo picture file
     */
    public void setLogoPictureFile(byte[] logoPictureFile) {
        this.logoPictureFile = logoPictureFile;
    }

    /**
     * Gets the module items.
     *
     * @return the module items
     */
    public List<BaseDto> getModuleItems() {
        return moduleItems;
    }

    /**
     * Sets the module items.
     *
     * @param moduleItems the new module items
     */
    public void setModuleItems(List<BaseDto> moduleItems) {
        this.moduleItems = moduleItems;
    }

    /**
     * Adds the module item.
     *
     * @param item the item
     */
    public void addModuleItem(BaseDto item) {
        if (!moduleItems.contains(item)) {
            moduleItems.add(item);
        }
    }

    /* (non-Javadoc)
     * @see org.meveo.model.IEntity#isTransient()
     */
    @Override
    public boolean isTransient() {
        return true;
    }

    /**
     * Gets the script.
     *
     * @return the script
     */
    public ScriptInstanceDto getScript() {
        return script;
    }

    /**
     * Sets the script.
     *
     * @param script the new script
     */
    public void setScript(ScriptInstanceDto script) {
        this.script = script;
    }

    /**
     * Checks if is code only.
     *
     * @return true, if is code only
     */
    public boolean isCodeOnly() {
        return StringUtils.isBlank(getDescription()) && license == null && StringUtils.isBlank(logoPicture) && logoPictureFile == null && script == null
                && (moduleItems == null || moduleItems.isEmpty());
    }
    
    @Override
    public String toString() {
        final int maxLen = 10;
        return String.format("ModuleDto [code=%s, license=%s, description=%s, logoPicture=%s, logoPictureFile=%s, moduleItems=%s, script=%s]", getCode(), license, getDescription(),
            logoPicture, logoPictureFile, moduleItems != null ? moduleItems.subList(0, Math.min(moduleItems.size(), maxLen)) : null, script);
    }
}