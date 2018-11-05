package gateway.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonSerializer {

    private static final Logger LOGGER = Logger.getLogger(JsonSerializer.class.toGenericString());
    private static ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
    }

    private JsonSerializer() {
    }

    public static Optional<String> toJsonString(Object object) {
        try {
            return Optional.of(objectMapper.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return Optional.empty();
    }

}
