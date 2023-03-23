<script lang="ts">

    import { API } from "$lib/api";
    import {dialogs, getClose} from "svelte-dialogs";
    import {onMount} from "svelte";
    import LoginModal from "./LoginModal.svelte";
    import {Review} from "../dto/Review";
    import {ME, User} from "../dto/User";
    import LoadingComponent from "$lib/component/LoadingComponent.svelte";

    const close = getClose();

    let loading = false;

    export let reviews: Review[];
    export let review: Review;
    export let editMode: boolean;

    let user: User | null = null;

    ME.subscribe(value => {
        user = value;
    });

    let locationInput;
    let contentInput;

    let location = editMode ? review.restaurant.location : "";
    let content = editMode ? review.content : "";
    let ratingInitialized = editMode;
    let rating = editMode ? review.rating : 0;
    let ratingStars = [];
    let previewSources = editMode ? review.imageUrls.slice() : [];
    let upload = [];
    let files = [];
    let oldUrls = [];

    onMount(async () => {
        if (!user) {
            const ret = await dialogs.modal(LoginModal, {redirect: null, reload: false});

            if (!ret) close(null);
        } else if (editMode && !review.user.isMe) {
            close(null);
        }

        changeStarImage(rating - 1);

        if (editMode) contentInput.focus();
        else locationInput.focus();
    })

    const handlePostReview = async () => {

        loading = true;
        const response = await API.postReview(content, rating, files, location);
        loading = false;

        if (response.ok) {
            const json = await response.json();
            const review = Review.fromJson(json);

            reviews = [review, ...reviews];
            close("Post Review Success");
        } else {
            await dialogs.error("리뷰 작성에 실패했습니다. 다시 시도해주세요.");
        }
    }

    const handlePatchReview = async () => {

        loading = true;
        const response = await API.patchReview(
            review.id,
            files,
            rating != review.rating ? rating : null,
            content != review.content ? content : null,
            oldUrls
        );
        loading = false;

        if (response.ok) {
            const json = await response.json();
            review = Review.fromJson(json);

            close("Post Review Success");
        } else {
            await dialogs.error("리뷰 수정에 실패했습니다. 다시 시도해주세요.");
        }
    }

    const changeStarImage = (index) => {
        for (let i = 0; i < ratingStars.length; i++) {
            if (i <= index) {
                ratingStars[i].src = "/icons/star-filled.svg";
            } else {
                ratingStars[i].src = "/icons/star.png";
            }
        }
    };

    const handleStarClick = (index) => {
        if (ratingInitialized && rating == index + 1) {
            rating = 0;
            changeStarImage(-1);
        } else {
            rating = index + 1;
            changeStarImage(index);
        }
        ratingInitialized = true;
    }

    const handleStarMouseOver = (index) => {
        if (ratingInitialized && index <= rating) return;
        changeStarImage(index);
    }

    const handleStarMouseOut = () => {
        if (ratingInitialized) {
            changeStarImage(rating - 1);
        } else {
            changeStarImage(-1);
        }
    }

    const handleImageUpload = () => {

        if (upload.length + previewSources.length > 10) {
            dialogs.warning("최대 10장의 사진만 업로드할 수 있습니다.");
            return;
        }

        const promises = [];

        for (let i = 0; i < upload.length; i++) {
            const reader = new FileReader();
            const promise = new Promise((resolve) => {
                reader.onload = (e) => {
                    resolve(e.target.result);
                };
            });
            promises.push(promise);
            reader.readAsDataURL(upload[i]);
        }

        files = [...files, ...upload];

        Promise.all(promises).then((results) => {
            previewSources = [...previewSources, ...results];
        });

        upload = [];
    };

    const handleImageDelete = (index) => {
        const deletedUrl = previewSources.splice(index, 1)[0];
        files.splice(index, 1);
        previewSources = [...previewSources];

        if (editMode && review.imageUrls.includes(deletedUrl)) {
            oldUrls.push(deletedUrl);
        }
    }

