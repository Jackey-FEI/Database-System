package project2;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;

/*
    The StudentFakebookOracle class is derived from the FakebookOracle class and implements
    the abstract query functions that investigate the database provided via the <connection>
    parameter of the constructor to discover specific information.
*/
public final class StudentFakebookOracle extends FakebookOracle {
    // [Constructor]
    // REQUIRES: <connection> is a valid JDBC connection
    public StudentFakebookOracle(Connection connection) {
        oracle = connection;
    }

    @Override
    // Query 0
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the total number of users for which a birth month is listed
    //        (B) Find the birth month in which the most users were born
    //        (C) Find the birth month in which the fewest users (at least one) were born
    //        (D) Find the IDs, first names, and last names of users born in the month
    //            identified in (B)
    //        (E) Find the IDs, first names, and last name of users born in the month
    //            identified in (C)
    //
    // This query is provided to you completed for reference. Below you will find the appropriate
    // mechanisms for opening up a statement, executing a query, walking through results, extracting
    // data, and more things that you will need to do for the remaining nine queries
    public BirthMonthInfo findMonthOfBirthInfo() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            // Step 1
            // ------------
            // * Find the total number of users with birth month info
            // * Find the month in which the most users were born
            // * Find the month in which the fewest (but at least 1) users were born
            ResultSet rst = stmt.executeQuery(
                    "SELECT COUNT(*) AS Birthed, Month_of_Birth " + // select birth months and number of uses with that birth month
                            "FROM " + UsersTable + " " + // from all users
                            "WHERE Month_of_Birth IS NOT NULL " + // for which a birth month is available
                            "GROUP BY Month_of_Birth " + // group into buckets by birth month
                            "ORDER BY Birthed DESC, Month_of_Birth ASC"); // sort by users born in that month, descending; break ties by birth month

            int mostMonth = 0;
            int leastMonth = 0;
            int total = 0;
            while (rst.next()) { // step through result rows/records one by one
                if (rst.isFirst()) { // if first record
                    mostMonth = rst.getInt(2); //   it is the month with the most
                }
                if (rst.isLast()) { // if last record
                    leastMonth = rst.getInt(2); //   it is the month with the least
                }
                total += rst.getInt(1); // get the first field's value as an integer
            }
            BirthMonthInfo info = new BirthMonthInfo(total, mostMonth, leastMonth);

            // Step 2
            // ------------
            // * Get the names of users born in the most popular birth month
            rst = stmt.executeQuery(
                    "SELECT User_ID, First_Name, Last_Name " + // select ID, first name, and last name
                            "FROM " + UsersTable + " " + // from all users
                            "WHERE Month_of_Birth = " + mostMonth + " " + // born in the most popular birth month
                            "ORDER BY User_ID"); // sort smaller IDs first

