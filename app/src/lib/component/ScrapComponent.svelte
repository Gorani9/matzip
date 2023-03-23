<script lang="ts">

    import {afterUpdate, onMount} from "svelte";
    import {API} from "../api";
    import {dialogs} from "svelte-dialogs";
    import {Review} from "../dto/Review";

    export let review: Review;
    export let minimized = false;

    let editInput;
    let editMode = false;
    let description;

    onMount(() => {
        description = review.scrapDescription;
    });

    afterUpdate(() => {
        if (editMode) {
            editInput.focus();
        }
    });

    const cancelEdit = () => {
        editMode = false;
        description = review.scrapDescription;
    }

    const patchScrap = async () => {
        const response = await API.putScrap(review.id, description);

        if (response.ok) {
            const json = await response.json();
            review = Review.fromJson(json);

            editMode = false;
        }
    }

    const toggleMinimized = () => {
        minimized = !minimized;
    }

    const handleEditScrap = () => {
        editMode = true;
    }

    const handleDeleteScrap = async () => {
        const confirmed = await dialogs.confirm({
            title: '정말로 삭제하시겠습니까?',
            confirmButtonText: '예',
            declineButtonText: '아니오',
        });

        if (!confirmed) return;

        const response = await API.deleteScrap(review.id);

        if (response.ok) {
            const json = await response.json();
            review = Review.fromJson(json);
        } else {
            await dialogs.error("삭제에 실패했습니다. 다시 시도해주세요.");
        }
    }

</script>

<main class="scrap-container" class:minimized={minimized}>
    <h4>Memo</h4>
    <button class="edit-scrap" on:click={handleEditScrap}>
        <img src="/icons/edit.png" alt="edit">
    </button>
    <button class="delete-scrap" on:click={handleDeleteScrap}>
        <img src="/icons/delete.png" alt="delete">
    </button>
    <button class="minimize" class:hidden={minimized} on:click={toggleMinimized}>
        <img src="/icons/minimize.png" alt="minimize">
    </button>
    <button class="maximize" class:hidden={!minimized} on:click={toggleMinimized}>
        <img src="/icons/maximize.png" alt="maximize">
    </button>
    {#if editMode}
        <textarea bind:this={editInput} bind:value={description} on:blur={cancelEdit}></textarea>
        <div class="edit-buttons">
            <button on:click={cancelEdit}>취소</button>
            <button on:click={patchScrap}>저장</button>
        </div>
    {:else}
        <div class="description">{review.scrapDescription}</div>
    {/if}
</main>

<style>

    .scrap-container.minimized .description,
    .scrap-container.minimized .edit-scrap,
    .scrap-container.minimized .delete-scrap,
    .scrap-container.minimized textarea,
    .scrap-container.minimized .edit-buttons,
    .hidden {
        display: none;
        padding: 0;
    }

    .scrap-container.minimized {
        overflow: hidden;
        transition: max-height 0.3s ease;
        max-height: 50px;
        margin-left: auto;
        width: 5.5rem;
    }

    .scrap-container {
        background-color: rgba(255, 165, 0, 0.2);
        border-radius: 10px;
        padding: 10px;
        margin: 10px;
        position: relative;
        overflow: hidden;
        transition: max-height 0.3s ease;
        max-height: 300px;
    }

    .scrap-container h4 {
        margin: 0;
        font-family: 'D2Coding', sans-serif;
    }

    .maximize {
        position: absolute;
        top: 7px;
        right: 2px;
    }

    .minimize {
        position: absolute;
        top: 7px;
        right: 2px;
    }

    .edit-scrap {
        position: absolute;
        top: 7px;
        right: 62px;
    }

    .delete-scrap {
        position: absolute;
        top: 7px;
        right: 32px;
    }

    .minimize, .maximize, .edit-scrap, .delete-scrap {
        background-color: transparent;
        border: none;
    }

    .minimize img, .maximize img, .edit-scrap img, .delete-scrap img {
        width: 20px;
        height: 20px;
    }

    .description {
        margin-top: 10px;
        margin-bottom: 10px;
        font-family: D2Coding, sans-serif;
    }

    textarea {
        width: 100%;
        height: 100px;
        border: none;
        border-radius: 10px;
        padding: 10px;
        margin-bottom: 10px;
        font-family: D2Coding, sans-serif;
    }

    .edit-buttons {
        display: flex;
        justify-content: flex-end;
    }

    .edit-buttons button {
        background-color: transparent;
        border: none;
        margin-left: 10px;
    }

</style>