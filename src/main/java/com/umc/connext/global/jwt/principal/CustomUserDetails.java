package com.umc.connext.global.jwt.principal;

import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.member.enums.MemberStatus;
import lombok.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomUserDetails implements UserDetails, OAuth2User {

    private final Member member;

    public CustomUserDetails(Member member) {
        this.member = member;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @NonNull
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> member.getRole().name());
    }

    @NonNull
    @Override
    public String getName() {
        return member.getNickname();
    }

    @Override
    public String getPassword() {
        return member != null ? member.getPassword() : null;
    }

    @NonNull
    @Override //로컬 로그인
    public String getUsername() {
        return member.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {return true;}

    @Override
    public boolean isAccountNonLocked() {return true;}

    @Override
    public boolean isCredentialsNonExpired() {return true;}

    @Override
    public boolean isEnabled() {return true;}

    public Long getMemberId() {
        return member.getId();
    }

    public MemberStatus getMemberStatus() {return  member.getMemberStatus();}

    public String getNickname() {
        return member.getNickname();
    }
}
