package de.numcodex.feasibility_gui_backend.service.query_executor.impl.dsf;

import ca.uhn.fhir.context.FhirContext;
import de.numcodex.feasibility_gui_backend.service.query_executor.BrokerClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for providing a {@link DSFBrokerClient} instance.
 */
@Configuration
public class DSFSpringConfig {

    @Value("${de.num-codex.FeasibilityGuiBackend.dsf.security.keystore.p12file}")
    private String keyStoreFile;

    @Value("${de.num-codex.FeasibilityGuiBackend.dsf.security.keystore.password}")
    private char[] keyStorePassword;

    @Value("${de.num-codex.FeasibilityGuiBackend.dsf.security.certificate}")
    private String certificateFile;

    @Value("${de.num-codex.FeasibilityGuiBackend.dsf.webservice.baseUrl}")
    private String webserviceBaseUrl;

    @Value("${de.num-codex.FeasibilityGuiBackend.dsf.webservice.readTimeout}")
    private int webserviceReadTimeout;

    @Value("${de.num-codex.FeasibilityGuiBackend.dsf.webservice.connectTimeout}")
    private int webserviceConnectTimeout;

    @Value("${de.num-codex.FeasibilityGuiBackend.dsf.websocket.url}")
    private String websocketUrl;

    @Value("${de.num-codex.FeasibilityGuiBackend.dsf.organizationId}")
    private String organizationId;

    @Qualifier("dsf")
    @Bean
    public BrokerClient dsfBrokerClient(QueryManager queryManager, QueryResultCollector queryResultCollector) {
        return new DSFBrokerClient(queryManager, queryResultCollector);
    }

    @Bean
    QueryManager dsfQueryManager(FhirWebClientProvider fhirWebClientProvider) {
        return new DSFQueryManager(fhirWebClientProvider, organizationId.replace(' ', '_'));
    }

    @Bean
    QueryResultCollector queryResultCollector(QueryResultStore resultStore, FhirContext fhirContext,
                                              FhirWebClientProvider webClientProvider, DSFQueryResultHandler resultHandler) {
        return new DSFQueryResultCollector(resultStore, fhirContext, webClientProvider, resultHandler);
    }

    @Bean
    QueryResultStore queryResultStore() {
        return new DSFQueryResultStore();
    }

    @Bean
    DSFQueryResultHandler queryResultHandler(FhirWebClientProvider webClientProvider) {
        return new DSFQueryResultHandler(webClientProvider);
    }

    @Bean
    FhirContext fhirContext() {
        return FhirContext.forR4();
    }


    @Bean
    FhirSecurityContextProvider fhirSecurityContextProvider() {
        return new DSFFhirSecurityContextProvider(keyStoreFile, keyStorePassword, certificateFile);
    }


    @Bean
    FhirWebClientProvider fhirWebClientProvider(FhirContext fhirContext, FhirSecurityContextProvider securityContextProvider) {
        return new DSFFhirWebClientProvider(fhirContext, webserviceBaseUrl, webserviceReadTimeout,
                webserviceConnectTimeout, websocketUrl, securityContextProvider);
    }
}
