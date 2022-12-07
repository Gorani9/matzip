package com.matzip.server.domain.review;

import com.matzip.server.domain.review.repository.ReviewRepository;
import com.matzip.server.global.config.TestQueryDslConfig;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(TestQueryDslConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Tag("RepositoryTest")
public class ReviewRepositoryTest {
    @Autowired
    private ReviewRepository reviewRepository;
}
