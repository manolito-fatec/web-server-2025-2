package com.pardal.app.service.appUser;

import com.pardal.app.entity.AppRole;
import com.pardal.app.entity.AppUser;
import com.pardal.app.entity.dto.AppUserDto;
import com.pardal.app.entity.dto.UpdateUserRoleDto;
import com.pardal.app.mail.EmailService;
import com.pardal.app.repository.AppRoleRepository;
import com.pardal.app.repository.AppUserRepository;
import com.pardal.app.repository.UserRepository;
import com.pardal.app.service.vault.HashService;
import com.pardal.app.service.vault.VaultEncryptionService;
import lombok.RequiredArgsConstructor;
import com.pardal.app.service.vault.VaultEncryptionService.EncryptedData;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AppUserService implements UserDetailsService {

    private final AppUserRepository appUserRepository;
    private final AppRoleRepository appRoleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final VaultEncryptionService vaultEncryptionService;
    private final HashService hashService;


    /**
     * Converts an AppUser entity to its DTO representation.
     * <p>
     * This private method handles the conversion between the entity and DTO,
     * including mapping roles to their names.
     * </p>
     *
     * @param appUser the user entity to convert (must not be null)
     * @return the converted user DTO
     * @see AppUserDto
     */
    public AppUserDto convertUserToDto(AppUser appUser) {
        return AppUserDto.builder()
                .id(appUser.getId())
                .name(vaultEncryptionService.decryptWithEnvelope(new EncryptedData(
                        appUser.getEncryptedName(),
                        appUser.getNameDEK()
                )))
                .email(vaultEncryptionService.decryptWithEnvelope(new EncryptedData(
                        appUser.getEncryptedEmail(),
                        appUser.getEmailDEK()
                )))
                .phone(vaultEncryptionService.decryptWithEnvelope(new EncryptedData(
                        appUser.getEncryptedPhone(),
                        appUser.getPhoneDEK()
                )))
                .role(appUser.getRole())
                .expireDate(appUser.getExpireDate())
                .password(appUser.getPassword())
                .build();
    }

    /**
     * Retrieves a user by their ID.
     * <p>
     * This method fetches a user from the repository and converts it to a DTO.
     * </p>
     *
     * @param id the ID of the user to retrieve (must not be null)
     * @return the user DTO containing user information
     * @throws NoSuchElementException if no user is found with the given ID
     * @example <pre>{@code
     * // Get user with ID 123
     * AppUserDto user = appUserService.getUserById(123);
     * }</pre>
     * @see AppUserDto
     */
    public AppUserDto getUserById(Integer id) {
        Optional<AppUser> user = appUserRepository.findById(id);
        if (user.isEmpty()) {
            throw new NoSuchElementException();
        }
        return convertUserToDto(user.get());
    }

    /**
     * Loads user details by email for authentication.
     * <p>
     * This method implements Spring Security's {@link UserDetailsService} interface
     * to support user authentication.
     * </p>
     *
     * @param email the email to search for (must not be null or empty)
     * @return the user details for authentication
     * @throws UsernameNotFoundException if no user is found with the given email
     * @see UserDetails
     *
     * @example
     * <pre>{@code
     * // Used internally by Spring Security during authentication
     * UserDetails userDetails = userService.loadUserByUsername("test@example.com");
     * }</pre>
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<AppUser> user = appUserRepository.findByEmailHash(email);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        return user.get();
    }

    /**
     * Retrieves all users in the system.
     * <p>
     * This method fetches all users from the repository and converts them to DTOs.
     * </p>
     *
     * @return a list of user DTOs
     * @throws NoSuchElementException if no users exist in the system
     * @see AppUserDto
     *
     * @example
     * <pre>{@code
     * // Get all users
     * List<AppUserDto> users = userService.getAllUsers();
     * }</pre>
     */
    public List<AppUserDto> getAllUsers() {
        List<AppUser> users = userRepository.findAllByExpireDateIsNull();
        if (users.isEmpty()) {
            throw new NoSuchElementException("No users found");
        }
        List<AppUserDto> userDtos = new ArrayList<>();
        for (AppUser user : users) {
            userDtos.add(convertUserToDto(user));
        }
        return userDtos;
    }

    /**
     * Creates a new user.
     * <p>
     * This method creates a new user with the provided information, encoding the password
     * and validating the assigned roles.
     * </p>
     *
     * @param appUserDto the DTO containing new user information (must not be null)
     * @return the created user DTO
     * @throws IllegalArgumentException if the provided roles don't exist in the system
     * @see AppUserDto
     *
     * @example
     * <pre>{@code
     * // Create new user
     * AppUserDto newUser = userService.createUser(userDto);
     * }</pre>
     */
    public AppUserDto createUser(AppUserDto appUserDto) {

        String verificationToken = UUID.randomUUID().toString();

        EncryptedData encryptedEmail = vaultEncryptionService.encryptWithEnvelope(appUserDto.getEmail());

        EncryptedData encryptedPhone = vaultEncryptionService.encryptWithEnvelope(appUserDto.getPhone());

        EncryptedData encryptedName = vaultEncryptionService.encryptWithEnvelope(appUserDto.getName());


        AppUser appUser = AppUser.builder()
                .encryptedEmail(encryptedEmail.getEncryptedValue())
                .emailHash(hashService.hashEmail(appUserDto.getEmail()))
                .emailDEK(encryptedEmail.getEncryptedDEK())
                .encryptedPhone(encryptedPhone.getEncryptedValue())
                .phoneDEK(encryptedPhone.getEncryptedDEK())
                .encryptedName(encryptedName.getEncryptedValue())
                .nameDEK(encryptedName.getEncryptedDEK())
                .password(passwordEncoder.encode(appUserDto.getPassword()))
                .expireDate(LocalDate.now())
                .role(appRoleRepository.getAppRoleById(2))
                .emailVerified(false)
                .verificationToken(verificationToken)
                .build();

        AppUser newUser = userRepository.save(appUser);
        emailService.sendValidationEmail(vaultEncryptionService.decryptWithEnvelope(
                new EncryptedData(
                        newUser.getEncryptedEmail(),
                        newUser.getEmailDEK()
                )), newUser.getVerificationToken());

        return convertUserToDto(userRepository.save(appUser));
    }

    public AppUser getUser(String token) {
        Optional<AppUser> appuser = appUserRepository.getAppUserByVerificationToken(token);
        if (appuser.isEmpty()) {
            throw new NoSuchElementException("User not found with verification token: " + token);
        }
        return appuser.get();
    }

    public AppUser getUserByEmail(String email) {
        Optional<AppUser> appuser = appUserRepository.getAppUserByEmailHash(email);
        if (appuser.isEmpty()) {
            throw new IllegalArgumentException("User not found with email: " + email);
        }
        return appuser.get();
    }

    public AppUserDto updateUser(AppUser appUser) {
        appUserRepository.save(appUser);
        return convertUserToDto(appUser);
    }

    public AppUserDto updateUserRole(UpdateUserRoleDto appUserDto) {
        AppUser appUser = appUserRepository.findById(appUserDto.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        AppRole role = appRoleRepository.getByRlName(appUserDto.getRole());

        appUser.setRole(role);
        AppUser updatedUser = appUserRepository.save(appUser);

        return convertUserToDto(updatedUser);
    }

    /**
     * Deletes a user by their ID.
     * <p>
     * This method removes a user from the system after verifying their existence.
     * </p>
     *
     * @param id the ID of the user to delete (must not be null)
     * @return the DTO of the deleted user
     * @throws NoSuchElementException if no user exists with the given ID
     * @see AppUserDto
     *
     * @example
     * <pre>{@code
     * // Delete user with ID 123
     * ApplicationUserDto deletedUser = userService.deleteUser(123);
     * }</pre>
     */
    public AppUserDto deleteUser(Integer id) {
        Optional<AppUser> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new NoSuchElementException("User not found");
        }
        user.get().setExpireDate(LocalDate.now());
        updateUser(user.get());
        return convertUserToDto(user.get());
    }
}
