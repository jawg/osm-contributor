#!/bin/bash

# Script using Rezenerator to generate the images
# Place the rezenerator jar in the same directory as this script

# Get the location of this script
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $DIR
REZENERATOR_JAR=$DIR/rezenerator-standalone-1.0-RC2-jar-with-dependencies.jar

java -Drezenerator.definition.dirs=src/android/definition -jar $REZENERATOR_JAR src/android/drawable target/res 

