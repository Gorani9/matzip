<script lang="ts">

    import {API} from "../api";
    import {dialogs, getClose} from "svelte-dialogs";
    import {Review} from "../dto/Review";
    import {onMount} from "svelte";

    const close = getClose();

    export let review: Review;

    let description: string = '';
    let descriptionInput;

    const handleScrap = async () => {
        const response = await API.putScrap(review.id, description);
        const json = await response.json();

        if (response.ok) {
            review = Review.fromJson(json);
            close("scraped");
        } else {
            await dialogs.error("스크랩에 실패했습니다. 다시 시도해주세요.");
            location.reload();
        }
    }

    onMount(() => descriptionInput.focus());

</script>

<main>
    <h3>Memo</h3>
    <textarea bind:value={description} bind:this={descriptionInput}></textarea>
    <button on:click={handleScrap}>스크랩</button>
</main>

<style>

    main {
        display: flex;
        flex-direction: column;
        align-items: start;
        justify-content: center;
    }

    h3 {
        margin-bottom: 1rem;
    }

    textarea {
        min-height: 150px;
        width: 100%;
        padding: 0.5rem;
        margin-bottom: 1rem;
        border: 2px solid #66bfbf;
        border-radius: 0.5rem;
    }

    textarea:active {
        border-color: #aaa;
    }

    button {
        width: 100%;
        padding: 0.5rem;
        border: 1px solid #ccc;
        border-radius: 0.5rem;
        background-color: #66bfbf;
        color: #fff;
        font-weight: 600;
    }

    button:hover {
        background-color: #aaa;
    }

    button:active {
        background-color: #888;
    }

</style>