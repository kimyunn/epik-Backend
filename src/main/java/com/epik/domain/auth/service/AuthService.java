package com.epik.domain.auth.service;

import com.epik.domain.auth.dto.response.EmailAvailabilityResponse;
import com.epik.domain.auth.dto.response.NicknameAvailabilityResponse;
import com.epik.domain.auth.entity.enums.NicknameInvalidReason;
import com.epik.domain.auth.repository.ForbiddenWordRepository;
import com.epik.domain.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final ForbiddenWordRepository forbiddenWordRepository;

    private List<String> cachedForbiddenWords;

    /**
     * 회원 가입 시 이메일 중복 조회
     * @param email 확인할 이메일 주소
     * @return 사용 가능 여부 (true: 사용 가능, false: 사용 불가)
     */
    public EmailAvailabilityResponse isEmailAvailable(String email) {
        boolean exists = userRepository.existsByEmail(email);
        // 존재하면 true -> 사용 불가
        // available = 사용 가능 여부 = !exists
        return new EmailAvailabilityResponse(!exists);
    }

    /**
     * 회원 가입 시 닉네임 유효성 검사
     * @param nickname
     * @return
     */
    public NicknameAvailabilityResponse isNicknameAvailable(String nickname) {
        if (containsForbiddenWords(nickname)) {
            return new NicknameAvailabilityResponse(false, NicknameInvalidReason.FORBIDDEN_WORD);
        }

        if (isNicknameDuplicated(nickname)) {
            return new NicknameAvailabilityResponse(false, NicknameInvalidReason.DUPLICATED);
        }

        return new NicknameAvailabilityResponse(true, null);
    }

    /**
     * 닉네임 금칙어 포함 검사
     * @param nickname
     * @return
     */
    public boolean containsForbiddenWords(String nickname) {
        if (cachedForbiddenWords == null) {
            cachedForbiddenWords = forbiddenWordRepository.findAllWords();
        }

        for (String word : cachedForbiddenWords) {
            if (nickname.contains(word)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 닉네임 중복 검사
     * @param nickname
     * @return
     */
    public boolean isNicknameDuplicated(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

}
