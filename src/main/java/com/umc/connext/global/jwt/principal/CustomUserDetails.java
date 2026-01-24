package com.umc.connext.global.jwt.principal;

import com.umc.connext.common.enums.Role;
import com.umc.connext.domain.member.entity.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

public class CustomUserDetails implements UserDetails {
    private final Member member;
    private final String username; // JWT 기반
    private final Role role;       // JWT 기반

    public CustomUserDetails(Member member) {
        this.member = member;
        this.username = null;
        this.role = null;
    }

    // JWT 기반 생성자
    public CustomUserDetails(String username, Role role) {
        this.member = null;
        this.username = username;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {

                return getRole().toString();
            }
        });

        return collection;
    }

    @Override
    public String getPassword() {

        return member != null ? member.getPassword() : null;
    }

    @Override
    public String getUsername() {

        return member != null ? member.getUsername() : username;
    }

    public Role getRole() {
        return member != null ? member.getRole() : role;
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

