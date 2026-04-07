package com.codedu.services.implementations;

import com.codedu.dtos.learning.ChapterDTO;
import com.codedu.dtos.learning.QuestionDTO;
import com.codedu.models.learning.Chapter;
import com.codedu.models.learning.CodeImplementationQuestion;
import com.codedu.models.learning.Question;
import com.codedu.services.interfaces.ChapterService;
import com.codedu.repositories.interfaces.ChapterRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChapterServiceImpl implements ChapterService {

    private final ChapterRepository chapterRepository;

    @Autowired
    public ChapterServiceImpl(ChapterRepository chapterRepository) {
        this.chapterRepository = chapterRepository;
    }

    @Override
    public Optional<ChapterDTO> getChapterById(int id) {
        return chapterRepository.findById(id).map(this::toDTO);
    }

    @Override
    public List<ChapterDTO> getAllChapters() {
        return chapterRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Chapter> getChapterWithQuestions(Long id) {
        return chapterRepository.findByIdWithQuestions(id);
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<ChapterDTO> getChapterDtoWithQuestions(Long id) {
        return chapterRepository.findByIdWithQuestions(id).map(this::toFullDTO);
    }

    // --- MAPPING METHODS ---

    // Basic DTO for path viewing (no heavy questions loaded)
    private ChapterDTO toDTO(Chapter ch) {
        return ChapterDTO.builder()
                .id(ch.getId())
                .title(ch.getTitle())
                .description(ch.getDescription())
                .iconEmoji(ch.getIconEmoji())
                .iconImage(ch.getIconImage())
                .difficulty(ch.getDifficulty())
                .totalLessons(ch.getTotalLessons())
                .xpReward(ch.getXpReward())
                .tokenReward(ch.getTokenReward())
                .orderIndex(ch.getOrderIndex())
                .topicName(ch.getTopicName())
                .build();
    }

    // Full DTO including Questions and Learn Text for the Chapter View
    private ChapterDTO toFullDTO(Chapter ch) {
        String learnText = null;
        List<QuestionDTO> questionDTOs = new ArrayList<>();

        if (ch.getContent() != null) {
            learnText = ch.getContent().getLearnText();
            if (ch.getContent().getQuestions() != null) {
                questionDTOs = ch.getContent().getQuestions().stream()
                        .map(this::toQuestionDTO)
                        .collect(Collectors.toList());
            }
        }

        return ChapterDTO.builder()
                .id(ch.getId())
                .title(ch.getTitle())
                .description(ch.getDescription())
                .iconEmoji(ch.getIconEmoji())
                .iconImage(ch.getIconImage())
                .learnText(learnText)
                .difficulty(ch.getDifficulty())
                .totalLessons(ch.getTotalLessons())
                .xpReward(ch.getXpReward())
                .tokenReward(ch.getTokenReward())
                .orderIndex(ch.getOrderIndex())
                .topicName(ch.getTopicName())
                .questions(questionDTOs)
                .build();
    }

    // Maps a Question Entity to a QuestionDTO
    private QuestionDTO toQuestionDTO(Question q) {
        int rXp = (q.getReward() != null) ? q.getReward().getXp() : 0;
        int rTok = (q.getReward() != null) ? q.getReward().getToken() : 0;

        String boilerplate = "";
        // Specific check to pull code for coding questions
        if (q instanceof CodeImplementationQuestion cq) {
            boilerplate = cq.getBoilerplateCode();
        }

        return QuestionDTO.builder()
                .id(q.getId())
                .title(q.getTitle())
                .content(q.getContent())
                .hint(q.getHint())
                .solution(q.getSolution())
                .questionType(q.getQuestionType())
                .questionDifficulty(q.getQuestionDifficulty())
                .rewardXp(rXp)
                .rewardToken(rTok)
                .boilerplateCode(boilerplate)
                .build();
    }
}