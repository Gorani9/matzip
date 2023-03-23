<script lang="ts">

    import {ME, User} from "../dto/User";
    import {API} from "../api";
    import {dialogs, getClose} from "svelte-dialogs";
    import {onMount} from "svelte";

    const close = getClose();

    let user: User | null = null;

    let profileInput;
    let upload;
    let fileInput;
    let source;
    let profileString;

    ME.subscribe((value) => {
        user = value;
        source = value?.profileImageUrl ?? "/icons/anonymous.png";
        profileString = value?.profileString ?? '';
    });

    onMount(() => profileInput.focus());

    const handleAddImage = () => {
        fileInput.click();
    };

    const handleFileChange = () => {
        const reader = new FileReader();
        reader.onload = (event) => {
            source = event.target.result;
        };
        reader.readAsDataURL(upload[0]);
    };

    const handlePatchMe = async () => {
        console.log(upload);
        const response = await API.patchMe(
            upload && source != user?.profileImageUrl ? upload[0] : null,
            profileString != user?.profileString ? profileString : null
        );

        if (response.ok) {
            const json = await response.json();
            ME.update(() => User.fromJson(json));

            close("Success");
        } else {
            await response.json();
            await dialogs.error('프로필 수정에 실패했습니다. 다시 시도해주세요.');
        }
    };

</script>

<input type="file" bind:this={fileInput} bind:files={upload} style="display: none;" on:change={handleFileChange} />


<main>

    <div class="user-marker-container">
        <!-- User Profile Image -->
        <div class="image-container" on:click={handleAddImage} on:keydown={e => e.key === 'Enter' && handleAddImage()}>
            <img class="user-image" src={source} alt="preview" />
        </div>
        <!-- User Name -->
        <p class="username">{user.username}</p>
    </div>

    <form on:submit|preventDefault={handlePatchMe}>
        <div class="form-floating mb-3 mt-3">
            <input id="profileString" name="profileString" class="form-control" type="text" bind:this={profileInput}
                   bind:value={profileString} required={false}/>
            <label for="profileString">프로필 메세지</label>
        </div>
        <button type="submit" disabled={!upload && !profileString}>작성</button>
    </form>

</main>

<style>

    .user-marker-container {
        width: 100%;
        display: flex;
        align-items: center;
    }

    .image-container:hover {
        cursor: pointer;
    }

    .user-image {
        width: 75px;
        height: 75px;
        border-radius: 50%;
        margin-right: 10px;
        padding: 2px;
        border: 1px solid rgba(0, 0, 0, 1);
    }

    .username {
        font-family: 'D2Coding Bold', sans-serif;
        font-size: 50px;
        margin-left: 10px;
        margin-right: 10px;
        margin-bottom: 0;
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