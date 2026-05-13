package com.example.rualingo.controller;

import com.example.rualingo.repository.CourseRepository;
import com.example.rualingo.repository.LessonRepository;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/debug")
public class DebugDbController {

    private final DataSource dataSource;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;

    public DebugDbController(
            DataSource dataSource,
            CourseRepository courseRepository,
            LessonRepository lessonRepository) {
        this.dataSource = dataSource;
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
    }

    @GetMapping("/db")
    public Map<String, Object> dbInfo() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.putAll(connectionInfo());
        out.put("courses_count", courseRepository.count());
        out.put("lessons_count", lessonRepository.count());
        return out;
    }

    private Map<String, Object> connectionInfo() {
        Map<String, Object> out = new LinkedHashMap<>();
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("""
                        select
                          database() as connected_schema,
                          @@hostname as hostname,
                          @@port as port,
                          @@socket as socket,
                          @@datadir as datadir
                        """)) {
            if (resultSet.next()) {
                out.put("connected_schema", resultSet.getString("connected_schema"));
                out.put("hostname", resultSet.getString("hostname"));
                out.put("port", resultSet.getInt("port"));
                out.put("socket", resultSet.getString("socket"));
                out.put("datadir", resultSet.getString("datadir"));
            } else {
                out.put("connected_schema", null);
            }
        } catch (Exception ex) {
            out.put("connected_schema", null);
            out.put("error", ex.getMessage());
        }
        return out;
    }
}
