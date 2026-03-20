package com.codedu.repositories.implementations;

import com.codedu.models.social.ForumPost;
import com.codedu.repositories.interfaces.ForumPostRepository;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public class ForumPostRepositoryImpl extends GenericRepositoryImpl<ForumPost> implements ForumPostRepository {

    public ForumPostRepositoryImpl() {
        super(ForumPost.class);
    }
    @Override
    public List<ForumPost> findAllMainPosts() {
        return getEntityManager().createQuery(
                        "SELECT p FROM ForumPost p " +
                                "LEFT JOIN FETCH p.author " +
                                "WHERE p.isDeleted = false " +
                                "AND p NOT IN (SELECT r FROM ForumPost parent JOIN parent.replies r) " +
                                "ORDER BY p.id DESC", ForumPost.class)
                .getResultList();
    }

    @Override
    public ForumPost findByIdWithReplies(Integer id) {
        return getEntityManager().createQuery(
                        "SELECT p FROM ForumPost p " +
                                "LEFT JOIN FETCH p.author " +
                                "LEFT JOIN FETCH p.replies r " +
                                "LEFT JOIN FETCH r.author " +
                                "WHERE p.id = :id AND p.isDeleted = false", ForumPost.class)
                .setParameter("id", id)
                .getSingleResult();
    }
}
