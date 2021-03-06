package club.beingsoft.restaurants.controller;

import club.beingsoft.restaurants.model.Dish;
import club.beingsoft.restaurants.model.User;
import club.beingsoft.restaurants.to.DishTo;
import club.beingsoft.restaurants.util.SecurityUtil;
import club.beingsoft.restaurants.util.exception.EntityDeletedException;
import club.beingsoft.restaurants.util.exception.NotFoundException;
import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import java.util.List;

import static club.beingsoft.restaurants.DishTestData.*;
import static club.beingsoft.restaurants.UserTestData.ADMIN;
import static club.beingsoft.restaurants.util.DishUtil.asTo;
import static club.beingsoft.restaurants.util.DishUtil.createNewFromTo;
@RunWith(SpringRunner.class)
@SpringBootTest
@Sql(scripts = "classpath:data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class DishControllerTest {
    private static final Logger log = LoggerFactory.getLogger(DishControllerTest.class);

    @Autowired
    private DishController dishController;

    private static MockedStatic<SecurityUtil> securityUtilMocked;

    @BeforeClass
    public static void beforeAll() {
        securityUtilMocked = Mockito.mockStatic(SecurityUtil.class);
        User user = ADMIN;
        securityUtilMocked.when(SecurityUtil::getAuthUser).thenReturn(user);
    }

    @AfterClass
    public static void close() {
        securityUtilMocked.close();
    }

    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            System.out.println("Starting test: " + description.getMethodName());
        }
    };

    @Test
    public void getAllDishes() {
        List<Dish> dishesDB = dishController.getAll();
        DISH_MATCHER.assertMatch(dishesDB, DISHES);
    }

    @Test
    public void getDish() {
        Dish dishDB = dishController.get(DISH_1_ID);
//        Assert.assertEquals(DISH_1, dishDB);
        DISH_MATCHER.assertMatch(dishDB, DISH_1);
    }

    @Test
    public void getDishNullId() {
        Assert.assertThrows(ConstraintViolationException.class, () -> dishController.get(null));
    }

    @Test
    public void getDishNotFound() {
        Assert.assertThrows(NotFoundException.class, () -> dishController.get(NOT_FOUND_ID));
    }

    @Test
    public void saveNewDish() {
        DishTo dishDB = dishController.save(null, getNewDish()).getBody();
        assert dishDB != null;
        DishTo newDish = getNewDish();
        newDish.setId(dishDB.getId());
        DISH_MATCHER.assertMatch(createNewFromTo(dishDB), createNewFromTo(newDish));
    }

    @Test
    public void saveNewNullDish() {
        Assert.assertThrows(ConstraintViolationException.class, () -> dishController.save(null, null));
    }

    @Test
    public void updateDish() {
        dishController.save(DISH_3_ID, asTo(UPDATED_DISH));
        Dish dishDB = dishController.get(DISH_3_ID);
        DISH_MATCHER.assertMatch(dishDB, UPDATED_DISH);
    }

    @Test
    @Transactional
    public void deleteDish() {
        dishController.delete(DELETED_DISH_ID);
        Dish dishDB = dishController.get(DELETED_DISH_ID);
        DISH_MATCHER.assertMatch(dishDB, DELETED_DISH);
    }

    @Test
    public void deleteDishNotFound() {
        Assert.assertThrows(NotFoundException.class, () -> dishController.delete(NOT_FOUND_ID));
    }

    @Test
    public void deleteDishInMenu() {
        Assert.assertThrows(EntityDeletedException.class, () -> dishController.delete(DISH_1_ID));
    }

}