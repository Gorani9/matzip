<script lang="ts">

    import {Review} from "$lib/dto/Review.js";
    import CommentsComponent from "$lib/component/CommentsComponent.svelte";

    export let review: Review;

</script>

<CommentsComponent bind:review={review} view="modal" />