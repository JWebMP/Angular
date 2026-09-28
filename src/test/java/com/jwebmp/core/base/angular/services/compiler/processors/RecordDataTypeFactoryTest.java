package com.jwebmp.core.base.angular.services.compiler.processors;

import com.jwebmp.core.base.angular.client.annotations.angular.NgDataType;
import com.jwebmp.core.base.angular.client.services.interfaces.INgDataType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RecordDataTypeFactoryTest {
    @NgDataType
    public record ProfileData(String id, int count, boolean active, List<String> roles,
                              Map<String, String> attributes, Optional<String> alias)
            implements INgDataType<ProfileData> {
        public ProfileData {
            roles = List.copyOf(roles);
            attributes = Map.copyOf(attributes);
            alias = alias.or(() -> Optional.empty());
        }
    }

    @Test
    void rendersRecordWithoutAZeroArgumentConstructorOrGuiceBinding() throws Exception {
        ProfileData record = (ProfileData) RecordDataTypeFactory.create(ProfileData.class);
        String typeScript = record.renderClassTs().toString();

        assertTrue(typeScript.contains("id? : string"), typeScript);
        assertTrue(typeScript.contains("count : number"), typeScript);
        assertTrue(typeScript.contains("roles? : string[]"), typeScript);
        assertTrue(typeScript.contains("attributes? : Record<string, string>"), typeScript);
        assertEquals(List.of(), record.roles());
    }
}
