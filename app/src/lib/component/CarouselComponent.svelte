<script lang="ts">

    import {onMount} from "svelte";

    export let images: string[];

    let activeSlideIndex = 0;
    let startX = 0;
    let carouselTrack;

    onMount(() => {
        carouselTrack.style.setProperty('--total-slides', images.length.toString());
        slideCarouselTrack(0);
    })

    const slideCarouselTrack = (index: number) => {
        carouselTrack.style.setProperty('--active-slide-index', index.toString());
    };

    const disableVerticalScroll = () => {
        document.body.style.overflow = 'hidden';
    };

    const enableVerticalScroll = () => {
        document.body.style.overflow = 'auto';
    };

    const disableTransition = () => {
        carouselTrack.style.transition = 'none';
    };

    const enableTransition = () => {
        carouselTrack.style.transition = 'transform 0.3s ease-in-out';
    };

    const handleNextSlide = () => {
        activeSlideIndex = activeSlideIndex === images.length - 1 ? 0 : activeSlideIndex + 1
        slideCarouselTrack(activeSlideIndex);
    };

    const handlePrevSlide = () => {
        activeSlideIndex = activeSlideIndex === 0 ? images.length - 1 : activeSlideIndex - 1;
        slideCarouselTrack(activeSlideIndex);
    };

    const handleDotClick = (index: number) => {
        activeSlideIndex = index;
        slideCarouselTrack(activeSlideIndex);
    };

    const handleTouch = (event) => {
        if (event.type === 'touchstart') {
            startX = event.touches[0].clientX;
            disableTransition();
        } else if (event.type === 'touchmove') {
            const endX = event.touches[0].clientX;
            let distance = (endX - startX) / carouselTrack.clientWidth;

            if (Math.abs(distance) > 0.1) disableVerticalScroll();
            else return;

            const leftDistanceLimit = activeSlideIndex === 0 ? 0.3 : 1;
            const rightDistanceLimit = activeSlideIndex === images.length - 1 ? 0.3 : 1;
            distance = Math.min(Math.max(-rightDistanceLimit, distance), leftDistanceLimit);

            slideCarouselTrack(activeSlideIndex - distance);
        } else if (event.type === 'touchend') {
            const endX = event.changedTouches[0].clientX;
            let distance = (endX - startX) / carouselTrack.clientWidth;

            activeSlideIndex = activeSlideIndex - Math.round(distance);
            activeSlideIndex = Math.min(Math.max(0, activeSlideIndex), images.length - 1);
            enableTransition();
            enableVerticalScroll();
            slideCarouselTrack(activeSlideIndex);
        }
    }

</script>

<main>
    <div class="review-carousel" on:touchstart={handleTouch} on:touchmove={handleTouch} on:touchend={handleTouch}>
        {#if images.length > 1}
            <button class="carousel-prev" on:click={handlePrevSlide}></button>
        {/if}
        <div class="carousel-track" bind:this={carouselTrack}>
            {#each images as image}
                <div class="carousel-item">
                    <img src={image} alt={"image"}>
                </div>
            {/each}
        </div>
        {#if images.length > 1}
            <button class="carousel-next" on:click={handleNextSlide}></button>
        {/if}
    </div>
    <ul class="carousel-dots">
        {#if images.length > 1}
            {#each images as _, i}
                <div class="carousel-dot" class:current={activeSlideIndex === i}
                     on:click={() => handleDotClick(i)}
                     on:keydown={e => e.key === 'Enter' && handleDotClick(i)}></div>
            {/each}
        {/if}
    </ul>
</main>

<style>
    .review-carousel {
        position: relative;
        display: flex;
        flex-direction: row;
        align-items: center;
        overflow: hidden;
    }

    .carousel-prev, .carousel-next {
        position: absolute;
        z-index: 1;
        padding: 0;
        border: none;
        cursor: pointer;
        color: white;
        opacity: 0.4;

        width: 30px;
        height: 30px;
        border-radius: 50%;
        background-repeat: no-repeat;
        background-size: contain;
        box-shadow: 0 0 5px rgba(0, 0, 0, 0.3);
    }

    .carousel-prev {
        margin-left: 10px;
        left: 0;
        background-image: url('$lib/asset/angle-small-left.svg');
    }

    .carousel-next {
        margin-right: 10px;
        right: 0;
        background-image: url('$lib/asset/angle-small-right.svg');
    }

    .carousel-track {
        display: flex;
        width: calc(100% * var(--total-slides));
        transform: translateX(calc(var(--active-slide-index) * -100%));
        transition: transform 0.3s ease-in-out;
    }

    .carousel-item {
        display: block;
        flex-shrink: 0;
        width: 100%;
        height: 0;
        padding-bottom: 75%;
        margin-right: 0;
        position: relative;
    }

    .carousel-item img {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        object-fit: contain;
    }

    .carousel-dots {
        width: 100%;
        height: 20px;
        display: flex;
        justify-content: center;
        margin-top: 10px;
        z-index: 1;
    }

    .carousel-dot {
        width: 10px;
        height: 10px;
        border-radius: 50%;
        margin-right: 10px;
        background-color: rgba(0, 0, 0, 0.2);
        border: none;
        cursor: pointer;
        /* space between items = 10px */
        flex: 0 0 calc(100% / var(--total-slides) - 10px);
    }

    .carousel-dot:hover {
        width: 15px;
        height: 15px;
        background-color: rgba(0, 0, 0, 0.4);
    }

    .carousel-dot.current {
        background-color: rgba(0, 0, 0, 0.6);
    }
</style>