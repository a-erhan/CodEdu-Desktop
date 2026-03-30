package com.codedu.services.interfaces;

import org.springframework.boot.CommandLineRunner;

public interface QuestionSeederService extends CommandLineRunner {

    void generateQuestions();
}
