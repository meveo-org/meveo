package org.meveo.service.crm.impl;

import com.fasterxml.jackson.core.type.TypeReference;
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

    public static Boolean validateStringType(List<String> sampleValues, CustomFieldStorageTypeEnum storageType) {
        Boolean result = true;
        Gson gson = new Gson();
        try {
            if (CollectionUtils.isNotEmpty(sampleValues)) {
                for (String sampleValue : sampleValues) {
                    if (storageType == CustomFieldStorageTypeEnum.SINGLE) {
                        if (sampleValue.startsWith("[") || sampleValue.startsWith("{")) {
                            return false;
                        }
                        String[] values = sampleValue.split(",");
                        if (values == null || values.length == 0) {
                            return false;
                        }
                    } else if (storageType == CustomFieldStorageTypeEnum.LIST) {
                        if (!sampleValue.startsWith("[") || !sampleValue.endsWith("]")) {
                            return false;
                        }
                        ObjectMapper mapper = new ObjectMapper();
                        List<String> list = mapper.readValue(sampleValue, new TypeReference<List<String>>() {});
                        if (CollectionUtils.isEmpty(list)) {
                            return false;
                        }
                    } else if (storageType == CustomFieldStorageTypeEnum.MAP) {
                        ObjectMapper mapper = new ObjectMapper();
                        Map<String, String> map = mapper.readValue(sampleValue, new TypeReference<Map<String, String>>() {});
                        if (map.isEmpty()) {
                            return false;
                        }
                    }
                }
            }
        } catch (IOException e) {
            return false;
        }
        return result;
    }

    public static Boolean validateLongType(List<String> sampleValues, CustomFieldStorageTypeEnum storageType) {
        Boolean result = true;
        Gson gson = new Gson();
        try {
            if (CollectionUtils.isNotEmpty(sampleValues)) {
                for (String sampleValue : sampleValues) {
                    if (storageType == CustomFieldStorageTypeEnum.SINGLE) {
                        List<String> values = Arrays.asList(sampleValue.split(","));
                        if (CollectionUtils.isNotEmpty(values)) {
                            List<Long> longs = new ArrayList<>();
                            for (String value : values) {
                                try {
                                    longs.add(Long.parseLong(value.trim()));
                                } catch (NumberFormatException e) {
                                    return false;
                                }
                            }
                            if (CollectionUtils.isEmpty(longs)) {
                                return false;
                            }
                        }
                    } else if (storageType == CustomFieldStorageTypeEnum.LIST) {
                        if (!sampleValue.startsWith("[") || !sampleValue.endsWith("]")) {
                            return false;
                        }
                        sampleValue = sampleValue.substring(1, (sampleValue.length() - 1));
                        List<Long> longs = Arrays.asList(sampleValue.split(","))
                                .stream().map(s -> Long.parseLong(s.trim()))
                                .collect(Collectors.toList());
                        if (CollectionUtils.isEmpty(longs)) {
                            return false;
                        }
                    } else if (storageType == CustomFieldStorageTypeEnum.MAP) {
                        ObjectMapper mapper = new ObjectMapper();
                        Map<String, Long> map = mapper.readValue(sampleValue, new TypeReference<Map<String, Long>>() {});
                        if (map.isEmpty()) {
                            return false;
                        }
                    }
                }
            }
        }catch (IOException e) {
                return false;
        }
        return result;
    }

    public static Boolean validateDoubleType(List<String> sampleValues, CustomFieldStorageTypeEnum storageType) {
        Boolean result = true;
        Gson gson = new Gson();
        try {
            if (CollectionUtils.isNotEmpty(sampleValues)) {
                for (String sampleValue : sampleValues) {
                    if (storageType == CustomFieldStorageTypeEnum.SINGLE) {
                        List<String> values = Arrays.asList(sampleValue.split(","));
                        if (CollectionUtils.isNotEmpty(values)) {
                            List<Double> doubles = new ArrayList<>();
                            for (String value : values) {
                                try {
                                    doubles.add(Double.parseDouble(value.trim()));
                                } catch (NumberFormatException e) {
                                    return false;
                                }
                            }
                            if (CollectionUtils.isEmpty(doubles)) {
                                return false;
                            }
                        }
                    } else if (storageType == CustomFieldStorageTypeEnum.LIST) {
                        if (!sampleValue.startsWith("[") || !sampleValue.endsWith("]")) {
                            return false;
                        }
                        sampleValue = sampleValue.substring(1, (sampleValue.length() - 1));
                        List<Double> doubles = Arrays.asList(sampleValue.split(","))
                                .stream().map(s -> Double.parseDouble(s.trim()))
                                .collect(Collectors.toList());
                        if (CollectionUtils.isEmpty(doubles)) {
                            return false;
                        }
                    } else if (storageType == CustomFieldStorageTypeEnum.MAP) {
                        ObjectMapper mapper = new ObjectMapper();
                        Map<String, Double> map = mapper.readValue(sampleValue, new TypeReference<Map<String, Double>>() {});
                        if (map.isEmpty()) {
                            return false;
                        }
                    }
                }
            }
        }catch (IOException e) {
            return false;
        }
        return result;
    }

    public static Boolean validateChildEntityType(Map<String, CustomFieldTemplate> customFieldTemplates, List<String> sampleValues, CustomFieldStorageTypeEnum storageType) {
        Boolean result = true;
        try {
            if (CollectionUtils.isNotEmpty(sampleValues)) {
                for (String sampleValue : sampleValues) {
                    if (storageType == CustomFieldStorageTypeEnum.LIST) {
                        ObjectMapper mapper = new ObjectMapper();
                        List<Map<String, Object>> data = mapper.readValue(sampleValue, new TypeReference<List<Map<String, Object>>>() {});
                        if (CollectionUtils.isNotEmpty(data)) {
                            for (Map<String, Object> item : data) {
                                for (Map.Entry<String, Object> entry : item.entrySet()) {
                                    if (!customFieldTemplates.containsKey(entry.getKey())) {
                                        return false;
                                    }
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
                                    } catch (NumberFormatException e) {
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            return false;
        }
        return result;
    }
}
