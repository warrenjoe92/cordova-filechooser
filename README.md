Cordova FileChooser Plugin

Requires Cordova >= 2.8.0

Install with Cordova CLI
	
	$ cordova plugin add https://github.com/warrenjoe92/cordova-filechooser

Install with Plugman 

	$ plugman --platform android --project /path/to/project \ 
		--plugin https://github.com/warrenjoe92/cordova-filechooser

API

	fileChooser.open(successCallback, failureCallback);

The success callback get the uri of the selected file

	fileChooser.open(function(file) {
		console.log('uri', file.uri);
		console.log('file name', file.filename);
	});
	
Screenshot

![Screenshot](filechooser.png "Screenshot")

TODO rename `open` to pick, select, or choose.
