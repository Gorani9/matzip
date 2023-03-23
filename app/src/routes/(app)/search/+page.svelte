<script lang="ts">

    import {Review} from "../../../lib/dto/Review";
    import {User} from "../../../lib/dto/User";
    import LoadingComponent from "$lib/component/LoadingComponent.svelte";
    import {API} from "../../../lib/api";
    import ReviewResultContainer from "$lib/component/ReviewResultContainer.svelte";
    import UserResultContainer from "$lib/component/UserResultContainer.svelte";

    let editSearchOptions = false;
    let searchMode: "user" | "review" = "review";
    let reviewSort: "username" | "level" | "followers" | "hearts" | "scraps" | "comments" | "rating" = "username";
    let userSort: "username" | "level" | "followers" = "username";
    let sortOrder: boolean = false;

    let previousKeyword;
    let searchKeyword = "";

    let searchResults: User[] | Review[] = [];

    let loading = false;

    const toggleSearchOptions = () => {
        editSearchOptions = !editSearchOptions;
    }

    const toggleSearchMode = () => {
        if (searchMode === "review") {
            searchMode = "user";
            userSort = "username";
        } else {
            searchMode = "review";
            reviewSort = "username";
        }
        sortOrder = false;
        handleSearch();
    }

    const setSearchMode = (mode: "user" | "review") => {
        searchMode = mode;
        handleSearch();
    }

    const setReviewSortMode = (mode: "username" | "level" | "followers" | "hearts" | "scraps" | "comments" | "rating") => {
        reviewSort = mode;
        handleSearch();
    }

    const setUserSortMode = (mode: "username" | "level" | "followers") => {
        userSort = mode;
        handleSearch();
    }

    const toggleSortOrder = () => {
        sortOrder = !sortOrder;
        handleSearch();
    }

    const handleSearch = () => {
        if (loading || !searchKeyword) return;
        loading = true;
        previousKeyword = searchKeyword;
        if (searchMode === "review") {
            const response = API.searchReviews(searchKeyword, reviewSort, sortOrder);

            response.then(async (reviews) => {
                const json = await reviews.json();
                searchResults = Review.fromJsonArray(json.content);
                loading = false;
            });
        } else {
            const response = API.searchUsers(searchKeyword, userSort, sortOrder);

            response.then(async (users) => {
                const json = await users.json();
                searchResults = User.fromJsonArray(json.content);
                loading = false;
            });
        }
    }

</script>

