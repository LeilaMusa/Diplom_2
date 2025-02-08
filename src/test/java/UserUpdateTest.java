import client.RestClient;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import model.User;
import model.UserCredentials;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.Matchers.*;

public class UserUpdateTest {
    private RestClient client;
    private String accessToken;
    private User user;

    @Before
    public void setUp() {
        client = new RestClient();
        user = new User("test" + System.currentTimeMillis() + "@test.com", "password123", "TestUser");
        accessToken = client.createUser(user).path("accessToken");
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            client.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Update user with authorization")
    public void updateUserWithAuthTest() {
        User updatedUser = new User(user.getEmail(), user.getPassword(), "NewName");

        Response response = client.updateUser(accessToken, updatedUser);

        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.name", equalTo("NewName"));
    }

    @Test
    @DisplayName("Update user email with authorization")
    public void updateUserEmailWithAuthTest() {
        String newEmail = "updated" + System.currentTimeMillis() + "@test.com";
        User updatedUser = new User(newEmail, user.getPassword(), user.getName());

        Response response = client.updateUser(accessToken, updatedUser);

        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(newEmail));
    }

    @Test
    @DisplayName("Update user without authorization")
    public void updateUserWithoutAuthTest() {
        User updatedUser = new User(user.getEmail(), user.getPassword(), "NewName");

        Response response = client.updateUser(null, updatedUser);

        response.then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @Test
    @DisplayName("Update user password with authorization")
    public void updateUserPasswordWithAuthTest() {
        String newPassword = "newPassword123";
        User updatedUser = new User(user.getEmail(), newPassword, user.getName());

        Response response = client.updateUser(accessToken, updatedUser);

        response.then()
                .statusCode(200)
                .body("success", equalTo(true));

        // Verify we can login with new password
        UserCredentials newCredentials = UserCredentials.builder()
                .email(user.getEmail())
                .password(newPassword)
                .build();

        Response loginResponse = client.loginUser(newCredentials);
        loginResponse.then()
                .statusCode(200)
                .body("success", equalTo(true));
    }
}