DELETE FROM
       EntryCategory as ec,
       Category as c
WHERE
       ec.entry_id = /*entry-id*/1
       AND
       ec.category_id = c.id
       AND
       c.name = /*name*/'Hoge'