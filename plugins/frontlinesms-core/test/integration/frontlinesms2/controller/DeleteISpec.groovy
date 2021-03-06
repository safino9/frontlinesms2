package frontlinesms2.controller

import spock.lang.*
import frontlinesms2.*
import grails.plugin.spock.*

class DeleteISpec extends IntegrationSpec {
	def trashService
	def pollController
	def folderController
	def messageController

	def setup() {
		pollController = new PollController()
		pollController.trashService = trashService

		folderController = new FolderController()

		messageController = new MessageController()
		messageController.trashService = trashService
	}

	def "deleted polls are not included in the pollInstanceList"() {
		given:
			def message1 = TextMessage.build(src:'Bob', text:'I like manchester', inbound:true, date: new Date()).save()
			def message2 = TextMessage.build(src:'Alice', text:'go barcelona', inbound:true, date: new Date()).save()
			def p = new Poll(name: 'This is a poll')
			p.editResponses(choiceA: 'Manchester', choiceB:'Barcelona')
			p.save(failOnError:true, flush:true)
			PollResponse.findByValue('Manchester').addToMessages(message1)
			PollResponse.findByValue('Barcelona').addToMessages(message2)
			p.save(flush:true, failOnError:true)
		when:
			messageController.beforeInterceptor.action.call()
			def model1 = messageController.getShowModel()
		then:
			model1.activityInstanceList == [p]
		when:
			pollController.params.id = p.id
			pollController.delete()
			messageController.beforeInterceptor.action.call()
			def model2 = messageController.getShowModel()
		then:
			!model2.activityInstanceList
	}
	
	def "deleted polls are not included in the polls list"() {
		given:
			def message1 = TextMessage.build(src:'Bob', text:'I like manchester', inbound:true, date: new Date()).save()
			def message2 = TextMessage.build(src:'Alice', text:'go barcelona', inbound:true, date: new Date()).save()
			def p = new Poll(name: 'This is a poll')
			p.editResponses(choiceA: 'Manchester', choiceB:'Barcelona')
			p.save(failOnError:true, flush:true)
			PollResponse.findByValue('Manchester').addToMessages(message1)
			PollResponse.findByValue('Barcelona').addToMessages(message2)
			p.save(flush:true, failOnError:true)
		when:
			messageController.beforeInterceptor.action.call()
			def model1 = messageController.getShowModel()
		then:
			model1.activityInstanceList == [p]
		when:
			pollController.params.id = p.id
			pollController.delete()
			def model2 = messageController.getShowModel()
		then:
			!model2.activityInstanceList
	}
	
	def "deleted folders are not included in the folderInstanceList"() {
		given:
			def f = new Folder(name:'test').save(failOnError:true)
			def m = TextMessage.build(src: '123456', date: new Date(), inbound: true)
			f.addToMessages(m)
			f.save(flush:true, failOnError:true)
		when:
			messageController.beforeInterceptor.action.call()
			def model1 = messageController.getShowModel()
		then:
			model1.folderInstanceList == [f]
		when:
			folderController.params.id = f.id
			folderController.delete()
			messageController.beforeInterceptor.action.call()
			def model2 = messageController.getShowModel()
		then:
			!model2.folderInstanceList
	}
	
	def "deleted folders are included in the trash list"() {
		given:
			def f = new Folder(name:'test').save(failOnError:true)
			def m = TextMessage.build(src: '12345', inbound: true, date: new Date())
			def m2 = TextMessage.build(src: '12345', inbound: true, date: new Date()-1)
			f.addToMessages(m)
			f.addToMessages(m2)
			delete(f)
			f.save(flush:true, failOnError:true)
		when:
			messageController.beforeInterceptor.action.call()
			messageController.trash()
			def model = messageController.modelAndView.model.trashInstanceList
		then:
			model*.object.id == [f.id]
	}
	
	def "polls, folders and messages appear in the trash section"() {
		given:
			def f = new Folder(name:'test').save(failOnError:true)
			def m = TextMessage.build(src: '12345', inbound: true, date: new Date())
			def m2 = TextMessage.build(src: '12345', inbound: true, date: new Date()-1)
			f.addToMessages(m)
			f.addToMessages(m2)
			delete(f)
			f.save(flush:true, failOnError:true)
			
			def message1 = TextMessage.build(src:'Bob', text:'I like manchester', inbound:true, date: new Date()).save()
			def message2 = TextMessage.build(src:'Alice', text:'go barcelona', inbound:true, date: new Date()).save()
			
			def m3 = TextMessage.build(src: '1235', text:"not in folder", isDeleted: true, date: new Date(), inbound: true).save(flush:true, failOnError:true)
			delete(m3)
			def p = new Poll(name: 'This is a poll')
			p.editResponses(choiceA: 'Manchester', choiceB:'Barcelona')
			p.save(failOnError:true, flush:true)
			PollResponse.findByValue('Manchester').addToMessages(message1)
			PollResponse.findByValue('Barcelona').addToMessages(message2)
			delete(p)
			p.save(flush:true, failOnError:true)
		when:
			messageController.beforeInterceptor.action.call()
			messageController.trash()
			def model = messageController.modelAndView.model.trashInstanceList
		then:
			model*.object.id == [p.id, m3.id, f.id]
	}
	
	private def delete(def o) {
		trashService.sendToTrash(o)
	}

}

