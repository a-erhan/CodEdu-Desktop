package com.codedu.config;

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
}
