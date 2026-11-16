package ru.ulstu.grade_analysis;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.SelectOption;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import ru.ulstu.grade_analysis.entity.Grade;
import ru.ulstu.grade_analysis.entity.Group;
import ru.ulstu.grade_analysis.entity.Role;
import ru.ulstu.grade_analysis.entity.Subject;
import ru.ulstu.grade_analysis.entity.User;
import ru.ulstu.grade_analysis.repository.GradeRepository;
import ru.ulstu.grade_analysis.repository.GroupRepository;
import ru.ulstu.grade_analysis.repository.RoleRepository;
import ru.ulstu.grade_analysis.repository.SubjectRepository;
import ru.ulstu.grade_analysis.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TeacherStudentE2ETest {

  @LocalServerPort private int port;

  @Autowired private UserRepository userRepository;
  @Autowired private RoleRepository roleRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private SubjectRepository subjectRepository;
  @Autowired private GradeRepository gradeRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private PlatformTransactionManager transactionManager;

  private static Playwright playwright;
  private static Browser browser;
  private Page page;

  private User teacher;
  private Group groupBachelor;
  private Group groupMaster;
  private Subject subjectVychmat;

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

    // Вся подготовка данных — в ОДНОЙ транзакции с ОДНИМ коммитом
    // (иначе detached-entity merge ломается на teacher_groups).
    TransactionTemplate tx = new TransactionTemplate(transactionManager);
    tx.executeWithoutResult(
        status -> {
          // Очистка БД (в той же транзакции, чтобы был тот же коннект)
          jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
          jdbcTemplate.execute("DELETE FROM teacher_groups");
          jdbcTemplate.execute("DELETE FROM student_groups");
          jdbcTemplate.execute("DELETE FROM teacher_subjects");
          jdbcTemplate.execute("DELETE FROM subject_groups");
          jdbcTemplate.execute("DELETE FROM grades");
          jdbcTemplate.execute("DELETE FROM subjects");
          jdbcTemplate.execute("DELETE FROM groups");
          jdbcTemplate.execute("DELETE FROM users");
          jdbcTemplate.execute("DELETE FROM roles");
          jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");

          // 1. Роли
          Role teacherRole = roleRepository.save(new Role(null, "TEACHER"));
          Role studentRole = roleRepository.save(new Role(null, "STUDENT"));

          // 2. Преподаватель
          teacher = new User();
          teacher.setUsername("e2e_teacher");
          teacher.setPasswordHash(passwordEncoder.encode("pass"));
          teacher.setLastName("Анкилов");
          teacher.setFirstName("Андрей");
          teacher.setMiddleName("Владимирович");
          teacher.setRole(teacherRole);
          teacher = userRepository.save(teacher);

          // 3. Группы
          groupBachelor = new Group();
          groupBachelor.setPrefix("ПИбд");
          groupBachelor.setCourse("1");
          groupBachelor.setSubgroup("4");
          groupBachelor.setEducationLevel(Group.EducationLevel.BACHELOR);
          groupBachelor = groupRepository.save(groupBachelor);

          groupMaster = new Group();
          groupMaster.setPrefix("ПИМбд");
          groupMaster.setCourse("2");
          groupMaster.setSubgroup("2");
          groupMaster.setEducationLevel(Group.EducationLevel.MASTER);
          groupMaster = groupRepository.save(groupMaster);

          // 4. Связываем препода с группами (обе сущности уже managed)
          groupBachelor.getTeachers().add(teacher);
          groupMaster.getTeachers().add(teacher);

          // 5. Студенты (обе сущности уже managed)
          User kalim =
              createStudent(
                  "e2e_kalim", "Калимуллин", "Артур", "Рамилевич", studentRole, groupBachelor);
          User ered =
              createStudent("e2e_ered", "Ередавкин", "Роман", null, studentRole, groupBachelor);
          User master =
              createStudent("e2e_master", "Магистрант", "Тест", null, studentRole, groupMaster);

          // 6. Предмет
          subjectVychmat = new Subject();
          subjectVychmat.setName("Вычмат");
          subjectVychmat.getTeachers().add(teacher);
          subjectVychmat.getGroups().addAll(Set.of(groupBachelor, groupMaster));
          subjectVychmat = subjectRepository.save(subjectVychmat);

          // 7. Оценки
          createGradeIfNotExists(kalim, subjectVychmat, 5);
          createGradeIfNotExists(ered, subjectVychmat, 3);
          createGradeIfNotExists(master, subjectVychmat, 0);
        });
  }

  private User createStudent(
      String username,
      String lastName,
      String firstName,
      String middleName,
      Role role,
      Group group) {
    User u = new User();
    u.setUsername(username);
    u.setPasswordHash(passwordEncoder.encode("pass"));
    u.setLastName(lastName);
    u.setFirstName(firstName);
    u.setMiddleName(middleName);
    u.setRole(role);
    u.setCourse(group.getCourse());
    User savedUser = userRepository.save(u);
    group.getStudents().add(savedUser);
    return savedUser;
  }

  private void createGradeIfNotExists(User student, Subject subject, int value) {
    Grade g = new Grade();
    g.setStudent(student);
    g.setSubject(subject);
    g.setTeacher(teacher);
    g.setValue(value);
    g.setDate(LocalDate.now());
    gradeRepository.save(g);
  }

  @Test
  void fullFlow_teacherGradesAndStudentSeesRating() {
    // ==========================================
    // ЭТАП 1: Преподаватель
    // ==========================================
    page.navigate("http://localhost:" + port + "/login");
    page.locator("input[type='text']").fill("e2e_teacher");
    page.locator("input[type='password']").fill("pass");
    page.locator("button[type='submit']").click();
    page.waitForURL("**/teacher");

    // Списки студентов
    page.locator("a.nav-link[href='/teacher/students']").click();
    page.waitForURL("**/teacher/students");

    page.waitForResponse(
        r ->
            r.url().contains("/api/teacher/groups/")
                && r.url().contains("/students")
                && r.status() == 200,
        () -> page.locator("select").first().selectOption(new SelectOption().setLabel("ПИбд-14")));
    assertThat(page.locator("table tbody")).containsText("Калимуллин Артур Рамилевич");
    assertThat(page.locator("table tbody")).containsText("Ередавкин Роман");

    // Выставление оценок
    page.locator("a.nav-link[href='/teacher/grades']").click();
    page.waitForURL("**/teacher/grades");

    page.waitForResponse(
        r -> r.url().contains("/api/teacher/grades/subject/") && r.status() == 200,
        () -> page.locator("select").first().selectOption(new SelectOption().setLabel("Вычмат")));
    page.waitForSelector("text=Калимуллин Артур Рамилевич");

    // Калимуллину 5
    page.locator("tr:has-text('Калимуллин Артур Рамилевич')").locator("button").click();
    page.locator(".modal-content select").selectOption(new SelectOption().setLabel("5 (Отлично)"));
    page.locator("button:has-text('Сохранить')").click();
    page.waitForSelector("text=Оценка для Калимуллин Артур Рамилевич успешно сохранена!");

    // Ередавкину 3
    page.locator("tr:has-text('Ередавкин Роман')").locator("button").click();
    page.locator(".modal-content select")
        .selectOption(new SelectOption().setLabel("3 (Удовлетворительно)"));
    page.locator("button:has-text('Сохранить')").click();
    page.waitForSelector("text=Оценка для Ередавкин Роман успешно сохранена!");

    // Магистранту Неявку
    page.locator("tr:has-text('Магистрант Тест')").locator("button").click();
    page.locator(".modal-content select").selectOption(new SelectOption().setLabel("Неявка"));
    page.locator("button:has-text('Сохранить')").click();
    page.waitForSelector("text=Оценка для Магистрант Тест успешно сохранена!");

    // Статистика
    page.locator("a.nav-link[href='/teacher/group-stats']").click();
    page.waitForURL("**/teacher/group-stats");

    page.waitForResponse(
        r ->
            r.url().contains("/api/teacher/groups/")
                && r.url().contains("/subjects")
                && r.status() == 200,
        () -> page.locator("select").first().selectOption(new SelectOption().setLabel("ПИМбд-22")));
    page.locator("select").nth(1).selectOption(new SelectOption().setLabel("Вычмат"));

    page.waitForResponse(
        r -> r.url().contains("/api/teacher/stats") && r.status() == 200,
        () -> page.locator("button:has-text('Сформировать')").click());

    Locator failedCount = page.locator("h5:has-text('Не сдали') + h2");
    assertThat(failedCount).containsText("1");
    Locator masterRow =
        page.locator("tr").filter(new Locator.FilterOptions().setHasText("Магистрант Тест"));
    assertThat(masterRow).containsText("Неявка");

    // ==========================================
    // ЭТАП 2: Выход и вход студентом
    // ==========================================
    page.locator("button:has-text('Выйти')").click();
    page.locator(".modal-footer button:has-text('Выйти')").click();

    // Ждем, пока произойдет выход (URL перестанет содержать /teacher)
    page.waitForURL(url -> !url.toString().contains("/teacher"));

    // Явно переходим на страницу входа
    page.navigate("http://localhost:" + port + "/login");
    page.waitForURL("**/login");

    // Далее логинимся под студентом
    page.locator("input[type='text']").fill("e2e_ered");
    page.locator("input[type='password']").fill("pass");
    page.locator("button[type='submit']").click();
    page.waitForURL("**/student");

    // Мои оценки
    page.locator("a.nav-link[href='/student/grades']").click();
    page.waitForURL("**/student/grades");
    page.waitForSelector("text=Вычмат");
    assertThat(page.locator("tr:has-text('Вычмат')").locator("td").nth(1)).containsText("3");

    // Рейтинг в группе
    page.locator("a.nav-link[href='/student/rating']").click();
    page.waitForURL("**/student/rating");
    page.waitForSelector("text=Топ-3 студента");

    assertThat(page.locator("table tbody tr").nth(0)).containsText("5.0");
    assertThat(page.locator("table tbody tr").nth(1)).containsText("3.0");
    assertThat(page.locator(".display-1")).containsText("2");

    Locator avgText = page.locator("p:has-text('Средний балл:') strong");
    assertThat(avgText).containsText("3.0");
  }
}
