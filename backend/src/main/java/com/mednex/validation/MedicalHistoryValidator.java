package com.mednex.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.*;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Set;

@Component
public class MedicalHistoryValidator {

    private final JsonSchema schema;
    private final ObjectMapper mapper;

    public MedicalHistoryValidator(ObjectMapper mapper) {
        this.mapper = mapper;
        InputStream schemaStream = getClass()
            .getResourceAsStream("/schemas/medical-history-v1.json");
        this.schema = JsonSchemaFactory
            .getInstance(SpecVersion.VersionFlag.V7)
            .getSchema(schemaStream);
    }

    public void validate(String medicalHistoryJson) {
        try {
            JsonNode node = mapper.readTree(medicalHistoryJson);
            Set<ValidationMessage> errors = schema.validate(node);
            if (!errors.isEmpty()) {
                String msg = errors.stream()
                    .map(ValidationMessage::getMessage)
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Invalid medical history");
                throw new InvalidMedicalHistoryException(msg);
            }
        } catch (InvalidMedicalHistoryException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidMedicalHistoryException("Medical history must be valid JSON: " + e.getMessage());
        }
    }
}
