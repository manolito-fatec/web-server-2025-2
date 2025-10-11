package com.pardal.app.service.appUser;

import com.pardal.app.entity.AppUser;
import com.pardal.app.entity.dto.AppUserDto;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

public interface AppUserService {
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
    public AppUserDto getUserById(Integer id) throws UsernameNotFoundException;
    public AppUserDto convertUserToDto(AppUser appUser);
    public List<AppUserDto> getAllUsers();
}
