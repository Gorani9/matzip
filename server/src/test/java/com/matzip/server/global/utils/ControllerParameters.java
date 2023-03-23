package com.matzip.server.global.utils;

import org.springframework.mock.web.MockMultipartFile;

import java.util.Arrays;
import java.util.List;

public class ControllerParameters {
    public static class Common {
        public static String[] validNullableNotBlankMax30 = new String[]{
                null,
                "maxLengthOfUsernameIs.30.chars",
                "!".repeat(30),
                "underscore_username",
                "1111",
                "using_dot.is_allowed",
                ".start_with_dot_is_allowed",
                "specialForbidden!",
                "space Forbidden",
                "end_with_dot_is_not_allowed.",
                "double_dot.._is_not_allowed",
                "admin",
                "Admin",
                "ADMIN",
        };
        public static String[] invalidNullableNotBlankMax30 = new String[]{
                "", "   ", "veryLongUsernameThatIsOver30Characters", "!".repeat(31)
        };
        public static String[] validMax50 = new String[]{"", "  ", null, "a", "a12!@_.!@#$%$", "!".repeat(50)};
        public static String[] invalidMax50 = new String[]{"!".repeat(51)};
        public static String[] validNotBlankMax100 = new String[]{"a", "a12!@_.!@#$%$", "!".repeat(100)};
        public static String[] invalidNotBlankMax100 = new String[]{"", "  ", null, "!".repeat(101)};
        public static String[] validIds = new String[]{
                "1", "2", "3", "100",
        };
        public static String[] invalidIds = new String[]{
                "-1", "0", "nonInt"
        };
        public static String[] validPages = new String[]{
                "0", "1", "2", "100", "", "   ", null
        };
        public static String[] invalidPages = new String[]{
                "-1", "nonInt"
        };
        public static String[] validSizes = new String[]{
                "1", "2", "99", "100", "", "   ", null
        };
        public static String[] invalidSizes = new String[]{
                "-1", "0", "nonInt"
        };
        public static String[] validAsc = new String[]{
                null, "true", "TRUE", "True", "tRue", "tRUe", "false", "FALSE", "False", "faLse", "0", "1",
        };
        public static String[] invalidAsc = new String[]{
                "null", "bool", "Boolean",
        };
        public static String[] validUserProperties = new String[]{
                null, "username", "USERNAME", "level", "MATZIP_LEVEL", "followers", "NUMBER_OF_FOLLOWERS",
        };
        public static String[] invalidUserProperties = new String[]{
                "null", "user", "name", "matzip-level", "MATZIP-LEVEL", "NUMBER-OF-FOLLOWERS", "follower", "follow"
        };
        public static String[] validReviewProperties = new String[]{
                "REVIEWER_USERNAME", "username",
                "REVIEWER_MATZIP_LEVEL", "level",
                "REVIEWER_NUMBER_OF_FOLLOWERS", "followers",
                "NUMBER_OF_HEARTS", "hearts",
                "NUMBER_OF_SCRAPS", "scraps",
                "NUMBER_OF_COMMENTS", "comments",
                "RATING", "rating"
        };
        public static String[] invalidReviewProperties = new String[]{
                "reviewer-username", "reviewer", "heart", "follow", "user", "heart", "scrap", "comment"
        };
    }

    public static class Signup {
        public static String[] validUsernames = new String[]{
                "maxLengthOfUsernameIs.30.chars",
                "underscore_username",
                "1111",
                "using_dot.is_allowed",
                ".start_with_dot_is_allowed"
        };
        public static String[] invalidUsernames = new String[]{
                null,
                "",
                "   ",
                "maxLengthOfUsername.Is.30.chars",
                "specialForbidden!",
                "space Forbidden",
                "end_with_dot_is_not_allowed.",
                "double_dot.._is_not_allowed",
                "admin",
                "Admin",
                "ADMIN",
        };
        public static String[] validPasswords = new String[]{
                "simplePassword1!",
                "maxLengthOfPasswordIs30Chars!!",
                "no_upper_case1!",
                "NO_LOWER_CASE1!",
        };
        public static String[] invalidPasswords = new String[]{
                null,
                "",
                "   ",
                "short",
                "veryVeryLongPasswordThatIsOver30Characters!!!!!!!!!",
                "noNumeric!",
                "noSpecial1",
                "1111111!!!!!1111!!",
                "dot.is.not.allowed",
        };
    }

