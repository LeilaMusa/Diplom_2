import client.RestClient;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import model.Order;
import model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import static org.hamcrest.Matchers.*;

public class OrderCreationTest {
    private RestClient client;
    private String accessToken;
    private User user;
    private static final String VALID_INGREDIENT = "61c0c5a71d1f82001bdaaa6d";
    private static final String INVALID_INGREDIENT = "invalid_ingredient_hash";

    @Before
    public void setUp() {
        client = new RestClient();
        user = User.builder()
                .email("test" + System.currentTimeMillis() + "@test.com")
                .password("password123")
                .name("TestUser")
                .build();
        accessToken = client.createUser(user).path("accessToken");
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            client.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Create order with authorization")
    public void createOrderWithAuthTest() {
        Order order = Order.builder()
                .ingredients(Arrays.asList(VALID_INGREDIENT))
                .build();

        Response response = client.createOrder(accessToken, order);

        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order", notNullValue())
                .body("name", notNullValue());
    }

    @Test
    @DisplayName("Create order without authorization")
    public void createOrderWithoutAuthTest() {
        Order order = Order.builder()
                .ingredients(Arrays.asList(VALID_INGREDIENT))
                .build();

        Response response = client.createOrder(null, order);

        int actualStatusCode = response.getStatusCode();
        if (actualStatusCode == 200) {
            System.out.println("⚠️ ВНИМАНИЕ: API не требует авторизации, хотя должен!");
        }

        response.then()
                .statusCode(401)  // Ожидаемый код (API должно быть исправлено)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @Test
    @DisplayName("Create order without ingredients")
    public void createOrderWithoutIngredientsTest() {
        Order order = Order.builder()
                .ingredients(Collections.emptyList())
                .build();

        Response response = client.createOrder(accessToken, order);

        response.then()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Create order with invalid ingredient hash")
    public void createOrderWithInvalidIngredientTest() {
        Order order = Order.builder()
                .ingredients(Arrays.asList(INVALID_INGREDIENT))
                .build();

        Response response = client.createOrder(accessToken, order);

        response.then()
                .statusCode(500);
    }
}
