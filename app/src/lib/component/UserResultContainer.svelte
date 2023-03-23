<script lang="ts">

    import {User} from "../dto/User";
    import {goto} from "$app/navigation";

    export let user: User;

    let maxLength = 20;
    const truncProfileString = (profileString: string) => {
        if (!profileString) return '프로필이 없습니다.';
        if (profileString.length > maxLength) {
            return profileString.substring(0, maxLength) + "...";
        } else {
            return profileString;
        }
    }

</script>

<main on:click={() => goto("/users/" + user.username)}
      on:keydown={(e) => e.key === 'Enter' && goto("/users/" + user.username)}>
    <div class="user-image">
        <img src={user.profileImageUrl ?? "/icons/anonymous.png"} alt="user">
    </div>
    <div class="container">
        <div class="container-top">
            <p>{user.username}</p>
            <div class="user-level">
                <img src="/icons/user-level.png" alt="user-level">
                <p>{user.matzipLevel}</p>
            </div>
            <div class="user-followers">
                <img src="/icons/user-follower.png" alt="follower-count">
                <p>{user.numberOfFollowers}</p>
            </div>
        </div>
        <div class="container-bottom">
            <p>{truncProfileString(user.profileString)}</p>
        </div>
    </div>
</main>

<style>

    main {
        display: flex;
        flex-direction: row;
        width: 100%;
        height: 100%;
        border-radius: 10px;
        box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
        background-color: white;
    }

    main:hover {
        cursor: pointer;
    }

    .user-image {
        width: 30%;
        overflow: hidden;
        display: flex;
        justify-content: center;
        align-items: center;
        border-radius: 10px 0 0 10px;
        position: relative;
    }

    .user-image:before {
        content: "";
        display: block;
        padding-top: 100%;
    }

    .user-image img {
        position: absolute;
        width: 100%;
        border-radius: 10px 0 0 10px;
        object-fit: cover;
    }

    .container {
        padding: 0.5rem;
    }

    .container-top {
        display: flex;
        align-items: center;
    }

    .container-top p {
        font-size: 20px;
        font-family: 'D2Coding', sans-serif;
        margin: 0 0 0 0;
    }

    .container-top .user-level {
        display: flex;
        align-items: center;
        margin: 0 0 0 1rem;
    }

    .container-top .user-level img {
        width: 20px;
        height: 20px;
        margin: 0 0.5rem 0 0;
    }

    .container-top .user-level p {
        font-size: 15px;
        font-family: 'D2Coding', sans-serif;
        margin: 0 0 0 0;
    }

    .container-top .user-followers {
        display: flex;
        align-items: center;
        margin: 0 0 0 1rem;
    }

    .container-top .user-followers img {
        width: 20px;
        height: 20px;
        margin: 0 0.5rem 0 0;
    }

    .container-top .user-followers p {
        font-size: 15px;
        font-family: 'D2Coding', sans-serif;
        margin: 0 0 0 0;
    }

    .container-bottom p {
        font-size: 15px;
        font-family: 'D2Coding', sans-serif;
        color: #808080;
        margin: 8px 0 0 0;
    }

</style>