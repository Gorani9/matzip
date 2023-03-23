<script lang="ts">

    import {Review} from "../dto/Review";
    import UserMarkerComponent from "$lib/component/UserMarkerComponent.svelte";
    import TimeMarkerComponent from "$lib/component/TimeMarkerComponent.svelte";
    import {goto} from "$app/navigation";

    export let review: Review;
    export let keyword: string;

    const maxLength = 20;
    const truncContent = (content: string) => {
        // if content is longer than the maxLength, then find where the keyword is located and truncate the content
        if (content.length > maxLength) {
            const keywordIndex = content.indexOf(keyword);
            if (keywordIndex === -1) {
                return content.substring(0, maxLength - 3) + "...";
            } else {
                const start = Math.max(0, keywordIndex - maxLength / 2);
                const end = Math.min(content.length, keywordIndex + maxLength / 2);
                return (start == 0 ? "" : "..") +
                    content.substring(Math.max(0, start - 1), Math.min(end + 1, content.length)) +
                    (end == content.length ? "" : "..");
            }
        } else {
            return content;
        }
    }

</script>

<main on:click={() => goto("/reviews/" + review.id)}
      on:keydown={(e) => e.key === 'Enter' && goto("/reviews/" + review.id)}>
    <div class="review-image">
        <img src={review.imageUrls[0]} alt="review">
    </div>
    <div class="container">
        <div class="container-top">
            <UserMarkerComponent user={review.user} markerSize={40} />
            <p class="restaurant">{review.restaurant.location}</p>
            <div class="rating">
                {#each Array(Math.max(0, review.rating)) as _, i}
                    <img class="star" src={"/icons/star-filled.svg"} alt="star">
                {/each}
            </div>
            <TimeMarkerComponent createdAt={review.createdAt} modifiedAt={review.createdAt} fontSize={10}/>
        </div>
        <div class="container-bottom">
            <p>{truncContent(review.content)}</p>
        </div>
    </div>
</main>

<style>

    main {
        display: flex;
        flex-direction: row;
        width: 100%;
        height: 100%;
        border-radius: 10px;
        box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
        background-color: white;
    }

    main:hover {
        cursor: pointer;
    }

    .review-image {
        width: 30%;
        overflow: hidden;
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 1px;
        border: 1px solid rgba(0, 0, 0, 0.1);
        border-radius: 10px 0 0 10px;
        position: relative;
    }

    .review-image::before {
        content: "";
        display: block;
        padding-top: 100%;
    }

    .review-image img {
        position: absolute;
        width: 100%;
        object-fit: cover;
    }

    .container {
        padding: 0.5rem;
    }

    .container-top {
        display: flex;
        align-items: center;
    }

    .restaurant {
        font-size: 13px;
        font-family: 'D2Coding', sans-serif;
        color: rgba(0, 0, 0, 0.5);
        margin-right: 5px;
        margin-bottom: 0;
    }

    .rating {
        display: flex;
        align-items: center;
    }

    .star {
        width: 0.5rem;
        height: 0.5rem;
        margin-right: 0.2rem;
    }

    .container-bottom p {
        font-size: 15px;
        font-family: 'D2Coding', sans-serif;
        margin: 5px 0 0 0;
    }

</style>