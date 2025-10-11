package com.pardal.app.service.appUser;

import com.pardal.app.entity.AppUser;
import com.pardal.app.entity.dto.AppUserDto;
import com.pardal.app.repository.AppUserRepository;
import com.pardal.app.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AppUserServiceImpl implements AppUserService, UserDetailsService {

    private final AppUserRepository appUserRepository;
    private final UserRepository userRepository;

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
    @Override
    public AppUserDto convertUserToDto(AppUser appUser) {
        return AppUserDto.builder()
                .id(appUser.getId())
                .name(appUser.getName())
                .email(appUser.getEmail())
                .phone(appUser.getPhone())
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
    @Override
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
        Optional<AppUser> user = appUserRepository.findByEmail(email);
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
    @Override
    public List<AppUserDto> getAllUsers() {
        List<AppUser> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new NoSuchElementException("No users found");
        }
        List<AppUserDto> userDtos = new ArrayList<>();
        for (AppUser user : users) {
            userDtos.add(convertUserToDto(user));
        }
        return userDtos;
    }
}
