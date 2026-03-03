package com.alessandro.congress_management.security;

import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.repositories.authenticate.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Buscar usuario en la base de datos
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuario no encontrado: " + username));

        // Verificar que el usuario esté activo
        if (!user.getIsActive()) {
            throw new UsernameNotFoundException("Usuario inactivo: " + username);
        }

        // IMPORTANTE: Convertir el rol a GrantedAuthority
        Collection<GrantedAuthority> authorities = getAuthorities(user);

        // Convertir a UserDetails de Spring Security CON ROLES
        return new CustomUserDetails(
                user.getIdUser(),           // ← ID DEL USUARIO
                user.getUsername(),
                user.getPassword(),
                true,                        // enabled
                true,                        // accountNonExpired
                true,                        // credentialsNonExpired
                true,                        // accountNonLocked
                getAuthorities(user)
        );
    }


    private Collection<GrantedAuthority> getAuthorities(UserEntity user) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (user.getRole() != null) {
            // Agregar el rol con prefijo ROLE_
            // Spring Security espera que los roles tengan este prefijo
            String roleName = user.getRole().getRoleName();

            // Si el rol no tiene el prefijo ROLE_, agregarlo
            if (!roleName.startsWith("ROLE_")) {
                roleName = "ROLE_" + roleName;
            }

            authorities.add(new SimpleGrantedAuthority(roleName));
        }

        return authorities;
    }
}