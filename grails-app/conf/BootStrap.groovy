import grails.util.Environment
import frontlinesms2.*
import org.mockito.Mockito
import java.lang.reflect.Field
import serial.SerialClassFactory
import serial.mock.MockSerial
import serial.mock.SerialPortHandler
import serial.mock.CommPortIdentifier
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import frontlinesms2.enums.MessageStatus
import net.frontlinesms.test.serial.hayes.*

class BootStrap {
	def init = { servletContext ->
		if (Environment.current == Environment.DEVELOPMENT) {
			//DB Viewer
			//org.hsqldb.util.DatabaseManager.main()
			// do custom init for dev here
			['Friends', 'Listeners', 'Not Cats', 'Adults'].each() { createGroup(it) }
			def alice = createContact("Alice", "+123456789")
			def friends = Group.findByName('Friends')
			def notCats = Group.findByName('Not Cats')
			def bob = createContact("Bob", "+198765432")
			Contact.findAll().each() {
				it.addToGroups(friends)
				it.addToGroups(notCats)
			}
			createContact("Kate", "+198730948")

			[new CustomField(name: 'lake', value: 'Victoria', contact: alice),
					new CustomField(name: 'town', value: 'Kusumu', contact: bob)].each() {
				it.save(failOnError:true, flush:true)
			}

			new EmailFconnection(name:"mr testy's email", receiveProtocol:EmailReceiveProtocol.IMAPS, serverName:'imap.zoho.com',
					serverPort:993, username:'mr.testy@zoho.com', password:'mister').save(failOnError:true)

			SerialClassFactory.javaxCommPropertiesPath = "jni/windows/javax.comm.properties"
//			initialiseMockSerialDevice()
			initialiseRealSerialDevice()
			
			println "PORTS:"
			serial.CommPortIdentifier.portIdentifiers.each {
				println "> Port identifier: ${it.name}"
			}
			println "END OF PORTS LIST"
			
			new SmslibFconnection(name:"Huawei Modem", port:'/dev/cu.HUAWEIMobile-Modem', baud:9600, pin:'1234').save(failOnError:true)
			new SmslibFconnection(name:"COM4", port:'COM4', baud:9600).save(failOnError:true)
			new SmslibFconnection(name:"USB0", port:'/dev/ttyUSB0', baud:9600, pin:'1149').save(failOnError:true)
			
			new SmslibFconnection(name:"COM98 mock smslib device", port:'COM98', baud:9600).save(failOnError:true)
			new SmslibFconnection(name:"COM99 mock smslib device", port:'COM99', baud:9600).save(failOnError:true)
			
			[new Fmessage(src:'+123456789', dst:'+2541234567', text:'manchester rules!'),
					new Fmessage(src:'+198765432', dst:'+254987654', text:'go manchester'),
					new Fmessage(src:'Joe', dst:'+254112233', text:'pantene is the best'),
					new Fmessage(src:'Jill', dst:'+254987654', text:"where's the hill?", dateReceived:createDate("2011/01/21")),
					new Fmessage(src:'+254675334', dst:'+254112233', text:"where's the pale?", dateReceived:createDate("2011/01/20")),
					new Fmessage(src:'Humpty', dst:'+254112233', text:"where're the king's men?", starred:true, dateReceived:createDate("2011/01/23"))].each() {
						it.status = MessageStatus.INBOUND
						it.save(failOnError:true)
					}
			(1..11).each {
				new Fmessage(src:'+198765432', dst:'+254987654', text:"text-${it}", dateReceived: new Date() - it, status:MessageStatus.INBOUND).save(failOnError:true)
			}

			[new Fmessage(src: '+123456789', dst: '+254114433', text: "time over?", status: MessageStatus.SEND_FAILED),
							new Fmessage(src: 'Jhonny', dst: '+254114433', text: "I am in a meeting", status: MessageStatus.SENT),
							new Fmessage(src: 'Sony', dst: '+254116633', text: "Hurry up", status: MessageStatus.SENT),
							new Fmessage(src: 'Jill', dst: '+254115533', text: "sample sms", status: MessageStatus.SEND_PENDING)].each {
						it.save(failOnError: true)
					}

			[Poll.createPoll(title: 'Football Teams', choiceA: 'manchester', choiceB:'barcelona', question:'who will win?', instruction:'Reply A,B'),
					Poll.createPoll(title: 'Shampoo Brands', choiceA: 'pantene', choiceB:'oriele')].each() {
				it.save(failOnError:true, flush:true)
			}

			PollResponse.findByValue('manchester').addToMessages(Fmessage.findBySrc('+198765432'))
			PollResponse.findByValue('manchester').addToMessages(Fmessage.findBySrc('+123456789'))
			PollResponse.findByValue('pantene').addToMessages(Fmessage.findBySrc('Joe'))
			
			def barcelonaResponse = PollResponse.findByValue('barcelona');
			10.times {
				def msg = new Fmessage(src: "+9198765432${it}", dst: "+4498765432${it}",dateReceived: new Date() - it, text: "Yes", status: MessageStatus.INBOUND);
				msg.save(failOnError: true);
				barcelonaResponse.addToMessages(msg);
			}
			
			['Work', 'Projects'].each {
				new Folder(name:it).save(failOnError:true, flush:true)
			}
			
			[new Fmessage(src:'Max', dst:'+254987654', text:'I will be late'),
					new Fmessage(src:'Jane', dst:'+2541234567', text:'Meeting at 10 am'),
					new Fmessage(src:'Patrick', dst:'+254112233', text:'Project has started'),
					new Fmessage(src:'Zeuss', dst:'+234234', text:'Sewage blocked')].each() {
				it.status = MessageStatus.INBOUND
				it.dateReceived = new Date()
				it.save(failOnError:true, flush:true)
			}
			
			[Folder.findByName('Work').addToMessages(Fmessage.findBySrc('Max')),
					Folder.findByName('Work').addToMessages(Fmessage.findBySrc('Jane')),
					Folder.findByName('Projects').addToMessages(Fmessage.findBySrc('Zeuss')),
					Folder.findByName('Projects').addToMessages(Fmessage.findBySrc('Patrick'))].each() {
				it.save(failOnError:true, flush:true)
			}

			def radioShow = new RadioShow(name: "Health")
			radioShow.addToMessages(new Fmessage(text: "eat fruits", src: "src", dst: "dst"))
			radioShow.addToMessages(new Fmessage(text: "excerise", src: "src", dst: "dst"))
			radioShow.save(flush: true)
		}
	}


