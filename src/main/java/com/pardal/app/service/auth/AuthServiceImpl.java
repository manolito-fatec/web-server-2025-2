package com.pardal.app.service.auth;

import com.pardal.app.entity.AppUser;
import com.pardal.app.entity.dto.*;
import com.pardal.app.entity.dto.auth.JwtAuthenticationResponseDto;
import com.pardal.app.entity.dto.auth.LoginRequestDto;
import com.pardal.app.entity.dto.auth.ResponseUserCreatedDto;
import com.pardal.app.entity.dto.auth.SignupRequestDto;
import com.pardal.app.exceptions.AppUserNotFoundException;
import com.pardal.app.repository.AppRoleRepository;
import com.pardal.app.service.JwtService;
import com.pardal.app.service.appUser.AppUserService;
import com.pardal.app.service.vault.HashService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final HashService hashService;
    private final AppUserService appUserService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    /**
     * Authenticates a user using the provided login credentials and returns a JWT token upon success.
     *
     * <p>The method attempts to authenticate the user using the Spring Security {@link AuthenticationManager}.
     * If authentication is successful, it retrieves the corresponding {@link AppUser} from the repository
     * and generates a JWT token using the {@link JwtService}.</p>
     *
     * @param request the {@link LoginRequestDto} containing the user's email and password
     * @return a {@link JwtAuthenticationResponseDto} containing the generated JWT token
     * @throws IllegalArgumentException if authentication fails or the user is not found
     */
    public JwtAuthenticationResponseDto login(LoginRequestDto request) {
        request.setEmail(hashService.hashEmail(request.getEmail()));
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));

            var user = appUserService.getUserByEmail(request.getEmail());


            var jwt = jwtService.generateToken(user);
            return JwtAuthenticationResponseDto.builder().token(jwt).build();
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("Invalid credentials", e);
        }
    }

    /**
     * Handles the user signup process by validating the input request,
     * creating a new {@link AppUser}, saving it to the repository,
     * and returning a simplified DTO with user details.
     *
     * <p>This method is transactional to ensure that user creation and account
     * creation happen atomically.</p>
     *
     * @param request the {@link SignupRequestDto} containing user registration data
     * @return a {@link ResponseUserCreatedDto} containing selected details of the newly registered user
     * @throws IllegalArgumentException if the request is invalid, roles are missing/invalid, or tool is not found
     */
    @Transactional
    @Override
    public ResponseUserCreatedDto signup(SignupRequestDto request) {
        validateRequest(request);

        AppUserDto appUserDto = AppUserDto.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(request.getPassword())
                .build();

        AppUserDto registeredUser =  appUserService.createUser(appUserDto);

        return new ResponseUserCreatedDto(
                registeredUser.getId(),
                registeredUser.getName(),
                registeredUser.getEmail(),
                registeredUser.getRole().getRlName()
        );
    }

    @Override
    public ResponseUserCreatedDto verify(String token) {
        AppUser appUser = appUserService.getUser(token);
        if (appUser == null) {
            throw new AppUserNotFoundException("User not found with the given token");
        }
        appUser.setEmailVerified(true);
        appUser.setExpireDate(null);

        appUserService.updateUser(appUser);

        return new ResponseUserCreatedDto(
                appUser.getId(),
                appUser.getEncryptedName(),
                appUser.getEncryptedEmail(),
                appUser.getRole().getRlName()
        );

    }

    /**
     * Validates the contents of a {@link SignupRequestDto} object.
     *
     * <p>This method ensures that:
     * <ul>
     *     <li>The request object is not null</li>
     *     <li>The roles list is not null or empty</li>
     *     <li>Required fields (email, username, password, toolUserId) are not null or blank</li>
     *     <li>The toolId is not null</li>
     * </ul>
     *
     * @param request the {@code SignupRequestDto} to validate
     * @throws IllegalArgumentException if any field is invalid or missing
     */
    protected void validateRequest(SignupRequestDto request)
    {
        if (request == null)
        {
            throw new IllegalArgumentException("Request cannot be null");
        }

        validateField(request.getEmail(), "Email");
        validateField(request.getName(), "Name");
        validateField(request.getPassword(), "Password");
        validateField(request.getPhone(), "Phone");
    }

    /**
     * Validates that the given string field is neither null nor blank.
     *
     * @param field the string value to validate
     * @param fieldName the name of the field being validated, used for error messaging
     * @throws IllegalArgumentException if the field is null or blank
     */
    private void validateField(String field, String fieldName)
    {
        if (field == null || field.isBlank())
        {
            throw new IllegalArgumentException(fieldName + " cannot be null or blank");
        }
    }
}
