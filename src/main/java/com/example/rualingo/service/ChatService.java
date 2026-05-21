package com.example.rualingo.service;

import org.springframework.stereotype.Service; // This fixes Line 4
import org.springframework.beans.factory.annotation.Autowired; // This fixes Line 6
import java.util.Optional; // This fixes Line 15
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.NoSuchElementException;
import com.example.rualingo.repository.VocabularyRepository; // This fixes Line 7 & 15
import com.example.rualingo.model.Vocabulary;
import com.example.rualingo.DTO.ActivityLogDTO;
import com.example.rualingo.DTO.CourseDTO;
import com.example.rualingo.DTO.LanguageDTO;
import com.example.rualingo.DTO.LessonDTO;
import com.example.rualingo.DTO.UserAnalyticsDTO;
import com.example.rualingo.model.Language;
import com.example.rualingo.model.User;
import com.example.rualingo.repository.LanguageRepository;
import com.example.rualingo.repository.UserRepository;

@Service
public class ChatService {
    @Autowired
    private VocabularyRepository vocabularyRepository;

    @Autowired
    private LanguageService languageService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private LessonService lessonService;

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private LanguageRepository languageRepository;

    @Autowired
    private UserRepository userRepository;

    private static final Pattern FIRST_NUMBER = Pattern.compile("\\b(\\d+)\\b");
    private static final Pattern TOKEN = Pattern.compile("[\\p{L}\\p{N}']+");
    private static final Pattern IN_LANGUAGE = Pattern.compile("(?i)\\b(?:in|to)\\s+([\\p{L}\\p{N} ][\\p{L}\\p{N} ]*)\\b");

