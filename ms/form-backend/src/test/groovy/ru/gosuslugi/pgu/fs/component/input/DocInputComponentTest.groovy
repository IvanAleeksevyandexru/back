package ru.gosuslugi.pgu.fs.component.input

import ru.atc.carcass.security.rest.model.DocsCollection
import ru.atc.carcass.security.rest.model.person.Kids
import ru.atc.carcass.security.rest.model.person.PersonDoc
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.fs.common.utils.AnswerUtil
import ru.gosuslugi.pgu.fs.component.input.model.DocInputDto
import spock.lang.Specification

class DocInputComponentTest extends Specification {

    private final static def DOC_TYPE_RF_PASSPORT = 'RF_PASSPORT'
    private final static def DOC_TYPE_RF_BIRTH_CERT = 'RF_BRTH_CERT'
    private final static def DOC_TYPE_FID_BIRTH_CERT = 'FID_BRTH_CERT'
    private final static def DOC_TYPE_MDCL_PLCY = 'MDCL_PLCY'
    private final static def DOC_TYPE_UNKNOWN = 'UNKNOWN'

    def 'it should have correct component type'() {
        given:
        def userPersonalData = Mock(UserPersonalData)
        def component = new DocInputComponent(userPersonalData)

        when:
        def result = component.getType()

        then:
        result == ComponentType.DocInput
    }

    def 'it should have empty docInput initial value when document type is omitted'() {
        given:
        def userPersonalData = Mock(UserPersonalData)
        def component = new DocInputComponent(userPersonalData)
        def fieldComponent = fieldComponentWithoutType()
        def scenarioDto = Mock(ScenarioDto)
        def serviceDescriptor = Mock(ServiceDescriptor)

        when:
        def initialValue = component.getInitialValue(fieldComponent, scenarioDto, serviceDescriptor)

        then:
        initialValue && initialValue.get() == [] as DocInputDto
    }

    def 'it should have valid initial value (RF_PASSPORT)'() {
        given:
        def userPersonalData = Mock(UserPersonalData)
        def component = new DocInputComponent(userPersonalData)
        def fieldComponent = passportRfFieldComponent()
        def scenarioDto = Mock(ScenarioDto)
        def serviceDescriptor = Mock(ServiceDescriptor)

        when:
        userPersonalData.docs >> { documents() }
        def initialValue = component.getInitialValue(fieldComponent, scenarioDto, serviceDescriptor)

        then:
        initialValue && initialValue.get() == [
                series : '1111',
                number : '111111',
                date   : '2020-01-01T00:00:00.000Z',
                emitter: 'УФМС'
        ] as DocInputDto
    }

    def 'it should have valid initial value (MDCL_PLCY)'() {
        given:
        def userPersonalData = Mock(UserPersonalData)
        def component = new DocInputComponent(userPersonalData)
        def fieldComponent = medicalPolicyFieldComponent()
        def scenarioDto = Mock(ScenarioDto)
        def serviceDescriptor = Mock(ServiceDescriptor)

        when:
        userPersonalData.docs >> { documents() }
        def initialValue = component.getInitialValue(fieldComponent, scenarioDto, serviceDescriptor)

        then:
        initialValue && initialValue.get() == [
                number        : '7852110823003091',
                expirationDate: '2018-05-01T00:00:00.000Z',
        ] as DocInputDto
    }

    def 'it should have valid initial value (UNKNOWN)'() {
        given:
        def userPersonalData = Mock(UserPersonalData)
        def component = new DocInputComponent(userPersonalData)
        def fieldComponent = unknownFieldComponent()
        def scenarioDto = Mock(ScenarioDto)
        def serviceDescriptor = Mock(ServiceDescriptor)

        when:
        userPersonalData.docs >> { documents() }
        def initialValue = component.getInitialValue(fieldComponent, scenarioDto, serviceDescriptor)

        then:
        initialValue && initialValue.get() == [] as DocInputDto
    }


    def 'it should have valid cycled initial value for child that exist (RF_BRTH_CERT)'() {
        given:
        def userPersonalData = Mock(UserPersonalData)
        def component = new DocInputComponent(userPersonalData)
        def fieldComponent = rfBirthCertificateFieldComponent()

        when:
        userPersonalData.docs >> { documents() }
        userPersonalData.kids >> { [[id: 'kid', documents: [elements: documents()] as DocsCollection] as Kids] as LinkedList<Kids> }
        def initialValue = component.getCycledInitialValue(fieldComponent, [id: 'kid'] as HashMap<String, Object>)

        then:
        initialValue && initialValue.get() == [
                series: 'V-ДД',
                number: '123456',
                date  : '2017-01-09T00:00:00.000Z',

        ] as DocInputDto
    }

