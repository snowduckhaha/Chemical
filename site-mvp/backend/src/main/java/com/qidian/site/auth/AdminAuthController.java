package com.qidian.site.auth;

import com.qidian.site.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/auth")
public class AdminAuthController {

    private final AuthenticationManager authenticationManager;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final CsrfTokenRepository csrfTokenRepository;
    private final HttpSessionSecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AdminAuthController(
        AuthenticationManager authenticationManager,
        AdminUserRepository adminUserRepository,
        PasswordEncoder passwordEncoder,
        CsrfTokenRepository csrfTokenRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.csrfTokenRepository = csrfTokenRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse
    ) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username().trim(), request.password())
            );
        } catch (org.springframework.security.core.AuthenticationException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(401, "invalid username or password"));
        }

        // Ensure a session exists before rotating its identifier to prevent session fixation.
        httpRequest.getSession();
        httpRequest.changeSessionId();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);
        adminUserRepository.updateLastLogin(authentication.getName());

        return ResponseEntity.ok(ApiResponse.ok(currentUser(authentication)));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        SecurityContextHolder.clearContext();
        return ApiResponse.ok(null);
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me(Authentication authentication) {
        return ApiResponse.ok(currentUser(authentication));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> changePassword(
        @Valid @RequestBody ChangePasswordRequest request,
        Authentication authentication
    ) {
        AdminUser user = adminUserRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.currentPassword(), user.passwordHash())) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(400, "current password is incorrect"));
        }
        if (passwordEncoder.matches(request.newPassword(), user.passwordHash())) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(400, "new password must differ from current password"));
        }
        if (request.newPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(400, "new password must be at most 72 bytes"));
        }

        adminUserRepository.updatePassword(user.username(), passwordEncoder.encode(request.newPassword()));
        return ResponseEntity.ok(ApiResponse.ok(Map.of("updated", true)));
    }

    @GetMapping("/csrf")
    public ApiResponse<Map<String, String>> csrf(HttpServletRequest request, HttpServletResponse response) {
        CsrfToken token = csrfTokenRepository.loadDeferredToken(request, response).get();
        return ApiResponse.ok(Map.of("headerName", token.getHeaderName(), "token", token.getToken()));
    }

    private Map<String, Object> currentUser(Authentication authentication) {
        String role = authentication.getAuthorities().stream()
            .findFirst()
            .map(authority -> authority.getAuthority().replace("ROLE_", ""))
            .orElseThrow();
        return Map.of("username", authentication.getName(), "role", role);
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record ChangePasswordRequest(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = 8, max = 72) String newPassword
    ) {
    }
}