    public String processInput(String input, Long userId) {
        String normalized = input == null ? "" : input.trim();
        if (normalized.isBlank()) {
            return "Rua says: Please type something (e.g., 'languages', 'courses', or a vocabulary word).";
        }

        String lower = normalized.toLowerCase(Locale.ROOT);
        if (containsAny(lower, "help", "what can you do", "commands")) {
            return """
                    Rua says: Try:
                    - languages
                    - courses (or: courses language 3)
                    - lessons (or: lessons course 2)
                    - my activity (requires login)
                    - analytics / my stats (requires login)
                    - or ask about a vocabulary word
                    """.trim();
        }

        // Translation: "good morning in tok pisin", "translate good morning to motu", "what is X in Y"
        if (looksLikeTranslationQuery(lower)) {
            TranslationRequest request = parseTranslationRequest(normalized);
            if (request.englishPhrase == null || request.englishPhrase.isBlank()) {
                return "Rua says: Tell me the English word/phrase to translate (e.g., 'good morning in Tok Pisin').";
            }

            List<Language> targetLanguages = resolveTargetLanguages(userId, request.languageNameOrNull);
            if (targetLanguages.isEmpty()) {
                if (request.languageNameOrNull != null) {
                    return "Rua says: I couldn't find a language named '" + request.languageNameOrNull + "'. Try 'languages' to see options.";
                }
                return "Rua says: Please specify a language (e.g., 'in Tok Pisin') or log in so I can use your learning language.";
            }

            String reply = translateEnglishPhrase(request.englishPhrase, targetLanguages);
            if (userId != null) {
                safeLogActivity(userId, "CHATBOT_TRANSLATE");
            }
            return reply;
        }

        // Activity logs (requires userId to be useful)
        if (containsAny(lower, "activity", "activity log", "activity logs", "history", "my activity")) {
            if (userId == null) {
                return "Rua says: Please log in so I can show your activity logs.";
            }
            List<ActivityLogDTO> logs = activityLogService.getActivityLogsByUserId(userId);
            if (logs.isEmpty()) {
                return "Rua says: No activity logs found for your account yet.";
            }
            StringBuilder sb = new StringBuilder("Rua says: Your recent activity:\n");
            int limit = Math.min(5, logs.size());
            for (int i = 0; i < limit; i++) {
                ActivityLogDTO log = logs.get(i);
                sb.append("- ")
                        .append(log.getAction() != null ? log.getAction() : "ACTION")
                        .append(log.getTimestamp() != null ? " @ " + log.getTimestamp() : "")
                        .append(log.getLessonId() != null ? " (lessonId=" + log.getLessonId() + ")" : "")
                        .append(log.getExerciseId() != null ? " (exerciseId=" + log.getExerciseId() + ")" : "")
                        .append("\n");
            }
            safeLogActivity(userId, "CHATBOT_VIEW_ACTIVITY");
            return sb.toString().trim();
        }

        // Analytics (requires userId)
        if (containsAny(lower, "analytics", "stats", "statistics", "my stats", "my analytics", "progress")) {
            if (userId == null) {
                return "Rua says: Please log in so I can generate your analytics.";
            }
            try {
                UserAnalyticsDTO analytics = activityLogService.getUserAnalytics(userId);
                StringBuilder sb = new StringBuilder("Rua says: Your analytics summary:\n");
                sb.append("- totalActivities: ").append(defaultLong(analytics.getTotalActivities())).append("\n");
                sb.append("- distinctLessons: ").append(defaultLong(analytics.getDistinctLessons())).append("\n");
                sb.append("- distinctExercises: ").append(defaultLong(analytics.getDistinctExercises())).append("\n");
                if (analytics.getLastActiveAt() != null) {
                    sb.append("- lastActiveAt: ").append(analytics.getLastActiveAt()).append("\n");
                }
                sb.append("- totalChats: ").append(defaultLong(analytics.getTotalChats())).append("\n");
                if (analytics.getLastChatAt() != null) {
                    sb.append("- lastChatAt: ").append(analytics.getLastChatAt()).append("\n");
                }
                if (analytics.getActivityCountsByAction() != null && !analytics.getActivityCountsByAction().isEmpty()) {
                    sb.append("- topActions:\n");
                    int shown = 0;
                    for (var entry : analytics.getActivityCountsByAction().entrySet()) {
                        sb.append("  - ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                        shown++;
                        if (shown >= 5) {
                            break;
                        }
                    }
                }
                safeLogActivity(userId, "CHATBOT_VIEW_ANALYTICS");
                return sb.toString().trim();
            } catch (NoSuchElementException ex) {
                return "Rua says: " + ex.getMessage();
            }
        }

        // Languages
        if (containsAny(lower, "language", "languages")) {
            List<LanguageDTO> languages = languageService.getAllLanguages();
            if (languages.isEmpty()) {
                return "Rua says: No languages found yet.";
            }
            StringBuilder sb = new StringBuilder("Rua says: Available languages:\n");
            for (LanguageDTO language : languages) {
                sb.append("- ").append(language.getId()).append(": ").append(nullToPlaceholder(language.getName()))
                        .append("\n");
            }
            safeLogActivity(userId, "CHATBOT_LIST_LANGUAGES");
            return sb.toString().trim();
        }

        // Courses
        if (containsAny(lower, "course", "courses")) {
            // Optional: "courses for language 3" or "courses language 3"
            Long languageId = extractFirstNumber(afterAny(lower, "language", "lang"));
            List<CourseDTO> courses;
            try {
                courses = languageId != null
                        ? courseService.getCoursesByLanguage(languageId)
                        : courseService.getAllCourses();
            } catch (NoSuchElementException ex) {
                return "Rua says: " + ex.getMessage();
            }
            if (courses.isEmpty()) {
                return languageId != null
                        ? "Rua says: No courses found for languageId=" + languageId + "."
                        : "Rua says: No courses found yet.";
            }
            StringBuilder sb = new StringBuilder("Rua says: Courses");
            if (languageId != null) {
                sb.append(" for languageId=").append(languageId);
            }
            sb.append(":\n");
            for (CourseDTO course : courses) {
                sb.append("- ").append(course.getId()).append(": ").append(nullToPlaceholder(course.getTitle()))
                        .append("\n");
            }
            safeLogActivity(userId, "CHATBOT_LIST_COURSES");
            return sb.toString().trim();
        }

        // Lessons
        if (containsAny(lower, "lesson", "lessons")) {
            // Optional: "lessons for course 2"
            Long courseId = extractFirstNumber(afterAny(lower, "course"));
            List<LessonDTO> lessons;
            try {
                lessons = courseId != null
                        ? lessonService.getLessonsByCourse(courseId)
                        : lessonService.getAllLessons();
            } catch (NoSuchElementException ex) {
                return "Rua says: " + ex.getMessage();
            }
            if (lessons.isEmpty()) {
                return courseId != null
                        ? "Rua says: No lessons found for courseId=" + courseId + "."
                        : "Rua says: No lessons found yet.";
            }
            StringBuilder sb = new StringBuilder("Rua says: Lessons");
            if (courseId != null) {
                sb.append(" for courseId=").append(courseId);
            }
            sb.append(":\n");
            for (LessonDTO lesson : lessons) {
                sb.append("- ").append(lesson.getId()).append(": ").append(nullToPlaceholder(lesson.getTitle()))
                        .append("\n");
            }
            safeLogActivity(userId, "CHATBOT_LIST_LESSONS");
            return sb.toString().trim();
        }

        // Vocabulary fallback (original behavior)
        for (String word : tokenize(lower)) {
            Optional<Vocabulary> match = vocabularyRepository.findByWord(word);
            if (match.isPresent()) {
                String reply = "Rua says: I recognize '" + word + "'. In English, that's '"
                        + nullToPlaceholder(match.get().getTranslation()) + "'. Keep practicing!";
                safeLogActivity(userId, "CHATBOT_VOCAB_LOOKUP");
                return reply;
            }
        }

        safeLogActivity(userId, "CHATBOT_UNKNOWN_QUERY");
        return "Rua says: I’m not sure about that yet. Try 'languages', 'courses', 'lessons', or ask about a vocabulary word.";
    }

    public UserAnalyticsDTO getUserAnalytics(Long userId) {
        return activityLogService.getUserAnalytics(userId);
    }

    private static boolean containsAny(String haystack, String... needles) {
        for (String needle : needles) {
            if (haystack.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private static String afterAny(String text, String... keywords) {
        for (String keyword : keywords) {
            int idx = text.indexOf(keyword);
            if (idx >= 0) {
                return text.substring(idx + keyword.length());
            }
        }
        return "";
    }

    private static Long extractFirstNumber(String text) {
        if (text == null) {
            return null;
        }
        Matcher matcher = FIRST_NUMBER.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        try {
            return Long.parseLong(matcher.group(1));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        Matcher matcher = TOKEN.matcher(text);
        java.util.ArrayList<String> tokens = new java.util.ArrayList<>();
        while (matcher.find()) {
            tokens.add(matcher.group().toLowerCase(Locale.ROOT));
        }
        return tokens;
    }

    private static String nullToPlaceholder(String value) {
        return value == null || value.isBlank() ? "(unnamed)" : value;
    }

    private static long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private void safeLogActivity(Long userId, String action) {
        if (userId == null) {
            return;
        }
        try {
            activityLogService.createActivityLog(userId, action, null, null, null);
        } catch (RuntimeException ignored) {
            // Don't fail the chatbot response if activity logging fails.
        }
    }

    private static boolean looksLikeTranslationQuery(String lower) {
        return lower.contains("translate")
                || lower.contains("translation")
                || lower.contains("how do you say")
                || lower.contains("what is")
                || lower.contains(" in ")
                || lower.contains(" to ");
    }

    private TranslationRequest parseTranslationRequest(String originalInput) {
        String input = originalInput == null ? "" : originalInput.trim();
        String lower = input.toLowerCase(Locale.ROOT);

        // Try: split at " in " / " to " (last occurrence wins for phrases)
        int idxIn = lastKeywordIndex(lower, " in ");
        int idxTo = lastKeywordIndex(lower, " to ");
        int splitIdx = Math.max(idxIn, idxTo);

        String englishPart = input;
        String languagePart = null;
        if (splitIdx >= 0) {
            englishPart = input.substring(0, splitIdx).trim();
            languagePart = input.substring(splitIdx + 4).trim(); // length of " in " / " to "
        } else {
            Matcher matcher = IN_LANGUAGE.matcher(input);
            if (matcher.find()) {
                languagePart = matcher.group(1) != null ? matcher.group(1).trim() : null;
                englishPart = input.substring(0, matcher.start()).trim();
            }
        }

        englishPart = stripLeadingPhrases(englishPart);
        englishPart = englishPart.replaceAll("[\"“”']", "").trim();
        if (languagePart != null) {
            languagePart = languagePart.replaceAll("[\"“”']", "").trim();
        }

        // If the englishPart is still long and contains " in <lang>" as part of phrase, ignore.
        return new TranslationRequest(englishPart, languagePart != null && !languagePart.isBlank() ? languagePart : null);
    }

    private static int lastKeywordIndex(String lower, String keywordWithSpaces) {
        return lower.lastIndexOf(keywordWithSpaces);
    }

    private static String stripLeadingPhrases(String text) {
        String t = text == null ? "" : text.trim();
        String lower = t.toLowerCase(Locale.ROOT);
        String[] prefixes = new String[] {
                "translate ",
                "translation of ",
                "what is ",
                "what's ",
                "how do you say "
        };
        for (String prefix : prefixes) {
            if (lower.startsWith(prefix)) {
                return t.substring(prefix.length()).trim();
            }
        }
        return t;
    }

    private List<Language> resolveTargetLanguages(Long userId, String languageNameOrNull) {
        if (languageNameOrNull != null && !languageNameOrNull.isBlank()) {
            List<Language> matches = languageRepository.findByNameContainingIgnoreCase(languageNameOrNull.trim());
            if (!matches.isEmpty()) {
                return matches;
            }
            // Try a few aliases
            String alias = languageNameOrNull.trim().toLowerCase(Locale.ROOT);
            if (alias.contains("tok") || alias.contains("pisin")) {
                matches = languageRepository.findByNameContainingIgnoreCase("tok");
                if (matches.isEmpty()) {
                    matches = languageRepository.findByNameContainingIgnoreCase("pisin");
                }
            } else if (alias.contains("motu") || alias.contains("hiri")) {
                matches = languageRepository.findByNameContainingIgnoreCase("motu");
                if (matches.isEmpty()) {
                    matches = languageRepository.findByNameContainingIgnoreCase("hiri");
                }
            }
            return matches;
        }

        if (userId == null) {
            return List.of();
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return List.of();
        }
        Set<Language> learning = user.getLanguages();
        if (learning == null || learning.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(learning);
    }

    private String translateEnglishPhrase(String englishPhrase, List<Language> targetLanguages) {
        String normalizedEnglish = englishPhrase.trim();
        String lowerEnglish = normalizedEnglish.toLowerCase(Locale.ROOT);

        StringBuilder sb = new StringBuilder("Rua says: Translation for '").append(normalizedEnglish).append("':\n");
        int found = 0;
        for (Language language : targetLanguages) {
            if (language == null) {
                continue;
            }
            Long languageId = language.getId();
            if (languageId == null) {
                continue;
            }

            List<Vocabulary> hits = vocabularyRepository.findByWordIgnoreCaseAndLanguageId(normalizedEnglish, languageId);
            if (hits.isEmpty()) {
                hits = vocabularyRepository.findByTranslationIgnoreCaseAndLanguageId(normalizedEnglish, languageId);
            }
            if (hits.isEmpty()) {
                hits = vocabularyRepository.findByWordContainingIgnoreCaseAndLanguageId(normalizedEnglish, languageId);
            }
            if (hits.isEmpty()) {
                hits = vocabularyRepository.findByTranslationContainingIgnoreCaseAndLanguageId(normalizedEnglish, languageId);
            }

            if (hits.isEmpty()) {
                continue;
            }

            String best = pickBestTranslation(normalizedEnglish, hits.get(0));
            if (best == null || best.isBlank()) {
                continue;
            }
            sb.append("- ").append(nullToPlaceholder(language.getName())).append(": ").append(best).append("\n");
            found++;
        }

        if (found == 0) {
            return "Rua says: I couldn't find a translation for '" + normalizedEnglish + "' in your vocabulary yet.";
        }
        return sb.toString().trim();
    }

    private static String pickBestTranslation(String englishPhrase, Vocabulary vocabulary) {
        if (vocabulary == null) {
            return null;
        }
        String wordTarget = vocabulary.getWordTarget();
        String word = vocabulary.getWord();
        String translation = vocabulary.getTranslation();

        if (word != null && word.equalsIgnoreCase(englishPhrase)) {
            return wordTarget != null && !wordTarget.isBlank() ? wordTarget : translation;
        }
        if (translation != null && translation.equalsIgnoreCase(englishPhrase)) {
            return wordTarget != null && !wordTarget.isBlank() ? wordTarget : word;
        }
        return wordTarget != null && !wordTarget.isBlank() ? wordTarget : (word != null ? word : translation);
    }

    private static final class TranslationRequest {
        private final String englishPhrase;
        private final String languageNameOrNull;

        private TranslationRequest(String englishPhrase, String languageNameOrNull) {
            this.englishPhrase = englishPhrase;
            this.languageNameOrNull = languageNameOrNull;
        }
    }
}
