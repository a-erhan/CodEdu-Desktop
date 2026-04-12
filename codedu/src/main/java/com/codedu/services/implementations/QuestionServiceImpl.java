package com.codedu.services.implementations;

import com.codedu.models.learning.Question;
import com.codedu.services.interfaces.QuestionService;
import com.codedu.repositories.interfaces.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;

    @Autowired
    public QuestionServiceImpl(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public Optional<Question> getQuestionById(int id) {

        return questionRepository.findById(id);
    }

    public void saveQuestion(Question question) {
        questionRepository.save(question);
    }
}