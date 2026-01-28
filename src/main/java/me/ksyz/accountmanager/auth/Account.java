/*
 * Decompiled with CFR 0.152.
 */
package me.ksyz.accountmanager.auth;

public class Account {
    private String refreshToken;
    private String accessToken;
    private String username;
    private long unban;
    private String clientId;
    private String scope;

    public Account(String refreshToken, String accessToken, String username, String clientId, String scope) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.unban = 0L;
        this.clientId = clientId;
        this.scope = scope;
    }

    public Account(String refreshToken, String accessToken, String username, long unban, String clientId, String scope) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.unban = unban;
        this.clientId = clientId;
        this.scope = scope;
    }

    public String getClientId() {
        return this.clientId;
    }

    public String getScope() {
        return this.scope;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public String getUsername() {
        return this.username;
    }

    public long getUnban() {
        return this.unban;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUnban(long unban) {
        this.unban = unban;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}

