package com.caspian.pichak.model.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "oauth_client_details")
public class OauthClientDetails {
    @Id
    @Column(name = "ID", unique = true, nullable = false, columnDefinition = "NUMBER(10)")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "client_id")
    private String clientId;
    @Column(name = "client_name")
    private String clientName;
    @Column(name = "resource_ids")
    private String resourceIds;
    @Column(name = "client_secret")
    private String clientSecret;
    @Column(name = "scope")
    private String scope;
    @Column(name = "authorized_grant_types")
    private String authorizedGrantTypes;
    @Column(name = "web_server_redirect_uri")
    private String webServerRedirectUri;
    @Column(name = "authorities")
    private String authorities;
    @Column(name = "access_token_validity", columnDefinition = "NUMBER(11)")
    private Integer accessTokenValidity;
    @Column(name = "refresh_token_validity", columnDefinition = "NUMBER(11)")
    private Integer refreshTokenValidity;
    @Column(name = "additional_information", length = 4096)
    private String additionalInformation;
    @Column(name = "autoapprove", columnDefinition = "NUMBER(4)")
    private Integer autoapprove;
    @Column(name = "uuid", columnDefinition = "NVARCHAR2(400)")
    private String uuid;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created")
    private Date created;
    @Column(columnDefinition = "NUMBER(4) default 1")
    private Boolean enabled;
    @Transient
    private String[] scopes;
    @Transient
    private String[] grantTypes;
    @Transient
    private String ownerEmail;

    public Integer getId() {
        return this.id;
    }

    public String getClientId() {
        return this.clientId;
    }

    public String getClientName() {
        return this.clientName;
    }

    public String getResourceIds() {
        return this.resourceIds;
    }

    public String getClientSecret() {
        return this.clientSecret;
    }

    public String getScope() {
        return this.scope;
    }

    public String getAuthorizedGrantTypes() {
        return this.authorizedGrantTypes;
    }

    public String getWebServerRedirectUri() {
        return this.webServerRedirectUri;
    }

    public String getAuthorities() {
        return this.authorities;
    }

    public Integer getAccessTokenValidity() {
        return this.accessTokenValidity;
    }

    public Integer getRefreshTokenValidity() {
        return this.refreshTokenValidity;
    }

    public String getAdditionalInformation() {
        return this.additionalInformation;
    }

    public Integer getAutoapprove() {
        return this.autoapprove;
    }

    public String getUuid() {
        return this.uuid;
    }

    public Date getCreated() {
        return this.created;
    }

    public Boolean getEnabled() {
        return this.enabled;
    }

    public String[] getScopes() {
        return this.scopes;
    }

    public String[] getGrantTypes() {
        return this.grantTypes;
    }

    public String getOwnerEmail() {
        return this.ownerEmail;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    public void setClientName(final String clientName) {
        this.clientName = clientName;
    }

    public void setResourceIds(final String resourceIds) {
        this.resourceIds = resourceIds;
    }

    public void setClientSecret(final String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setScope(final String scope) {
        this.scope = scope;
    }

    public void setAuthorizedGrantTypes(final String authorizedGrantTypes) {
        this.authorizedGrantTypes = authorizedGrantTypes;
    }

    public void setWebServerRedirectUri(final String webServerRedirectUri) {
        this.webServerRedirectUri = webServerRedirectUri;
    }

    public void setAuthorities(final String authorities) {
        this.authorities = authorities;
    }

    public void setAccessTokenValidity(final Integer accessTokenValidity) {
        this.accessTokenValidity = accessTokenValidity;
    }

    public void setRefreshTokenValidity(final Integer refreshTokenValidity) {
        this.refreshTokenValidity = refreshTokenValidity;
    }

    public void setAdditionalInformation(final String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    public void setAutoapprove(final Integer autoapprove) {
        this.autoapprove = autoapprove;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public void setEnabled(final Boolean enabled) {
        this.enabled = enabled;
    }

    public void setScopes(final String[] scopes) {
        this.scopes = scopes;
    }

    public void setGrantTypes(final String[] grantTypes) {
        this.grantTypes = grantTypes;
    }

    public void setOwnerEmail(final String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }
}