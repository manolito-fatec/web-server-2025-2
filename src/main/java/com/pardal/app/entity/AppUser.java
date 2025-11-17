package com.pardal.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "app_users", schema = "pardal")
public class AppUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usr_id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "usr_encryptedName", nullable = false, length = 1000)
    private String encryptedName;

    @NotNull
    @Column(name = "usr_encryptedEmail", nullable = false, length = 1000)
    private String encryptedEmail;

    @Column(name = "usr_emailHash", length = 64)
    private String emailHash;

    @Column(name = "usr_encryptedPhone", nullable = false, length = 1000)
    private String encryptedPhone;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rl_id", nullable = false)
    private AppRole role;

    @Column(name = "usr_expire_date")
    private LocalDate expireDate;

    @Size(max = 255)
    @Column(name = "usr_pwd")
    private String password;

    @Column(name = "usr_email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "usr_verification_token")
    private String verificationToken;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.role == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.role.getRlName().toUpperCase()));
    }

    @Override
    public String getUsername() {
        return this.emailHash;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.expireDate == null || this.expireDate.isAfter(LocalDate.now());
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.emailVerified;
    }
}