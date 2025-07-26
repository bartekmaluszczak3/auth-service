create table _user(
    id integer PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    user_uid VARCHAR(255) unique NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255)
);

CREATE sequence _user_seq increment 1 start 1;

create table token(
    id integer PRIMARY KEY,
    revoked boolean,
    expired boolean,
    token VARCHAR(255) NOT NULL,
    user_id integer,
    CONSTRAINT fk_token
        FOREIGN KEY(user_id)
	        REFERENCES _user(id)
);

CREATE sequence token_seq increment 1 start 1;
