import {Restaurant} from "./Restaurant";
import {User} from "./User";
import {Comment} from "./Comment";

export class Review {
    public constructor(
        public id: number,
        public createdAt: Date,
        public modifiedAt: Date,
        public user: User,
        public content: string,
        public imageUrls: string[],
        public rating: number,
        public restaurant: Restaurant,
        public views: number,
        public comments: Comment[],
        public numberOfLikes: number,
        public numberOfScraps: number,
        public isDeletable: boolean,
        public isLiked: boolean,
        public isScrapped: boolean,
        public scrapDescription: string,
    ) {}

    public static fromJson(json: any): Review {
        // return new Review object: json naming property is snake case
        return new Review(
            json.id,
            new Date(json.created_at),
            new Date(json.modified_at),
            User.fromJson(json.user),
            json.content,
            json.image_urls,
            json.rating,
            Restaurant.fromJson(json.restaurant),
            json.views,
            Comment.fromJsonArray(json.comments),
            json.number_of_hearts,
            json.number_of_scraps,
            json.is_deletable,
            json.is_hearted,
            json.is_scraped,
            json.scrap_description,
        );
    }
    public static fromJsonArray(jsonArray: any[]): Review[] {
        if (!jsonArray) return new Array<Review>();
        else return jsonArray.map(Review.fromJson);
    }
}