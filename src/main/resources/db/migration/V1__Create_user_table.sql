create table user_table
(
    id           bigserial primary key,
    login        varchar not null unique,
    password     varchar not null,
    firstname    varchar not null,
    surname      varchar not null,
    email        varchar not null unique,
    phone_number varchar not null unique,
    sex         varchar not null,
    user_type    varchar not null
);


create table session (
    id bigserial primary key,
    user_id bigint references user_table(id) on delete cascade,
    session varchar not null
);



create table teacher_extension
(
    teacher_id bigint references user_table(id) on delete cascade unique,
    average_grade decimal default 0,
    grade_amount integer default 0,
    bio varchar
);

create table lesson
(
    id bigserial,
    teacher_id bigint references user_table(id) not null,
    date timestamp not null,
    price decimal not null ,
    zoom_link varchar not null,
    student_id bigint references user_table(id),
    homework varchar,
    answer varchar,
    mark decimal
);

