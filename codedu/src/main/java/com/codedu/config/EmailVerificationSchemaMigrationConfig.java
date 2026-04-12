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
public class EmailVerificationSchemaMigrationConfig {

    @Bean
    public static BeanPostProcessor emailVerificationColumnsMigration() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (!"dataSource".equals(beanName) || !(bean instanceof DataSource ds)) {
                    return bean;
                }
                try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
                    s.execute(
                            "ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified BOOLEAN NOT NULL DEFAULT TRUE");
                    s.execute(
                            "ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verification_token VARCHAR(64)");
                    s.execute(
                            "ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verification_expires_at TIMESTAMPTZ");
                    s.execute(
                            "CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email_verification_token "
                                    + "ON users (email_verification_token) "
                                    + "WHERE email_verification_token IS NOT NULL");
                } catch (SQLException e) {
                    throw new BeanCreationException("Could not ensure users email verification columns exist", e);
                }
                return bean;
            }
        };
    }
}
