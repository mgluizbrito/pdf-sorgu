package io.github.mgluizbrito.PdfSorgu.security;

import io.github.mgluizbrito.PdfSorgu.model.User;
import io.github.mgluizbrito.PdfSorgu.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService service;
    private final UserRepository repository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = this.recoverToken(request);
        String login = service.validateToken(token);

        if (login == null) {
            filterChain.doFilter(request, response);
            return;
        }

        User user = repository.findByEmail(login).orElse(null);
        List<SimpleGrantedAuthority> roleUser = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, roleUser);
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request){
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null) return null;

        return authHeader.replace("Bearer ", "");
    }
}
