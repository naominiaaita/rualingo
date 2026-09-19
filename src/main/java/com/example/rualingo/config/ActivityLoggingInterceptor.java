package com.example.rualingo.config;

import com.example.rualingo.model.User;
import com.example.rualingo.service.ActivityLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ActivityLoggingInterceptor implements HandlerInterceptor {

    private static final Pattern LESSON_ID_PATTERN = Pattern.compile(".*/lessons?/(?<lessonId>\\d+).*", Pattern.CASE_INSENSITIVE);
    private static final Pattern EXERCISE_ID_PATTERN = Pattern.compile(".*/exercises?/(?<exerciseId>\\d+).*", Pattern.CASE_INSENSITIVE);

    private final ActivityLogService activityLogService;

    public ActivityLoggingInterceptor(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (ex != null || response.getStatus() >= 400) {
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            return;
        }

        Long userId = user.getId();
        if (userId == null) {
            return;
        }

        String action = request.getMethod() + " " + request.getRequestURI();
        Long lessonId = extractId(LESSON_ID_PATTERN, request.getRequestURI());
        Long exerciseId = extractId(EXERCISE_ID_PATTERN, request.getRequestURI());

        try {
            activityLogService.createActivityLog(userId, action, lessonId, exerciseId, null);
        } catch (Exception ignore) {
            // Do not fail the user request because activity logging could not be recorded.
        }
    }

    private Long extractId(Pattern pattern, String uri) {
        if (uri == null) {
            return null;
        }
        Matcher matcher = pattern.matcher(uri);
        if (!matcher.find()) {
            return null;
        }
        try {
            String id = matcher.group(1);
            return id != null ? Long.valueOf(id) : null;
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            return null;
        }
    }
}
