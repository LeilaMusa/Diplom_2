package constants;

public class ApiEndpoints {
    public static final String BASE_URL = "https://stellarburgers.nomoreparties.site/api";

    // Auth endpoints
    public static final String REGISTER = BASE_URL + "/auth/register";
    public static final String LOGIN = BASE_URL + "/auth/login";
    public static final String LOGOUT = BASE_URL + "/auth/logout";
    public static final String USER = BASE_URL + "/auth/user";

    // Order endpoints
    public static final String ORDERS = BASE_URL + "/orders";
    public static final String ALL_ORDERS = BASE_URL + "/orders/all";

    // Ingredients endpoint
    public static final String INGREDIENTS = BASE_URL + "/ingredients";
}