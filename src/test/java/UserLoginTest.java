import client.RestClient;
import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import model.User;
import model.UserCredentials;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.Matchers.*;

public class UserLoginTest {
    private RestClient client;
    private String accessToken;
    private User user;

    @Before
    @Step("Настройка тестового окружения")
    public void setUp() {
        client = new RestClient();
        user = createUser("test" + System.currentTimeMillis() + "@test.com", "password123", "TestUser");
        accessToken = createUserViaApi(user).path("accessToken");
    }

    @After
    @Step("Очистка тестового окружения")
    public void tearDown() {
        if (accessToken != null) {
            deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Логин с валидными данными")
    @Description("Тест проверяет успешный логин с корректными email и паролем")
    public void loginValidCredentialsTest() {
        UserCredentials credentials = createCredentials(user.getEmail(), user.getPassword());
        Response response = loginUser(credentials);
        validateSuccessfulLogin(response);
    }

    @Test
    @DisplayName("Логин с неверным email")
    @Description("Тест проверяет, что логин невозможен с неверным email")
    public void loginWithInvalidEmailTest() {
        UserCredentials credentials = createCredentials("wrong@email.com", user.getPassword());
        Response response = loginUser(credentials);
        validateFailedLogin(response);
    }

    @Test
    @DisplayName("Логин с неверным паролем")
    @Description("Тест проверяет, что логин невозможен с неверным паролем")
    public void loginWithInvalidPasswordTest() {
        UserCredentials credentials = createCredentials(user.getEmail(), "wrongpassword");
        Response response = loginUser(credentials);
        validateFailedLogin(response);
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

    @Step("Создание учётных данных с email = {email}, password = {password}")
    private UserCredentials createCredentials(String email, String password) {
        return UserCredentials.builder()
                .email(email)
                .password(password)
                .build();
    }

    @Step("Логин пользователя")
    private Response loginUser(UserCredentials credentials) {
        return client.loginUser(credentials);
    }

    @Step("Проверка успешного логина")
    private void validateSuccessfulLogin(Response response) {
        response.then()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Step("Проверка неудачного логина")
    private void validateFailedLogin(Response response) {
        response.then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Step("Удаление пользователя с токеном {accessToken}")
    private void deleteUser(String accessToken) {
        client.deleteUser(accessToken);
    }
}