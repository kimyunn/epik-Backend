package com.epik.domain.auth.service;

import com.epik.domain.auth.dto.request.LogoutRequest;
import com.epik.domain.auth.dto.request.SignupRequest;
import com.epik.domain.auth.dto.response.EmailAvailabilityResponse;
import com.epik.domain.auth.dto.response.JoinMethodResponse;
import com.epik.domain.auth.dto.response.NicknameAvailabilityResponse;
import com.epik.domain.auth.dto.response.TokenResponse;
import com.epik.domain.auth.entity.ConsentItem;
import com.epik.domain.auth.entity.User;
import com.epik.domain.auth.entity.UserConsent;
import com.epik.domain.auth.entity.enums.ConsentItemCode;
import com.epik.domain.auth.entity.enums.NicknameInvalidReason;
import com.epik.domain.auth.repository.ConsentItemRepository;
import com.epik.domain.auth.repository.ForbiddenWordRepository;
import com.epik.domain.auth.repository.UserConsentRepository;
import com.epik.domain.auth.repository.UserRepository;
import com.epik.domain.auth.token.RefreshToken;
import com.epik.domain.auth.token.RefreshTokenRepository;
import com.epik.global.exception.custom.BusinessException;
import com.epik.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final ForbiddenWordRepository forbiddenWordRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConsentItemRepository consentItemRepository;
    private final UserConsentRepository userConsentRepository;
    private final TokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    private List<String> cachedForbiddenWords;

    // ========== Public (Controller용) ==========

    /**
     * 이메일 사용 가능 여부를 확인한다.
     *
     * @param email 확인할 이메일 주소
     * @return 사용 가능 여부 응답
     */
    public EmailAvailabilityResponse isEmailAvailable(String email) {
        boolean exists = userRepository.existsByEmail(email);
        // 존재하면 true -> 사용 불가
        // available = 사용 가능 여부 = !exists
        return new EmailAvailabilityResponse(!exists);
    }

    /**
     * 닉네임 사용 가능 여부를 확인한다.
     * 금칙어 포함 여부와 중복 여부를 검사한다.
     *
     * @param nickname 확인할 닉네임
     * @return 사용 가능 여부 및 불가 사유
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
     * 닉네임에 금칙어가 포함되어 있는지 확인한다.
     * 금칙어 목록은 최초 호출 시 캐싱된다.
     *
     * @param nickname 검사할 닉네임
     * @return 금칙어 포함 여부
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
     * 닉네임 중복 여부를 확인한다.
     *
     * @param nickname 검사할 닉네임
     * @return 중복 여부
     */
    public boolean isNicknameDuplicated(String nickname) {
        return userRepository.existsByNickname(nickname);
    }


    /**
     * 이메일 회원가입을 처리한다.
     *
     * @param request 회원가입 요청 정보
     * @throws BusinessException 이메일/닉네임 중복, 필수 약관 미동의 시 발생
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
     * 이메일 중복 여부를 검증한다.
     *
     * @param email 검사할 이메일
     * @throws BusinessException 이메일이 이미 사용 중인 경우
     */
    private void validateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    /**
     * 닉네임의 금칙어 포함 및 중복 여부를 검증한다.
     *
     * @param nickname 검사할 닉네임
     * @throws BusinessException 금칙어 포함 또는 중복인 경우
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
     * 필수 약관 동의 여부를 검증한다.
     *
     * @param request 회원가입 요청 DTO
     * @throws BusinessException 필수 약관 미동의 시 발생
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
     * 약관 동의 이력을 저장한다.
     * 필수 약관은 무조건 저장하고, 선택 약관은 동의한 경우에만 저장한다.
     *
     * @param user 사용자
     * @param request 회원가입 요청 정보
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
     * 특정 약관 항목에 대한 동의 이력을 저장한다.
     *
     * @param user 사용자
     * @param code 약관 항목 코드
     * @param isAgreed 동의 여부
     * @throws BusinessException 약관 항목을 찾을 수 없는 경우
     */
    private void saveUserConsent(User user, ConsentItemCode code, Boolean isAgreed) {
        ConsentItem consentItem = consentItemRepository.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSENT_ITEM_NOT_FOUND));

        UserConsent userConsent = UserConsent.create(user, consentItem, isAgreed);

        userConsentRepository.save(userConsent);
    }

    /**
     * 이메일로 회원 가입 여부 및 가입 방식을 조회한다.
     * 탈퇴한 회원은 미가입으로 처리한다.
     *
     * @param email 확인할 이메일
     * @return 가입 여부 및 가입 방식
     */
    public JoinMethodResponse checkJoinMethod(String email) {
        return userRepository.findByEmail(email)
                .filter(user -> user.getDeletedAt() == null)  // null이면 활성 회원
                .map(user -> JoinMethodResponse.registered(user.getEmail(), user.getJoinType()))
                .orElse(JoinMethodResponse.notRegistered(email));
    }

    /**
     * 이메일과 비밀번호로 로그인한다.
     * User Enumeration 공격 방지를 위해 이메일/비밀번호 오류 시 동일한 예외를 발생시킨다.
     *
     * @param email 이메일
     * @param password 비밀번호
     * @return Access Token과 Refresh Token
     * @throws BusinessException 이메일 또는 비밀번호가 일치하지 않는 경우
     */
    public TokenResponse login(String email, String password) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
        verifyPassword(password, user.getPassword());

        return tokenService.createTokenResponseFrom(user);

    }

    /**
     * 비밀번호 일치 여부를 검증한다.
     *
     * @param rawPassword 평문 비밀번호
     * @param encodedPassword 암호화된 비밀번호
     * @throws BusinessException 비밀번호가 일치하지 않는 경우
     */
    private void verifyPassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    @Transactional
    public void logout(Long userId, LogoutRequest request) {
        refreshTokenRepository.deleteByUserIdAndToken(userId, request.getRefreshToken());
    }
}
