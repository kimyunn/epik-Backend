package com.epik.domain.auth.service;

import com.epik.domain.auth.dto.request.SignupRequest;
import com.epik.domain.auth.dto.response.EmailAvailabilityResponse;
import com.epik.domain.auth.dto.response.NicknameAvailabilityResponse;
import com.epik.domain.auth.entity.ConsentItem;
import com.epik.domain.auth.entity.User;
import com.epik.domain.auth.entity.UserConsent;
import com.epik.domain.auth.entity.enums.ConsentItemCode;
import com.epik.domain.auth.entity.enums.NicknameInvalidReason;
import com.epik.domain.auth.repository.ConsentItemRepository;
import com.epik.domain.auth.repository.ForbiddenWordRepository;
import com.epik.domain.auth.repository.UserConsentRepository;
import com.epik.domain.auth.repository.UserRepository;
import com.epik.global.exception.BusinessException;
import com.epik.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final ForbiddenWordRepository forbiddenWordRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConsentItemRepository consentItemRepository;
    private final UserConsentRepository userConsentRepository;

    private List<String> cachedForbiddenWords;

    // ========== Public (Controller용) ==========

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


    /**
     * 이메일 회원가입
     * @param request
     */
    @Transactional
    public void signup(SignupRequest request) {
        // 이메일 중복 검사
        // 닉네임 중복 + 금칙어 검사
        // 필수 약관 검증
        validateEmail(request.getEmail());
        validateNickname(request.getNickname());
        validateRequiredConsents(request);

        // 3. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 4. User 엔티티 생성 및 저장
        User user = User.createEmailUser(request.getEmail(), encodedPassword, request.getNickname());
        userRepository.save(user);

        // 5. 동의 이력(consent_histories) 저장
        saveAllUserConsent(user, request);
    }

    // ========== Private (내부용) ==========
    /**
     * 이메일 검증 (예외 발생)
     */
    private void validateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    /**
     * 닉네임 검증 (예외 발생)
     */
    private void validateNickname(String nickname) {
        // 금칙어
        if (containsForbiddenWords(nickname)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_WORD);
        }

        // 중복
        if (userRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }
    }

    /**
     * 필수 약관 검증
     */
    private void validateRequiredConsents(SignupRequest request) {
        boolean allAgreed = request.isTermsOfServiceAgreed()
                && request.isPrivacyPolicyAgreed()
                && request.isLocationServiceAgreed();

        if (!allAgreed) {
            throw new BusinessException(ErrorCode.REQUIRED_CONSENT_NOT_AGREED);
        }
    }

    /**
     * 가입 시 사용자의 모든 약관 동의를 저장
     */
    private void saveAllUserConsent(User user, SignupRequest request) {
        // 필수 약관 3개 → 동의 이력 3개
        saveUserConsent(user, ConsentItemCode.TERMS, request.isTermsOfServiceAgreed());
        saveUserConsent(user, ConsentItemCode.PRIVACY, request.isPrivacyPolicyAgreed());
        saveUserConsent(user, ConsentItemCode.LOCATION, request.isLocationServiceAgreed());

        // 선택 약관 (동의 시)
        if (Boolean.TRUE.equals(request.getMarketingConsent())) {
            saveUserConsent(user, ConsentItemCode.MARKETING, true);
        }
    }

    /**
     * 약관 동의 이력 하나 저장
     */
    private void saveUserConsent(User user, ConsentItemCode code, Boolean isAgreed) {
        ConsentItem consentItem = consentItemRepository.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSENT_ITEM_NOT_FOUND));

        UserConsent userConsent = UserConsent.create(user, consentItem, isAgreed);

        userConsentRepository.save(userConsent);
    }


}
