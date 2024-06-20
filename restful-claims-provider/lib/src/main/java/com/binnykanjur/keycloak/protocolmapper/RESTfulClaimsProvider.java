package com.binnykanjur.keycloak.protocolmapper;

import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;
import org.json.JSONObject;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RESTfulClaimsProvider extends AbstractOIDCProtocolMapper
        implements OIDCAccessTokenMapper, OIDCIDTokenMapper {

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    public static final String INCLUDED_SERVICE_URL = "included.service.url";
    public static final String INCLUDED_SERVICE_URL_LABEL = "Service Url";
    public static final String INCLUDED_SERVICE_URL_HELP_TEXT = "Endpoint to get claims from";

    public static final String PROVIDER_ID = "oidc-RESTful-claims-mapper";

    static {
        ProviderConfigProperty property = new ProviderConfigProperty();
        property.setName(INCLUDED_SERVICE_URL);
        property.setLabel(INCLUDED_SERVICE_URL_LABEL);
        property.setHelpText(INCLUDED_SERVICE_URL_HELP_TEXT);
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setRequired(true);
        property.setDefaultValue("");
        configProperties.add(property);

        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, RESTfulClaimsProvider.class);

        // Don't include claims in ID Token by default
        for (ProviderConfigProperty prop : configProperties) {
            if (OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN.equals(prop.getName())) {
                prop.setDefaultValue("false");
            }
        }
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "RESTful Claims Provider";
    }

    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    @Override
    public String getHelpText() {
        return "Adds custom claims returned from a call to the Service Url";
    }

    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession,
            KeycloakSession keycloakSession, ClientSessionContext clientSessionCtx) {
        String serviceUrl = mappingModel.getConfig().get(INCLUDED_SERVICE_URL);
        if (serviceUrl == null || serviceUrl.isBlank()) {
            return;
        }

        String userId = token.getSubject();
        if (userId == null) {
            return;
        }

        // Fetch data from the remote GET endpoint using the authenticated user's ID
        Map<String, Object> customClaims = fetchCustomClaimsFromApi(serviceUrl, userId);

        // Add the retrieved key-value pairs as custom claims to the token
        for (Map.Entry<String, Object> entry : customClaims.entrySet()) {
            Object claimValue = entry.getValue();
            if (claimValue == null) {
                continue;
            }

            token.getOtherClaims().put(entry.getKey(), claimValue);
        }
    }

    private Map<String, Object> fetchCustomClaimsFromApi(String baseEndpointUrl, String userId) {
        Map<String, Object> customClaims = new HashMap<>();
        HttpClient client = HttpClient.newHttpClient();

        // Encode the userId to ensure it is URL-safe
        String encodedUserId = URLEncoder.encode(userId, StandardCharsets.UTF_8);
        // Construct the complete endpoint URL with the userId as a query parameter
        String endpointUrl = String.format("%s?objectId=%s", baseEndpointUrl, encodedUserId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpointUrl))
                .GET()
                .header("Accept", "application/json")
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String json = response.body();

            JSONObject jsonResponse = new JSONObject(json);
            for (String key : jsonResponse.keySet()) {
                Object value = jsonResponse.get(key);
                customClaims.put(key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return customClaims;
    }

}