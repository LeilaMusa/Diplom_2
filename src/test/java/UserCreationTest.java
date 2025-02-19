import client.RestClient;
import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.Matchers.*;

public class UserCreationTest {
    private RestClient client;
    private String accessToken;
    private User user;

    @Before
    @Step("Настройка тестового окружения")
    public void setUp() {
        client = new RestClient();
    }

    @After
    @Step("Очистка тестового окружения")
    public void tearDown() {
        if (accessToken != null) {
            deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Создание уникального пользователя")
    @Description("Тест проверяет успешное создание пользователя с уникальными данными")
    public void createUniqueUserTest() {
        user = createUser("test" + System.currentTimeMillis() + "@test.com", "password123", "TestUser");
        Response response = createUserViaApi(user);
        validateSuccessfulUserCreation(response);
        accessToken = extractAccessToken(response);
    }

    @Test
    @DisplayName("Создание дубликата пользователя")
    @Description("Тест проверяет, что нельзя создать пользователя с уже существующим email")
    public void createDuplicateUserTest() {
        user = createUser("test" + System.currentTimeMillis() + "@test.com", "password123", "TestUser");
        createUserViaApi(user); // Первое создание пользователя
        Response response = createUserViaApi(user); // Попытка создания дубликата
        validateDuplicateUserCreation(response);
    }

    @Test
    @DisplayName("Создание пользователя без email")
    @Description("Тест проверяет, что нельзя создать пользователя без указания email")
    public void createUserWithoutEmailTest() {
        user = createUser("", "password123", "TestUser");
        Response response = createUserViaApi(user);
        validateUserCreationWithoutRequiredField(response);
    }

    @Test
    @DisplayName("Создание пользователя без пароля")
    @Description("Тест проверяет, что нельзя создать пользователя без указания пароля")
    public void createUserWithoutPasswordTest() {
        user = createUser("test" + System.currentTimeMillis() + "@test.com", "", "TestUser");
        Response response = createUserViaApi(user);
        validateUserCreationWithoutRequiredField(response);
    }

    @Test
    @DisplayName("Создание пользователя без имени")
    @Description("Тест проверяет, что нельзя создать пользователя без указания имени")
    public void createUserWithoutNameTest() {
        user = createUser("test" + System.currentTimeMillis() + "@test.com", "password123", "");
        Response response = createUserViaApi(user);
        validateUserCreationWithoutRequiredField(response);
    }

    @Step("Создание пользователя с email = {email}, password = {password}, name = {name}")
    private User createUser(String email, String password, String name) {
        return User.builder()
                .email(email)
                .password(password)
                .name(name)
                .build();
    }

    @Step("Создание пользователя через API")
    private Response createUserViaApi(User user) {
        return client.createUser(user);
    }

    @Step("Проверка успешного создания пользователя")
    private void validateSuccessfulUserCreation(Response response) {
        response.then()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Step("Извлечение токена доступа из ответа")
    private String extractAccessToken(Response response) {
        return response.path("accessToken");
    }

    @Step("Проверка создания дубликата пользователя")
    private void validateDuplicateUserCreation(Response response) {
        response.then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @Step("Проверка создания пользователя без обязательного поля")
    private void validateUserCreationWithoutRequiredField(Response response) {
        response.then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Step("Удаление пользователя с токеном {accessToken}")
    private void deleteUser(String accessToken) {
        client.deleteUser(accessToken);
    }
}