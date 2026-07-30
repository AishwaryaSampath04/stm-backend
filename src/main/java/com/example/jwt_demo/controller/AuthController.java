package com.example.jwt_demo.controller;

import com.example.jwt_demo.model.User;
import com.example.jwt_demo.repository.UserRepository;
import com.example.jwt_demo.security.JwtUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtil jwtUtils;

    // ----- LOGIN -----
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody User user) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtUtils.generateToken(userDetails.getUsername());

        // Fetch user info from DB
        User loggedUser = userRepository.findByUsername(user.getUsername());

        // Prepare response with token + user info
        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", jwt);
        response.put("id", loggedUser.getId());
        response.put("username", loggedUser.getUsername());
        response.put("roleId", loggedUser.getRoleId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Map<String, Object>>> getRoles() {

        String sql = "SELECT role_id, role_name FROM role_master ORDER BY role_id";

        System.out.println("Executing SQL: " + sql);

        List<Map<String, Object>> roles = jdbcTemplate.queryForList(sql);

        System.out.println("Roles: " + roles);

        return ResponseEntity.ok(roles);
    }

    // ----- SIGNUP -----
@PostMapping("/signup")
public ResponseEntity<?> registerUser(@RequestBody User user) {
    if (userRepository.existsByUsername(user.getUsername())) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Error: Username is already taken!");
        return ResponseEntity.ok(response);
    }

    User newUser = new User(
            null,
            user.getUsername(),
            encoder.encode(user.getPassword()),
            user.getRoleId()
    );

    userRepository.save(newUser);

    return ResponseEntity.ok(newUser);
}
    // ----- CHANGE PASSWORD BY USERNAME -----
    @PutMapping("/changepassword")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String newPassword = request.get("password");

        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Password updated successfully!");
        return ResponseEntity.ok(response);
    }


}
