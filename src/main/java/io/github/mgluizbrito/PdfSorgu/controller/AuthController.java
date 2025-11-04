package io.github.mgluizbrito.PdfSorgu.controller;

import io.github.mgluizbrito.PdfSorgu.dto.LoginRequest;
import io.github.mgluizbrito.PdfSorgu.dto.AuthResponse;
import io.github.mgluizbrito.PdfSorgu.dto.RegisterRequest;
import io.github.mgluizbrito.PdfSorgu.model.RoleEnum;
import io.github.mgluizbrito.PdfSorgu.model.User;
import io.github.mgluizbrito.PdfSorgu.repository.UserRepository;
import io.github.mgluizbrito.PdfSorgu.security.TokenService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User Login and Registration Endpoints")
public class AuthController {

    private final UserRepository repository;
    private final PasswordEncoder encoder;
    private final TokenService tokenService;

    @Value("${api.security.jwt.expiration-seconds:604800}")
    private long expiration;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {

        Optional<User> byEmail = repository.findByEmail(request.email());
        if (byEmail.isEmpty() || !encoder.matches(request.password(), byEmail.get().getPassword())){
            throw new BadCredentialsException("email or password is invalid");
        }

        String token = tokenService.generateToken(byEmail.get());
        return ResponseEntity.ok(new AuthResponse(
                byEmail.get().getEmail(), token, LocalDateTime.now().plusSeconds(expiration)
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        Optional<User> byEmail = repository.findByEmail(request.email());
        if (byEmail.isPresent()) return ResponseEntity.badRequest().build();

        User newUser = new User();
        newUser.setEmail(request.email());
        newUser.setPassword(encoder.encode(request.password()));
        newUser.setRoles(Set.of(RoleEnum.ROLE_USER));
        repository.save(newUser);

        String token = this.tokenService.generateToken(newUser);
        return ResponseEntity.ok(new AuthResponse(
                request.email(), token, LocalDateTime.now().plusSeconds(expiration)
        ));
    }
}
