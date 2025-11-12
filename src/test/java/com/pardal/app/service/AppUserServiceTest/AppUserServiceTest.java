package com.pardal.app.service.AppUserServiceTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.*;

import com.pardal.app.entity.AppRole;
import com.pardal.app.entity.AppUser;
import com.pardal.app.entity.dto.AppUserDto;
import com.pardal.app.mail.EmailService;
import com.pardal.app.repository.AppRoleRepository;
import com.pardal.app.repository.AppUserRepository;
import com.pardal.app.repository.logging.LogEntryRepository;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private AppRoleRepository appRoleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private VaultEncryptionService vaultEncryptionService;

    @Mock
    private HashService hashService;

    @Mock
    private DekService dekService;

    @Mock
    private LogEntryRepository logRepository;

    @InjectMocks
    private AppUserService appUserService;

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

        // Setup user with encrypted fields
        testUser = new AppUser();
        testUser.setId(1);
        testUser.setEncryptedName("encryptedNameValue");
        testUser.setEncryptedEmail("encryptedEmailValue");
        testUser.setEmailHash("hashedEmail");
        testUser.setEncryptedPhone("encryptedPhoneValue");
        testUser.setPassword("hashedPassword");
        testUser.setRole(testRole);
        testUser.setEmailVerified(false);
        testUser.setExpireDate(null);
        testUser.setVerificationToken("test-token-123");

        testUserDto = new AppUserDto();
        testUserDto.setId(1);
        testUserDto.setName("Test User");
        testUserDto.setEmail("test@example.com");
        testUserDto.setPassword("password123");
        testUserDto.setPhone("123456789");
        testUserDto.setRole(testRole);
    }

    @Test
    @DisplayName("Should convert AppUser entity to AppUserDto successfully")
    void convertUserToDto_whenValidUser_shouldReturnDto() {
        when(dekService.findByUserId(1)).thenReturn(Optional.of(testDek));
        when(vaultEncryptionService.decryptWithEnvelope(any(EncryptedData.class)))
                .thenReturn("Test User", "test@example.com", "123456789");

        AppUserDto result = appUserService.convertUserToDto(testUser);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals("Test User", result.getName());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("123456789", result.getPhone());
        assertEquals(testUser.getRole(), result.getRole());

        verify(dekService, times(1)).findByUserId(1);
        verify(vaultEncryptionService, times(3)).decryptWithEnvelope(any(EncryptedData.class));
    }

    @Test
    @DisplayName("Should return user DTO when valid ID is provided")
    void getUserById_whenValidId_shouldReturnUserDto() {
        when(appUserRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(dekService.findByUserId(1)).thenReturn(Optional.of(testDek));
        when(vaultEncryptionService.decryptWithEnvelope(any(EncryptedData.class)))
                .thenReturn("Test User", "test@example.com", "123456789");

        AppUserDto result = appUserService.getUserById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Test User", result.getName());
        assertEquals("test@example.com", result.getEmail());

        verify(appUserRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should throw NoSuchElementException when user ID not found")
    void getUserById_whenIdNotFound_shouldThrowException() {
        when(appUserRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> appUserService.getUserById(999));

        verify(appUserRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("Should return user details when email hash is found")
    void loadUserByUsername_whenValidEmail_shouldReturnUserDetails() {
        when(appUserRepository.findByEmailHash("test@example.com")).thenReturn(Optional.of(testUser));

        var result = appUserService.loadUserByUsername("test@example.com");

        assertNotNull(result);
        assertEquals(testUser, result);

        verify(appUserRepository, times(1)).findByEmailHash("test@example.com");
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when email not found")
    void loadUserByUsername_whenEmailNotFound_shouldThrowException() {
        when(appUserRepository.findByEmailHash("notfound@example.com")).thenReturn(Optional.empty());

        UsernameNotFoundException thrown = assertThrows(
                UsernameNotFoundException.class,
                () -> appUserService.loadUserByUsername("notfound@example.com")
        );

        assertTrue(thrown.getMessage().contains("User not found with email"));

        verify(appUserRepository, times(1)).findByEmailHash("notfound@example.com");
    }

    @Test
    @DisplayName("Should return all users when users exist in system")
    void getAllUsers_whenUsersExist_shouldReturnUserList() {
        AppUser user2 = new AppUser();
        user2.setId(2);
        user2.setEncryptedName("encryptedName2");
        user2.setEncryptedEmail("encryptedEmail2");
        user2.setEmailHash("hashedEmail2");
        user2.setEncryptedPhone("encryptedPhone2");
        user2.setRole(testRole);

        DataEncryptionKey dek2 = DataEncryptionKey.builder()
                .nameDek("nameDek2")
                .emailDek("emailDek2")
                .phoneDek("phoneDek2")
                .referenceId(2)
                .build();

        List<AppUser> userList = Arrays.asList(testUser, user2);

        when(appUserRepository.findAllByExpireDateIsNull()).thenReturn(userList);
        when(dekService.findByUserId(1)).thenReturn(Optional.of(testDek));
        when(dekService.findByUserId(2)).thenReturn(Optional.of(dek2));
        when(vaultEncryptionService.decryptWithEnvelope(any(EncryptedData.class)))
                .thenReturn("Test User", "test@example.com", "123456789",
                        "Another User", "another@example.com", "987654321");

        List<AppUserDto> result = appUserService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(appUserRepository, times(1)).findAllByExpireDateIsNull();
    }

    @Test
    @DisplayName("Should throw NoSuchElementException when no users exist in system")
    void getAllUsers_whenNoUsersExist_shouldThrowException() {
        when(appUserRepository.findAllByExpireDateIsNull()).thenReturn(Collections.emptyList());

        NoSuchElementException thrown = assertThrows(
                NoSuchElementException.class,
                () -> appUserService.getAllUsers()
        );

        assertEquals("No users found", thrown.getMessage());

        verify(appUserRepository, times(1)).findAllByExpireDateIsNull();
    }

    @Test
    @DisplayName("Should create user successfully")
    void createUser_whenValidUserDto_shouldCreateUser() {
        when(vaultEncryptionService.encryptWithEnvelope("Test User")).thenReturn(encryptedName);
        when(vaultEncryptionService.encryptWithEnvelope("test@example.com")).thenReturn(encryptedEmail);
        when(vaultEncryptionService.encryptWithEnvelope("123456789")).thenReturn(encryptedPhone);
        when(hashService.hashEmail("test@example.com")).thenReturn("hashedEmail");
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(appRoleRepository.getAppRoleById(2)).thenReturn(testRole);
        when(appUserRepository.save(any(AppUser.class))).thenReturn(testUser);
        when(dekService.save(any(DataEncryptionKey.class))).thenReturn(testDek);
        when(dekService.findByUserId(1)).thenReturn(Optional.of(testDek));
        when(vaultEncryptionService.decryptWithEnvelope(any(EncryptedData.class)))
                .thenReturn("test@example.com", "Test User", "test@example.com", "123456789");

        AppUserDto result = appUserService.createUser(testUserDto);

        assertNotNull(result);
        verify(appUserRepository, times(1)).save(any(AppUser.class));
        verify(dekService, times(1)).save(any(DataEncryptionKey.class));
        verify(emailService, times(1)).sendValidationEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("Should retrieve user by verification token successfully")
    void getUser_whenValidToken_shouldReturnUser() {
        when(appUserRepository.getAppUserByVerificationToken("test-token-123"))
                .thenReturn(Optional.of(testUser));

        AppUser result = appUserService.getUser("test-token-123");

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("test-token-123", result.getVerificationToken());

        verify(appUserRepository, times(1)).getAppUserByVerificationToken("test-token-123");
    }

    @Test
    @DisplayName("Should throw NoSuchElementException when verification token not found")
    void getUser_whenTokenNotFound_shouldThrowException() {
        when(appUserRepository.getAppUserByVerificationToken("invalid-token"))
                .thenReturn(Optional.empty());

        NoSuchElementException thrown = assertThrows(
                NoSuchElementException.class,
                () -> appUserService.getUser("invalid-token")
        );

        assertEquals("User not found with verification token: invalid-token", thrown.getMessage());

        verify(appUserRepository, times(1)).getAppUserByVerificationToken("invalid-token");
    }

    @Test
    @DisplayName("Should retrieve user by email hash successfully")
    void getUserByEmail_whenValidEmail_shouldReturnUser() {
        when(appUserRepository.getAppUserByEmailHash("test@example.com"))
                .thenReturn(Optional.of(testUser));

        AppUser result = appUserService.getUserByEmail("test@example.com");

        assertNotNull(result);
        assertEquals(1, result.getId());

        verify(appUserRepository, times(1)).getAppUserByEmailHash("test@example.com");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when email not found")
    void getUserByEmail_whenEmailNotFound_shouldThrowException() {
        when(appUserRepository.getAppUserByEmailHash("notfound@example.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> appUserService.getUserByEmail("notfound@example.com")
        );

        assertEquals("User not found with email: notfound@example.com", thrown.getMessage());

        verify(appUserRepository, times(1)).getAppUserByEmailHash("notfound@example.com");
    }

    @Test
    @DisplayName("Should update user successfully")
    void updateUser_whenValidUser_shouldUpdateAndReturnDto() {
        when(appUserRepository.save(testUser)).thenReturn(testUser);
        when(dekService.findByUserId(1)).thenReturn(Optional.of(testDek));
        when(vaultEncryptionService.decryptWithEnvelope(any(EncryptedData.class)))
                .thenReturn("Test User", "test@example.com", "123456789");

        AppUserDto result = appUserService.updateUser(testUser);

        assertNotNull(result);
        assertEquals(1, result.getId());

        verify(appUserRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Should delete user by setting expire date")
    void deleteUser_whenValidId_shouldSetExpireDate() {
        when(appUserRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(appUserRepository.save(any(AppUser.class))).thenReturn(testUser);
        when(dekService.findByUserId(1)).thenReturn(Optional.of(testDek));
        when(vaultEncryptionService.decryptWithEnvelope(any(EncryptedData.class)))
                .thenReturn("Test User", "test@example.com", "123456789");

        AppUserDto result = appUserService.deleteUser(1);

        assertNotNull(result);
        assertNotNull(testUser.getExpireDate());
        assertEquals(LocalDate.now(), testUser.getExpireDate());

        verify(appUserRepository, times(1)).findById(1);
        verify(appUserRepository, times(1)).save(any(AppUser.class));
    }

    @Test
    @DisplayName("Should throw NoSuchElementException when deleting non-existent user")
    void deleteUser_whenIdNotFound_shouldThrowException() {
        when(appUserRepository.findById(999)).thenReturn(Optional.empty());

        NoSuchElementException thrown = assertThrows(
                NoSuchElementException.class,
                () -> appUserService.deleteUser(999)
        );

        assertEquals("User not found", thrown.getMessage());

        verify(appUserRepository, times(1)).findById(999);
    }
}