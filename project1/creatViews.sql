-- -- PRAGMA foreign_keys = ON;

CREATE TABLE Current_City_F As:
SELECT
    Users.user_id,
    Cities.city_id,
    Cities.city_name,
    Cities.state_name,
    Cities.country_name
FROM Users
JOIN User_Current_Cities
  ON Users.user_id = User_Current_Cities.user_id
JOIN Cities
  ON User_Current_Cities.city_id = Cities.city_id;

CREATE TABLE Hometown_City_F As:
SELECT
    Users.user_id,
    Cities.city_id,
    Cities.city_name,
    Cities.state_name,
    Cities.country_name
FROM Users
JOIN User_Hometown_Cities
  ON Users.user_id = User_Hometown_Cities.user_id
JOIN Cities
  ON User_Hometown_Cities.city_id = Cities.city_id;

CREATE TABLE Institution_F As:
SELECT
   Users.user_id,
   Programs.institution,
   Education.program_year,
   Programs.concentration,
   Programs.degree,
FROM Users
JOIN Education
  ON Users.user_id = Education.user_id
JOIN Programs
  ON Education.program_id = Programs.program_id;


CREATE TABLE View_User_Information As:
SELECT
    Users.user_id,
    Users.first_name,
    Users.last_name,
    Users.year_of_birth,
    Users.month_of_birth,
    Users.day_of_birth,
    Users.gender,
    Current_City_F.city_name As current_city,
    Current_City_F.state_name As current_state,
    Current_City_F.country_name As current_country,
    Hometown_City_F.city_name As hometown_city,
    Hometown_City_F.state_name As hometown_state,
    Hometown_City_F.country_name As hometown_country,
    Institution_F.institution,
    Institution_F.program_year,
    Institution_F.concentration As program_concentration,
    Institution_F.degree As program_degree
FROM Users
JOIN Current_City_F
  ON Users.user_id = Current_City_F.user_id
JOIN Hometown_City_F
  ON User.user_id = Hometown_City_F.user_id
LEFT JOIN Institution_F
  ON Users.user_id = Institution_F.user_id;

ALTER TABLE View_User_Information
MODIFY first_name NOT NULL;

ALTER TABLE View_User_Information
MODIFY last_name NOT NULL;

ALTER TABLE View_User_Information
MODIFY year_of_birth NOT NULL;

ALTER TABLE View_User_Information
MODIFY month_of_birth NOT NULL;

ALTER TABLE View_User_Information
MODIFY day_of_birth NOT NULL;

ALTER TABLE View_User_Information
MODIFY gender NOT NULL;

CREATE TABLE View_Are_Friends (
    user1_id INTEGER NOT NULL,
    user2_id INTEGER NOT NULL,
    PRIMARY KEY (user1_id, user2_id),
    FOREIGN KEY (user1_id) REFERENCES Users(user_id),
    FOREIGN KEY (user2_id) REFERENCES Users(user_id),
    CHECK (user1_id <> user2_id)
);

INSERT INTO View_Are_Friends(user1_id,user2_id)
SELECT user1_id,user2_id
FROM Friends;

CREATE TABLE View_Photo_Information As:
SELECT
    Albums.album_id,
    Albums.owner_id,
    Albums.cover_photo_id,
    Albums.album_name,
    Albums.album_created_time,
    Albums.album_modified_time,
    Albums.album_link,
    Albums.album_visibility,
    Photos.photo_id,
    Photos.photo_caption,
    Photos.photo_created_time,
    Photos.photo_modified_time,
    Photos.photo_link
FROM Albums
JOIN Photos
  ON Albums.album_id = Photos.album_id;

ALTER TABLE View_Photo_Information
MODIFY album_id NOT NULL;

ALTER TABLE View_Photo_Information
MODIFY owner_id NOT NULL;

ALTER TABLE View_Photo_Information
MODIFY cover_photo_id NOT NULL;

ALTER TABLE View_Photo_Information
MODIFY album_name NOT NULL;

ALTER TABLE View_Photo_Information
MODIFY album_created_time NOT NULL;

ALTER TABLE View_Photo_Information
MODIFY album_modified_time NOT NULL;

ALTER TABLE View_Photo_Information
MODIFY album_link NOT NULL;
    
ALTER TABLE View_Photo_Information
MODIFY album_visibility NOT NULL;

ALTER TABLE View_Photo_Information
MODIFY photo_id NOT NULL;

ALTER TABLE View_Photo_Information
MODIFY photo_created_time NOT NULL;

ALTER TABLE View_Photo_Information
MODIFY photo_modified_time NOT NULL;

ALTER TABLE View_Photo_Information
MODIFY photo_link NOT NULL;

CREATE TABLE View_Tag_Information (
    photo_id INTEGER NOT NULL,
    tag_subject_id INTEGER NOT NULL,
    tag_created_time TIMESTAMP NOT NULL,
    tag_x_coordinate NUMBER NOT NULL,
    tag_y_coordinate NUMBER NOT NULL,
);

INSERT INTO View_Tag_Information(photo_id, tag_subject_id, tag_created_time, tag_x_coordinate, tag_y_coordinate)
SELECT tag_photo_id, tag_subject_id, tag_created_time, tag_x, tag_y
FROM Tags;

CREATE TABLE View_Event_Information As:
SELECT
    User_Events.event_id,
    User_Events.event_creator_id,
    User_Events.event_name,
    User_Events.event_tagline,
    User_Events.event_description,
    User_Events.event_host,
    User_Events.event_type,
    User_Events.event_subtype,
    User_Events.event_address,
    Cities.city_name As event_city,
    Cities.state_name AS event_state,
    Cities.country_name As event_country,
    User_Events.event_start_time,
    User_Events.event_end_time
FROM User_Events
JOIN Cities
  ON User_Events.event_city_id = Cities.city_id;

ALTER TABLE View_Event_Information
MODIFY event_id NOT NULL;

ALTER TABLE View_Event_Information
MODIFY event_creator_id NOT NULL;

ALTER TABLE View_Event_Information
MODIFY event_name NOT NULL;

ALTER TABLE View_Event_Information
MODIFY event_host NOT NULL;

ALTER TABLE View_Event_Information
MODIFY event_type NOT NULL;

ALTER TABLE View_Event_Information
MODIFY event_subtype NOT NULL;

ALTER TABLE View_Event_Information
MODIFY event_address NOT NULL;

ALTER TABLE View_Event_Information
MODIFY event_city NOT NULL;

ALTER TABLE View_Event_Information
MODIFY event_state NOT NULL;

ALTER TABLE View_Event_Information
MODIFY event_country NOT NULL;

ALTER TABLE View_Event_Information
MODIFY event_start_time NOT NULL;

ALTER TABLE View_Event_Information
MODIFY event_end_time NOT NULL;