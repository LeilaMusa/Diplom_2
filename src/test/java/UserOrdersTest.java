import client.RestClient;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import model.Order;
import model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;

public class UserOrdersTest {
    private RestClient client;
    private String accessToken;
    private User user;
    private static final String VALID_INGREDIENT = "61c0c5a71d1f82001bdaaa6d";

    @Before
    @Step("Set up test data: create user and order")
    public void setUp() {
        client = new RestClient();
        user = new User("test" + System.currentTimeMillis() + "@test.com", "password123", "TestUser");
        accessToken = createUserAndGetToken(user);

        // Create a test order for the user
        List<String> ingredients = Arrays.asList(VALID_INGREDIENT);
        Order order = new Order(ingredients);
        createOrderForUser(accessToken, order);
    }

    @After
    @Step("Clean up test data: delete user")
    public void tearDown() {
        if (accessToken != null) {
            deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Get orders for authorized user")
    @Description("Test retrieving orders for an authorized user. Expected: 200 OK, orders list is not empty")
    public void getOrdersWithAuthTest() {
        Response response = getUserOrders(accessToken);

        verifyOrdersResponse(response);
    }

    @Test
    @DisplayName("Get orders without authorization")
    @Description("Test retrieving orders without authorization. Expected: 200 Unauthorized, error message")
    public void getOrdersWithoutAuthTest() {
        Response response = getUserOrders(null);

        verifyUnauthorizedResponse(response);
    }

    @Test
    @DisplayName("Create order with valid ingredients")
    @Description("Test creating an order with valid ingredients. Expected: 200 OK, order details are returned")
    public void createOrderWithValidIngredientsTest() {
        // Создаем заказ с несколькими действительными ингредиентами
        Order order = Order.builder()
                .ingredients(Arrays.asList(
                        VALID_INGREDIENT,
                        "61c0c5a71d1f82001bdaaa6c",
                        "61c0c5a71d1f82001bdaaa6e"
                ))
                .build();

        Response response = createOrderForUser(accessToken, order);

        verifyOrderCreationResponse(response);
    }

    @Step("Create user and get access token")
    private String createUserAndGetToken(User user) {
        return client.createUser(user).path("accessToken");
    }

    @Step("Create order for user")
    private Response createOrderForUser(String accessToken, Order order) {
        return client.createOrder(accessToken, order);
    }

    @Step("Get user orders")
    private Response getUserOrders(String accessToken) {
        return client.getUserOrders(accessToken);
    }

    @Step("Delete user")
    private void deleteUser(String accessToken) {
        client.deleteUser(accessToken);
    }

    @Step("Verify orders response: status code 200, orders list is not empty")
    private void verifyOrdersResponse(Response response) {
        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("orders", notNullValue())
                .body("orders", not(empty()));
    }

    @Step("Verify unauthorized response: status code 401, error message")
    private void verifyUnauthorizedResponse(Response response) {
        response.then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @Step("Verify order creation response: status code 200, order details are returned")
    private void verifyOrderCreationResponse(Response response) {
        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order", notNullValue())
                .body("order.ingredients", hasSize(3))  // Проверяем, что все ингредиенты включены
                .body("name", notNullValue());
    }
}