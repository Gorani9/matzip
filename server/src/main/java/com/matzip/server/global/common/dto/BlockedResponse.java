package com.matzip.server.global.common.dto;

import lombok.Getter;

@Getter
public class BlockedResponse extends BaseResponse {
    private static final BlockedResponse blockedUserResponse = new BlockedResponse("This user is blocked");
    private static final BlockedResponse blockedReviewResponse = new BlockedResponse("This review is blocked");
    private static final BlockedResponse blockedCommentResponse = new BlockedResponse("This comment is blocked");
    private final String description;
    BlockedResponse(String description) {
        super(false);
        this.description = description;
    }

    public static BlockedResponse ofBlockedUser() {
        return blockedUserResponse;
    }
    public static BlockedResponse ofBlockedReview() {
        return blockedReviewResponse;
    }
    public static BlockedResponse ofBlockedComment() {
        return blockedCommentResponse;
    }
}