create table user
(
    id             bigint auto_increment
        primary key,
    created_at     datetime(6)  null,
    modified_at    datetime(6)  null,
    blocked        bit          not null,
    blocked_reason varchar(255) null,
    deleted        bit          not null,
    deleted_date   datetime(6)  null,
    matzip_level   int          null,
    password       varchar(255) null,
    profile_string varchar(255) null,
    username       varchar(255) null,
    user_image_id  bigint       null,
    constraint UK_sb8bbouer5wak8vyiiy4pf2bx
        unique (username)
);

create table follow
(
    id          bigint auto_increment
        primary key,
    created_at  datetime(6) null,
    modified_at datetime(6) null,
    followee_id bigint      null,
    follower_id bigint      null,
    constraint FKjhmtcmoxpgcojx2p3h7lcphsq
        foreign key (followee_id) references user (id),
    constraint FKmow2qk674plvwyb4wqln37svv
        foreign key (follower_id) references user (id)
);

create table review
(
    id             bigint auto_increment
        primary key,
    created_at     datetime(6)  null,
    modified_at    datetime(6)  null,
    blocked        bit          not null,
    blocked_reason varchar(255) null,
    deleted        bit          not null,
    deleted_date   datetime(6)  null,
    content        varchar(255) null,
    location       varchar(255) null,
    rating         int          null,
    user_id        bigint       null,
    constraint FKiyf57dy48lyiftdrf7y87rnxi
        foreign key (user_id) references user (id)
);

create table comment
(
    id             bigint auto_increment
        primary key,
    created_at     datetime(6)  null,
    modified_at    datetime(6)  null,
    blocked        bit          not null,
    blocked_reason varchar(255) null,
    deleted        bit          not null,
    deleted_date   datetime(6)  null,
    content        varchar(255) null,
    review_id      bigint       null,
    user_id        bigint       null,
    constraint FK8kcum44fvpupyw6f5baccx25c
        foreign key (user_id) references user (id),
    constraint FKnf4ni761w29tmtgdxymmgvg8r
        foreign key (review_id) references review (id)
);

create table heart
(
    id          bigint auto_increment
        primary key,
    created_at  datetime(6) null,
    modified_at datetime(6) null,
    review_id   bigint      null,
    user_id     bigint      null,
    constraint FK5pv32bwn1jhofpwouomqupc6u
        foreign key (user_id) references user (id),
    constraint FKnc2h6th4bgo4b1rfem7vt8s8f
        foreign key (review_id) references review (id)
);

create table review_image
(
    id             bigint auto_increment
        primary key,
    created_at     datetime(6)  null,
    modified_at    datetime(6)  null,
    blocked        bit          not null,
    blocked_reason varchar(255) null,
    deleted        bit          not null,
    deleted_date   datetime(6)  null,
    image_url      varchar(255) null,
    review_id      bigint       null,
    constraint FK16wp089tx9nm0obc217gvdd6l
        foreign key (review_id) references review (id)
);

create table scrap
(
    id          bigint auto_increment
        primary key,
    created_at  datetime(6)  null,
    modified_at datetime(6)  null,
    description varchar(255) null,
    review_id   bigint       null,
    user_id     bigint       null,
    constraint FKcoeryk243w3c3h9h8u3bv0qmr
        foreign key (review_id) references review (id),
    constraint FKgt91kwgqa4f4oaoi9ljgy75mw
        foreign key (user_id) references user (id)
);

create table user_image
(
    id             bigint auto_increment
        primary key,
    created_at     datetime(6)  null,
    modified_at    datetime(6)  null,
    blocked        bit          not null,
    blocked_reason varchar(255) null,
    deleted        bit          not null,
    deleted_date   datetime(6)  null,
    image_url      varchar(255) null,
    user_id        bigint       null,
    constraint FK5m3lhx7tcj9h9ju10xo4ruqcn
        foreign key (user_id) references user (id)
);

alter table user
    add constraint FK8f10cke7w02e8wkt0mafbqj83
        foreign key (user_image_id) references user_image (id);

