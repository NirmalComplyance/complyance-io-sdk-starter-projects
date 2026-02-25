package io.complyance.sdk;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Main response class matching the backend UnifyResponseDto structure
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnifyResponse {
    private String status;
    private String message;
    private UnifyResponseData data;
    private Object metadata;
    private ErrorDetail error;

    // Getters and setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UnifyResponseData getData() {
        return data;
    }

    public void setData(UnifyResponseData data) {
        this.data = data;
    }

    public Object getMetadata() {
        return metadata;
    }

    public void setMetadata(Object metadata) {
        this.metadata = metadata;
    }

    public ErrorDetail getError() {
        return error;
    }

    public void setError(ErrorDetail error) {
        this.error = error;
    }

    // Helper methods
    public boolean isSuccess() {
        return "success".equals(status);
    }

    public boolean hasError() {
        return error != null || "error".equals(status);
    }

    /**
     * Main data container matching backend structure
     */
    public static class UnifyResponseData {
        private SourceResponse source;
        private PayloadResponse payload;
        private TemplateResponse template;
        private LogicalDocumentTypeResponse logicalDocumentType; // New field for logical document type info
        private ConversionResponse conversion; // Optional - only for purpose="invoicing"
        private DocumentResponse document; // Optional - only for purpose="invoicing"
        private ValidationResponse validation; // Optional - only for purpose="invoicing"
        private SubmissionResponse submission; // Optional - only for purpose="invoicing"
        private ProcessingResponse processing;
        private DestinationsResponse destinations; // Destination configuration and validation

        // Getters and setters
        public SourceResponse getSource() {
            return source;
        }

        public void setSource(SourceResponse source) {
            this.source = source;
        }

        public PayloadResponse getPayload() {
            return payload;
        }

        public void setPayload(PayloadResponse payload) {
            this.payload = payload;
        }

        public TemplateResponse getTemplate() {
            return template;
        }

        public void setTemplate(TemplateResponse template) {
            this.template = template;
        }

        public LogicalDocumentTypeResponse getLogicalDocumentType() {
            return logicalDocumentType;
        }

        public void setLogicalDocumentType(LogicalDocumentTypeResponse logicalDocumentType) {
            this.logicalDocumentType = logicalDocumentType;
        }

        public ConversionResponse getConversion() {
            return conversion;
        }

        public void setConversion(ConversionResponse conversion) {
            this.conversion = conversion;
        }

        public DocumentResponse getDocument() {
            return document;
        }

        public void setDocument(DocumentResponse document) {
            this.document = document;
        }

        public ValidationResponse getValidation() {
            return validation;
        }

        public void setValidation(ValidationResponse validation) {
            this.validation = validation;
        }

        public SubmissionResponse getSubmission() {
            return submission;
        }

        public void setSubmission(SubmissionResponse submission) {
            this.submission = submission;
        }

        public ProcessingResponse getProcessing() {
            return processing;
        }

        public void setProcessing(ProcessingResponse processing) {
            this.processing = processing;
        }

        public DestinationsResponse getDestinations() {
            return destinations;
        }

        public void setDestinations(DestinationsResponse destinations) {
            this.destinations = destinations;
        }
    }

    /**
     * Logical Document Type Response - Information about logical document type processing
     */
    public static class LogicalDocumentTypeResponse {
        private String originalType;
        private MetaConfigFlags metaConfig;

        public LogicalDocumentTypeResponse() {}

        public LogicalDocumentTypeResponse(String originalType, MetaConfigFlags metaConfig) {
            this.originalType = originalType;
            this.metaConfig = metaConfig;
        }

        public String getOriginalType() {
            return originalType;
        }

        public void setOriginalType(String originalType) {
            this.originalType = originalType;
        }

        public MetaConfigFlags getMetaConfig() {
            return metaConfig;
        }

        public void setMetaConfig(MetaConfigFlags metaConfig) {
            this.metaConfig = metaConfig;
        }
    }

    /**
     * Meta Configuration Flags - Flags set by logical document type processing
     */
    public static class MetaConfigFlags {
        private Boolean isExport;
        private Boolean isSelfBilled;
        private Boolean isThirdParty;
        private Boolean isNominalSupply;
        private Boolean isSummary;

        public MetaConfigFlags() {}

        public MetaConfigFlags(Boolean isExport, Boolean isSelfBilled, Boolean isThirdParty, 
                             Boolean isNominalSupply, Boolean isSummary) {
            this.isExport = isExport;
            this.isSelfBilled = isSelfBilled;
            this.isThirdParty = isThirdParty;
            this.isNominalSupply = isNominalSupply;
            this.isSummary = isSummary;
        }

        public Boolean getIsExport() {
            return isExport;
        }

        public void setIsExport(Boolean isExport) {
            this.isExport = isExport;
        }

        public Boolean getIsSelfBilled() {
            return isSelfBilled;
        }

        public void setIsSelfBilled(Boolean isSelfBilled) {
            this.isSelfBilled = isSelfBilled;
        }

        public Boolean getIsThirdParty() {
            return isThirdParty;
        }

        public void setIsThirdParty(Boolean isThirdParty) {
            this.isThirdParty = isThirdParty;
        }

        public Boolean getIsNominalSupply() {
            return isNominalSupply;
        }

        public void setIsNominalSupply(Boolean isNominalSupply) {
            this.isNominalSupply = isNominalSupply;
        }

        public Boolean getIsSummary() {
            return isSummary;
        }

        public void setIsSummary(Boolean isSummary) {
            this.isSummary = isSummary;
        }

        @Override
        public String toString() {
            return "MetaConfigFlags{" +
                    "isExport=" + isExport +
                    ", isSelfBilled=" + isSelfBilled +
                    ", isThirdParty=" + isThirdParty +
                    ", isNominalSupply=" + isNominalSupply +
                    ", isSummary=" + isSummary +
                    '}';
        }
    }

    /**
     * Destinations: Configuration and validation of delivery destinations
     */
    public static class DestinationsResponse {
        private Integer count;
        private boolean stored;
        private List<String> types;
        private Integer valid;

        // Getters and setters
        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public boolean isStored() {
            return stored;
        }

        public void setStored(boolean stored) {
            this.stored = stored;
        }

        public List<String> getTypes() {
            return types;
        }

        public void setTypes(List<String> types) {
            this.types = types;
        }

        public Integer getValid() {
            return valid;
        }

        public void setValid(Integer valid) {
            this.valid = valid;
        }
    }

    /**
     * Step 1: Source and Payload Storage
     */
    public static class SourceResponse {
        private String sourceId;
        private String sourceid; // API returns lowercase version
        private String type;
        private String name;
        private String version;
        private boolean created;
        private String id;

        // Getters and setters
        public String getSourceId() {
            return sourceId;
        }

        public void setSourceId(String sourceId) {
            this.sourceId = sourceId;
        }

        public String getSourceid() {
            return sourceid;
        }

        public void setSourceid(String sourceid) {
            this.sourceid = sourceid;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public boolean isCreated() {
            return created;
        }

        public void setCreated(boolean created) {
            this.created = created;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    /**
     * Analysis: Payload analysis information
     */
    public static class AnalysisResponse {
        private boolean hasNested;
        private List<String> keys;
        private Integer size;

        // Getters and setters
        public boolean isHasNested() {
            return hasNested;
        }

        public void setHasNested(boolean hasNested) {
            this.hasNested = hasNested;
        }

        public List<String> getKeys() {
            return keys;
        }

        public void setKeys(List<String> keys) {
            this.keys = keys;
        }

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }
    }

    public static class PayloadResponse {
        private String payloadId;
        private String documentType;
        private String country;
        private String environment;
        private String storedAt;
        private AnalysisResponse analysis;

        // Getters and setters
        public String getPayloadId() {
            return payloadId;
        }

        public void setPayloadId(String payloadId) {
            this.payloadId = payloadId;
        }

        public String getDocumentType() {
            return documentType;
        }

        public void setDocumentType(String documentType) {
            this.documentType = documentType;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getEnvironment() {
            return environment;
        }

        public void setEnvironment(String environment) {
            this.environment = environment;
        }

        public String getStoredAt() {
            return storedAt;
        }

        public void setStoredAt(String storedAt) {
            this.storedAt = storedAt;
        }

        public AnalysisResponse getAnalysis() {
            return analysis;
        }

        public void setAnalysis(AnalysisResponse analysis) {
            this.analysis = analysis;
        }
    }

    /**
     * Step 2: Template Mapping (always included)
     */
    public static class TemplateResponse {
        private String templateId;
        private String templateName;
        private boolean mappingCompleted;
        private Integer totalMandatoryFields;
        private Integer mappedMandatoryFields;
        private Boolean aiMappingApplied;

        // Getters and setters
        public String getTemplateId() {
            return templateId;
        }

        public void setTemplateId(String templateId) {
            this.templateId = templateId;
        }

        public String getTemplateName() {
            return templateName;
        }

        public void setTemplateName(String templateName) {
            this.templateName = templateName;
        }

        public boolean isMappingCompleted() {
            return mappingCompleted;
        }

        public void setMappingCompleted(boolean mappingCompleted) {
            this.mappingCompleted = mappingCompleted;
        }

        public Integer getTotalMandatoryFields() {
            return totalMandatoryFields;
        }

        public void setTotalMandatoryFields(Integer totalMandatoryFields) {
            this.totalMandatoryFields = totalMandatoryFields;
        }

        public Integer getMappedMandatoryFields() {
            return mappedMandatoryFields;
        }

        public void setMappedMandatoryFields(Integer mappedMandatoryFields) {
            this.mappedMandatoryFields = mappedMandatoryFields;
        }

        public Boolean getAiMappingApplied() {
            return aiMappingApplied;
        }

        public void setAiMappingApplied(Boolean aiMappingApplied) {
            this.aiMappingApplied = aiMappingApplied;
        }
    }

    /**
     * Step 3: Conversion (only for purpose="invoicing")
     */
    public static class ConversionResponse {
        private boolean success;
        private Object getsDocument;
        private Integer conversionTime;
        private List<String> errors;

        // Getters and setters
        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public Object getGetsDocument() {
            return getsDocument;
        }

        public void setGetsDocument(Object getsDocument) {
            this.getsDocument = getsDocument;
        }

        public Integer getConversionTime() {
            return conversionTime;
        }

        public void setConversionTime(Integer conversionTime) {
            this.conversionTime = conversionTime;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void setErrors(List<String> errors) {
            this.errors = errors;
        }
    }

    /**
     * Step 4: Document Creation (only for purpose="invoicing")
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DocumentResponse {
        @JsonProperty("documentId")
        private String documentId;
        @JsonProperty("documentType")
        private String documentType;
        @JsonProperty("createdAt")
        private String createdAt;
        @JsonProperty("metadata")
        private Map<String, Object> metadata;
        @JsonProperty("status")
        private String status; // Add status field

        // Getters and setters
        public String getDocumentId() {
            return documentId;
        }

        public void setDocumentId(String documentId) {
            this.documentId = documentId;
        }

        public String getDocumentType() {
            return documentType;
        }

        public void setDocumentType(String documentType) {
            this.documentType = documentType;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    /**
     * Step 5: Validation (only for purpose="invoicing")
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ValidationResponse {
        private boolean overallSuccess;
        private List<String> methods;
        private List<ValidationError> errors;
        private String validatedAt;
        @JsonProperty("success")
        private Boolean success;

        // Getters and setters
        public boolean isOverallSuccess() {
            return overallSuccess;
        }

        public void setOverallSuccess(boolean overallSuccess) {
            this.overallSuccess = overallSuccess;
        }

        public List<String> getMethods() {
            return methods;
        }

        public void setMethods(List<String> methods) {
            this.methods = methods;
        }

        public List<ValidationError> getErrors() {
            return errors;
        }

        public void setErrors(List<ValidationError> errors) {
            this.errors = errors;
        }

        public String getValidatedAt() {
            return validatedAt;
        }

        public void setValidatedAt(String validatedAt) {
            this.validatedAt = validatedAt;
        }

        public Boolean getSuccess() {
            return success;
        }

        public void setSuccess(Boolean success) {
            this.success = success;
        }

        public static class ValidationError {
            private String method;
            private String message;
            private String code;
            private List<String> path; // Add path field for validation error location

            // Getters and setters
            public String getMethod() {
                return method;
            }

            public void setMethod(String method) {
                this.method = method;
            }

            public String getMessage() {
                return message;
            }

            public void setMessage(String message) {
                this.message = message;
            }

            public String getCode() {
                return code;
            }

            public void setCode(String code) {
                this.code = code;
            }

            public List<String> getPath() {
                return path;
            }

            public void setPath(List<String> path) {
                this.path = path;
            }
        }
    }

    /**
     * Step 6: Government Submission (only for purpose="invoicing")
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubmissionResponse {
        private String submissionId;
        private String country;
        private String authority;
        private String status; // 'submitted' | 'accepted' | 'rejected' | 'failed'
        private String submittedAt;
        private SubmissionResponseData response;
        private Object governmentResponse; // Accept governmentResponse from backend
        private List<SubmissionError> errors;

        // Getters and setters
        public String getSubmissionId() {
            return submissionId;
        }

        public void setSubmissionId(String submissionId) {
            this.submissionId = submissionId;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getAuthority() {
            return authority;
        }

        public void setAuthority(String authority) {
            this.authority = authority;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getSubmittedAt() {
            return submittedAt;
        }

        public void setSubmittedAt(String submittedAt) {
            this.submittedAt = submittedAt;
        }

        public SubmissionResponseData getResponse() {
            return response;
        }

        public void setResponse(SubmissionResponseData response) {
            this.response = response;
        }

        public Object getGovernmentResponse() {
            return governmentResponse;
        }

        public void setGovernmentResponse(Object governmentResponse) {
            this.governmentResponse = governmentResponse;
        }

        public List<SubmissionError> getErrors() {
            return errors;
        }

        public void setErrors(List<SubmissionError> errors) {
            this.errors = errors;
        }

        // Helper methods
        public boolean isAccepted() {
            return "accepted".equals(status);
        }

        public boolean isRejected() {
            return "rejected".equals(status);
        }

        public boolean isFailed() {
            return "failed".equals(status);
        }

        public boolean isSubmitted() {
            return "submitted".equals(status);
        }

        public static class SubmissionResponseData {
            private String clearanceStatus;
            private String uuid;
            private String hash;
            private String qrCode;
            private String submissionNumber;

            // Getters and setters
            public String getClearanceStatus() {
                return clearanceStatus;
            }

            public void setClearanceStatus(String clearanceStatus) {
                this.clearanceStatus = clearanceStatus;
            }

            public String getUuid() {
                return uuid;
            }

            public void setUuid(String uuid) {
                this.uuid = uuid;
            }

            public String getHash() {
                return hash;
            }

            public void setHash(String hash) {
                this.hash = hash;
            }

            public String getQrCode() {
                return qrCode;
            }

            public void setQrCode(String qrCode) {
                this.qrCode = qrCode;
            }

            public String getSubmissionNumber() {
                return submissionNumber;
            }

            public void setSubmissionNumber(String submissionNumber) {
                this.submissionNumber = submissionNumber;
            }
        }

        public static class SubmissionError {
            private String code;
            private String message;

            // Getters and setters
            public String getCode() {
                return code;
            }

            public void setCode(String code) {
                this.code = code;
            }

            public String getMessage() {
                return message;
            }

            public void setMessage(String message) {
                this.message = message;
            }
        }
    }

    /**
     * Processing metadata
     */
    public static class ProcessingResponse {
        private String purpose; // 'invoicing' | 'mapping'
        private List<String> completedSteps;
        private Integer totalProcessingTime;
        private String completedAt;
        private String processedAt;
        private String requestId;
        private String status;

        // Getters and setters
        public String getPurpose() {
            return purpose;
        }

        public void setPurpose(String purpose) {
            this.purpose = purpose;
        }

        public List<String> getCompletedSteps() {
            return completedSteps;
        }

        public void setCompletedSteps(List<String> completedSteps) {
            this.completedSteps = completedSteps;
        }

        public Integer getTotalProcessingTime() {
            return totalProcessingTime;
        }

        public void setTotalProcessingTime(Integer totalProcessingTime) {
            this.totalProcessingTime = totalProcessingTime;
        }

        public String getCompletedAt() {
            return completedAt;
        }

        public void setCompletedAt(String completedAt) {
            this.completedAt = completedAt;
        }

        public String getProcessedAt() {
            return processedAt;
        }

        public void setProcessedAt(String processedAt) {
            this.processedAt = processedAt;
        }

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        // Helper methods
        public boolean isInvoicingPurpose() {
            return "invoicing".equals(purpose);
        }

        public boolean isMappingPurpose() {
            return "mapping".equals(purpose);
        }
    }
}