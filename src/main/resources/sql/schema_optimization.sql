-- =============================================================================
-- RUALINGO MASTER DATABASE OPTIMIZATION & ANALYTICS SCRIPT
-- Purpose: Documentation Evidence for Capstone 1: Database Development (100 Marks)
-- =============================================================================

-- SECTION 1: ADVANCED ANALYTICS (SELECT, JOIN, GROUP BY)
-- Purpose: Calculate real-time accuracy percentage per language across 5 tables.
-- Demonstrates: 3NF Joins, Conditional Aggregation, and Grouping.

SELECT
    l.name AS language_name,
    COUNT(DISTINCT ur.user_id) AS total_active_students,
    COUNT(ur.user_answer) AS total_drills_answered,
    ROUND(AVG(CASE WHEN ur.is_correct = TRUE THEN 100 ELSE 0 END), 2) AS average_accuracy_percentage
FROM
    language l
JOIN
    course c ON l.language_id = c.language_id
JOIN
    lesson les ON c.course_id = les.course_id
JOIN
    exercise e ON les.lesson_id = e.lesson_id
JOIN
    user_response ur ON e.exercise_id = ur.exercise_id
GROUP BY
    l.language_id
ORDER BY
    average_accuracy_percentage DESC;


-- SECTION 2: STORED PROCEDURES & MODULAR LOGIC
-- Purpose: Automate student streak management at the database layer.
-- Demonstrates: DELIMITER usage, INTERVAL logic, and Bulk Updates.

DELIMITER //

CREATE PROCEDURE ResetInactiveStreaks()
BEGIN
    -- Identification: Users whose last activity was > 48 hours ago
    UPDATE user
    SET streak = 0
    WHERE last_streak_update < NOW() - INTERVAL 2 DAY
    AND streak > 0;

    SELECT 'Student streaks successfully synchronized with server time' AS log_status;
END //

DELIMITER ;


-- SECTION 3: CRUD OPERATIONS (THE DATA LIFECYCLE)
-- Demonstrates: INSERT, SELECT (Filter), UPDATE (Moderation), and DELETE (Cascade).

-- 3.1 CREATE: Bulk Seed Cultural Vocabulary
INSERT INTO vocabulary (word_target, word, translation, topic, language_id, lesson_id)
VALUES
('Gaba', 'Gaba', 'Market', 'Trade', 1, 1),
('Oi hege', 'Oi hege', 'Turn around', 'Directions', 1, 2);

-- 3.2 READ: Check Sequential Progress (Conditional Read)
SELECT COUNT(*) FROM activity_log
WHERE user_id = 1
AND lesson_id = 1
AND action = 'LESSON_COMPLETED';

-- 3.3 UPDATE: Administrative Moderation
UPDATE lesson
SET submission_status = 'APPROVED',
    moderation_note = 'Content verified by linguist team'
WHERE lesson_id = 50;

-- 3.4 DELETE: Data Privacy & Cascade Proof
-- Note: This triggers ON DELETE CASCADE across activity_log and chat_logs
DELETE FROM user
WHERE is_active = FALSE
AND last_streak_update < NOW() - INTERVAL 1 YEAR;


-- SECTION 4: PERFORMANCE OPTIMIZATION (INDEXES)
-- Purpose: Ensure sub-second response times for the AI Chatbot and Analytics.

-- Improve word lookup speed for the translation engine
CREATE INDEX idx_vocabulary_lookup ON vocabulary(word_target, language_id);

-- Speed up streak calculations for the dashboard
CREATE INDEX idx_user_activity ON user(last_streak_update, streak);

-- Optimize AI memory retrieval
CREATE INDEX idx_chat_history ON chat_logs(user_id, timestamp);
