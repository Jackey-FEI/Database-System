import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TreeSet;
import java.util.Vector;

import org.json.JSONObject;
import org.json.JSONArray;

public class GetData {

    static String prefix = "project3.";

    // You must use the following variable as the JDBC connection
    Connection oracleConnection = null;

    // You must refer to the following variables for the corresponding 
    // tables in your database
    String userTableName = null;
    String friendsTableName = null;
    String cityTableName = null;
    String currentCityTableName = null;
    String hometownCityTableName = null;

    // DO NOT modify this constructor
    public GetData(String u, Connection c) {
        super();
        String dataType = u;
        oracleConnection = c;
        userTableName = prefix + dataType + "_USERS";
        friendsTableName = prefix + dataType + "_FRIENDS";
        cityTableName = prefix + dataType + "_CITIES";
        currentCityTableName = prefix + dataType + "_USER_CURRENT_CITIES";
        hometownCityTableName = prefix + dataType + "_USER_HOMETOWN_CITIES";
    }

    // TODO: Implement this function
    @SuppressWarnings("unchecked")
    public JSONArray toJSON() throws SQLException {

        // This is the data structure to store all users' information
        JSONArray users_info = new JSONArray();
        
        try (Statement stmt = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            try (Statement stmt2 = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)){
                // Your implementation goes here....
                ResultSet rst = stmt.executeQuery (
                    "SELECT U.user_id, U.first_name, U.last_name, U.year_of_birth, U.month_of_birth, U.day_of_birth, U.gender, " + 
                    "City1.city_name AS current_city_name, City1.state_name AS current_state_name, City1.country_name AS current_country_name, " +
                    "City2.city_name AS hometown_city_name, City2.state_name AS hometown_state_name, City2.country_name AS hometown_country_name " +
                    "FROM " + userTableName + " U, " +
                    currentCityTableName + " Cur, " +
                    hometownCityTableName + " Home, " +
                    cityTableName + " City1, " +
                    cityTableName + " City2 " +
                    "WHERE U.user_id = Cur.user_id AND Cur.current_city_id = City1.city_id " +
                    "AND U.user_id = Home.user_id AND Home.hometown_city_id = City2.city_id"
                    );
                while (rst.next ()) {
                    JSONObject user = new JSONObject();
                    long user_id = rst.getLong (1);

                    user.put("user_id", rst.getLong("user_id"));
                    user.put("first_name", rst.getString("first_name"));
                    user.put("last_name", rst.getString("last_name"));
                    user.put("YOB", rst.getInt("year_of_birth"));
                    user.put("MOB", rst.getInt("month_of_birth"));
                    user.put("DOB", rst.getInt("day_of_birth"));
                    user.put("gender", rst.getString("gender"));

                    // Current City
                    JSONObject cur_city = new JSONObject();
                    cur_city.put("country", rst.getString("current_country_name"));
                    cur_city.put("state", rst.getString("current_state_name"));
                    cur_city.put("city", rst.getString("current_city_name"));
                    user.put("current", cur_city);

                    // Hometown City
                    JSONObject home_city = new JSONObject();
                    home_city.put("country", rst.getString("hometown_country_name"));
                    home_city.put("state", rst.getString("hometown_state_name"));
                    home_city.put("city", rst.getString("hometown_city_name"));
                    user.put("hometown", home_city);

                    ResultSet rst1 = stmt2.executeQuery (
                    "SELECT user2_id " +
                    "FROM " + friendsTableName + " " +
                    "WHERE user1_id = " + user_id
                    );
                    JSONArray friends = new JSONArray();
                    while(rst1.next()){
                        friends.put(rst1.getLong(1));
                    }
                    user.put("friends",friends);

                    users_info.put(user);
                }
                stmt2.close();
            }
            stmt.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return users_info;
    }

    // This outputs to a file "output.json"
    // DO NOT MODIFY this function
    public void writeJSON(JSONArray users_info) {
        try {
            FileWriter file = new FileWriter(System.getProperty("user.dir") + "/output.json");
            file.write(users_info.toString());
            file.flush();
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
