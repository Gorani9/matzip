<script lang="ts">

    export let createdAt: Date;
    export let modifiedAt: Date;
    export let fontSize: number;

    const isSameDate = (date1: Date, date2: Date) => {
        const timeDiff = date2.getTime() - date1.getTime();
        return timeDiff < 1000;
    }

    const timeDifferenceFromNow = (time: Date) => {
        const difference = Date.now() - time.getTime();
        const seconds = difference / 1000;
        const minutes = seconds / 60;
        const hours = minutes / 60;
        const days = hours / 24;
        const weeks = days / 7;
        const months = days / 30;
        const years = days / 365;

        if (seconds < 60) {
            return `${Math.round(seconds)}초`;
        } else if (minutes < 60) {
            return `${Math.round(minutes)}분`;
        } else if (hours < 24) {
            return `${Math.round(hours)}시간`;
        } else if (days < 7) {
            return `${Math.round(days)}일`;
        } else if (weeks < 4) {
            return `${Math.round(weeks)}주`;
        } else if (months < 12) {
            return `${Math.round(months)}개월`;
        } else {
            return `${Math.round(years)}년`;
        }
    }

</script>

<main class="time-marker">
    {#if isSameDate(createdAt, modifiedAt)}
        <p style="--fontSize: {fontSize}px">{timeDifferenceFromNow(createdAt)}</p>
    {:else}
        <p style="--fontSize: {fontSize}px">Edited {timeDifferenceFromNow(modifiedAt)}</p>
    {/if}
</main>

<style>

    .time-marker {
        display: flex;
        align-items: center;
        margin-left: 5px;
        margin-bottom: 0;
    }

    .time-marker p {
        font-family: 'D2Coding', sans-serif;
        font-size: var(--fontSize);
        color: rgba(0, 0, 0, 0.5);
        margin-left: 5px;
        margin-bottom: 0;
    }

</style>