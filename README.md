## OpenStreetMap Contributor Mapping Tool

![Filters Drawer](/images/drawer.jpg)
![Menu](/images/menu.png)

### About the App
The Openstreetmap Contributor app allows anyone to contribute to OSM. It enables those-who-know to
easily manage a group of newbies as the contribution process is intuitive and easy.  
The App comes in three flavours: store (for the Android Store version), poi-storage (for MapSquare
POI Databases), and template (for the osm.mapsquare.io tool for Mapping parties).  
Bring your MapParties to a whole new level!

### How it works
For years, we have been using apps that do everything. For OSM, it means: add any node/way, edit,
delete, comment, tag... The hardest thing is that an amenity is not constrained to any model.  
If it does not really raise any issue for a desktop use, it is challenging when it comes to mobile
contribution.

The approach proposed here is a little different, even though it stays compliant with the classical
approach:

 1. Go on the website and select a contribution zone
 2. Load all amenities of the zone, or manually declare the amenity model
 3. Generate the custom contributor app
 4. Give it to your team, play, and start mapping!

### Beta instances
@Community: We are grateful for any feedback you might have on either the frontend or the mobile
apps. Go crazy and let us know!

http://osm.mapsquare.io

### How to compile the application
This application has three flavours: store, template and poi_storage.  
Every flavours use Google Analytics and Crashlytics. If you don't want them, add the following property
when using gradle assemble task:  
```
-Pfoss=true
```  

The flavour you will most probably want to build yourself is the store flavour which is the one on the Google Playstore.  
To build this flavour:  
 1. Create a conf.properties file at the project's root  
 2. Generate a [Bing Maps API key](https://www.bingmapsportal.com)  
 3. Put the generated key in conf.properties:
 ```
 bingApiKey=My_Api_Key
 ```  
 4. Execute the gradle task assembleStoreRelease, you can specify the OSM api url as a parameter:  
 ```
 ./gradlew assembleStoreRelease -PosmUrl=http://api.openstreetmap.org/api/0.6
 ```  

### Feedback / Issue tracking
Please use this github project as a feedback, feature request or issue tracker.  
You can use the following tags to help us: [Bug], [Mobile], [Web], [Feature], [Feedback]

### Twitter
You can also reach us on Twitter:  
[@LoicOrtola](https://twitter.com/LoicOrtola)  (Technical)  
[@Olduv](https://twitter.com/Olduv)  (Legal)  
[@DesignMyApp](https://twitter.com/DesignMyApp)

### RoadMap
 + Performance enhancements coming soon to all flavors !
 + Stay tuned for a new workflow on points edition / modification on the store flavor in the next few
weeks.

### Changelog

See the [CHANGELOG.md](/CHANGELOG.md) file.

### More Screenshots
![Notes on Map](/images/note.png)|
![Ways edition](/images/ways.png)
![Changeset Commit](/images/changeset.jpg)|
![POI Creation](/images/note_discussion.jpg)

### Contributors
This app is actively developed by:

 + [fredszaq](https://github.com/fredszaq)
 + [sdesprez](https://github.com/sdesprez)
 + [nicolasfavier](https://github.com/nicolasfavier)
 + [tonymanou](https://github.com/tonymanou)
 + [loicortola](https://github.com/loicortola)

We welcome any contributors with issues / pull requests.
