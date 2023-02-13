

CREATE VIEW Photoids As
SELECT *
FROM (
  SELECT Tag_Photo_ID, COUNT(*) As count
  FROM project2.Public_Tags
  GROUP BY Tag_Photo_ID
  ORDER BY COUNT(*) DESC, Tag_Photo_ID ASC
  FETCH FIRST 5 ROWS ONLY
);

SELECT P.Photo_ID, P.Album_id, P.Photo_Link, A.Album_Name, U.User_ID, U.First_Name, U.Last_Name 
FROM project2.Public_Photos P,  project2.Public_Albums A, Photoids,
 project2.Public_Users U, project2.Public_Tags T
WHERE P.Album_id = A.Album_id 
    AND Photoids.Tag_Photo_ID = P.Photo_ID 
    AND T.Tag_Photo_ID = Photoids.Tag_Photo_ID
    AND T.Tag_Subject_ID = U.User_ID
ORDER BY Photoids.count DESC, Photoids.Tag_Photo_ID ASC, U.User_ID ASC;