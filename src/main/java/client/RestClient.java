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

import static io.restassured.RestAssured.given;

public class RestClient {
    private static final Logger logger = LoggerFactory.getLogger(RestClient.class);

    @Step("Create user")
    public Response createUser(User user) {
        try {
            return given()
                    .contentType(ContentType.JSON)
                    .body(user)
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
                    .body(credentials)
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
                    .body(user)
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
                    .body(order)
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
                    .body("{\"token\": \"" + refreshToken + "\"}")
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
}