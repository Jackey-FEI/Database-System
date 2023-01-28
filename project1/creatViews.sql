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
ADD PRIMARY KEY (user_id);

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

