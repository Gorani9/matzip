<script lang="ts">

    import {User} from "../dto/User";
    import {goto} from "$app/navigation";

    export let user: User;
    export let markerSize: number = 30;

</script>

<main>
    <div on:click={() => goto("/users/" + user.username)}
         on:keydown={e => e.key === 'Enter' && goto("/users/" + user.username)}>
        <img class="user-image"
             src={user.profileImageUrl ?? "/icons/anonymous.png"} alt={"user image"} style="--size: {markerSize}px">
        <p class="username" style="--size: {markerSize / 2}px">{user.username}</p>
    </div>
</main>

<style>
    div {
        display: flex;
        align-items: center;
    }

    div:hover {
        cursor: pointer;
    }

    .user-image {
        width: var(--size);
        height: var(--size);
        border-radius: 50%;
        margin-right: 10px;
        padding: 2px;
        border: 1px solid rgba(0, 0, 0, 1);
    }

    .username {
        font-family: 'D2Coding Bold', sans-serif;
        font-size: var(--size);
        margin-right: 10px;
        margin-bottom: 0;
    }

</style>