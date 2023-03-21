// Query 5
// Find the oldest friend for each user who has a friend. For simplicity,
// use only year of birth to determine age, if there is a tie, use the
// one with smallest user_id. You may find query 2 and query 3 helpful.
// You can create selections if you want. Do not modify users collection.
// Return a javascript object : key is the user_id and the value is the oldest_friend id.
// You should return something like this (order does not matter):
// {user1:userx1, user2:userx2, user3:userx3,...}

function oldest_friend(dbname) {
    db = db.getSiblingDB(dbname);

    let results = {};
    // TODO: implement oldest friends
    db.users.aggregate([
        {$unwind: {path: "$friends"}},
        {$project: {_id: false, user_id: true, friends: true}},
        {$out: "flat_users"}
    ]);

    let reverse_friends = db.flat_users.aggregate([ 
        {
            $project: {
                _id: false,
                user_id: "$friends",
                friends: "$user_id"
            }
        }
    ]).toArray();

    db.flat_users.insertMany(reverse_friends);

    db.flat_users.aggregate([
        {
            $lookup: {
                from: "users",
                localField: "friends",
                foreignField: "user_id",
                as: "friends_info"
            }
        },
        {
            $project: {
                user_id: true,
                friends: true,
                YOB: "$friends_info.YOB"
            }
        },
        {
            $sort: {
                user_id: 1,
                YOB: 1,
                friends: 1
            }
        }
    ]).forEach((cur) => {
        if (!(cur.user_id in results)) {
            results[cur.user_id] = cur.friends;
        }
    });

    return results;
}
