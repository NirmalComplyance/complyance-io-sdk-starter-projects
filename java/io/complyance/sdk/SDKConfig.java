package io.complyance.sdk;

import java.util.ArrayList;
import java.util.List;

public class SDKConfig {
    private String apiKey;
    private Environment environment;
    private List<Source> sources;
    private RetryConfig retryConfig;
    private boolean autoGenerateTaxDestination = true;
    private String correlationId;

    public SDKConfig(String apiKey, Environment environment, List<Source> sources) {
        this.apiKey = apiKey;
        this.environment = environment;
        this.sources = sources;
        this.retryConfig = RetryConfig.defaultConfig(); // Default retry configuration
    }

    public SDKConfig(String apiKey, Environment environment, List<Source> sources, RetryConfig retryConfig) {
        this.apiKey = apiKey;
        this.environment = environment;
        this.sources = sources;
        this.retryConfig = retryConfig != null ? retryConfig : RetryConfig.defaultConfig();
    }

    // Builder pattern
    public static class Builder {
        private String apiKey;
        private Environment environment = Environment.DEV;
        private List<Source> sources = new ArrayList<>();
        private RetryConfig retryConfig;
        private boolean autoGenerateTaxDestination = true;
        private String correlationId;
        
        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }
        
        public Builder environment(Environment environment) {
            this.environment = environment;
            return this;
        }
        
        public Builder sources(List<Source> sources) {
            this.sources = sources != null ? new ArrayList<>(sources) : new ArrayList<>();
            return this;
        }
        
        public Builder retryConfig(RetryConfig retryConfig) {
            this.retryConfig = retryConfig;
            return this;
        }
        
        public Builder autoGenerateTaxDestination(boolean autoGenerate) {
            this.autoGenerateTaxDestination = autoGenerate;
            return this;
        }
        
        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }
        
        public SDKConfig build() {
            return new SDKConfig(apiKey, environment, sources, retryConfig);
        }
    }

    public String getApiKey() {
        return apiKey;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public List<Source> getSources() {
        return sources;
    }

    public RetryConfig getRetryConfig() {
        return retryConfig;
    }
    
    public boolean isAutoGenerateTaxDestination() {
        return autoGenerateTaxDestination;
    }
    
    public String getCorrelationId() {
        return correlationId;
    }

    public void setRetryConfig(RetryConfig retryConfig) {
        this.retryConfig = retryConfig != null ? retryConfig : RetryConfig.defaultConfig();
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public void setSources(List<Source> sources) {
        this.sources = sources;
    }
    
    public void setAutoGenerateTaxDestination(boolean autoGenerateTaxDestination) {
        this.autoGenerateTaxDestination = autoGenerateTaxDestination;
    }
    
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
}