    def 'it should have valid cycled initial value for child that not exist (RF_BRTH_CERT)'() {
        given:
        def userPersonalData = Mock(UserPersonalData)
        def component = new DocInputComponent(userPersonalData)
        def fieldComponent = rfBirthCertificateFieldComponent()

        when:
        userPersonalData.docs >> { documents() }
        userPersonalData.kids >> { [[] as Kids] as LinkedList<Kids> }
        def initialValue = component.getCycledInitialValue(fieldComponent, [id: 'kid'] as HashMap<String, Object>)

        then:
        initialValue && initialValue.get() == [] as DocInputDto
    }

    def 'it should not contain incorrect values after validation (FID_BRTH_CERT)'() {
        def value = '{"series":"","number":"1234567","date":"2021-12-08T00:00:00.000+03:00","emitter":"аапаап","issuedBy":"Кем выдан"}'
        given:
        def userPersonalData = Mock(UserPersonalData)
        def component = new DocInputComponent(userPersonalData)
        def fieldComponent = fidBirthCertificateNotRequiredFieldComponent()
        def scenarioDto = [
                currentValue: [
                        'di2': [visited: true, value: value] as ApplicantAnswer
                ] as HashMap<String, ApplicantAnswer>
        ] as ScenarioDto
        def incorrectAnswers = [] as HashMap<String, String>

        when:
        userPersonalData.docs >> { documents() }
        userPersonalData.kids >> { [[] as Kids] as LinkedList<Kids> }

        component.validateAfterSubmit(incorrectAnswers,
                scenarioDto.getCurrentValue().entrySet().iterator().next(), scenarioDto, fieldComponent)
        then:
        incorrectAnswers.isEmpty()

    }

    def 'it should validate correctly (positive)'() {
        given:
        def scenarioDto = [] as ScenarioDto
        def userPersonalData = Mock(UserPersonalData)
        def component = new DocInputComponent(userPersonalData)
        def fieldComponent = passportRfFieldComponent()
        def incorrectAnswers = [] as HashMap<String, String>
        def entry = AnswerUtil.createAnswerEntry('di1', '{"series":"1234","number":"123456","date":"2000-01-01T00:00:00.000+03:00","expirationDate":"2023-01-01T00:00:00.000+03:00","emitter":"УФМС"}')

        when:
        userPersonalData.docs >> { documents() }
        component.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent)

