Grails coffeescript-compiler Plugin
===================================

[![Build Status](https://travis-ci.org/brian428/grails-coffeescript-compiler-plugin.png?branch=master)](https://travis-ci.org/brian428/grails-coffeescript-compiler-plugin)

## Introduction

A simple Grails plugin that compiles [ CoffeeScript ](http://coffeescript.org/) source files into JavaScript files. It has no resources plugin integration, leaving it to you to integrate the generated JavaScript with the resources plugin in any way you choose. It also maintains the directory structure from your CoffeeScript sources when generating the JavaScript, allowing you to take advantage of deferred class loading with frameworks like [ ExtJS ](http://www.sencha.com/products/extjs). The plugin uses the WRO4J library.

## Usage
Usage of the plugin is very straightforward:

Add the plugin to the `plugins` block of your `BuildConfig.groovy`:

```groovy
compile ":coffeescript-compiler:0.9.4"
```

By default, the plugin will compile CoffeeScript source files (`*.coffee`) from `src/coffee/` into JavaScript in `web-app/js/app/`. You can override these defaults and specify one or more CoffeeScript source folders and corresponding JavaScript output folders in your `Config.groovy` file:

```groovy
"coffeescript-compiler" {

	appSource {
		coffeeSourcePath = "src/coffee/app"
		jsOutputPath = "web-app/js/app"
	}

	testSource {
		coffeeSourcePath = "src/coffee/spec"
		jsOutputPath = "web-app/js/spec"
	}
	
}
```

At application startup, the plugin can purge all *.js files `jsOutputPath` directories (see below `purgeJS` docs) and then compile fresh JavaScript files for all CoffeeScript files found under the `coffeeSourcePath` directories. It also monitors any `*.coffee` files found under `src/` and `web-app/`. If a `*.coffee` file is changed, the plugin locates the appropriate `jsOutputPath` and recompiles the JavaScript file. Files within hidden directories should be ignored by the compiler.

## Special Note for Grails 2.2.x and Up, When Resources Plugin Is Used

There's a strange issue with the newer versions of the Resources plugin. I've created issues in the Grails and Resources plugin issue trackers to try and deal with the problem. In the meantime, runtime compilation of .coffee files will log an error from the Resources plugin. This doesn't really affect the running app, but it is annoying. One option is to remove the Resoruces plugin while you develop. Or, if you want to suppress these errors, you can edit the `environment` section of your `Config.groovy` so that it looks like:

```groovy
environments {
    development {
        // Specify path for your generated .js files to force Resources plugin to bypass them...
        ResourcesGrailsPlugin.RELOADABLE_RESOURCE_EXCLUDES.push( "**/js/app/**/*.js" )
        ...
    }
    ...
}
```

## Additional Configuration Options

By default, the generated JavaScript is unminified in the `DEVELOPMENT` environment. In `PRODUCTION`, the JavaScript output is minified using the Uglify processor. To change this behavior, you can specify an additional `pluginConfig.minifyInEnvironment` list in the configuration:

```groovy
"coffeescript-compiler" {

	// Does not minify in any environment...
	pluginConfig {
		minifyInEnvironment = []
	}
	
	// CoffeeScript source paths would go here...
}
```  

To override the default of Production only, specify the environments similar to below.

```groovy
"coffeescript-compiler" {

	// Minify in both DEVELOPMENT and PRODUCTION environments...
	pluginConfig {
		minifyInEnvironment = [ Environment.DEVELOPMENT, Environment.PRODUCTION ]
		//or use string literals
		//minifyInEnvironment = [ "DEVELOPMENT", "PRODUCTION" ]
	}

	// CoffeeScript source paths would go here...
}
```

To cause the compiler to use a "--no-wrap" simply add the following to your config which will cause the resulting JavaScript to **not** include the `.call()` wrapper.

```groovy
"coffeescript-compiler" {
	pluginConfig {
	    wrapJS = false
	}
}
```

**NOTE** By default the plugin will NOT purge the js output folders on startup (this is a change in behavior in version 0.9).  If you wish to purge the output directory of *.js files on startup add the following:

```groovy
"coffeescript-compiler" {
	pluginConfig {
	    purgeJS = true
	}
}
```

By default, the plugin will recompile all `*.coffee` files in the configured source paths. If you would prefer to only process modified `*.coffee` files (where the `.coffee` file's modified timestamp is newer than the companion `.js` file), use `overrideJS=false`.

```groovy
"coffeescript-compiler" {
	pluginConfig {
		overrideJS = false
	}
}
```

The CoffeeScript compilation is excluded from the `test` and `functional_test` scopes.

## Logging

To see information log output (such as files being compiled), add the appropriate logging key to your log4j configuration in `Config.groovy`:

```groovy
log4j = {
	info 'org.grails.plugins.coffee.compiler'
}
```

## Which CoffeeScript Compiler is Used?

The default compiler uses a bundled version of Rhino, but if you have node coffee-script installed that will be used instead.  Hopefully this does not cause any variances amongst environments with and without node coffee-script, but note that it COULD.

If you wish to use node as the default, install NodeJS. CoffeeScript should be included by default, but if you don't have that module you can install it using `npm install -g coffee-script`.
