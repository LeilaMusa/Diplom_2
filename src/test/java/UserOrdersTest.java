import client.RestClient;
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
    public void setUp() {
        client = new RestClient();
        user = new User("test" + System.currentTimeMillis() + "@test.com", "password123", "TestUser");
        accessToken = client.createUser(user).path("accessToken");

        // Create a test order for the user
        List<String> ingredients = Arrays.asList(VALID_INGREDIENT);
        Order order = new Order(ingredients);
        client.createOrder(accessToken, order);
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            client.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Get orders for authorized user")
    public void getOrdersWithAuthTest() {
        Response response = client.getUserOrders(accessToken);

        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("orders", notNullValue())
                .body("orders", not(empty()));
    }

    @Test
    @DisplayName("Get orders without authorization")
    public void getOrdersWithoutAuthTest() {
        Response response = client.getUserOrders(null);

        response.then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @Test
    @DisplayName("Create order with valid ingredients")
    public void createOrderWithValidIngredientsTest() {
        // Создаем заказ с несколькими действительными ингредиентами
        Order order = Order.builder()
                .ingredients(Arrays.asList(
                        VALID_INGREDIENT,
                        "61c0c5a71d1f82001bdaaa6c",
                        "61c0c5a71d1f82001bdaaa6e"
                ))
                .build();

        Response response = client.createOrder(accessToken, order);

        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order", notNullValue())
                .body("order.ingredients", hasSize(3))  // Проверяем, что все ингредиенты включены
                .body("name", notNullValue());
    }
}
