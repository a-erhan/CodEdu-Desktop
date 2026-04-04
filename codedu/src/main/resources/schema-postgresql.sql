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
