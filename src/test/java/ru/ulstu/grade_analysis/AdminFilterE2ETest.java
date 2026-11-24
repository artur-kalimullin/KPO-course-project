package ru.ulstu.grade_analysis;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import ru.ulstu.grade_analysis.entity.Group;
import ru.ulstu.grade_analysis.entity.Role;
import ru.ulstu.grade_analysis.entity.User;
import ru.ulstu.grade_analysis.repository.GroupRepository;
import ru.ulstu.grade_analysis.repository.RoleRepository;
import ru.ulstu.grade_analysis.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AdminFilterE2ETest {

  @LocalServerPort private int port;

  @Autowired private UserRepository userRepository;
  @Autowired private RoleRepository roleRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  private static Playwright playwright;
  private static Browser browser;
  private Page page;

  @BeforeAll
  static void launchBrowser() {
    playwright = Playwright.create();
    browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
  }

  @AfterAll
  static void closeBrowser() {
    browser.close();
    playwright.close();
  }

  @BeforeEach
  void setUp() {
    page = browser.newPage();

    Role adminRole =
        roleRepository
            .findByName("ADMIN")
            .orElseGet(() -> roleRepository.save(new Role(null, "ADMIN")));
    if (userRepository.findByUsername("e2e_admin").isEmpty()) {
      User admin = new User();
      admin.setUsername("e2e_admin");
      admin.setPasswordHash(passwordEncoder.encode("e2e_pass"));
      admin.setLastName("Админ");
      admin.setFirstName("Е2Е");
      admin.setRole(adminRole);
      userRepository.save(admin);
    }

    if (groupRepository.count() < 17) {
      for (int i = 1; i <= 17; i++) {
        Group g = new Group();
        g.setPrefix("ТЕСТ");
        g.setCourse("1");
        g.setSubgroup(String.valueOf(i % 9 + 1));
        g.setEducationLevel(Group.EducationLevel.BACHELOR);
        groupRepository.save(g);
      }
    }
  }

  @Test
  void adminCanLoginAndUseFilters() {
    page.navigate("http://localhost:" + port + "/login");

    page.locator("input[type='text']").fill("e2e_admin");
    page.locator("input[type='password']").fill("e2e_pass");
    page.locator("button[type='submit']").click();

    page.waitForURL("**/admin");
    assertThat(page.locator("h1")).containsText("Добро пожаловать, Администратор!");

    Response apiResponse =
        page.waitForResponse(
            response ->
                response.url().contains("/api/admin/groups")
                    && response.request().method().equals("GET"),
            () -> page.locator("a:has-text('Группы')").click());
    assertThat(apiResponse.status()).isEqualTo(200);
    page.waitForURL("**/admin/groups");

    page.waitForSelector("tbody tr");
    int rowCount = page.locator("tbody tr").count();
    assertThat(rowCount).isEqualTo(16);
    assertThat(page.locator(".pagination")).isVisible();

    page.waitForResponse(
        response ->
            response.url().contains("/api/admin/groups?page=1&search=") && response.status() == 200,
        () -> page.locator("input[placeholder='Поиск по названию']").fill("ТЕСТ-11"));
    int filteredRowCount = page.locator("tbody tr").count();
    assertThat(filteredRowCount).isEqualTo(1);

    page.waitForResponse(
        response -> response.url().contains("/api/admin/groups?page=1") && response.status() == 200,
        () -> page.locator("input[placeholder='Поиск по названию']").fill(""));

    page.locator("button:has-text('Добавить группу')").click();

    page.locator(".modal-content input[type='text']").fill("ЭТЭ");

    page.waitForResponse(
        response ->
            response.url().contains("/api/admin/groups")
                && response.request().method().equals("POST"),
        () -> page.locator("button:has-text('Сохранить')").click());

    page.waitForResponse(
        response ->
            response.url().contains("/api/admin/groups?page=1&search=") && response.status() == 200,
        () -> page.locator("input[placeholder='Поиск по названию']").fill("ЭТЭ"));

    page.waitForSelector("text=ЭТЭ-11");
    assertThat(page.locator("td:has-text('ЭТЭ-11')")).isVisible();
  }
}
