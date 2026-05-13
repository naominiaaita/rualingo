-- Sample data for testing

-- Insert roles
INSERT INTO role (name, description) VALUES ('ADMIN', 'Administrator role');
INSERT INTO role (name, description) VALUES ('USER', 'Regular user role');

-- Insert users
INSERT INTO `user` (username, email, password, first_name, second_name, gender, is_active, role_id) VALUES
('admin', 'admin@example.com', 'password', 'Admin', 'User', 'Male', 1, 1),
('user1', 'user1@example.com', 'password', 'John', 'Doe', 'Male', 1, 2);

-- Insert languages
INSERT INTO language (name, province, district, clan, flag, user_id) VALUES
('English', 'Province1', 'District1', 'Clan1', 'flag1.png', 1),
('Spanish', 'Province2', 'District2', 'Clan2', 'flag2.png', 2);

-- Insert courses
INSERT INTO course (course_id, title, name, description, category, language_id) VALUES
(1, 'Basic English', 'English Basics', 'Learn basic English', 'Language', 1),
(2, 'Intermediate Spanish', 'Spanish Intermediate', 'Learn intermediate Spanish', 'Language', 2);

INSERT INTO course_metadata (course_id, meta_key, meta_value) VALUES
(1, 'level', 'beginner'),
(2, 'level', 'intermediate');

-- Insert lessons
INSERT INTO lesson (title, content, course_id) VALUES
('Greetings', 'Learn basic greetings', 1),
('Numbers', 'Learn numbers 1-10', 1);

-- Insert exercises (questions)
INSERT INTO exercise (exercise_id, type, question_text, question, correct_answer, hint, lesson_id) VALUES
(1, 'multiple_choice', 'What is the capital of France?', 'What is the capital of France?', 'Paris', 'It starts with P', 1),
(2, 'multiple_choice', 'What is 2 + 2?', 'What is 2 + 2?', '4', 'Basic math', 2),
(3, 'multiple_choice', 'How do you say hello in Spanish?', 'How do you say hello in Spanish?', 'Hola', 'Common greeting', 1);

INSERT INTO exercise_option (exercise_id, option_order, option_text) VALUES
(1, 1, 'Paris'),
(1, 2, 'London'),
(1, 3, 'Berlin'),
(1, 4, 'Madrid'),
(2, 1, '3'),
(2, 2, '4'),
(2, 3, '5'),
(2, 4, '6'),
(3, 1, 'Hola'),
(3, 2, 'Adios'),
(3, 3, 'Gracias'),
(3, 4, 'Por favor');

-- Insert vocabulary
INSERT INTO vocabulary (word_target, word, translation, language_id, course_id, lesson_id) VALUES
('Hello', 'Hola', 'Hello', 2, 2, NULL),
('Goodbye', 'Adios', 'Goodbye', 2, 2, NULL);
