CREATE TABLE app_role (
    rl_id serial,
    rl_name VARCHAR(20) NOT NULL,

    CONSTRAINT rl_id_pk primary key (rl_id)
);

INSERT INTO app_role (rl_id,rl_name) values (0,'Admin');
INSERT INTO app_role (rl_id,rl_name) values (1,'Manager');
INSERT INTO app_role (rl_id,rl_name) values (2,'Operator');

CREATE TABLE app_users (
                           usr_id serial,
                           usr_encrypted_name VARCHAR(1000) NOT NULL,
                           usr_encrypted_email VARCHAR(1000) NOT NULL,
                           usr_email_hash VARCHAR(64),
                           usr_encrypted_phone VARCHAR(1000) NOT NULL,
                           rl_id int NOT NULL DEFAULT 1,
                           usr_pwd VARCHAR(255) ,
                           usr_email_verified BOOLEAN DEFAULT FALSE,
                           usr_expire_date date DEFAULT NULL,
                           usr_verification_token VARCHAR(255) DEFAULT NULL,

                           CONSTRAINT usr_id_pk primary key (usr_id),
                           CONSTRAINT rl_id_fk FOREIGN KEY (rl_id)
                               REFERENCES app_role(rl_id)

);