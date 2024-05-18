CREATE TABLE bike
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    type           ENUM('ROAD', 'MOUNTAIN', 'GRAVEL', 'CITY', 'BMX', 'KIDS', 'ELECTRIC', 'TRICYCLE', 'CARGO', 'OTHER')          NULL,
    brand          ENUM('BIANCHI', 'BMC', 'CANNONDALE','CANYON','CERVELO','CROSS','CUBE','DEVINCI','DIAMONDBACK','DRAG','EDDY_MERCKX','EVIL','FELT','FOCUS','FORESTAL','FUJI','FULCRUM','GHOST','GIANT','GT','IBIS','KELLYS','KONA','LOOK','LOTUS','MARIN','MERIDA','NUKEPROOF','NORCO','ORBEA','ORANGE','PEUGEOT','PINARELLO','PIVOT','POLE','POLYGON','RADON','ROSE','SANTA_CRUZ','SCOTT','SPECIALIZED','TRANSITION','TREK','TRIBAN','YT','OTHER')    NOT NULL,
    model          VARCHAR(255)          NULL,
    size           VARCHAR(255)          NULL,
    wheel_size     INT                   NOT NULL,
    frame_material VARCHAR(255)          NULL,
    CONSTRAINT pk_bike PRIMARY KEY (id)
);

CREATE TABLE favourite
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    user_id    BIGINT                NULL,
    listing_id BIGINT                NULL,
    CONSTRAINT pk_favourite PRIMARY KEY (id)
);

CREATE TABLE listing
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    user_id       BIGINT                NULL,
    bike_id       BIGINT                NULL,
    title         VARCHAR(255)          NULL,
    `description` VARCHAR(255)          NULL,
    price         DOUBLE                NOT NULL,
    location      VARCHAR(255)          NULL,
    date          VARCHAR(255)          NULL,
    CONSTRAINT pk_listing PRIMARY KEY (id)
);

CREATE TABLE listing_images
(
    listing_id BIGINT       NOT NULL,
    images     VARCHAR(255) NULL
);

CREATE TABLE user
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    name            VARCHAR(255)          NOT NULL,
    email           VARCHAR(255)          NOT NULL,
    passw           VARCHAR(255)          NOT NULL,
    full_name       VARCHAR(255)          NOT NULL,
    phone_number    VARCHAR(255)          NOT NULL,
    profile_picture VARCHAR(255)          NULL,
    fcm             VARCHAR(255)          NULL,
    `role`          ENUM('USER', 'ROLE')          NULL,
    CONSTRAINT pk_user PRIMARY KEY (id)
);

CREATE TABLE user_favourites
(
    user_id       BIGINT NOT NULL,
    favourites_id BIGINT NOT NULL
);

CREATE TABLE user_listings
(
    user_id     BIGINT NOT NULL,
    listings_id BIGINT NOT NULL
);

ALTER TABLE user
    ADD CONSTRAINT uc_user_email UNIQUE (email);

ALTER TABLE user_favourites
    ADD CONSTRAINT uc_user_favourites_favourites UNIQUE (favourites_id);

ALTER TABLE user_listings
    ADD CONSTRAINT uc_user_listings_listings UNIQUE (listings_id);

ALTER TABLE user
    ADD CONSTRAINT uc_user_name UNIQUE (name);

ALTER TABLE favourite
    ADD CONSTRAINT FK_FAVOURITE_ON_LISTING FOREIGN KEY (listing_id) REFERENCES listing (id) ON DELETE CASCADE;

ALTER TABLE favourite
    ADD CONSTRAINT FK_FAVOURITE_ON_USER FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE;

ALTER TABLE listing
    ADD CONSTRAINT FK_LISTING_ON_BIKE FOREIGN KEY (bike_id) REFERENCES bike (id) ON DELETE CASCADE;

ALTER TABLE listing
    ADD CONSTRAINT FK_LISTING_ON_USER FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE;

ALTER TABLE listing_images
    ADD CONSTRAINT fk_listing_images_on_listing FOREIGN KEY (listing_id) REFERENCES listing (id) ON DELETE CASCADE;

ALTER TABLE user_favourites
    ADD CONSTRAINT fk_usefav_on_favourite FOREIGN KEY (favourites_id) REFERENCES favourite (id) ON DELETE CASCADE;

ALTER TABLE user_favourites
    ADD CONSTRAINT fk_usefav_on_user FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE;

ALTER TABLE user_listings
    ADD CONSTRAINT fk_uselis_on_listing FOREIGN KEY (listings_id) REFERENCES listing (id) ON DELETE CASCADE;

ALTER TABLE user_listings
    ADD CONSTRAINT fk_uselis_on_user FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE;
