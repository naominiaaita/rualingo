package com.example.rualingo.service;

import com.example.rualingo.DTO.AccountPhotoDTO;
import com.example.rualingo.DTO.AccountProfileDTO;
import com.example.rualingo.DTO.AccountSettingsDTO;
import com.example.rualingo.DTO.CompletedLessonDTO;
import com.example.rualingo.DTO.CropAccountPhotoRequestDTO;
import com.example.rualingo.DTO.CourseDTO;
import com.example.rualingo.DTO.ExerciseSubmissionResultDTO;
import com.example.rualingo.DTO.LanguageDTO;
import com.example.rualingo.DTO.LessonDTO;
import com.example.rualingo.DTO.LessonReviewDTO;
import com.example.rualingo.DTO.LessonReviewExerciseDTO;
import com.example.rualingo.DTO.NotificationDTO;
import com.example.rualingo.DTO.ProgressStatsDTO;
import com.example.rualingo.DTO.StudentExerciseDTO;
import com.example.rualingo.DTO.UpdateAccountProfileRequestDTO;
import com.example.rualingo.DTO.UpdateAccountPhotoRequestDTO;
import com.example.rualingo.DTO.UpdateAccountSettingsRequestDTO;
import com.example.rualingo.model.Course;
import com.example.rualingo.model.ActivityLog;
import com.example.rualingo.model.Exercise;
import com.example.rualingo.model.Language;
import com.example.rualingo.model.Lesson;
import com.example.rualingo.model.Notification;
import com.example.rualingo.model.User;
import com.example.rualingo.model.UserResponse;
import com.example.rualingo.repository.ActivityLogRepository;
import com.example.rualingo.repository.CourseRepository;
import com.example.rualingo.repository.ExerciseRepository;
import com.example.rualingo.repository.LanguageRepository;
import com.example.rualingo.repository.LessonRepository;
import com.example.rualingo.repository.NotificationRepository;
import com.example.rualingo.repository.UserRepository;
import com.example.rualingo.repository.UserResponseRepository;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Comparator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccountService {
    private static final int XP_PER_COMPLETED_LESSON = 50;
    private static final String LESSON_COMPLETED_ACTION = "LESSON_COMPLETED";
    private static final String NOTIFICATION_TYPE_CONSISTENCY_REMINDER = "CONSISTENCY_REMINDER";
    private static final String NOTIFICATION_TYPE_LESSON_COMPLETED = "LESSON_COMPLETED";

    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;
    private final LanguageRepository languageRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserResponseRepository userResponseRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(
            UserRepository userRepository,
            ActivityLogRepository activityLogRepository,
            LanguageRepository languageRepository,
            CourseRepository courseRepository,
            LessonRepository lessonRepository,
            ExerciseRepository exerciseRepository,
            UserResponseRepository userResponseRepository,
            NotificationRepository notificationRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.activityLogRepository = activityLogRepository;
        this.languageRepository = languageRepository;
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.exerciseRepository = exerciseRepository;
        this.userResponseRepository = userResponseRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public AccountProfileDTO getProfile(User authenticatedUser) {
        User user = requireManagedUser(authenticatedUser);
        return toProfileDTO(user);
    }

    @Transactional(readOnly = true)
    public AccountSettingsDTO getSettings(User authenticatedUser) {
        User user = requireManagedUser(authenticatedUser);
        return toSettingsDTO(user);
    }

    @Transactional(readOnly = true)
    public AccountPhotoDTO getPhoto(User authenticatedUser) {
        User user = requireManagedUser(authenticatedUser);
        return toPhotoDTO(user);
    }

    @Transactional(readOnly = true)
    public Set<LanguageDTO> getLanguages(User authenticatedUser) {
        User user = requireManagedUser(authenticatedUser);
        return user.getLanguages().stream()
                .map(language -> new LanguageDTO(
                        language.getId(),
                        language.getName(),
                        language.getProvince(),
                        language.getDistrict(),
                        language.getClan(),
                        language.getFlag(),
                        language.getUser() != null ? language.getUser().getId() : null))
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public Set<CourseDTO> getCourses(User authenticatedUser) {
        User user = requireManagedUser(authenticatedUser);
        return user.getCourses().stream()
                .map(course -> new CourseDTO(
                        course.getId(),
                        course.getTitle(),
                        course.getName(),
                        course.getDescription(),
                        course.getLanguage() != null ? course.getLanguage().getId() : null))
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public Set<LessonDTO> getCourseLessons(User authenticatedUser, Long courseId) {
        User user = requireManagedUser(authenticatedUser);
        Course course = requireSelectedCourse(user, courseId);
        return course.getLessons().stream()
                .map(lesson -> new LessonDTO(
                        lesson.getId(),
                        lesson.getTitle(),
                        lesson.getDescription(),
                        lesson.getContent(),
                        lesson.getCourse() != null ? lesson.getCourse().getId() : null,
                        lesson.getSubmissionStatus(),
                        lesson.getModerationNote(),
                        lesson.getReviewedAt() != null ? lesson.getReviewedAt().toString() : null,
                        lesson.getTopic()))
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public Set<StudentExerciseDTO> getLessonExercises(User authenticatedUser, Long lessonId) {
        User user = requireManagedUser(authenticatedUser);
        Lesson lesson = requireAccessibleLesson(user, lessonId);
        return exerciseRepository.findByLessonId(lesson.getId()).stream()
                .map(exercise -> new StudentExerciseDTO(
                        exercise.getId(),
                        exercise.getType(),
                        exercise.getQuestionText(),
                        exercise.getQuestion(),
                        exercise.getOptions(),
                        exercise.getHint(),
                        exercise.getLesson() != null ? exercise.getLesson().getId() : null))
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public List<CompletedLessonDTO> getCompletedLessons(User authenticatedUser) {
        User user = requireManagedUser(authenticatedUser);
        return user.getCourses().stream()
                .flatMap(course -> course.getLessons().stream())
                .filter(lesson -> isLessonCompleted(user, lesson))
                .map(lesson -> toCompletedLessonDTO(user, lesson))
                .sorted(Comparator.comparing(
                        CompletedLessonDTO::getCompletedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotifications(User authenticatedUser) {
        User user = requireManagedUser(authenticatedUser);
        generateConsistencyReminderIfNeeded(user);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toNotificationDTO)
                .collect(Collectors.toList());
    }

    public NotificationDTO markNotificationRead(User authenticatedUser, Long notificationId) {
        User user = requireManagedUser(authenticatedUser);
        Long requiredNotificationId = Objects.requireNonNull(notificationId, "notificationId must not be null");
        Notification notification = notificationRepository.findByIdAndUserId(requiredNotificationId, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Notification not found: " + notificationId));
        notification.setRead(true);
        return toNotificationDTO(notificationRepository.save(notification));
    }

    public int markAllNotificationsRead(User authenticatedUser) {
        User user = requireManagedUser(authenticatedUser);
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        int updatedCount = 0;
        for (Notification notification : notifications) {
            if (notification.isRead()) {
                continue;
            }
            notification.setRead(true);
            updatedCount++;
        }
        if (updatedCount > 0) {
            notificationRepository.saveAll(notifications);
        }
        return updatedCount;
    }

    @Transactional(readOnly = true)
    public ProgressStatsDTO getProgressStats(User authenticatedUser) {
        User user = requireManagedUser(authenticatedUser);
        List<ActivityLog> completionLogs = activityLogRepository
                .findByUserIdAndActionOrderByTimestampDesc(user.getId(), LESSON_COMPLETED_ACTION);

        if (completionLogs.isEmpty()) {
            return new ProgressStatsDTO(0, 0, 0, 0, null);
        }

        List<LocalDate> completionDates = completionLogs.stream()
                .map(ActivityLog::getTimestamp)
                .filter(Objects::nonNull)
                .map(java.time.LocalDateTime::toLocalDate)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        int currentStreakDays = calculateCurrentStreak(completionDates);
        int longestStreakDays = calculateLongestStreak(completionDates);
        int completedLessons = (int) completionLogs.stream()
                .map(ActivityLog::getLesson)
                .filter(Objects::nonNull)
                .map(Lesson::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new))
                .size();
        int totalXp = completedLessons * XP_PER_COMPLETED_LESSON;
        String lastCompletedAt = completionLogs.get(0).getTimestamp() != null
                ? completionLogs.get(0).getTimestamp().toString()
                : null;

        return new ProgressStatsDTO(
                currentStreakDays,
                longestStreakDays,
                totalXp,
                completedLessons,
                lastCompletedAt);
    }

    @Transactional(readOnly = true)
    public LessonReviewDTO reviewCompletedLesson(User authenticatedUser, Long lessonId) {
        User user = requireManagedUser(authenticatedUser);
        Lesson lesson = requireAccessibleLesson(user, lessonId);
        if (!isLessonCompleted(user, lesson)) {
            throw new IllegalArgumentException("This lesson is not completed yet.");
        }

        List<UserResponse> responses = userResponseRepository.findByUserIdAndLessonIdOrderByTimeStampDesc(user.getId(), lesson.getId());
        List<LessonReviewExerciseDTO> exercises = exerciseRepository.findByLessonId(lesson.getId()).stream()
                .map(exercise -> toLessonReviewExerciseDTO(exercise, findLatestResponse(responses, exercise.getId()).orElse(null)))
                .collect(Collectors.toList());

        return new LessonReviewDTO(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getDescription(),
                lesson.getContent(),
                lesson.getCourse() != null ? lesson.getCourse().getId() : null,
                true,
                getCompletedAt(user, lesson),
                exercises);
    }

    public LessonReviewDTO revisitLesson(User authenticatedUser, Long lessonId) {
        User user = requireManagedUser(authenticatedUser);
        Lesson lesson = requireAccessibleLesson(user, lessonId);
        if (!isLessonCompleted(user, lesson)) {
            throw new IllegalArgumentException("Complete the lesson before revisiting it.");
        }

        ActivityLog activityLog = new ActivityLog();
        activityLog.setAction("LESSON_REVISITED");
        activityLog.setTimestamp(java.time.LocalDateTime.now());
        activityLog.setUser(user);
        activityLog.setLesson(lesson);
        activityLogRepository.save(activityLog);

        return reviewCompletedLesson(user, lessonId);
    }

    public AccountProfileDTO updateProfile(User authenticatedUser, UpdateAccountProfileRequestDTO request) {
        User user = requireManagedUser(authenticatedUser);
        if (request == null) {
            throw new IllegalArgumentException("Profile update request must not be null.");
        }

        if (request.getUsername() != null && !request.getUsername().isBlank()
                && !request.getUsername().equals(user.getUsername())) {
            validateUniqueUsername(request.getUsername());
            user.setUsername(request.getUsername().trim());
        }

        if (request.getFirst_name() != null) {
            user.setFirstName(request.getFirst_name().trim());
        }

        if (request.getSecond_name() != null) {
            user.setSecondName(request.getSecond_name().trim());
        }

        if (request.getGender() != null) {
            user.setGender(request.getGender().trim());
        }

        if (request.getProfile_picture() != null) {
            user.setProfilePicture(request.getProfile_picture().trim());
        }

        return toProfileDTO(userRepository.save(user));
    }

    public AccountSettingsDTO updateSettings(User authenticatedUser, UpdateAccountSettingsRequestDTO request) {
        User user = requireManagedUser(authenticatedUser);
        if (request == null) {
            throw new IllegalArgumentException("Settings update request must not be null.");
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            String normalizedEmail = request.getEmail().trim().toLowerCase();
            validateUniqueEmail(normalizedEmail);
            user.setEmail(normalizedEmail);
        }

        return toSettingsDTO(userRepository.save(user));
    }

    public AccountPhotoDTO addOrReplacePhoto(User authenticatedUser, UpdateAccountPhotoRequestDTO request) {
        User user = requireManagedUser(authenticatedUser);
        if (request == null || request.getProfile_picture() == null || request.getProfile_picture().isBlank()) {
            throw new IllegalArgumentException("Profile picture is required.");
        }

        user.setProfilePicture(request.getProfile_picture().trim());
        clearPhotoCrop(user);
        return toPhotoDTO(userRepository.save(user));
    }

    public AccountPhotoDTO cropPhoto(User authenticatedUser, CropAccountPhotoRequestDTO request) {
        User user = requireManagedUser(authenticatedUser);
        if (user.getProfilePicture() == null || user.getProfilePicture().isBlank()) {
            throw new IllegalArgumentException("Add a profile picture before cropping it.");
        }
        if (request == null) {
            throw new IllegalArgumentException("Crop request must not be null.");
        }
        validateCropValue(request.getCropX(), "cropX");
        validateCropValue(request.getCropY(), "cropY");
        validatePositiveCropValue(request.getCropWidth(), "cropWidth");
        validatePositiveCropValue(request.getCropHeight(), "cropHeight");

        user.setProfilePictureCropX(request.getCropX());
        user.setProfilePictureCropY(request.getCropY());
        user.setProfilePictureCropWidth(request.getCropWidth());
        user.setProfilePictureCropHeight(request.getCropHeight());
        return toPhotoDTO(userRepository.save(user));
    }

    public AccountPhotoDTO removePhoto(User authenticatedUser) {
        User user = requireManagedUser(authenticatedUser);
        user.setProfilePicture(null);
        clearPhotoCrop(user);
        return toPhotoDTO(userRepository.save(user));
    }

    public Set<LanguageDTO> addLanguage(User authenticatedUser, Long languageId) {
        User user = requireManagedUser(authenticatedUser);
        Long requiredLanguageId = Objects.requireNonNull(languageId, "languageId must not be null");
        Language language = languageRepository.findById(requiredLanguageId)
                .orElseThrow(() -> new NoSuchElementException("Language not found: " + languageId));
        user.getLanguages().add(language);
        return getLanguages(userRepository.save(user));
    }

    public Set<LanguageDTO> removeLanguage(User authenticatedUser, Long languageId) {
        User user = requireManagedUser(authenticatedUser);
        user.getLanguages().removeIf(language -> language.getId().equals(languageId));
        return getLanguages(userRepository.save(user));
    }

    public Set<CourseDTO> addCourse(User authenticatedUser, Long courseId) {
        User user = requireManagedUser(authenticatedUser);
        Long requiredCourseId = Objects.requireNonNull(courseId, "courseId must not be null");
        Course course = courseRepository.findById(requiredCourseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found: " + courseId));
        user.getCourses().add(course);
        return getCourses(userRepository.save(user));
    }

    public Set<CourseDTO> removeCourse(User authenticatedUser, Long courseId) {
        User user = requireManagedUser(authenticatedUser);
        user.getCourses().removeIf(course -> course.getId().equals(courseId));
        return getCourses(userRepository.save(user));
    }

    public ExerciseSubmissionResultDTO submitExerciseAnswer(
            User authenticatedUser,
            Long exerciseId,
            String answer,
            Integer attempts) {
        User user = requireManagedUser(authenticatedUser);
        Exercise exercise = requireAccessibleExercise(user, exerciseId);

        if (answer == null || answer.isBlank()) {
            throw new IllegalArgumentException("Answer is required.");
        }

        int resolvedAttempts = attempts != null && attempts > 0 ? attempts : 1;
        boolean correct = exercise.getCorrectAnswer() != null
                && exercise.getCorrectAnswer().trim().equalsIgnoreCase(answer.trim());
        String feedbackMessage = buildFeedbackMessage(correct, resolvedAttempts);
        String suggestedHint = correct ? null : buildSuggestedHint(exercise);

        UserResponse response = new UserResponse();
        response.setUser(user);
        response.setExercise(exercise);
        response.setAnswer(answer);
        response.setAttempts(resolvedAttempts);
        response.setResponseTime(java.time.LocalDateTime.now());
        response.setTimeStamp(java.time.LocalDateTime.now());
        response.setIsCorrect(correct);
        userResponseRepository.save(response);
        markLessonCompletedIfReady(user, exercise.getLesson());

        long totalCorrectResponses = userResponseRepository.countCorrectByUserId(user.getId());
        long totalResponses = userResponseRepository.countByUserId(user.getId());
        double accuracy = totalResponses == 0 ? 0.0 : (totalCorrectResponses * 100.0) / totalResponses;

        return new ExerciseSubmissionResultDTO(
                exercise.getId(),
                exercise.getLesson() != null ? exercise.getLesson().getId() : null,
                answer,
                correct,
                feedbackMessage,
                suggestedHint,
                resolvedAttempts,
                totalCorrectResponses,
                accuracy);
    }

    public void changePassword(User authenticatedUser, String currentPassword, String newPassword) {
        User user = requireManagedUser(authenticatedUser);

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("This account does not have a local password yet.");
        }
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new IllegalArgumentException("Current password is required.");
        }
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password is required.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public AccountSettingsDTO deactivateAccount(User authenticatedUser, String currentPassword) {
        User user = requireManagedUser(authenticatedUser);

        if ("LOCAL".equalsIgnoreCase(user.getAuthProvider())) {
            if (currentPassword == null || currentPassword.isBlank()) {
                throw new IllegalArgumentException("Current password is required to deactivate this account.");
            }
            if (user.getPassword() == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new IllegalArgumentException("Current password is incorrect.");
            }
        }

        user.setActive(false);
        return toSettingsDTO(userRepository.save(user));
    }

    private User requireManagedUser(User authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.getId() == null) {
            throw new IllegalArgumentException("Authenticated user is required.");
        }

        return userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user no longer exists."));
    }

    private void validateUniqueEmail(String email) {
        if (userRepository.existsByEmail(Objects.requireNonNull(email, "email must not be null"))) {
            throw new IllegalArgumentException("Email is already in use.");
        }
    }

    private void validateUniqueUsername(String username) {
        if (userRepository.existsByUsername(Objects.requireNonNull(username, "username must not be null"))) {
            throw new IllegalArgumentException("Username is already in use.");
        }
    }

    private void validateCropValue(Integer value, String fieldName) {
        if (value == null || value < 0) {
            throw new IllegalArgumentException(fieldName + " must be 0 or greater.");
        }
    }

    private void validatePositiveCropValue(Integer value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0.");
        }
    }

    private void clearPhotoCrop(User user) {
        user.setProfilePictureCropX(null);
        user.setProfilePictureCropY(null);
        user.setProfilePictureCropWidth(null);
        user.setProfilePictureCropHeight(null);
    }

    private String buildFeedbackMessage(boolean correct, int attempts) {
        if (correct) {
            return attempts > 1 ? "Correct. You got it after " + attempts + " attempts." : "Correct. Great job.";
        }
        return attempts > 1
                ? "Not quite yet. Review the hint and try again."
                : "Not quite right. Try once more.";
    }

    private int calculateCurrentStreak(List<LocalDate> completionDatesDesc) {
        if (completionDatesDesc.isEmpty()) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        LocalDate cursor = completionDatesDesc.get(0);
        if (!cursor.equals(today) && !cursor.equals(today.minusDays(1))) {
            return 0;
        }

        int streak = 1;
        for (int i = 1; i < completionDatesDesc.size(); i++) {
            LocalDate nextDate = completionDatesDesc.get(i);
            if (nextDate.equals(cursor.minusDays(1))) {
                streak++;
                cursor = nextDate;
                continue;
            }
            break;
        }
        return streak;
    }

    private int calculateLongestStreak(List<LocalDate> completionDatesDesc) {
        if (completionDatesDesc.isEmpty()) {
            return 0;
        }
        int longest = 1;
        int current = 1;
        LocalDate cursor = completionDatesDesc.get(0);
        for (int i = 1; i < completionDatesDesc.size(); i++) {
            LocalDate nextDate = completionDatesDesc.get(i);
            if (nextDate.equals(cursor.minusDays(1))) {
                current++;
                longest = Math.max(longest, current);
            } else {
                current = 1;
            }
            cursor = nextDate;
        }
        return longest;
    }

    private String buildSuggestedHint(Exercise exercise) {
        if (exercise.getHint() != null && !exercise.getHint().isBlank()) {
            return exercise.getHint();
        }
        return "Read the question carefully and check the key terms before submitting again.";
    }

    private void markLessonCompletedIfReady(User user, Lesson lesson) {
        if (lesson == null || !isLessonCompleted(user, lesson)) {
            return;
        }
        boolean alreadyLogged = activityLogRepository
                .findTopByUserIdAndLessonIdAndActionOrderByTimestampDesc(user.getId(), lesson.getId(), LESSON_COMPLETED_ACTION)
                .isPresent();
        if (alreadyLogged) {
            return;
        }

        ActivityLog activityLog = new ActivityLog();
        activityLog.setAction(LESSON_COMPLETED_ACTION);
        activityLog.setTimestamp(java.time.LocalDateTime.now());
        activityLog.setUser(user);
        activityLog.setLesson(lesson);
        activityLogRepository.save(activityLog);
        createLessonCompletedNotification(user, lesson);
    }

    private void generateConsistencyReminderIfNeeded(User user) {
        // Check if a consistency reminder notification already exists for today
        LocalDate today = LocalDate.now();
        boolean hasReminderToday = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .filter(notification -> NOTIFICATION_TYPE_CONSISTENCY_REMINDER.equals(notification.getType()))
                .anyMatch(notification -> notification.getCreatedAt() != null 
                        && notification.getCreatedAt().toLocalDate().equals(today));
        
        if (!hasReminderToday) {
            // Check if user has completed any lesson recently
            List<ActivityLog> recentLessons = activityLogRepository
                    .findByUserIdAndActionOrderByTimestampDesc(user.getId(), LESSON_COMPLETED_ACTION);
            
            if (recentLessons.isEmpty()) {
                // Create consistency reminder notification
                Notification notification = new Notification();
                notification.setUser(user);
                notification.setType(NOTIFICATION_TYPE_CONSISTENCY_REMINDER);
                notification.setMessage("Keep up your learning streak! Complete a lesson today.");
                notification.setCreatedAt(LocalDateTime.now());
                notification.setRead(false);
                notificationRepository.save(notification);
            }
        }
    }

    private void createLessonCompletedNotification(User user, Lesson lesson) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(NOTIFICATION_TYPE_LESSON_COMPLETED);
        notification.setMessage("You completed the lesson: " + lesson.getTitle());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        notificationRepository.save(notification);
    }

    private boolean isLessonCompleted(User user, Lesson lesson) {
        List<Exercise> exercises = exerciseRepository.findByLessonId(lesson.getId());
        if (exercises.isEmpty()) {
            return false;
        }
        long correctCount = userResponseRepository.countDistinctCorrectExercisesByUserIdAndLessonId(user.getId(), lesson.getId());
        return correctCount >= exercises.size();
    }

    private String getCompletedAt(User user, Lesson lesson) {
        return activityLogRepository.findTopByUserIdAndLessonIdAndActionOrderByTimestampDesc(
                        user.getId(),
                        lesson.getId(),
                        LESSON_COMPLETED_ACTION)
                .map(ActivityLog::getTimestamp)
                .map(java.time.LocalDateTime::toString)
                .orElse(null);
    }

    private CompletedLessonDTO toCompletedLessonDTO(User user, Lesson lesson) {
        List<Exercise> exercises = exerciseRepository.findByLessonId(lesson.getId());
        long correctCount = userResponseRepository.countDistinctCorrectExercisesByUserIdAndLessonId(user.getId(), lesson.getId());
        return new CompletedLessonDTO(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getCourse() != null ? lesson.getCourse().getId() : null,
                getCompletedAt(user, lesson),
                exercises.size(),
                correctCount);
    }

    private Optional<UserResponse> findLatestResponse(List<UserResponse> responses, Long exerciseId) {
        return responses.stream()
                .filter(response -> response.getExercise() != null && response.getExercise().getId().equals(exerciseId))
                .findFirst();
    }

    private LessonReviewExerciseDTO toLessonReviewExerciseDTO(Exercise exercise, UserResponse response) {
        return new LessonReviewExerciseDTO(
                exercise.getId(),
                exercise.getType(),
                exercise.getQuestionText(),
                exercise.getQuestion(),
                exercise.getOptions(),
                exercise.getHint(),
                response != null ? response.getAnswer() : null,
                response != null ? response.getIsCorrect() : null,
                response != null ? response.getAttempts() : null,
                response != null && response.getResponseTime() != null ? response.getResponseTime().toString() : null);
    }

    private Course requireSelectedCourse(User user, Long courseId) {
        Long requiredCourseId = Objects.requireNonNull(courseId, "courseId must not be null");
        return user.getCourses().stream()
                .filter(course -> course.getId().equals(requiredCourseId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Course not found in the student's selections: " + courseId));
    }

    private Lesson requireAccessibleLesson(User user, Long lessonId) {
        Long requiredLessonId = Objects.requireNonNull(lessonId, "lessonId must not be null");
        Lesson lesson = lessonRepository.findById(requiredLessonId)
                .orElseThrow(() -> new NoSuchElementException("Lesson not found: " + lessonId));
        if (lesson.getCourse() == null) {
            throw new IllegalArgumentException("Lesson is not assigned to a course.");
        }
        requireSelectedCourse(user, lesson.getCourse().getId());
        return lesson;
    }

    private Exercise requireAccessibleExercise(User user, Long exerciseId) {
        Long requiredExerciseId = Objects.requireNonNull(exerciseId, "exerciseId must not be null");
        Exercise exercise = exerciseRepository.findById(requiredExerciseId)
                .orElseThrow(() -> new NoSuchElementException("Exercise not found: " + exerciseId));
        if (exercise.getLesson() == null) {
            throw new IllegalArgumentException("Exercise is not assigned to a lesson.");
        }
        requireAccessibleLesson(user, exercise.getLesson().getId());
        return exercise;
    }

    private AccountProfileDTO toProfileDTO(User user) {
        return new AccountProfileDTO(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getSecondName(),
                user.getGender(),
                user.getProfilePicture(),
                user.getProfilePictureCropX(),
                user.getProfilePictureCropY(),
                user.getProfilePictureCropWidth(),
                user.getProfilePictureCropHeight(),
                user.getRole() != null ? user.getRole().getName() : "USER");
    }

    private AccountSettingsDTO toSettingsDTO(User user) {
        return new AccountSettingsDTO(
                user.getId(),
                user.getEmail(),
                user.isActive(),
                user.getAuthProvider(),
                user.getPassword() != null && !user.getPassword().isBlank());
    }

    private AccountPhotoDTO toPhotoDTO(User user) {
        return new AccountPhotoDTO(
                user.getId(),
                user.getProfilePicture(),
                user.getProfilePicture() != null && !user.getProfilePicture().isBlank(),
                user.getProfilePictureCropX(),
                user.getProfilePictureCropY(),
                user.getProfilePictureCropWidth(),
                user.getProfilePictureCropHeight());
    }

    private NotificationDTO toNotificationDTO(Notification notification) {
        return new NotificationDTO(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt() != null ? notification.getCreatedAt().toString() : null,
                notification.getScheduledFor() != null ? notification.getScheduledFor().toString() : null,
                notification.getSentAt() != null ? notification.getSentAt().toString() : null);
    }
}
