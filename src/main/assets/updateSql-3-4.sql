ALTER TABLE POI ADD COLUMN "LEVEL" VARCHAR;
update poi set level=(select value from poi_tag where poi_id=poi.id and key='level');