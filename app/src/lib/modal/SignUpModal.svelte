<script lang="ts">
    import {onMount} from 'svelte';
    import {goto} from '$app/navigation';
    import {API} from '$lib/api';
    import {dialogs, getClose} from "svelte-dialogs";
    import {ME, TOKEN, User} from "../dto/User";
    import LoadingComponent from "$lib/component/LoadingComponent.svelte";

    const close = getClose();

    export let redirect = '/';
    export let reload: boolean = false;

    let loading = false;

    let usernameInput;
    let passwordInput;
    let confirmPasswordInput;
    let username = '';
    let password = '';
    let confirmPassword = '';
    let isValidUsername = false;
    let isValidPassword = false;
    let isValidConfirmPassword = false;

    const handleUsernameChange = async ()  => {
        const invalidUsername = !/^(?!.*\.{2})(?!.*\.$)[\w.]{1,30}$/.test(username);
        const json = await (await API.checkUsername(username)).json();
        const usernameExists = json.result;

        isValidUsername = !!username && !invalidUsername && !usernameExists;
    };

    const handleConfirmPasswordChange = () => {
        isValidConfirmPassword = password === confirmPassword;
    };

    const handlePasswordChange = async () => {
        isValidPassword = /^(?=.*?[a-zA-Z])(?=.*?[0-9])(?=.*?([^\w\s]|_)).{8,20}$/.test(password);

        if (confirmPassword) {
            handleConfirmPasswordChange();
        }
    };

    const handleSubmit = async () => {

        loading = true;
        const response = await API.signup(username, password);
        loading = false;

        if (response.ok) {
            close("Signup Success");

            const tokenJson = await response.json();
            TOKEN.update(() => tokenJson.token);

            const json = await (await API.fetchMe()).json();
            ME.update(() => User.fromJson(json));

            if (redirect) await goto(redirect);
            else if (reload) {
                localStorage.setItem("token", tokenJson.token);
                location.reload();
                TOKEN.update(() => localStorage.getItem("token"));
                localStorage.removeItem("token");
            }
        } else {
            await dialogs.error("회원가입에 실패했습니다. 다시 시도해주세요.");
            usernameInput.focus();
        }
    };

    onMount(() => usernameInput.focus());

</script>

{#if loading}
    <LoadingComponent />
{:else}
    <main>
        <div class="container">
            <h1>회원가입</h1>
            <form on:submit|preventDefault={handleSubmit} class="needs-validation">
                <div class="form-floating mb-3 mt-3">
                    <input
                            type="text"
                            id="username"
                            name="username"
                            class="form-control {username ? (isValidUsername ? 'is-valid' : 'is-invalid') : ''}"
                            placeholder="Username"
                            bind:value={username}
                            bind:this={usernameInput}
                            on:input={handleUsernameChange}
                            required
                            autocomplete="off"
                    />
                    <label for="username">아이디</label>
                </div>

                <div class="form-floating mb-3 mt-3">
                    <input
                            type="password"
                            id="password"
                            name="password"
                            class="form-control {password ? (isValidPassword ? 'is-valid' : 'is-invalid') : ''}"
                            placeholder="Password"
                            bind:value={password}
                            bind:this={passwordInput}
                            on:input={handlePasswordChange}
                            required
                    />
                    <label for="password">비밀번호</label>
                </div>

                <div class="form-floating mb-3 mt-3">
                    <input
                            type="password"
                            id="confirm-password"
                            name="confirm-password"
                            class="form-control {isValidPassword ? (isValidConfirmPassword ? 'is-valid' : 'is-invalid') : ''}"
                            placeholder="Confirm Password"
                            bind:value={confirmPassword}
                            bind:this={confirmPasswordInput}
                            on:input={handleConfirmPasswordChange}
                            required
                    />
                    <label for="confirm-password">비밀번호 확인</label>

                </div>

                <button type="submit" disabled={!isValidUsername || !isValidPassword || !isValidConfirmPassword}>
                    가입
                </button>
            </form>
        </div>
    </main>
{/if}

<style>
    main {
        max-width: 400px;
        margin: 0 auto;
        padding: 2rem;
        border: 1px solid #ccc;
        border-radius: 0.5rem;
        background-color: #fff;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
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
        padding: 0.5rem;
        border: 1px solid #ccc;
        border-radius: 0.25rem;
        background-color: #f7f7f7;
        color: #333;
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

    button:disabled {
        background-color: #ccc;
        color: #666;
        cursor: not-allowed;
        opacity: 0.6;
    }

</style>