	def createGroup(String n) {
		new Group(name: n).save(failOnError: true)
	}

	def createContact(String n, String a) {
		def c = new Contact(name: n, primaryMobile: a)
		c.save(failOnError: true)
	}

	def destroy = {
	}
	
	def initialiseRealSerialDevice() {
		// adapted from http://techdm.com/grails/?p=255&lang=en
		def addJavaLibraryPath = { path, boolean prioritise=true ->
			def dir = new File(path)
			assert dir.exists()
			def oldPathList = System.getProperty('java.library.path', '')
			def newPathList = prioritise?
					dir.canonicalPath + File.pathSeparator + oldPathList:
					oldPathList + File.pathSeparator + dir.canonicalPath
			log.info "Setting java.library.path to $newPathList"
			System.setProperty('java.library.path', newPathList)
			ClassLoader.@sys_paths = null
		}
		
		def os = {
			def osNameString = System.properties['os.name'].toLowerCase()
			for(name in ['linux', 'windows', 'mac']) {
				if(osNameString.contains(name)) return name
			}
		}.call()

		def osArch = System.properties['os.arch']
		def architecture = osArch=='amd64'?'amd64': osArch.contains('64')? 'x86_64': 'i686'
		
		log.info "Adding jni/$os/$architecture to library paths..."
		addJavaLibraryPath "jni/$os/$architecture"
		serial.SerialClassFactory.init(serial.SerialClassFactory.PACKAGE_RXTX) // TODO hoepfully this step of specifying the package is unnecessary
	}

	def initialiseMockSerialDevice() {
		// Set up modem simulation
		MockSerial.init();
		MockSerial.setMultipleOwnershipAllowed(true);
		HayesState state_initial = HayesState.createState("ERROR: 1",
				"AT", "OK",
				"AT+CMEE=1", "OK",
				"AT+STSF=1", "OK",
				"AT+CPIN?", "+CPIN: READY",
				"AT+CGMI", "WAVECOM MODEM\rOK",
				"AT+CGMM", "900P\rOK",
				"AT+CNUM", "+CNUM :\"Phone\", \"0712345678\",129\rOK",
				"AT+CGSN", "123456789099998\rOK",
				"AT+CIMI", "254123456789012\rOK",
				//"AT+CBC"
				"AT+COPS=0", "OK",
				"AT+CLIP=1", "OK",
				"ATE0", "OK",
				"AT+CREG?", "+CREG: 1,1\rOK",
				"AT+CPMS?", "+CPMS: \"SM\",3, 10,\"SM\",3,10\rOK",
				"AT+CMGF=0", "OK",
				"+++", "", // switch 2 command mode
				"AT+CPMS?", "+CPMS:\r\"ME\",1,15,\"SM\",0,100\rOK", // get storage locations
				"AT+CPMS=\"ME\"", "OK",
				~/AT\+CMGL=\d/, '''+CMGL: 2,1,,51
07915892000000F0040B915892214365F70000701010221555232441D03CDD86B3CB2072B9FD06BDCDA069730AA297F17450BB3C9F87CF69F7D905
+CMGL: 3,1,,62
07915892000000F0040B915892214365F700007040213252242331493A283D0795C3F33C88FE06C9CB6132885EC6D341EDF27C1E3E97E7207B3A0C0A5241E377BB1D7693E72E

OK''')
		HayesState state_waitingForPdu = HayesState.createState(new HayesResponse("ERROR: 2", state_initial),
				~/.+/, new HayesResponse('+CMGS: 0\rOK', state_initial))
		state_initial.setResponse(~/AT\+CMGS=\d+/, "OK", state_waitingForPdu)
		
		SerialPortHandler portHandler = new StatefulHayesPortHandler(state_initial);
		CommPortIdentifier cpi = new CommPortIdentifier("COM99", portHandler);
		MockSerial.setIdentifier("COM98", cpi);
		MockSerial.setIdentifier("COM99", cpi);
		Mockito.when(MockSerial.getMock().values()).thenReturn(Arrays.asList([cpi]));
	}

	Date createDate(String dateAsString) {
		DateFormat format = createDateFormat();
		return format.parse(dateAsString)
	}

	DateFormat createDateFormat() {
		return new SimpleDateFormat("yyyy/MM/dd")
	}
}
