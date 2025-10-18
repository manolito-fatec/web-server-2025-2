package com.pardal.app.service.appUser;

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
import com.pardal.app.repository.UserRepository;
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
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AppUserService appUserService;

    private AppUser testUser;
    private AppUserDto testUserDto;
    private AppRole testRole;

    @BeforeEach
    void setUp() {
        testRole = new AppRole();
        testRole.setId(2);
        testRole.setRlName("USER");

        testUser = new AppUser();
        testUser.setId(1);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setPhone("123456789");
        testUser.setRole(testRole);
        testUser.setEmailVerified(false);
        testUser.setExpireDate(LocalDate.now());
        testUser.setVerificationToken("test-token-123");

        testUserDto = new AppUserDto();
        testUserDto.setId(1);
        testUserDto.setName("Test User");
        testUserDto.setEmail("test@example.com");
        testUserDto.setPassword("hashedPassword");
        testUserDto.setPhone("123456789");
        testUserDto.setRole(testRole);
        testUserDto.setExpireDate(LocalDate.now());
    }

    @Test
    @DisplayName("Should convert AppUser entity to AppUserDto successfully")
    void convertUserToDto_whenValidUser_shouldReturnDto() {
        AppUserDto result = appUserService.convertUserToDto(testUser);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getName(), result.getName());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getPhone(), result.getPhone());
        assertEquals(testUser.getRole(), result.getRole());
        assertEquals(testUser.getPassword(), result.getPassword());
    }

    @Test
    @DisplayName("Should return user DTO when valid ID is provided")
    void getUserById_whenValidId_shouldReturnUserDto() {
        when(appUserRepository.findById(1)).thenReturn(Optional.of(testUser));

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

        NoSuchElementException thrown = assertThrows(
                NoSuchElementException.class,
                () -> appUserService.getUserById(999),
                "Expected NoSuchElementException when user not found"
        );

        verify(appUserRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("Should return user details when email is found")
    void loadUserByUsername_whenValidEmail_shouldReturnUserDetails() {
        when(appUserRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        var result = appUserService.loadUserByUsername("test@example.com");

        assertNotNull(result);
        assertEquals(testUser, result);

        verify(appUserRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when email not found")
    void loadUserByUsername_whenEmailNotFound_shouldThrowException() {
        when(appUserRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        UsernameNotFoundException thrown = assertThrows(
                UsernameNotFoundException.class,
                () -> appUserService.loadUserByUsername("notfound@example.com"),
                "Expected UsernameNotFoundException when user not found"
        );

        assertTrue(thrown.getMessage().contains("User not found with email"));

        verify(appUserRepository, times(1)).findByEmail("notfound@example.com");
    }

    @Test
    @DisplayName("Should return all users when users exist in system")
    void getAllUsers_whenUsersExist_shouldReturnUserList() {
        AppUser user2 = new AppUser();
        user2.setId(2);
        user2.setName("Another User");
        user2.setEmail("another@example.com");
        user2.setPhone("987654321");
        user2.setRole(testRole);

        List<AppUser> userList = Arrays.asList(testUser, user2);

        when(userRepository.findAll()).thenReturn(userList);

        List<AppUserDto> result = appUserService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test User", result.get(0).getName());
        assertEquals("Another User", result.get(1).getName());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should throw NoSuchElementException when no users exist in system")
    void getAllUsers_whenNoUsersExist_shouldThrowException() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        NoSuchElementException thrown = assertThrows(
                NoSuchElementException.class,
                () -> appUserService.getAllUsers(),
                "Expected NoSuchElementException when no users found"
        );

        assertEquals("No users found", thrown.getMessage());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should retrieve user by verification token")
    void getUser_whenValidToken_shouldReturnUser() {
        when(appUserRepository.getAppUserByVerificationToken("test-token-123"))
                .thenReturn(Optional.of(testUser));

        AppUser result = appUserService.getUser("test-token-123");

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Test User", result.getName());
        assertEquals("test-token-123", result.getVerificationToken());

        verify(appUserRepository, times(1)).getAppUserByVerificationToken("test-token-123");
    }

    @Test
    @DisplayName("Should throw NoSuchElementException when token not found")
    void getUser_whenTokenNotFound_shouldThrowException() {
        when(appUserRepository.getAppUserByVerificationToken("invalid-token"))
                .thenReturn(Optional.empty());

        NoSuchElementException thrown = assertThrows(
                NoSuchElementException.class,
                () -> appUserService.getUser("invalid-token"),
                "Expected NoSuchElementException when token not found"
        );

        verify(appUserRepository, times(1)).getAppUserByVerificationToken("invalid-token");
    }

    @Test
    @DisplayName("Should retrieve user by email successfully")
    void getUserByEmail_whenValidEmail_shouldReturnUser() {
        when(appUserRepository.getAppUserByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        AppUser result = appUserService.getUserByEmail("test@example.com");

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("test@example.com", result.getEmail());

        verify(appUserRepository, times(1)).getAppUserByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when email not found")
    void getUserByEmail_whenEmailNotFound_shouldThrowException() {
        when(appUserRepository.getAppUserByEmail("notfound@example.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> appUserService.getUserByEmail("notfound@example.com"),
                "Expected IllegalArgumentException when user not found"
        );

        assertEquals("User not found", thrown.getMessage());

        verify(appUserRepository, times(1)).getAppUserByEmail("notfound@example.com");
    }

    @Test
    @DisplayName("Should update user successfully")
    void updateUser_whenValidUser_shouldUpdateAndReturnDto() {
        testUser.setName("Updated Name");

        when(appUserRepository.save(testUser)).thenReturn(testUser);

        AppUserDto result = appUserService.updateUser(testUser);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("test@example.com", result.getEmail());

        verify(appUserRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Should return user DTO after update")
    void updateUser_whenUserUpdated_shouldReturnUpdatedDto() {
        AppUser updatedUser = new AppUser();
        updatedUser.setId(1);
        updatedUser.setName("Updated User");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setPhone("999999999");
        updatedUser.setRole(testRole);

        when(appUserRepository.save(updatedUser)).thenReturn(updatedUser);

        AppUserDto result = appUserService.updateUser(updatedUser);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Updated User", result.getName());
        assertEquals("updated@example.com", result.getEmail());

        verify(appUserRepository, times(1)).save(updatedUser);
    }
}