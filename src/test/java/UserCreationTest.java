import client.RestClient;
import io.qameta.allure.junit4.DisplayName;
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
    public void setUp() {
        client = new RestClient();
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            client.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Create unique user successfully")
    public void createUniqueUserTest() {
        user = User.builder()
                .email("test" + System.currentTimeMillis() + "@test.com")
                .password("password123")
                .name("TestUser")
                .build();

        Response response = client.createUser(user);
        response.then()
                .statusCode(200)
                .body("success", equalTo(true));

        accessToken = response.path("accessToken");
    }

    @Test
    @DisplayName("Cannot create duplicate user")
    public void createDuplicateUserTest() {
        user = User.builder()
                .email("test" + System.currentTimeMillis() + "@test.com")
                .password("password123")
                .name("TestUser")
                .build();

        client.createUser(user);
        Response response = client.createUser(user);

        response.then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @Test
    @DisplayName("Cannot create user without required field")
    public void createUserWithoutRequiredFieldTest() {
        user = User.builder()
                .email("")
                .password("password123")
                .name("TestUser")
                .build();

        Response response = client.createUser(user);

        response.then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }
}
