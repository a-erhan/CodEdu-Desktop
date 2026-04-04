package com.codedu.services.implementations;

import com.codedu.dtos.learning.QuestionDTO;
import com.codedu.models.learning.CodeImplementationQuestion;
import com.codedu.services.interfaces.CodeExecutionService;
import com.codedu.services.interfaces.QuestionEvaluationService;
import com.codedu.models.learning.Question;
import com.codedu.models.learning.TestCase;
import com.codedu.repositories.interfaces.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuestionEvaluationServiceImpl implements QuestionEvaluationService {

    private final CodeExecutionService codeExecutionService;
    private final QuestionRepository questionRepository;

    public QuestionEvaluationServiceImpl(CodeExecutionService codeExecutionService, QuestionRepository questionRepository) {
        this.codeExecutionService = codeExecutionService;
        this.questionRepository = questionRepository;
    }

    // 🚀 METHOD 1: Handles the new QuestionDTO (Used by Learning Path)
    @Override
    @Transactional
    public boolean evaluate(QuestionDTO questionDto, String userAnswer) {
        if (questionDto == null) return false;

        // Fetch the full entity from the DB using the DTO's ID
        Question loadedQuestion = questionRepository.findById(questionDto.id()).orElse(null);

        // Pass it to the main evaluation logic below
        return evaluate(loadedQuestion, userAnswer);
    }

    // 🚀 METHOD 2: Handles the raw Entity (Used by Matchmaking)
    @Override
    @Transactional
    public boolean evaluate(Question question, String userAnswer) {
        if (question == null) return false;

        // Ensure it's fully loaded to avoid Hibernate LazyInitializationExceptions
        Question loadedQuestion = questionRepository.findById(question.getId()).orElse(question);

        if (loadedQuestion instanceof CodeImplementationQuestion codeQuestion) {

            List<TestCase> testCases = codeQuestion.getTestCases();
            if (testCases == null || testCases.isEmpty()) {
                return false;
            }

            for (TestCase tc : testCases) {
                String actualOutput = codeExecutionService.executeJavaCode(userAnswer, tc.getInput());

                if (actualOutput != null && (actualOutput.startsWith("EXECUTION ERROR")
                        || actualOutput.startsWith("CONNECTION ERROR"))) {
                    return false;
                }

                String safeActual = (actualOutput != null) ? actualOutput.trim().replace("\r\n", "\n") : "";
                String safeExpected = (tc.getExpectedOutput() != null)
                        ? tc.getExpectedOutput().trim().replace("\r\n", "\n")
                        : "";

                if (!safeActual.equals(safeExpected)) {
                    return false;
                }
            }
            return true;
        } else {
            return loadedQuestion.validateAnswer(userAnswer);
        }
    }
}