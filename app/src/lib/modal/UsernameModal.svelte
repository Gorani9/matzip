<script lang="ts">
    import { onMount } from 'svelte';
    import { API } from '$lib/api';
    import {dialogs, getClose} from "svelte-dialogs";
    import {ME, User} from "../dto/User";

    const close = getClose();

    let usernameInput;
    let username = '';
    let isValidUsername = false;

    const handleUsernameChange = async ()  => {
        const invalidUsername = !/^(?!.*\.{2})(?!.*\.$)[\w.]{1,30}$/.test(username);
        const usernameExists = await API.checkUsername(username);

        console.log("usernameExists", usernameExists);

        isValidUsername = !!username && !invalidUsername && !usernameExists;
    };

    const handleSubmit = async () => {
        const response = await API.changeUsername(username);

        if (response.ok) {
            close("Success");

            const json = await response.json();
            ME.update(() => User.fromJson(json));

        } else {
            await dialogs.error("유저네임 변경에 실패했습니다. 다시 시도해주세요.");
            usernameInput.focus();
        }
    };

    onMount(() => {
        usernameInput.focus();
    });
</script>

<main>
    <div class="container">
        <h1>아이디 변경</h1>
        <form on:submit|preventDefault={handleSubmit} class="needs-validation">
            <div class="form-floating mb-3 mt-3">
                <input
                        type="text"
                        id="username"
                        name="username"
                        class="form-control {username ? (isValidUsername ? 'is-valid' : 'is-invalid') : ''}"
                        placeholder="username"
                        bind:value={username}
                        bind:this={usernameInput}
                        on:input={handleUsernameChange}
                        required
                        autocomplete="off"
                />
                <label for="username">아이디</label>
            </div>

            <button type="submit" disabled={!isValidUsername}>변경</button>
        </form>
    </div>
</main>

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
