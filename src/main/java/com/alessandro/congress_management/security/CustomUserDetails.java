package com.alessandro.congress_management.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUserDetails extends User {


    private final Long idUser;

    public CustomUserDetails(
            Long idUser,
            String username,
            String password,
            Collection<? extends GrantedAuthority> authorities) {

        super(username, password, authorities);
        this.idUser = idUser;
    }

    public CustomUserDetails(
            Long idUser,
            String username,
            String password,
            boolean enabled,
            boolean accountNonExpired,
            boolean credentialsNonExpired,
            boolean accountNonLocked,
            Collection<? extends GrantedAuthority> authorities) {

        super(username, password, enabled, accountNonExpired,
                credentialsNonExpired, accountNonLocked, authorities);
        this.idUser = idUser;
    }
}