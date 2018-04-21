package com.knwedu.db;

public interface DBConstants {

    public static final int DB_VERSION = 1;

    public static final String DB_NAME = "CPSDataBase.db";
    /* public static final String DB_NAME = Environment.getExternalStorageDirectory() + "/CPSDataBase.db";*/ //This is for debugging purpose only

    public static final String _ID = "_id";

    final String CREATE_TABLE_BASE = "CREATE TABLE IF NOT EXISTS ";

    final String ON = " ON ";

    final String PRIMARY_KEY = " PRIMARY KEY";

    final String INTEGER = " Integer";

    final String TEXT = " TEXT";

    final String DATE_TIME = " DATETIME";

    final String BLOB = " BLOB";

    final String AUTO_ICNREMENT = " AUTOINCREMENT";

    final String UNIQUE = "UNIQUE";

    final String START_COLUMN = " ( ";

    final String FINISH_COLUMN = " ) ";

    final String COMMA = ",";

    final String ON_CONFLICT_REPLACE = "ON CONFLICT REPLACE";


    // OFFLINE NOTIFICATION Table
    public static final String NOTIFICATION_TABLE = " notificationTable";

    public static final String NOTIFICATION_TITLE = "notificationTitle";

    public static final String NOTIFICATION_MESSAGE = "notificationMessage";

    public static final String ROLE = "role";

    public static final String MODULE = "module";

    public static final String ORGANIZATIONID = "organizationId";

    public static final String TIMESTAMP = "timestamp";
}
