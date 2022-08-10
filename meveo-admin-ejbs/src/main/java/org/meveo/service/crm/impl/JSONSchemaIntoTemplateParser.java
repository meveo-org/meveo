package org.meveo.service.crm.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.meveo.api.dto.CustomEntityTemplateDto;
import org.meveo.api.dto.CustomFieldTemplateDto;
import org.meveo.api.dto.CustomRelationshipTemplateDto;
import org.meveo.model.crm.custom.CustomFieldIndexTypeEnum;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.persistence.DBStorageType;

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
    public CustomEntityTemplateDto parseJsonFromFile(String file) {
        CustomEntityTemplateDto customEntityTemplateDto = new CustomEntityTemplateDto();
        try {
            File sourceDir = new File(file);
            byte[] mapData = Files.readAllBytes(sourceDir.toPath());
            ObjectMapper objectMapper = new ObjectMapper();
            jsonMap = objectMapper.readValue(mapData, HashMap.class);
            parseCode(jsonMap, customEntityTemplateDto);
            parseName(jsonMap, customEntityTemplateDto);
            parseDescription(jsonMap, customEntityTemplateDto);
            parseStorage(jsonMap, customEntityTemplateDto);
            parseSuperTemplate(jsonMap, customEntityTemplateDto);
            parseFields(jsonMap, customEntityTemplateDto);
        } catch (IOException e) {
        }
        return customEntityTemplateDto;
    }

    public CustomEntityTemplateDto parseJsonContent(String content) {
        CustomEntityTemplateDto customEntityTemplateDto = new CustomEntityTemplateDto();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            jsonMap = objectMapper.readValue(content, HashMap.class);
            parseCode(jsonMap, customEntityTemplateDto);
            parseName(jsonMap, customEntityTemplateDto);
            parseDescription(jsonMap, customEntityTemplateDto);
            parseStorage(jsonMap, customEntityTemplateDto);
            parseSuperTemplate(jsonMap, customEntityTemplateDto);
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

    private void parseDescription(Map<String, Object> jsonMap, CustomEntityTemplateDto customEntityTemplateDto) {
        if (jsonMap.containsKey("description")) {
            customEntityTemplateDto.setDescription((String) jsonMap.get("description"));
        }
    }

    private void parseStorage(Map<String, Object> jsonMap, CustomEntityTemplateDto customEntityTemplateDto) {
        if (jsonMap.containsKey("storages")) {
            List<String> storages = (List<String>) jsonMap.get("storages");
            List<DBStorageType> storageTypeList = new ArrayList<>();
            for (String storage : storages) {
                storageTypeList.add(DBStorageType.valueOf(storage));
            }
            customEntityTemplateDto.setAvailableStorages(storageTypeList);
        }
    }

    private void parseSuperTemplate(Map<String, Object> jsonMap, CustomEntityTemplateDto customEntityTemplateDto) {
        if (jsonMap.containsKey("allOf")) {
            List<HashMap<String, Object>> allOfs = (ArrayList<HashMap<String, Object>>) jsonMap.get("allOf");
            for (HashMap<String, Object> allOf : allOfs) {
                if (allOf.containsKey("$ref")) {
                    String ref = (String) allOf.get("$ref");
                    if (ref != null) {
                        String[] data = ref.split("/");
                        if (data.length > 0) {
                            customEntityTemplateDto.setSuperTemplate(data[data.length - 1]);
                        }
                    }
                }
            }
        }
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
            customFieldTemplateDto.setAllowEdit(!(Boolean) values.get("readOnly"));
            customFieldTemplateDto.setValueRequired(!(Boolean)values.get("nullable"));
            customFieldTemplateDto.setVersionable((Boolean)values.get("versionable"));
            if (values.containsKey("storages")) {
                List<String> storages = (List<String>) values.get("storages");
                List<DBStorageType> storageList = new ArrayList<>();
                for (String storage : storages) {
                    storageList.add(DBStorageType.valueOf(storage));
                }
                customFieldTemplateDto.setStorages(storageList);
            }
            if (values.containsKey("$ref")) {
                customFieldTemplateDto.setStorageType(CustomFieldStorageTypeEnum.SINGLE);
                String ref = (String) values.get("$ref");
                if (ref != null) {
                    String[] data = ref.split("/");
                    if (data.length > 0) {
                        customFieldTemplateDto.setEntityClazz(data[data.length - 1]);
                    }
                }
            } else if (values.get("type") != null){
                if (values.get("type").equals("array")) {
                    customFieldTemplateDto.setUnique((Boolean) values.get("uniqueItems"));
                    Map<String, Object> value = (Map<String, Object>) values.get("items");
                    if (value.containsKey("$ref")) {
                        customFieldTemplateDto.setFieldType(CustomFieldTypeEnum.ENTITY);
                        customFieldTemplateDto.setStorageType(CustomFieldStorageTypeEnum.LIST);
                        String ref = (String) value.get("$ref");
                        if (ref != null) {
                            String[] data = ref.split("/");
                            if (data.length > 0) {
                                customFieldTemplateDto.setEntityClazz(data[data.length - 1]);
                            }
                        }
                    }else {
                        customFieldTemplateDto.setFieldType(CustomFieldTypeEnum.STRING);
                        customFieldTemplateDto.setStorageType(CustomFieldStorageTypeEnum.LIST);
                    }
                } else if (values.get("type").equals("number")) {
                    customFieldTemplateDto.setFieldType(CustomFieldTypeEnum.LONG);
                    customFieldTemplateDto.setStorageType(CustomFieldStorageTypeEnum.SINGLE);
                } else if (values.get("type").equals("object")) {
                    customFieldTemplateDto.setFieldType(CustomFieldTypeEnum.STRING);
                    customFieldTemplateDto.setStorageType(CustomFieldStorageTypeEnum.MAP);
                } else {
                    customFieldTemplateDto.setFieldType((CustomFieldTypeEnum.valueOf(((String) values.get("type")).toUpperCase())));
                    customFieldTemplateDto.setStorageType(CustomFieldStorageTypeEnum.SINGLE);
                }
            }
            if (values.get("maxLength") != null) {
                customFieldTemplateDto.setMaxValue(Long.valueOf(values.get("maxLength").toString()));
            }
            if (values.get("indexType") != null) {
                customFieldTemplateDto.setIndexType(CustomFieldIndexTypeEnum.valueOf((String) values.get("indexType")));
            }
            customFieldTemplateDtos.add(customFieldTemplateDto);
        }
        customEntityTemplateDto.setFields(customFieldTemplateDtos);
    }

    @SuppressWarnings("unchecked")
    public CustomRelationshipTemplateDto parseJsonFromFileIntoCRT(String file) {
        CustomRelationshipTemplateDto customRelationshipTemplateDto = new CustomRelationshipTemplateDto();
        try {
            File sourceDir = new File(file);
            byte[] mapData = Files.readAllBytes(sourceDir.toPath());
            ObjectMapper objectMapper = new ObjectMapper();
            jsonMap = objectMapper.readValue(mapData, HashMap.class);
            parseCodeCRT(jsonMap, customRelationshipTemplateDto);
            parseNameCRT(jsonMap, customRelationshipTemplateDto);
            parseDescriptionCRT(jsonMap, customRelationshipTemplateDto);
            parseStorageCRT(jsonMap, customRelationshipTemplateDto);
            parseUnique(jsonMap, customRelationshipTemplateDto);
            parseStartNode(jsonMap, customRelationshipTemplateDto);
            parseEndNode(jsonMap, customRelationshipTemplateDto);
            parseFieldsCRT(jsonMap, customRelationshipTemplateDto);
        } catch (IOException e) {
        }
        return customRelationshipTemplateDto;
    }

    public CustomRelationshipTemplateDto parseJsonContentIntoCRT(String content) {
        CustomRelationshipTemplateDto customRelationshipTemplateDto = new CustomRelationshipTemplateDto();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            jsonMap = objectMapper.readValue(content, HashMap.class);
            parseCodeCRT(jsonMap, customRelationshipTemplateDto);
            parseNameCRT(jsonMap, customRelationshipTemplateDto);
            parseDescriptionCRT(jsonMap, customRelationshipTemplateDto);
            parseStorageCRT(jsonMap, customRelationshipTemplateDto);
            parseUnique(jsonMap, customRelationshipTemplateDto);
            parseStartNode(jsonMap, customRelationshipTemplateDto);
            parseEndNode(jsonMap, customRelationshipTemplateDto);
            parseFieldsCRT(jsonMap, customRelationshipTemplateDto);
        } catch (IOException e) {
        }
        return customRelationshipTemplateDto;
    }

    private void parseCodeCRT(Map<String, Object> jsonMap, CustomRelationshipTemplateDto customRelationshipTemplateDto) {
        customRelationshipTemplateDto.setCode((String)jsonMap.get("id"));
    }

    private void parseNameCRT(Map<String, Object> jsonMap, CustomRelationshipTemplateDto customRelationshipTemplateDto) {
        customRelationshipTemplateDto.setName((String)jsonMap.get("title"));
    }

    private void parseDescriptionCRT(Map<String, Object> jsonMap, CustomRelationshipTemplateDto customRelationshipTemplateDto) {
        if (jsonMap.containsKey("description")) {
            customRelationshipTemplateDto.setDescription((String) jsonMap.get("description"));
        }
    }

    private void parseStorageCRT(Map<String, Object> jsonMap, CustomRelationshipTemplateDto customRelationshipTemplateDto) {
        if (jsonMap.containsKey("storages")) {
            List<String> storages = (List<String>) jsonMap.get("storages");
            List<DBStorageType> storageTypeList = new ArrayList<>();
            for (String storage : storages) {
                storageTypeList.add(DBStorageType.valueOf(storage));
            }
            customRelationshipTemplateDto.setAvailableStorages(storageTypeList);
        }
    }

    private void parseUnique(Map<String, Object> jsonMap, CustomRelationshipTemplateDto customRelationshipTemplateDto) {
        if (jsonMap.containsKey("unique")) {
            customRelationshipTemplateDto.setUnique((Boolean) jsonMap.get("unique"));
        }
    }

    private void parseStartNode(Map<String, Object> jsonMap, CustomRelationshipTemplateDto customRelationshipTemplateDto) {
        if (jsonMap.containsKey("source")) {
            Map<String, Object> source = (Map<String, Object>) jsonMap.get("source");
            if (source.containsKey("$ref")) {
                String ref = (String) source.get("$ref");
                if (ref != null) {
                    String[] data = ref.split("/");
                    if (data.length > 0) {
                        customRelationshipTemplateDto.setStartNodeCode(data[data.length - 1]);
                    }
                }
            }
        }
    }


    private void parseEndNode(Map<String, Object> jsonMap, CustomRelationshipTemplateDto customRelationshipTemplateDto) {
        if (jsonMap.containsKey("target")) {
            Map<String, Object> target = (Map<String, Object>) jsonMap.get("target");
            if (target.containsKey("$ref")) {
                String ref = (String) target.get("$ref");
                if (ref != null) {
                    String[] data = ref.split("/");
                    if (data.length > 0) {
                        customRelationshipTemplateDto.setEndNodeCode(data[data.length - 1]);
                    }
                }
            }
        }
    }

    private void parseFieldsCRT(Map<String, Object> jsonMap, CustomRelationshipTemplateDto customRelationshipTemplateDto) {
        Map<String, Object> items = (Map<String, Object>) jsonMap.get("properties");
        List<CustomFieldTemplateDto> customFieldTemplateDtos = new ArrayList<>();
        for (Map.Entry<String, Object> item : items.entrySet() ) {
            String code = item.getKey();
            Map<String, Object> values = (Map<String, Object>) item.getValue();
            CustomFieldTemplateDto customFieldTemplateDto = new CustomFieldTemplateDto();
            customFieldTemplateDto.setCode(code);
            customFieldTemplateDto.setDescription((String)values.get("description"));
            customFieldTemplateDto.setAllowEdit(!(Boolean) values.get("readOnly"));
            customFieldTemplateDto.setValueRequired(!(Boolean)values.get("nullable"));
            customFieldTemplateDto.setVersionable((Boolean)values.get("versionable"));
            if (values.containsKey("storages")) {
                List<String> storages = (List<String>) values.get("storages");
                List<DBStorageType> storageList = new ArrayList<>();
                for (String storage : storages) {
                    storageList.add(DBStorageType.valueOf(storage));
                }
                customFieldTemplateDto.setStorages(storageList);
            }
            if (values.containsKey("$ref")) {
                customFieldTemplateDto.setStorageType(CustomFieldStorageTypeEnum.SINGLE);
                String ref = (String) values.get("$ref");
                if (ref != null) {
                    String[] data = ref.split("/");
                    if (data.length > 0) {
                        customFieldTemplateDto.setEntityClazz(data[data.length - 1]);
                    }
                }
            } else if (values.get("type") != null){
                if (values.get("type").equals("array")) {
                    customFieldTemplateDto.setUnique((Boolean) values.get("uniqueItems"));
                    Map<String, Object> value = (Map<String, Object>) values.get("items");
                    if (value.containsKey("$ref")) {
                        customFieldTemplateDto.setFieldType(CustomFieldTypeEnum.ENTITY);
                        customFieldTemplateDto.setStorageType(CustomFieldStorageTypeEnum.LIST);
                        String ref = (String) value.get("$ref");
                        if (ref != null) {
                            String[] data = ref.split("/");
                            if (data.length > 0) {
                                customFieldTemplateDto.setEntityClazz(data[data.length - 1]);
                            }
                        }
                    }else {
                        customFieldTemplateDto.setFieldType(CustomFieldTypeEnum.STRING);
                        customFieldTemplateDto.setStorageType(CustomFieldStorageTypeEnum.LIST);
                    }
                } else if (values.get("type").equals("number")) {
                    customFieldTemplateDto.setFieldType(CustomFieldTypeEnum.LONG);
                    customFieldTemplateDto.setStorageType(CustomFieldStorageTypeEnum.SINGLE);
                } else if (values.get("type").equals("object")) {
                    customFieldTemplateDto.setFieldType(CustomFieldTypeEnum.STRING);
                    customFieldTemplateDto.setStorageType(CustomFieldStorageTypeEnum.MAP);
                } else {
                    customFieldTemplateDto.setFieldType((CustomFieldTypeEnum.valueOf(((String) values.get("type")).toUpperCase())));
                    customFieldTemplateDto.setStorageType(CustomFieldStorageTypeEnum.SINGLE);
                }
            }
            if (values.get("maxLength") != null) {
                customFieldTemplateDto.setMaxValue(Long.valueOf(values.get("maxLength").toString()));
            }
            if (values.get("indexType") != null) {
                customFieldTemplateDto.setIndexType(CustomFieldIndexTypeEnum.valueOf((String) values.get("indexType")));
            }
            customFieldTemplateDtos.add(customFieldTemplateDto);
        }
        customRelationshipTemplateDto.setFields(customFieldTemplateDtos);
    }

}