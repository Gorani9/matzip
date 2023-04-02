import {TOKEN} from "./dto/User";

const DOMAIN = "https://dev.matzip-server.link"

let token: string | null = null;
TOKEN.subscribe((t) => token = t);

export const API = {
    "login": async (username: string, password: string) => {
        return await fetch(DOMAIN + "/api/v1/auth/login", {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username: username,
                password: password
            })
        })
    },
    "logout": async () => {
        sessionStorage.removeItem("token");
        return  await fetch(DOMAIN + "/api/v1/auth/logout", {
            credentials: 'include',
            method: 'POST'
        })
    },
    "signup": async (username: string, password: string) => {
        return await fetch(DOMAIN + "/api/v1/auth/signup", {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username: username,
                password: password
            })
        })
    },
    "refresh": async () => {
        return await fetch(DOMAIN + "/api/v1/auth/refresh", {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Authorization': token || ""
            }
        })
    },

    /* Search API */
    "getReviews": async () => {
        return await fetch(DOMAIN + "/api/v1/search/reviews?" + new URLSearchParams({
            "page": "0",
            "size": "100"
        }), {
            headers: {
                'Accept': 'application/json',
                'Authorization': token || ""
            }
        });
    },
    "searchUsers": async (
        username: string,
        sort: "username" | "level" | "followers",
        asc: boolean
    ) => {
        return await fetch(DOMAIN + "/api/v1/search/users?" + new URLSearchParams({
            "page": "0",
            "size": "100",
            "username": username,
            "sort": sort,
            "asc": asc.toString()
        }), {
            headers: {
                'Accept': 'application/json',
                'Authorization': token || ""
            }
        });
    },
    "searchReviews": async (
        keyword: string,
        sort: "username" | "level" | "followers" | "hearts" | "scraps" | "comments" | "rating",
        asc: boolean
    ) => {
        return await fetch(DOMAIN + "/api/v1/search/reviews?" + new URLSearchParams({
            "page": "0",
            "size": "100",
            "keyword": keyword,
            "sort": sort,
            "asc": asc.toString()
        }), {
            headers: {
                'Accept': 'application/json',
                'Authorization': token || ""
            }
        });
    },

    /* Me API */

    "fetchMe": async () => {
        return await fetch(DOMAIN + "/api/v1/me", {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Authorization': token || ""
            }
        })
    },
    "changeUsername": async (username: string) => {
        return await fetch(DOMAIN + "/api/v1/me/username", {
            method: 'PUT',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'Authorization': token || ""
            },
            body: JSON.stringify({
                username: username
            })
        });
    },
    "changePassword": async (password: string) => {
        return await fetch(DOMAIN + "/api/v1/me/password", {
            method: 'PUT',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'Authorization': token || ""
            },
            body: JSON.stringify({
                password: password
            })
        });
    },
    "patchMe": async (image: File, profile: string) => {
        const formData = new FormData();
        if (profile) formData.append('profile', profile);
        if (image) formData.append('image', image);

        return await fetch(DOMAIN + '/api/v1/me', {
            method: 'PATCH',
            headers: {
                'Accept': 'application/json',
                'Authorization': token || ""
            },
            body: formData
        });
    },
    "deleteMe": async () => {
        return await fetch(DOMAIN + "/api/v1/me", {
            method: 'DELETE',
            headers: {
                'Accept': 'application/json',
                'Authorization': token || ""
            }
        })
    },

    /* Review API */

    "postReview": async (content: string, rating: number, images: File[], restaurant: string) => {
        const formData = new FormData();
        formData.append("content", content);
        formData.append("rating", rating.toString());
        for (let i = 0; i < images.length; i++) formData.append("images", images[i]);
        formData.append("restaurant", restaurant);

        return await fetch(DOMAIN + "/api/v1/reviews", {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Authorization': token || ""
            },
            body: formData
        })
    },
    "fetchReview": async (reviewId: number | string) => {
        return await fetch(DOMAIN + "/api/v1/reviews/" + reviewId, {
            headers: {
                'Accept': 'application/json',
                'Authorization': token || ""
            }
        });
    },
    "patchReview": async (reviewId: number, images: File[], rating: number | null, content: string | null, oldURls: string[]) => {
        const formData = new FormData();
        if (content) formData.append("content", content);
        if (rating) formData.append("rating", rating.toString());
        if (images) for (let i = 0; i < images.length; i++) formData.append("images", images[i]);
        if (oldURls) for (let i = 0; i < oldURls.length; i++) formData.append("oldUrls", oldURls[i]);

        return await fetch(DOMAIN + "/api/v1/reviews/" + reviewId, {
            method: 'PATCH',
            headers: {
                'Accept': 'application/json',
                'Authorization': token || ""
            },
            body: formData
        })
    },
    "deleteReview": async (reviewId: number) => {
        return await fetch(DOMAIN + "/api/v1/reviews/" + reviewId, {
            method: 'DELETE',
            headers: {
                'Accept': 'application/json',
                'Authorization': token || ""
            }
        });
    },

    /* Review Interaction API */

    "putLike": async (reviewId: number) => {
        return await fetch(DOMAIN + "/api/v1/reviews/" + reviewId + "/heart", {
            method: 'PUT',
            headers: {
                'Accept': 'application/json',
                'Authorization': token || ""
            }
        });
    },
    "deleteLike": async (reviewId: number) => {
        return await fetch(DOMAIN + "/api/v1/reviews/" + reviewId + "/heart", {
            method: 'DELETE',
            headers: {
                'Accept': 'application/json',
                'Authorization': token || ""
            }
        });
    },
    "putScrap": async (reviewId: number, description: string) => {
        return await fetch(DOMAIN + "/api/v1/reviews/" + reviewId + "/scrap", {
            method: 'PUT',
            headers: {
                'Accept': 'application/json',
                'Authorization': token || "",
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                description: description
            })
        });
    },
    "deleteScrap": async (reviewId: number) => {
        return await fetch(DOMAIN + "/api/v1/reviews/" + reviewId + "/scrap", {
            method: 'DELETE',
            headers: {
                'Accept': 'application/json',
                'Authorization': token || ""
            }
        });
    },

    /* Comment API */

    "postComment": async (reviewId: number, content: string) => {
        return await fetch(DOMAIN + "/api/v1/comments", {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Authorization': token || "",
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                review_id: reviewId,
                content: content
            })
        });
    },
    "patchComment": async (commentId: number, content: string) => {
        return await fetch(DOMAIN + "/api/v1/comments/" + commentId, {
            method: 'PATCH',
            headers: {
                'Accept': 'application/json',
                'Authorization': token || "",
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                content: content
            })
        });
    },
    "deleteComment": async (commentId: number) => {
        return await fetch(DOMAIN + "/api/v1/comments/" + commentId, {
            method: 'DELETE',
            headers: {
                'Accept': 'application/json',
                'Authorization': token || ""
            }
        });
    },

    /* User API */

    "checkUsername": async (username: string) => {
        if (!username) return false;
        return await fetch(DOMAIN + "/api/v1/users/exists?" + new URLSearchParams({
            username: username
        }), {
            method: 'GET'
        })
    },
    "fetchUser": async (username: string) => {
        return await fetch(DOMAIN + "/api/v1/users/" + username, {
            headers: {
                'Accept': 'application/json',
                'Authorization': token || ""
            }
        });
    },
    "followUser": async (username: string) => {
        return await fetch(DOMAIN + "/api/v1/users/" + username + "/follow", {
            method: 'PUT',
            headers: {
                'Accept': 'application/json',
                'Authorization': token || ""
            }
        });
    },
    "unfollowUser": async (username: string) => {
        return await fetch(DOMAIN + "/api/v1/users/" + username + "/follow", {
            method: 'DELETE',
            headers: {
                'Accept': 'application/json',
                'Authorization': token || ""
            }
        });
    },
}
