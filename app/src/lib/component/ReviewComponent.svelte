<script lang="ts">
    import {Review} from '../dto/Review';
    import {API} from "../api";
    import {Dialog, dialogs} from "svelte-dialogs";
    import LoginModal from "../modal/LoginModal.svelte";
    import UserMarkerComponent from "./UserMarkerComponent.svelte";
    import TimeMarkerComponent from "./TimeMarkerComponent.svelte";
    import CarouselComponent from "./CarouselComponent.svelte";
    import {formatNumbers} from "$lib/util/NumberFormatter.js";
    import LoadingComponent from "$lib/component/LoadingComponent.svelte";
    import CommentsComponent from "$lib/component/CommentsComponent.svelte";
    import ScrapModal from "../modal/ScrapModal.svelte";
    import {goto} from "$app/navigation";
    import ScrapComponent from "$lib/component/ScrapComponent.svelte";
    import {ME, User} from "../dto/User";
    import ReviewModal from "$lib/modal/ReviewModal.svelte";

    export let review: Review;
    export let withScrap: boolean;
    export let view: "main" | "page";

    let user: User | null;

    ME.subscribe(value => {
        user = value;
    });

    let isExpanded = view == "page";
    let isTruncated = false;

    let scrapModal;
    let editModal;

    const toggleExpanded = () => {
        if (view == "page") return;
        isExpanded = !isExpanded;
    }

    const toggleLike = async () => {
        if (review.isLiked) {
            if (!user) {
                await dialogs.modal(LoginModal, {redirect: null, reload: true});
            } else {
                const response = await API.deleteLike(review.id);
                const json = await response.json();

                if (response.ok) {
                    review = Review.fromJson(json);
                } else {
                    await dialogs.error("알 수 없는 오류가 발생했습니다. 다시 시도해주세요.");
                }
            }
        } else {
            if (!user) {
                await dialogs.modal(LoginModal, {redirect: null, reload: true});
            } else if (review.user.isMe) {
                await dialogs.warning("자신의 리뷰에는 좋아요를 누를 수 없습니다.");
            } else if (review.isLiked) {
                await dialogs.warning("이미 좋아요를 누른 리뷰입니다.");
            } else {
                const response = await API.putLike(review.id);
                const json = await response.json();

                if (response.ok) {
                    review = Review.fromJson(json);
                } else {
                    await dialogs.error("알 수 없는 오류가 발생했습니다. 다시 시도해주세요.");
                }
            }
        }
    }

    const handleScrapReview = () => {
        if (!user) {
            dialogs.modal(LoginModal, {redirect: null, reload: null});
        } else if (review.user.isMe) {
            dialogs.warning("자신의 리뷰는 스크랩할 수 없습니다.");
        } else if (review.isScrapped) {
            dialogs.warning("이미 스크랩한 리뷰입니다.");
        } else {
            scrapModal.open();
        }
    }

    const truncContent = (content: string) => {
        if (view == "main") {
            const lineBreakIndex = content.indexOf("\n", 30);
            if (content.length > 30 || lineBreakIndex !== -1) {
                isTruncated = true;
                if (lineBreakIndex !== -1) return content.substring(0, lineBreakIndex) + " ...";
                else return content.substring(0, 30) + " ...";
            } else {
                isTruncated = false;
                return content;
            }
        } else {
            isTruncated = false;
            return content;
        }
    }

    const handleEdit = () => {
        editModal.open();
    }

    const handleDelete = async () => {
        const ret = await dialogs.confirm({
            title: '정말로 삭제하시겠습니까?',
            text: "삭제하면 복구할 수 없습니다.",
            confirmButtonText: '삭제',
            declineButtonText: '취소',
        })

        if (!ret) return;

        const response = await API.deleteReview(review.id);

        if (response.ok) {
            await goto("/");
        } else {
            await dialogs.error("알 수 없는 오류가 발생했습니다. 다시 시도해주세요.");
        }
    }

</script>

<Dialog bind:this={scrapModal}
        on:show={() => document.body.style.overflow = "hidden"}
        on:hide={() => document.body.style.overflow = "auto"}>
    <ScrapModal bind:review={review} />
</Dialog>

<Dialog bind:this={editModal}
        on:show={() => document.body.style.overflow = "hidden"}
        on:hide={() => document.body.style.overflow = "auto"}>
    <ReviewModal reviews={[]} bind:review={review} editMode={true} />
</Dialog>

