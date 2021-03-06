package frontlinesms2.controllers

import frontlinesms2.*

import spock.lang.*
import grails.test.mixin.*

@TestFor(ContactController)
@Mock([Contact])
class ContactControllerSpec extends Specification {
	@Unroll
	def "check for duplicates should return true or false depending on matches"() {
		given:
			new Contact(name: "Alice", mobile: "12345").save(flush: true)
			new Contact(name: "Bob", mobile: "54321").save(flush: true)
			println "ids: ${Contact.list()*.id}"
		when:
			params.mobile = suppliedMobile
			params.contactId = contactId
			controller.checkForDuplicates()
		then:
			controller.response.contentAsString == expectedResponse
		where:
			contactId | suppliedMobile | expectedResponse
			'1'       | '12345'        | 'true'
			''        | '56789'        | 'true'
			''        | '54321'        | 'false'
	}
}

