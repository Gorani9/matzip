<script lang="ts">

    import {onMount} from "svelte";
    import {dialogs} from "svelte-dialogs";
    import LoginModal from "$lib/modal/LoginModal.svelte";
    import {API} from "$lib/api";
    import UserPageComponent from "$lib/component/UserPageComponent.svelte";
    import {User} from "$lib/dto/User";
    import {goto} from "$app/navigation";
    import {ME} from "../../../lib/dto/User";
    import LoadingComponent from "$lib/component/LoadingComponent.svelte";

    let user: User | null = null;

    let loading = true;

    ME.subscribe(value => {
        user = value;
    });

    onMount(async () => {

        if (!user) {
            const ret = await dialogs.modal({
                content: LoginModal,
                props: {redirect: null, reload: false},
            });

            if (!ret) {
                await goto("/");
                return;
            }
        }

        loading = true;
        const response = await API.fetchMe();
        const json = await response.json();
        loading = false;

        user = User.fromJson(json);
    });

</script>

{#if loading}
    <LoadingComponent />
{:else}
    <main>
        <UserPageComponent bind:user={user} isMe={true} />
    </main>
{/if}