<script lang="ts">

    import PageData = App.PageData;
    import {Review} from "../../../../lib/dto/Review";
    import {onMount} from "svelte";
    import {API} from "../../../../lib/api";
    import ReviewComponent from "$lib/component/ReviewComponent.svelte";

    export let data: PageData;

    let review: Review;
    let withScrap: boolean;
    let notFound: boolean;

    onMount(async () => {
        const response = await API.fetchReview(data.param);

        if (response.ok) {
            const json = await response.json();
            review = Review.fromJson(json);
            withScrap = data.query.get('withScrap') === 'true';
        } else if (response.status === 404) {
            notFound = true;
        }
    });

</script>

<main>
    {#if notFound}
        <div class="not-found">리뷰를 찾을 수 없습니다!</div>
    {:else}
        <ReviewComponent review={review} withScrap={withScrap} view="page"/>
    {/if}
</main>