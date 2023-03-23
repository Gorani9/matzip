import type {PageLoad} from "../../../../../.svelte-kit/types/src/routes";

export const load = (({ params, url }) => {

    return { param: params.username, query: url.searchParams };

}) satisfies PageLoad;