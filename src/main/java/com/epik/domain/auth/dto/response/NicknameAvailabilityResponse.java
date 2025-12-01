package com.epik.domain.auth.dto.response;

import com.epik.domain.auth.entity.enums.NicknameInvalidReason;
import com.fasterxml.jackson.annotation.JsonInclude;

public record NicknameAvailabilityResponse(
        boolean available,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        NicknameInvalidReason reason   // "DUPLICATED", "FORBIDDEN_WORD"
) {}
