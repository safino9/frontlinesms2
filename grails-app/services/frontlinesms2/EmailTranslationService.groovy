package frontlinesms2

import org.apache.camel.Processor
import org.apache.camel.Exchange

class EmailTranslationService implements Processor {
	static final String EMAIL_PROTOCOL_PREFIX = 'email:'
	static final char UNDERLINE_CHAR = '='

	static transactional = false

	void process(Exchange exchange) {
		Fmessage message = new Fmessage()
		def i = exchange.in
		message.src = EMAIL_PROTOCOL_PREFIX + i.getHeader('From')
		message.dst = EMAIL_PROTOCOL_PREFIX + i.getHeader('To')
		def emailBody = i.body
		def emailSubject = i.getHeader('Subject')
		message.text = emailSubject
		if(emailBody != null) {
			message.text = message.text ? "${message.text}\n${underline(emailSubject)}\n\n${emailBody}" : emailBody
		}
		assert exchange.out != null
		exchange.out.body = message
	}

	private String underline(String title) {
		def u = ''
		title.collect { u += UNDERLINE_CHAR }
		u
	}
}
