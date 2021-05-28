-- migrate data from old table discographies (if exists)
INSERT INTO album(band_id, "position", type, typecount, name, year, status, maType, maTypeCount, maname)

SELECT (SELECT id FROM band WHERE name = band_name)                                         AS band_id,
       position,
       SUBSTRING(typeAndTypeCount FROM 1 FOR LENGTH(typeAndTypeCount) - 3)                  AS type,
       CAST(SUBSTRING(typeAndTypeCount FROM LENGTH(typeAndTypeCount) - 2 FOR 2) AS InT)     AS typeCount,
       name,
       CAST((CASE WHEN year IS NULL THEN mayear ELSE year END) AS InT),
       status,
       SUBSTRING(maTypeAndTypeCount FROM 1 FOR LENGTH(maTypeAndTypeCount) - 3)              AS maType,
       CAST(SUBSTRING(maTypeAndTypeCount FROM LENGTH(maTypeAndTypeCount) - 2 FOR 2) AS InT) AS maTypeCount,
       maname

FROM (SELECT band_name,
             position,
             CASE WHEN mp3_cover = 1 THEN 3 WHEN i_have = 1 THEN 2 WHEN i_miss = 1 THEN 1 ELSE 0 END                                                               AS status,
             album_name                                                                                                                                            AS original,
             SUBSTRING(album_name FROM 1 FOR POSITION(' - ' IN album_name))                                                                                        AS typeAndTypeCount,
             SUBSTRING(album_name FROM (POSITION(' - ' IN album_name) + 3) FOR (LENGTH(album_name) - (POSITION(' - ' IN album_name) + 3) - 6))                     AS name,
             SUBSTRING(album_name FROM (LENGTH(album_name) - 4) FOR 4)                                                                                             AS year,
             real_album_name                                                                                                                                       AS maoriginal,
             SUBSTRING(real_album_name FROM 1 FOR POSITION(' - ' IN real_album_name))                                                                              AS maTypeAndTypeCount,
             SUBSTRING(real_album_name FROM (POSITION(' - ' IN real_album_name) + 3) FOR (LENGTH(real_album_name) - (POSITION(' - ' IN real_album_name) + 3) - 6)) AS maname,
             SUBSTRING(real_album_name FROM (LENGTH(real_album_name) - 4) FOR 4)                                                                                   AS mayear
      FROM discographies) subQuery

ORDER BY band_name, position;


-- clean imported data
UPDATE album
SET type = 'FULLLENGTH'
WHERE type = '';
UPDATE album
SET maType = 'FULLLENGTH'
WHERE maType = '';
UPDATE album
SET maName = NULL
WHERE maName = '';

UPDATE album
SET maType = NULL
WHERE type = maType;
UPDATE album
SET maTypeCount = NULL
WHERE type IS NOT null
  AND typeCount = maTypeCount
  AND name IS NOT null;
UPDATE album
SET maName = NULL
WHERE name = maName;
