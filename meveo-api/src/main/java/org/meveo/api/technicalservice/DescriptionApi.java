/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. This program is
 * not suitable for any direct or indirect application in MILITARY industry See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.api.technicalservice;

import org.meveo.api.dto.ProcessEntityDescription;
import org.meveo.api.dto.technicalservice.*;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.technicalservice.*;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomRelationshipTemplateService;
import org.meveo.service.technicalservice.DescriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author clement.bareth
 * @since 04.02.2019
 */
public class DescriptionApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(DescriptionApi.class);

    @Inject
    private CustomEntityTemplateService customEntityTemplateService;

    @Inject
    private CustomRelationshipTemplateService customRelationshipTemplateService;

    @Inject
    private CustomFieldTemplateService customFieldTemplateService;

    @Inject
    private DescriptionService descriptionService;

    /**
     * Extract the list of descriptions dto from a technical service
     *
     * @param technicalService Service from where to get the descriptions
     * @return The list of descriptions dtos
     */
    public List<InputOutputDescription> fromDescriptions(TechnicalService technicalService) {
        return InputOutputDescription.fromDescriptions(technicalService.getDescriptions());
    }

    /**
     * Convert a JPA service input definition to a DTO
     *
     * @param inputMeveoProperty Input property to convert
     * @return DTO representation of the input
     */
    public InputPropertyDto toInputPropertyDto(InputMeveoProperty inputMeveoProperty){
        InputPropertyDto inputPropertyDto = new InputPropertyDto();
        inputPropertyDto.setComparator(inputMeveoProperty.getComparator());
        inputPropertyDto.setComparisonValue(inputMeveoProperty.getComparisonValue());
        inputPropertyDto.setDefaultValue(inputMeveoProperty.getDefaultValue());
        inputPropertyDto.setRequired(inputMeveoProperty.isRequired());
        inputPropertyDto.setDescriptionName(inputMeveoProperty.getDescription().getName());
        inputPropertyDto.setProperty(inputMeveoProperty.getCet().getCode());
        return inputPropertyDto;
    }

    /**
     * Retrieve the input property corresponding to the dto
     *
     * @param serviceCode Service that is described by the property
     * @param propertyDto DTO object modelizing the property
     * @return The JPA input property
     */
    public InputMeveoProperty fromInputPropertyDto(String serviceCode, InputPropertyDto propertyDto){
        return descriptionService.find(serviceCode, propertyDto.getDescriptionName(), propertyDto.getProperty());
    }

    /**
     * Convert a description dto to a JPA description of technical service
     *
     * @param technicalService Technical service descibred by the description
     * @param dto DTO Object modelizing the description
     * @return The JPA description of the technical service
     * @throws EntityDoesNotExistsException if the CET associated to the dto is not found
     */
    public Description toDescription(TechnicalService technicalService, InputOutputDescription dto) throws EntityDoesNotExistsException {
        Description description;
        String code;
        String appliesTo;
        if (dto instanceof ProcessEntityDescription) {
            description = new MeveoEntityDescription();
            ((MeveoEntityDescription) description).setName(dto.getName());
            final CustomEntityTemplate customEntityTemplate = customEntityTemplateService.findByCode(dto.getType());
            if (customEntityTemplate == null) {
                throw new EntityDoesNotExistsException(CustomEntityTemplate.class, dto.getType());
            }
            ((MeveoEntityDescription) description).setType(customEntityTemplate);
            code = customEntityTemplate.getCode();
            appliesTo = customEntityTemplate.getAppliesTo();
        } else {
            description = new RelationDescription();
            ((RelationDescription) description).setSource(((ProcessRelationDescription) dto).getSource());
            ((RelationDescription) description).setTarget(((ProcessRelationDescription) dto).getTarget());
            final CustomRelationshipTemplate customRelationshipTemplate = customRelationshipTemplateService.findByCode(dto.getType());
            if (customRelationshipTemplate == null) {
                throw new EntityDoesNotExistsException(CustomRelationshipTemplate.class, dto.getType());
            }
            ((RelationDescription) description).setType(customRelationshipTemplate);
            code = customRelationshipTemplate.getCode();
            appliesTo = customRelationshipTemplate.getAppliesTo();
        }
        description.setService(technicalService);
        Map<String, CustomFieldTemplate> customFields = customFieldTemplateService.findByAppliesTo(appliesTo);
        description.setInput(dto.isInput());
        description.setOutput(dto.isOutput());
        final List<InputMeveoProperty> inputProperties = new ArrayList<>();
        final List<OutputMeveoProperty> outputProperties = new ArrayList<>();
        for (InputPropertyDto p : dto.getInputProperties()) {
            InputMeveoProperty inputProperty = new InputMeveoProperty();
            CustomFieldTemplate property = customFields.get(p.getProperty());
            if (property == null) {
                LOGGER.error("No custom field template for property {} of custom template {}", p.getProperty(), code);
                throw new EntityDoesNotExistsException(CustomRelationshipTemplate.class, p.getProperty());
            }
            inputProperty.setDescription(description);
            inputProperty.setProperty(property);
            inputProperty.setComparator(p.getComparator());
            inputProperty.setComparisonValue(p.getComparisonValue());
            inputProperty.setDefaultValue(p.getDefaultValue());
            inputProperty.setRequired(p.isRequired());
            inputProperties.add(inputProperty);
        }
        description.setInputProperties(inputProperties);
        for (OutputPropertyDto p : dto.getOutputProperties()) {
            OutputMeveoProperty outputProperty = new OutputMeveoProperty();
            CustomFieldTemplate property = customFields.get(p.getProperty());
            if (property == null) {
                throw new EntityDoesNotExistsException(CustomRelationshipTemplate.class, p.getProperty());
            }
            outputProperty.setProperty(property);
            outputProperty.setDescription(description);
            outputProperty.setTrustness(p.getTrustness());
            outputProperties.add(outputProperty);
        }
        description.setOutputProperties(outputProperties);
        return description;
    }

    /**
     * Convert the descriptions of a techincal service dto to JPA descriptions
     *
     * @param service JPA technical service to attach the descriptions
     * @param postData DTO technical service from where to retrieve the descriptions
     * @return the list of descriptions created
     * @throws EntityDoesNotExistsException if a CET associated to one of a description dto is not found
     */
    public List<Description> fromDescriptionsDto(TechnicalService service, TechnicalServiceDto postData) throws EntityDoesNotExistsException {
        List<Description> descriptions = new ArrayList<>();
        for (InputOutputDescription descDto : postData.getDescriptions()) {
            descriptions.add(toDescription(service, descDto));
        }
        return descriptions;
    }

    /**
     * Convert the descriptions of a techincal service dto to JPA descriptions
     *
     * @param service JPA technical service to attach the descriptions
     * @param dtos DTOs of descriptions
     * @return the list of descriptions created
     * @throws EntityDoesNotExistsException if a CET associated to one of a description dto is not found
     */
    public List<Description> fromDescriptionsDto(TechnicalService service, List<InputOutputDescription> dtos) throws EntityDoesNotExistsException {
        List<Description> descriptions = new ArrayList<>();
        for (InputOutputDescription descDto : dtos) {
            descriptions.add(toDescription(service, descDto));
        }
        return descriptions;
    }
}
