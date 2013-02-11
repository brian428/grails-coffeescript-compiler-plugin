grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsCentral()
        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        mavenLocal()
        mavenCentral()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }
    dependencies {
        runtime("ro.isdc.wro4j:wro4j-extensions:1.6.2") {
            excludes(
                    "slf4j-log4j12",
                    "spring-web",
                    "gmaven-runtime-1.6",
                    "gmaven-runtime-1.7",
                    "servlet-api",
                    "ant",
                    "groovy-all",
                    "jsp-api",
                    "commons-pool",
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
        build(":tomcat:$grailsVersion",
              ":release:2.0.3",
              ":rest-client-builder:1.0.2") {
            export = false
        }

        test(":spock:0.6",
             ":code-coverage:1.2.5",
             ":codenarc:0.17") {
            export = false
        }
    }
}

coverage {
    xml = true
    exclusions = ['**/*Tests*']
}

codenarc {
    processTestUnit = false
    processTestIntegration = false
    processServices = false
    processDomain = false
    propertiesFile = 'codenarc.properties'
    ruleSetFiles = 'file:grails-app/conf/codenarc.groovy'
    reports = {
        CoffeeScriptCompilerReport('xml') {                    // The report name 'MyXmlReport' is user-defined; Report type is 'xml'
            outputFile = 'target/codenarc.xml'      // Set the 'outputFile' property of the (XML) Report
            title = 'Grails CoffeeScript Compiler Plugin'             // Set the 'title' property of the (XML) Report
        }
    }
}

