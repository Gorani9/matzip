<script lang="ts">

    import {onMount} from "svelte";
    import {browser} from "$app/environment";
    import {goto} from "$app/navigation";
    import LoginModal from "../modal/LoginModal.svelte";
    import {dialogs} from "svelte-dialogs";
    import {API} from "../api";
    import UsernameModal from "../modal/UsernameModal.svelte";
    import PasswordModal from "../modal/PasswordModal.svelte";
    import PatchMeModal from "../modal/PatchMeModal.svelte";
    import {ME, TOKEN, User} from "../dto/User";

    let popupElement;
    let menuVisible = false;

    let me: User | null;
    let token: string | null;

    TOKEN.subscribe(value => {
        token = value;
    });

    ME.subscribe(value => {
        me = value;
    });

    const toggleMenu = () => {
        menuVisible = !menuVisible;
    };

    const handleClickOutside = (event) => {
        if (!popupElement?.contains(event.target)
            && !event.target.classList.contains("menu-button")
            && !event.target.classList.contains("menu-button-img")) {
            menuVisible = false;
        }
    };

    onMount(async () => {
        sessionStorage.getItem("token") && TOKEN.update(() => sessionStorage.getItem("token"));
        const response = await API.fetchMe();

        if (response.ok) {
            const json = await response.json();
            ME.update(() => User.fromJson(json));
        } else {
            ME.update(() => null);
            localStorage.clear();
        }

        if (browser) {
            document.addEventListener("click", handleClickOutside);
        }

        return () => {
            if (browser) {
                document.removeEventListener("click", handleClickOutside);
            }
        };
    });

    const handleLogin = async () => {
        await dialogs.modal(LoginModal, { redirect: null, reload: true });
        toggleMenu();
    }

    const handleLogout = async () => {
        const ret = await dialogs.confirm({
            title: "로그아웃 하시겠습니까?",
            confirmButtonText: "확인",
            declineButtonText: "취소"
        });

        if (!ret) return;

        await API.logout();

        ME.update(() => null);

        location.reload();
        toggleMenu();
    }

    const handleChangeUsername = async () => {
        toggleMenu();
        await dialogs.modal(UsernameModal);
    }

    const handleChangePassword = async () => {
        toggleMenu();
        await dialogs.modal(PasswordModal);
    }

    const handleDeleteAccount = async () => {
        const ret = await dialogs.confirm({
            title: "정말로 계정을 삭제하시겠습니까?",
            text: "삭제하면 복구할 수 없습니다.",
            confirmButtonText: "삭제",
            declineButtonText: "취소"
        });

        if (ret) {

            await API.deleteMe();

            ME.update(() => null);
            await goto('/');
        }
    }

    const handlePatchMe = async () => {
        await dialogs.modal(PatchMeModal);
        toggleMenu();
    }

</script>

<main>
    <h1
            aria-label="Home"
            role="button"
            on:keydown="{(e) => e.key === 'Enter' && goto('/')}"
            on:click="{() => goto('/')}"
            class="title"
    >
        MATZIP
    </h1>
    <button class="menu-button" on:click={toggleMenu}>
        <img src="/icons/menu.png" alt="menu" class="menu-button-img"/>
    </button>
    {#if menuVisible}
        <div class="popup" bind:this={popupElement}>
            {#if me}
                <!-- Logout Button -->
                <button on:click={handleLogout}>로그아웃</button>
                <!-- Line Separator -->
                <hr/>
                <!-- Patch Me Button -->
                <button on:click={handlePatchMe}>계정 정보 변경</button>
                <!-- Change Username Button -->
                <button on:click={handleChangeUsername}>아이디 변경</button>
                <!-- Change Password Button -->
                <button on:click={handleChangePassword}>비밀번호 변경</button>
                <!-- Line Separator -->
                <hr/>
                <!-- Delete Account Button -->
                <button on:click={handleDeleteAccount}>계정 삭제</button>
            {:else}
                <!-- Login Button -->
                <button on:click={handleLogin}>로그인</button>
            {/if}
        </div>
    {/if}
</main>

<style>

    main {
        display: flex;
        position: relative;
        justify-content: space-between;
        align-items: center;
        padding: 0 20px;
        height: 50px;
        box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
        border: 2px solid #e0e0e0;
    }

    .title {
        font-weight: 700;
        color: #333;
        padding-top: 5px;
        cursor: pointer;
    }

    .title:focus {
        outline: 2px solid #66bfbf;
    }

    h1 {
        font-weight: 700;
        color: #333;
        padding-top: 5px;
    }

    h1:hover {
        cursor: pointer;
    }

    .menu-button {
        position: absolute;
        right: 0;
        top: 0;
        padding: 10px;
        border: none;
        background: none;
        cursor: pointer;
    }

    .menu-button-img {
        width: 30px;
        height: 30px;
    }

    .popup {
        position: absolute;
        top: 50px;
        right: 5px;
        width: 150px;
        background-color: #f5f5f5;
        box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
        border-radius: 5px;
        padding: 10px;
        z-index: 1001;
    }

    .popup button {
        width: 100%;
        padding: 5px;
        border: none;
        background-color: #f5f5f5;
        cursor: pointer;
    }

    .popup button:hover {
        background-color: #e0e0e0;
    }

</style>