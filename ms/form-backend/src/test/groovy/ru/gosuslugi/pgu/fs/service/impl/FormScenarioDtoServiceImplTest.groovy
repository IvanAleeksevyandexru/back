package ru.gosuslugi.pgu.fs.service.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import ru.gosuslugi.pgu.fs.common.exception.NoScreensFoundException
import ru.gosuslugi.pgu.dto.ApplicantAnswer
import ru.gosuslugi.pgu.dto.ApplicantRole
import ru.gosuslugi.pgu.dto.ScenarioDto
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.components.descriptor.types.Stage
import ru.gosuslugi.pgu.components.descriptor.types.StageStatus
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType
import ru.gosuslugi.pgu.dto.descriptor.types.ScreenType
import ru.gosuslugi.pgu.fs.FormServiceApp
import spock.lang.Specification

@SpringBootTest(classes = FormServiceApp.class)
class FormScenarioDtoServiceImplTest extends Specification {

    @Autowired
    FormScenarioDtoServiceImpl scenarioDtoService

    def "find init screen ID without conditions"() {
        ServiceDescriptor descriptor = ServiceDescriptor.builder().initScreens([
                Coapplicant: [
                        Coapplicants: "s55"
                ],
                Applicant  : [
                        Applicant         : "s1",
                        Coapplicants      : "lockscreen56",
                        Approval_InProcess: "saip",
                        Approval          : "sa"
                ]
        ]).build()

        expect:
        expectedScreenId == scenarioDtoService.findInitScreenId(descriptor, stage, role, new ScenarioDto())

        where:
        stage << [
                Stage.Coapplicants.name(),
                Stage.Applicant.name(),
                Stage.Coapplicants.name(),
                Stage.Approval.name() + '_' + StageStatus.InProcess.name(),
                Stage.Approval.name() + '_' + StageStatus.Filled.name(),
                Stage.Approval.name(),
        ]
        role << [
                ApplicantRole.Coapplicant,
                ApplicantRole.Applicant,
                ApplicantRole.Applicant,
                ApplicantRole.Applicant,
                ApplicantRole.Applicant,
                ApplicantRole.Applicant
        ]
        expectedScreenId << [
                "s55",
                "s1",
                "lockscreen56",
                "saip",
                "sa",
                "sa"
        ]
    }

    def "find init screen ID with conditions"() {
        ServiceDescriptor descriptor = ServiceDescriptor.builder().initScreens([
                Applicant: [
                        Applicant: [
                                [
                                        conditions : [
                                                [
                                                        field    : "q3.value",
                                                        fieldType: "String",
                                                        predicate: "equals",
                                                        args     : [
                                                                [
                                                                        type : "UserConst",
                                                                        value: "Да"
                                                                ]
                                                        ]
                                                ]
                                        ],
                                        nextDisplay: "s25"
                                ],
                                [
                                        conditions : [
                                                [
                                                        field    : "q3.value",
                                                        fieldType: "String",
                                                        predicate: "equals",
                                                        args     : [
                                                                [
                                                                        type : "UserConst",
                                                                        value: "Нет"
                                                                ]
                                                        ]
                                                ]
                                        ],
                                        nextDisplay: "s43"
                                ]
                        ]
                ]
        ]).screens([new ScreenDescriptor(id: "s25"), new ScreenDescriptor(id: "s43")]).build()

        ScenarioDto scenarioDto = new ScenarioDto(applicantAnswers: [
                q3: new ApplicantAnswer(value: answer, visited: true)
        ])

        expect:
        expectedScreenId == scenarioDtoService.findInitScreenId(descriptor, stage, role, scenarioDto)

        where:
        stage << [
                Stage.Applicant.name(),
                Stage.Applicant.name()
        ]
        role << [
                ApplicantRole.Applicant,
                ApplicantRole.Applicant
        ]
        answer << [
               "Да",
               "Нет"
        ]
        expectedScreenId << [
               "s25",
               "s43"
        ]
    }

