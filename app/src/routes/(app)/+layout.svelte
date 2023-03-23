<script lang="ts">

    import FooterComponent from "$lib/component/FooterComponent.svelte";
    import HeaderComponent from "$lib/component/HeaderComponent.svelte";

</script>

<main class="main-frame">

    <div class="header-wrapper">
        <HeaderComponent />
    </div>
    <div class="slot">
        <slot />
    </div>
    <div class="footer-wrapper">
        <FooterComponent />
    </div>

</main>

<style>

    @font-face {
        font-family: 'D2Coding';
        src: url('$lib/asset/D2Coding/D2Coding.ttf') format('truetype');
        font-style: normal;
        font-weight: normal;
    }

    @font-face {
        font-family: 'D2Coding Bold';
        src: url('$lib/asset/D2Coding/D2CodingBold.ttf') format('truetype');
        font-style: normal;
        font-weight: bold;
    }

    .main-frame {
        /* Set width to 100%, at maximum 600px */
        width: 100%;
        min-width: 420px;
        max-width: 600px;
        /* align for the center */
        margin: 0 auto;
        /* Set font */
        font-family: 'D2Coding', sans-serif;
        font-size: 16px;
        /* Set background color */
        background-color: #f5f5f5;
        border: 2px solid #e0e0e0;
        padding-bottom: 70px;
    }

    .slot {
        min-height: calc(100vh - 60px);
        padding: 70px 20px 0;
    }

    .header-wrapper {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        z-index: 1000; /* Ensure the header is always on top */
        width: 100%;
        min-width: 420px;
        max-width: 600px;
        margin: 0 auto;
        background-color: #f5f5f5; /* Match the background color of the main content */
    }

    .footer-wrapper {
        width: 100%;
        min-width: 420px;
        max-width: 600px;
        margin: 0 auto;
        position: fixed;
        bottom: 0;
        left: 0;
        right: 0;
        background-color: #ffffff;
        border: 2px solid #e0e0e0;
    }

</style>
