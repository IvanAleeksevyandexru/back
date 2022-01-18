package ru.gosuslugi.pgu.fs.helper.impl;

import org.junit.Assert;
import org.junit.Test;

import ru.gosuslugi.pgu.dto.descriptor.types.ScreenType;

public class GUniqueScreenHelperTest {

    @Test
    public void testType() {
        GUniqueScreenHelper helper = new GUniqueScreenHelper();
        Assert.assertNotNull(helper);
        Assert.assertTrue(helper.getType() == ScreenType.GUNIQUE);
    }

    @Test
    public void testNewType() {
        GUniqueScreenHelper helper = new GUniqueScreenHelper();
        Assert.assertNotNull(helper);
        Assert.assertTrue(helper.getNewType() == ScreenType.UNIQUE);
    }

}
