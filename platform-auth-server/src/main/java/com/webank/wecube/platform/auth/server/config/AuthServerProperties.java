package com.webank.wecube.platform.auth.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * 
 * @author gavin
 *
 */
@ConfigurationProperties(prefix = "platform.auth.server")
public class AuthServerProperties {
    private String privateKey;
    private String publicKey;
    private String dbInitStrategy = "update";

    @NestedConfigurationProperty
    private JwtTokenProperties jwtToken = new JwtTokenProperties();

    @NestedConfigurationProperty
    private AppConfigProperties config = new AppConfigProperties();

    public JwtTokenProperties getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(JwtTokenProperties jwtToken) {
        this.jwtToken = jwtToken;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getDbInitStrategy() {
        return dbInitStrategy;
    }

    public void setDbInitStrategy(String dbInitStrategy) {
        this.dbInitStrategy = dbInitStrategy;
    }

    public AppConfigProperties getConfig() {
        return config;
    }

    public void setConfig(AppConfigProperties config) {
        this.config = config;
    }

    public static class JwtTokenProperties {
        private int userRefreshToken = 30;
        private int userAccessToken = 5;
        private int subSystemRefreshToken = 1440;
        private int subSystemAccessToken = 60;

        private String signingKey = "Platform+Auth+Server+Secret";

        public int getUserRefreshToken() {
            return userRefreshToken;
        }

        public void setUserRefreshToken(int userRefreshToken) {
            this.userRefreshToken = userRefreshToken;
        }

        public int getUserAccessToken() {
            return userAccessToken;
        }

        public void setUserAccessToken(int userAccessToken) {
            this.userAccessToken = userAccessToken;
        }

        public int getSubSystemRefreshToken() {
            return subSystemRefreshToken;
        }

        public void setSubSystemRefreshToken(int subSystemRefreshToken) {
            this.subSystemRefreshToken = subSystemRefreshToken;
        }

        public int getSubSystemAccessToken() {
            return subSystemAccessToken;
        }

        public void setSubSystemAccessToken(int subSystemAccessToken) {
            this.subSystemAccessToken = subSystemAccessToken;
        }

        public String getSigningKey() {
            return signingKey;
        }

        public void setSigningKey(String signingKey) {
            this.signingKey = signingKey;
        }

        @Override
        public String toString() {
            return "userRefreshToken=" + userRefreshToken + ", userAccessToken=" + userAccessToken
                    + ", subSystemRefreshToken=" + subSystemRefreshToken + ", subSystemAccessToken="
                    + subSystemAccessToken + "";
        }

    }

    public static class AppConfigProperties {
        private String propertyRsaKey = null;
        private String propertyRsaPubKey = null;

        public String getPropertyRsaKey() {
            return propertyRsaKey;
        }

        public void setPropertyRsaKey(String propertyRsaKey) {
            this.propertyRsaKey = propertyRsaKey;
        }

        public String getPropertyRsaPubKey() {
            return propertyRsaPubKey;
        }

        public void setPropertyRsaPubKey(String propertyRsaPubKey) {
            this.propertyRsaPubKey = propertyRsaPubKey;
        }
    }

}