<main>
    <div class="search-bar">
        <button class="search-mode" class:user={searchMode === "user"} on:click={toggleSearchMode}>
            {#if searchMode === "review"}
                <img src="/icons/search-review.png" alt="review-mode" />
            {:else}
                <img src="/icons/search-user.png" alt="user-mode" />
            {/if}
        </button>
        <div class="search-input">
            <button class="search-options" on:click={toggleSearchOptions}>
                <img src="/icons/options.png" alt="options" />
            </button>
            <input
                    bind:value="{searchKeyword}"
                    type="text"
                    placeholder="{searchMode === 'user' ? '검색할 이름을 입력해주세요' : '검색어를 입력해주세요'}"
                    on:input={handleSearch}
                    on:keydown="{(e) => e.key === 'Enter' && handleSearch()}"
            />
            <button class="search-button" on:click={handleSearch}>
                <img src="/icons/search.png" alt="search" />
            </button>
        </div>
    </div>
    {#if editSearchOptions}
        <div class="option-grid">
            <div class="header">종류</div>
            <div class="items">
                <button class:active={searchMode === "review"} on:click={() => setSearchMode("review")}>리뷰</button>
                <button class:active={searchMode === "user"} on:click={() => setSearchMode("user")}>유저</button>
            </div>
            <div class="header">정렬기준</div>
            <div class="items">
                {#if searchMode === "review"}
                    <button class:active={reviewSort === "username"} on:click={() => setReviewSortMode("username")}>작성자 이름</button>
                    <button class:active={reviewSort === "level"} on:click={() => setReviewSortMode("level")}>작성자 레벨</button>
                    <button class:active={reviewSort === "followers"} on:click={() => setReviewSortMode("followers")}>작성자 팔로워</button>
                    <button class:active={reviewSort === "hearts"} on:click={() => setReviewSortMode("hearts")}>좋아요</button>
                    <button class:active={reviewSort === "scraps"} on:click={() => setReviewSortMode("scraps")}>스크랩</button>
                    <button class:active={reviewSort === "comments"} on:click={() => setReviewSortMode("comments")}>댓글</button>
                    <button class:active={reviewSort === "rating"} on:click={() => setReviewSortMode("rating")}>평점</button>
                {:else}
                    <button class:active={userSort === "username"} on:click={() => setUserSortMode("username")}>이름</button>
                    <button class:active={userSort === "level"} on:click={() => setUserSortMode("level")}>레벨</button>
                    <button class:active={userSort === "followers"} on:click={() => setUserSortMode("followers")}>팔로워</button>
                {/if}
            </div>
            <div class="header">순서</div>
            <div class="items">
                <button class:active={sortOrder} on:click={toggleSortOrder}>역순</button>
            </div>
        </div>
    {/if}
    {#if loading}
        <LoadingComponent />
    {:else}
        <div class="search-results">
            {#if previousKeyword && searchResults.length === 0}
                <div class="no-results">
                    <img src="/icons/no-results.png" alt="no-results" />
                    <p>검색 결과가 없습니다.</p>
                </div>
            {:else}
                {#each searchResults as result}
                    {#if searchMode === "review"}
                        <ReviewResultContainer review={result} keyword={searchKeyword} />
                    {:else}
                        <UserResultContainer user={result} />
                    {/if}
                {/each}
            {/if}
        </div>
    {/if}

</main>

<style>

    main {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        width: 100%;
        height: 100%;
    }

    .search-bar {
        display: flex;
        flex-direction: row;
        align-items: center;
        justify-content: center;
        width: 100%;
        height: 50px;
        background-color: #f5f5f5;
        margin-bottom: 20px;
    }

    .search-mode {
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0;
        padding: 0;
        border: none;
        background-color: #f5f5f5;
    }

    .search-mode img {
        width: 50px;
        height: 50px;
    }

    .search-input {
        display: flex;
        flex-direction: row;
        margin-top: 0;
        margin-left: 15px;
        width: 100%;
        height: 50px;
        border: 1px solid #e0e0e0;
        border-radius: 50px;
        background-color: #f5f5f5;
        padding: 0 5px 0 20px;
    }

    .search-input .search-options {
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0;
        padding: 0;
        border: none;
        background-color: #f5f5f5;
    }

    .search-input .search-options img {
        width: 30px;
        height: 30px;
    }

    .search-input input {
        width: 100%;
        height: 100%;
        border: none;
        margin-left: 10px;
        border-radius: 10px;
        background-color: #f5f5f5;
        outline: none;
    }

    .search-button {
        display: flex;
        align-items: center;
        justify-content: center;
        border: none;
        background: transparent;
    }

    .search-button img {
        width: 30px;
        height: 30px;
    }

    .no-results {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        margin-top: 10vh;
        width: 100%;
        height: 100%;
    }

    .no-results img {
        width: 100px;
        height: 100px;
    }

    .no-results p {
        margin-top: 10vh;
        padding: 0;
        font-size: 20px;
        font-weight: 500;
    }

    .option-grid button {
        border: 2px solid #66bfbf;
        border-radius: 20px;
        background-color: #ffffff;
        padding: 0 5px 0 5px;
    }

    .option-grid button.active {
        background-color: #66bfbf;
        color: #ffffff;
    }

    .option-grid {
        display: grid;
        grid-template-columns: 1fr 4fr;
        grid-template-rows: 1fr 2fr 1fr;
        width: 100%;
        height: 100px;
        margin: 0 0 50px 0;
        background-color: #f5f5f5;
    }

    .header {
        margin: 10px 0;
    }

    .items {
        margin: 5px 0;
    }

    .items button {
        margin: 3px 0;
        padding: 0 10px;
    }

    .search-results {
        display: flex;
        flex-direction: column;
        width: 100%;
    }

</style>