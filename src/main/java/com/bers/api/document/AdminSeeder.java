package com.bers.api.document;

import com.bers.domain.entities.User;
import com.bers.domain.entities.enums.UserRole;
import com.bers.domain.entities.enums.UserStatus;
import com.bers.domain.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

//@Component
public record AdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) implements CommandLineRunner {

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("admin@busconnect.com").isEmpty()) {
            User admin = User.builder()
                    .username("Juan Carlos")
                    .email("juan759madrid@gmail.com")
                    .phone("3226933699")
                    .dateOfBirth(LocalDate.of(2002, 03, 27))
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .role(UserRole.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(admin);
            System.out.println(" Admin user created: " + admin.getEmail() + " / ADMIN");
        }
    }
}
