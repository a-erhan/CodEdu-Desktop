package com.codedu;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Component
@ConditionalOnProperty(name = "app.db.run-on-startup-fix", havingValue = "true")
public class DatabaseFix {
    private final DataSource dataSource;

    public DatabaseFix(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void fix() {
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.execute("ALTER TABLE users ALTER COLUMN token_balance DROP NOT NULL");
            System.out.println("SUCCESSFULLY DROPPED NOT NULL CONSTRAINT ON token_balance!");
            int updated = s.executeUpdate("UPDATE users SET is_active = true WHERE is_active = false");
            System.out.println("Restored " + updated + " old user accounts that had is_active=false.");
        } catch (Exception e) {
            System.out.println("Failed to drop NOT NULL constraint: " + e.getMessage());
        }
    }
}
