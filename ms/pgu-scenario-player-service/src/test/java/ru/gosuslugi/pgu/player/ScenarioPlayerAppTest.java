package ru.gosuslugi.pgu.player;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.testng.Assert;
import ru.gosuslugi.pgu.player.controller.ScenarioController;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = ScenarioPlayerApp.class, properties = "data.cache.enabled=false")
public class ScenarioPlayerAppTest {

    @Autowired
    private ScenarioController controller;

    @Test
    public void testContextLoads(){
        Assert.assertNotNull(controller);
    }
}
