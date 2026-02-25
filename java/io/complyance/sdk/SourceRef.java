package io.complyance.sdk;

import java.util.Objects;

public final class SourceRef {
    private final String name;
    private final String version;
    
    public SourceRef(String name, String version) {
        // Allow empty strings for optional source references (e.g., MAPPING purpose)
        this.name = (name == null) ? "" : name.trim();
        this.version = (version == null) ? "" : version.trim();
    }
    
    public String getName() { return name; }
    public String getVersion() { return version; }
    
    public String getIdentity() {
        return name + ":" + version;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SourceRef sourceRef = (SourceRef) obj;
        return Objects.equals(name, sourceRef.name) && Objects.equals(version, sourceRef.version);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, version);
    }
    
    @Override
    public String toString() {
        return "SourceRef{name='" + name + "', version='" + version + "'}";
    }
}
