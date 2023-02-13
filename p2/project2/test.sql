WITH Bidirection1 As
(SELECT User1_ID As first_user, User2_ID As second_user
FROM project2.Public_Friends
UNION
SELECT User2_ID As first_user, User1_ID As second_user
FROM project2.Public_Friends), 
Bidirection2 As
(SELECT User1_ID As first_user, User2_ID As second_user
FROM project2.Public_Friends
UNION
SELECT User2_ID As first_user, User1_ID As second_user
FROM project2.Public_Friends
),
Selection As
(
SELECT Bidirection1.first_user, Bidirection2.second_user, COUNT(*) As count
FROM  Bidirection1, Bidirection2
WHERE Bidirection1.second_user = Bidirection2.first_user
AND Bidirection1.first_user < Bidirection2.second_user
AND NOT EXISTS (SELECT * FROM project2.Public_Friends F WHERE Bidirection1.first_user = F.User1_ID AND Bidirection2.second_user = F.User2_ID) 
GROUP BY Bidirection1.first_user, Bidirection2.second_user
ORDER BY COUNT(*) DESC, Bidirection1.first_user ASC, Bidirection2.second_user ASC
FETCH FIRST 5 ROWS ONLY)
SELECT F1.First_Name, F1.Last_Name, Bidirection1.first_user, 
Bidirection2.second_user, Bidirection1.second_user,
F2.First_Name, F2.Last_Name,
F3.First_Name, F3.Last_Name
FROM  Bidirection1, Bidirection2, Selection, project2.Public_Users F1, project2.Public_Users F2, project2.Public_Users F3
WHERE Bidirection1.second_user = Bidirection2.first_user
AND Bidirection1.first_user < Bidirection2.second_user
AND Bidirection1.first_user = Selection.first_user 
AND Bidirection2.second_user = Selection.second_user
AND F1.User_ID = Bidirection1.first_user 
AND F2.User_ID = Bidirection1.second_user
AND F3.User_ID = Bidirection2.second_user
ORDER BY Selection.count DESC, Bidirection1.first_user ASC, Bidirection2.second_user ASC;



-- SELECT F1.First_Name, F1.Last_Name, Bidirection1.first_user, Bidirection2.second_user, F2.First_Name, F2.Last_Name
-- FROM  Bidirection1, Bidirection2, project2.Public_Users F1, project2.Public_Users F2
-- WHERE Bidirection1.second_user = Bidirection2.first_user
-- AND Bidirection1.first_user < Bidirection2.second_user
-- AND F1.User_ID = Bidirection1.first_user 
-- AND F2.User_ID = Bidirection2.second_user
-- AND NOT EXISTS (SELECT * FROM project2.Public_Friends F WHERE Bidirection1.first_user = F.User1_ID AND Bidirection2.second_user = F.User2_ID) 
-- GROUP BY Bidirection1.first_user, Bidirection2.second_user
-- ORDER BY COUNT(*) DESC, Bidirection1.first_user ASC, Bidirection2.second_user ASC
-- FETCH FIRST 5 ROWS ONLY;

