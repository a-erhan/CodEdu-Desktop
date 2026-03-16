package com.codedu.repositories.implementations;

import com.codedu.models.TestCase;
import com.codedu.repositories.interfaces.TestCaseRepository;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public class TestCaseRepositoryImpl extends GenericRepositoryImpl<TestCase> implements TestCaseRepository {

    public TestCaseRepositoryImpl() {
        super(TestCase.class);
    }
}
