CREATE TABLE IF NOT EXISTS role (
    role_id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    PRIMARY KEY (role_id),
    CONSTRAINT uk_role_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS `user` (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(255),
    email VARCHAR(255),
    password VARCHAR(255),
    first_name VARCHAR(255),
    second_name VARCHAR(255),
    gender VARCHAR(255),
    date_of_birth VARCHAR(255),
    province_of_origin VARCHAR(255),
    is_active BIT(1) NOT NULL DEFAULT 0,
    auth_provider VARCHAR(50),
    provider_user_id VARCHAR(255),
    role_id BIGINT,
    profile_picture VARCHAR(255),
    profile_picture_crop_x INTEGER,
    profile_picture_crop_y INTEGER,
    profile_picture_crop_width INTEGER,
    profile_picture_crop_height INTEGER,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_user_role
        FOREIGN KEY (role_id) REFERENCES role (role_id)
);

CREATE TABLE IF NOT EXISTS language (
    language_id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255),
    province VARCHAR(255),
    district VARCHAR(255),
    clan VARCHAR(255),
    flag VARCHAR(255),
    user_id BIGINT,
    PRIMARY KEY (language_id),
    CONSTRAINT fk_language_user
        FOREIGN KEY (user_id) REFERENCES `user` (user_id)
);

CREATE TABLE IF NOT EXISTS course (
    course_id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255),
    name VARCHAR(255),
    description VARCHAR(255),
    category VARCHAR(255),
    submission_status VARCHAR(50),
    moderation_note VARCHAR(1000),
    reviewed_at TIMESTAMP NULL,
    language_id BIGINT,
    PRIMARY KEY (course_id),
    CONSTRAINT fk_course_language
        FOREIGN KEY (language_id) REFERENCES language (language_id)
);

CREATE TABLE IF NOT EXISTS course_metadata (
    course_metadata_id BIGINT NOT NULL AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    meta_key VARCHAR(255) NOT NULL,
    meta_value VARCHAR(1000),
    PRIMARY KEY (course_metadata_id),
    CONSTRAINT uk_course_metadata_key UNIQUE (course_id, meta_key),
    CONSTRAINT fk_course_metadata_course
        FOREIGN KEY (course_id) REFERENCES course (course_id)
);

CREATE TABLE IF NOT EXISTS lesson (
    lesson_id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255),
    description VARCHAR(255),
    content VARCHAR(255),
    submission_status VARCHAR(50),
    moderation_note VARCHAR(1000),
    reviewed_at TIMESTAMP NULL,
    course_id BIGINT,
    PRIMARY KEY (lesson_id),
    CONSTRAINT fk_lesson_course
        FOREIGN KEY (course_id) REFERENCES course (course_id)
);

CREATE TABLE IF NOT EXISTS exercise (
    exercise_id BIGINT NOT NULL AUTO_INCREMENT,
    type VARCHAR(255),
    question_text VARCHAR(255),
    question VARCHAR(255),
    correct_answer VARCHAR(255),
    hint VARCHAR(255),
    lesson_id BIGINT,
    PRIMARY KEY (exercise_id),
    CONSTRAINT fk_exercise_lesson
        FOREIGN KEY (lesson_id) REFERENCES lesson (lesson_id)
);

CREATE TABLE IF NOT EXISTS exercise_option (
    exercise_option_id BIGINT NOT NULL AUTO_INCREMENT,
    exercise_id BIGINT NOT NULL,
    option_order INTEGER NOT NULL,
    option_text VARCHAR(255) NOT NULL,
    PRIMARY KEY (exercise_option_id),
    CONSTRAINT uk_exercise_option_order UNIQUE (exercise_id, option_order),
    CONSTRAINT fk_exercise_option_exercise
        FOREIGN KEY (exercise_id) REFERENCES exercise (exercise_id)
);

