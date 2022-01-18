package ru.gosuslugi.pgu.fs.helper.impl;

import org.junit.Assert;
import org.junit.Test;

import ru.gosuslugi.pgu.dto.descriptor.types.ScreenType;

public class GCustomScreenHelperTest {

    @Test
    public void testType() {
        GCustomScreenHelper helper = new GCustomScreenHelper();
        Assert.assertNotNull(helper);
        Assert.assertTrue(helper.getType() == ScreenType.GCUSTOM);
    }

    @Test
    public void testNewType() {
        GCustomScreenHelper helper = new GCustomScreenHelper();
        Assert.assertNotNull(helper);
        Assert.assertTrue(helper.getNewType() == ScreenType.CUSTOM);
    }

}
