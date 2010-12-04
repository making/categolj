SELECT 
     e.id, e.title, e.content, e.created_at, e.updated_at 
FROM 
     Entry as e
/*IF name != null && index != null*/     
     ,Category as c,
     EntryCategory as ec
WHERE
     c.name = /*name*/'Hoge'
     AND
     c.index = /*index*/1
     AND
     c.id = ec.category_id
     AND
     ec.entry_id = e.id
/*END*/
ORDER BY 
     e.updated_at
DESC 
LIMIT           
     /*limit*/5
OFFSET 
     /*offset*/0
       