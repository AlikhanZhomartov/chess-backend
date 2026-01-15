package io.github.alikhanzhomartov.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MakeMoveRequestDto.class, name = "MOVE")
})
public interface ClientRequest {
    String requestId();
}
