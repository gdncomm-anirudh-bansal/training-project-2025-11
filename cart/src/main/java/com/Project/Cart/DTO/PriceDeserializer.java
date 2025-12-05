package com.Project.Cart.DTO;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class PriceDeserializer extends JsonDeserializer<Long> {

    @Override
    public Long deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        
        // If it's a number, return it directly
        if (node.isNumber()) {
            return node.asLong();
        }
        
        // If it's an object, try to extract value from common fields
        if (node.isObject()) {
            JsonNode valueNode = node.get("value");
            if (valueNode != null && valueNode.isNumber()) {
                return valueNode.asLong();
            }
            
            valueNode = node.get("amount");
            if (valueNode != null && valueNode.isNumber()) {
                return valueNode.asLong();
            }
            
            valueNode = node.get("price");
            if (valueNode != null && valueNode.isNumber()) {
                return valueNode.asLong();
            }
        }
        
        // If it's a text node, try to parse it
        if (node.isTextual()) {
            try {
                return Long.parseLong(node.asText());
            } catch (NumberFormatException e) {
                // Ignore and return 0
            }
        }
        
        return 0L;
    }
}

