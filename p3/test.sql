SELECT U.user_id, U.first_name, U.last_name, U.year_of_birth, U.month_of_birth, U.day_of_birth, U.gender,
City1.city_name AS current_city_name, City1.state_name AS current_state_name, City1.country_name AS current_country_name,
City2.city_name AS hometown_city_name, City2.state_name AS hometown_state_name, City2.country_name AS hometown_country_name
FROM project3.public_users U, project3.public_user_current_cities Cur, project3.public_user_hometown_cities Home,
project3.public_cities City1, project3.public_cities City2
WHERE U.user_id = Cur.user_id AND Cur.current_city_id = City1.city_id
AND U.user_id = Home.user_id AND Home.hometown_city_id = City2.city_id
AND ROWNUM = 1;