package ru.gosuslugi.pgu.fs.variable

import ru.gosuslugi.pgu.common.core.json.JsonFileUtil
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil
import ru.gosuslugi.pgu.draft.model.DraftHolderDto
import ru.gosuslugi.pgu.dto.descriptor.ScreenDescriptor
import ru.gosuslugi.pgu.dto.descriptor.ScreenRule
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor
import ru.gosuslugi.pgu.fs.common.service.JsonProcessingService
import ru.gosuslugi.pgu.fs.common.service.ProtectedFieldService
import ru.gosuslugi.pgu.fs.common.service.RuleConditionService
import ru.gosuslugi.pgu.fs.common.service.condition.ApplicantAnswerPredicateFactory
import ru.gosuslugi.pgu.fs.common.service.condition.ArrayPredicateFactory
import ru.gosuslugi.pgu.fs.common.service.condition.BooleanPredicateFactory
import ru.gosuslugi.pgu.fs.common.service.condition.ConditionCheckerHelper
import ru.gosuslugi.pgu.fs.common.service.condition.DatePredicateFactory
import ru.gosuslugi.pgu.fs.common.service.condition.IntegerPredicateFactory
import ru.gosuslugi.pgu.fs.common.service.condition.StringPredicateFactory
import ru.gosuslugi.pgu.fs.common.service.impl.JsonProcessingServiceImpl
import ru.gosuslugi.pgu.fs.common.service.impl.RuleConditionServiceImpl
import ru.gosuslugi.pgu.fs.common.service.impl.ScreenFinderServiceImpl
import ru.gosuslugi.pgu.fs.common.variable.VariableRegistry
import spock.lang.Shared
import spock.lang.Specification

class ScreenFinderServiceTest extends Specification {

    @Shared
    JsonProcessingService jsonProcessingService = new JsonProcessingServiceImpl(JsonProcessingUtil.getObjectMapper())
    ProtectedFieldService protectedFieldService = Stub()
    VariableRegistry variableRegistry = Stub()

    @Shared
    ConditionCheckerHelper checkerHelper = new ConditionCheckerHelper(
            new StringPredicateFactory(),
            new IntegerPredicateFactory(),
            new BooleanPredicateFactory(),
            new DatePredicateFactory(),
            new ArrayPredicateFactory(),
            new ApplicantAnswerPredicateFactory(),
            protectedFieldService,
            variableRegistry
    )

    RuleConditionService ruleConditionService = new RuleConditionServiceImpl(checkerHelper, protectedFieldService, variableRegistry)

    ScreenFinderServiceImpl screenFinderService = new ScreenFinderServiceImpl(jsonProcessingService, ruleConditionService)

    @Shared
    ServiceDescriptor serviceDescriptorEPGUCORE47396
    @Shared
    DraftHolderDto draftHolderDtoEPGUCORE47396

    def setupSpec() {
        serviceDescriptorEPGUCORE47396 = jsonProcessingService.fromJson(
                JsonFileUtil.getJsonFromFile(this.getClass(),"-descriptor-EPGUCORE-47396.json"),
                ServiceDescriptor.class)

        draftHolderDtoEPGUCORE47396 = jsonProcessingService.fromJson(
                JsonFileUtil.getJsonFromFile(this.getClass(),"-draft-EPGUCORE-47396.json"),
                DraftHolderDto.class)
    }


    def "getValue EPGUCORE 47396"() {
//        given:
        //ruleConditionService.isRuleApplyToAnswers(_ as Map, _ as Map, _ as DocumentContext, _ as DocumentContext, _ as DocumentContext, _ as Set) >>> [true, true]
        when:
        ScreenDescriptor screenDescriptor = screenFinderService.findScreenDescriptorByRules(draftHolderDtoEPGUCORE47396.getBody(), serviceDescriptorEPGUCORE47396, new ArrayList<ScreenRule>())
        then:
        "s55_5(1)_payUIN" == screenDescriptor.getId()
    }
}
