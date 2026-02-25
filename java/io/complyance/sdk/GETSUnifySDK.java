package io.complyance.sdk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Main entry point for the GETS Unify Java SDK.
 */
public class GETSUnifySDK {
    private static final Logger logger = LoggerFactory.getLogger(GETSUnifySDK.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<Map<String, Object>>() {
    };
    private static SDKConfig config;
    private static APIClient apiClient;
    private static PersistentQueueManager queueManager;

    /**
     * Configure the SDK with API key, environment, and sources.
     */
    public static void configure(SDKConfig sdkConfig) {
        config = sdkConfig;
        if (sdkConfig != null) {
            // Validate country restrictions for production environments
            validateEnvironmentCountryRestrictions(sdkConfig.getEnvironment());

            apiClient = new APIClient(
                    sdkConfig.getApiKey(),
                    sdkConfig.getEnvironment(),
                    sdkConfig.getRetryConfig());
            // Initialize PersistentQueueManager for handling failed submissions with shared circuit breaker
            queueManager = new PersistentQueueManager(sdkConfig.getApiKey(),
                    sdkConfig.getEnvironment() == Environment.LOCAL,
                    apiClient.getCircuitBreaker());
        } else {
            ErrorDetail error = new ErrorDetail(ErrorCode.MISSING_FIELD, "SDKConfig is required",
                    "Call GETSUnifySDK.configure() with a valid SDKConfig.");
            throw new RuntimeException(error.toString());
        }
    }

    /**
     * Validate country restrictions based on environment
     */
    private static void validateEnvironmentCountryRestrictions(Environment environment) {
        if (environment == Environment.SANDBOX ||
                environment == Environment.SIMULATION ||
                environment == Environment.PRODUCTION) {

            // For production environments, only SA, MY, BE, and DE are allowed
            // This validation happens at configuration time, not at request time
            logger.info("Production environment detected: {}. Only SA, MY, BE, and DE countries will be allowed.", environment);
        } else {
            // For development environments, all countries are allowed
            logger.info("Development environment detected: {}. All countries are allowed.", environment);
        }
    }

    /**
     * Submit a payload to the GETS Unify API.
     * 
     * @param clientPayloadJson The raw JSON payload.
     * @param sourceId          The source ID.
     * @param country           The country.
     * @param documentType      The document type.
     * @return SubmissionResponse
     * @throws SDKException if required fields are missing or source is invalid.
     */
    public static SubmissionResponse submitPayload(
            String clientPayloadJson,
            String sourceId,
            Country country,
            DocumentType documentType) throws SDKException {
        if (config == null) {
            throw new SDKException(new ErrorDetail(ErrorCode.MISSING_FIELD, "SDK not configured",
                    "Call GETSUnifySDK.configure() first."));
        }
        if (clientPayloadJson == null || clientPayloadJson.trim().isEmpty()) {
            throw new SDKException(
                    new ErrorDetail(ErrorCode.MISSING_FIELD, "Payload is required", "Provide a valid JSON payload."));
        }
        if (sourceId == null || sourceId.trim().isEmpty()) {
            throw new SDKException(
                    new ErrorDetail(ErrorCode.MISSING_FIELD, "Source ID is required", "Provide a valid source ID."));
        }
        if (country == null) {
            throw new SDKException(
                    new ErrorDetail(ErrorCode.MISSING_FIELD, "Country is required", "Provide a valid country."));
        }
        if (documentType == null) {
            throw new SDKException(new ErrorDetail(ErrorCode.MISSING_FIELD, "Document type is required",
                    "Provide a valid document type."));
        }
        Source source = config.getSources().stream()
                .filter(s -> s.getId().equals(sourceId))
                .findFirst()
                .orElse(null);
        if (source == null) {
            throw new SDKException(new ErrorDetail(ErrorCode.INVALID_SOURCE, "Source not found",
                    "Check the source ID or configure the source."));
        }

        // Validate country restrictions for current environment
        validateCountryForEnvironment(country, config.getEnvironment());

        return apiClient.sendPayload(clientPayloadJson, source, country, documentType);
    }

    /**
     * Get the status of a submission by its ID.
     */
    public static SubmissionStatus getStatus(String submissionId) {
        // Stub: In a real implementation, this would query the API or local cache.
        return SubmissionStatus.QUEUED;
    }

    /**
     * Get the circuit breaker instance for testing/debugging purposes
     * @return CircuitBreaker instance or null if SDK not configured
     */
    public static CircuitBreaker getCircuitBreaker() {
        if (apiClient == null) {
            return null;
        }
        return apiClient.getCircuitBreaker();
    }

    /**
     * Get queue status and statistics
     */
    public static String getQueueStatus() {
        if (queueManager != null) {
            PersistentQueueManager.QueueStatus status = queueManager.getQueueStatus();
            return String.format("Persistent Queue Status: %s", status.toString());
        } else {
            return "Queue Manager is not initialized";
        }
    }

    public static PersistentQueueManager.QueueStatus getDetailedQueueStatus() {
        if (queueManager != null) {
            return queueManager.getQueueStatus();
        } else {
            return new PersistentQueueManager.QueueStatus(0, 0, 0, 0, false);
        }
    }

    public static void retryFailedSubmissions() {
        if (queueManager != null) {
            queueManager.retryFailedSubmissions();
        }
    }

    public static void cleanupOldSuccessFiles(int daysToKeep) {
        if (queueManager != null) {
            queueManager.cleanupOldSuccessFiles(daysToKeep);
        }
    }

    /**
     * Clear all files from the queue (emergency cleanup)
     */
    public static void clearAllQueues() {
        if (queueManager != null) {
            queueManager.clearAllQueues();
        } else {
            logger.warn("Queue Manager is not initialized");
        }
    }

    /**
     * Clean up duplicate files across queue directories
     */
    public static void cleanupDuplicateFiles() {
        if (queueManager != null) {
            queueManager.cleanupDuplicateFiles();
        } else {
            logger.warn("Queue Manager is not initialized");
        }
    }

    public static void processPendingSubmissions() {
        if (queueManager != null) {
            queueManager.processPendingSubmissionsNow();
        }
    }

    /**
     * Process queued submissions before handling new requests
     */
    public static void processQueuedSubmissionsFirst() {
        if (queueManager != null) {
            logger.debug("Processing queued submissions first");
            queueManager.processPendingSubmissionsNow();
        }
    }

    /**
     * Push to Unify API using a stored UnifyRequest object directly
     * This is used for retrying queued submissions
     */
    public static UnifyResponse pushToUnify(UnifyRequest unifyRequest) throws SDKException {
        if (config == null) {
            throw new SDKException(new ErrorDetail(ErrorCode.MISSING_FIELD, "SDK not configured",
                    "Call GETSUnifySDK.configure() first."));
        }

        if (unifyRequest == null) {
            throw new SDKException(new ErrorDetail(ErrorCode.MISSING_FIELD, "UnifyRequest is required",
                    "Provide a valid UnifyRequest object."));
        }

        logger.debug("Processing stored UnifyRequest - ID: {}, Type: {}, Country: {}", 
                unifyRequest.getRequestId(), unifyRequest.getDocumentTypeString(), unifyRequest.getCountry());

        // Use APIClient to send the stored UnifyRequest directly
        APIClient apiClient = new APIClient(config.getApiKey(), config.getEnvironment(), config.getRetryConfig());
        return apiClient.sendUnifyRequest(unifyRequest);
    }

    /**
     * Push to Unify API with enum parameters and destinations
     * 
     * @deprecated Use pushToUnify(String sourceName, String sourceVersion, ...)
     *             instead
     */
    // @Deprecated
    // public static UnifyResponse pushToUnify(
    // String sourceId,
    // DocumentType documentType,
    // String country,
    // Operation operation,
    // Mode mode,
    // Purpose purpose,
    // Map<String, Object> payload,
    // List<Destination> destinations) throws SDKException {
    // if (config == null) {
    // throw new SDKException(new ErrorDetail(ErrorCode.MISSING_FIELD, "SDK not
    // configured",
    // "Call GETSUnifySDK.configure() first."));
    // }

    // // Resolve source object from sourceId
    // Source source = config.getSources().stream()
    // .filter(s -> s.getId().equals(sourceId))
    // .findFirst()
    // .orElse(null);
    // if (source == null) {
    // throw new SDKException(new ErrorDetail(ErrorCode.INVALID_SOURCE, "Source not
    // found",
    // "Check the source ID or configure the source."));
    // }

    // // Use the new implementation
    // return pushToUnify(source.getName(), source.getVersion(), documentType,
    // Country.valueOf(country.toUpperCase()), operation, mode, purpose, payload,
    // destinations);
    // }

    /**
     * Push to Unify API with enum parameters (backward compatibility -
     * auto-generates tax authority destination)
     * 
     * @deprecated Use pushToUnify(String sourceName, String sourceVersion, ...)
     *             instead
     */
    // @Deprecated
    // public static UnifyResponse pushToUnify(
    // String sourceId,
    // DocumentType documentType,
    // String country,
    // Operation operation,
    // Mode mode,
    // Purpose purpose,
    // Map<String, Object> payload) throws SDKException {

    // // Auto-generate tax authority destination based on country
    // List<Destination> destinations = Arrays.asList(
    // createTaxAuthorityDestination(country, documentType.toString()));

    // return pushToUnify(sourceId, documentType, country, operation, mode, purpose,
    // payload, destinations);
    // }

    /**
     * Push to Unify API with string parameters (for backward compatibility)
     * Converts strings to appropriate enum types
     * 
     * @deprecated Use pushToUnify(String sourceName, String sourceVersion, ...)
     *             instead
     */
    // @Deprecated
    // public static UnifyResponse pushToUnify(
    // String sourceId,
    // String documentType,
    // String country,
    // String operation,
    // String mode,
    // String purpose,
    // Map<String, Object> payload) throws SDKException {
    // try {
    // return pushToUnify(
    // sourceId,
    // DocumentType.fromString(documentType),
    // country,
    // Operation.fromString(operation),
    // Mode.fromString(mode),
    // Purpose.fromString(purpose),
    // payload);
    // } catch (IllegalArgumentException e) {
    // throw new SDKException(new ErrorDetail(ErrorCode.INVALID_ARGUMENT, "Invalid
    // enum value", e.getMessage()));
    // }
    // }

    /**
     * Push to Unify API with string parameters and destinations
     * 
     * @deprecated Use pushToUnify(String sourceName, String sourceVersion, ...)
     *             instead
     */
    // @Deprecated
    // public static UnifyResponse pushToUnify(
    // String sourceId,
    // String documentType,
    // String country,
    // String operation,
    // String mode,
    // String purpose,
    // Map<String, Object> payload,
    // List<Destination> destinations) throws SDKException {
    // try {
    // return pushToUnify(
    // sourceId,
    // DocumentType.fromString(documentType),
    // country,
    // Operation.fromString(operation),
    // Mode.fromString(mode),
    // Purpose.fromString(purpose),
    // payload,
    // destinations);
    // } catch (IllegalArgumentException e) {
    // throw new SDKException(new ErrorDetail(ErrorCode.INVALID_ARGUMENT, "Invalid
    // enum value", e.getMessage()));
    // }
    // }

    /**
     * Push to Unify API with explicit source name and version parameters.
     * This is the primary method for all workflows.
     */
    // public static UnifyResponse pushToUnify(
    // final String sourceName,
    // final String sourceVersion,
    // final DocumentType documentType,
    // final Country country,
    // final Operation operation,
    // final Mode mode,
    // final Purpose purpose,
    // final Map<String, Object> payload,
    // final List<Destination> destinations) throws SDKException {

    // if (config == null) {
    // throw new SDKException(new ErrorDetail(ErrorCode.MISSING_FIELD, "SDK not
    // configured",
    // "Call GETSUnifySDK.configure() first."));
    // }

    // // Validate required parameters
    // if (sourceName == null || sourceName.trim().isEmpty()) {
    // throw new SDKException(new ErrorDetail(ErrorCode.MISSING_FIELD, "Source name
    // is required",
    // "Provide a valid source name."));
    // }
    // if (sourceVersion == null || sourceVersion.trim().isEmpty()) {
    // throw new SDKException(new ErrorDetail(ErrorCode.MISSING_FIELD, "Source
    // version is required",
    // "Provide a valid source version."));
    // }
    // if (documentType == null) {
    // throw new SDKException(new ErrorDetail(ErrorCode.MISSING_FIELD, "Document
    // type is required",
    // "Provide a valid document type."));
    // }
    // if (country == null) {
    // throw new SDKException(new ErrorDetail(ErrorCode.MISSING_FIELD, "Country is
    // required",
    // "Provide a valid country."));
    // }
    // if (operation == null) {
    // throw new SDKException(new ErrorDetail(ErrorCode.MISSING_FIELD, "Operation is
    // required",
    // "Provide a valid operation."));
    // }
    // if (mode == null) {
    // throw new SDKException(new ErrorDetail(ErrorCode.MISSING_FIELD, "Mode is
    // required",
    // "Provide a valid mode."));
    // }
    // if (purpose == null) {
    // throw new SDKException(new ErrorDetail(ErrorCode.MISSING_FIELD, "Purpose is
    // required",
    // "Provide a valid purpose."));
    // }
    // if (payload == null) {
    // throw new SDKException(new ErrorDetail(ErrorCode.MISSING_FIELD, "Payload is
    // required",
    // "Provide a valid payload."));
    // }

    // // Validate country restrictions for current environment
    // validateCountryForEnvironment(country, config.getEnvironment());

    // // Create source reference
    // SourceRef sourceRef = new SourceRef(sourceName, sourceVersion);

    // // Auto-generate destinations if none provided and auto-generation is enabled
    // List<Destination> finalDestinations = destinations != null ? destinations :
    // (config.isAutoGenerateTaxDestination() ?
    // generateDefaultDestinations(country.toString(), documentType.toString()) :
    // new ArrayList<>());

    // // Build and send request
    // return pushToUnifyInternal(sourceRef, documentType, country, operation, mode,
    // purpose, payload, finalDestinations);
    // }

    /**
     * Internal method to push to Unify API with custom document type string.
     */
    private static UnifyResponse pushToUnifyInternalWithDocumentType(
            final SourceRef sourceRef,
            final DocumentType baseDocumentType,
            final String documentTypeString,
            final Country country,
            final Operation operation,
            final Mode mode,
            final Purpose purpose,
            final Map<String, Object> payload,
            final List<Destination> destinations) throws SDKException {

        // Build UnifyRequest with custom document type string
        UnifyRequest request = UnifyRequest.builder()
                .source(buildSourceObject(sourceRef))
                .documentType(baseDocumentType)
                .documentTypeString(documentTypeString) // Add custom document type string
                .country(country.toString())
                .operation(operation)
                .mode(mode)
                .purpose(purpose)
                .payload(payload)
                .destinations(destinations)
                .apiKey(config.getApiKey())
                .requestId("req_" + System.currentTimeMillis() + "_" + Math.random())
                .timestamp(java.time.Instant.now().toString())
                .env(mapEnvironmentToApiValue(config.getEnvironment()))
                .correlationId(config.getCorrelationId())
                .build();

        try {
            return apiClient.sendUnifyRequest(request);
        } catch (SDKException e) {
            logger.debug("SDKException caught - Error: {}, Retryable: {}, QueueManager: {}",
                    e.getMessage(),
                    e.getErrorDetail() != null ? e.getErrorDetail().isRetryable() : false,
                    queueManager != null);

            // Check if the error is retryable and queue is enabled
            if (e.getErrorDetail() != null && e.getErrorDetail().isRetryable() && queueManager != null) {
                // Store the complete UnifyRequest as JSON to maintain exact API format
                String completeRequestJson;
                try {
                    completeRequestJson = OBJECT_MAPPER.writeValueAsString(request);
                    logger.debug("Converted complete UnifyRequest to JSON with length: {}",
                            completeRequestJson.length());
                } catch (Exception jsonError) {
                    logger.warn("Failed to convert UnifyRequest to JSON, using toString(): {}", jsonError.getMessage());
                    completeRequestJson = request.toString();
                }

                // Create a Source object for backward compatibility with queue
                Source source = new Source(sourceRef.getName(), sourceRef.getVersion(), null);

                PayloadSubmission submission = new PayloadSubmission(
                        completeRequestJson, // Store complete UnifyRequest as JSON to maintain exact API format
                        source,
                        country,
                        baseDocumentType);

                logger.debug("Created PayloadSubmission with complete request length: {}",
                        submission.getPayload().length());

                // Enqueue the failed submission for background retry
                queueManager.enqueue(submission);

                // Return a response indicating the submission was queued
                UnifyResponse queuedResponse = new UnifyResponse();
                queuedResponse.setStatus("queued");
                queuedResponse.setMessage(
                        "Request failed but has been queued for retry. Submission ID: " + request.getRequestId());

                // Set submission ID in the submission response data
                UnifyResponse.SubmissionResponse submissionResponse = new UnifyResponse.SubmissionResponse();
                submissionResponse.setSubmissionId(request.getRequestId());

                UnifyResponse.UnifyResponseData responseData = new UnifyResponse.UnifyResponseData();
                responseData.setSubmission(submissionResponse);
                queuedResponse.setData(responseData);

                return queuedResponse;
            }

            // If not retryable or queue not available, re-throw the exception
            throw e;
        }
    }

    /**
     * Push to Unify API using SourceRef.
     */
    // public static UnifyResponse pushToUnify(
    // final SourceRef source,
    // final DocumentType documentType,
    // final Country country,
    // final Operation operation,
    // final Mode mode,
    // final Purpose purpose,
    // final Map<String, Object> payload,
    // final List<Destination> destinations) throws SDKException {

    // return pushToUnify(source.getName(), source.getVersion(), documentType,
    // country,
    // operation, mode, purpose, payload, destinations);
    // }

    /**
     * Push to Unify API without destinations (auto-generates).
     */
    // public static UnifyResponse pushToUnify(
    // final String sourceName,
    // final String sourceVersion,
    // final DocumentType documentType,
    // final Country country,
    // final Operation operation,
    // final Mode mode,
    // final Purpose purpose,
    // final Map<String, Object> payload) throws SDKException {

    // return pushToUnify(sourceName, sourceVersion, documentType, country,
    // operation, mode, purpose, payload, null);
    // }

    /**
     * Enhanced pushToUnify with logical document types.
     * Evaluates country policy and merges meta.config flags.
     */
    // public static UnifyResponse pushToUnifyLogical(
    // final Country country,
    // final LogicalDocType logicalType,
    // final String sourceName,
    // final String sourceVersion,
    // final Map<String, Object> payload,
    // final List<Destination> destinations) throws SDKException {

    // // Evaluate country policy
    // PolicyResult policy = CountryPolicyRegistry.evaluate(country, logicalType);

    // // Merge meta.config flags
    // Map<String, Object> mergedPayload = deepMergeIntoMetaConfig(payload,
    // policy.getMetaConfigFlags());

    // // Build source and destinations
    // SourceRef sourceRef = new SourceRef(sourceName, sourceVersion);
    // List<Destination> finalDestinations = destinations != null ? destinations :
    // generateDefaultDestinations(country.toString(), policy.getDocumentType());

    // // Send to standard pushToUnify with custom document type string
    // return pushToUnifyInternalWithDocumentType(sourceRef, policy.getBaseType(),
    // getMetaConfigDocumentType(logicalType), country, Operation.SINGLE,
    // Mode.DOCUMENTS, Purpose.INVOICING, mergedPayload, finalDestinations);
    // }

    /**
     * Deep merge meta.config flags into payload.
     * User values take precedence over policy defaults.
     */
    private static Map<String, Object> deepMergeIntoMetaConfig(Map<String, Object> payload,
            Map<String, Object> configFlags) {
        Map<String, Object> merged = new HashMap<>(payload);

        @SuppressWarnings("unchecked")
        Map<String, Object> meta = (Map<String, Object>) merged.getOrDefault("meta", new HashMap<>());
        Map<String, Object> config = (Map<String, Object>) meta.getOrDefault("config", new HashMap<>());

        // Merge config flags (user values take precedence)
        Map<String, Object> mergedConfig = new HashMap<>(configFlags);
        mergedConfig.putAll(config);

        meta.put("config", mergedConfig);
        merged.put("meta", meta);

        return merged;
    }

    /**
     * Helper method to create a tax authority destination based on country
     */
    private static Destination createTaxAuthorityDestination(String country, String documentType) {
        String authority;
        switch (country.toUpperCase()) {
            case "SA":
                authority = "ZATCA";
                break;
            case "MY":
                authority = "LHDN";
                break;
            default:
                authority = "UNKNOWN";
                break;
        }

        return Destination.taxAuthority(country.toUpperCase(), authority, documentType);
    }

    /**
     * Generate default destinations for a country and document type.
     */
    private static List<Destination> generateDefaultDestinations(String country, String documentType) {
        List<Destination> destinations = new ArrayList<>();

        // Auto-generate tax authority destination
        String authority = getDefaultTaxAuthority(country);
        if (authority != null) {
            destinations.add(Destination.taxAuthority(country.toUpperCase(), authority, documentType));
        }

        return destinations;
    }

    /**
     * Get default tax authority for a country.
     */
    private static String getDefaultTaxAuthority(String country) {
        switch (country.toUpperCase()) {
            case "SA":
                return "ZATCA";
            case "MY":
                return "LHDN";
            case "AE":
                return "FTA";
            case "SG":
                return "IRAS";
            default:
                return null;
        }
    }

    /**
     * Internal method to build and send UnifyRequest.
     */
    // private static UnifyResponse pushToUnifyInternal(
    // final SourceRef sourceRef,
    // final DocumentType documentType,
    // final Country country,
    // final Operation operation,
    // final Mode mode,
    // final Purpose purpose,
    // final Map<String, Object> payload,
    // final List<Destination> destinations) throws SDKException {

    // // Build UnifyRequest
    // UnifyRequest request = UnifyRequest.builder()
    // .source(buildSourceObject(sourceRef))
    // .documentType(documentType)
    // .country(country.toString())
    // .operation(operation)
    // .mode(mode)
    // .purpose(purpose)
    // .payload(payload)
    // .destinations(destinations)
    // .apiKey(config.getApiKey())
    // .requestId("req_" + System.currentTimeMillis() + "_" + Math.random())
    // .timestamp(java.time.Instant.now().toString())
    // .env(mapEnvironmentToApiValue(config.getEnvironment()))
    // .correlationId(config.getCorrelationId())
    // .build();

    // try {
    // return apiClient.sendUnifyRequest(request);
    // } catch (SDKException e) {
    // // Check if the error is retryable and queue is enabled
    // if (e.getErrorDetail() != null && e.getErrorDetail().isRetryable() &&
    // queueManager != null) {
    // // Create PayloadSubmission for queue - store as proper JSON to maintain
    // exact structure
    // ObjectMapper mapper = new ObjectMapper();
    // String jsonPayload;
    // try {
    // jsonPayload = mapper.writeValueAsString(payload);
    // logger.info("🔥 QUEUE: Successfully converted payload to JSON with length:
    // {}", jsonPayload.length());
    // logger.info("🔥 QUEUE: JSON preview: {}", jsonPayload.substring(0,
    // Math.min(200, jsonPayload.length())));
    // } catch (Exception jsonError) {
    // logger.warn("Failed to convert payload to JSON, using toString(): {}",
    // jsonError.getMessage());
    // jsonPayload = payload.toString();
    // }

    // // Create a Source object for backward compatibility with queue
    // Source source = new Source(sourceRef.getName(), sourceRef.getVersion(),
    // null);

    // PayloadSubmission submission = new PayloadSubmission(
    // jsonPayload, // Store as proper JSON string to maintain exact structure
    // source,
    // country,
    // documentType
    // );

    // logger.info("🔥 QUEUE: Created PayloadSubmission with payload length: {}",
    // submission.getPayload().length());

    // // Enqueue the failed submission for background retry
    // queueManager.enqueue(submission);

    // // Return a response indicating the submission was queued
    // UnifyResponse queuedResponse = new UnifyResponse();
    // queuedResponse.setStatus("queued");
    // queuedResponse.setMessage("Request failed but has been queued for retry.
    // Submission ID: " + request.getRequestId());

    // // Set submission ID in the submission response data
    // UnifyResponse.SubmissionResponse submissionResponse = new
    // UnifyResponse.SubmissionResponse();
    // submissionResponse.setSubmissionId(request.getRequestId());

    // UnifyResponse.UnifyResponseData responseData = new
    // UnifyResponse.UnifyResponseData();
    // responseData.setSubmission(submissionResponse);
    // queuedResponse.setData(responseData);

    // return queuedResponse;
    // }

    // // If not retryable or queue not available, re-throw the exception
    // throw e;
    // }
    // }

    /**
     * Build source object from SourceRef for the request.
     */
    private static Source buildSourceObject(SourceRef sourceRef) {
        Source source = new Source(sourceRef.getName(), sourceRef.getVersion(), null);

        // Add type if available from registry
        SourceType type = getSourceTypeFromRegistry(sourceRef.getName(), sourceRef.getVersion());
        if (type != null) {
            source = new Source(sourceRef.getName(), sourceRef.getVersion(), type);
        }

        return source;
    }

    /**
     * Get source type from registry by name and version.
     */
    private static SourceType getSourceTypeFromRegistry(String name, String version) {
        if (config != null && config.getSources() != null) {
            return config.getSources().stream()
                    .filter(s -> s.getName().equals(name) && s.getVersion().equals(version))
                    .map(Source::getSourceTypeEnum)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    /**
     * Convenience method to create a complete invoicing request with tax authority
     * destination using the new API.
     */
    // public static UnifyResponse submitInvoice(
    // String sourceName,
    // String sourceVersion,
    // Country country,
    // LogicalDocType logicalType,
    // Map<String, Object> payload) throws SDKException {

    // return pushToUnify(
    // sourceName,
    // sourceVersion,
    // logicalType,
    // country,
    // Operation.SINGLE,
    // Mode.DOCUMENTS,
    // Purpose.INVOICING,
    // payload);
    // }

    // /**
    // * Convenience method to create a mapping request using the new API.
    // */
    // public static UnifyResponse createMapping(
    // String sourceName,
    // String sourceVersion,
    // Country country,
    // LogicalDocType logicalType,
    // Map<String, Object> payload) throws SDKException {

    // return pushToUnify(
    // sourceName,
    // sourceVersion,
    // logicalType,
    // country,
    // Operation.SINGLE,
    // Mode.DOCUMENTS,
    // Purpose.MAPPING,
    // payload);
    // }

    // /**
    // * Convenience method to create a complete invoicing request with tax
    // authority
    // * destination (legacy method for backward compatibility)
    // * @deprecated Use submitInvoice(String sourceName, String sourceVersion, ...)
    // instead
    // */
    // @Deprecated
    // public static UnifyResponse submitInvoice(
    // String sourceId,
    // String country,
    // LogicalDocType logicalType,
    // Map<String, Object> payload) throws SDKException {

    // return pushToUnify(
    // sourceId,
    // "1.0",
    // logicalType,
    // Country.valueOf(country.toUpperCase()),
    // Operation.SINGLE,
    // Mode.DOCUMENTS,
    // Purpose.INVOICING,
    // payload);
    // }

    /**
     * Convenience method to create a mapping request (legacy method for backward
     * compatibility)
     * 
     * @deprecated Use createMapping(String sourceName, String sourceVersion, ...)
     *             instead
     */
    // @Deprecated
    // public static UnifyResponse createMapping(
    // String sourceId,
    // String country,
    // LogicalDocType logicalType,
    // Map<String, Object> payload) throws SDKException {

    // return pushToUnify(
    // sourceId,
    // "1.0",
    // logicalType,
    // Country.valueOf(country.toUpperCase()),
    // Operation.SINGLE,
    // Mode.DOCUMENTS,
    // Purpose.MAPPING,
    // payload);
    // }

    /**
     * Push to Unify API with logical document types but full control over
     * operation, mode, and purpose.
     * This combines the benefits of logical document types with the flexibility of
     * standard pushToUnify.
     */
    public static UnifyResponse pushToUnify(
            final String sourceName,
            final String sourceVersion,
            final LogicalDocType logicalType, // ← LOGICAL DOCUMENT TYPE
            final Country country,
            final Operation operation,
            final Mode mode,
            final Purpose purpose,
            final String jsonPayload) throws SDKException {

        if (jsonPayload == null || jsonPayload.trim().isEmpty()) {
            throw new SDKException(new ErrorDetail(
                    ErrorCode.EMPTY_PAYLOAD,
                    "Payload is required for pushToUnify",
                    "Provide a non-empty JSON payload string."));
        }

        try {
            Map<String, Object> payloadMap = OBJECT_MAPPER.readValue(jsonPayload, MAP_TYPE_REFERENCE);
            return pushToUnify(sourceName, sourceVersion, logicalType, country, operation, mode, purpose, payloadMap);
        } catch (IOException parseError) {
            ErrorDetail error = new ErrorDetail(
                    ErrorCode.MALFORMED_JSON,
                    "Failed to parse JSON payload",
                    "Ensure the payload is valid JSON before passing it to pushToUnify.");
            int snippetLength = Math.min(jsonPayload.length(), 200);
            error.addContextValue("payloadSnippet", jsonPayload.substring(0, snippetLength));
            throw new SDKException(error);
        }
    }

    /**
     * Push to Unify API with logical document types using a JsonNode payload.
     */
    public static UnifyResponse pushToUnify(
            final String sourceName,
            final String sourceVersion,
            final LogicalDocType logicalType, // ← LOGICAL DOCUMENT TYPE
            final Country country,
            final Operation operation,
            final Mode mode,
            final Purpose purpose,
            final JsonNode jsonNodePayload) throws SDKException {

        if (jsonNodePayload == null || jsonNodePayload.isNull()) {
            throw new SDKException(new ErrorDetail(
                    ErrorCode.MISSING_FIELD,
                    "JsonNode payload is required for pushToUnify",
                    "Provide a non-null JsonNode payload."));
        }

        try {
            Map<String, Object> payloadMap = OBJECT_MAPPER.convertValue(jsonNodePayload, MAP_TYPE_REFERENCE);
            return pushToUnify(sourceName, sourceVersion, logicalType, country, operation, mode, purpose, payloadMap);
        } catch (IllegalArgumentException conversionError) {
            ErrorDetail error = new ErrorDetail(
                    ErrorCode.INVALID_PAYLOAD_FORMAT,
                    "Failed to convert JsonNode payload to Map",
                    "Ensure the JsonNode represents an object structure compatible with the SDK payload format.");
            error.addContextValue("jsonNodeType", jsonNodePayload.getNodeType().toString());
            throw new SDKException(error);
        }
    }

    public static UnifyResponse pushToUnify(
            final String sourceName,
            final String sourceVersion,
            final LogicalDocType logicalType, // ← LOGICAL DOCUMENT TYPE
            final Country country,
            final Operation operation,
            final Mode mode,
            final Purpose purpose,
            final Map<String, Object> payload,
            final List<Destination> destinations) throws SDKException {

        if (config == null) {
            throw new SDKException(new ErrorDetail(ErrorCode.MISSING_FIELD, "SDK not configured",
                    "Call GETSUnifySDK.configure() first."));
        }

        // Process queued submissions first before handling new requests
        processQueuedSubmissionsFirst();

        // Validate required parameters
        // Handle sourceName and sourceVersion based on purpose
        String finalSourceName;
        String finalSourceVersion;

        if (purpose == Purpose.MAPPING) {
            // For MAPPING purpose, sourceName and sourceVersion are optional - set to empty
            // string if null
            finalSourceName = (sourceName == null) ? "" : sourceName;
            finalSourceVersion = (sourceVersion == null) ? "" : sourceVersion;
        } else {
            // For all other purposes, sourceName and sourceVersion are mandatory
            if (sourceName == null || sourceName.trim().isEmpty()) {
                throw new SDKException(new ErrorDetail(ErrorCode.MISSING_FIELD, "Source name is required",
                        "Provide a valid source name."));
            }
            if (sourceVersion == null || sourceVersion.trim().isEmpty()) {
                throw new SDKException(new ErrorDetail(ErrorCode.MISSING_FIELD, "Source version is required",
                        "Provide a valid source version."));
            }
            finalSourceName = sourceName;
            finalSourceVersion = sourceVersion;
        }
        if (logicalType == null) {
            throw new SDKException(new ErrorDetail(ErrorCode.MISSING_FIELD, "Logical document type is required",
                    "Provide a valid logical document type."));
        }
        if (country == null) {
            throw new SDKException(new ErrorDetail(ErrorCode.MISSING_FIELD, "Country is required",
                    "Provide a valid country."));
        }
        if (operation == null) {
            throw new SDKException(new ErrorDetail(ErrorCode.MISSING_FIELD, "Operation is required",
                    "Provide a valid operation."));
        }
        if (mode == null) {
            throw new SDKException(new ErrorDetail(ErrorCode.MISSING_FIELD, "Mode is required",
                    "Provide a valid mode."));
        }
        if (purpose == null) {
            throw new SDKException(new ErrorDetail(ErrorCode.MISSING_FIELD, "Purpose is required",
                    "Provide a valid purpose."));
        }
        if (payload == null) {
            throw new SDKException(new ErrorDetail(ErrorCode.MISSING_FIELD, "Payload is required",
                    "Provide a valid payload."));
        }

        // Validate country restrictions for current environment
        validateCountryForEnvironment(country, config.getEnvironment());

        // Evaluate country policy to get base document type and meta.config flags
        PolicyResult policy = CountryPolicyRegistry.evaluate(country, logicalType);

        // Merge meta.config flags into payload
        Map<String, Object> mergedPayload = deepMergeIntoMetaConfig(payload, policy.getMetaConfigFlags());

        // Auto-set invoice_data.document_type based on LogicalDocType
        setInvoiceDataDocumentType(mergedPayload, logicalType);

        // Create source reference
        SourceRef sourceRef = new SourceRef(finalSourceName, finalSourceVersion);

        // Auto-generate destinations if none provided and auto-generation is enabled
        List<Destination> finalDestinations = destinations != null ? destinations
                : (config.isAutoGenerateTaxDestination()
                        ? generateDefaultDestinations(country.toString(), policy.getDocumentType())
                        : new ArrayList<>());

        // Build and send request using the resolved base document type
        return pushToUnifyInternalWithDocumentType(sourceRef, policy.getBaseType(),
                getMetaConfigDocumentType(logicalType), country, operation, mode, purpose, mergedPayload,
                finalDestinations);
    }

    private static String getMetaConfigDocumentType(LogicalDocType logicalType) {
        if (logicalType.name().contains("CREDIT_NOTE")) {
            return "credit_note";
        } else if (logicalType.name().contains("DEBIT_NOTE")) {
            return "debit_note";
        } else {
            return "tax_invoice";
        }
    }

    /**
     * Automatically sets the invoice_data.document_type field based on
     * LogicalDocType
     */
    private static void setInvoiceDataDocumentType(Map<String, Object> payload, LogicalDocType logicalType) {
        if (payload == null)
            return;

        @SuppressWarnings("unchecked")
        Map<String, Object> invoiceData = (Map<String, Object>) payload.get("invoice_data");
        if (invoiceData == null)
            return;

        // Determine document type string based on LogicalDocType
        String documentType;
        if (logicalType.name().contains("CREDIT_NOTE")) {
            documentType = "credit_note";
        } else if (logicalType.name().contains("DEBIT_NOTE")) {
            documentType = "debit_note";
        } else {
            documentType = "tax_invoice"; // Default for TAX_INVOICE and SIMPLIFIED_TAX_INVOICE
        }

        // Set the document_type field
        invoiceData.put("document_type", documentType);
    }

    /**
     * Push to Unify API with logical document types but full control over
     * operation, mode, and purpose.
     * Overload without destinations (auto-generates).
     */
    public static UnifyResponse pushToUnify(
            final String sourceName,
            final String sourceVersion,
            final LogicalDocType logicalType, // ← LOGICAL DOCUMENT TYPE
            final Country country,
            final Operation operation,
            final Mode mode,
            final Purpose purpose,
            final Map<String, Object> payload) throws SDKException {

        return pushToUnify(sourceName, sourceVersion, logicalType, country,
                operation, mode, purpose, payload, null);
    }

    /**
     * Push to Unify API with logical document types using SourceRef.
     */
    public static UnifyResponse pushToUnify(
            final SourceRef source,
            final LogicalDocType logicalType, // ← LOGICAL DOCUMENT TYPE
            final Country country,
            final Operation operation,
            final Mode mode,
            final Purpose purpose,
            final Map<String, Object> payload,
            final List<Destination> destinations) throws SDKException {

        return pushToUnify(source.getName(), source.getVersion(), logicalType, country,
                operation, mode, purpose, payload, destinations);
    }

    /**
     * Push to Unify API with logical document types using SourceRef without
     * destinations.
     */
    public static UnifyResponse pushToUnify(
            final SourceRef source,
            final LogicalDocType logicalType, // ← LOGICAL DOCUMENT TYPE
            final Country country,
            final Operation operation,
            final Mode mode,
            final Purpose purpose,
            final Map<String, Object> payload) throws SDKException {

        return pushToUnify(source.getName(), source.getVersion(), logicalType, country,
                operation, mode, purpose, payload, null);
    }

    /**
     * Map Environment enum to API-expected string values.
     * The API expects "sandbox" or "prod", not the enum names.
     */
    private static String mapEnvironmentToApiValue(Environment environment) {
        switch (environment) {
            case LOCAL:
            case DEV:
            case TEST:
            case STAGE:
            case SANDBOX:
                return "sandbox";
            case SIMULATION:
                return "simulation";
            case PRODUCTION:
                return "prod";
            default:
                return "sandbox"; // Default to sandbox for safety
        }
    }

    /**
     * Validate country restrictions based on current environment.
     * Implements the three-tier country access control:
     * - SA: Allowed in all production environments (SANDBOX, SIMULATION,
     * PRODUCTION)
     * - MY: Allowed in SANDBOX and PRODUCTION only (blocked in SIMULATION)
     * - BE: Allowed in SANDBOX and PRODUCTION only (blocked in SIMULATION)
     * - DE: Allowed in SANDBOX and PRODUCTION only (blocked in SIMULATION)
     * - AE: Allowed in SANDBOX and PRODUCTION only (blocked in SIMULATION)
     * - Others: Blocked in all production environments
     */
    private static void validateCountryForEnvironment(Country country, Environment environment) throws SDKException {
        if (environment == Environment.SANDBOX ||
                environment == Environment.SIMULATION ||
                environment == Environment.PRODUCTION) {

            // SA is allowed in all production environments
            if (country == Country.SA) {
                return; // SA is always allowed
            }

            // MY is only allowed in SANDBOX and PRODUCTION (not SIMULATION)
            if (country == Country.MY) {
                if (environment == Environment.SIMULATION) {
                    throw new SDKException(new ErrorDetail(ErrorCode.INVALID_ARGUMENT,
                            "Country not allowed for simulation environment",
                            "MY (Malaysia) is not allowed in SIMULATION environment. Use SANDBOX or PRODUCTION."));
                }
                return; // MY is allowed in SANDBOX and PRODUCTION
            }

            // BE is only allowed in SANDBOX and PRODUCTION (not SIMULATION)
            if (country == Country.BE) {
                if (environment == Environment.SIMULATION) {
                    throw new SDKException(new ErrorDetail(ErrorCode.INVALID_ARGUMENT,
                            "Country not allowed for simulation environment",
                            "BE (Belgium) is not allowed in SIMULATION environment. Use SANDBOX or PRODUCTION."));
                }
                return; // BE is allowed in SANDBOX and PRODUCTION
            }

            // DE is only allowed in SANDBOX and PRODUCTION (not SIMULATION)
            if (country == Country.DE) {
                if (environment == Environment.SIMULATION) {
                    throw new SDKException(new ErrorDetail(ErrorCode.INVALID_ARGUMENT,
                            "Country not allowed for simulation environment",
                            "DE (Germany) is not allowed in SIMULATION environment. Use SANDBOX or PRODUCTION."));
                }
                return; // DE is allowed in SANDBOX and PRODUCTION
            }

            // AE (UAE) is only allowed in SANDBOX and PRODUCTION (not SIMULATION)
            if (country == Country.AE) {
                if (environment == Environment.SIMULATION) {
                    throw new SDKException(new ErrorDetail(ErrorCode.INVALID_ARGUMENT,
                            "Country not allowed for simulation environment",
                            "AE (UAE) is not allowed in SIMULATION environment. Use SANDBOX or PRODUCTION."));
                }
                return; // AE is allowed in SANDBOX and PRODUCTION
            }

            // All other countries are blocked in production environments
            throw new SDKException(new ErrorDetail(ErrorCode.INVALID_ARGUMENT,
                    "Country not allowed for production environment",
                    "Only SA, MY, BE, DE, and AE are allowed for " + environment + ". Use DEV/TEST/STAGE for other countries."));
        }

        // For DEV/TEST/STAGE/LOCAL, all countries are allowed
    }
}