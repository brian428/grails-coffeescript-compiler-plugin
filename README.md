Grails coffeescript-compiler Plugin
===================================

## Introduction

A simple Grails plugin that compiles [CoffeeScript](http://coffeescript.org/) source files into JavaScript files. It has no resources plugin integration, leaving it to you to integrate the generated JavaScript with the resources plugin in any way you choose. It also maintains the directory structure from your CoffeeScript sources when generating the JavaScript, allowing you to take advantage of deferred class loading with frameworks like [ExtJS](http://www.sencha.com/products/extjs). The plugin uses the CoffeeScript 1.4 compiler through a modified version of the [jcoffeescript](https://github.com/yeungda/jcoffeescript) JAR.

## Usage
Usage of the plugin is very straightforward:

Add the plugin to the `plugins` block of your `BuildConfig.groovy`:

```groovy
compile ":coffeescript-compiler:0.1"
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

At application startup, the plugin will purge all `jsOutputPath` directories and then compile fresh JavaScript files for all CoffeeScript files found under the `coffeeSourcePath` directories. It also monitors any `*.coffee` files found under `src/` and `web-app/`. If a `*.coffee` file is changed, the plugin locates the appropriate `jsOutputPath` and recompiles the JavaScript file.

