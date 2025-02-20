package client;

import constants.ApiEndpoints;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import model.Order;
import model.User;
import model.UserCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;

import static io.restassured.RestAssured.given;

public class RestClient {
    private static final Logger logger = LoggerFactory.getLogger(RestClient.class);
    private static final Gson gson = new Gson();

    @Step("Create user")
    public Response createUser(User user) {
        try {
            return given()
                    .contentType(ContentType.JSON)
                    .body(gson.toJson(user))
                    .post(ApiEndpoints.REGISTER);
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage());
            throw e;
        }
    }

    @Step("Login user")
    public Response loginUser(UserCredentials credentials) {
        try {
            return given()
                    .contentType(ContentType.JSON)
                    .body(gson.toJson(credentials))
                    .post(ApiEndpoints.LOGIN);
        } catch (Exception e) {
            logger.error("Error logging in user: {}", e.getMessage());
            throw e;
        }
    }

    @Step("Update user")
    public Response updateUser(String accessToken, User user) {
        try {
            return given()
                    .contentType(ContentType.JSON)
                    .when()
                    .headers(accessToken != null ? "Authorization" : "", accessToken != null ? accessToken : "")
                    .body(gson.toJson(user))
                    .patch(ApiEndpoints.USER);
        } catch (Exception e) {
            logger.error("Error updating user: {}", e.getMessage());
            throw e;
        }
    }

    @Step("Create order")
    public Response createOrder(String accessToken, Order order) {
        try {
            return given()
                    .contentType(ContentType.JSON)
                    .when()
                    .headers(accessToken != null ? "Authorization" : "", accessToken != null ? accessToken : "")
                    .body(gson.toJson(order))
                    .post(ApiEndpoints.ORDERS);
        } catch (Exception e) {
            logger.error("Error creating order: {}", e.getMessage());
            throw e;
        }
    }

    @Step("Get user orders")
    public Response getUserOrders(String accessToken) {
        try {
            return given()
                    .contentType(ContentType.JSON)
                    .when()
                    .headers(accessToken != null ? "Authorization" : "", accessToken != null ? accessToken : "")
                    .get(ApiEndpoints.ORDERS);
        } catch (Exception e) {
            logger.error("Error getting user orders: {}", e.getMessage());
            throw e;
        }
    }

    @Step("Delete user")
    public Response deleteUser(String accessToken) {
        try {
            return given()
                    .header("Authorization", accessToken)
                    .delete(ApiEndpoints.USER);
        } catch (Exception e) {
            logger.error("Error deleting user: {}", e.getMessage());
            throw e;
        }
    }

    @Step("Logout user")
    public Response logoutUser(String refreshToken) {
        try {
            return given()
                    .contentType(ContentType.JSON)
                    .body(gson.toJson(new LogoutRequest(refreshToken)))
                    .post(ApiEndpoints.LOGOUT);
        } catch (Exception e) {
            logger.error("Error logging out user: {}", e.getMessage());
            throw e;
        }
    }

    @Step("Get all orders")
    public Response getAllOrders() {
        try {
            return given()
                    .get(ApiEndpoints.ALL_ORDERS);
        } catch (Exception e) {
            logger.error("Error getting all orders: {}", e.getMessage());
            throw e;
        }
    }

    @Step("Reset user password")
    public Response resetPassword(UserCredentials credentials) {
        try {
            return given()
                    .contentType(ContentType.JSON)
                    .body(gson.toJson(credentials))
                    .post(ApiEndpoints.PASSWORD_RESET);
        } catch (Exception e) {
            logger.error("Error resetting password: {}", e.getMessage());
            throw e;
        }
    }

    @Step("Reset user password with token")
    public Response resetPasswordWithToken(String newPassword, String resetToken) {
        try {
            PasswordResetRequest request = new PasswordResetRequest(newPassword, resetToken);
            return given()
                    .contentType(ContentType.JSON)
                    .body(gson.toJson(request))
                    .post(ApiEndpoints.PASSWORD_RESET_RESET);
        } catch (Exception e) {
            logger.error("Error resetting password with token: {}", e.getMessage());
            throw e;
        }
    }

    // Вспомогательные классы для сериализации
    private static class LogoutRequest {
        private final String token;

        public LogoutRequest(String token) {
            this.token = token;
        }
    }

    private static class PasswordResetRequest {
        private final String password;
        private final String token;

        public PasswordResetRequest(String password, String token) {
            this.password = password;
            this.token = token;
        }
    }
}