// Query 6
// Find the average friend count per user.
// Return a decimal value as the average user friend count of all users in the users collection.

function find_average_friendcount(dbname) {
    db = db.getSiblingDB(dbname);

    // TODO: calculate the average friend count
    let count = 0;
    let num = 0;
    let curs = db.users.aggregate([
        {
        $project: {
            friend_num:  { $cond: { if: { $isArray: "$friends" }, then: { $size: "$friends" }, else: 0 } }
        },
    },
     {   $group: {
            _id: 0,
            average: {$avg: "$friend_num"} 
        }
    }
    ])
    // db.users.find({}
    //     ,{
    //         _id:0,
    //         friend_num: { $cond: { if: { $isArray: "$friends" }, then: { $size: "$friends" }, else: 0 } }
    //     }).forEach( function(userall){
    //         count+=userall.friend_num;
    //         num = num +1;
    //     });
    return curs.next().average;
}