</script>

{#if loading}
    <LoadingComponent/>
{:else}
    <main>
        <h4>리뷰를 {editMode ? "수정": "작성"}해주세요</h4>
        <form on:submit|preventDefault={editMode ? handlePatchReview : handlePostReview}>
            <fieldset>
                <div class="form-floating mb-3 mt-3">
                    <input id="location" name="location" class="form-control" type="text"
                           bind:this={locationInput} bind:value={location} disabled={editMode} required/>
                    <label for="location">장소</label>
                </div>

                <div class="form-floating mb-3 mt-3">
                    <div id="images">
                        {#each previewSources as source, index}
                            <div class="image-container">
                                <button class="delete-button" type="button" on:click={() => handleImageDelete(index)}></button>
                                <img src={source} alt="preview">
                            </div>
                        {/each}
                        <div class="button-container">
                            <input type="file" accept="image/*" multiple bind:files={upload} on:change={handleImageUpload}/>
                        </div>
                    </div>
                    <label for="images"></label>
                </div>

                <div class="form-floating mb-3 mt-3">
                    <div id="rating">
                        {#each Array(5) as _, index}
                            <button on:click={() => handleStarClick(index)}
                                    on:mouseover={() => handleStarMouseOver(index)}
                                    on:focus={() => handleStarMouseOver(index)}
                                    on:mouseleave={handleStarMouseOut}
                                    on:blur={handleStarMouseOut}
                                    type="button">
                                <img bind:this={ratingStars[index]} src="/icons/star.png" alt="star" />
                            </button>
                        {/each}
                    </div>
                    <label for="rating"></label>
                </div>

                <div class="form-floating mb-3 mt-3">
                    <textarea id="content" name="content" class="form-control" type="text"
                              bind:this={contentInput} bind:value={content} required></textarea>
                    <label for="content">리뷰</label>
                </div>
            </fieldset>
            <button type="submit" disabled={!location || !content || previewSources.length == 0}>작성</button>
        </form>
    </main>
{/if}

<style>

    label {
        font-family: D2Coding, sans-serif;
    }

    #rating {
        display: flex;
        flex-direction: row;
        justify-content: space-between;
        width: 100%;
    }
    #rating button {
        background-color: transparent;
        border: none;
        padding: 0;
        margin: 0;
    }
    #rating img {
        max-width: 100%;
        height: auto;
    }

    #images {
        display: flex;
        flex-wrap: wrap;
        justify-content: start;
        background-color: rgba(211, 211, 211, 0.7);
        margin: 0;
        border-radius: 10px;
        border: 2px solid #ccc;
        padding: 10px;
    }
    #images .image-container {
        position: relative;
        display: inline-block;
    }
    #images .delete-button {
        width: 25px;
        height: 25px;
        margin: 0;
        padding: 0;
        position: absolute;
        background-image: url('../../lib/asset/minus.svg');
        background-size: 25px 25px;
        background-repeat: no-repeat;
        z-index: 2;
        cursor: pointer;
        border: none;
        background-color: transparent;
    }
    #images img {
        display: inline-block;
        width: 50px;
        height: 50px;
        object-fit: cover;
        margin: 5px;
        position: relative;
    }
    #images input[type=file] {
        background-image: url('../../lib/asset/plus.svg');
        background-size: 25px 25px;
        background-repeat: no-repeat;
        background-position: center;
        text-indent: -9999px;
        border: none;
        padding: 0 10px;
        margin: 5px;
        cursor: pointer;
        width: 50px;
        height: 50px;
    }

    #content {
        min-height: 250px;
    }

    form button {
        margin-top: 1rem;
        padding: 0.5rem;
        width: 100%;
        background-color: #007bff;
        color: #fff;
        border: none;
        border-radius: 0.25rem;
        cursor: pointer;
        text-align: center;
    }

    button:disabled {
        background-color: #ccc;
        color: #666;
        cursor: not-allowed;
        opacity: 0.6;
    }

</style>