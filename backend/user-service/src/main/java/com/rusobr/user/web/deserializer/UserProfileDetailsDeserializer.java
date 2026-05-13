package com.rusobr.user.web.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusobr.user.infrastructure.enums.UserRole;
import com.rusobr.user.infrastructure.service.user.UserProfileDetails;
import com.rusobr.user.web.dto.parent.ParentDetails;
import com.rusobr.user.web.dto.student.StudentDetails;
import com.rusobr.user.web.dto.teacher.TeacherDetails;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class UserProfileDetailsDeserializer extends JsonDeserializer<Map<UserRole, UserProfileDetails>> {
    @Override
    public Map<UserRole, UserProfileDetails> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();

        JsonNode node = mapper.readTree(p);
        Map<UserRole, UserProfileDetails> res = new HashMap<>();

        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            UserRole role = UserRole.valueOf(entry.getKey());
            JsonNode value = entry.getValue();

            UserProfileDetails details = switch (role) {
                case TEACHER -> mapper.treeToValue(value, TeacherDetails.class);
                case STUDENT -> mapper.treeToValue(value, StudentDetails.class);
                case PARENT -> mapper.treeToValue(value, ParentDetails.class);
                default -> null;
            };

            res.put(role, details);
        }

        return res;
    }
}
