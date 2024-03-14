package com.team05.assetsrepo;

import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user2")
public class UserEntity implements UserDetails {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id", nullable = false, unique = true)
    private int id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "role")
    private String role;
    
    public int getID() {
        return id;
    }

    public void setID(int id) {
        this.id = id;
    }

    public String getRole() {
      return role;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
      String userRole = getRole();
      
      if ("admin".equalsIgnoreCase(userRole)) {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
      } else if ("user".equalsIgnoreCase(userRole)) {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
      } else if ("viewer".equalsIgnoreCase(userRole)) {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_VIEWER"));
      } else {
        return Collections.emptyList();
      }
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
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
        return true;
    }
    
}
