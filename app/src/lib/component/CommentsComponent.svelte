<script lang="ts">

    import {Review} from "../dto/Review";
    import CommentComponent from "$lib/component/CommentComponent.svelte";
    import CommentModal from "$lib/modal/CommentModal.svelte";
    import {Dialog} from "svelte-dialogs";
    import CommentEditComponent from "$lib/component/CommentEditComponent.svelte";

    export let view: "main" | "modal" | "page";
    export let review: Review;

    let commentModal;
    const toggleModal = () => {
        if (view !== "main") return;
        commentModal.open();
    }

</script>

<Dialog bind:this={commentModal}
        on:show={() => document.body.style.overflow = "hidden"}
        on:hide={() => document.body.style.overflow = "auto"}>
    <CommentModal bind:review={review} />
</Dialog>

<main>
    {#if view !== "modal"}
        <p class="comment-count" class:hoverable={view === "main"}
           on:click={toggleModal}
           on:keydown={e => e.key === 'Enter' && toggleModal()}>
            댓글 {review.comments.length}
        </p>
    {/if}
    {#if view !== "main"}
        <div class={view === "modal" ? "comments modal-view" : "comments"}>
            {#each review.comments as comment}
                <CommentComponent bind:comment={comment} bind:review={review} />
            {/each}
        </div>
    {:else if review.comments.length > 0}
        <CommentComponent bind:comment={review.comments[review.comments.length - 1]} bind:review={review} />
    {/if}
    {#if view !== "main"}
        {#if review.comments.length === 0}
            <p class="comment-count">댓글이 없습니다.</p>
        {/if}
        <CommentEditComponent bind:review={review} comment={null} isEdit={false} editing={false} />
    {/if}
</main>

<style>

    main {
        display: flex;
        flex-direction: column;
        margin-left: 1rem;
        margin-right: 1rem;
        align-items: start;
    }

    .comments {
        width: 100%;
    }

    .modal-view {
        max-height: 400px; /* Adjust this value based on your desired maximum height */
        overflow-y: auto;
    }

    .comment-count {
        font-family: 'D2Coding', sans-serif;
        font-size: 15px;
        color: rgba(0, 0, 0, 0.8);
        margin-bottom: 10px;
    }

    .comment-count.hoverable:hover {
        text-decoration: underline;
        cursor: pointer;
    }

</style>