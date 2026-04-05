-- Align legacy DBs with UserGameState.doubleXpActiveUntil (Hibernate maps to double_xp_active_until).
ALTER TABLE user_game_states ADD COLUMN IF NOT EXISTS double_xp_active_until TIMESTAMP NULL;

-- Forum thread replies (matches com.codedu.models.social.Reply)
CREATE TABLE IF NOT EXISTS replies (
    id SERIAL PRIMARY KEY,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    title VARCHAR(255),
    content TEXT,
    author_id INTEGER REFERENCES users (id),
    forum_post_id INTEGER REFERENCES forum_posts (id)
);
