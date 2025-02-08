import client.RestClient;
import io.qameta.allure.junit4.DisplayName;
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
    @DisplayName("Login with valid credentials")
    public void loginValidCredentialsTest() {
        UserCredentials credentials = UserCredentials.builder()
                .email(user.getEmail())
                .password(user.getPassword())
                .build();

        Response response = client.loginUser(credentials);

        response.then()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Test
    @DisplayName("Login with invalid credentials")
    public void loginInvalidCredentialsTest() {
        UserCredentials credentials = UserCredentials.builder()
                .email("wrong@email.com")
                .password("wrongpassword")
                .build();

        Response response = client.loginUser(credentials);

        response.then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }
}