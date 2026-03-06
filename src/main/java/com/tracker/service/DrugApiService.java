package com.tracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracker.dto.DrugInfoResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DrugApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final String OPEN_FDA_URL = "https://api.fda.gov/drug/label.json?search=openfda.brand_name.exact:\"{name}\"&limit=1";

    public DrugApiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public DrugInfoResponse getDrugInfo(String medicineName) {
        try {
            String url = OPEN_FDA_URL.replace("{name}", medicineName.toUpperCase());
            String response = restTemplate.getForObject(url, String.class);

            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode results = root.path("results").get(0);

                if (results != null) {
                    DrugInfoResponse info = new DrugInfoResponse();

                    JsonNode openfda = results.path("openfda");
                    info.setBrandName(getTextOrEmpty(openfda.path("brand_name")));
                    info.setGenericName(getTextOrEmpty(openfda.path("generic_name")));

                    info.setActiveIngredients(getTextOrEmpty(results.path("active_ingredient")));
                    info.setWarnings(getTextOrEmpty(results.path("warnings")));
                    info.setIndicationsAndUsage(getTextOrEmpty(results.path("indications_and_usage")));

                    return info;
                }
            }
        } catch (Exception e) {
            System.err.println("Could not fetch drug info from OpenFDA: " + e.getMessage());
        }
        return null;
    }

    private String getTextOrEmpty(JsonNode node) {
        if (node.isArray() && node.size() > 0) {
            return node.get(0).asText();
        } else if (node.isTextual()) {
            return node.asText();
        }
        return "N/A";
    }
}
