package org.meveo.api;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.httpclient.util.HttpURLConnection;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.meveo.admin.exception.BusinessException;
import org.meveo.commons.utils.ParamBean;
import org.meveo.export.RemoteAuthenticationException;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.crm.custom.CustomFieldValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CETUtils {

    private static final Logger log = LoggerFactory.getLogger(CETUtils.class);

    public Response callMeveoApi(String url, String body, String httpMethod) throws BusinessException {
        log.info("callMeveoApi url:{},body:{}", url, body);
        try {
            ParamBean paramBean = ParamBean.getInstance();
            String baseurl = paramBean.getProperty("meveo.admin.baseUrl", "http://localhost:8080/meveo/");
            String username = paramBean.getProperty("meveo.admin.login", "meveo.admin");
            String password = paramBean.getProperty("meveo.admin.password", "meveo.admin");
            ResteasyClient client = new ResteasyClientBuilder().build();
            ResteasyWebTarget target = client.target(baseurl + url);
            log.debug("call {} with body:{}", baseurl + url, body);
            BasicAuthentication basicAuthentication = new BasicAuthentication(username, password);
            target.register(basicAuthentication);
            Response response = null;
            if (HttpMethod.POST.equals(httpMethod)) {
                response = target.request().post(Entity.entity(body, MediaType.APPLICATION_JSON));
            } else if (HttpMethod.GET.equals(httpMethod)) {
                response = target.request().get();
            }
            if (response.getStatus() != HttpURLConnection.HTTP_OK) {
                if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED || response.getStatus() == HttpURLConnection.HTTP_FORBIDDEN) {
                    throw new RemoteAuthenticationException("Http status " + response.getStatus() + ", info " + response.getStatusInfo().getReasonPhrase());
                } else {
                    throw new BusinessException("Http status " + response.getStatus() + ", info " + response.getStatusInfo().getReasonPhrase());
                }
            }
            return response;
        } catch (Exception e) {
            log.error("Failed to communicate {}. Reason {}", url, (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()), e);
            throw new BusinessException("Failed to communicate " + url + ". Error " + e.getMessage());
        }
    }

    public static String getMonthAndYearFromDate(Date date) {
        String result = null;
        if (date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            result = calendar.get(Calendar.MONTH) + 1 + "-" + calendar.get(Calendar.YEAR);
        }
        return result;
    }

    public static boolean isParsableAsLong(final String s) {
        try {
            Long.valueOf(s);
            return true;
        } catch (NumberFormatException numberFormatException) {
            return false;
        }
    }

    public static List<String> getAllIpAddressesBetweenTwoIp(String startIp, String endIp) {
        List<String> ips = new ArrayList<>();

        String[] startParts = startIp.split("(?<=\\.)(?!.*\\.)");
        String[] endParts = endIp.split("(?<=\\.)(?!.*\\.)");

        int first = Integer.parseInt(startParts[1]);
        int last = Integer.parseInt(endParts[1]);
        for (int i = first; i <= last; i++) {
            ips.add(startParts[0] + i);
        }
        return ips;
    }

    public static Date parseDateWithPattern(String dateValue, String pattern) {
        if (dateValue == null || dateValue.trim().length() == 0) {
            return null;
        }
        Date result = null;
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        try {
            result = sdf.parse(dateValue);
        } catch (Exception ignored) {}
        return result;
    }

    public static String evaluateExpression(String query, Map<String, Object> queryValues) {
        StrSubstitutor sub = new StrSubstitutor(queryValues);
        String resolvedStatement = sub.replace(query);
        return resolvedStatement.replaceAll("null", "");
    }

    public static boolean existedMacAddresses(Set<String> newMacAddresses, Set<String> existingMacAddresses) {
        for (String newMacAddress : newMacAddresses) {
            boolean isCovered = false;
            for (String existingMacAddress : existingMacAddresses) {
                if (newMacAddress.equalsIgnoreCase(existingMacAddress)) {
                    isCovered = true;
                    break;
                }
            }
            if (!isCovered) {
                return false;
            }
        }
        return true;
    }

    public static boolean coverMacAddresses(Set<String> newMacAddresses, Set<String> existingMacAddresses) {
        for (String newMacAddress : newMacAddresses) {
            for (String existingMacAddress : existingMacAddresses) {
                if (newMacAddress.equalsIgnoreCase(existingMacAddress)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String stripAndFormatFields(String value) {
        String convertedValue = org.apache.commons.lang3.StringUtils.stripAccents(value.toLowerCase()).trim();
        convertedValue = convertedValue.replaceAll("[.,\\s-'’]", "");
        return convertedValue;
    }

    public static String extractBetweenString(String documentContent, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(documentContent);
        String result = null;
        while (matcher.find()) {
            result = matcher.group(1);
        }
        return result;
    }

    public static String decode(String source) {
        byte[] decodedBytes = org.apache.commons.codec.binary.Base64.decodeBase64(source.getBytes());
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    public static void decode(String source, String targetFile) throws Exception {

        byte[] decodedBytes = org.apache.commons.codec.binary.Base64.decodeBase64(source.getBytes());

        writeByteArraysToFile(targetFile, decodedBytes);
    }

    public static void writeByteArraysToFile(String fileName, byte[] content) throws IOException {
        File file = new File(fileName);
        BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(file));
        writer.write(content);
        writer.flush();
        writer.close();

    }
    
    /**
     * Init a {@link CustomFieldValues} container from values map and fields definitions
     * 
     * @param valuesMap	Values map
     * @param fields	Fields definition
     * @return instance of {@link CustomFieldValues}
     */
    public static CustomFieldValues initCustomFieldValues(Map<String, Object> valuesMap, Collection<CustomFieldTemplate> fields) {
    	CustomFieldValues values = new CustomFieldValues();
    	
    	valuesMap.forEach((k,v) -> {
    		values.setValue(k, v);
    	});
    	
    	fields.stream().filter(f -> CustomFieldStorageTypeEnum.LIST.equals(f.getStorageType()))
    		.filter(f -> valuesMap.get(f.getDbFieldname()) == null)
			.forEach(f -> values.setValue(f.getDbFieldname(), f.getNewListValue(), f.getFieldType().getDataClass()));
    	
    	return values;
    }

}

