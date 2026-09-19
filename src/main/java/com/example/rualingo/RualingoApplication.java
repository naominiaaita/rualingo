package com.example.rualingo;

import com.example.rualingo.service.LeaderboardService;
import com.example.rualingo.model.Role;
import com.example.rualingo.model.User;
import com.example.rualingo.repository.RoleRepository;
import com.example.rualingo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Optional;
import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableConfigurationProperties({AuthProperties.class})
@EnableAsync
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

	@Bean
	CommandLineRunner initAdmin(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			Role adminRole = roleRepository.findByName("ADMIN")
					.orElseGet(() -> roleRepository.save(new Role("ADMIN", "System Admin")));

			Optional<User> existingAdmin = userRepository.findByEmail("admin@rualingo.com");
			if (existingAdmin.isEmpty()) {
				User admin = new User();
				admin.setUsername("admin");
				admin.setEmail("admin@rualingo.com");
				admin.setPassword(passwordEncoder.encode("AdminPassword123!"));
				admin.setFirstName("System");
				admin.setSecondName("Admin");
				admin.setRole(adminRole);
				admin.setActive(true);
				admin.setAuthProvider("LOCAL");
				userRepository.save(admin);
				System.out.println("[Rualingo] Emergency Admin created: admin@rualingo.com / AdminPassword123!");
			} else {
				User admin = existingAdmin.get();
				admin.setPassword(passwordEncoder.encode("AdminPassword123!"));
				userRepository.save(admin);
				System.out.println("[Rualingo] Admin password reset for: admin@rualingo.com -> AdminPassword123!");
			}
		};
	}

	@Bean
	CommandLineRunner warmupRedis(UserRepository userRepository, LeaderboardService leaderboardService) {
		return args -> {
			System.out.println("[Rualingo] Warming up Redis leaderboard from MySQL...");
			List<User> users = userRepository.findAll();
			for (User user : users) {
				if (user.getUsername() != null) {
					int streak = user.getStreak() != null ? user.getStreak() : 0;
					leaderboardService.updateScore(user.getUsername(), streak);
				}
			}
			System.out.println("[Rualingo] Redis warmup complete. Synced " + users.size() + " users.");
		};
	}
}


