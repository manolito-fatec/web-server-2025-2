package com.pardal.app.service.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pardal.app.entity.AppRole;
import com.pardal.app.entity.AppUser;
import com.pardal.app.entity.dto.auth.JwtAuthenticationResponseDto;
import com.pardal.app.entity.dto.auth.LoginRequestDto;
import com.pardal.app.entity.dto.auth.ResponseUserCreatedDto;
import com.pardal.app.entity.dto.auth.SignupRequestDto;
import com.pardal.app.entity.dto.AppUserDto;
import com.pardal.app.exceptions.AppUserNotFoundException;
import com.pardal.app.repository.AppRoleRepository;
import com.pardal.app.service.JwtService;
import com.pardal.app.service.appUser.AppUserService;
import com.pardal.app.service.dek.DekService;
import com.pardal.app.service.vault.HashService;
import com.pardal.app.service.vault.VaultEncryptionService;
import com.pardal.app.service.vault.VaultEncryptionService.EncryptedData;
import com.pardal.dek.entity.DataEncryptionKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AppUserService appUserService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private DekService dekService;

    @Mock
    private VaultEncryptionService vaultEncryptionService;

    @Mock
    private HashService hashService;

    @Mock
    private AppRoleRepository appRoleRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private LoginRequestDto loginRequest;
    private SignupRequestDto signupRequest;
    private AppUser testUser;
    private AppUserDto testUserDto;
    private AppRole testRole;
    private DataEncryptionKey testDek;
    private EncryptedData encryptedName;
    private EncryptedData encryptedEmail;
    private EncryptedData encryptedPhone;

    @BeforeEach
    void setUp() {
        testRole = new AppRole();
        testRole.setId(2);
        testRole.setRlName("USER");

        // Setup encrypted data
        encryptedName = new EncryptedData("encryptedNameValue", "nameDekValue");
        encryptedEmail = new EncryptedData("encryptedEmailValue", "emailDekValue");
        encryptedPhone = new EncryptedData("encryptedPhoneValue", "phoneDekValue");

        // Setup DEK
        testDek = DataEncryptionKey.builder()
                .nameDek("nameDekValue")
                .emailDek("emailDekValue")
                .phoneDek("phoneDekValue")
                .referenceId(1)
                .build();

        // Setup user entity with encrypted fields
        testUser = new AppUser();
        testUser.setId(1);
        testUser.setEncryptedName("encryptedNameValue");
        testUser.setEncryptedEmail("encryptedEmailValue");
        testUser.setEmailHash("hashedEmailValue");
        testUser.setEncryptedPhone("encryptedPhoneValue");
        testUser.setPassword("hashedPassword");
        testUser.setRole(testRole);
        testUser.setEmailVerified(false);
        testUser.setVerificationToken("test-token-123");
        testUser.setExpireDate(null);

        // Setup user DTO (decrypted data)
        testUserDto = new AppUserDto();
        testUserDto.setId(1);
        testUserDto.setName("Test User");
        testUserDto.setEmail("test@example.com");
        testUserDto.setPhone("123456789");
        testUserDto.setRole(testRole);
        testUserDto.setPassword("password123");

        loginRequest = new LoginRequestDto();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        signupRequest = new SignupRequestDto();
        signupRequest.setName("Test User");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setPhone("123456789");
    }

    @Test
    @DisplayName("Should return JWT token when credentials are valid")
    void login_whenValidCredentials_shouldReturnJwtToken() {
        String expectedToken = "jwt.token.here";

        when(hashService.hashEmail("test@example.com")).thenReturn("hashedEmailValue");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(appUserService.getUserByEmail("hashedEmailValue")).thenReturn(testUser);
        when(jwtService.generateToken(testUser)).thenReturn(expectedToken);

        JwtAuthenticationResponseDto result = authService.login(loginRequest);

        assertNotNull(result);
        assertEquals(expectedToken, result.getToken());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(appUserService, times(1)).getUserByEmail("hashedEmailValue");
        verify(jwtService, times(1)).generateToken(testUser);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when authentication fails")
    void login_whenAuthenticationFails_shouldThrowException() {
        when(hashService.hashEmail("test@example.com")).thenReturn("hashedEmailValue");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new AuthenticationException("Invalid credentials") {});

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(loginRequest),
                "Expected IllegalArgumentException when authentication fails"
        );

        assertEquals("Invalid credentials", thrown.getMessage());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Should successfully create user when valid signup request is provided")
    void signup_whenValidRequest_shouldCreateUser() {
        when(appUserService.createUser(any(AppUserDto.class))).thenReturn(testUserDto);

        ResponseUserCreatedDto result = authService.signup(signupRequest);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Test User", result.getName());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("USER", result.getRole());

        verify(appUserService, times(1)).createUser(any(AppUserDto.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when signup request is null")
    void signup_whenRequestIsNull_shouldThrowException() {
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> authService.signup(null),
                "Expected IllegalArgumentException when request is null"
        );

        assertEquals("Request cannot be null", thrown.getMessage());
        verifyNoInteractions(appUserService);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when email is blank")
    void signup_whenEmailIsBlank_shouldThrowException() {
        signupRequest.setEmail("");

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> authService.signup(signupRequest),
                "Expected IllegalArgumentException when email is blank"
        );

        assertEquals("Email cannot be null or blank", thrown.getMessage());
        verifyNoInteractions(appUserService);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when name is null")
    void signup_whenNameIsNull_shouldThrowException() {
        signupRequest.setName(null);

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> authService.signup(signupRequest),
                "Expected IllegalArgumentException when name is null"
        );

        assertEquals("Name cannot be null or blank", thrown.getMessage());
        verifyNoInteractions(appUserService);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when password is blank")
    void signup_whenPasswordIsBlank_shouldThrowException() {
        signupRequest.setPassword("   ");

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> authService.signup(signupRequest),
                "Expected IllegalArgumentException when password is blank"
        );

        assertEquals("Password cannot be null or blank", thrown.getMessage());
        verifyNoInteractions(appUserService);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when phone is null")
    void signup_whenPhoneIsNull_shouldThrowException() {
        signupRequest.setPhone(null);

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> authService.signup(signupRequest),
                "Expected IllegalArgumentException when phone is null"
        );

        assertEquals("Phone cannot be null or blank", thrown.getMessage());
        verifyNoInteractions(appUserService);
    }

    @Test
    @DisplayName("Should verify user and set emailVerified to true")
    void verify_whenValidToken_shouldVerifyUser() {
        String token = "valid.token";

        // User não verificado
        AppUser unverifiedUser = new AppUser();
        unverifiedUser.setId(1);
        unverifiedUser.setEncryptedName("encryptedNameValue");
        unverifiedUser.setEncryptedEmail("encryptedEmailValue");
        unverifiedUser.setEmailHash("hashedEmailValue");
        unverifiedUser.setEncryptedPhone("encryptedPhoneValue");
        unverifiedUser.setRole(testRole);
        unverifiedUser.setEmailVerified(false);
        unverifiedUser.setVerificationToken(token);

        // DTO retornado após update
        AppUserDto verifiedUserDto = new AppUserDto();
        verifiedUserDto.setId(1);
        verifiedUserDto.setName("Test User");
        verifiedUserDto.setEmail("test@example.com");
        verifiedUserDto.setPhone("123456789");
        verifiedUserDto.setRole(testRole);

        when(appUserService.getUser(token)).thenReturn(unverifiedUser);
        when(appUserService.updateUser(any(AppUser.class))).thenReturn(verifiedUserDto);

        ResponseUserCreatedDto result = authService.verify(token);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("encryptedNameValue", result.getName());
        assertEquals("encryptedEmailValue", result.getEmail());
        assertTrue(unverifiedUser.getEmailVerified());
        assertNull(unverifiedUser.getExpireDate());

        verify(appUserService, times(1)).getUser(token);
        verify(appUserService, times(1)).updateUser(unverifiedUser);
    }

    @Test
    @DisplayName("Should throw AppUserNotFoundException when user not found for token")
    void verify_whenUserNotFound_shouldThrowException() {
        String token = "invalid.token";

        when(appUserService.getUser(token)).thenReturn(null);

        AppUserNotFoundException thrown = assertThrows(
                AppUserNotFoundException.class,
                () -> authService.verify(token),
                "Expected AppUserNotFoundException when user not found"
        );

        assertEquals("User not found with the given token", thrown.getMessage());
        verify(appUserService, never()).updateUser(any());
    }
}