    public static class Login {
        public static String[] validUsernames = new String[]{
                "maxLengthOfUsernameIs.30.chars",
                "underscore_username",
                "1111",
                "using_dot.is_allowed",
                ".start_with_dot_is_allowed",
                "specialForbidden!",
                "space Forbidden",
                "end_with_dot_is_not_allowed.",
                "double_dot.._is_not_allowed",
                "admin",
                "Admin",
                "ADMIN",
        };
        public static String[] invalidUsernames = new String[]{
                null, "", "   ", "veryLongUsernameThatIsOver30Characters"
        };
        public static String[] validPasswords = new String[]{
                "simplePassword1!",
                "maxLengthOfPasswordIs30Chars!!",
                "short",
                "noNumeric!",
                "noSpecial1",
                "no_upper_case1!",
                "NO_LOWER_CASE1!"
        };
        public static String[] invalidPasswords = new String[]{
                null, "", "   ", "veryVeryLongPasswordThatIsOver30Characters!!!!!!!!!"
        };
    }

    public static class SearchUser {
        public static String[] validUsernames = new String[]{
                "maxLengthOfUsernameIs.30.chars",
                "underscore_username",
                "1111",
                "using_dot.is_allowed",
                ".start_with_dot_is_allowed"
        };
        public static String[] invalidUsernames = new String[]{
                "   ",
                "maxLengthOfUsername.Is.30.chars",
                "specialForbidden!",
                "space Forbidden",
                "end_with_dot_is_not_allowed.",
                "double_dot.._is_not_allowed",
                "admin",
                "Admin",
                "ADMIN",
                };
    }

    private static final MockMultipartFile multipartFile = new MockMultipartFile("images", new byte[0]);
    private static final List<MockMultipartFile> multipartFilesOfSize10 = List.of(
            multipartFile, multipartFile, multipartFile, multipartFile, multipartFile,
            multipartFile, multipartFile, multipartFile, multipartFile, multipartFile
    );
    private static final List<MockMultipartFile> multipartFilesOfSize11 = List.of(
            multipartFile, multipartFile, multipartFile, multipartFile, multipartFile,
            multipartFile, multipartFile, multipartFile, multipartFile, multipartFile,
            multipartFile
    );

    public static class PostReview {
        public static String[] validContents = new String[]{"a", "a12!@_.!@#$%$", "!".repeat(3000)};
        public static String[] invalidContents = new String[]{"", "  ", null, "!".repeat(3001)};
        public static Object[] validImages = new Object[]{List.of(multipartFile), multipartFilesOfSize10};
        public static Object[] invalidImages = new Object[]{multipartFilesOfSize11, List.of()};
        public static Object[] validRatings = new Object[]{"0","1","2","3","4","5"};
        public static Object[] invalidRatings = new Object[]{"-1","11","nonInt"};
        public static String[] validRestaurants = new String[]{"somewhere"};
        public static String[] invalidRestaurants = new String[]{"", "  ", null};
    }

    public static class PatchReview {
        public static String[] validContents = new String[]{null, "a", "a12!@_.!@#$%$", "!".repeat(3000)};
        public static String[] invalidContents = new String[]{"", "  ", "!".repeat(3001)};
        public static Object[] validImages = new Object[]{List.of(multipartFile), List.of()};
        public static Object[] invalidImages = new Object[]{multipartFilesOfSize11};
        public static Object[] validOldUrls = new Object[]{List.of("http://image-source.url")};
        public static Object[] invalidOldUrls = new Object[]{
                List.of("http://image-source.url", ""), List.of(" "), List.of("domain.com"),
                Arrays.asList(new String[]{"null", null})
        };
    }

    public static class PatchMe {
        public static MockMultipartFile[] validImage = new MockMultipartFile[]{multipartFile, null};
    }
}