    def "test merge pdf documents"() {
        def cp1 = new FieldComponent(id: "fucMerge1", type: ComponentType.FileUploadComponent)
        def cp2 = new FieldComponent(id: "fucMerge2", type: ComponentType.FileUploadComponent)
        def cp3 = new FieldComponent(id: "fucSingle", type: ComponentType.FileUploadComponent)
        def cp4 = new FieldComponent(id: "fucNoPdf", type: ComponentType.FileUploadComponent)
        def cp5 = new FieldComponent(id: "fucEmpty", type: ComponentType.FileUploadComponent)
        def cp6 = new FieldComponent(id: "fucNoValue", type: ComponentType.FileUploadComponent)
        def cp7 = new FieldComponent(id: "fucEmptyUploads", type: ComponentType.FileUploadComponent)
        def cp8 = new FieldComponent(id: "fucNoUploads", type: ComponentType.FileUploadComponent)
        def cp9 = new FieldComponent(id: "infoScr", type: ComponentType.InfoScr)
        def cp10 = new FieldComponent(id: "fucNoData", type: ComponentType.FileUploadComponent)
        def cp11 = new FieldComponent(id: "fucEmptyValue", type: ComponentType.FileUploadComponent)


        ServiceDescriptor descriptor = new ServiceDescriptor(applicationFields: [
                cp1, cp2,cp3, cp4, cp5, cp6, cp7, cp8, cp9, cp10, cp11
        ])

        ScenarioDto scenarioDto = new ScenarioDto(applicantAnswers: [
                // Разные компоненты в один файл
                fucMerge1 : new ApplicantAnswer(value: "{\"uploads\":[{\"value\":[{\"fileUid\":1,\"mnemonic\":\"comp.passport.0\", \"fileExt\": \"jpg\"},{\"fileUid\":2,\"mnemonic\":\"comp.passport.1\", \"fileExt\": \"jpg\"}],\"pdfFileName\":\"fucMerge.pdf\"}]}"),
                fucMerge2 : new ApplicantAnswer(value: "{\"uploads\": [{\"value\": [{\"fileUid\": 3, \"mnemonic\": \"comp.doc.0\", \"fileExt\": \"jpg\"}, {\"fileUid\": 4, \"mnemonic\": \"comp.doc.1\", \"fileExt\": \"jpg\"}, {\"fileUid\": 5, \"mnemonic\": \"comp.doc.2\", \"fileExt\": \"jpg\"}], \"pdfFileName\": \"fucMerge.pdf\"}, {\"value\": [{\"fileUid\": 6, \"mnemonic\": \"comp.doc.3\", \"fileExt\": \"jpg\"}], \"pdfFileName\": \"fucMerge.pdf\"}]}"),

                // Один компонент с несколькими файлами
                fucSingle : new ApplicantAnswer(value: "{\"uploads\":[{\"value\":[{\"fileUid\":7,\"mnemonic\":\"comp.fucSingle1.0\", \"fileExt\": \"jpg\"},{\"fileUid\":8,\"mnemonic\":\"comp.fucSingle1.1\", \"fileExt\": \"jpg\"}],\"pdfFileName\":\"fucSingle1.pdf\"},{\"value\":[{\"fileUid\":9,\"mnemonic\":\"comp.fucSingle2.0\", \"fileExt\": \"jpg\"}],\"pdfFileName\":\"fucSingle2.pdf\"}]}"),

                // Не FileUploadComponent
                notFuc : new ApplicantAnswer(value: "{\"uploads\":[{\"value\":[{\"mnemonic\":\"comp.notFuc.0\"},{\"mnemonic\":\"comp.notFuc.1\"}],\"pdfFileName\":\"notFuc.pdf\"}]}"),

                // Без pdfFileName
                fucNoPdf : new ApplicantAnswer(value: "{\"uploads\":[{\"value\":[{\"mnemonic\":\"comp.fucNoPdf.0\",\"fileExt\": \"jpg\"},{\"mnemonic\":\"comp.fucNoPdf.1\", \"fileExt\": \"jpg\"}]}]}"),

                // без value или с пустым value
                fucEmpty : new ApplicantAnswer(value: "{\"uploads\":[{\"value\":[],\"pdfFileName\":\"fucEmpty.pdf\"}]}"),
                fucNoValue : new ApplicantAnswer(value: "{\"uploads\":[{\"pdfFileName\":\"fucNoValue.pdf\"}]}"),

                // без uploads или пустое
                fucEmptyUploads : new ApplicantAnswer(value: "{\"uploads\":[]}"),
                fucNoUploads : new ApplicantAnswer(value: "{}"),

                // нет компонента
                fucNotExists : new ApplicantAnswer(value: "{}"),

                // другой тип
                infoScr: new ApplicantAnswer(value: "тест"),

                // нет данных
                fucNoData: new ApplicantAnswer(value: ""),

                // пустой json
                fucEmptyValue: new ApplicantAnswer(value: "{}")
        ])

        when:
        scenarioDtoService.mergePdfDocuments(scenarioDto, descriptor)

        then:
        scenarioDto.getPackageToPdf().size() == 5
        scenarioDto.getPackageToPdf().find({it -> (it.filename == "fucMerge.pdf") }).fileInfos.findAll({it.uid >= 1 && it.uid <=6 }).size() == 6

        scenarioDto.getPackageToPdf().find({it -> (it.filename == "fucSingle1.pdf") }).fileInfos.size() == 2
        scenarioDto.getPackageToPdf().find({it -> (it.filename == "fucSingle1.pdf") }).fileInfos.findAll({it.uid == 8 || it.uid == 7 }).size() == 2

        scenarioDto.getPackageToPdf().find({it -> (it.filename == "fucSingle2.pdf") }).fileInfos.size() == 1
        scenarioDto.getPackageToPdf().find({it -> (it.filename == "fucSingle2.pdf") }).fileInfos.findAll({it.uid == 9}).size() == 1

        scenarioDto.getPackageToPdf().find({it -> (it.filename == "notFuc.pdf") }) == null
        scenarioDto.getPackageToPdf().find({it -> (it.filename == "fucEmpty.pdf") }).fileInfos.size() == 0
        scenarioDto.getPackageToPdf().find({it -> (it.filename == "fucNoValue.pdf") }).fileInfos.size() == 0
    }

