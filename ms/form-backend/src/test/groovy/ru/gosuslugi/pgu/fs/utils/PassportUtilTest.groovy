package ru.gosuslugi.pgu.fs.utils

import spock.lang.Specification

class PassportUtilTest extends Specification {
    def "must format issueId"() {
        expect:
        PassportUtil.formatIssueId(issueId) == formattedIssueId

        where:
        issueId         | formattedIssueId
        '123422'        | '123-422'
        '123-422'       | '123-422'
        '123MISTAKE422' | '123MISTAKE422'
        null            | null
        ""              | ""
    }
}
