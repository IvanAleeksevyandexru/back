package ru.gosuslugi.pgu.fs.helper.impl;

import org.junit.Assert;
import org.junit.Test;

import ru.gosuslugi.pgu.dto.descriptor.types.ScreenType;

public class GInfoScreenHelperTest {

    @Test
    public void testType() {
        GInfoScreenHelper helper = new GInfoScreenHelper();
        Assert.assertNotNull(helper);
        Assert.assertTrue(helper.getType() == ScreenType.GINFO);
    }

    @Test
    public void testNewType() {
        GInfoScreenHelper helper = new GInfoScreenHelper();
        Assert.assertNotNull(helper);
        Assert.assertTrue(helper.getNewType() == ScreenType.INFO);
    }

}
