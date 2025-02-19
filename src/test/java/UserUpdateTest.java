import client.RestClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import model.User;
import model.UserCredentials;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.github.javafaker.Faker;

import static org.hamcrest.Matchers.*;

public class UserUpdateTest {
    private RestClient client;
    private String accessToken;
    private User user;
    private Faker faker;

    @Before
    @Step("Set up test data")
    public void setUp() {
        client = new RestClient();
        faker = new Faker();
        user = new User(
                faker.internet().emailAddress(),
                faker.internet().password(),
                faker.name().fullName()
        );
        accessToken = client.createUser(user).path("accessToken");
    }

    @After
    @Step("Clean up test data")
    public void tearDown() {
        if (accessToken != null) {
            client.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Update user name with authorization")
    @Description("Test updating user name with valid authorization token")
    public void updateUserNameWithAuthTest() {
        User updatedUser = new User(user.getEmail(), user.getPassword(), "NewName");

        Response response = client.updateUser(accessToken, updatedUser);

        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.name", equalTo("NewName"));
    }

    @Test
    @DisplayName("Update user email with authorization")
    @Description("Test updating user email with valid authorization token")
    public void updateUserEmailWithAuthTest() {
        String newEmail = faker.internet().emailAddress();
        User updatedUser = new User(newEmail, user.getPassword(), user.getName());

        Response response = client.updateUser(accessToken, updatedUser);

        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(newEmail));
    }

    @Test
    @DisplayName("Update user without authorization")
    @Description("Test updating user without authorization token")
    public void updateUserWithoutAuthTest() {
        User updatedUser = new User(user.getEmail(), user.getPassword(), "NewName");

        Response response = client.updateUser(null, updatedUser);

        response.then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }
}