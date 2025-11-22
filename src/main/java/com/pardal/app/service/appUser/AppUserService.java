package com.pardal.app.service.appUser;

import com.pardal.app.entity.AppRole;
import com.pardal.app.entity.AppUser;
import com.pardal.app.entity.dto.AppUserDto;
import com.pardal.app.entity.dto.AuditDto;
import com.pardal.app.entity.dto.UpdateUserRoleDto;
import com.pardal.app.entity.dto.UserInformationDto;
import com.pardal.app.entity.log.LogEntry;
import com.pardal.app.exceptions.AppUserNotUniqueException;
import com.pardal.app.mail.EmailService;
import com.pardal.app.repository.AppRoleRepository;
import com.pardal.app.repository.AppUserRepository;
import com.pardal.app.repository.UserRepository;
import com.pardal.app.service.vault.HashService;
import com.pardal.app.service.vault.VaultEncryptionService;
import com.pardal.dek.entity.DataEncryptionKey;
import com.pardal.app.service.dek.DekService;
import lombok.RequiredArgsConstructor;
import com.pardal.app.service.vault.VaultEncryptionService.EncryptedData;
import com.pardal.app.repository.logging.LogEntryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AppUserService implements UserDetailsService {

    private final AppUserRepository appUserRepository;
    private final AppRoleRepository appRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final LogEntryRepository logRepository;
    private final EmailService emailService;
    private final VaultEncryptionService vaultEncryptionService;
    private final HashService hashService;
    private final DekService dekService;

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
        Optional<DataEncryptionKey> dataEncryptionKey = dekService.findByUserId(appUser.getId());
        return AppUserDto.builder()
                .id(appUser.getId())
                .name(vaultEncryptionService.decryptWithEnvelope(new EncryptedData(
                        appUser.getEncryptedName(),
                        dataEncryptionKey.get().getNameDek()
                )))
                .email(vaultEncryptionService.decryptWithEnvelope(new EncryptedData(
                        appUser.getEncryptedEmail(),
                        dataEncryptionKey.get().getEmailDek()
                )))
                .phone(vaultEncryptionService.decryptWithEnvelope(new EncryptedData(
                        appUser.getEncryptedPhone(),
                        dataEncryptionKey.get().getPhoneDek()
                )))
                .role(appUser.getRole())
                .expireDate(appUser.getExpireDate())
                .password(appUser.getPassword())
                .emailVerified(appUser.getEmailVerified())
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
            log.error("User with ID: "+id+" Doesn't exist");
            throw new NoSuchElementException();
        }
        return convertUserToDto(user.get());
    }

    /**
     * Retrieves a user by their email.
     * <p>
     * This method fetches a user from the repository and converts it to a DTO.
     * </p>
     *
     * @param email the email of the user to retrieve (must not be null)
     * @return the user DTO containing user information
     * @throws NoSuchElementException if no user is found
     * @example <pre>{@code
     * // Get user with ID 123
     * AppUserDto user = appUserService.getUserDtoByEmail(email@eae.com);
     * }</pre>
     * @see AppUserDto
     */
    public AppUserDto getUserDtoByEmail(String email) {
        Optional<AppUser> appuser = appUserRepository.getAppUserByEmailHash(email);
        if (appuser.isEmpty()) {
            throw new IllegalArgumentException("User not found with email: " + email);
        }
        return convertUserToDto(appuser.get());
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
        List<AppUser> users = appUserRepository.findAllByEmailHashAndExpireDateIsNull(email);
        if (users.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        List<AppUser> validatedAppUsers = validateAppUserList(users);
        if (validatedAppUsers.size() != 1){
            log.error("Invalid user found with email: "+email);
            throw new AppUserNotUniqueException("User with email: " + email + " has more than one account!");
        }
        return validatedAppUsers.getFirst();
    }

    /**
     * Validates the integrity of a list of {@code AppUser} entities by checking
     * for the existence of their corresponding Data Encryption Keys (DEKs).
     * <p>
     * This method ensures that for every user record found in the primary database
     * ({@code app_users}), there is a corresponding encryption key stored in the
     * secondary key vault database ({@code db-dek}). This check is crucial for
     * maintaining the integrity required by the Envelope Encryption architecture,
     * especially after operations like data migration or potential failures
     * in key creation.
     * </p>
     *
     * @param users A list of {@link AppUser} objects potentially retrieved by a unique identifier (e.g., email hash).
     * @return A new {@code List<AppUser>} containing only the users for whom a valid
     * Data Encryption Key exists in the key vault database.
     * @see com.pardal.dek.entity.DataEncryptionKey
     * @see com.pardal.app.service.dek.DekService#findByUserId(Integer)
     *
     * @example
     * <pre>{@code
     * List<AppUser> potentialUsers = appUserRepository.findAllByEmailHash(emailHash);
     * List<AppUser> validated = validateAppUserList(potentialUsers);
     * // Only users in 'validated' are considered viable for login or data access.
     * }</pre>
     */
    public List<AppUser> validateAppUserList(List<AppUser> users) {
        List<AppUser> validatedAppUsers = new ArrayList<>();
        for (AppUser appUser : users) {
            if (dekService.findByUserId(appUser.getId()).isPresent()) {
                validatedAppUsers.add(appUser);
            }
        }
        return validatedAppUsers;
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
        List<AppUser> users = appUserRepository.findAllByEmailHashIsNotNull();
        if (users.isEmpty()) {
            throw new NoSuchElementException("No users found");
        }
        List<AppUserDto> userDtos = new ArrayList<>();
        for (AppUser user : users) {
            if(dekService.findByUserId(user.getId()).isPresent()) {
                userDtos.add(convertUserToDto(user));
            }
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
                .encryptedPhone(encryptedPhone.getEncryptedValue())
                .encryptedName(encryptedName.getEncryptedValue())
                .password(passwordEncoder.encode(appUserDto.getPassword()))
                .expireDate(LocalDate.now())
                .role(appRoleRepository.getAppRoleById(2))
                .emailVerified(false)
                .verificationToken(verificationToken)
                .build();

        AppUser newUser = appUserRepository.save(appUser);

        dekService.save(DataEncryptionKey.builder()
                .emailDek(encryptedEmail.getEncryptedDEK())
                .phoneDek(encryptedPhone.getEncryptedDEK())
                .nameDek(encryptedName.getEncryptedDEK())
                .referenceId(newUser.getId())
                .build());


        emailService.sendPreRegistrationEmail(vaultEncryptionService.decryptWithEnvelope(
                new EncryptedData(
                        newUser.getEncryptedEmail(),
                        dekService.findByUserId(newUser.getId()).get().getEmailDek()
                        )));

        return convertUserToDto(newUser);
    }

    /**
     * Approves a user, sets their email as verified, and asynchronously sends the approval email.
     *
     * @param appUserId the ID of the user to be approved
     * @author paulo arantes
     * @return an AppUserDto representing the approved user
     */
    @Transactional
    public AppUserDto approvalUser(Integer appUserId) {
        AppUser user =  getUserAllAttributes(appUserId);
        user.setEmailVerified(true);
        user.setExpireDate(null);
        Optional<DataEncryptionKey> deks = dekService.findByUserId(appUserId);
        if (deks.isPresent()) {
            emailService.sendApprovalEmail(vaultEncryptionService.decryptWithEnvelope(new EncryptedData(
                    user.getEncryptedEmail(), deks.get().getEmailDek()
            )));
            return convertUserToDto(user);
        }
        return null;
    }

    /**
     * Retrieves an AppUser from the repository by ID.
     *
     * @param userId the ID of the user to retrieve
     * @return the AppUser object if found
     * @author paulo arantes
     * @throws NoSuchElementException if a user with the given ID does not exist
     */
    private AppUser getUserAllAttributes(Integer userId)
    {
        Optional<AppUser> user = appUserRepository.findById(userId);
        if (user.isEmpty()) {
            log.error("Usuario com id: "+userId+" não existe");
            throw new NoSuchElementException();
        }
        return user.get();
    }

    /**
     Updates a user's profile data (name and email).
     <p>
     This method retrieves a user by their ID, updates their name and email
     based on the data provided in the DTO, and saves the changes.
     </p>
     @param appUserDto the DTO containing the ID and new data (name, email)
     @return the updated user DTO
     @throws NoSuchElementException if no user is found with the given ID
     @see AppUserDto*/
    public AppUserDto updateProfile(AppUserDto appUserDto) {
        AppUser existingUser = appUserRepository.findById(appUserDto.getId()).orElseThrow(() -> new NoSuchElementException("Usuário não encontrado com ID: " + appUserDto.getId()));

        EncryptedData encryptedName = vaultEncryptionService.encryptWithEnvelope(appUserDto.getName());
        DataEncryptionKey userDek = dekService.findByUserId(appUserDto.getId()).get();
        userDek.setNameDek(encryptedName.getEncryptedDEK());
        dekService.updateDek(userDek);
        existingUser.setEncryptedName(encryptedName.getEncryptedValue());
        existingUser.setPassword(passwordEncoder.encode(appUserDto.getPassword()));

        AppUser savedUser = appUserRepository.save(existingUser);

        return convertUserToDto(savedUser);
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
    public boolean deleteUser(Integer id) {
        Optional<AppUser> user = appUserRepository.findById(id);
        if (user.isEmpty()) {
            throw new NoSuchElementException("User not found");
        }
        user.get().setExpireDate(LocalDate.now());
        user.get().setEmailHash(null);
        appUserRepository.save(user.get());

        boolean dekCleaned = dekService.deleteByUserId(id);
        if(dekCleaned){
            return true;
        }
        return false;
    }

    /**
     * Retrieves all essential information for a specific user.
     * * This method creates a new {@code UserInformationDto}, populates its
     * audit information by calling {@code getAuditInformation()}, and returns the resulting DTO.
     * Currently, it only sets the audit information.
     *
     * @param id The unique identifier (ID) of the user whose information is to be retrieved.
     * @author paulo arantes
     * @return A {@code UserInformationDto} object containing the requested user's information,
     * including audit details.
     */
    public UserInformationDto getAllInformationAboutUser(Integer id)
    {
        UserInformationDto userInfo = new UserInformationDto();
        var user = getUserById(id);

        if(user.getRole().getRlName().equals("Admin")) {
            userInfo.setAuditInfomation(getTop5AuditInformation());
        } else {
            userInfo.setAuditInfomation(getTop5AuditInformationByUser(user.getEmail()));
        }

        AppUserDto appUserDto = getUserById(id);
        filterPrivateInformation(appUserDto, userInfo);
        return userInfo;
    }

    /**
     * Retrieves all essential information for a specific user.
     * * This method creates a new {@code UserInformationDto}, populates its
     * audit information by calling {@code getAuditInformation()}, and returns the resulting DTO.
     * Currently, it only sets the audit information.
     *
     * @param email The email of the user whose information is to be retrieved.
     * @return A {@code UserInformationDto} object containing the requested user's information,
     * including audit details.
     */
    public UserInformationDto getAllInformationAboutUser(String email)
    {
        UserInformationDto userInfo = new UserInformationDto();
        var user = getUserDtoByEmail(email);

        if(user.getRole().getRlName().equals("Admin")) {
            userInfo.setAuditInfomation(getTop5AuditInformation());
        } else {
            userInfo.setAuditInfomation(getTop5AuditInformationByUser(user.getEmail()));
        }

        AppUserDto appUserDto = getUserDtoByEmail(email);
        filterPrivateInformation(appUserDto, userInfo);
        return userInfo;
    }

    /**
     * Retrieves the most recent audit information (logs) from the system.
     * * <p>It fetches the top 5 log entries that have an associated HTTP method
     * and converts them into a list of {@code AuditDto} objects. 
     * If an error occurs during log retrieval, it logs the error and returns an 
     * empty list.</p>
     *
     * @return A {@code List<AuditDto>} containing the top 5 recent audit entries.
     * @author paulo arantes
     * Returns an empty list if no logs are found or if an exception occurs.
     */
    private List<AuditDto> getTop5AuditInformation()
    {
        try {
            List<LogEntry> logs = logRepository.findTop5WithHttpMethod();
            return getAuditDtos(logs);

        } catch (Exception e) {
            log.error("Error ao tentar buscar a lista de logs");
            return Collections.emptyList();
        }
    }

    /**
     * Retrieves the most recent audit information (logs) for a specific user.
     * <p>It fetches the top 5 log entries associated with the given user email
     * that have an associated HTTP method and converts them into a list of {@code AuditDto} objects.</p>
     *
     * @param userEmail The email of the user whose logs are to be retrieved.
     * @return A {@code List<AuditDto>} containing the top 5 recent audit entries for the user.
     * Returns an empty list if no logs are found or if an exception occurs.
     */
    private List<AuditDto> getTop5AuditInformationByUser(String userEmail) {
        try {
            List<LogEntry> logs = logRepository.findTop5ByUserEmail(userEmail);

            return getAuditDtos(logs);

        } catch (Exception e) {
            log.error("Error ao tentar buscar a lista de logs para o usuário: {}", userEmail, e);
            return Collections.emptyList();
        }
    }

    private List<AuditDto> getAuditDtos(List<LogEntry> logs) {
        return logs.stream().map(log -> {
            AuditDto dto = new AuditDto();
            dto.setEvent(log.getTitle());
            dto.setUser(log.getUserEmail());
            dto.setDate(log.getTimestamp() != null ? log.getTimestamp().toString() : null);
            dto.setLocale(log.getRemoteIp());
            dto.setDetails(log.getMessage());
            return dto;
        }).collect(Collectors.toList());
    }

    private UserInformationDto filterPrivateInformation (AppUserDto appUserDto, UserInformationDto userInformationDto) {
        AppUserDto userDto = AppUserDto.builder()
                .id(appUserDto.getId())
                .email(appUserDto.getEmail())
                .phone(appUserDto.getPhone())
                .name(appUserDto.getName())
                .role(appUserDto.getRole())
                .build();

        userInformationDto.setAppUser(userDto);
        return userInformationDto;
    }

    /**
     * Retrieves a list of all audit logs from the repository.
     *
     * If an error occurs during retrieval, it logs the error and
     * returns an empty list to prevent application failure.
     *
     * @author paulo arantes
     * @return A {@code List<LogEntry>} containing all audit logs,
     * or an empty list if an exception occurs.
     */
    public List<LogEntry> getAllAuditLog()
    {
        try {
            return logRepository.findAllAuditLogs();
        }catch (Exception e) {
            log.error("Erro ao tentar buscar a lista de logs");
            return Collections.emptyList();
        }
    }
}
