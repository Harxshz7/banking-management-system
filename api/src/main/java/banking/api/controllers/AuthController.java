package banking.api.controllers;

import banking.api.dto.LoginRequest;
import banking.api.security.JwtUtil;
import banking.models.User;
import banking.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        User user = authService.loginStateless(req.username(), req.password());
        if (user != null) {
            String token = jwtUtil.generateToken(user.getId());
            return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getUsername(), user.getFullName()));
        }
        return ResponseEntity.status(401).body("Invalid credentials or account inactive");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        boolean success = authService.register(req.username(), req.password(), req.fullName(), req.email(), req.phone());
        if (success) {
            return ResponseEntity.ok("User registered successfully");
        }
        return ResponseEntity.badRequest().body("Registration failed. Username may already exist.");
    }

    public record RegisterRequest(String username, String password, String fullName, String email, String phone) {}
    public record AuthResponse(String token, String id, String username, String fullName) {}
}
