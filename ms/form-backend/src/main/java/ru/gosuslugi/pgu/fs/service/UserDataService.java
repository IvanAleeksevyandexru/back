package ru.gosuslugi.pgu.fs.service;

import ru.atc.carcass.security.rest.model.person.Person;

import java.util.Optional;

public interface UserDataService {

    String isChief();

    Optional<Person> searchChiefIdenticalToCurrentUser();

    Optional<Person> searchAnyChief();
}
