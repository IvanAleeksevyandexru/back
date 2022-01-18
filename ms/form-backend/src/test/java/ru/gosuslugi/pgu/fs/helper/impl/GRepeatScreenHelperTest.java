package ru.gosuslugi.pgu.fs.helper.impl;

import org.junit.Assert;
import org.junit.Test;
import ru.gosuslugi.pgu.dto.descriptor.types.ScreenType;

public class GRepeatScreenHelperTest {

    @Test
    public void testType() {
        GRepeatScreenHelper helper = new GRepeatScreenHelper();
        Assert.assertNotNull(helper);
        Assert.assertTrue(helper.getType() == ScreenType.GREPEATABLE);
    }

    @Test
    public void testNewType() {
        GRepeatScreenHelper helper = new GRepeatScreenHelper();
        Assert.assertNotNull(helper);
        Assert.assertTrue(helper.getNewType() == ScreenType.REPEATABLE);
    }

}
