package com.codedu.services;

import com.codedu.models.learning.CodeImplementationQuestion;
import com.codedu.models.learning.Question;
import com.codedu.models.learning.TestCase;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionEvaluationService {

    private final CodeExecutionService codeExecutionService;

    public QuestionEvaluationService(CodeExecutionService codeExecutionService) {
        this.codeExecutionService = codeExecutionService;
    }

    public boolean evaluate(Question question, String userAnswer) {

        if (question instanceof CodeImplementationQuestion codeQuestion) {

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