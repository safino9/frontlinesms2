package frontlinesms2

import grails.converters.JSON

abstract class Step {

	def i18nUtilService
	
	static belongsTo = [activity: CustomActivity]
	static hasMany = [stepProperties: StepProperty]
	static def implementations = [JoinActionStep, LeaveActionStep, ReplyActionStep, WebconnectionActionStep, ForwardActionStep]

	static transients = ['i18nUtilService']
	static configFields = [:]

	static constraints = {
		stepProperties(nullable: true)
	}
	
	static mapping = {
		stepProperties cascade: "all-delete-orphan"
	}
	
	abstract def process(TextMessage message)

	String getPropertyValue(key) {
		stepProperties?.find { it.key == key }?.value
	}

	def setPropertyValue(key, value){
		def prop = stepProperties?.find { it.key == key }
		prop? (prop.value = value) : this.addToStepProperties(key:key, value:value)
	}

	// helper method to retrieve list of entities saved as StepProperties
	def getEntityList(entityType, propertyName) {
		entityType.getAll(StepProperty.findAllByStepAndKey(this, propertyName)*.value) - null
	}

	String getJsonConfig() {
		return getConfig() as JSON
	}

	def activate() {}
	def deactivate() {}
}
