CREATE TABLE app_role (
    rl_id serial,
    rl_name VARCHAR(20) NOT NULL,

    CONSTRAINT rl_id_pk primary key (rl_id)
);

INSERT INTO app_role (rl_name) values ('Admin');

CREATE TABLE app_users (
                           usr_id serial,
                           usr_name VARCHAR(255) NOT NULL,
                           usr_email VARCHAR(255) UNIQUE NOT NULL,
                           usr_phone VARCHAR(15) NOT NULL,
                           rl_id int NOT NULL DEFAULT 1,
                           usr_expire_date date DEFAULT NULL,
                           usr_pwd VARCHAR(255) ,

                           CONSTRAINT usr_id_pk primary key (usr_id),
                           CONSTRAINT rl_id_fk FOREIGN KEY (rl_id)
                               REFERENCES app_role(rl_id)

);