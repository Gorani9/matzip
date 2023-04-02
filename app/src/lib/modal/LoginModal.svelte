<script lang="ts">
    import {goto} from '$app/navigation';
    import {API} from '$lib/api';
    import {dialogs, getClose} from "svelte-dialogs";
    import SignUpModal from "$lib/modal/SignUpModal.svelte";
    import {onMount} from "svelte";
    import {ME, TOKEN, User} from "../dto/User";
    import LoadingComponent from "$lib/component/LoadingComponent.svelte";

    const close = getClose();

    export let redirect = '/';
    export let reload = false;

    let loading = false;

    let usernameInput;
    let username = '';
    let password = '';

    const handleLogin = async () => {

        loading = true;
        const response = await API.login(username, password);
        loading = false;

        if (response.ok) {
            close("Login Success");

            const tokenJson = await response.json();
            TOKEN.update(() => tokenJson.token);

            const json = await (await API.fetchMe()).json();
            ME.update(() => User.fromJson(json));

            if (redirect) await goto(redirect);
            else if (reload) {
                sessionStorage.setItem("token", tokenJson.token);
                location.reload();
            }
        } else {
            await dialogs.error("로그인에 실패했습니다. 다시 시도해주세요.");
            usernameInput.focus();
        }
    };

    const handleSignup = async () => {
        const ret = await dialogs.modal(SignUpModal, { redirect: redirect, reload: reload });

        if (ret) close(ret);
    };

    onMount(() => usernameInput.focus());

</script>


{#if loading}
    <LoadingComponent />
{:else}
    <main>
        <h1>로그인</h1>
        <form on:submit|preventDefault={handleLogin}>
            <div class="form-floating mb-3 mt-3">
                <input
                        type="text"
                        id="username"
                        class="form-control"
                        placeholder="Username"
                        bind:value={username}
                        bind:this={usernameInput}
                        required
                />
                <label for="username">아이디</label>
            </div>

            <div class="form-floating mb-3 mt-3">
                <input
                        type="password"
                        id="password"
                        class="form-control"
                        placeholder="Password"
                        bind:value={password}
                        required
                />
                <label for="password">비밀번호</label>
            </div>

            <div class="button-group">
                <button type="submit">로그인</button>
                <button on:click|preventDefault={handleSignup}>회원가입</button>
            </div>
        </form>
    </main>
{/if}

<style>
    main {
        max-width: 400px;
        margin: 0 auto;
        padding: 2rem;
        border: 1px solid #ccc;
        border-radius: 0.5rem;
    }

    h1 {
        text-align: center;
        margin-bottom: 2rem;
    }

    form div {
        display: flex;
        flex-direction: column;
        margin-bottom: 1rem;
    }

    label {
        font-weight: bold;
        margin-bottom: 0.5rem;
    }

    input {
        width: 100%;
        padding: 0.5rem;
        border: 1px solid #ccc;
        border-radius: 0.25rem;
    }

    button {
        width: 100%;
        margin-top: 1rem;
        padding: 0.5rem;
        background-color: #007bff;
        color: #fff;
        border: none;
        border-radius: 0.25rem;
        cursor: pointer;
    }

    .button-group {
        display: flex;
        flex-direction: row;
        gap: 1rem;
    }
</style>
