package io.lroyia.converter.util;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库查询工具
 * @author lroyia
 * @since 2022/2/16 17:39
 **/
public class SQLUtil {

    private static String URL = "";

    private static String USER_NAME = "";

    private static String PASSWORD = "";

    /**
     * 执行SQL查询
     * @param sql   查询SQL
     * @return  查询结果
     * @throws Exception    异常
     * @author lroyia
     * @since  2022年2月17日 09:38:09
     */
    public static List<Map<String, Object>> select(String sql) throws Exception{
        try(Connection connection = getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql)){
            List<Map<String, Object>> result = new ArrayList<>();
            while (resultSet.next()){
                ResultSetMetaData metaData = resultSet.getMetaData();
                Map<String, Object> atom = new HashMap<>();
                for(int i = 1; i < metaData.getColumnCount() + 1; i++){
                    String columnName = metaData.getColumnName(i);
                    atom.put(columnName, resultSet.getObject(i));
                }
                result.add(atom);
            }
            return result;
        }
    }

    /**
     * 获取数据库连接
     * @return  数据库连接
     * @throws Exception    连接异常
     * @author lroyia
     * @since  2022年2月17日 09:46:03
     */
    public static Connection getConnection() throws Exception{
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER_NAME, PASSWORD);
    }

    public static void setURL(String URL) {
        SQLUtil.URL = URL;
    }

    public static void setUserName(String userName) {
        USER_NAME = userName;
    }

    public static void setPASSWORD(String PASSWORD) {
        SQLUtil.PASSWORD = PASSWORD;
    }
}
