package com.matzip.server.domain.search.repository;

import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.review.repository.ReviewRepository;
import com.matzip.server.domain.search.dto.SearchDto.ReviewSearch;
import com.matzip.server.domain.search.model.ReviewDocument;
import com.matzip.server.domain.user.repository.UserRepository;
import com.matzip.server.global.utils.TestDataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

/**
 * Before running this test, you need to run the following command. <br>
 * $ docker-compose -f src/test/resources/elastic/elk.yaml up -d
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("SearchService 리뷰 검색 성능 테스트")
class ReviewSearchPerformanceTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private ReviewSearchRepository reviewSearchRepository;
    @Autowired
    private ReviewSearchQueryRepository reviewSearchQueryRepository;

    @BeforeEach
    public void setUp() {
        userRepository.saveAllAndFlush(TestDataUtils.searchTestData());
        reviewRepository.flush();
        reviewSearchRepository.deleteAll();
        reviewRepository.findAll().forEach(review -> reviewSearchRepository.save(new ReviewDocument(review)));
    }

    /** <h1>결과</h1>
     * <h2>1st Try</h2>
     * <h3>Using Like Syntax     : 128ms</h3>
     * <h3>Using FullText Syntax : 9ms</h3>
     * <h3>Using Elasticsearch   : 94ms</h3>
     * <h2>2nd Try</h2>
     * <h3>Using Like Syntax     : 102ms</h3>
     * <h3>Using FullText Syntax : 16ms</h3>
     * <h3>Using Elasticsearch   : 59ms</h3>
     * <h2>3rd Try</h2>
     * <h3>Using Like Syntax     : 250ms</h3>
     * <h3>Using FullText Syntax : 23ms</h3>
     * <h3>Using Elasticsearch   : 143ms</h3>
     */
    @Test
    @DisplayName("리뷰 검색 성능 테스트")
    void searchReviewPerformanceTest() {
        long start, end;
        ReviewSearch request = new ReviewSearch("맛있", 0, 20, null, false);

        // LIKE Syntax
        System.out.println("========= LIKE SYNTAX =========");
        start = System.currentTimeMillis();
        Slice<Review> likeReviews = reviewRepository.searchReviewsByKeyword(request);
        end = System.currentTimeMillis();
        long likeSyntaxTime = end - start;
        int likeSyntaxRow = likeReviews.getNumberOfElements();

        // FullText Syntax
        System.out.println("========= FULL TEXT SYNTAX =========");
        start = System.currentTimeMillis();
        Slice<Review> fullTextReviews = reviewRepository.searchReviewsByKeywordUsingFullText(request);
        end = System.currentTimeMillis();
        long fullTextSyntaxTime = end - start;
        int fullTextSyntaxRow = fullTextReviews.getNumberOfElements();

        // Elasticsearch
        System.out.println("========= ELASTICSEARCH =========");
        start = System.currentTimeMillis();
        List<ReviewDocument> byContent = reviewSearchQueryRepository.findByContent(request);
        end = System.currentTimeMillis();
        long elasticsearchTime = end - start;
        int elasticsearchRow = byContent.size();

        // Print Result
        System.out.println("========= LIKE SYNTAX RESULT =========");
        likeReviews.getContent().forEach(r -> System.out.println(r.getContent()));
        System.out.println("========= FULL TEXT SYNTAX RESULT =========");
        fullTextReviews.getContent().forEach(r -> System.out.println(r.getContent()));
        System.out.println("========= ELASTICSEARCH RESULT =========");
        byContent.forEach(r -> System.out.println(r.getContent()));

        System.out.printf("TESTED WITH %d USERS, %d REVIEWS%n", userRepository.count(), reviewRepository.count());
        System.out.printf(
                """
                     Like      : %dms, %d rows
                     Full Text : %dms, %d rows
                     Elasticsearch : %dms, %d rows
                     """,
                likeSyntaxTime, likeSyntaxRow,
                fullTextSyntaxTime, fullTextSyntaxRow,
                elasticsearchTime, elasticsearchRow);
    }

}