<main>
    {#if !review}
        <LoadingComponent />
    {:else}
        {#if view !== "modal"}
            <div class="review-meta-info">
                <UserMarkerComponent user={review.user} markerSize={45} />
                <p class="restaurant">{review.restaurant.location}</p>
                <div class="rating">
                    {#each Array(Math.max(0, review.rating)) as _, i}
                        <img class="star" src={"/icons/star-filled.svg"} alt="star">
                    {/each}
                </div>
                <TimeMarkerComponent createdAt={review.createdAt} modifiedAt={review.modifiedAt} fontSize={15}/>
            </div>
        {/if}
        <CarouselComponent images={review.imageUrls} />
        <div class="review-info">
            <div class="review-popularity">
                <div class="likes" on:click={toggleLike} on:keydown={e => e.key === 'Enter' && toggleLike()}>
                    {#if review.isLiked}
                        <img src="/icons/like-filled.png" alt="heart">
                    {:else}
                        <img src="/icons/like.png" alt="heart">
                    {/if}
                    <p>{formatNumbers(review.numberOfLikes)}</p>
                </div>
                <div class="scraps" on:click={handleScrapReview} disabled={review.isScrapped}
                     on:keydown={e => e.key === 'Enter' && handleScrapReview()}>
                    {#if review.isScrapped}
                        <img src="/icons/scrap-filled.png" alt="scrap">
                    {:else}
                        <img src="/icons/scrap.png" alt="scrap">
                    {/if}
                    <p>{formatNumbers(review.numberOfScraps)}</p>
                </div>
                {#if view === "main"}
                    <div class="full-screen" on:click={() => goto("/reviews/" + review.id + "?withScrap=false")}
                         on:keydown={e => e.key === 'Enter' && goto("/reviews/" + review.id + "?withScrap=false")}>
                        <img src="/icons/expand.png" alt="full-screen">
                    </div>
                {:else}
                    {#if review.user.isMe}
                        <div class="edit" on:click={handleEdit} on:keydown={e => e.key === 'Enter' && handleEdit()}>
                            <img src="/icons/edit.png" alt="edit">
                        </div>
                        <div class="delete" on:click={handleDelete} on:keydown={e => e.key === 'Enter' && handleDelete()}>
                            <img src="/icons/delete.png" alt="delete">
                        </div>
                    {/if}
                    <div class="views" class:solo={!review.user.isMe}>
                        <img src="/icons/view.png" alt="eye">
                        <p>{formatNumbers(review.views)}</p>
                    </div>
                {/if}
            </div>
            {#if view === "page" && review.isScrapped}
                <ScrapComponent bind:review={review} minimized={!withScrap}/>
            {/if}
            <p class="content" on:click={toggleExpanded} on:keydown={e => e.key === 'Enter' && toggleExpanded()}
               class:hoverable={isTruncated}>{truncContent(review.content)}</p>
        </div>
        <CommentsComponent bind:review={review} view={view}/>
    {/if}
</main>

<style>

    main {
        align-items: center;
        width: 100%;
        margin-top: 20px;
    }

    .review-meta-info {
        display: flex;
        align-items: center;
        margin-bottom: 10px;
    }

    .review-meta-info .restaurant {
        font-family: 'D2Coding', sans-serif;
        font-size: 15px;
        color: rgba(0, 0, 0, 0.5);
        margin-right: 5px;
        margin-bottom: 0;
    }

    .review-meta-info .rating {
        display: flex;
        margin-left: 10px;
    }

    .review-meta-info .rating .star {
        width: 15px;
        padding: 2px;
    }

    .review-info .content {
        margin-top: 20px;
        margin-bottom: 20px;
        font-family: 'D2Coding', sans-serif;
        font-size: 15px;
        line-height: 1.5;
        text-align: left;
        border: 1px;
        padding: 15px;
    }

    .review-info .hoverable:hover {
        cursor: pointer;
    }

    .review-info .review-popularity {
        display: flex;
        margin-top: 10px;
    }

    .likes, .scraps, .full-screen, .views, .edit, .delete {
        display: flex;
        align-items: center;
        cursor: pointer;
        margin-right: 10px;
    }

    .edit, .full-screen {
        margin-left: auto;
    }

    .views.solo {
        margin-left: auto;
    }

    .likes img, .scraps img, .views img, .full-screen img, .edit img, .delete img {
        width: 20px;
        height: 20px;
        margin-right: 5px;
    }

    .likes p, .scraps p, .views p {
        font-family: 'D2Coding', sans-serif;
        font-size: 15px;
        color: rgba(0, 0, 0, 0.8);
        margin-bottom: 0;
    }

</style>
