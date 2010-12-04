SELECT 
       c.id, c.name, c.index
FROM
       Category AS c, EntryCategory AS ec
WHERE
       ec.entry_id = /*entry_id*/1 AND c.id = ec.category_id
ORDER BY 
      `index` ASC
       
