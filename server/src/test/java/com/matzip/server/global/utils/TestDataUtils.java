package com.matzip.server.global.utils;

import com.matzip.server.domain.user.model.User;
import com.matzip.server.global.utils.Relation.ReviewConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.matzip.server.global.utils.Relation.CommentConfiguration.comment;
import static com.matzip.server.global.utils.Relation.FollowConfiguration.followersOf;
import static com.matzip.server.global.utils.Relation.ReviewConfiguration.review;
import static com.matzip.server.global.utils.Relation.UserConfiguration.user;

public class TestDataUtils {
    public static List<User> testData() {
        return Relation
                .withUsers(
                        user("user-01").level(3),
                        user("user-02").level(16),
                        user("user-03").level(8),
                        user("user-04").level(3),
                        user("user-05").level(5))
                .withReviews(
                        review("review-01").rating(9).by("user-01")
                                .withComments(
                                        comment("comment-01").by("user-02"),
                                        comment("comment-02").by("user-04"),
                                        comment("comment-03").by("user-04"),
                                        comment("comment-04").by("user-01"),
                                        comment("comment-05").by("user-05"),
                                        comment("comment-06").by("user-01"),
                                        comment("comment-07").by("user-02"),
                                        comment("comment-08").by("user-01"),
                                        comment("comment-09").by("user-02"))
                                .likedBy("user-02", "user-05")
                                .scrapedBy("user-02"),
                        review("review-02").rating(4).by("user-01")
                                .likedBy("user-03"),
                        review("review-03").rating(7).by("user-03")
                                .withComments(
                                        comment("comment-10").by("user-04"),
                                        comment("comment-11").by("user-01"),
                                        comment("comment-12").by("user-04"),
                                        comment("comment-13").by("user-01"),
                                        comment("comment-14").by("user-04"))
                                .scrapedBy("user-05"),
                        review("review-04").rating(2).by("user-05")
                                .withComments(
                                        comment("comment-15").by("user-01"),
                                        comment("comment-16").by("user-05"),
                                        comment("comment-17").by("user-01")),
                        review("review-05").rating(1).by("user-05")
                                .likedBy("user-01", "user-02", "user-03", "user-04")
                                .scrapedBy("user-01"),
                        review("review-06").rating(10).by("user-04")
                                .withComments(
                                        comment("comment-18").by("user-01"),
                                        comment("comment-19").by("user-03"),
                                        comment("comment-20").by("user-01"),
                                        comment("comment-21").by("user-05"),
                                        comment("comment-22").by("user-03"),
                                        comment("comment-23").by("user-04"),
                                        comment("comment-24").by("user-03"),
                                        comment("comment-25").by("user-05"),
                                        comment("comment-26").by("user-02"),
                                        comment("comment-27").by("user-05"),
                                        comment("comment-28").by("user-03"))
                                .likedBy("user-01", "user-03"))
                .withFollows(
                        followersOf("user-01").are("user-02", "user-04", "user-05"),
                        followersOf("user-02").are("user-01", "user-03", "user-04"),
                        followersOf("user-03").are("user-01", "user-02"),
                        followersOf("user-04").are("user-02"),
                        followersOf("user-05").are("user-01", "user-02", "user-03", "user-04"))
                .build();
    }

