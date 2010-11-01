DELETE FROM 
       Category as c,
       EntryCategory as ec  
WHERE 
       ec.entry_id = /*entry-id*/1
AND
       ec.category_id = c.id