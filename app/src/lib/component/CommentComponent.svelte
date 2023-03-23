<script lang="ts">

    import {Comment} from "../dto/Comment";
    import {Review} from "../dto/Review";
    import {API} from "../api";
    import {dialogs} from "svelte-dialogs";
    import UserMarkerComponent from "./UserMarkerComponent.svelte";
    import TimeMarkerComponent from "./TimeMarkerComponent.svelte";
    import CommentEditComponent from "$lib/component/CommentEditComponent.svelte";

    export let review: Review;
    export let comment: Comment;

    let editing = false;

    const toggleEdit = () => {
        editing = !editing;
    }

    const handleDelete = async () => {
        const confirmed = await dialogs.confirm({
            title: '정말로 삭제하시겠습니까?',
            text: "삭제하면 복구할 수 없습니다.",
            confirmButtonText: '삭제',
            declineButtonText: '취소',
        });

        if (!confirmed) return;

        const response = await API.deleteComment(comment.id);
        const json = await response.json();

        if (response.ok) {
            review = Review.fromJson(json);
        } else {
            await dialogs.error("댓글 삭제에 실패했습니다. 다시 시도해주세요.");
        }
    }

</script>


<main>
    <div class="comment-meta-info">
        <UserMarkerComponent user={comment.user} markerSize={30} />
        <TimeMarkerComponent createdAt={comment.createdAt} modifiedAt={comment.modifiedAt} fontSize={15}/>
        {#if !editing && comment.user.isMe}
            <button class="edit-button" on:click={toggleEdit}>
                <img src="/icons/edit.png" alt="edit" />
            </button>
            <button class="delete-button" on:click={handleDelete}>
                <img src="/icons/delete.png" alt="delete" />
            </button>
        {/if}
    </div>
    {#if editing}
        <CommentEditComponent review={review} comment={comment} isEdit={true} bind:editing={editing} on:cancel={toggleEdit} />
    {:else}
        <div class="comment-content">{comment.content}</div>
    {/if}
</main>

<style>

    main {
        display: flex;
        flex-direction: column;
        align-items: flex-start;
        margin: 3px 0;
        width: 100%;
    }

    /* top and bottom borders */
    main:not(:first-child) {
        padding-top: 5px;
        border-top: 1px solid #eaeaea;
    }

    .comment-meta-info {
        display: flex;
        align-items: center;
        margin-bottom: 5px;
    }

    .comment-content {
        padding-left: 0;
        margin-right: 5px;
        font-family: "D2Coding", sans-serif;
        white-space: pre-wrap;
        word-break: break-word;
        text-align: left;
    }

    .edit-button {
        border: none;
        background-color: transparent;
        cursor: pointer;
        margin-left: 10px;
        margin-bottom: 5px;
    }

    .delete-button {
        border: none;
        background-color: transparent;
        cursor: pointer;
        margin-bottom: 5px;
    }

    .delete-button:hover {
        opacity: 0.5;
    }

    .edit-button:hover {
        opacity: 0.5;
    }

    .edit-button > img {
        width: 13px;
        height: 13px;
    }

    .delete-button > img {
        width: 13px;
        height: 13px;
    }

</style>