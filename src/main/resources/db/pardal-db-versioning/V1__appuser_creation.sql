CREATE TABLE app_profile (
    prf_id serial,
    prf_name VARCHAR(20) NOT NULL,

    CONSTRAINT prf_id_pk primary key (prf_id)
);

INSERT INTO app_profile (prf_name) values ('Admin');

CREATE TABLE app_users (
                           usr_id serial,
                           usr_name VARCHAR(255) NOT NULL,
                           usr_email VARCHAR(255) NOT NULL,
                           usr_phone VARCHAR(15) NOT NULL,
                           prf_id int NOT NULL DEFAULT 1,
                           usr_expire_date date DEFAULT NULL,

                           CONSTRAINT usr_id_pk primary key (usr_id),
                           CONSTRAINT prf_id_fk FOREIGN KEY (prf_id)
                               REFERENCES app_profile(prf_id)

);