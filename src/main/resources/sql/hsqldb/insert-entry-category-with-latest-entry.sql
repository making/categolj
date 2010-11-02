INSERT INTO EntryCategory (entry_id, category_id) 
VALUES (
     /* Using sequence is better to guarantee consistency ? */
     (SELECT id FROM Entry ORDER BY id DESC LIMIT 1),
/*IF name == null*/
     /*category-id*/1
/*END*/
/*IF name != null*/
     (SELECT id FROM Category WHERE name = /*name*/'Hoge')
/*END*/
)
