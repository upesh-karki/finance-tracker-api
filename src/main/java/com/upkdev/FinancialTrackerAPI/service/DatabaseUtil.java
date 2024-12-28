package com.upkdev.FinancialTrackerAPI.service;

import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUtil {

    public static List<String> getAllStoredProcedures(JdbcTemplate jdbcTemplate) {
        List<String> storedProcedures = new ArrayList<>();
        try {
            DatabaseMetaData metaData = jdbcTemplate.getDataSource().getConnection().getMetaData();
            ResultSet rs = metaData.getProcedures(null, null, "%");
            while (rs.next()) {
                storedProcedures.add(rs.getString("PROCEDURE_NAME"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return storedProcedures;
    }
}