    public static String[] contents = new String[]{
            "맛집 찾았어요!", "분위기 좋은 식당", "뷰가 좋은 카페", "강추 맛집", "그냥 그래요",
            "맛있어요!", "분위기가 좋아요.", "서비스가 훌륭해요.", "재료가 신선해요.", "가격이 저렴해요.",
            "친절한 직원들이 있어요.", "음식이 너무 매워요.", "가족과 함께 가기 좋아요.", "포장이 잘되어있어요.", "남자친구랑 갔어요.",
            "이 식당은 좀 별로예요.", "음식이 너무 짜요.", "청결한 식당이에요.", "빠른 서비스가 좋아요.", "야외 좌석이 있어요.",
            "친구들과 함께 가기 좋아요.", "특별한 날에 가기 좋아요.", "음식이 너무 달아요.", "메뉴가 다양해요.", "평범한 식당이에요.",
            "이 식당은 꼭 가봐야해요!", "창가 자리가 좋아요.", "주차장이 넓어요.", "예약이 가능해요.", "데이트하기 좋아요.",
            "식당이 깔끔해요.", "재방문 의사가 있어요.", "좀 시끄러워요.", "음식이 빨리 나와요.", "식당이 좀 좁아요.",
            "분위기가 아늑해요.", "주문이 잘못 나왔어요.", "음식이 따뜻해요.", "디저트가 맛있어요.", "커피가 맛있어요.",
            "식당이 작아요.", "조용한 분위기가 좋아요.", "음식이 차가워요.", "별로 추천하지 않아요.", "재료가 좀 부실해요.",
            "식당이 너무 비싸요.", "주방이 열려있어요.", "화장실이 깨끗해요.", "서비스가 좀 느려요.", "강력 추천해요!",
            "야채가 신선해요.", "매장이 넓어요.", "직원들이 친절해요.", "이곳에서 브런치를 즐겨요.", "바쁜 시간대는 피하세요.",
            "식당이 좀 아쉬워요.", "음식이 너무 짭짤해요.", "맥주가 맛있어요.", "샐러드가 아주 맛있어요.",
            "와인 목록이 좋아요.", "피자가 맛있어요.", "뷔페가 훌륭해요.", "커리가 매력적이에요.", "냄새가 좀 나요.",
            "주방이 깨끗해요.", "햄버거가 맛있어요.", "포차 분위기가 좋아요.", "빵이 부드러워요.", "치킨이 바삭해요.",
            "주류가 다양해요.", "와인이 훌륭해요.", "디저트 메뉴가 풍부해요.", "스테이크가 부드러워요.", "칵테일이 훌륭해요.",
            "매장 인테리어가 멋져요.", "테라스가 아름다워요.", "케이크가 달콤해요.", "샌드위치가 아주 맛있어요.", "파스타가 알맞게 익었어요.",
            "해산물이 신선해요.", "주인이 친절해요.", "바닷가 전망이 좋아요.", "음악이 취향에 맞아요.", "낮에 방문하기 좋아요.",
            "야간 분위기가 좋아요.", "바에서 마실만한 곳이에요.", "다양한 음료가 있어요.", "라이브 공연이 있어요.", "종업원이 전문적이에요.",
            "차 종류가 다양해요.", "초밥이 아주 맛있어요.", "비건 메뉴가 있어요.", "유기농 재료를 사용해요.", "테이크아웃이 가능해요.",
            "테마가 독특해요.", "아이들과 가기 좋아요.", "주말에 방문하기 좋아요.", "재즈 음악이 좋아요.", "지역 특산물을 사용해요.",
            "분위기가 아늑해요.", "나무 인테리어가 독특해요.", "놀이터가 있어요.", "친환경 식당이에요.", "차가운 국수가 맛있어요.",
            "김치찌개가 정감이 가요.", "라멘이 일품이에요.", "달고나 커피를 추천해요.", "디저트 피자가 인상적이에요.", "모밀국수가 시원해요.",
            "고기가 부드러워요.", "샐러드가 신선해요.", "마라탕이 얼얼해요.", "마카롱이 예쁘게 장식되어 있어요.", "크로와상이 바삭해요.",
            "야채가 푸짐해요.", "오믈렛이 부드러워요.", "한식이 정갈해요.", "멕시코 음식이 매콤해요.", "인도 음식이 향긋해요.",
            "튀김이 바삭해요.", "죽이 시원해요.", "떡볶이가 매콤해요.", "새우튀김이 맛있어요.", "두부가 부드러워요.",
            "갈비탕이 시원해요.", "닭발이 매콤해요.", "족발이 쫄깃해요.", "생선구이가 맛있어요.", "갈비찜이 부드러워요.",
            "빙수가 시원해요.", "타코가 알싸해요.", "물회가 시원해요.", "닭갈비가 맛있어요.", "동태찌개가 얼큰해요.",
            "곰탕이 깊은 맛이에요.", "비빔밥이 알록달록해요.", "만두가 꽉 차 있어요.", "떡국이 푸짐해요.", "육개장이 얼얼해요.",
            "돈까스가 촉촉해요.", "칼국수가 시원해요.", "수제비가 쫄깃해요.", "순대국이 얼큰해요.", "부대찌개가 푸짐해요."
    };

    public static List<User> searchTestData() {
        Random random = new Random();
        random.setSeed(99);

        List<User> users = new ArrayList<>();
        List<ReviewConfiguration> reviews = new ArrayList<>();

        int phraseIndex = 0;

        for (int i = 1; i <= 50; i++) {
            String username = "user-" + i;
            users.add(user(username).level(i));

            int numReviews = random.nextInt(1, 4);
            for (int j = 1; j <= numReviews; j++) {
                String reviewContent = contents[phraseIndex++];
                int rating = random.nextInt(0, 5);
                String reviewId = "review-" + username + "-" + j;
                ReviewConfiguration review = review(reviewContent).rating(rating).by(username);

                int numComments = random.nextInt(0, 3);
                for (int k = 1; k <= numComments; k++) {
                    String commenter = "user-" + random.nextInt(1, 50);
                    review.withComments(comment("comment-" + reviewId + "-" + k).by(commenter));
                }

                int numLikes = random.nextInt(0, 10);
                String[] likes = new String[numLikes];
                for (int l = 0; l < numLikes; l++) {
                    likes[l] = "user-" + random.nextInt(1, 50);
                }
                review.likedBy(likes);

                reviews.add(review);
            }
        }

        return new Relation(users)
                .withReviews(reviews.toArray(new ReviewConfiguration[0]))
                .build();
    }
}
