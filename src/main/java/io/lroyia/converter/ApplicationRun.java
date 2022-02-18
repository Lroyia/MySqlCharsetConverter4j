package io.lroyia.converter;

import io.lroyia.converter.util.SQLUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 程序执行入口
 *
 * @author lroyia
 * @since 2022/2/16 17:34
 **/
public class ApplicationRun {

    private static final String CHARSET = "utf8mb4"; // 目标编码
    private static final String COLLATION = "utf8mb4_0900_ai_ci";// 目标编码排序

    private static final String HOST = ""; // 连接域
    private static final String PORT = ""; // 连接端口
    private static final String DB_NAME = "";// 连接默认数据库
    private static final String USERNAME = ""; // 连接用户名
    private static final String PASSWORD = ""; // 连接密码

    // 需要修改统一编码的数据库
    private static final String[] dbList = {
            "crgs",
            "aiccs",
            "crev",
            "aicorg",
            "crgsaicorg",
    };

    public static void main(String[] args) throws Exception {
        // 连接信息
        SQLUtil.setURL(String.format("jdbc:mysql://%s:%s/%s?characterEncoding=utf-8" +
                "&useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&serverTimezone=UTC", HOST, PORT, DB_NAME));
        SQLUtil.setUserName(USERNAME);
        SQLUtil.setPASSWORD(PASSWORD);

        List<String> alterSQLList = new ArrayList<>();
        alterSQLList.add("SET NAMES " + CHARSET + ";");
        for (String db : dbList) {
            List<Map<String, Object>> tableInfoList = SQLUtil.select(String.format("SHOW TABLE STATUS FROM %s", db));
            for (Map<String, Object> tableInfo : tableInfoList) {
                Object engine = tableInfo.get("Engine");
                if (engine == null) {
                    continue;
                }
                String tableName = tableInfo.get("Name").toString();
                List<String> alterList = getTableAlterSQL(db, tableName);
                alterSQLList.addAll(alterList);
                if (tableName.contains(".")) {
                    alterSQLList.add(String.format("ALTER TABLE %s DEFAULT CHARSET=%s COLLATE=%s;",
                            tableName, CHARSET, COLLATION));
                } else {
                    alterSQLList.add(String.format("ALTER TABLE %s.%s DEFAULT CHARSET=%s COLLATE=%s;",
                            db, tableName, CHARSET, COLLATION));
                }
            }
        }
//        File outputFile = new File("alter.sql");
//        try (FileOutputStream fileWriter = new FileOutputStream(outputFile)) {
//            fileWriter.write(String.join("\n", alterSQLList).getBytes(StandardCharsets.UTF_8));
//        }
        for (String sql : alterSQLList) {
            try{
                System.out.println(sql);
                SQLUtil.executeAlter(sql);
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * 获取表修改SQL
     *
     * @param db        库名
     * @param tableName 表名
     * @return 获取结果
     * @author lroyia
     * @since 2022年2月17日 10:07:04
     */
    public static List<String> getTableAlterSQL(String db, String tableName) throws Exception {
        List<Map<String, Object>> tableInfo;
        if (tableName.contains(".")) {
            tableInfo = SQLUtil.select(String.format(
                    "SHOW FULL COLUMNS FROM %s", tableName
            ));
        } else {
            tableInfo = SQLUtil.select(String.format(
                    "SHOW FULL COLUMNS FROM %s.%s", db, tableName
            ));
        }
        List<String> resultList = new ArrayList<>();
        for (Map<String, Object> each : tableInfo) {
            String field = each.get("Field").toString();
            String type = each.get("Type").toString();
            if(!(type.contains("char") || type.contains("text"))){
                continue;
            }
            String isNull = each.get("Null").toString().equals("YES") ? "NULL" : "NOT NULL";
            String comment = each.get("Comment").toString();
            if (tableName.contains(".")) {
                resultList.add(String.format("ALTER TABLE %s " +
                                "MODIFY COLUMN %s %s " +
                                "CHARACTER SET %s COLLATE %s " +
                                "%s COMMENT '%s';",
                        tableName,
                        field, type,
                        CHARSET, COLLATION,
                        isNull, comment
                ));
            } else {
                resultList.add(String.format("ALTER TABLE %s.%s " +
                                "MODIFY COLUMN %s %s " +
                                "CHARACTER SET %s COLLATE %s " +
                                "%s COMMENT '%s';",
                        db, tableName,
                        field, type,
                        CHARSET, COLLATION,
                        isNull, comment
                ));
            }
        }
        return resultList;
    }
}
