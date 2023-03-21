// Query 4
// Find user pairs (A,B) that meet the following constraints:
// i) user A is male and user B is female
// ii) their Year_Of_Birth difference is less than year_diff
// iii) user A and B are not friends
// iv) user A and B are from the same hometown city
// The following is the schema for output pairs:
// [
//      [user_id1, user_id2],
//      [user_id1, user_id3],
//      [user_id4, user_id2],
//      ...
//  ]
// user_id is the field from the users collection. Do not use the _id field in users.
// Return an array of arrays.

function suggest_friends(year_diff, dbname) {
    db = db.getSiblingDB(dbname);

    let pairs = [];
    // TODO: implement suggest friends
    db.users.find({ 
      "gender": "male"
   },
   {
      _id : 0,
      "user_id": 1,
      "gender" : 1,
      "YOB": 1,
      "hometown.city": 1,
      "friends": 1
   }).forEach( function(user_A){
       db.users.find({
          "user_id":{$nin:user_A.friends, $ne:user_A.user_id },
          "gender": "female",
          "YOB": { $lt:user_A.YOB+year_diff, $gt:user_A.YOB-year_diff },
          "friends":{ $nin: [user_A.user_id]},
          "hometown.city": user_A.hometown.city
       }).forEach(  function(user_B)
       {
          pairs.push([user_A.user_id,user_B.user_id]);   
       });
    
   });
        
    return pairs;
}
