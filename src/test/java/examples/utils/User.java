package examples.utils;

public class User {
    private static String id;
    private static String object;

    public static String getId() {
        return id;
    }

    public static void setId(String id) {
        User.id = id;
    }

    public static String getObject() {
        return object;
    }

    public static void setObject(String object) {
        User.object = object;
    }
}
