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

package org.meveo.jmeter.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.util.JMeterUtils;
import org.meveo.api.dto.function.FunctionDto;
import org.meveo.jmeter.login.model.Host;
import org.meveo.jmeter.login.model.HostConnection;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.typereferences.GenericTypeReferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.nio.ch.IOUtil;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class FunctionManager {

    private static final String LOGGING_URL = JMeterUtils.getProperty("meveo.logging.url");
    private static final String SECRET = JMeterUtils.getProperty("meveo.logging.secret");
    private static final String CLIENT_ID = JMeterUtils.getProperty("meveo.logging.client-id");
    private static final String GRANT_TYPE = "password";

    public static final String ENDPOINT_URL = JMeterUtils.getProperty("meveo.function.endpoint");
    public static final String UPLOAD_URL = JMeterUtils.getProperty("meveo.function.upload");

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Logger LOG = LoggerFactory.getLogger(FunctionManager.class);

    private static String token = System.getProperty("token");
    private static String refresh_token;
    private static CompletableFuture<List<FunctionDto>> functions;
    private static long loginTimeout;
    private static ScheduledFuture<?> refreshTask;
    private static Host currentHost;

    static {
        String hostName = System.getProperty("hostName");
        String portNumber = System.getProperty("portNumber");
        String protocol = System.getProperty("protocol");

        if(hostName != null && portNumber != null && protocol != null){
            currentHost = new Host();
            currentHost.setProtocol(protocol);
            currentHost.setPortNumber(portNumber);
            currentHost.setHostName(hostName);
        }
    }

    public static Map<String, Object> test(String functionCode, Arguments arguments) {

        if(token == null){
            throw new IllegalArgumentException("Authorization token must not be null");
        }

        return doRequest(() -> {
            String serialiazedArgs = OBJECT_MAPPER.writeValueAsString(arguments.getArgumentsAsMap());
            String testUrl = String.format(getHostUri() + UPLOAD_URL, functionCode);
            HttpPost post = new HttpPost(testUrl);

            setBearer(post);
            setContentType(post, "application/json");
            post.setEntity(new StringEntity(serialiazedArgs));

            return post;

        }, responseData -> OBJECT_MAPPER.readValue(responseData.getContent(), GenericTypeReferences.MAP_STRING_OBJECT), "Cannot execute test");

    }

    public static void upload(String functionCode, File testSuite) {
        CompletableFuture.runAsync(() -> doRequest(() -> {
            String uploadUrl = String.format(getHostUri() + UPLOAD_URL, functionCode);
            HttpPatch patch = new HttpPatch(uploadUrl);

            setBearer(patch);
            setContentType(patch, "application/octet-stream");
            patch.setEntity(new FileEntity(testSuite));

            return patch;
        }, responseData -> {
            LOG.info("Test for function {} successfully uploaded", functionCode);
            refresh();
            return null;
        }, "Error while uploading test"));

    }

    public static boolean login(HostConnection hostConnection) {
        currentHost = hostConnection.getHost();

        return doRequest(() -> {

            HttpPost post = new HttpPost(getHostUri() + LOGGING_URL);

            ArrayList<NameValuePair> postParameters = new ArrayList<>();
            postParameters.add(new BasicNameValuePair("client_id", CLIENT_ID));
            postParameters.add(new BasicNameValuePair("username", hostConnection.getUserName()));
            postParameters.add(new BasicNameValuePair("password", hostConnection.getPassword()));
            postParameters.add(new BasicNameValuePair("grant_type", GRANT_TYPE));
            postParameters.add(new BasicNameValuePair("client_secret", SECRET));

            post.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
            return post;

        }, responseData -> {
            Map<String, String> responseBody = OBJECT_MAPPER.readValue(responseData.getContent(), GenericTypeReferences.MAP_STRING_STRING);
            token = responseBody.get("access_token");
            refresh_token = responseBody.get("refresh_token");
            loginTimeout = Long.parseLong(responseBody.get("expires_in"));

            functions = CompletableFuture.supplyAsync(FunctionManager::download);

            return true;
        }, "Cannot log in", false);
    }

    private static List<FunctionDto> download() {

        return doRequest(() -> {
            HttpGet httpGet = new HttpGet(getHostUri() + ENDPOINT_URL);
            setBearer(httpGet);
            return httpGet;
        }, responseData -> {
            String result = IOUtils.toString(responseData.getContent(), StandardCharsets.UTF_8);
            if (token != null) {
                refreshTokenThread();
            }
            return OBJECT_MAPPER.readValue(result, FunctionDto.DTO_LIST_TYPE_REF);
        }, "Error while retrieving functions from meveo");

    }

    private static long getRefreshToken() {

        return doRequest(() -> {
            HttpPost post = new HttpPost(getHostUri() + LOGGING_URL);

            ArrayList<NameValuePair> postParameters = new ArrayList<>();
            postParameters.add(new BasicNameValuePair("client_id", CLIENT_ID));
            postParameters.add(new BasicNameValuePair("grant_type", "refresh_token"));
            postParameters.add(new BasicNameValuePair("refresh_token", refresh_token));
            postParameters.add(new BasicNameValuePair("client_secret", SECRET));

            post.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));

            return post;
        }, responseData -> {
            Map<String, String> responseBody = OBJECT_MAPPER.readValue(responseData.getContent(), GenericTypeReferences.MAP_STRING_STRING);
            token = responseBody.get("access_token");
            refresh_token = responseBody.get("refresh_token");
            return Long.parseLong(responseBody.get("expires_in"));
        }, "Cannot refresh token", 3600L);

    }

    public static List<FunctionDto> getFunctions() {
        try {
            return functions.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized void refresh() {
        if (functions == null || functions.isDone()) {
            functions = CompletableFuture.supplyAsync(FunctionManager::download);
        }
    }

    private static String getHostUri() {
        String hostUri = currentHost.getProtocol() + "://" + currentHost.getHostName();
        if(!StringUtils.isBlank(currentHost.getPortNumber())){
            hostUri += ":" + currentHost.getPortNumber();
        }
        return hostUri;
    }


    private static void refreshTokenThread() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        if (refreshTask != null) {
            refreshTask.cancel(false);
        }

        Runnable task = () -> {
            loginTimeout = getRefreshToken();
            refreshTokenThread();
        };
        refreshTask = scheduler.schedule(task, loginTimeout, TimeUnit.SECONDS);
    }

    private static CloseableHttpClient createAcceptSelfSignedCertificateClient()
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

        // use the TrustSelfSignedStrategy to allow Self Signed Certificates
        SSLContext sslContext = SSLContextBuilder
                .create()
                .loadTrustMaterial(new TrustSelfSignedStrategy())
                .build();

        // we can optionally setEnable hostname verification.
        // if you don't want to further weaken the security, you don't have to include this.
        HostnameVerifier allowAllHosts = new NoopHostnameVerifier();

        // create an SSL Socket Factory to use the SSLContext with the trust self signed certificate strategy
        // and allow all hosts verifier.
        SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts);

        // finally create the HttpClient using HttpClient factory methods and assign the ssl socket factory
        return HttpClients
                .custom()
                .setSSLSocketFactory(connectionFactory)
                .build();
    }

    private static <T> T doRequest(PrepareRequest method, OnSuccess<T> onSuccess, String errorMessage, T defaultValue) {
        try (CloseableHttpClient client = createAcceptSelfSignedCertificateClient()) {
            final HttpUriRequest request = method.prepare();
            try(CloseableHttpResponse response = client.execute(request)) {
                if (response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 400) {
                    return onSuccess.onSuccess(response.getEntity());
                } else {
                    final String responseContentString = IOUtils.toString(response.getEntity().getContent());
                    try{
                        Map<String, Object> responseContent = JacksonUtil.OBJECT_MAPPER.readValue(responseContentString, GenericTypeReferences.MAP_STRING_OBJECT);
                        LOG.error(errorMessage + " : {}. {}.", response.getStatusLine(), responseContent.get("error_description"));
                    }catch (Exception e){
                        LOG.error(errorMessage + " : {}. {}.", response.getStatusLine(), responseContentString);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(errorMessage, e);
        }
        return defaultValue;
    }

    private static void setContentType(HttpUriRequest request, String type) {
        request.addHeader("Content-Type", type);
    }

    private static void setBearer(HttpUriRequest request) {
        request.addHeader("Authorization", "Bearer " + token);
    }

    private static <T> T doRequest(PrepareRequest method, OnSuccess<T> onSuccess, String errorMessage) {
        return doRequest(method, onSuccess, errorMessage, null);
    }

    @FunctionalInterface
    private interface PrepareRequest {
        HttpUriRequest prepare() throws Exception;
    }

    @FunctionalInterface
    private interface OnSuccess<T> {
        T onSuccess(HttpEntity responseData) throws Exception;
    }

}
