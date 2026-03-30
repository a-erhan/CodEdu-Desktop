package com.codedu.services.implementations;

import com.codedu.models.learning.Question;
import com.codedu.repositories.interfaces.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;

    @Autowired
    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    // 1. Fetch a question by its Database ID
    public Optional<Question> getQuestionById(int id) {
        // Since your GenericRepository likely implements basic CRUD, this will work perfectly!
        return questionRepository.findById(id);
    }

    // 2. Save or update a question
    public void saveQuestion(Question question) {
        questionRepository.save(question);
    }
}