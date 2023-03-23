export const formatNumbers = (views: number) => {
    if (views > 100000) {
        return (views / 1000000).toFixed(1) + "M";
    } else if (views > 1000) {
        return (views / 1000).toFixed(1) + "K";
    } else {
        return views;
    }
}