        then:
        incorrectAnswers.isEmpty()
    }

    def 'it should validate correctly (negative)'() {
        given:
        def scenarioDto = [] as ScenarioDto
        def userPersonalData = Mock(UserPersonalData)
        def component = new DocInputComponent(userPersonalData)
        def fieldComponent = passportRfFieldComponent()
        def incorrectAnswers = [] as HashMap<String, String>
        def entry = AnswerUtil.createAnswerEntry('di1', '{}')

        when:
        userPersonalData.docs >> { documents() }
        component.validateAfterSubmit(incorrectAnswers, entry, scenarioDto, fieldComponent)

        then:
        incorrectAnswers['di1'] == '{"date":"Поле не может быть пустым","number":"Поле не может быть пустым","series":"Поле не может быть пустым"}'
    }

    private static def fieldComponentWithoutType() {
        [
                id   : 'di1',
                type : ComponentType.DocInput,
                attrs: [
                        fields: [
                                series : [type: 'input', label: 'Серия', hint: 'при наличии', required: false, attrs: [required: false], value: ''],
                                number : [type: 'input', label: 'Номер', required: true, attrs: [validation: [[type: 'RegExp', value: '.+', ref: '', dataType: '', condition: '', errorMsg: 'Необходимо заполнить Номер', updateOn: 'blur']]]],
                                date   : [type: 'date', label: 'Дата выдачи', required: true, attrs: [maxDate: 'today', minDate: '-130y', accuracy: 'day'], validation: [[type: 'RegExp', value: '.+', ref: '', dataType: '', condition: '', errorMsg: 'Необходимо заполнить Дата выдачи', updateOn: 'blur']]],
                                emitter: [type: 'input', label: 'Кем выдан', required: false, attrs: [hidden: true, validation: []]]
                        ]
                ]
        ] as FieldComponent
    }

    private static def passportRfFieldComponent() {
        [
                id   : 'di1',
                type : ComponentType.DocInput,
                attrs: [
                        type  : DOC_TYPE_RF_PASSPORT,
                        fields: [
                                series : [type: 'input', label: 'Серия', required: true, attrs: [preset: [type: 'REF', value: ''], required: true, mask: ['/\\d/', '/\\d/', ' ', '/\\d/', '/\\d/'], validation: [type: 'RegExp', value: '.+', errorMsg: 'Необходимо заполнить Серию', updateOn: 'blur']]],
                                number : [type: 'input', label: 'Номер', required: true, attrs: [preset: [type: 'REF', value: ''], mask: ['/\\d/', '/\\d/', '/\\d/', '/\\d/', '/\\d/', '/\\d/'], validation: [[type: 'RegExp', value: '^[0-9]{6}$', ref: '', dataType: '', condition: '', errorMsg: 'Необходимо заполнить Номер', updateOn: 'blur']]]],
                                date   : [type: 'date', label: 'Дата выдачи', required: true, attrs: [preset_from: [type: 'REF', value: ''], maxDate: 'today', minDate: '-10d', accuracy: 'day'], validation: [[type: 'RegExp', value: '.+', ref: '', dataType: '', condition: '', errorMsg: 'Необходимо заполнить Дата выдачи', updateOn: 'blur']]],
                                emitter: [type: 'input', label: 'Кем выдан', required: false, attrs: [preset: [type: 'REF', value: ''], validation: [[type: 'RegExp', value: '^.{0,100}$', ref: '', dataType: '', condition: '', errorMsg: 'Поле может содержать не более 100 символов', updateOn: 'blur']]]]
                        ]
                ]
        ] as FieldComponent
    }

    private static def medicalPolicyFieldComponent() {
        [
                id   : 'di1',
                type : ComponentType.DocInput,
                attrs: [
                        type  : DOC_TYPE_MDCL_PLCY,
                        fields: [
                                number        : [type: 'input', label: 'Номер', required: true, attrs: [preset_from: [type: 'PROTECTED', value: 'foreignPassportNumber'], mask: ['/\\d/', '/\\d/', '/\\d/', '/\\d/', '/\\d/', '/\\d/'], validation: [[type: 'RegExp', value: '^[0-9]{16}$', ref: '', dataType: '', condition: '', errorMsg: 'Поле "Номер" должно содержать 16 цифр', updateOn: 'blur']]]],
                                expirationDate: [type: 'date', label: 'Действителен до', required: true, attrs: [maxDate: 'today', minDate: '-130y', accuracy: 'day'], validation: [[type: 'RegExp', value: '.+', ref: '', dataType: '', condition: '', errorMsg: 'Необходимо заполнить поле "Дата окончания"', updateOn: 'blur']]],
                        ]
                ]
        ] as FieldComponent
    }

    private static def rfBirthCertificateFieldComponent() {
        [
                id   : 'di1',
                type : ComponentType.DocInput,
                attrs: [
                        type  : DOC_TYPE_RF_BIRTH_CERT,
                        fields: [
                                series  : [type: 'input', label: 'Серия', required: true, attrs: [required: true, mask: ['/\\d/', '/\\d/'], validation: [type: 'RegExp', value: '^[a-zA-Z0-9]{2}$', errorMsg: 'Поле "Серия" должно содержать 2 цифры', updateOn: 'blur']]],
                                number  : [type: 'input', label: 'Номер', required: true, attrs: [mask: ['/\\d/', '/\\d/', '/\\d/', '/\\d/', '/\\d/', '/\\d/'], validation: [[type: 'RegExp', value: '^[a-zA-Z0-9]{7}$', ref: '', dataType: '', condition: '', errorMsg: 'Поле "Номер" должно содержать 7 цифр', updateOn: 'blur']]]],
                                date    : [type: 'date', label: 'Дата выдачи', required: true, attrs: [maxDate: 'today', minDate: '-130y', accuracy: 'day'], validation: [[type: 'RegExp', value: '.+', ref: '', dataType: '', condition: '', errorMsg: 'Необходимо заполнить поле "Дата выдачи"', updateOn: 'blur']]],
                                issuedBy: [type: 'input', label: 'Кем выдан', required: true, attrs: [validation: [[type: 'RegExp', value: '^[\\s\\S]{0,256}$', ref: '', dataType: '', condition: '', errorMsg: 'Измените поле «Кем выдан»', errorDesc: 'В поле помещается не больше 256 символов. Заполните его точно как в паспорте, чтобы отправить заявление', updateOn: 'blur']]]]
                        ]
                ]
        ] as FieldComponent
    }

    private static def fidBirthCertificateNotRequiredFieldComponent() {
        [
                id   : 'di2',
                type : ComponentType.DocInput,
                attrs: [
                        type  : DOC_TYPE_FID_BIRTH_CERT,
                        fields: [
                                series  : [type: 'input', label: 'Серия', required: false, attrs: [fstuc: 'uppercase', mask: ['/\\D/', '/\\D/'], validation: [[type: 'RegExp', value: '^[A-Z]+$', ref: '', dataType: '', condition: '',errorMsg: 'Нужно вводить только заглавными английскими буквами', updateOn: 'blur']]]],
                                number  : [type: 'input', label: 'Номер', required: true, attrs: [mask: ['/\\d/', '/\\d/', '/\\d/', '/\\d/', '/\\d/', '/\\d/'], validation: [[type: 'RegExp', value: '^[a-zA-Z0-9]{7}$', ref: '', dataType: '', condition: '', errorMsg: 'Поле "Номер" должно содержать 7 цифр', updateOn: 'blur']]]],
                                date    : [type: 'date', label: 'Дата выдачи', required: true, attrs: [maxDate: 'today', minDate: '-130y', accuracy: 'day'], validation: [[type: 'RegExp', value: '.+', ref: '', dataType: '', condition: '', errorMsg: 'Необходимо заполнить поле "Дата выдачи"', updateOn: 'blur']]],
                                issuedBy: [type: 'input', label: 'Кем выдан', required: true, attrs: [validation: [[type: 'RegExp', value: '^[\\s\\S]{0,256}$', ref: '', dataType: '', condition: '', errorMsg: 'Измените поле «Кем выдан»', errorDesc: 'В поле помещается не больше 256 символов. Заполните его точно как в паспорте, чтобы отправить заявление', updateOn: 'blur']]]]
                        ]
                ]
        ] as FieldComponent
    }

    private static def unknownFieldComponent() {
        [
                id   : 'di1',
                type : ComponentType.DocInput,
                attrs: [
                        type  : DOC_TYPE_UNKNOWN,
                        fields: [
                                number        : [type: 'input', label: 'Номер', required: true, attrs: [preset_from: [type: 'PROTECTED', value: 'foreignPassportNumber'], mask: ['/\\d/', '/\\d/', '/\\d/', '/\\d/', '/\\d/', '/\\d/'], validation: [[type: 'RegExp', value: '^[0-9]{16}$', ref: '', dataType: '', condition: '', errorMsg: 'Поле "Номер" должно содержать 16 цифр', updateOn: 'blur']]]],
                                expirationDate: [type: 'date', label: 'Действителен до', required: true, attrs: [maxDate: 'today', minDate: '-130y', accuracy: 'day'], validation: [[type: 'RegExp', value: '.+', ref: '', dataType: '', condition: '', errorMsg: 'Необходимо заполнить поле "Дата окончания"', updateOn: 'blur']]],
                        ]
                ]
        ] as FieldComponent
    }

    private static def documents() {
        [
                [type: 'RF_PASSPORT', vrfStu: 'VERIFIED', series: '1111', number: '111111', issueDate: '01.01.2020', issuedBy: 'УФМС'] as PersonDoc,
                [type: 'RF_PASSPORT', vrfStu: 'NOT_VERIFIED', series: '1111', number: '111111', issueDate: '01.01.2020', issuedBy: 'УФМС'] as PersonDoc,
                [type: 'RF_BRTH_CERT', vrfStu: 'VERIFIED', series: 'V-ДД', number: '123456', issueDate: '09.01.2017', issuedBy: 'Спб'] as PersonDoc,
                [type: 'MDCL_PLCY', vrfStu: 'NOT_VERIFIED', number: '7852110823003091', expiryDate: '01.05.2018'] as PersonDoc,
                [type: 'FID_BRTH_CERT', vrfStu: 'VERIFIED', series: '', number: '9876543', issueDate: '19.11.2021', issuedBy: 'Cancun, Mexico'] as PersonDoc
        ]
    }
}
