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

    /**
     * Soru tipine göre doğru değerlendirme stratejisini seçer.
     */
    public boolean evaluate(Question question, String userAnswer) {

        // 1. Eğer soru "Kod Yazma" sorusu ise (Senin yolladığın class)
        // (Java 16+ Pattern Matching özelliğini kullanıyoruz)
        if (question instanceof CodeImplementationQuestion codeQuestion) {

            List<TestCase> testCases = codeQuestion.getTestCases();
            if (testCases == null || testCases.isEmpty()) {
                return false;
            }

            // Test senaryolarını Judge0 üzerinden geçir
            for (TestCase tc : testCases) {
                // Kod motoruna kullanıcının yazdığı kodu ve test case'in gizli girdisini yolluyoruz
                String actualOutput = codeExecutionService.executeJavaCode(userAnswer, tc.getInput());

                // Gelen çıktı (actualOutput), beklenen çıktıyla (expectedOutput) eşleşmiyor mu?
                if (actualOutput == null || !actualOutput.trim().equals(tc.getExpectedOutput().trim())) {
                    System.out.println("Test patladı! Beklenen: " + tc.getExpectedOutput() + " | Gelen: " + actualOutput);
                    return false;
                }
            }
            return true; // Tüm kod testlerini geçti!
        }

        // 2. Eğer soru Diğer Tiplerden biriyse (Çoktan Seçmeli, Boşluk Doldurma vs.)
        // OOP'nin gücünü kullan: Modelin kendi içindeki validateAnswer metodunu çağır!
        else {
            return question.validateAnswer(userAnswer);
        }
    }
}