package com.example.rualingo;

import com.example.rualingo.config.AuthProperties;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(AuthProperties.class)
public class RualingoApplication {

	public static void main(String[] args) {
		SpringApplication.run(RualingoApplication.class, args);
	}

	@Bean
	CommandLineRunner logConnectedDatabase(DataSource dataSource) {
		return args -> {
			try (Connection connection = dataSource.getConnection();
				 Statement statement = connection.createStatement();
				 ResultSet resultSet = statement.executeQuery("select database()")) {
				String db = resultSet.next() ? resultSet.getString(1) : null;
				System.out.println("[Rualingo] Connected schema: " + db);
			} catch (Exception ex) {
				System.out.println("[Rualingo] Failed to detect connected schema: " + ex.getMessage());
			}
		};
	}
}
