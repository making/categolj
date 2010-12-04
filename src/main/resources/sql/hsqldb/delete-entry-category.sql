DELETE FROM 
     EntryCategory 
WHERE 
     entry_id = /*entry_id*/1
     /*IF category_id != null*/
     AND
     category_id = /*category_id*/1
     /*END*/
     /*IF category_id == null && name != null*/
     AND
     category_id = (SELECT id FROM Category WHERE name = /*name*/'Hoge' LIMIT 1)
     /*END*/
     