package com.codedu.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Configuration
public class UserGameStateSchemaMigrationConfig {

    private static final Logger log = LoggerFactory.getLogger(UserGameStateSchemaMigrationConfig.class);

    @Bean
    public static BeanPostProcessor userGameStateDoubleXpColumnMigration() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (!"dataSource".equals(beanName) || !(bean instanceof DataSource ds)) {
                    return bean;
                }
                try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
                    s.execute(
                            "ALTER TABLE user_game_states ADD COLUMN IF NOT EXISTS double_xp_active_until TIMESTAMP NULL");
                } catch (SQLException e) {
                    throw new BeanCreationException(
                            "Could not ensure user_game_states.double_xp_active_until exists", e);
                }
                return bean;
            }
        };
    }

    @Bean
    public static BeanPostProcessor userGameStateUserIdColumnMigration() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (!"dataSource".equals(beanName) || !(bean instanceof DataSource ds)) {
                    return bean;
                }
                try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
                    s.execute("ALTER TABLE user_game_states ADD COLUMN IF NOT EXISTS user_id INTEGER");
                } catch (SQLException e) {
                    log.warn("Could not ensure user_game_states.user_id exists: {}", e.getMessage());
                }
                try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
                    int n = s.executeUpdate(
                            "UPDATE user_game_states AS ugs SET user_id = u.id FROM users u "
                                    + "WHERE u.game_state_id = ugs.id AND ugs.user_id IS NULL");
                    if (n > 0) {
                        log.info("Backfilled user_id on {} user_game_states row(s).", n);
                    }
                } catch (SQLException e) {
                    log.warn("user_game_states.user_id backfill skipped: {}", e.getMessage());
                }
                try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
                    s.execute("ALTER TABLE users ALTER COLUMN game_state_id DROP NOT NULL");
                } catch (SQLException e) {
                    log.warn("Could not relax users.game_state_id NOT NULL (may be absent or already nullable): {}",
                            e.getMessage());
                }
                return bean;
            }
        };
    }
}
