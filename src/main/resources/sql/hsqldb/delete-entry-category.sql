DELETE FROM 
     EntryCategory 
WHERE 
     entry_id = /*entry-id*/1
     /*IF category-id != null*/
     AND
     category_id = /*category-id*/1
     /*END*/
     