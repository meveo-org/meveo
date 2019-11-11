package org.meveo.model.customEntities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.meveo.api.dto.CustomEntityTemplateDto;
import org.meveo.api.dto.CustomFieldTemplateDto;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONSchemaIntoTemplateParser {

    private Map<String, Object> jsonMap;

    @SuppressWarnings("unchecked")
    public CustomEntityTemplateDto parseJson(String file) {
        CustomEntityTemplateDto customEntityTemplateDto = new CustomEntityTemplateDto();
        try {
            File sourceDir = new File(file);
            byte[] mapData = Files.readAllBytes(sourceDir.toPath());
            ObjectMapper objectMapper = new ObjectMapper();
            jsonMap = objectMapper.readValue(mapData, HashMap.class);
            parseCode(jsonMap, customEntityTemplateDto);
            parseName(jsonMap, customEntityTemplateDto);
            parseFields(jsonMap, customEntityTemplateDto);
        } catch (IOException e) {
        }
        return customEntityTemplateDto;
    }

    private void parseCode(Map<String, Object> jsonMap, CustomEntityTemplateDto customEntityTemplateDto) {
        customEntityTemplateDto.setCode((String)jsonMap.get("id"));
    }

    private void parseName(Map<String, Object> jsonMap, CustomEntityTemplateDto customEntityTemplateDto) {
        customEntityTemplateDto.setName((String)jsonMap.get("title"));
    }

    private void parseFields(Map<String, Object> jsonMap, CustomEntityTemplateDto customEntityTemplateDto) {
        Map<String, Object> items = (Map<String, Object>) jsonMap.get("properties");
        List<CustomFieldTemplateDto> customFieldTemplateDtos = new ArrayList<>();
        for (Map.Entry<String, Object> item : items.entrySet() ) {
            String code = item.getKey();
            Map<String, Object> values = (Map<String, Object>) item.getValue();
            CustomFieldTemplateDto customFieldTemplateDto = new CustomFieldTemplateDto();
            customFieldTemplateDto.setCode(code);
            customFieldTemplateDto.setDescription((String)values.get("description"));
            if (values.get("type").equals("array")) {
                customFieldTemplateDto.setFieldType(CustomFieldTypeEnum.LIST);
                customFieldTemplateDto.setUnique((Boolean)values.get("uniqueItems"));
                Map<String, Object> value = (Map<String, Object>) values.get("items");

            } else {
                customFieldTemplateDto.setFieldType((CustomFieldTypeEnum.valueOf(((String)values.get("type")).toUpperCase())));
            }
            if (values.get("maxLength") != null) {
                customFieldTemplateDto.setMaxValue(Long.valueOf(values.get("maxLength").toString()));
            }
            customFieldTemplateDtos.add(customFieldTemplateDto);
        }
        customEntityTemplateDto.setFields(customFieldTemplateDtos);
    }

}