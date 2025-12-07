package com.example.ProjectTeam121.Security;

public class SQLValidator {

    private static final String[] DANGEROUS_KEYWORDS = {
            "DROP", "DELETE", "UPDATE", "INSERT", "ALTER", "TRUNCATE"
    };

    public static boolean isSafe(String sql) {
        if (sql == null) return false;

        String upper = sql.toUpperCase();

        for (String danger : DANGEROUS_KEYWORDS) {
            if (upper.contains(danger)) return false;
        }

        return true;
    }


}
