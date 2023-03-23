import {Review} from "./Review";
import {Comment} from "./Comment";
import { writable } from 'svelte/store';

export class User {
    public constructor(
        public username: string,
        public createdAt: Date,
        public profileImageUrl: string,
        public profileString: string,
        public matzipLevel: number,
        public isMyFollower: boolean,
        public isMyFollowing: boolean,
        public isMe: boolean,
        public numberOfFollowers: number,
        public numberOfFollowings: number,
        public reviews: Review[],
        public myFollowers: User[],
        public myFollowings: User[],
        public likes: Review[],
        public scraps: Review[],
        public comments: Comment[]
    ) {}

    public static fromJson(json: any): User {
        return new User(
            json.username,
            new Date(json.created_at),
            json.profile_image_url,
            json.profile_string,
            json.matzip_level,
            json.is_my_follower,
            json.is_my_following,
            json.is_me,
            json.number_of_followers,
            json.number_of_followings,
            Review.fromJsonArray(json.reviews?.data),
            User.fromJsonArray(json.my_followers?.data),
            User.fromJsonArray(json.my_followings?.data),
            Review.fromJsonArray(json.hearted_reviews?.data),
            Review.fromJsonArray(json.scraps?.data),
            Comment.fromJsonArray(json.comments?.data)
        );
    }

    public static fromJsonArray(jsonArray: any[]): User[] {
        if (!jsonArray) return new Array<User>();
        else return jsonArray.map(User.fromJson);
    }
}

export const ME = writable<User | null>(null);
export const TOKEN = writable<string | null>(null);