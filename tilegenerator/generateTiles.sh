#!/bin/bash
# This file is used to generate a set of geojson tiles usable by the application
# It will also generate a descriptor file
#
# Dependencies 
#    - https://github.com/tyrasd/osmtogeojson
#    - curl
#    - bc
#    - sed
#
# Usage
#    - ./generateTiles.sh target north east south west
#    - tiles will be generated in target forder
#
# Example 
#    ./generateTiles.sh out 48.82894 2.377785 48.82715 2.37489



out=$1
north=$2
east=$3
south=$4
west=$5


inclng=0.002
inclat=0.002

mkdir $out

vf=$out/vectorialfiles.json
echo "[" > $vf 

for lng in `LANG=EN seq $south $inclng $north`
do
    echo $lng
    for lat in `LANG=EN seq $west $inclat $east`
    do
        filename="tile_"$lng"_"$lat".geojson"
        echo $filename
        ulng=`echo "$lng+$inclng" | bc`
        ulat=`echo "$lat+$inclat" | bc`
        curl http://overpass-api.de/api/interpreter?data="node($lng,$lat,$ulng,$ulat);way(bn);(._;>;);out;" | osmtogeojson > $out/$filename
        cat << EOF >> $vf
  {
    "name": "$filename",
    "north": "$ulng",
    "south": "$lng",
    "east": "$ulat",
    "west": "$lat"
  },
EOF
        
    done
done
sed -i '$ d' $vf

echo "}]" >> $vf
