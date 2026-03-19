package com.regalos.back_regalos.config;

import com.regalos.back_regalos.models.*;
import com.regalos.back_regalos.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StoreSettingRepository settingRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 Starting Data Initialization...");

        // --- VERIFY TABLES ---
        try {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS store_banners (id SERIAL PRIMARY KEY, image_base64 TEXT NOT NULL, title VARCHAR(255) NOT NULL, subtitle VARCHAR(255), display_order INTEGER DEFAULT 0, is_active BOOLEAN DEFAULT true, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            System.out.println("✅ Table store_banners verified.");
        } catch (Exception e) {
            System.err.println("❌ Error creating table: " + e.getMessage());
        }

        // --- ENSURE ROLES EXIST ---
        if (roleRepository.count() == 0) {
            roleRepository.save(Role.builder().name("SUPER_ADMIN").description("Administrador total").isActive(true).build());
            roleRepository.save(Role.builder().name("CLIENTE").description("Cliente registrado").isActive(true).build());
            System.out.println("✅ Roles created.");
        }

        // --- RESET/CREATE ADMIN USERS ---
        resetUser("admin1@regalos.com", "admin123", "Admin", "Uno");
        resetUser("admin2@regalos.com", "admin123", "Admin", "Dos");

        // --- SEED SETTINGS ---
        if (settingRepository.count() == 0) {
            List<StoreSetting> defaultSettings = Arrays.asList(
                StoreSetting.builder().configGroup("GENERAL").configKey("store_name").configValue("GiftMagic").description("Nombre comercial").build(),
                StoreSetting.builder().configGroup("SOCIAL").configKey("social_whatsapp").configValue("573000000000").description("WhatsApp").build()
            );
            settingRepository.saveAll(defaultSettings);
            System.out.println("✅ Default settings seeded.");
        }
        
        System.out.println("🏁 Data Initialization Finished.");
    }

    private void resetUser(String email, String pass, String first, String last) {
        Optional<AppUser> userOpt = userRepository.findByEmail(email);
        
        // Ensure role exists for the user
        Role superAdminRole = roleRepository.findByName("SUPER_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("SUPER_ADMIN").description("Administrador total").isActive(true).build()));
        
        if (userOpt.isPresent()) {
            AppUser user = userOpt.get();
            String hashed = passwordEncoder.encode(pass);
            user.setPasswordHash(hashed);
            user.setIsActive(true);
            user.setRole(superAdminRole);
            userRepository.save(user);
            System.out.println("🔄 User " + email + " password reset successfully to: " + pass);
        } else {
            AppUser newUser = AppUser.builder()
                    .email(email)
                    .passwordHash(passwordEncoder.encode(pass))
                    .firstName(first)
                    .lastName(last)
                    .role(superAdminRole)
                    .isActive(true)
                    .build();
            userRepository.save(newUser);
            System.out.println("✨ User " + email + " created successfully with pass: " + pass);
        }
    }
}