CREATE TABLE IF NOT EXISTS vocabulary (
    vocab_id BIGINT NOT NULL AUTO_INCREMENT,
    word_target VARCHAR(255),
    word VARCHAR(255),
    phonetic VARCHAR(255),
    example_sentence VARCHAR(255),
    translation VARCHAR(255),
    language_id BIGINT,
    course_id BIGINT,
    lesson_id BIGINT,
    PRIMARY KEY (vocab_id),
    CONSTRAINT fk_vocabulary_language
        FOREIGN KEY (language_id) REFERENCES language (language_id),
    CONSTRAINT fk_vocabulary_course
        FOREIGN KEY (course_id) REFERENCES course (course_id),
    CONSTRAINT fk_vocabulary_lesson
        FOREIGN KEY (lesson_id) REFERENCES lesson (lesson_id)
);

CREATE TABLE IF NOT EXISTS login (
    login_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT,
    PRIMARY KEY (login_id),
    CONSTRAINT uk_login_user UNIQUE (user_id),
    CONSTRAINT fk_login_user
        FOREIGN KEY (user_id) REFERENCES `user` (user_id)
);

CREATE TABLE IF NOT EXISTS user_response (
    userresponse_id BIGINT NOT NULL AUTO_INCREMENT,
    user_answer VARCHAR(255),
    is_correct BIT(1),
    attempts INTEGER,
    response_time TIMESTAMP,
    time_stamp TIMESTAMP,
    user_id BIGINT,
    exercise_id BIGINT,
    PRIMARY KEY (userresponse_id),
    CONSTRAINT fk_user_response_user
        FOREIGN KEY (user_id) REFERENCES `user` (user_id),
    CONSTRAINT fk_user_response_exercise
        FOREIGN KEY (exercise_id) REFERENCES exercise (exercise_id)
);

CREATE TABLE IF NOT EXISTS activity_log (
    activity_log_id BIGINT NOT NULL AUTO_INCREMENT,
    action VARCHAR(255),
    time_stamp TIMESTAMP,
    lesson_id BIGINT,
    exercise_id BIGINT,
    user_id BIGINT,
    PRIMARY KEY (activity_log_id),
    CONSTRAINT fk_activity_log_lesson
        FOREIGN KEY (lesson_id) REFERENCES lesson (lesson_id),
    CONSTRAINT fk_activity_log_exercise
        FOREIGN KEY (exercise_id) REFERENCES exercise (exercise_id),
    CONSTRAINT fk_activity_log_user
        FOREIGN KEY (user_id) REFERENCES `user` (user_id)
);

CREATE TABLE IF NOT EXISTS user_notification (
    notification_id BIGINT NOT NULL AUTO_INCREMENT,
    type VARCHAR(100) NOT NULL,
    message_text VARCHAR(500) NOT NULL,
    is_read BIT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    scheduled_for TIMESTAMP NULL,
    sent_at TIMESTAMP NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (notification_id),
    CONSTRAINT fk_notification_user
        FOREIGN KEY (user_id) REFERENCES `user` (user_id)
);

CREATE TABLE IF NOT EXISTS language_has_user (
    user_user_id BIGINT NOT NULL,
    language_language_id BIGINT NOT NULL,
    PRIMARY KEY (user_user_id, language_language_id),
    CONSTRAINT fk_language_has_user_user
        FOREIGN KEY (user_user_id) REFERENCES `user` (user_id),
    CONSTRAINT fk_language_has_user_language
        FOREIGN KEY (language_language_id) REFERENCES language (language_id)
);

CREATE TABLE IF NOT EXISTS course_has_user (
    user_user_id BIGINT NOT NULL,
    course_course_id BIGINT NOT NULL,
    PRIMARY KEY (user_user_id, course_course_id),
    CONSTRAINT fk_course_has_user_user
        FOREIGN KEY (user_user_id) REFERENCES `user` (user_id),
    CONSTRAINT fk_course_has_user_course
        FOREIGN KEY (course_course_id) REFERENCES course (course_id)
);
