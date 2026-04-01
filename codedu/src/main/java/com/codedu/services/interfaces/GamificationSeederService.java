package com.codedu.services.interfaces;

import org.springframework.boot.CommandLineRunner;

public interface GamificationSeederService extends CommandLineRunner {

    void seedAchievements();
}
