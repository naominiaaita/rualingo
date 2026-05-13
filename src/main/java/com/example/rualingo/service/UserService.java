package com.example.rualingo.service;

import com.example.rualingo.DTO.ActivityLogDTO;
import com.example.rualingo.DTO.CourseDTO;
import com.example.rualingo.DTO.LanguageDTO;
import com.example.rualingo.DTO.UserDTO;
import com.example.rualingo.DTO.UserResponseDTO;
import com.example.rualingo.model.ActivityLog;
import com.example.rualingo.model.Course;
import com.example.rualingo.model.Exercise;
import com.example.rualingo.model.Language;
import com.example.rualingo.model.Lesson;
import com.example.rualingo.model.Login;
import com.example.rualingo.model.Role;
import com.example.rualingo.model.User;
import com.example.rualingo.model.UserResponse;
import com.example.rualingo.repository.ActivityLogRepository;
import com.example.rualingo.repository.CourseRepository;
import com.example.rualingo.repository.ExerciseRepository;
import com.example.rualingo.repository.LanguageRepository;
import com.example.rualingo.repository.LessonRepository;
import com.example.rualingo.repository.LoginRepository;
import com.example.rualingo.repository.RoleRepository;
import com.example.rualingo.repository.UserRepository;
import com.example.rualingo.repository.UserResponseRepository;
import java.util.Objects;
import java.util.Optional;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final LoginRepository loginRepository;
    private final RoleRepository roleRepository;
    private final LanguageRepository languageRepository;
    private final CourseRepository courseRepository;
    private final ActivityLogRepository activityLogRepository;
    private final UserResponseRepository userResponseRepository;
    private final ExerciseRepository exerciseRepository;
    private final LessonRepository lessonRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            LoginRepository loginRepository,
            RoleRepository roleRepository,
            LanguageRepository languageRepository,
            CourseRepository courseRepository,
            ActivityLogRepository activityLogRepository,
            UserResponseRepository userResponseRepository,
            ExerciseRepository exerciseRepository,
            LessonRepository lessonRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.loginRepository = loginRepository;
        this.roleRepository = roleRepository;
        this.languageRepository = languageRepository;
        this.courseRepository = courseRepository;
        this.activityLogRepository = activityLogRepository;
        this.userResponseRepository = userResponseRepository;
        this.exerciseRepository = exerciseRepository;
        this.lessonRepository = lessonRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDTO createUser(UserDTO dto) {
        validateNewUser(dto);

        User user = toEntity(dto);
        User savedUser = userRepository.save(user);

        Login login = new Login(savedUser);
        savedUser.setLogin(login);
        loginRepository.save(login);

        return toDTO(savedUser);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long userId) {
        return toDTO(requireUser(userId));
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public UserDTO updateUser(Long userId, UserDTO dto) {
        User user = requireUser(userId);

        if (dto.getUsername() != null && !dto.getUsername().isBlank()
                && !dto.getUsername().equals(user.getUsername())) {
            validateUniqueUsername(dto.getUsername());
            user.setUsername(dto.getUsername());
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()
                && !dto.getEmail().equals(user.getEmail())) {
            validateUniqueEmail(dto.getEmail());
            user.setEmail(dto.getEmail());
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getFirst_name() != null) {
            user.setFirstName(dto.getFirst_name());
        }

        if (dto.getSecond_name() != null) {
            user.setSecondName(dto.getSecond_name());
        }

        if (dto.getGender() != null) {
            user.setGender(dto.getGender());
        }

        if (dto.getDate_of_birth() != null) {
            user.setDateOfBirth(dto.getDate_of_birth());
        }

        if (dto.getProvince_of_origin() != null) {
            user.setProvinceOfOrigin(dto.getProvince_of_origin());
        }

        if (dto.getIs_active() != null) {
            user.setActive(dto.getIs_active());
        }

        if (dto.getProfile_picture() != null) {
            user.setProfilePicture(dto.getProfile_picture());
        }
        if (dto.getRoleName() != null && !dto.getRoleName().isBlank()) {
            Role role = roleRepository.findByName(dto.getRoleName())
                    .orElseThrow(() -> new NoSuchElementException("Role not found: " + dto.getRoleName()));
            user.setRole(role);
        }

        User saved = userRepository.save(user);
        syncLogin(saved);
        return toDTO(saved);
    }

    public void deleteUser(Long userId) {
        User user = requireUser(userId);
        loginRepository.findByUser(user).ifPresent(loginRepository::delete);
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public boolean userExists(Long userId) {
        Long requiredUserId = Objects.requireNonNull(userId, "userId must not be null");
        return userRepository.existsById(requiredUserId);
    }

    @Transactional(readOnly = true)
    public Optional<User> findEntityById(Long userId) {
        Long requiredUserId = Objects.requireNonNull(userId, "userId must not be null");
        return userRepository.findById(requiredUserId);
    }

    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByUsername(String username) {
        return userRepository.findByUsername(username).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByRoleName(String roleName) {
        String requiredRoleName = Objects.requireNonNull(roleName, "roleName must not be null");
        return userRepository.findByRoleNameIgnoreCase(requiredRoleName).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public UserDTO register(UserDTO dto) {
        return createUser(dto);
    }

    @Transactional(readOnly = true)
    public boolean authenticate(String email, String rawPassword) {
        return userRepository.findByEmail(email)
                .map(user -> user.getPassword() != null && passwordEncoder.matches(rawPassword, user.getPassword()))
                .orElse(false);
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = requireUser(userId);
        if (user.getPassword() == null || !passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        User saved = userRepository.save(user);
        syncLogin(saved);
    }

    public void resetPassword(Long userId, String newPassword) {
        User user = requireUser(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        User saved = userRepository.save(user);
        syncLogin(saved);
    }

    public void deactivateUser(Long userId) {
        User user = requireUser(userId);
        user.setActive(false);
        userRepository.save(user);
    }

    public void activateUser(Long userId) {
        User user = requireUser(userId);
        user.setActive(true);
        userRepository.save(user);
    }

    public UserDTO assignRole(Long userId, Long roleId) {
        User user = requireUser(userId);
        Long requiredRoleId = Objects.requireNonNull(roleId, "roleId must not be null");
        Role role = roleRepository.findById(requiredRoleId)
                .orElseThrow(() -> new NoSuchElementException("Role not found: " + roleId));
        user.setRole(role);
        return toDTO(userRepository.save(user));
    }

    public UserDTO revokeRole(Long userId) {
        User user = requireUser(userId);
        Role fallbackRole = roleRepository.findByName("USER")
                .orElseGet(() -> roleRepository.save(new Role("USER", "Default application user role")));
        user.setRole(fallbackRole);
        return toDTO(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public Boolean getIs_active(Long userId) {
        return requireUser(userId).isActive();
    }

    public UserDTO updateUsername(Long userId, String username) {
        User user = requireUser(userId);
        if (username != null && !username.equals(user.getUsername())) {
            validateUniqueUsername(username);
            user.setUsername(username);
        }
        User savedUser = Objects.requireNonNull(userRepository.save(user), "Saved user must not be null");
        return toDTO(savedUser);
    }

    public UserDTO updateEmail(Long userId, String email) {
        User user = requireUser(userId);
        if (email != null && !email.equals(user.getEmail())) {
            validateUniqueEmail(email);
            user.setEmail(email);
        }
        User saved = userRepository.save(user);
        syncLogin(saved);
        return toDTO(saved);
    }

    public UserDTO updateFirst_name(Long userId, String first_name) {
        User user = requireUser(userId);
        user.setFirstName(first_name);
        User savedUser = Objects.requireNonNull(userRepository.save(user), "Saved user must not be null");
        return toDTO(savedUser);
    }

    public UserDTO updateSecond_name(Long userId, String second_name) {
        User user = requireUser(userId);
        user.setSecondName(second_name);
        User savedUser = Objects.requireNonNull(userRepository.save(user), "Saved user must not be null");
        return toDTO(savedUser);
    }

    @Transactional(readOnly = true)
    public String getProfile_picture(Long userId) {
        return requireUser(userId).getProfilePicture();
    }

    public UserDTO updateProfile_picture(Long userId, String profile_picture) {
        User user = requireUser(userId);
        user.setProfilePicture(profile_picture);
        User savedUser = Objects.requireNonNull(userRepository.save(user), "Saved user must not be null");
        return toDTO(savedUser);
    }

    public UserDTO deleteProfile_picture(Long userId) {
        User user = requireUser(userId);
        user.setProfilePicture(null);
        User savedUser = Objects.requireNonNull(userRepository.save(user), "Saved user must not be null");
        return toDTO(savedUser);
    }

    public UserDTO addLanguageToUser(Long userId, Long languageId) {
        User user = requireUser(userId);
        Long requiredLanguageId = Objects.requireNonNull(languageId, "languageId must not be null");
        Language language = languageRepository.findById(requiredLanguageId)
                .orElseThrow(() -> new NoSuchElementException("Language not found: " + languageId));
        user.getLanguages().add(language);
        User savedUser = Objects.requireNonNull(userRepository.save(user), "Saved user must not be null");
        return toDTO(savedUser);
    }

    public UserDTO removeLanguageFromUser(Long userId, Long languageId) {
        User user = requireUser(userId);
        user.getLanguages().removeIf(language -> language.getId().equals(languageId));
        User savedUser = Objects.requireNonNull(userRepository.save(user), "Saved user must not be null");
        return toDTO(savedUser);
    }

    @Transactional(readOnly = true)
    public Set<LanguageDTO> getUserLanguages(Long userId) {
        return requireUser(userId).getLanguages().stream()
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

    public UserDTO enrollUserInCourse(Long userId, Long courseId) {
        User user = requireUser(userId);
        Long requiredCourseId = Objects.requireNonNull(courseId, "courseId must not be null");
        Course course = courseRepository.findById(requiredCourseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found: " + courseId));
        user.getCourses().add(course);
        User savedUser = Objects.requireNonNull(userRepository.save(user), "Saved user must not be null");
        return toDTO(savedUser);
    }

    public UserDTO unenrollUserFromCourse(Long userId, Long courseId) {
        User user = requireUser(userId);
        user.getCourses().removeIf(course -> course.getId().equals(courseId));
        User savedUser = Objects.requireNonNull(userRepository.save(user), "Saved user must not be null");
        return toDTO(savedUser);
    }

    @Transactional(readOnly = true)
    public Set<CourseDTO> getUserCourses(Long userId) {
        return requireUser(userId).getCourses().stream()
                .map(course -> new CourseDTO(
                        course.getId(),
                        course.getTitle(),
                        course.getName(),
                        course.getDescription(),
                        course.getLanguage() != null ? course.getLanguage().getId() : null))
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public List<ActivityLogDTO> getUserActivityLogs(Long userId) {
        Long requiredUserId = Objects.requireNonNull(userId, "userId must not be null");
        requireUser(requiredUserId);
        return activityLogRepository.findByUserIdOrderByTimestampDesc(requiredUserId).stream()
                .map(log -> {
                    ActivityLogDTO dto = new ActivityLogDTO();
                    dto.setId(log.getId());
                    dto.setAction(log.getAction());
                    dto.setTimestamp(log.getTimestamp() != null ? log.getTimestamp().toString() : null);
                    dto.setLessonId(log.getLesson() != null ? log.getLesson().getId() : null);
                    dto.setExerciseId(log.getExercise() != null ? log.getExercise().getId() : null);
                    dto.setUserId(log.getUser() != null ? log.getUser().getId() : null);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public void logUserActivity(Long userId, String action, Long lessonId, Long exerciseId) {
        User user = requireUser(userId);
        ActivityLog activityLog = new ActivityLog();
        activityLog.setAction(action);
        activityLog.setTimestamp(java.time.LocalDateTime.now());
        activityLog.setUser(user);

        if (lessonId != null) {
            Long requiredLessonId = Objects.requireNonNull(lessonId, "lessonId must not be null");
            Lesson lesson = lessonRepository.findById(requiredLessonId)
                    .orElseThrow(() -> new NoSuchElementException("Lesson not found: " + lessonId));
            activityLog.setLesson(lesson);
        }

        if (exerciseId != null) {
            Long requiredExerciseId = Objects.requireNonNull(exerciseId, "exerciseId must not be null");
            Exercise exercise = exerciseRepository.findById(requiredExerciseId)
                    .orElseThrow(() -> new NoSuchElementException("Exercise not found: " + exerciseId));
            activityLog.setExercise(exercise);
        }

        activityLogRepository.save(activityLog);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getUserResponses(Long userId) {
        Long requiredUserId = Objects.requireNonNull(userId, "userId must not be null");
        requireUser(requiredUserId);
        return userResponseRepository.findByUserId(requiredUserId).stream()
                .map(response -> new UserResponseDTO(
                        response.getId(),
                        response.getAnswer(),
                        response.getIsCorrect(),
                        response.getAttempts(),
                        response.getResponseTime() != null ? response.getResponseTime().toString() : null,
                        response.getTimeStamp() != null ? response.getTimeStamp().toString() : null,
                        response.getUser() != null ? response.getUser().getId() : null,
                        response.getExercise() != null ? response.getExercise().getId() : null))
                .collect(Collectors.toList());
    }

    public void saveUserResponse(Long userId, Long exerciseId, String answer, boolean isCorrect, int attempts) {
        User user = requireUser(userId);
        if (exerciseId == null) {
            throw new IllegalArgumentException("Exercise ID must not be null.");
        }
        Long requiredExerciseId = Objects.requireNonNull(exerciseId, "exerciseId must not be null");
        Exercise exercise = exerciseRepository.findById(requiredExerciseId)
                .orElseThrow(() -> new NoSuchElementException("Exercise not found: " + exerciseId));

        UserResponse response = new UserResponse();
        response.setUser(user);
        response.setExercise(exercise);
        response.setAnswer(answer);
        response.setAttempts(attempts);
        response.setResponseTime(java.time.LocalDateTime.now());
        response.setTimeStamp(java.time.LocalDateTime.now());

        boolean calculatedCorrect = isCorrect;
        if (exercise.getCorrectAnswer() != null && answer != null) {
            calculatedCorrect = exercise.getCorrectAnswer().trim().equalsIgnoreCase(answer.trim());
        }
        response.setIsCorrect(calculatedCorrect);

        userResponseRepository.save(response);
    }

    @Transactional(readOnly = true)
    public long countCorrectResponses(Long userId) {
        Long requiredUserId = Objects.requireNonNull(userId, "userId must not be null");
        requireUser(requiredUserId);
        return userResponseRepository.countCorrectByUserId(requiredUserId);
    }

    @Transactional(readOnly = true)
    public double getAccuracy(Long userId) {
        Long requiredUserId = Objects.requireNonNull(userId, "userId must not be null");
        requireUser(requiredUserId);
        long total = userResponseRepository.countByUserId(requiredUserId);
        if (total == 0) {
            return 0.0;
        }
        long correct = userResponseRepository.countCorrectByUserId(requiredUserId);
        return (correct * 100.0) / total;
    }

    public UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPassword(null);
        dto.setFirst_name(user.getFirstName());
        dto.setSecond_name(user.getSecondName());
        dto.setGender(user.getGender());
        dto.setDate_of_birth(user.getDateOfBirth());
        dto.setProvince_of_origin(user.getProvinceOfOrigin());
        dto.setIs_active(user.isActive());
        dto.setProfile_picture(user.getProfilePicture());
        dto.setRoleName(user.getRole() != null ? user.getRole().getName() : null);
        return dto;
    }

    public User toEntity(UserDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        if (dto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        user.setFirstName(dto.getFirst_name());
        user.setSecondName(dto.getSecond_name());
        user.setGender(dto.getGender());
        user.setDateOfBirth(dto.getDate_of_birth());
        user.setProvinceOfOrigin(dto.getProvince_of_origin());
        user.setActive(dto.getIs_active() != null ? dto.getIs_active() : true);
        user.setAuthProvider("LOCAL");
        user.setProfilePicture(dto.getProfile_picture());
        if (dto.getRoleName() != null && !dto.getRoleName().isBlank()) {
            Role role = roleRepository.findByName(dto.getRoleName())
                    .orElseThrow(() -> new NoSuchElementException("Role not found: " + dto.getRoleName()));
            user.setRole(role);
        }
        return user;
    }

    public void validateNewUser(UserDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("UserDTO must not be null.");
        }
        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }
        validateUniqueUsername(dto.getUsername());
        validateUniqueEmail(dto.getEmail());
    }

    public void validateUniqueEmail(String email) {
        String requiredEmail = Objects.requireNonNull(email, "email must not be null");
        if (userRepository.existsByEmail(requiredEmail)) {
            throw new IllegalArgumentException("Email is already in use.");
        }
    }

    public void validateUniqueUsername(String username) {
        String requiredUsername = Objects.requireNonNull(username, "username must not be null");
        if (userRepository.existsByUsername(requiredUsername)) {
            throw new IllegalArgumentException("Username is already in use.");
        }
    }

    private User requireUser(Long userId) {
        Long requiredUserId = Objects.requireNonNull(userId, "userId must not be null");
        return userRepository.findById(requiredUserId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
    }

    private void syncLogin(User user) {
        Login login = Optional.ofNullable(user.getLogin())
                .or(() -> loginRepository.findByUser(user))
                .orElseGet(Login::new);
        login.setUser(user);
        user.setLogin(login);
        loginRepository.save(login);
    }

}
