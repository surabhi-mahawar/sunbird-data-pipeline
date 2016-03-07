package org.ekstep.ep.samza;

import java.sql.*;
import java.util.*;

public class ChildDto {
    public static final String HANDLE = "handle";
    public static final String STANDARD = "standard";
    public static final String GENDER = "gender";
    public static final String YEAR_OF_BIRTH = "year_of_birth";
    public static final String DAY = "day";
    public static final String MONTH = "month";
    private String host;
    private String port;
    private String schema;
    private String userName;
    private String password;

    public ChildDto(String host, String port, String schema, String userName, String password){

        this.host = host;
        this.port = port;
        this.schema = schema;
        this.userName = userName;
        this.password = password;
    }

    public Child process(Child child, java.util.Date timeOfEvent) throws SQLException {
        String profileQuery = String.format("select * from profile where uid = '%s'", child.getUid());
        String learnerQuery = String.format("select * from learner where uid = '%s'", child.getUid());

        HashMap<String, Object> childData = new HashMap<String, Object>();
        Boolean profileExist = false;
        Statement statement = null;
        Connection connection=null;
        ResultSet profileResultSet = null;
        ResultSet learnerResultSet = null;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            String url = String.format("jdbc:mysql://%s:%s/%s", host,port,schema);
            connection = DriverManager
                    .getConnection(url,userName,password);
            statement = connection.createStatement();

            System.out.println("trying to read from profile table");
            profileResultSet = statement.executeQuery(profileQuery);


            while (profileResultSet.next()) {
                profileExist = true;
                childData.put(HANDLE, profileResultSet.getString(HANDLE));
                childData.put(STANDARD, profileResultSet.getInt(STANDARD));
                String gender = profileResultSet.getString(GENDER);
                String genderValue = gender == null ? "Not known" : gender;
                childData.put(GENDER, genderValue);
                childData.put(YEAR_OF_BIRTH, profileResultSet.getInt(YEAR_OF_BIRTH));
                childData.put(DAY, profileResultSet.getInt(DAY));
                childData.put(MONTH,profileResultSet.getInt(MONTH));
            }

            if(!profileExist){
                System.out.println("trying to read from learner table");
                learnerResultSet = statement.executeQuery(learnerQuery);

                if(learnerResultSet.first()) {
                    childData.put(HANDLE, null);
                    childData.put(STANDARD, 0);
                    childData.put(GENDER, "Not known");
                    childData.put(YEAR_OF_BIRTH, null);
                    childData.put(DAY,0);
                    childData.put(MONTH,0);
                }
            }

            child.populate(childData,timeOfEvent);
            return child;

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            if(statement!=null)
                statement.close();
            if(connection!=null)
                connection.close();
            if(profileResultSet!=null)
                profileResultSet.close();
            if(learnerResultSet!=null)
                learnerResultSet.close();

        }
        return child;
    }
}
