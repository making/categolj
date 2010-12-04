SELECT 
     COUNT(e.id)
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