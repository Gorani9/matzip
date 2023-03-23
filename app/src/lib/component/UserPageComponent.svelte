<script lang="ts">

    import {User} from "../dto/User";
    import {formatNumbers} from "$lib/util/NumberFormatter.js";
    import LoadingComponent from "$lib/component/LoadingComponent.svelte";
    import {goto} from "$app/navigation";
    import {API} from "../api";
    import {dialogs} from "svelte-dialogs";

    export let user: User | null;
    export let isMe: boolean = false;

    let active = true;

    const toggleActive = () => {
        active = !active;
    }

    const toggleFollow = async () => {
        if (user?.isMyFollowing) {
            const ret = await dialogs.confirm({
                title: "팔로우를 취소하시겠습니까?",
                confirmButtonText: "예",
                declineButtonText: "아니오"
            });

            if (!ret) return;

            const response = await API.unfollowUser(user.username);

            if (response.ok) {
                const json = await response.json();
                user = User.fromJson(json);
            } else {
                await dialogs.error("팔로우 취소에 실패했습니다. 다시 시도해주세요.");
            }
        } else {
            const response = await API.followUser(user!.username);

            if (response.ok) {
                const json = await response.json();
                user = User.fromJson(json);
            } else {
                await dialogs.error("팔로우에 실패했습니다. 다시 시도해주세요.");
            }
        }
    }

</script>

<main>
    {#if !user}
        <LoadingComponent/>
    {:else}
        <div class="user-info">
            <div class="info-top">
                <img class="user-image" src={user.profileImageUrl ?? "/icons/anonymous.png"} alt="user">
                <div class="user-popularity">
                    <div class="user-review-count">
                        <p>리뷰</p>
                        <p>{formatNumbers(user.reviews.length)}</p>
                    </div>
                    <div class="user-followers-count">
                        <p>팔로워</p>
                        <p>{formatNumbers(user.numberOfFollowers)}</p>
                    </div>
                    <div class="user-following-count">
                        <p>팔로잉</p>
                        <p>{formatNumbers(user.numberOfFollowings)}</p>
                    </div>
                </div>
                {#if !isMe}
                    <button class="follow-button" class:follow={user.isMyFollowing} on:click={toggleFollow}>팔로우</button>
                {/if}
            </div>
            <div class="info-bottom">
                <div class="username">{user.username}</div>
                <div class="user-profile">{user.profileString ?? "프로필이 없습니다."}</div>
            </div>
        </div>
        {#if isMe}
            <div class="tabs">
                <div class="tab" class:active={active} on:click={toggleActive}
                     on:keydown={(e) => e.key === 'Enter' && toggleActive()}>리뷰</div>
                <div class="tab" class:active={!active} on:click={toggleActive}
                     on:keydown={(e) => e.key === 'Enter' && toggleActive()}>스크랩</div>
            </div>
        {/if}
        <div class="review-tab" style="display: {active ? 'grid' : 'none'}">
            {#if user.reviews.length === 0}
                <div class="no-review">
                    <p>작성한 리뷰가 없습니다.</p>
                </div>
            {:else}
                <div class="image-grid">
                    {#each user.reviews ?? [] as review, i}
                        <div class="review-image" on:click={() => goto("/reviews/"+ review.id)}
                             on:keydown={(e) => e.key === 'Enter' && goto('/reviews/' + review.id)}>
                            <img src={review.imageUrls[0]} alt="review">
                        </div>
                    {/each}
                </div>
            {/if}
        </div>
        <div class="scrap-tab" style="display: {!active ? 'grid' : 'none'}">
            {#if user.scraps.length === 0}
                <div class="no-review">
                    <p>스크랩한 리뷰가 없습니다.</p>
                </div>
            {:else}
                <div class="image-grid">
                    {#each user.scraps ?? [] as review}
                        <div class="review-image" on:click={() => goto("/reviews/" + review.id + "?withScrap=true")}
                             on:keydown={(e) => e.key === 'Enter' && goto('/reviews/' + review.id + "?withScrap=true")}>
                            <img src={review?.imageUrls[0]} alt="review" >
                        </div>
                    {/each}
                </div>
            {/if}
        </div>
    {/if}
</main>

<style>
    main {
        display: flex;
        flex-direction: column;
        align-items: center;
        width: 100%;
        height: 100%;
    }

    .user-info {
        display: flex;
        flex-direction: column;
        align-items: center;
        width: 100%;
        height: 100%;
        padding: 10px;
        border-bottom: 1px solid #e9e9e9;
    }

    .info-top {
        display: flex;
        flex-direction: row;
        align-items: center;
        width: 100%;
        height: 100%;
        padding: 10px;
    }

    .user-image {
        width: 70px;
        height: 70px;
        border-radius: 50%;
        padding: 5px;
        border: 2px solid rgba(0, 0, 0, 1);
    }

    .user-popularity {
        display: flex;
        flex-direction: row;
        align-items: center;
        justify-content: space-between;
        width: 100%;
        height: 100%;
        padding: 0 20px;
    }

    .user-review-count, .user-followers-count, .user-following-count {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        width: 100%;
        height: 100%;
    }

    .user-review-count p:first-child, .user-followers-count p:first-child, .user-following-count p:first-child {
        font-size: 12px;
        color: #999999;
    }

    .user-review-count p:last-child, .user-followers-count p:last-child, .user-following-count p:last-child {
        font-family: 'D2Coding Bold', sans-serif;
        font-size: 16px;
        font-weight: 600;
    }

    .follow-button {
        width: 100px;
        height: 30px;
        border: 1px solid #e9e9e9;
        border-radius: 5px;
        background-color: #ffffff;
        color: #999999;
        font-size: 14px;
        font-weight: 600;
        cursor: pointer;
        margin-top: 15px;
    }

    .follow-button.follow {
        border: 1px solid #66bfbf;
        background-color: #66bfbf;
        color: #ffffff;
    }

    .info-bottom {
        display: flex;
        flex-direction: column;
        align-items: start;
        width: 100%;
        height: 100%;
        padding: 0 10px 0;
    }

    .username {
        font-family: 'D2Coding Bold', sans-serif;
        font-size: 20px;
        font-weight: 600;
        color: #000000;
        padding: 0;
    }

    .user-profile {
        font-family: 'D2Coding', sans-serif;
        font-size: 16px;
        padding-left: 0;
        padding-top: 10px;
        color: #999999;
    }

    .tabs {
        display: flex;
        flex-direction: row;
        align-items: center;
        justify-content: space-around;
        width: 100%;
        height: 100%;
        padding: 20px;
    }

    .tab {
        height: 100%;
        font-family: "D2Coding", sans-serif;
        font-size: 18px;
        color: #999999;
        cursor: pointer;
    }

    .tab.active {
        color: #000000;
        border-bottom: 2px solid #000000;
    }

    .image-grid {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        grid-auto-rows: 100%;
        width: 100%;
        height: 100%;
        box-sizing: border-box;
    }

    .review-image {
        display: inline-block;
        width: 100%;
        height: 100%;
        overflow: hidden;
        border-style: groove;
        border-width: 1px;
        border-color: #e9e9e9;
        border-radius: 5px;
    }

    .review-image img {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }

    .review-image:hover {
        cursor: pointer;
    }

    .no-review {
        display: flex;
        flex-direction: row;
        align-items: center;
        justify-content: center;
        width: 100%;
        height: 100%;
    }

    .no-review p {
        margin-top: 50%;
        font-size: 16px;
        color: #999999;
    }

</style>