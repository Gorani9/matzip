export class Restaurant {
    constructor(
        public location: string,
    ) {}

    public static fromJson(json: any): Restaurant {
        return new Restaurant(
            json
        );
    }
}