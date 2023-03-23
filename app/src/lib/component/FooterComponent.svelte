<script lang="ts">
    import {goto} from "$app/navigation";
</script>

<main>
    <div class="tab">
        <button on:click={() => goto("/")} on:keydown={(e) => e.key === 'Enter' && goto('/')}>
            <img src="/icons/post.png" alt="reviews">
        </button>
        <button on:click={() => goto("/search")} on:keydown={(e) => e.key === 'Enter' && goto('/search')}>
            <img src="/icons/search.png" alt="search">
        </button>
        <button on:click={() => goto("/me")} on:keydown={(e) => e.key === 'Enter' && goto('/me')}>
            <img src="/icons/my.png" alt="user">
        </button>
    </div>
</main>

<style>
    .tab {
        display: flex;
        flex-direction: row;
        justify-content: space-around;
        width: 100%;
        padding: 10px 0;
    }

    .tab button {
        border: none;
        background: none;
        cursor: pointer;
        padding: 0;
    }

    .tab img {
        width: 50px;
        height: 50px;
        filter: brightness(0.2);
    }

    .tab button:hover img {
        filter: brightness(0.5);
    }

    .tab button:active img {
        filter: brightness(0.2);
    }

    .tab button:disabled img {
        filter: brightness(0.2);
    }

    .tab button:disabled:hover img {
        filter: brightness(0.2);
    }
</style>
