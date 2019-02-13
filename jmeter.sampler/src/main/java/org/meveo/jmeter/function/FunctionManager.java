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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.jmeter.util.JMeterUtils;
import org.meveo.api.dto.function.FunctionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
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
    private static final String LOGIN = JMeterUtils.getProperty("meveo.logging.login");
    private static final String PASSWORD = JMeterUtils.getProperty("meveo.logging.password");
    private static final String GRANT_TYPE = "password";

    public static final String ENDPOINT_URL = JMeterUtils.getProperty("meveo.function.endpoint");
    public static final String UPLOAD_URL = JMeterUtils.getProperty("meveo.function.upload");

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Logger LOG = LoggerFactory.getLogger(FunctionManager.class);

    private static String token;
    private static String refresh_token;
    private static int loginAttempts = 0;
    private static CompletableFuture<List<FunctionDto>> functions;
    private static long loginTimeout;
    private static ScheduledFuture<?> refreshTask;

    static {
        refresh();
    }

    public static void upload(String functionCode, File testSuite){
        CompletableFuture.runAsync(() -> {
            try (CloseableHttpClient client = createAcceptSelfSignedCertificateClient()){

                String uploadUrl = String.format(UPLOAD_URL, functionCode);
                HttpPatch patch = new HttpPatch(uploadUrl);

                if(token == null){
                    login();
                }

                patch.addHeader("Authorization", "Bearer " + token);
                patch.addHeader("Content-Type", "application/octet-stream");
                patch.setEntity(new FileEntity(testSuite));

                CloseableHttpResponse response = client.execute(patch);

                final int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 204 || statusCode == 200) {
                    LOG.info("Test for function {} successfully uploaded", functionCode);
                    refresh();
                } else {
                    LOG.error("Error while uploading test");
                }

                response.close();
            } catch (IOException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
                LOG.error("Error while uploading test", e);
            }
        });
    }

    public static List<FunctionDto> getFunctions() {
        try {
            return functions.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized void refresh(){
        if(functions == null || functions.isDone()) {
            functions = CompletableFuture.supplyAsync(FunctionManager::download);
            if(token != null){
                refreshTokenThread();
            }
        }
    }

    private static void login() throws IOException {
        try (CloseableHttpClient client = createAcceptSelfSignedCertificateClient()){

            HttpPost post = new HttpPost(LOGGING_URL);

            ArrayList<NameValuePair> postParameters = new ArrayList<>();
            postParameters.add(new BasicNameValuePair("client_id", CLIENT_ID));
            postParameters.add(new BasicNameValuePair("username", LOGIN));
            postParameters.add(new BasicNameValuePair("password", PASSWORD));
            postParameters.add(new BasicNameValuePair("grant_type", GRANT_TYPE));
            postParameters.add(new BasicNameValuePair("client_secret", SECRET));

            post.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
            CloseableHttpResponse response = client.execute(post);

            if (response.getStatusLine().getStatusCode() == 200) {
                final TypeReference mapTypeRef = new TypeReference<Map<String, String>>() {};
                Map<String, String> responseBody = OBJECT_MAPPER.readValue(response.getEntity().getContent(), mapTypeRef);
                token = responseBody.get("access_token");
                refresh_token = responseBody.get("refresh_token");
                loginTimeout = Long.parseLong(responseBody.get("expires_in"));
                refreshTokenThread();
                loginAttempts = 0;
            }else {
                LOG.error("Cannot log in. Login attempts = {}. Server answered with {} ", ++loginAttempts, response.getEntity().getContent());
            }

            response.close();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException  e) {
            LOG.error("Cannot log in", e);
        }
    }

    private static List<FunctionDto> download() {

        try (CloseableHttpClient client = createAcceptSelfSignedCertificateClient()){

            HttpGet httpGet = new HttpGet(ENDPOINT_URL);

            if(token == null){
                login();
            }

            httpGet.addHeader("Authorization", "Bearer " + token);

            CloseableHttpResponse response = client.execute(httpGet);

            if (response.getStatusLine().getStatusCode() == 404 && loginAttempts < 5) {
                login();
                download();
            } else if (response.getStatusLine().getStatusCode() == 200) {
                final TypeReference dtoListTypeRef = new TypeReference<List<FunctionDto>>() {};
                String result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                return OBJECT_MAPPER.readValue(result, dtoListTypeRef);
            } else {
                LOG.error("Cannot retrieve functions from meveo");
            }

            response.close();
        } catch (IOException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            LOG.error("Error while retrieving functions from meveo : ", e);
        }

        return null;
    }

    private static void refreshTokenThread(){
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        if(refreshTask != null){
            refreshTask.cancel(false);
        }
        Runnable task = () -> {
            loginTimeout = getRefreshToken();
            refreshTokenThread();
        };
        refreshTask = scheduler.schedule(task, loginTimeout, TimeUnit.SECONDS);
    }

    private static long getRefreshToken() {
        try (CloseableHttpClient client = createAcceptSelfSignedCertificateClient()){

            HttpPost post = new HttpPost(LOGGING_URL);

            ArrayList<NameValuePair> postParameters = new ArrayList<>();
            postParameters.add(new BasicNameValuePair("client_id", CLIENT_ID));
            postParameters.add(new BasicNameValuePair("grant_type", "refresh_token"));
            postParameters.add(new BasicNameValuePair("refresh_token", refresh_token));
            postParameters.add(new BasicNameValuePair("client_secret", SECRET));

            post.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
            CloseableHttpResponse response = client.execute(post);

            if (response.getStatusLine().getStatusCode() == 200) {
                final TypeReference mapTypeRef = new TypeReference<Map<String, String>>() {};
                Map<String, String> responseBody = OBJECT_MAPPER.readValue(response.getEntity().getContent(), mapTypeRef);
                token = responseBody.get("access_token");
                refresh_token = responseBody.get("refresh_token");
                return Long.parseLong(responseBody.get("expires_in"));
            }else {
                String responseString = OBJECT_MAPPER.readValue(response.getEntity().getContent(), String.class);
                LOG.error("Cannot refresh token. Server answered with {} ", responseString);
            }

            response.close();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException | IOException e) {
            LOG.error("Cannot log in", e);
        }

        return 3600;
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
}
