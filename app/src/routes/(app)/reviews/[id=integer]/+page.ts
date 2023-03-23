import type {PageLoad} from "../../../../../.svelte-kit/types/src/routes";

export const load = (({ params, url }) => {

    return { param: params.id, query: url.searchParams };

}) satisfies PageLoad;