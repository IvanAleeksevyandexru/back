package ru.gosuslugi.pgu.fs.component

import ru.atc.carcass.security.rest.model.DocsCollection
import ru.atc.carcass.security.rest.model.person.Kids
import ru.atc.carcass.security.rest.model.person.PersonDoc
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData

class EsiaUsers {

    def static userWithChildren = new UserPersonalData(kids: [
            new Kids(id: 'kid1', firstName: 'Петя', lastName: 'Петечкин', birthDate: '10.06.2005',
                    documents: new DocsCollection(elements: [new PersonDoc(type: 'BRTH_CERT', series: '22', number: '123456', issueDate: '10.10.2003', issuedBy: 'Загс')])),
            new Kids(id: 'kid2', firstName: 'Вася', middleName: 'Васютка', lastName: 'Васечкин', birthDate: '10.06.2005',
                    documents: new DocsCollection(elements: [new PersonDoc(type: 'RF_PASSPORT', series: '22', number: '123456', issueDate: '10.10.2013', issueId: 'Загс')])),
            new Kids(id: 'kid2', firstName: 'Вася', middleName: 'Васютка', lastName: 'Васечкин', birthDate: '10.05.2013',
                    documents: new DocsCollection(elements: [new PersonDoc(type: 'BRTH_CERT', series: '22', number: '123456', issueDate: '10.10.2013', issuedBy: 'Загс')])),
    ])
}
