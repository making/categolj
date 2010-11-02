DELETE FROM
       EntryCategory
WHERE
       entry_id = /*entry-id*/1
       AND
       category_id = (SELECT id FROM Category WHERE name = /*name*/'Hoge')