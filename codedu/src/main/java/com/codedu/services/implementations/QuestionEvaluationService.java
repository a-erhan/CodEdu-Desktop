package com.codedu.services.implementations;

import com.codedu.models.learning.CodeImplementationQuestion;
import com.codedu.models.learning.Question;
import com.codedu.models.learning.TestCase;
import com.codedu.repositories.interfaces.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuestionEvaluationService {

    private final CodeExecutionService codeExecutionService;
    private final QuestionRepository questionRepository;

    public QuestionEvaluationService(CodeExecutionService codeExecutionService, QuestionRepository questionRepository) {
        this.codeExecutionService = codeExecutionService;
        this.questionRepository = questionRepository;
    }

    @Transactional
    public boolean evaluate(Question question, String userAnswer) {
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
        }

        else {
            return question.validateAnswer(userAnswer);
        }
    }
}