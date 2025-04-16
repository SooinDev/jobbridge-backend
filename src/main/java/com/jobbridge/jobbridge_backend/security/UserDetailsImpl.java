package com.jobbridge.jobbridge_backend.security;

import com.jobbridge.jobbridge_backend.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class UserDetailsImpl implements UserDetails {

    private final User user;

    public UserDetailsImpl(User user) {
        this.user = user;
    }

    // 현재 사용자 반환 (커스텀용)
    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 사용자 권한이 따로 필요 없다면 빈 리스트 반환
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return user.getPw(); // User 엔티티에 pw 필드가 있다고 가정
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // email 로그인 기준일 경우
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
