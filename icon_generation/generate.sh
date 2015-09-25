#!/bin/bash

# Script using Rezenerator to generate the images

REZENERATOR_JAR=~/bin/rezenerator-standalone-1.0-RC2-jar-with-dependencies.jar

java -Drezenerator.definition.dirs=src/android/definition -jar $REZENERATOR_JAR src/android/drawable target/res 

