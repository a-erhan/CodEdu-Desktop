package com.codedu.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Drops legacy UNIQUE(user_inventory_id) on {@code inventory_items}. That uniqueness
 * (from an old @OneToOne mapping) allows only one line per inventory; many rows must share
 * the same {@code user_inventory_id}. Hibernate {@code ddl-auto=update} does not remove it.
 * <p>
 * Implemented as {@link ApplicationRunner}: with {@code spring.main.lazy-initialization=true},
 * beans that are never injected are not created, so {@code @PostConstruct} never ran. Spring Boot
 * still eagerly initializes {@link ApplicationRunner} beans so this always executes after the
 * context is up (and after Hibernate schema update).
 */
@Component
@Order(0)
@ConditionalOnBean(DataSource.class)
public class InventoryConstraintMigrationRunner implements ApplicationRunner {

    /**
     * Drop any single-column UNIQUE constraint on {@code user_inventory_id}, then ensure a
     * non-unique index exists for lookups.
     */
    private static final String SQL = """
            DO $$
            DECLARE
              r RECORD;
            BEGIN
              IF NOT EXISTS (
                  SELECT 1 FROM pg_catalog.pg_class rel
                  JOIN pg_catalog.pg_namespace nsp ON nsp.oid = rel.relnamespace
                  WHERE rel.relname = 'inventory_items'
                    AND nsp.nspname = ANY (current_schemas(true))
              ) THEN
                RETURN;
              END IF;

              FOR r IN (
                  SELECT c.conname
                  FROM pg_catalog.pg_constraint c
                  JOIN pg_catalog.pg_class t ON c.conrelid = t.oid
                  JOIN pg_catalog.pg_namespace n ON n.oid = t.relnamespace
                  WHERE t.relname = 'inventory_items'
                    AND n.nspname = ANY (current_schemas(true))
                    AND c.contype = 'u'
                    AND pg_catalog.array_length(c.conkey, 1) = 1
                    AND EXISTS (
                        SELECT 1
                        FROM pg_catalog.pg_attribute a
                        WHERE a.attrelid = c.conrelid
                          AND a.attnum = c.conkey[1]
                          AND a.attname = 'user_inventory_id'
                    )
              ) LOOP
                  EXECUTE format('ALTER TABLE inventory_items DROP CONSTRAINT IF EXISTS %I', r.conname);
              END LOOP;

              CREATE INDEX IF NOT EXISTS idx_inventory_items_user_inventory_id
                  ON inventory_items (user_inventory_id);
            END $$;
            """;

    private final DataSource dataSource;

    public InventoryConstraintMigrationRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection c = dataSource.getConnection()) {
            c.setAutoCommit(true);
            try (Statement st = c.createStatement()) {
                st.execute("ALTER TABLE inventory_items DROP CONSTRAINT IF EXISTS inventory_items_user_inventory_id_key");
                st.execute(SQL);
            }
        }
        System.out.println(">>> [InventoryMigration] Dropped legacy UNIQUE(user_inventory_id) if present; index ensured.");
    }
}
