package com.epik.domain.auth.service;

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
import com.epik.global.exception.BusinessException;
import com.epik.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
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
     * 닉네임 사용 가능 여부를 검사한다.
     *
     * <p>
     * 검사 기준:
     * 1) 금칙어 포함 여부
     * 2) 중복 여부
     *
     * 두 조건 중 하나라도 만족하지 않으면 사용할 수 없는 닉네임으로 판단하고
     * 해당 사유를 함께 반환한다.
     * </p>
     *
     * @param nickname 확인할 닉네임
     * @return 닉네임 사용 가능 여부 및 사유를 담은 NicknameAvailabilityResponse
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
     * 닉네임 내에 금칙어가 포함되어 있는지 검사한다.
     *
     * <p>
     * 금칙어 목록은 최초 호출 시 DB에서 조회하여 캐싱되며,
     * 이후부터는 메모리에 저장된 목록을 사용하여 조회 성능을 향상시킨다.
     * </p>
     *
     * @param nickname 검사할 닉네임
     * @return 금칙어가 포함되어 있으면 true, 아니면 false
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
     * 닉네임이 이미 사용 중인지 확인한다.
     *
     * @param nickname 검사할 닉네임
     * @return 중복된 닉네임이면 true, 아니면 false
     */
    public boolean isNicknameDuplicated(String nickname) {
        return userRepository.existsByNickname(nickname);
    }


    /**
     * 이메일 회원가입을 처리한다.
     *
     * <p>
     * 처리 과정:
     * 1) 이메일 중복 검사
     * 2) 닉네임 중복 + 금칙어 검사
     * 3) 필수 약관 동의 여부 검증
     * 4) 비밀번호 암호화 및 User 저장
     * 5) 동의 이력(consent_histories) 저장
     *
     * 가입 과정 중 하나라도 실패하면 BusinessException을 발생시킨다.
     * </p>
     *
     * @param request 회원가입 요청 정보(SignupRequest)
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
     * 이메일 중복 여부를 검사한다.
     *
     * @param email 검사할 이메일
     * @throws BusinessException EMAIL_ALREADY_EXISTS 중복 시 발생
     */
    private void validateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    /**
     * 닉네임이 금칙어를 포함하거나 이미 사용 중인지 검증한다.
     *
     * @param nickname 검사할 닉네임
     * @throws BusinessException FORBIDDEN_WORD 또는 NICKNAME_ALREADY_EXISTS
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
     * 필수 약관이 모두 동의되었는지 검증한다.
     *
     * @param request 회원가입 요청 DTO
     * @throws BusinessException REQUIRED_CONSENT_NOT_AGREED 필수 항목 미동의 시 발생
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
     * 회원 가입 시 동의한 약관 이력을 모두 저장한다.
     *
     * <p>
     * - 필수 약관 3개는 무조건 저장
     * - 선택 약관은 동의한 경우에만 저장
     * </p>
     *
     * @param user 저장 대상 사용자
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
     * 특정 약관 항목에 대한 동의 상태를 저장한다.
     *
     * @param user 동의를 수행한 사용자
     * @param code 약관 항목 코드
     * @param isAgreed 동의 여부
     * @throws BusinessException CONSENT_ITEM_NOT_FOUND 약관 항목이 존재하지 않을 경우
     */
    private void saveUserConsent(User user, ConsentItemCode code, Boolean isAgreed) {
        ConsentItem consentItem = consentItemRepository.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSENT_ITEM_NOT_FOUND));

        UserConsent userConsent = UserConsent.create(user, consentItem, isAgreed);

        userConsentRepository.save(userConsent);
    }

    /**
     * 주어진 이메일을 기준으로 회원 가입 여부를 조회한다.
     *
     * <p>
     * 처리 과정:
     * 1) 이메일로 사용자 조회
     * 2) 탈퇴되지 않은 사용자(deletedAt == null)인 경우 → 가입된 회원으로 판단
     * 3) 사용자가 없거나 탈퇴한 사용자라면 → 미가입 상태로 판단
     *
     * 결과는 JoinMethodResponse 형태로 반환되며,
     * 가입된 경우 가입 방식(joinType) 정보도 함께 포함된다.
     * </p>
     *
     * @param email 가입 여부를 확인할 이메일
     * @return 가입 상태(registered / notRegistered)를 담은 JoinMethodResponse
     */
    public JoinMethodResponse checkJoinMethod(String email) {
        return userRepository.findByEmail(email)
                .filter(user -> user.getDeletedAt() == null)  // null이면 활성 회원
                .map(user -> JoinMethodResponse.registered(user.getEmail(), user.getJoinType()))
                .orElse(JoinMethodResponse.notRegistered(email));
    }

    /**
     * 이메일과 비밀번호를 이용한 로그인 기능을 수행한다.
     *
     * <p>
     * 처리 과정:
     * 1) 이메일로 사용자 조회
     * 2) 입력한 비밀번호와 저장된 비밀번호 비교
     * 3) 검증 성공 시 Access Token + Refresh Token 발급
     *
     * 잘못된 이메일 또는 비밀번호가 입력된 경우 동일한 예외(INVALID_CREDENTIALS)를 발생시켜
     * 인증 정보 노출(User Enumeration)을 방지한다.
     * </p>
     *
     * @param email 로그인 요청 이메일
     * @param password 로그인 요청 비밀번호(평문)
     * @return 로그인 성공 시 TokenResponse(AccessToken + RefreshToken)
     */
    public TokenResponse login(String email, String password) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
        verifyPassword(password, user.getPassword());

        return tokenService.createTokenResponseFrom(user);

    }

    /**
     * 비밀번호 검증
     *
     * <p>
     * 저장된 암호화된 비밀번호(encoded)와
     * 로그인 요청 시 입력된 평문 비밀번호(raw)를 비교한다.
     *
     * 비밀번호가 일치하지 않으면 INVALID_CREDENTIALS 예외를 발생시킨다.
     * </p>
     *
     * @param encodedPassword 저장된 암호화된 비밀번호(BCrypt 등)
     * @param rawPassword 사용자가 로그인 시 입력한 평문 비밀번호
     */
    private void verifyPassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
    }
}
