package ru.gosuslugi.pgu.fs.component.personinfo;

import ru.gosuslugi.pgu.fs.component.personinfo.model.PersonAge;

public interface PersonAgeService {

    /**
     * Создает сущность возраста на текущую дату
     * @param birthDate дата рождения в ISO-8601
     * @return новый экземпляр {@link PersonAge}
     */
    PersonAge createPersonAge(String birthDate);
}
