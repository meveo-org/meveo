package org.meveo.service.crm.impl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.collections.CollectionUtils;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SampleValueHelper {

    public static Map<Integer, String> validateStringType(List<String> sampleValues, CustomFieldStorageTypeEnum storageType) {
        Map<Integer, String> errors = new HashMap<>();
            if (CollectionUtils.isNotEmpty(sampleValues)) {
                Integer line = 1;
                for (String sampleValue : sampleValues) {
                    if (storageType == CustomFieldStorageTypeEnum.SINGLE) {
                        if (sampleValue.startsWith("[") || sampleValue.startsWith("{")) {
                            errors.put(line, "cft.sample.error.notContain.single");
                        } else {
                            String[] values = sampleValue.split(",");
                            if (values == null || values.length == 0) {
                                errors.put(line, "cft.sample.error.typeString.single");
                                break;
                            }
                        }
                    } else if (storageType == CustomFieldStorageTypeEnum.LIST) {
                        if (!sampleValue.startsWith("[") || !sampleValue.endsWith("]")) {
                            errors.put(line, "cft.sample.error.notContain.list");
                        } else {
                            ObjectMapper mapper = new ObjectMapper();
                            List<String> list = new ArrayList<>();
                            try {
                                list = mapper.readValue(sampleValue, new TypeReference<List<String>>() {
                                });
                            } catch (IOException e) {
                                if (CollectionUtils.isEmpty(list)) {
                                    errors.put(line, "cft.sample.error.typeString.list");
                                }
                            }
                        }
                    } else if (storageType == CustomFieldStorageTypeEnum.MAP) {
                        if (!sampleValue.startsWith("{") || !sampleValue.endsWith("}")) {
                            errors.put(line, "cft.sample.error.notContain.map");
                        } else {
                            ObjectMapper mapper = new ObjectMapper();
                            Map<String, String> map = new HashMap<>();
                            try {
                                map = mapper.readValue(sampleValue, new TypeReference<Map<String, String>>() {});
                            } catch (IOException e) {
                                if (map.isEmpty()) {
                                    errors.put(line, "cft.sample.error.typeString.map");
                                }
                            }
                        }
                    }
                    line++;
                }
            }
        return errors;
    }

    public static Map<Integer, String> validateLongType(List<String> sampleValues, CustomFieldStorageTypeEnum storageType) {
        Map<Integer, String> errors = new HashMap<>();
            if (CollectionUtils.isNotEmpty(sampleValues)) {
                Integer line = 1;
                for (String sampleValue : sampleValues) {
                    if (storageType == CustomFieldStorageTypeEnum.SINGLE) {
                        if (sampleValue.startsWith("[") || sampleValue.startsWith("{")) {
                            errors.put(line, "cft.sample.error.notContain.single");
                        } else {
                            List<String> values = Arrays.asList(sampleValue.split(","));
                            if (CollectionUtils.isNotEmpty(values)) {
                                List<Long> longs = new ArrayList<>();
                                for (String value : values) {
                                    try {
                                        longs.add(Long.parseLong(value.trim()));
                                    } catch (NumberFormatException e) {
                                        errors.put(line, "cft.sample.error.typeLong.single");
                                        break;
                                    }
                                }
                            }
                        }
                    } else if (storageType == CustomFieldStorageTypeEnum.LIST) {
                        if (!sampleValue.startsWith("[") || !sampleValue.endsWith("]")) {
                            errors.put(line, "cft.sample.error.notContain.list");
                        } else {
                            sampleValue = sampleValue.substring(1, (sampleValue.length() - 1));
                            List<Long> longs = new ArrayList<>();
                            try {
                                longs = Arrays.asList(sampleValue.split(","))
                                        .stream().map(s -> Long.parseLong(s.trim()))
                                        .collect(Collectors.toList());
                            } catch (NumberFormatException e) {
                                if (CollectionUtils.isEmpty(longs)) {
                                    errors.put(line, "cft.sample.error.typeLong.list");
                                }
                            }
                        }
                    } else if (storageType == CustomFieldStorageTypeEnum.MAP) {
                        if (!sampleValue.startsWith("{") || !sampleValue.endsWith("}")) {
                            errors.put(line, "cft.sample.error.notContain.map");
                        } else {
                            ObjectMapper mapper = new ObjectMapper();
                            Map<String, Long> map = new HashMap<>();
                            try {
                                map = mapper.readValue(sampleValue, new TypeReference<Map<String, Long>>() {});
                            } catch (IOException e) {
                                if (map.isEmpty()) {
                                    errors.put(line, "cft.sample.error.typeLong.map");
                                }
                            }
                        }
                    }
                    line++;
                }
            }
        return errors;
    }

    public static Map<Integer, String> validateDoubleType(List<String> sampleValues, CustomFieldStorageTypeEnum storageType) {
        Map<Integer, String> errors = new HashMap<>();
            if (CollectionUtils.isNotEmpty(sampleValues)) {
                Integer line = 1;
                for (String sampleValue : sampleValues) {
                    if (storageType == CustomFieldStorageTypeEnum.SINGLE) {
                        if (sampleValue.startsWith("[") || sampleValue.startsWith("{")) {
                            errors.put(line, "cft.sample.error.notContain.single");
                        } else {
                            List<String> values = Arrays.asList(sampleValue.split(","));
                            if (CollectionUtils.isNotEmpty(values)) {
                                List<Double> doubles = new ArrayList<>();
                                for (String value : values) {
                                    try {
                                        doubles.add(Double.parseDouble(value.trim()));
                                    } catch (NumberFormatException e) {
                                        errors.put(line, "cft.sample.error.typeDouble.single");
                                        break;
                                    }
                                }
                            }
                        }
                    } else if (storageType == CustomFieldStorageTypeEnum.LIST) {
                        if (!sampleValue.startsWith("[") || !sampleValue.endsWith("]")) {
                            errors.put(line, "cft.sample.error.notContain.list");
                        } else {
                            sampleValue = sampleValue.substring(1, (sampleValue.length() - 1));
                            List<Double> doubles = new ArrayList<>();
                            try {
                                doubles = Arrays.asList(sampleValue.split(","))
                                    .stream().map(s -> Double.parseDouble(s.trim()))
                                    .collect(Collectors.toList());
                            } catch (NumberFormatException e) {
                                if (CollectionUtils.isEmpty(doubles)) {
                                    errors.put(line, "cft.sample.error.typeDouble.list");
                                }
                            }
                        }
                    } else if (storageType == CustomFieldStorageTypeEnum.MAP) {
                        if (!sampleValue.startsWith("{") || !sampleValue.endsWith("}")) {
                            errors.put(line, "cft.sample.error.notContain.map");
                        } else {
                            ObjectMapper mapper = new ObjectMapper();
                            Map<String, Double> map = new HashMap<>();
                            try {
                                map = mapper.readValue(sampleValue, new TypeReference<Map<String, Double>>() {});
                            } catch (IOException e) {
                                if (map.isEmpty()) {
                                    errors.put(line, "cft.sample.error.typeDouble.map");
                                }
                            }
                        }
                    }
                    line++;
                }
            }
        return errors;
    }

    public static Map<Integer, String> validateChildEntityType(Map<String, CustomFieldTemplate> customFieldTemplates, List<String> sampleValues, CustomFieldStorageTypeEnum storageType) {
        Map<Integer, String> errors = new HashMap<>();
        try {
            if (CollectionUtils.isNotEmpty(sampleValues)) {
                Integer line = 1;
                for (String sampleValue : sampleValues) {
                    if (storageType == CustomFieldStorageTypeEnum.SINGLE) {
                        ObjectMapper mapper = new ObjectMapper();
                        sampleValue = sampleValue.trim();
                        if (!sampleValue.startsWith("{") || !sampleValue.endsWith("}")) {
                            errors.put(line, "cft.sample.error.notContain.map");
                        } else {
                            Map<String, Object> data = mapper.readValue(sampleValue, new TypeReference<Map<String, Object>>() {});
                            if (!data.isEmpty()) {
                                for (Map.Entry<String, Object> entry : data.entrySet()) {
                                    if (!customFieldTemplates.containsKey(entry.getKey())) {
                                        errors.put(line, "cft.sample.error.typeChildEntity.notContainsKey");
                                        break;
                                    } else {
                                        Object value = entry.getValue();
                                        CustomFieldTemplate cft = customFieldTemplates.get(entry.getKey());
                                        try {
                                            if (cft.getFieldType() == CustomFieldTypeEnum.STRING || cft.getFieldType() == CustomFieldTypeEnum.SECRET) {
                                                String converted = (String) value;
                                            } else if (cft.getFieldType() == CustomFieldTypeEnum.LONG) {
                                                Long converted = (Long) value;
                                            } else if (cft.getFieldType() == CustomFieldTypeEnum.DOUBLE) {
                                                Double converted = (Double) value;
                                            } else if (cft.getFieldType() == CustomFieldTypeEnum.BOOLEAN) {
                                                Boolean converted = (Boolean) value;
                                            }
                                        } catch (ClassCastException e) {
                                            errors.put(line, "cft.sample.error.typeChildEntity.notContainsValue");
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (storageType == CustomFieldStorageTypeEnum.LIST) {
                        ObjectMapper mapper = new ObjectMapper();
                        sampleValue = sampleValue.trim();
                        if (!sampleValue.startsWith("[") || !sampleValue.endsWith("]")) {
                            errors.put(line, "cft.sample.error.notContain.list");
                        } else {
                            List<Map<String, Object>> data = mapper.readValue(sampleValue, new TypeReference<List<Map<String, Object>>>() {
                            });
                            if (CollectionUtils.isNotEmpty(data)) {
                                boolean isBreak = false;
                                for (Map<String, Object> item : data) {
                                    for (Map.Entry<String, Object> entry : item.entrySet()) {
                                        if (!customFieldTemplates.containsKey(entry.getKey())) {
                                            errors.put(line, "cft.sample.error.typeChildEntity.notContainsKey");
                                            isBreak = true;
                                            break;
                                        } else {
                                            Object value = entry.getValue();
                                            CustomFieldTemplate cft = customFieldTemplates.get(entry.getKey());
                                            try {
                                                if (cft.getFieldType() == CustomFieldTypeEnum.STRING) {
                                                    String converted = (String) value;
                                                } else if (cft.getFieldType() == CustomFieldTypeEnum.LONG) {
                                                    Long converted = (Long) value;
                                                } else if (cft.getFieldType() == CustomFieldTypeEnum.DOUBLE) {
                                                    Double converted = (Double) value;
                                                } else if (cft.getFieldType() == CustomFieldTypeEnum.BOOLEAN) {
                                                    Boolean converted = (Boolean) value;
                                                }
                                            } catch (ClassCastException e) {
                                                errors.put(line, "cft.sample.error.typeChildEntity.notContainsValue");
                                                isBreak = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (isBreak) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    line++;
                }
            }
        } catch (IOException e) {
            return null;
        }
        return errors;
    }
}
