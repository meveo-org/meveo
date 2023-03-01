/**
 *
 */
package org.meveo.model.admin;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.*;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.meveo.model.BusinessCFEntity;
import org.meveo.model.CustomFieldEntity;
import org.meveo.model.ModuleItem;
import org.meveo.model.ModuleItemOrder;
import org.meveo.model.persistence.JsonTypes;
import org.meveo.security.PasswordUtils;

@Entity
@Table(name = "mv_credential")
@GenericGenerator(name = "ID_GENERATOR", strategy = "increment")
@CustomFieldEntity(cftCodePrefix = "CREDENTIAL")
@ModuleItem(value = "MvCredential", path = "credentials")
@ModuleItemOrder(1)
public class MvCredential extends BusinessCFEntity {

    private static final long serialVersionUID = 3133165825335371795L;

    @Column(name = "api_key")
    private String apiKey;

    @Column(name = "header_value")
    private String headerValue;

    @Column(name = "public_key")
    private String publicKey;

    @Column(name = "token_expiry", columnDefinition = "TIMESTAMP")
    private Instant tokenExpiry;

    @Column(name = "last_connection", columnDefinition = "TIMESTAMP")
    private Instant lastConnection;

    @Column(name = "token")
    private String token;

    @Column(name = "private_key")
    private String privateKey;

    @Column(name = "domain_name")
    private String domainName;

    @Column(name = "password")
    private String passwordSecret;

    @Column(name = "authentication_type")
    @Enumerated(EnumType.STRING)
    private AuthenticationType authenticationType;

    @Column(name = "header_key")
    private String headerKey;

    @Column(name = "credit")
    private Long credit;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "status")
    private String status;

    @Column(name = "username")
    private String username;

    @Type(type = JsonTypes.JSON)
    @Column(name = "extra_parameters", columnDefinition = "text")
    private Map<String, String> extraParameters = new HashMap<>();

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getHeaderValue() {
        return headerValue;
    }

    public void setHeaderValue(String headerValue) {
        this.headerValue = headerValue;
    }

    public Instant getTokenExpiry() {
        return tokenExpiry;
    }

    public void setTokenExpiry(Instant tokenExpiry) {
        this.tokenExpiry = tokenExpiry;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public Instant getLastConnection() {
        return lastConnection;
    }

    public void setLastConnection(Instant lastConnection) {
        this.lastConnection = lastConnection;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPassword() {
        if (!StringUtils.isEmpty(this.passwordSecret)) {
            String salt = PasswordUtils.getSalt(code, username, apiKey, token);
            return PasswordUtils.decrypt(salt, this.passwordSecret);
        }
        return null;
    }

    public void setPassword(String password) {
        if (!StringUtils.isEmpty(password)) {
            String salt = PasswordUtils.getSalt(code, username, apiKey, token);
            passwordSecret = PasswordUtils.encrypt(salt, password);
        }
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(AuthenticationType authenticationType) {
        this.authenticationType = authenticationType;
    }

    public String getHeaderKey() {
        return headerKey;
    }

    public void setHeaderKey(String headerKey) {
        this.headerKey = headerKey;
    }

    public Long getCredit() {
        return credit;
    }

    public void setCredit(Long credit) {
        this.credit = credit;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Map<String, String> getExtraParameters() {
        return extraParameters;
    }

    public void setExtraParameters(Map<String, String> extraParameters) {
        this.extraParameters = extraParameters;
    }

    public enum AuthenticationType {
        API_KEY,
        HEADER,
        HTTP_BASIC,
        OAUTH2,
        SSH
    }
}
