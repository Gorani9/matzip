<script lang="ts">

    import {onMount} from "svelte";
    import {API} from "../../lib/api";
    import {Review} from "../../lib/dto/Review";
    import ReviewComponent from "$lib/component/ReviewComponent.svelte";
    import {Dialog} from "svelte-dialogs";
    import LoadingComponent from "$lib/component/LoadingComponent.svelte";
    import ReviewModal from "$lib/modal/ReviewModal.svelte";

    let reviews: Review[];
    let postReviewModal;

    onMount (async () => {
        const response = await API.getReviews();

        if (response.ok) {
            const json = await response.json();

            reviews = Review.fromJsonArray(json.content);
        }
    })

    const handlePostReview = () => {
        postReviewModal.open();
    }

</script>

<Dialog bind:this={postReviewModal}>
    <ReviewModal bind:reviews={reviews} review={null} editMode={false} />
</Dialog>

<main>
    {#if !reviews}
        <LoadingComponent/>
    {:else}
        <div class="grid-container">
            {#each reviews as review}
                <div class="grid-item">
                    <ReviewComponent {review} withScrap={false} view="main"/>
                </div>
            {/each}
        </div>
    {/if}
    <div class="button-container">
        <button class="post-review" on:click={handlePostReview}>
            <img src="/icons/food.png" alt="post-review">
        </button>
    </div>
</main>

<style>

    .grid-container {
        display: grid;
        grid-template-columns: 1fr;
        grid-gap: 20px;
        min-height: 100vh;
    }

    .grid-item {
        background-color: #f5f5f5;
        border-radius: 5px;
        box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
        padding: 10px;
        text-align: center;
    }

    .button-container {
        position: sticky;
        bottom: 100px;
    }

    .post-review {
        position: absolute;
        bottom: 0;
        right: 20px;
        width: 50px;
        height: 50px;
        border-radius: 50%;
        background-color: #f5f5f5;
        box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
        border: 1px solid black;
        outline: none;
        cursor: pointer;
    }

    .post-review img {
        width: 30px;
        height: 30px;
    }

</style>