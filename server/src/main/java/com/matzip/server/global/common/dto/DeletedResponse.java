package com.matzip.server.global.common.dto;

import lombok.Getter;

@Getter
public class DeletedResponse extends BaseResponse {
    private static final DeletedResponse deletedUserResponse = new DeletedResponse("This user is deleted");
    private static final DeletedResponse deletedReviewResponse = new DeletedResponse("This review is deleted");
    private static final DeletedResponse deletedCommentResponse = new DeletedResponse("This comment is deleted");
    private final String description;
    DeletedResponse(String description) {
        super(false);
        this.description = description;
    }

    public static DeletedResponse ofDeletedUser() {
        return deletedUserResponse;
    }
    public static DeletedResponse ofDeletedReview() {
        return deletedReviewResponse;
    }
    public static DeletedResponse ofDeletedComment() {
        return deletedCommentResponse;
    }
}
