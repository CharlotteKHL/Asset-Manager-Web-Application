package com.team05.assetsrepo;

import java.util.Collection;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class CustomUserDetailsService implements UserDetailsService {
    private UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Test");
        UserEntity user = userRepository.findByUsername(username);
        if(user == null) throw new BadCredentialsException("Incorrect username or password!");
        
     // Add this for testing purposes
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        authorities.forEach(auth -> System.out.println(auth.getAuthority()));
        return user;
    }
    
}