    def "find visited init screen"() {
        ServiceDescriptor descriptor = new ServiceDescriptor(
                initScreens: [ Applicant: [ Applicant: "s1"]],
                screens: [
                        new ScreenDescriptor(id: "s1", type: ScreenType.INFO, componentIds: []),
                        new ScreenDescriptor(id: "s22", type: ScreenType.INFO, componentIds: [])
                ]
        )
        ApplicantRole role = ApplicantRole.Applicant
        String stage = Stage.Applicant.name()
        String serviceId = null
        ScenarioDto scenarioDto = new ScenarioDto(
                finishedAndCurrentScreens: ["s1", "s2", "s2a", "s3", "s19", "s13", "s22", "s84"],
                display: [
                        id: "s22"
                ]
        )

        when:
        scenarioDtoService.prepareScenarioDto(scenarioDto, descriptor, serviceId, stage, role)

        then:
        scenarioDto.getDisplay().getId() == "s22"
    }

    def "no init screen found in the descriptor"() {
        ServiceDescriptor descriptor = new ServiceDescriptor(
                initScreens: [ Applicant: [ Applicant: "s1"]],
                screens: []
        )
        ApplicantRole role = ApplicantRole.Applicant
        String stage = Stage.Applicant.name()
        String serviceId = null
        ScenarioDto scenarioDto = new ScenarioDto(
                finishedAndCurrentScreens: ["s1", "s2", "s2a", "s3", "s19", "s13", "s22", "s84"],
                display: [
                    id: "s22"
                ]
        )

        when:
        scenarioDtoService.prepareScenarioDto(scenarioDto, descriptor, serviceId, stage, role)

        then:
        thrown(NoScreensFoundException)
    }

    def "must set correct hideBackButton flag depending on externalScreenIds"() {
        given:
        def scenarioDto = new ScenarioDto()
        def descriptor = new ServiceDescriptor(
                screens: [
                        new ScreenDescriptor(id: "s3", type: ScreenType.CUSTOM, componentIds: []),
                        new ScreenDescriptor(id: "s4", type: ScreenType.CUSTOM, componentIds: [])
                ],
                externalScreenIds: ["s3"]
        )

        when:
        def scenario = scenarioDtoService.prepareScreenById(descriptor, screenId, scenarioDto)

        then:
        scenario.display.hideBackButton == hideBackButton

        where:
        screenId | hideBackButton
        "s3"     | true
        "s4"     | false
    }

}
