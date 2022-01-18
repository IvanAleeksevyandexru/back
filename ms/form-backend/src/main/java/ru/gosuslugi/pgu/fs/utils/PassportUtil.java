package ru.gosuslugi.pgu.fs.utils;

public class PassportUtil {

    public static String formatIssueId(String issueId) {
        return (issueId != null && issueId.length() == 6)
            ? issueId.substring(0, 3) + "-" + issueId.substring(3, 6)
            : issueId;
    }

}
