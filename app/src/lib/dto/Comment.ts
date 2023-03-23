import {User} from "./User";
export class Comment {
    public constructor(
        public id: number,
        public createdAt: Date,
        public modifiedAt: Date,
        public user: User,
        public reviewId: number,
        public content: string,
        public isMine: boolean,
    ) {}

    public static fromJson(json: any): Comment {
        return new Comment(
            json.id,
            new Date(json.created_at),
            new Date(json.modified_at),
            User.fromJson(json.user),
            json.review_id,
            json.content,
            json.is_mine,
        );
    }

    public static fromJsonArray(jsonArray: any[]): Comment[] {
        if (!jsonArray) return new Array<Comment>();
        else return jsonArray.map(Comment.fromJson);
    }
}