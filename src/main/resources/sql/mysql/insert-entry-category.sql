INSERT INTO EntryCategory (entry_id, category_id) 
VALUES (
/*IF entry_id != null */
     /*entry_id*/1,
/*END*/
/*IF entry_id == null */
     (SELECT id FROM Entry ORDER BY id DESC LIMIT 1),
/*END*/
/*IF name == null*/
     /*category_id*/1
/*END*/
/*IF name != null*/
     (SELECT id FROM Category WHERE name = /*name*/'Hoge' LIMIT 1)
/*END*/
)
