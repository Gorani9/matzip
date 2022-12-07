package com.matzip.server.global.common.dto;

import lombok.Getter;

@Getter
public class DeletedResponse extends BaseResponse {
    private static final DeletedResponse blockedUserResponse = new DeletedResponse("This user is blocked");
    private static final DeletedResponse blockedReviewResponse = new DeletedResponse("This review is blocked");
    private static final DeletedResponse blockedCommentResponse = new DeletedResponse("This comment is blocked");
    private final String description;
    DeletedResponse(String description) {
        super(false);
        this.description = description;
    }

    public static DeletedResponse ofBlockedUser() {
        return blockedUserResponse;
    }
    public static DeletedResponse ofBlockedReview() {
        return blockedReviewResponse;
    }
    public static DeletedResponse ofBlockedComment() {
        return blockedCommentResponse;
    }
}
