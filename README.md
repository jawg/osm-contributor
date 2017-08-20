## OpenStreetMap Contributor Mapping Tool

![Filters Drawer](/images/drawer.png)
![Menu](/images/menu.png)

### About the App
The Openstreetmap Contributor app allows anyone to contribute to OSM. It enables those-who-know to
easily manage a group of newbies as the contribution process is intuitive and easy.
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

http://osm.jawg.io

### How to compile the application

The flavour you will most probably want to build yourself is the store flavour which is the one on the Google Playstore.  
To build the project:
 1. Create a conf.properties file at the project's root  
 2. Put all your secret API Key :
 ```
 flickr_api_key=apiKey
 flickr_api_secret=apiSecret
 flickr_token=token
 flickr_token_secret=tokenSecret
 mapbox_token=pk.yourApiKey
 ```  
 3. Execute the gradle task assembleStoreRelease, you can specify the OSM api url as a parameter:
 ```
 ./gradlew assembleStoreRelease -PosmUrl=https://api.openstreetmap.org/api/0.6
 ```  

### Feedback / Issue tracking
Please use this github project as a feedback, feature request or issue tracker.  
You can use the following tags to help us: [Bug], [Mobile], [Web], [Feature], [Feedback]

### Twitter
You can also reach us on Twitter:  
[@LoicOrtola](https://twitter.com/LoicOrtola)  (Technical)  
[@Olduv](https://twitter.com/Olduv)  (Legal)  
[@jawgio](https://twitter.com/jawgio)

### RoadMap
 + Add more options for edition of ways.

### Changelog

See the [CHANGELOG.md](/CHANGELOG.md) file.

### More Screenshots!
![Poi creation](/images/poi_creation.png)|
![Ways edition](/images/ways.png)
![Ways edition satellite view](/images/ways_satellite.png)|
![Changeset Commit](/images/changeset.png)
![Changeset Commit ](/images/changeset_detail.png)|
![Notes on Map](/images/note.png)
![Note discussion](/images/note_discussion.jpg)|
![Augmented reality](/images/augmented_reality.png)

### Contributors
This app is actively developed by:
 + [panieravide](https://github.com/panieravide)
 + [fredszaq](https://github.com/fredszaq)
 + [sdesprez](https://github.com/sdesprez)
 + [nicolasfavier](https://github.com/nicolasfavier)
 + [tonymanou](https://github.com/tonymanou)
 + [loicortola](https://github.com/loicortola)
 + [tommybuonomo](https://github.com/tommybuonomo)
 + [hugoo13](https://github.com/hugoo13)

### Translations / Beta testers

This app is being translated by the community via the awesome tool Transifex.
Want to help? [Add your language here](https://www.transifex.com/jawg/osm-contributor), and let us know on Twitter.
 + English: Development team
 + French: Development team
 + Spanish: [Nacho](https://twitter.com/ignaciolep) (@ignaciolep)
 + Spanish: [Marco Antonio](https://twitter.com/51114u9) (@51114u9)
 + Portuguese (BR): [Tiago Fassoni](https://github.com/tiagofassoni)
 + Russian: [Nikolay Parukhin]()
 + Japanese: [Takeshi Furuta](https://github.com/mq-sol)

We also want to thank our many beta testers:  
 + Gilles B.
 + Donat R.
 + 

We welcome any contributors with issues / pull requests.
