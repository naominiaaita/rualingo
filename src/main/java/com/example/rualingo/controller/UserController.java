package com.example.rualingo.controller;

import com.example.rualingo.DTO.ActivityLogDTO;
import com.example.rualingo.DTO.CourseDTO;
import com.example.rualingo.DTO.LanguageDTO;
import com.example.rualingo.DTO.UserDTO;
import com.example.rualingo.DTO.UserResponseDTO;
import com.example.rualingo.service.UserService;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        UserDTO createdUser = userService.createUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDTO> getAllUsers(@RequestParam(required = false) String roleName) {
        if (roleName != null) {
            return userService.getUsersByRoleName(roleName);
        }
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public UserDTO getUserById(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public UserDTO updateUser(@PathVariable Long userId, @RequestBody UserDTO userDTO) {
        System.out.println("Updating user: " + userId);
        return userService.updateUser(userId, userDTO);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }

    @GetMapping("/search/by-email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getUserByEmail(@RequestParam String email) {
        Optional<UserDTO> user = userService.getUserByEmail(email);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/search/by-username")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getUserByUsername(@RequestParam String username) {
        Optional<UserDTO> user = userService.getUserByUsername(username);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{userId}/password/change")
    @PreAuthorize("#userId == authentication.principal.id")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@PathVariable Long userId, @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userId, request.oldPassword(), request.newPassword());
    }

    @PostMapping("/{userId}/password/reset")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@PathVariable Long userId, @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(userId, request.newPassword());
    }

    @PatchMapping("/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activateUser(@PathVariable Long userId) {
        userService.activateUser(userId);
    }

    @PatchMapping("/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateUser(@PathVariable Long userId) {
        userService.deactivateUser(userId);
    }

    @PatchMapping("/{userId}/role/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDTO assignRole(@PathVariable Long userId, @PathVariable Long roleId) {
        return userService.assignRole(userId, roleId);
    }

    @DeleteMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDTO revokeRole(@PathVariable Long userId) {
        return userService.revokeRole(userId);
    }

    @GetMapping("/{userId}/is_active")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public Map<String, Boolean> getIs_active(@PathVariable Long userId) {
        return Map.of("is_active", userService.getIs_active(userId));
    }

    @PatchMapping("/{userId}/username")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public UserDTO updateUsername(@PathVariable Long userId, @RequestBody UpdateUsernameRequest request) {
        return userService.updateUsername(userId, request.username());
    }

    @PatchMapping("/{userId}/email")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public UserDTO updateEmail(@PathVariable Long userId, @RequestBody UpdateEmailRequest request) {
        return userService.updateEmail(userId, request.email());
    }

    @PatchMapping("/{userId}/first_name")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public UserDTO updateFirst_name(@PathVariable Long userId, @RequestBody UpdateFirst_nameRequest request) {
        return userService.updateFirst_name(userId, request.first_name());
    }

    @PatchMapping("/{userId}/second_name")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public UserDTO updateSecond_name(@PathVariable Long userId, @RequestBody UpdateSecond_nameRequest request) {
        return userService.updateSecond_name(userId, request.second_name());
    }

    @GetMapping("/{userId}/profile_picture")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public Map<String, String> getProfile_picture(@PathVariable Long userId) {
        return java.util.Collections.singletonMap("profile_picture", userService.getProfile_picture(userId));
    }

    @PatchMapping("/{userId}/profile_picture")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public UserDTO updateProfile_picture(
            @PathVariable Long userId,
            @RequestBody UpdateProfile_pictureRequest request) {
        return userService.updateProfile_picture(userId, request.profile_picture());
    }

    @DeleteMapping("/{userId}/profile_picture")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public UserDTO deleteProfile_picture(@PathVariable Long userId) {
        return userService.deleteProfile_picture(userId);
    }

    @PostMapping("/{userId}/languages/{languageId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public UserDTO addLanguageToUser(@PathVariable Long userId, @PathVariable Long languageId) {
        return userService.addLanguageToUser(userId, languageId);
    }

    @DeleteMapping("/{userId}/languages/{languageId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public UserDTO removeLanguageFromUser(@PathVariable Long userId, @PathVariable Long languageId) {
        return userService.removeLanguageFromUser(userId, languageId);
    }

    @GetMapping("/{userId}/languages")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public Set<LanguageDTO> getUserLanguages(@PathVariable Long userId) {
        return userService.getUserLanguages(userId);
    }

    @PostMapping("/{userId}/courses/{courseId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public UserDTO enrollUserInCourse(@PathVariable Long userId, @PathVariable Long courseId) {
        return userService.enrollUserInCourse(userId, courseId);
    }

    @DeleteMapping("/{userId}/courses/{courseId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public UserDTO unenrollUserFromCourse(@PathVariable Long userId, @PathVariable Long courseId) {
        return userService.unenrollUserFromCourse(userId, courseId);
    }

    @GetMapping("/{userId}/courses")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public Set<CourseDTO> getUserCourses(@PathVariable Long userId) {
        return userService.getUserCourses(userId);
    }

    @GetMapping("/{userId}/activity-logs")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public List<ActivityLogDTO> getUserActivityLogs(@PathVariable Long userId) {
        return userService.getUserActivityLogs(userId);
    }

    @PostMapping("/{userId}/activity-logs")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    @ResponseStatus(HttpStatus.CREATED)
    public void logUserActivity(@PathVariable Long userId, @RequestBody LogActivityRequest request) {
        userService.logUserActivity(userId, request.action(), request.lessonId(), request.exerciseId());
    }

    @GetMapping("/{userId}/responses")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public List<UserResponseDTO> getUserResponses(@PathVariable Long userId) {
        return userService.getUserResponses(userId);
    }

    @PostMapping("/{userId}/responses")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveUserResponse(@PathVariable Long userId, @RequestBody SaveUserResponseRequest request) {
        userService.saveUserResponse(
                userId,
                request.exerciseId(),
                request.answer(),
                request.isCorrect(),
                request.attempts());
    }

    @GetMapping("/{userId}/responses/correct-count")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public Map<String, Long> countCorrectResponses(@PathVariable Long userId) {
        return Map.of("correctResponses", userService.countCorrectResponses(userId));
    }

    @GetMapping("/{userId}/responses/accuracy")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public Map<String, Double> getAccuracy(@PathVariable Long userId) {
        return Map.of("accuracy", userService.getAccuracy(userId));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    public record ChangePasswordRequest(String oldPassword, String newPassword) {}

    public record ResetPasswordRequest(String newPassword) {}

    public record UpdateUsernameRequest(String username) {}

    public record UpdateEmailRequest(String email) {}

    public record UpdateFirst_nameRequest(String first_name) {}

    public record UpdateSecond_nameRequest(String second_name) {}

    public record UpdateProfile_pictureRequest(String profile_picture) {}

    public record LogActivityRequest(String action, Long lessonId, Long exerciseId) {}

    public record SaveUserResponseRequest(Long exerciseId, String answer, boolean isCorrect, int attempts) {}
}
