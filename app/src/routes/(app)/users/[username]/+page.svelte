<script lang="ts">

    import PageData = App.PageData;
    import UserPageComponent from "$lib/component/UserPageComponent.svelte";
    import {User} from "../../../../lib/dto/User";
    import {onMount} from "svelte";
    import {API} from "../../../../lib/api";
    import {goto} from "$app/navigation";

    export let data: PageData;

    let user: User;
    let notFound: Boolean = false;

    onMount(async () => {
        const response = await API.fetchUser(data.param);

        if (response.ok) {
            const json = await response.json();
            user = User.fromJson(json);

            if (user.isMe) await goto("/me");

        } else if (response.status === 404) {
            notFound = true;
        }
    });

</script>

<main>
    {#if notFound}
        <div class="error">유저를 찾을 수 없습니다!</div>
    {:else}
        <UserPageComponent user={user} isMe={false}/>
    {/if}
</main>

<style>


</style>