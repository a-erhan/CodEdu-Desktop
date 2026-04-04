package com.codedu.services.implementations;

import com.codedu.dtos.learning.ChapterDTO;
import com.codedu.models.learning.Chapter;
import com.codedu.services.interfaces.ChapterService;
import com.codedu.repositories.interfaces.ChapterRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public Optional<ChapterDTO> getChapterById(int id) {
        return chapterRepository.findById(id).map(this::toDTO);
    }

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
}