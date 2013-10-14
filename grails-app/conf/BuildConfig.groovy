grails.project.work.dir = 'target'

grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'

	repositories {
		grailsCentral()
		mavenLocal()
		mavenCentral()
	}

	dependencies {
		compile( "ro.isdc.wro4j:wro4j-extensions:1.6.2" ) {
			excludes(
					"slf4j-log4j12",
					"spring-web",
					"gmaven-runtime-1.6",
					"gmaven-runtime-1.7",
					"servlet-api",
					"ant",
					"groovy-all",
					"jsp-api",
					"spring-web",
					"gson",
					"closure-compiler",
					"dojo-shrinksafe",
					"jruby-complete",
					"sass-gems",
					"bourbon-gem-jar",
					"less4j"
			)
		}
	}

	plugins {
		build ':release:2.2.1', ':rest-client-builder:1.0.3', {
			export = false
		}
		test( ":spock:0.6",
				":code-coverage:1.2.5",
				":codenarc:0.17" ) {
			export = false
			//exclude "spock-grails-support"
		}
	}
}

coverage {
	xml = true
	exclusions = [ '**/*Tests*' ]
}

codenarc {
	processTestUnit = false
	processTestIntegration = false
	processServices = false
	processDomain = false
	propertiesFile = 'codenarc.properties'
	ruleSetFiles = 'file:grails-app/conf/codenarc.groovy'
	reports = {
		CoffeeScriptCompilerReport( 'xml' ) {                    // The report name 'MyXmlReport' is user-defined; Report type is 'xml'
			outputFile = 'target/codenarc.xml'      // Set the 'outputFile' property of the (XML) Report
			title = 'Grails CoffeeScript Compiler Plugin'             // Set the 'title' property of the (XML) Report
		}
	}
}
