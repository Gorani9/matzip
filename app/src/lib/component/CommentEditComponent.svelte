<script lang="ts">

    import {API} from "$lib/api.js";
    import {dialogs} from "svelte-dialogs";
    import LoginModal from "../modal/LoginModal.svelte";
    import {Review} from "../dto/Review";
    import {Comment} from "../dto/Comment";
    import {afterUpdate} from "svelte";

    export let review: Review;
    export let comment: Comment | null;

    export let isEdit: boolean = false;
    export let editing: boolean = false;

    const cancelEdit = () => {
        if (isEdit) {
            editing = false;
            inputContent = comment!.content;
        }
    }

    let inputElement;
    let inputContent = comment?.content ?? "";

    afterUpdate(() => {
        if (isEdit && editing) {
            inputElement.focus();
        }
    });

    const handlePost = async () => {
        const response = await API.postComment(review.id, inputContent);
        const json = await response.json();

        if (response.ok) {
            review = Review.fromJson(json);
            inputContent = "";
        } else if (response.status === 403) {
            await dialogs.modal(LoginModal, {redirect: null});
        } else {
            await dialogs.alert("댓글 작성에 실패했습니다. 다시 시도해주세요.");
        }
    }

    const handlePatch = async () => {
        const response = await API.patchComment(comment!.id, inputContent);
        const json = await response.json();

        if (response.ok) {
            review = Review.fromJson(json);
        } else if (response.status === 403) {
            await dialogs.modal(LoginModal, {redirect: null});
        } else {
            await dialogs.alert("댓글 수정에 실패했습니다. 다시 시도해주세요.");
        }
    }

    const handleClick = () => {
        if (isEdit) {
            handlePatch();
        } else {
            handlePost();
        }
    }

</script>

<main>
    <form on:submit|preventDefault>
        <div class="wrapper">
            <input type="text" bind:this={inputElement} bind:value={inputContent} on:blur={cancelEdit}>
            <button type="submit" on:click={handleClick}>
                <img src="/icons/send.png" alt="confirm" />
            </button>
        </div>
    </form>
</main>

<style>

    main, form {
        display: flex;
        align-items: center;
        margin-top: 5px;
        width: 100%;
    }

    main {
        border-bottom: 2px solid #66bfbf;
        margin-bottom: 7px;
    }

    form {
        background: white;
        border-radius: 10px;
    }

    .wrapper {
        display: flex;
        align-items: center;
        width: 100%;
    }

    input {
        width: 100%;
        font-family: "D2Coding", sans-serif;
        font-size: 15px;
        padding: 3px 5px;
        margin-right: 0;
        outline: none;
        border: 0;
        border-radius: 10px 10px 10px 5px;
    }


    button {
        border: none;
        background-color: transparent;
        cursor: pointer;
        margin-right: 5px;
    }

    button > img {
        width: 17px;
        height: 17px;
    }

</style>