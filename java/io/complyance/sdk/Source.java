package io.complyance.sdk;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Source {
    private final String name;
    private final String version;
    private final SourceType type; // Optional, not persisted
    
    // Remove id field - no longer needed
    
    @JsonCreator
    public Source(@JsonProperty("name") String name, @JsonProperty("version") String version, @JsonProperty("type") SourceType type) {
        // Allow empty strings for optional source references (e.g., MAPPING purpose)
        this.name = (name == null) ? "" : name.trim();
        this.version = (version == null) ? "" : version.trim();
        this.type = type; // Can be null
    }
    
    // Legacy constructor for backward compatibility
    @Deprecated
    public Source(SourceType type, String id, String name, String version) {
        this(name, version, type);
    }
    
    // Builder pattern
    public static class Builder {
        private String name;
        private String version;
        private SourceType type;
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder version(String version) {
            this.version = version;
            return this;
        }
        
        public Builder type(SourceType type) {
            this.type = type;
            return this;
        }
        
        public Source build() {
            return new Source(name, version, type);
        }
    }
    
    // Getters
    @JsonProperty("name")
    public String getName() { return name; }
    
    @JsonProperty("version")
    public String getVersion() { return version; }
    
    @JsonProperty("type")
    public String getType() { 
        return (type == null) ? "" : type.toString(); 
    }
    
    // Get the actual SourceType enum for internal use (not serialized)
    @JsonIgnore
    public SourceType getSourceTypeEnum() { 
        return type; 
    }
    
    // Legacy getter for backward compatibility
    @Deprecated
    public String getId() { 
        // Return a computed ID based on name and version for backward compatibility
        return name + ":" + version; 
    }
    
    // Identity methods
    public String getIdentity() {
        return name + ":" + version;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Source source = (Source) obj;
        return Objects.equals(name, source.name) && Objects.equals(version, source.version);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, version);
    }
    
    @Override
    public String toString() {
        return "Source{name='" + name + "', version='" + version + "', type=" + type + "}";
    }
}