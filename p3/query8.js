// Query 8
// Find the city average friend count per user using MapReduce.

let city_average_friendcount_mapper = function () {
    // TODO: Implement the map function
    emit(this.hometown.city, {count:1, num: this.friends.length});
};

let city_average_friendcount_reducer = function (key, values) {
    // TODO: Implement the reduce function
    reduceVal = {count:0, num:0};
    for (var idx =0; idx< values.length; idx++)
    {
       reduceVal.count= reduceVal.count + 1;
        reduceVal.num= values[idx].num + reduceVal.num;
       
    }
    return reduceVal;
};

let city_average_friendcount_finalizer = function (key, reduceVal) {
    // We've implemented a simple forwarding finalize function. This implementation
    // is naive: it just forwards the reduceVal to the output collection.
    // TODO: Feel free to change it if needed.
    return reduceVal.num/reduceVal.count;
    //return {_id:key, value: reduceVal.num/reduceVal.count};
};
