## Changelog
[3.0.24]

 - Fix bus lines concurrency display error
 - Fix no network error exception thrown when data not fully loaded
 - Fix bus line search not looking for 'network' result
 
 - Add firebase
 - Update takima logo

[3.0.2]

  - Empty value are not saved on OSM Server
  - All changeset are now marked as created by OSM Contributor

[3.0.0-beta.1]

 - OAuth support / Sign-in with Google
 - Vector tiles & switched to Mapbox-gl
 - OpenStreetMap Data Type parser (detect what kind of data is present: boolean, date, opening hours...)
 - Data type Widgets
 - OpenSource Preset Marketplace h2geo-presets
 - User eXperience improvements
 - Offline mode
 - POI Duplicator

[2.2.0]

 - Added an expert mode allowing the user to manage it's Poi Types and edit the pois without restraints
 - Added an augmented reality display of the map and the pois and notes
 - Update of poi types and icons

[2.1.1]

 - Fixed an issue during authentication

[2.1.0]

 - Improving autocompletion on tags
 - In the save interface, you can now see and revert the changes you made to a Poi
 - It's now possible to select which modifications to send to OpenStreetMap
 - Spanish translation
 - Portuguese (Brazil) translation

[2.0.1]

 - Download of Pois allowed at zoom level greater than 18
 - It is now impossible to add or comment a note without being connected to OpenStreetMap
 - Bugfixes

[2.0.0]

 - PoiTypes loaded from H2geo json file
 - New UI to select PoiTypes
 - Adding keywords to ease the Poitype search
 - Using possible values from H2geo file to ease the edition
 - Translation of Poitypes
 - Bugfixes

[1.4.0]

 - Removed manual management of PoiTypes
 - Removed automatic save of Pois modifications
 - Download of Pois and Notes is now manual
 - Changed markers' icons
 - Add the possibility to switch to Bing Aerial vue
 - Better drawing of vector tiles and ways edition
 - Performance enhancements
 - Disabled vector map.
 - In template flavor, include the map tiles in the app as mbtiles files

[1.3.1]

 - Display current zoom in the top-left corner of the map
 - Minor Bugfixes

[1.3.0]:

 - OSM suggestionsDto while adding a new PoiType
 - Performance enhancements
 - Refactor, code cleaning
 - Bugfixes

[1.2.0]:

 - Three flavours: store, template, poi_storage
 - Support on/off POI tag type
 - French translation
 - Javadoc
 - Refactor, preparation for OpenSourcing
 - Manual management of PoiTypes
 - Implemented second right drawer for filters
 - Updated Copyrights
 - Bugfixes
 - **OpenSource RELEASE**

[1.1.1]:

 - Fixed POI Deletion Bug
 - Minor Bugfixes

[1.1.0]:

 - Floating menu if only few POI Types are present
 - Implemented Ways edit support
 - Improvements of the vector render engine
 - Manual Sync is more detailed
 - Bugfixes

[1.0.0]:

 - Changesets can now be pushed together manually
 - Notes and comments are now supported
 - Implemented Filters for notes (Open/Closed)
 - Improvements of the vector render engine
 - Drawer layout improvements
 - New Splashscreen
 - About page
 - Bugfixes

[0.2.0]:

 - Bugfixes
 - Now supporting POI deletion
 - Implemented POI Type filters
 - Implemented level differentiators

[0.1.0]:

 - Bugfixes
 - Now supporting unlimited amount of amenities
 - New amenities icons

[0.0.0]:

 - MapView
 - Lightweight Vector Render engine for zoom level > 19
 - Display amenities as POIs on a map
 - Create new POI
 - Edit POI tags
 - Edit POI position
 - Autocompletion
 - Material Design
