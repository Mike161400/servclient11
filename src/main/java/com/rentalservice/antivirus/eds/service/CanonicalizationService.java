package com.rentalservice.antivirus.eds.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rentalservice.antivirus.eds.exception.EdsOperationException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

@Service
public class CanonicalizationService {

    private final ObjectMapper objectMapper;

    public CanonicalizationService() {
        this.objectMapper = JsonMapper.builder()
                .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .disable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public byte[] canonicalizeToBytes(Object payload) {
        return canonicalizeToJson(payload).getBytes(StandardCharsets.UTF_8);
    }

    public String canonicalizeToJson(Object payload) {
        try {
            JsonNode sourceNode = payload instanceof JsonNode jsonNode
                    ? jsonNode
                    : objectMapper.valueToTree(payload);
            JsonNode canonicalNode = canonicalizeNode(sourceNode);
            return objectMapper.writeValueAsString(canonicalNode);
        } catch (IllegalArgumentException | JsonProcessingException ex) {
            throw new EdsOperationException("Failed to canonicalize payload for EDS signing", ex);
        }
    }

    private JsonNode canonicalizeNode(JsonNode node) {
        if (node == null || node.isNull() || node.isValueNode()) {
            return node;
        }

        if (node.isArray()) {
            ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
            for (JsonNode element : node) {
                arrayNode.add(canonicalizeNode(element));
            }
            return arrayNode;
        }

        if (node.isObject()) {
            ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
            Map<String, JsonNode> sortedFields = new TreeMap<>();
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                sortedFields.put(entry.getKey(), canonicalizeNode(entry.getValue()));
            }
            sortedFields.forEach(objectNode::set);
            return objectNode;
        }

        return node;
    }
}