            while (rst.next()) {
                info.addMostPopularBirthMonthUser(new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)));
            }

            // Step 3
            // ------------
            // * Get the names of users born in the least popular birth month
            rst = stmt.executeQuery(
                    "SELECT User_ID, First_Name, Last_Name " + // select ID, first name, and last name
                            "FROM " + UsersTable + " " + // from all users
                            "WHERE Month_of_Birth = " + leastMonth + " " + // born in the least popular birth month
                            "ORDER BY User_ID"); // sort smaller IDs first

            while (rst.next()) {
                info.addLeastPopularBirthMonthUser(new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)));
            }

            // Step 4
            // ------------
            // * Close resources being used
            rst.close();
            stmt.close(); // if you close the statement first, the result set gets closed automatically

            return info;

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new BirthMonthInfo(-1, -1, -1);
        }
    }

    @Override
    // Query 1
    // -----------------------------------------------------------------------------------
    // GOALS: (A) The first name(s) with the most letters
    //        (B) The first name(s) with the fewest letters
    //        (C) The first name held by the most users
    //        (D) The number of users whose first name is that identified in (C)
    public FirstNameInfo findNameInfo() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                FirstNameInfo info = new FirstNameInfo();
                info.addLongName("Aristophanes");
                info.addLongName("Michelangelo");
                info.addLongName("Peisistratos");
                info.addShortName("Bob");
                info.addShortName("Sue");
                info.addCommonName("Harold");
                info.addCommonName("Jessica");
                info.setCommonNameCount(42);
                return info;
            */
            FirstNameInfo info = new FirstNameInfo();
             int longname = 0; 
             int shortname = 0;
             ResultSet rst = stmt.executeQuery(
                            "SELECT DISTINCT First_Name " +
                            "FROM " + UsersTable + " " +
                            "ORDER BY LENGTH(First_Name) DESC, First_Name ASC");
             while(rst.next()){
                String name = rst.getString(1);
                if (rst.isFirst())
                {
                    info.addLongName(name);
                    longname = name.length();
                }
                else if(name.length() == longname)
                {
                    info.addLongName(name);
                } else break;
             }
             rst = stmt.executeQuery(
                            "SELECT DISTINCT First_Name " +
                            "FROM " + UsersTable + " " +
                            "ORDER BY LENGTH(First_Name) ASC, First_Name ASC");
             while(rst.next()){
                String name = rst.getString(1);
                if (rst.isFirst())
                {
                    info.addShortName(name);
                    shortname = name.length();
                }
                else if(name.length() == shortname)
                {
                    info.addShortName(name);
                } else break;
             }
             rst = stmt.executeQuery(
                            "SELECT First_Name, COUNT(*) As count " +
                            "FROM " + UsersTable + " " +
                            "GROUP BY First_Name " +
                            "ORDER BY count DESC, First_Name ASC");
             int mostcount = 0;
             int sum = 0;
             while(rst.next())
             {
                if (rst.isFirst())
                {
                    mostcount = rst.getInt(2);
                    sum += rst.getInt(2);
                    info.addCommonName(rst.getString(1));
                }
                else if (rst.getInt(2) == mostcount)
                {
                    info.addCommonName(rst.getString(1));
                    sum+= rst.getInt(2);
                } else break;
             }
             info.setCommonNameCount(sum);
            return info; // placeholder for compilation
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new FirstNameInfo();
        }
    }

    @Override
    // Query 2
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of users without any friends
    //
    // Be careful! Remember that if two users are friends, the Friends table only contains
    // the one entry (U1, U2) where U1 < U2.
    public FakebookArrayList<UserInfo> lonelyUsers() throws SQLException {
        FakebookArrayList<UserInfo> results = new FakebookArrayList<UserInfo>(", ");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            ResultSet rst = stmt.executeQuery(
                "SELECT DISTINCT U.USER_ID, U.FIRST_NAME, U.LAST_NAME " +
                "FROM " + UsersTable + " U " +
                "WHERE U.USER_ID NOT IN " +
                "(SELECT F.USER1_ID AS USER_ID FROM " + FriendsTable + " F " +
                "UNION " +
                "SELECT F.USER2_ID AS USER_ID FROM " + FriendsTable + " F) " +
                "ORDER BY U.USER_ID ASC"
                );
            while (rst.next()) {
                UserInfo u = new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3));
                results.add(u);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 3
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of users who no longer live
    //            in their hometown (i.e. their current city and their hometown are different)
    public FakebookArrayList<UserInfo> liveAwayFromHome() throws SQLException {
        FakebookArrayList<UserInfo> results = new FakebookArrayList<UserInfo>(", ");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            ResultSet rst = stmt.executeQuery(
                "SELECT DISTINCT U.USER_ID, U.FIRST_NAME, U.LAST_NAME " +
                "FROM " + UsersTable + " U " +
                "WHERE U.USER_ID IN " +
                "(SELECT H.USER_ID AS USER_ID FROM " + HometownCitiesTable + " H, " + CurrentCitiesTable + " C " +
                "WHERE H.USER_ID = C.USER_ID AND H.HOMETOWN_CITY_ID <> C.CURRENT_CITY_ID) " +
                "ORDER BY U.USER_ID ASC"
                );
            while (rst.next()) {
                UserInfo u = new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3));
                results.add(u);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 4
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, links, and IDs and names of the containing album of the top
    //            <num> photos with the most tagged users
    //        (B) For each photo identified in (A), find the IDs, first names, and last names
    //            of the users therein tagged
    public FakebookArrayList<TaggedPhotoInfo> findPhotosWithMostTags(int num) throws SQLException {
        FakebookArrayList<TaggedPhotoInfo> results = new FakebookArrayList<TaggedPhotoInfo>("\n");
        
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                PhotoInfo p = new PhotoInfo(80, 5, "www.photolink.net", "Winterfell S1");
                UserInfo u1 = new UserInfo(3901, "Jon", "Snow");
                UserInfo u2 = new UserInfo(3902, "Arya", "Stark");
                UserInfo u3 = new UserInfo(3903, "Sansa", "Stark");
                TaggedPhotoInfo tp = new TaggedPhotoInfo(p);
                tp.addTaggedUser(u1);
                tp.addTaggedUser(u2);
                tp.addTaggedUser(u3);
                results.add(tp);
            */
            ResultSet rst = stmt.executeQuery(
                            "WITH Photoids As " +
                            "(SELECT Tag_Photo_ID, COUNT(*) As count " +
                            "FROM " + TagsTable + " " +
                            "GROUP BY Tag_Photo_ID " +
                            "ORDER BY COUNT(*) DESC, Tag_Photo_ID ASC " +
                            "FETCH FIRST " + num+ " ROWS ONLY) " +
                            "SELECT P.Photo_ID, P.Album_id, P.Photo_Link, A.Album_Name, U.User_ID, U.First_Name, U.Last_Name " +
                            "FROM "+ PhotosTable + " P, " + AlbumsTable + " A, Photoids, " + UsersTable + " U, " +TagsTable + " T " +
                            "WHERE P.Album_id = A.Album_id " + 
                            "AND Photoids.Tag_Photo_ID = P.Photo_ID " +
                            "AND T.Tag_Photo_ID = Photoids.Tag_Photo_ID "  +
                            "AND T.Tag_Subject_ID = U.User_ID " +
                            "ORDER BY Photoids.count DESC, Photoids.Tag_Photo_ID ASC, U.User_ID ASC"
                             );
            long id = -1;
            if (rst.next())
            {
                while(true)
                {
                    boolean needbreak = false;
                    PhotoInfo p = new PhotoInfo(rst.getLong(1),rst.getLong(2),rst.getString(3),rst.getString(4));
                    TaggedPhotoInfo  tp = new TaggedPhotoInfo(p);
                    id = rst.getLong(1);
                    // if (rst.getLong(1) != id)
                    // {
                    //     p = new PhotoInfo(rst.getLong(1),rst.getLong(2),rst.getString(3),rst.getString(4));
                    //     tp = new TaggedPhotoInfo(p);
                    //     id = rst.getLong(1);
                    // }
                    while (rst.getLong(1) == id)
                    {
                        UserInfo u1 = new UserInfo(rst.getLong(5), rst.getString(6), rst.getString(7));
                        tp.addTaggedUser(u1);
                        if (!rst.next())
                        {
                            needbreak = true;
                            break;
                        }
                    }
                    results.add(tp);
                    if (needbreak)
                    {
                        break;
                    }
                }
            }
            
            
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 5
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, last names, and birth years of each of the two
    //            users in the top <num> pairs of users that meet each of the following
    //            criteria:
    //              (i) same gender
    //              (ii) tagged in at least one common photo
    //              (iii) difference in birth years is no more than <yearDiff>
    //              (iv) not friends
    //        (B) For each pair identified in (A), find the IDs, links, and IDs and names of
    //            the containing album of each photo in which they are tagged together
    public FakebookArrayList<MatchPair> matchMaker(int num, int yearDiff) throws SQLException {
        FakebookArrayList<MatchPair> results = new FakebookArrayList<MatchPair>("\n");

        try (Statement stmt1 = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
                try (Statement stmt2 = oracle.createStatement(FakebookOracleConstants.AllScroll,
                        FakebookOracleConstants.ReadOnly)) {
                    ResultSet rst1 = stmt1.executeQuery(
                        "WITH TAGGED_PAIRS AS " + 
                        "(SELECT T1.TAG_SUBJECT_ID AS USER1_ID, T2.TAG_SUBJECT_ID AS USER2_ID, COUNT(*) AS COUNT_TAGGED " +
                        "FROM " + TagsTable + " T1, " + TagsTable + " T2 " + 
                        "WHERE T1.TAG_PHOTO_ID = T2.TAG_PHOTO_ID AND T1.TAG_SUBJECT_ID < T2.TAG_SUBJECT_ID " +
                        "GROUP BY T1.TAG_SUBJECT_ID, T2.TAG_SUBJECT_ID) " +
                        "SELECT * FROM ( " +
                            "SELECT DISTINCT U1.USER_ID AS USER1_ID, U1.FIRST_NAME AS USER1_FIRST_NAME, U1.LAST_NAME AS USER1_LAST_NAME, U1.YEAR_OF_BIRTH AS USER1_YEAR, U2.USER_ID AS USER2_ID, U2.FIRST_NAME AS USER2_FIRST_NAME, U2.LAST_NAME AS USER2_LAST_NAME, U2.YEAR_OF_BIRTH AS USER2_YEAR, T.COUNT_TAGGED " +
                            "FROM  " + UsersTable + " U1 " +
                            "JOIN TAGGED_PAIRS T ON T.USER1_ID = U1.USER_ID " +
                            "JOIN " + UsersTable + " u2 ON T.USER2_ID = U2.USER_ID " +
                            "WHERE U1.USER_ID < U2.USER_ID AND U1.GENDER = U2.GENDER " +
                            "AND ABS(U1.YEAR_OF_BIRTH - U2.YEAR_OF_BIRTH) <= " + yearDiff + " " +
                            "AND NOT EXISTS (SELECT * FROM " + FriendsTable + " F WHERE F.USER1_ID = U1.USER_ID AND F.USER2_ID = U2.USER_ID) " +
                            "ORDER BY T.COUNT_TAGGED DESC, U1.USER_ID ASC, U2.USER_ID ASC " +
                        ") WHERE ROWNUM <= " + num
                        );
                    while (rst1.next()) {
                        long user1_id = rst1.getLong("USER1_ID");
                        long user2_id = rst1.getLong("USER2_ID");

                        UserInfo u1 = new UserInfo(user1_id, rst1.getString("USER1_FIRST_NAME"), rst1.getString("USER1_LAST_NAME"));
                        UserInfo u2 = new UserInfo(user2_id, rst1.getString("USER2_FIRST_NAME"), rst1.getString("USER2_LAST_NAME"));
                        MatchPair mp = new MatchPair(u1, rst1.getLong("USER1_YEAR"), u2, rst1.getLong("USER2_YEAR"));
                        ResultSet rst2 = stmt2.executeQuery(
                            "SELECT T1.TAG_PHOTO_ID, P.PHOTO_LINK, P.ALBUM_ID, A.ALBUM_NAME " + 
                            "FROM " + TagsTable + " T1, " + TagsTable + " T2, " + PhotosTable + " P, " + AlbumsTable + " A " +
                            "WHERE T1.TAG_SUBJECT_ID = " + user1_id + " AND T2.TAG_SUBJECT_ID = " + user2_id + " AND T1.TAG_PHOTO_ID = T2.TAG_PHOTO_ID " +
                            "AND T1.TAG_PHOTO_ID = P.PHOTO_ID AND P.ALBUM_ID = A.ALBUM_ID " +
                            "ORDER BY T1.TAG_PHOTO_ID ASC"
                        );
                        while (rst2.next()) {
                            PhotoInfo p = new PhotoInfo(rst2.getLong(1), rst2.getLong(3), rst2.getString(2), rst2.getString(4));
                            mp.addSharedPhoto(p);
                        }
                        results.add(mp);
                    }
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(93103, "Romeo", "Montague");
                UserInfo u2 = new UserInfo(93113, "Juliet", "Capulet");
                MatchPair mp = new MatchPair(u1, 1597, u2, 1597);
                PhotoInfo p = new PhotoInfo(167, 309, "www.photolink.net", "Tragedy");
                mp.addSharedPhoto(p);
                results.add(mp);
            */
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 6
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of each of the two users in
    //            the top <num> pairs of users who are not friends but have a lot of
    //            common friends
    //        (B) For each pair identified in (A), find the IDs, first names, and last names
    //            of all the two users' common friends
    public FakebookArrayList<UsersPair> suggestFriends(int num) throws SQLException {
        FakebookArrayList<UsersPair> results = new FakebookArrayList<UsersPair>("\n");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(16, "The", "Hacker");
                UserInfo u2 = new UserInfo(80, "Dr.", "Marbles");
                UserInfo u3 = new UserInfo(192, "Digit", "Le Boid");
                UsersPair up = new UsersPair(u1, u2);
                up.addSharedFriend(u3);
                results.add(up);
            */
            ResultSet rst = stmt.executeQuery(
                            "WITH Bidirection1 As" +
                            "(SELECT User1_ID As first_user, User2_ID As second_user " +
                            "FROM " + FriendsTable +" " +
                            "UNION " +
                            "SELECT User2_ID As first_user, User1_ID As second_user " +
                            "FROM " + FriendsTable + "), " +
                            "Bidirection2 As " +
                            "(SELECT User1_ID As first_user, User2_ID As second_user " +
                            "FROM " + FriendsTable + " " +
                            "UNION " +
                            "SELECT User2_ID As first_user, User1_ID As second_user " +
                            "FROM " + FriendsTable + "), " +
                            "Selection As " +
                            "(SELECT Bidirection1.first_user, Bidirection2.second_user, COUNT(*) As count " +
                            "FROM  Bidirection1, Bidirection2 " +
                            "WHERE Bidirection1.second_user = Bidirection2.first_user " +
                            "AND Bidirection1.first_user < Bidirection2.second_user " +
                            "AND NOT EXISTS (SELECT * FROM " + FriendsTable + " F WHERE Bidirection1.first_user = F.User1_ID AND Bidirection2.second_user = F.User2_ID) " +
                            "GROUP BY Bidirection1.first_user, Bidirection2.second_user " +
                            "ORDER BY COUNT(*) DESC, Bidirection1.first_user ASC, Bidirection2.second_user ASC " +
                            "FETCH FIRST " + num + " ROWS ONLY) " +
                            "SELECT F1.First_Name, F1.Last_Name, Bidirection1.first_user, " + 
                            "Bidirection1.second_user, Bidirection2.second_user, " +
                            "F2.First_Name, F2.Last_Name, " +
                            "F3.First_Name, F3.Last_Name " + 
                            "FROM  Bidirection1, Bidirection2, Selection, " + UsersTable +" F1, " +UsersTable +" F2, " +UsersTable +" F3 " +
                            "WHERE Bidirection1.second_user = Bidirection2.first_user " +
                            "AND Bidirection1.first_user < Bidirection2.second_user " +
                            "AND Bidirection1.first_user = Selection.first_user " +
                            "AND Bidirection2.second_user = Selection.second_user " +
                            "AND F1.User_ID = Bidirection1.first_user " + 
                            "AND F2.User_ID = Bidirection1.second_user " +
                            "AND F3.User_ID = Bidirection2.second_user " +
                            "ORDER BY Selection.count DESC, Bidirection1.first_user ASC, Bidirection2.second_user ASC, Bidirection1.second_user ASC"
                             );
            long id1 = -1;
            long id2 = -1;
            if (rst.next())
            {
                while(true)
                {
                    boolean needbreak = false;
                    UserInfo u1 = new UserInfo(rst.getLong(3), rst.getString(1), rst.getString(2));
                    UserInfo u2 = new UserInfo(rst.getLong(5), rst.getString(8), rst.getString(9));
                    UsersPair up = new UsersPair(u1, u2);
                    id1 = rst.getLong(3);
                    id2 = rst.getLong(5);
                    // if (rst.getLong(1) != id)
                    // {
                    //     p = new PhotoInfo(rst.getLong(1),rst.getLong(2),rst.getString(3),rst.getString(4));
                    //     tp = new TaggedPhotoInfo(p);
                    //     id = rst.getLong(1);
                    // }
                    while (rst.getLong(3) == id1 && rst.getLong(5) == id2 )
                    {
                        UserInfo u3 = new UserInfo(rst.getLong(4), rst.getString(6), rst.getString(7));
                        up.addSharedFriend(u3);
                        if (!rst.next())
                        {
                            needbreak = true;
                            break;
                        }
                    }
                    results.add(up);
                    if (needbreak)
                    {
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 7
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the name of the state or states in which the most events are held
    //        (B) Find the number of events held in the states identified in (A)
    public EventStateInfo findEventStates() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                EventStateInfo info = new EventStateInfo(50);
                info.addState("Kentucky");
                info.addState("Hawaii");
                info.addState("New Hampshire");
                return info;
            */
             int max = 0;
             ResultSet rst = stmt.executeQuery(
                            "SELECT C.State_Name, COUNT(*) As count " +
                            "FROM " + CitiesTable + " C, " + EventsTable + " E " +
                            "WHERE C.City_ID = E.Event_City_ID " +
                            "GROUP BY C.State_Name " +
                            "ORDER BY COUNT(*) DESC, C.State_Name ASC");
             if (rst.next())
             {
                 max = rst.getInt(2);
                 EventStateInfo info = new EventStateInfo(max);
                 info.addState(rst.getString(1));
                 while(rst.next()){
                 if(rst.getInt(2) == max)
                 {
                    info.addState(rst.getString(1));
                 }
                 else
                 {
                    break;
                 }
                }
                return info;
             }
             else
               return new EventStateInfo(-1); // placeholder for compilation
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new EventStateInfo(-1);
        }
    }

    @Override
    // Query 8
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the ID, first name, and last name of the oldest friend of the user
    //            with User ID <userID>
    //        (B) Find the ID, first name, and last name of the youngest friend of the user
    //            with User ID <userID>
    public AgeInfo findAgeInfo(long userID) throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            UserInfo old = new UserInfo(-1, "UNWRITTEN", "UNWRITTEN");
            UserInfo young = new UserInfo(-1, "UNWRITTEN", "UNWRITTEN");
            ResultSet rst1 = stmt.executeQuery(
                "SELECT * FROM ( " +
                "SELECT U.USER_ID, U.FIRST_NAME, U.LAST_NAME " +
                "FROM " + UsersTable +" U " +
                "WHERE U.USER_ID IN ( " +
                    "SELECT F.USER1_ID AS USER_ID FROM " + FriendsTable + " F WHERE F.USER2_ID = " + userID + " " +
                    "UNION " +
                    "SELECT F.USER2_ID AS USER_ID FROM " + FriendsTable + " F WHERE F.USER1_ID = " + userID + " " +
                ") " +
                "ORDER BY U.YEAR_OF_BIRTH ASC, U.MONTH_OF_BIRTH ASC, U.DAY_OF_BIRTH ASC, U.USER_ID DESC " +
            ") " +
            "WHERE ROWNUM = 1"
            );
            while (rst1.next()) {
                old = new UserInfo(rst1.getLong(1), rst1.getString(2), rst1.getString(3)); 
            }

            ResultSet rst2 = stmt.executeQuery(
                "SELECT * FROM ( " +
                "SELECT U.USER_ID, U.FIRST_NAME, U.LAST_NAME " +
                "FROM " + UsersTable +" U " +
                "WHERE U.USER_ID IN ( " +
                    "SELECT F.USER1_ID AS USER_ID FROM " + FriendsTable + " F WHERE F.USER2_ID = " + userID + " " +
                    "UNION " +
                    "SELECT F.USER2_ID AS USER_ID FROM " + FriendsTable + " F WHERE F.USER1_ID = " + userID + " " +
                ") " +
                "ORDER BY U.YEAR_OF_BIRTH DESC, U.MONTH_OF_BIRTH DESC, U.DAY_OF_BIRTH DESC, U.USER_ID DESC " +
                ") " +
                "WHERE ROWNUM = 1"
            );
            while (rst2.next()) {
                young = new UserInfo(rst2.getLong(1), rst2.getString(2), rst2.getString(3)); 
            }
            return new AgeInfo(old, young); // placeholder for compilation
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new AgeInfo(new UserInfo(-1, "ERROR", "ERROR"), new UserInfo(-1, "ERROR", "ERROR"));
        }
    }

    @Override
    // Query 9
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find all pairs of users that meet each of the following criteria
    //              (i) same last name
    //              (ii) same hometown
    //              (iii) are friends
    //              (iv) less than 10 birth years apart
    public FakebookArrayList<SiblingInfo> findPotentialSiblings() throws SQLException {
        FakebookArrayList<SiblingInfo> results = new FakebookArrayList<SiblingInfo>("\n");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            ResultSet rst = stmt.executeQuery(
                "SELECT U1.USER_ID AS USER1_ID, U1.FIRST_NAME AS USER1_FIRST_NAME, U1.LAST_NAME AS USER1_LAST_NAME, U2.USER_ID AS USER2_ID, U2.FIRST_NAME AS USER2_FIRST_NAME, U2.LAST_NAME AS USER2_LAST_NAME " +
                "FROM " + UsersTable + " U1, " + UsersTable + " U2, " + HometownCitiesTable + " H1, " + HometownCitiesTable + " H2 " +
                "WHERE U1.USER_ID < U2.USER_ID AND U1.LAST_NAME = U2.LAST_NAME " +
                "AND U1.USER_ID = H1.USER_ID AND U2.USER_ID = H2.USER_ID AND H1.HOMETOWN_CITY_ID = H2.HOMETOWN_CITY_ID " +
                "AND ABS(U1.YEAR_OF_BIRTH - U2.YEAR_OF_BIRTH) < 10 " +
                "AND EXISTS (SELECT * FROM " + FriendsTable + " F WHERE F.USER1_ID = U1.USER_ID AND F.USER2_ID = U2.USER_ID) " +
                "ORDER BY U1.USER_ID ASC, U2.USER_ID ASC"
            );
            while (rst.next()) {
                UserInfo u1 = new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3));
                UserInfo u2 = new UserInfo(rst.getLong(4), rst.getString(5), rst.getString(6));
                SiblingInfo si = new SiblingInfo(u1, u2);
                results.add(si);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    // Member Variables
    private Connection oracle;
    private final String UsersTable = FakebookOracleConstants.UsersTable;
    private final String CitiesTable = FakebookOracleConstants.CitiesTable;
    private final String FriendsTable = FakebookOracleConstants.FriendsTable;
    private final String CurrentCitiesTable = FakebookOracleConstants.CurrentCitiesTable;
    private final String HometownCitiesTable = FakebookOracleConstants.HometownCitiesTable;
    private final String ProgramsTable = FakebookOracleConstants.ProgramsTable;
    private final String EducationTable = FakebookOracleConstants.EducationTable;
    private final String EventsTable = FakebookOracleConstants.EventsTable;
    private final String AlbumsTable = FakebookOracleConstants.AlbumsTable;
    private final String PhotosTable = FakebookOracleConstants.PhotosTable;
    private final String TagsTable = FakebookOracleConstants.TagsTable;
}
