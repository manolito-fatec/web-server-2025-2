
-- Creation of the tables for the terms of use
CREATE TABLE pardal.terms_of_use (
    terms_id serial PRIMARY KEY,
    title VARCHAR(256) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);


CREATE TABLE pardal.terms_check_items (
    id serial PRIMARY KEY,
    terms_id INTEGER NOT NULL REFERENCES pardal.terms_of_use(terms_id),
    label VARCHAR(200) NOT NULL,
    required BOOLEAN DEFAULT FALSE
);

CREATE TABLE pardal.user_terms_acceptance (
    user_id INTEGER NOT NULL REFERENCES pardal.app_users(usr_id),
    terms_id INTEGER NOT NULL REFERENCES pardal.terms_of_use(terms_id),
    check_id INTEGER NOT NULL REFERENCES pardal.terms_check_items(id),
    accepted BOOLEAN DEFAULT false,
    PRIMARY KEY (user_id, terms_id, check_id)
);

CREATE TABLE pardal.user_terms_assignment (
    user_id INTEGER NOT NULL REFERENCES pardal.app_users(usr_id),
    terms_id INTEGER NOT NULL REFERENCES pardal.terms_of_use(terms_id),
    is_pending BOOLEAN NOT NULL DEFAULT TRUE, 
    PRIMARY KEY (user_id, terms_id)
);

CREATE OR REPLACE FUNCTION pardal.assign_new_terms_to_all_users()
RETURNS TRIGGER AS $$
BEGIN

    INSERT INTO pardal.user_terms_assignment (user_id, terms_id, is_pending)
    SELECT
        u.usr_id, 
        NEW.terms_id,
        TRUE
    FROM
        pardal.app_users u;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER after_new_terms_of_use
AFTER INSERT ON pardal.terms_of_use
FOR EACH ROW
EXECUTE FUNCTION pardal.assign_new_terms_to_all_users();

CREATE OR REPLACE FUNCTION pardal.assign_new_check_item_to_all_users()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO pardal.user_terms_acceptance (user_id, terms_id, check_id, accepted)
    SELECT
        u.usr_id,
        NEW.terms_id,
        NEW.id,
        FALSE
    FROM
        pardal.app_users u;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER after_new_terms_check_item
AFTER INSERT ON pardal.terms_check_items
FOR EACH ROW
EXECUTE FUNCTION pardal.assign_new_check_item_to_all_users();
