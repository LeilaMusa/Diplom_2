import client.RestClient;
import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import model.Order;
import model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.Collections;
import static org.hamcrest.Matchers.*;
import static org.apache.http.HttpStatus.*;

public class OrderCreationTest {
    private RestClient client;
    private User user;
    private static final String VALID_INGREDIENT = "61c0c5a71d1f82001bdaaa6d";
    private static final String INVALID_INGREDIENT = "invalid_ingredient_hash";

    @Before
    @Step("Настройка тестового окружения")
    public void setUp() {
        client = new RestClient();
        user = User.builder()
                .email("test" + System.currentTimeMillis() + "@test.com")
                .password("password123")
                .name("TestUser")
                .build();
    }

    @After
    @Step("Очистка тестового окружения")
    public void tearDown() {
        String accessToken = client.createUser(user).path("accessToken");
        if (accessToken != null) {
            client.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Create order with authorization")
    @Description("Test to verify that an authorized user can create an order with valid ingredients")
    public void createOrderWithAuthTest() {
        String accessToken = createUserAndGetToken();
        Order order = createOrder(Collections.singletonList(VALID_INGREDIENT));
        Response response = createOrderWithAuth(accessToken, order);
        validateSuccessfulOrderCreation(response);
    }

    @Test
    @DisplayName("Create order without authorization")
    @Description("Test to verify that an order cannot be created without authorization")
    public void createOrderWithoutAuthTest() {
        Order order = createOrder(Collections.singletonList(VALID_INGREDIENT));
        Response response = createOrderWithoutAuth(order);
        validateUnauthorizedOrderCreation(response);
    }

    @Test
    @DisplayName("Create order without ingredients")
    @Description("Test to verify that an order cannot be created without ingredients")
    public void createOrderWithoutIngredientsTest() {
        String accessToken = createUserAndGetToken();
        Order order = createOrder(Collections.emptyList());
        Response response = createOrderWithAuth(accessToken, order);
        validateOrderCreationWithoutIngredients(response);
    }

    @Test
    @DisplayName("Create order with invalid ingredient hash")
    @Description("Test to verify that an order cannot be created with an invalid ingredient hash")
    public void createOrderWithInvalidIngredientTest() {
        String accessToken = createUserAndGetToken();
        Order order = createOrder(Collections.singletonList(INVALID_INGREDIENT));
        Response response = createOrderWithAuth(accessToken, order);
        validateOrderCreationWithInvalidIngredient(response);
    }

    @Step("Создание пользователя и получение токена")
    private String createUserAndGetToken() {
        return client.createUser(user).path("accessToken");
    }

    @Step("Создание заказа с ингредиентами: {ingredients}")
    private Order createOrder(java.util.List<String> ingredients) {
        return Order.builder()
                .ingredients(ingredients)
                .build();
    }

    @Step("Создание заказа с авторизацией")
    private Response createOrderWithAuth(String accessToken, Order order) {
        return client.createOrder(accessToken, order);
    }

    @Step("Создание заказа без авторизации")
    private Response createOrderWithoutAuth(Order order) {
        return client.createOrder(null, order);
    }

    @Step("Проверка успешного создания заказа")
    private void validateSuccessfulOrderCreation(Response response) {
        response.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("order", notNullValue())
                .body("name", notNullValue());
    }

    @Step("Проверка создания заказа без авторизации")
    private void validateUnauthorizedOrderCreation(Response response) {
        int actualStatusCode = response.getStatusCode();
        if (actualStatusCode == SC_OK) {
            System.out.println("⚠️ ВНИМАНИЕ: API не требует авторизации, хотя должен!");
        }

        response.then()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @Step("Проверка создания заказа без ингредиентов")
    private void validateOrderCreationWithoutIngredients(Response response) {
        response.then()
                .statusCode(SC_BAD_REQUEST)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Step("Проверка создания заказа с неверным хэшем ингредиента")
    private void validateOrderCreationWithInvalidIngredient(Response response) {
        response.then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
    }
}