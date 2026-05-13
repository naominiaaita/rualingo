package com.example.rualingo.controller;

import com.example.rualingo.DTO.AccountPhotoDTO;
import com.example.rualingo.DTO.AccountProfileDTO;
import com.example.rualingo.DTO.AccountSettingsDTO;
import com.example.rualingo.DTO.CompletedLessonDTO;
import com.example.rualingo.DTO.CourseDTO;
import com.example.rualingo.DTO.CropAccountPhotoRequestDTO;
import com.example.rualingo.DTO.ExerciseSubmissionResultDTO;
import com.example.rualingo.DTO.LanguageDTO;
import com.example.rualingo.DTO.LessonDTO;
import com.example.rualingo.DTO.LessonReviewDTO;
import com.example.rualingo.DTO.NotificationDTO;
import com.example.rualingo.DTO.ProgressStatsDTO;
import com.example.rualingo.DTO.StudentExerciseDTO;
import com.example.rualingo.DTO.UpdateAccountProfileRequestDTO;
import com.example.rualingo.DTO.UpdateAccountPhotoRequestDTO;
import com.example.rualingo.DTO.UpdateAccountSettingsRequestDTO;
import com.example.rualingo.model.User;
import com.example.rualingo.service.AccountService;
import java.util.NoSuchElementException;
import java.util.List;
import java.util.Set;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/me")
    public AccountProfileDTO getMyProfile(Authentication authentication) {
        return accountService.getProfile(requireAuthenticatedUser(authentication));
    }

    @GetMapping("/settings")
    public AccountSettingsDTO getMySettings(Authentication authentication) {
        return accountService.getSettings(requireAuthenticatedUser(authentication));
    }

    @GetMapping("/photo")
    public AccountPhotoDTO getMyPhoto(Authentication authentication) {
        return accountService.getPhoto(requireAuthenticatedUser(authentication));
    }

    @GetMapping("/languages")
    public Set<LanguageDTO> getMyLanguages(Authentication authentication) {
        return accountService.getLanguages(requireAuthenticatedUser(authentication));
    }

    @GetMapping("/courses")
    public Set<CourseDTO> getMyCourses(Authentication authentication) {
        return accountService.getCourses(requireAuthenticatedUser(authentication));
    }

    @GetMapping("/lessons/completed")
    public List<CompletedLessonDTO> getMyCompletedLessons(Authentication authentication) {
        return accountService.getCompletedLessons(requireAuthenticatedUser(authentication));
    }

    @GetMapping("/progress")
    public ProgressStatsDTO getMyProgress(Authentication authentication) {
        return accountService.getProgressStats(requireAuthenticatedUser(authentication));
    }

    @GetMapping("/notifications")
    public List<NotificationDTO> getMyNotifications(Authentication authentication) {
        return accountService.getNotifications(requireAuthenticatedUser(authentication));
    }

    @PatchMapping("/notifications/{notificationId}/read")
    public NotificationDTO markMyNotificationRead(
            Authentication authentication,
            @PathVariable Long notificationId) {
        return accountService.markNotificationRead(requireAuthenticatedUser(authentication), notificationId);
    }

    @PatchMapping("/notifications/read-all")
    public Map<String, Integer> markAllMyNotificationsRead(Authentication authentication) {
        int updated = accountService.markAllNotificationsRead(requireAuthenticatedUser(authentication));
        return Map.of("updatedCount", updated);
    }

    @GetMapping("/courses/{courseId}/lessons")
    public Set<LessonDTO> getMyCourseLessons(Authentication authentication, @org.springframework.web.bind.annotation.PathVariable Long courseId) {
        return accountService.getCourseLessons(requireAuthenticatedUser(authentication), courseId);
    }

    @GetMapping("/lessons/{lessonId}/exercises")
    public Set<StudentExerciseDTO> getMyLessonExercises(Authentication authentication, @org.springframework.web.bind.annotation.PathVariable Long lessonId) {
        return accountService.getLessonExercises(requireAuthenticatedUser(authentication), lessonId);
    }

    @GetMapping("/lessons/{lessonId}/review")
    public LessonReviewDTO reviewMyCompletedLesson(Authentication authentication, @org.springframework.web.bind.annotation.PathVariable Long lessonId) {
        return accountService.reviewCompletedLesson(requireAuthenticatedUser(authentication), lessonId);
    }

    @PostMapping("/lessons/{lessonId}/revisit")
    public LessonReviewDTO revisitMyCompletedLesson(Authentication authentication, @org.springframework.web.bind.annotation.PathVariable Long lessonId) {
        return accountService.revisitLesson(requireAuthenticatedUser(authentication), lessonId);
    }

    @PatchMapping("/profile")
    public AccountProfileDTO updateMyProfile(
            Authentication authentication,
            @RequestBody UpdateAccountProfileRequestDTO request) {
        return accountService.updateProfile(requireAuthenticatedUser(authentication), request);
    }

    @PatchMapping("/settings")
    public AccountSettingsDTO updateMySettings(
            Authentication authentication,
            @RequestBody UpdateAccountSettingsRequestDTO request) {
        return accountService.updateSettings(requireAuthenticatedUser(authentication), request);
    }

    @PostMapping("/photo")
    public AccountPhotoDTO addMyPhoto(
            Authentication authentication,
            @RequestBody UpdateAccountPhotoRequestDTO request) {
        return accountService.addOrReplacePhoto(requireAuthenticatedUser(authentication), request);
    }

    @PatchMapping("/photo")
    public AccountPhotoDTO replaceMyPhoto(
            Authentication authentication,
            @RequestBody UpdateAccountPhotoRequestDTO request) {
        return accountService.addOrReplacePhoto(requireAuthenticatedUser(authentication), request);
    }

    @PatchMapping("/photo/crop")
    public AccountPhotoDTO cropMyPhoto(
            Authentication authentication,
            @RequestBody CropAccountPhotoRequestDTO request) {
        return accountService.cropPhoto(requireAuthenticatedUser(authentication), request);
    }

    @PostMapping("/photo/remove")
    public AccountPhotoDTO removeMyPhoto(Authentication authentication) {
        return accountService.removePhoto(requireAuthenticatedUser(authentication));
    }

    @PostMapping("/languages/{languageId}")
    public Set<LanguageDTO> addMyLanguage(Authentication authentication, @org.springframework.web.bind.annotation.PathVariable Long languageId) {
        return accountService.addLanguage(requireAuthenticatedUser(authentication), languageId);
    }

    @DeleteMapping("/languages/{languageId}")
    public Set<LanguageDTO> removeMyLanguage(Authentication authentication, @org.springframework.web.bind.annotation.PathVariable Long languageId) {
        return accountService.removeLanguage(requireAuthenticatedUser(authentication), languageId);
    }

    @PostMapping("/courses/{courseId}")
    public Set<CourseDTO> addMyCourse(Authentication authentication, @org.springframework.web.bind.annotation.PathVariable Long courseId) {
        return accountService.addCourse(requireAuthenticatedUser(authentication), courseId);
    }

    @DeleteMapping("/courses/{courseId}")
    public Set<CourseDTO> removeMyCourse(Authentication authentication, @org.springframework.web.bind.annotation.PathVariable Long courseId) {
        return accountService.removeCourse(requireAuthenticatedUser(authentication), courseId);
    }

    @PostMapping("/exercises/{exerciseId}/submit")
    public ExerciseSubmissionResultDTO submitMyExerciseAnswer(
            Authentication authentication,
            @org.springframework.web.bind.annotation.PathVariable Long exerciseId,
            @RequestBody SubmitExerciseRequest request) {
        return accountService.submitExerciseAnswer(
                requireAuthenticatedUser(authentication),
                exerciseId,
                request.answer(),
                request.attempts());
    }

    @PostMapping("/password/change")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeMyPassword(Authentication authentication, @RequestBody ChangeMyPasswordRequest request) {
        accountService.changePassword(
                requireAuthenticatedUser(authentication),
                request.currentPassword(),
                request.newPassword());
    }

    @PostMapping("/deactivate")
    public AccountSettingsDTO deactivateMyAccount(
            Authentication authentication,
            @RequestBody DeactivateAccountRequest request) {
        return accountService.deactivateAccount(requireAuthenticatedUser(authentication), request.currentPassword());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    private User requireAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new IllegalArgumentException("Authenticated user is required.");
        }
        return user;
    }

    public record ChangeMyPasswordRequest(String currentPassword, String newPassword) {}

    public record DeactivateAccountRequest(String currentPassword) {}

    public record SubmitExerciseRequest(String answer, Integer attempts) {}
}
