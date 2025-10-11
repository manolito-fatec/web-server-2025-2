package com.pardal.app.service.appUser;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface AppUserService {
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
