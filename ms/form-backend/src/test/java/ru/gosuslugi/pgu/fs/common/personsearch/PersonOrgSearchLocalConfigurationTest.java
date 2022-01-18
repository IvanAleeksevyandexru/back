package ru.gosuslugi.pgu.fs.common.personsearch;

import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.atc.carcass.security.rest.model.orgs.Org;
import ru.atc.carcass.security.rest.model.person.Person;
import ru.gosuslugi.pgu.common.esia.search.service.OrgSearchService;
import ru.gosuslugi.pgu.common.esia.search.service.PersonSearchService;
import ru.gosuslugi.pgu.common.esia.search.service.impl.OrgSearchServiceStub;
import ru.gosuslugi.pgu.common.esia.search.service.impl.PersonSearchServiceStub;
import ru.gosuslugi.pgu.fs.FormServiceApp;

@ActiveProfiles("local")
@SpringBootTest(classes = FormServiceApp.class)
public class PersonOrgSearchLocalConfigurationTest {

    @Autowired
    PersonSearchService personSearchService;

    @Autowired
    OrgSearchService orgSearchService;

    @Test
    public void findPersonByOid() {
        Assume.assumeThat(personSearchService, Matchers.instanceOf(PersonSearchServiceStub.class));
        Person person = personSearchService.findUserById("1000473012");
        Assertions.assertEquals("Дрезина", person.getFirstName());
    }

    @Test
    public void findOrgByOid() {
        Assume.assumeThat(orgSearchService, Matchers.instanceOf(OrgSearchServiceStub.class));
        Org org = orgSearchService.findOrgById("100500");
        Assertions.assertEquals("100500", org.getOid());
        Assertions.assertEquals("Рога и копыта", org.getShortName());
    }
}
