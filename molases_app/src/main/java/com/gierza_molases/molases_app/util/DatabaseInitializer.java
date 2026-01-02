package com.gierza_molases.molases_app.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DatabaseInitializer {

    @SuppressWarnings("resource")
	public static void init() {
        try (
            Connection conn = DatabaseUtil.getConnection();
            Statement stmt = conn.createStatement()
        ) {
            InputStream is = DatabaseInitializer.class
                .getClassLoader()
                .getResourceAsStream("database/schema.sql");

            if (is == null) {
                throw new RuntimeException("schema.sql not found");
            }

            String sql = new BufferedReader(new InputStreamReader(is))
                    .lines()
                    .collect(Collectors.joining("\n"));

            stmt.executeUpdate(sql);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
}
