<script lang="ts">
    import { onMount } from 'svelte';
    import { API } from '$lib/api';
    import {dialogs, getClose} from "svelte-dialogs";
    import LoadingComponent from "$lib/component/LoadingComponent.svelte";

    const close = getClose();

    let loading = false;

    let passwordInput;
    let confirmPasswordInput;
    let password = '';
    let confirmPassword = '';
    let isValidPassword = false;
    let isValidConfirmPassword = false;

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
        const response = await API.changePassword(password);
        loading = false;

        if (response.ok) {
            close("Success");
        } else {
            await dialogs.error("비밀번호 변경에 실패했습니다. 다시 시도해주세요.");
            passwordInput.focus();
        }
    };

    onMount(() => passwordInput.focus());

</script>

{#if loading}
    <LoadingComponent />
{:else}
    <main>
        <div class="container">
            <h1>비밀번호 변경</h1>
            <form on:submit|preventDefault={handleSubmit} class="needs-validation">
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

                <button type="submit" disabled={!isValidPassword || !isValidConfirmPassword}>
                    변경
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
