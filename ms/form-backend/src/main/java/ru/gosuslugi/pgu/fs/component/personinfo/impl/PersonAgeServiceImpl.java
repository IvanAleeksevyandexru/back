package ru.gosuslugi.pgu.fs.component.personinfo.impl;

import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.common.core.date.model.Accuracy;
import ru.gosuslugi.pgu.common.core.date.util.DateUtil;
import ru.gosuslugi.pgu.fs.component.personinfo.PersonAgeService;
import ru.gosuslugi.pgu.fs.component.personinfo.model.PersonAge;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
class PersonAgeServiceImpl implements PersonAgeService {
    private long getAgeInMonths(String birthDate) {
        LocalDate localDate = LocalDate.now();
        LocalDate parsed = LocalDate.parse(DateUtil.cutString(birthDate, Accuracy.DAY.getName()));
        return ChronoUnit.MONTHS.between(parsed, localDate);
    }

    @Override
    public PersonAge createPersonAge(String birthDate) {
        long ageInMonths = getAgeInMonths(birthDate);
        long years = ageInMonths / 12;
        long months = ageInMonths % 12;

        return new PersonAge(years, months);
    }
}
