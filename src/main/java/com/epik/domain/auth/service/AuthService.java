package com.epik.domain.auth.service;

import com.epik.domain.auth.dto.response.AvailabilityCheckResponse;
import com.epik.domain.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;

    /**
     * 회원 가입 시 이메일 중복 조회
     * @param email 확인할 이메일 주소
     * @return 사용 가능 여부 (true: 사용 가능, false: 사용 불가)
     */
    public AvailabilityCheckResponse isEmailAvailable(String email) {
        boolean exists = userRepository.existsByEmail(email);
        // 존재하면 true -> 사용 불가
        // available = 사용 가능 여부 = !exists
        return new AvailabilityCheckResponse(!exists);
    }

}
