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

    public static List<Integer> validateStringType(List<String> sampleValues, CustomFieldStorageTypeEnum storageType) {
        List<Integer> lines = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(sampleValues)) {
                Integer line = 1;
                for (String sampleValue : sampleValues) {
                    if (storageType == CustomFieldStorageTypeEnum.SINGLE) {
                        if (sampleValue.startsWith("[") || sampleValue.startsWith("{")) {
                            lines.add(line);
                        } else {
                            String[] values = sampleValue.split(",");
                            if (values == null || values.length == 0) {
                                lines.add(line);
                                break;
                            }
                        }
                    } else if (storageType == CustomFieldStorageTypeEnum.LIST) {
                        if (!sampleValue.startsWith("[") || !sampleValue.endsWith("]")) {
                            lines.add(line);
                        } else {
                            ObjectMapper mapper = new ObjectMapper();
                            List<String> list = new ArrayList<>();
                            try {
                                list = mapper.readValue(sampleValue, new TypeReference<List<String>>() {
                                });
                            } catch (IOException e) {
                                if (CollectionUtils.isEmpty(list)) {
                                    lines.add(line);
                                }
                            }
                        }
                    } else if (storageType == CustomFieldStorageTypeEnum.MAP) {
                        if (!sampleValue.startsWith("{") || !sampleValue.endsWith("}")) {
                            lines.add(line);
                        } else {
                            ObjectMapper mapper = new ObjectMapper();
                            Map<String, String> map = new HashMap<>();
                            try {
                                map = mapper.readValue(sampleValue, new TypeReference<Map<String, String>>() {});
                            } catch (IOException e) {
                                if (map.isEmpty()) {
                                    lines.add(line);
                                }
                            }
                        }
                    }
                    line++;
                }
            }
        return lines;
    }

    public static List<Integer> validateLongType(List<String> sampleValues, CustomFieldStorageTypeEnum storageType) {
        List<Integer> lines = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(sampleValues)) {
                Integer line = 1;
                for (String sampleValue : sampleValues) {
                    if (storageType == CustomFieldStorageTypeEnum.SINGLE) {
                        List<String> values = Arrays.asList(sampleValue.split(","));
                        if (CollectionUtils.isNotEmpty(values)) {
                            List<Long> longs = new ArrayList<>();
                            for (String value : values) {
                                try {
                                    longs.add(Long.parseLong(value.trim()));
                                } catch (NumberFormatException e) {
                                    lines.add(line);
                                    break;
                                }
                            }
                        }
                    } else if (storageType == CustomFieldStorageTypeEnum.LIST) {
                        if (!sampleValue.startsWith("[") || !sampleValue.endsWith("]")) {
                            lines.add(line);
                        } else {
                            sampleValue = sampleValue.substring(1, (sampleValue.length() - 1));
                            List<Long> longs = new ArrayList<>();
                            try {
                                longs = Arrays.asList(sampleValue.split(","))
                                        .stream().map(s -> Long.parseLong(s.trim()))
                                        .collect(Collectors.toList());
                            } catch (NumberFormatException e) {
                                if (CollectionUtils.isEmpty(longs)) {
                                    lines.add(line);
                                }
                            }
                        }
                    } else if (storageType == CustomFieldStorageTypeEnum.MAP) {
                        if (!sampleValue.startsWith("{") || !sampleValue.endsWith("}")) {
                            lines.add(line);
                        } else {
                            ObjectMapper mapper = new ObjectMapper();
                            Map<String, Long> map = new HashMap<>();
                            try {
                                map = mapper.readValue(sampleValue, new TypeReference<Map<String, Long>>() {});
                            } catch (IOException e) {
                                if (map.isEmpty()) {
                                    lines.add(line);
                                }
                            }
                        }
                    }
                    line++;
                }
            }
        return lines;
    }

    public static List<Integer> validateDoubleType(List<String> sampleValues, CustomFieldStorageTypeEnum storageType) {
        List<Integer> lines = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(sampleValues)) {
                Integer line = 1;
                for (String sampleValue : sampleValues) {
                    if (storageType == CustomFieldStorageTypeEnum.SINGLE) {
                        List<String> values = Arrays.asList(sampleValue.split(","));
                        if (CollectionUtils.isNotEmpty(values)) {
                            List<Double> doubles = new ArrayList<>();
                            for (String value : values) {
                                try {
                                    doubles.add(Double.parseDouble(value.trim()));
                                } catch (NumberFormatException e) {
                                    lines.add(line);
                                    break;
                                }
                            }
                        }
                    } else if (storageType == CustomFieldStorageTypeEnum.LIST) {
                        if (!sampleValue.startsWith("[") || !sampleValue.endsWith("]")) {
                            lines.add(line);
                        } else {
                            sampleValue = sampleValue.substring(1, (sampleValue.length() - 1));
                            List<Double> doubles = new ArrayList<>();
                            try {
                                doubles = Arrays.asList(sampleValue.split(","))
                                    .stream().map(s -> Double.parseDouble(s.trim()))
                                    .collect(Collectors.toList());
                            } catch (NumberFormatException e) {
                                if (CollectionUtils.isEmpty(doubles)) {
                                    lines.add(line);
                                }
                            }
                        }
                    } else if (storageType == CustomFieldStorageTypeEnum.MAP) {
                        if (!sampleValue.startsWith("{") || !sampleValue.endsWith("}")) {
                            lines.add(line);
                        } else {
                            ObjectMapper mapper = new ObjectMapper();
                            Map<String, Double> map = new HashMap<>();
                            try {
                                map = mapper.readValue(sampleValue, new TypeReference<Map<String, Double>>() {});
                            } catch (IOException e) {
                                if (map.isEmpty()) {
                                    lines.add(line);
                                }
                            }
                        }
                    }
                    line++;
                }
            }
        return lines;
    }

    public static List<Integer> validateChildEntityType(Map<String, CustomFieldTemplate> customFieldTemplates, List<String> sampleValues, CustomFieldStorageTypeEnum storageType) {
        List<Integer> lines = new ArrayList<>();
        try {
            if (CollectionUtils.isNotEmpty(sampleValues)) {
                Integer line = 1;
                for (String sampleValue : sampleValues) {
                    if (storageType == CustomFieldStorageTypeEnum.SINGLE) {
                        ObjectMapper mapper = new ObjectMapper();
                        sampleValue = sampleValue.trim();
                        if (!sampleValue.startsWith("{") || !sampleValue.endsWith("}")) {
                            lines.add(line);
                        } else {
                            Map<String, Object> data = mapper.readValue(sampleValue, new TypeReference<Map<String, Object>>() {});
                            if (!data.isEmpty()) {
                                for (Map.Entry<String, Object> entry : data.entrySet()) {
                                    if (!customFieldTemplates.containsKey(entry.getKey())) {
                                        lines.add(line);
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
                                            lines.add(line);
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
                            lines.add(line);
                        } else {
                            List<Map<String, Object>> data = mapper.readValue(sampleValue, new TypeReference<List<Map<String, Object>>>() {
                            });
                            if (CollectionUtils.isNotEmpty(data)) {
                                boolean isBreak = false;
                                for (Map<String, Object> item : data) {
                                    for (Map.Entry<String, Object> entry : item.entrySet()) {
                                        if (!customFieldTemplates.containsKey(entry.getKey())) {
                                            lines.add(line);
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
                                                lines.add(line);
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
        return lines;
    }
}
