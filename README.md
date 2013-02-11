Grails coffeescript-compiler Plugin
===================================

## Introduction

A simple Grails plugin that compiles [CoffeeScript](http://coffeescript.org/) source files into JavaScript files. It has no resources plugin integration, leaving it to you to integrate the generated JavaScript with the resources plugin in any way you choose. It also maintains the directory structure from your CoffeeScript sources when generating the JavaScript, allowing you to take advantage of deferred class loading with frameworks like [ExtJS](http://www.sencha.com/products/extjs). The plugin uses the WRO4J library.

## Usage
Usage of the plugin is very straightforward:

Add the plugin to the `plugins` block of your `BuildConfig.groovy`:

```groovy
compile ":coffeescript-compiler:0.6"
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

At application startup, the plugin will purge all `jsOutputPath` directories and then compile fresh JavaScript files for all CoffeeScript files found under the `coffeeSourcePath` directories. It also monitors any `*.coffee` files found under `src/` and `web-app/`. If a `*.coffee` file is changed, the plugin locates the appropriate `jsOutputPath` and recompiles the JavaScript file. Files within hidden directories should be ignored by the compiler.

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

```groovy
"coffeescript-compiler" {

	// Minify in both DEVELOPMENT and PRODUCTION environments...
	pluginConfig {
		minifyInEnvironment = [ "DEVELOPMENT", "PRODUCTION" ]
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

**CAUTION** By default the plugin will purge the js folder on startup.  If you wish for your JavaScript and CoffeeScript to live happily together in your application or in the same folder simply add the `purgeJS=false` to the config.

```groovy
"coffeescript-compiler" {
	pluginConfig {
	    purgeJS = false
	}
}
```

By default the plugin on startup will process any `*.coffee` files whose companion `*.js` file has a newer timestamp.  If you wish to disable this feature to skip recompile on startup.

```groovy
"coffeescript-compiler" {
	pluginConfig {
	    overrideJS = false
	}
}
```

The CoffeeScript compilation is excluded from the "test" and "functional_test" scopes.

Which compiler is used?
-------------------------
The default compiler is Rhino, but if you have node coffee-script installed that will be used instead.  Hopefully this does not cause any variances amongst environments with and without node coffee-script, but note that is COULD.

If you wish to use node as the default you can install it as the following.

`npm install -g coffee-script`
