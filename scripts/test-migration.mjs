// Disposable PostgreSQL migration and authorization checks. No live database access.
import { PGlite } from '../.test-tools/node_modules/@electric-sql/pglite/dist/index.js';
import fs from 'node:fs/promises';
import assert from 'node:assert/strict';
const db = new PGlite();
await db.exec('CREATE ROLE anon; CREATE ROLE authenticated;');
let initial = await fs.readFile('supabase/schema.sql', 'utf8');
initial = initial.replace(/CREATE EXTENSION[^;]*;/, '');
await db.exec(initial);
await db.exec("INSERT INTO products(id,title,description,category,status) VALUES ('00000000-0000-0000-0000-000000000099','Legacy','Retain this data','Wood','saved')");
const migration = await fs.readFile('supabase/product_expansion.sql', 'utf8');
await db.exec(migration);
await db.exec(migration); // migration must be safe to reapply
const owner = 'a'.repeat(64), other = 'b'.repeat(64);
const payload = {product_id:'00000000-0000-0000-0000-000000000001',title:'Wood bowl',description:'Hand-carved bowl.',category:'Wood',
 tags:['wood'],original_image_url:'https://example.test/photo.jpg', enhanced_image_url:'https://example.test/photo.jpg',
 transcript:'private voice transcript',suggested_price:100,final_price:150,status:'saved',
 public_profile:{display_name:'Test artisan',contact:'private phone',contact_public:false,email:'private email'},
 market_data:{},attributes:{},language:'en',image_warning:true};
let count = 0;
function check(value, expected) { assert.deepEqual(value, expected); count++; }
const save = (p, who=owner) => db.query('SELECT kalasetu_save($1::jsonb,$2) AS id',[JSON.stringify(p),who]);
await db.exec('SET ROLE anon');
await assert.rejects(db.query('SELECT * FROM products')); count++;
await assert.rejects(db.query('SELECT * FROM artisans')); count++;
check((await db.query('SELECT * FROM marketplace_products')).rows.length,0);
await save(payload); await save(payload);
check((await db.query('SELECT * FROM marketplace_products')).rows.length,1);
const publicRow = (await db.query('SELECT * FROM marketplace_products')).rows[0];
check('owner_hash' in publicRow,false); check('voice_transcript' in publicRow,false);
check(publicRow.public_profile.contact,''); check('email' in publicRow.public_profile,false);
await assert.rejects(save({...payload,title:'Stolen'},other)); count++;
check((await db.query('SELECT * FROM kalasetu_catalog($1)',[other])).rows.length,0);
check((await db.query('SELECT * FROM kalasetu_catalog($1)',[owner])).rows.length,1);
await save({...payload,status:'archived'});
check((await db.query('SELECT * FROM marketplace_products')).rows.length,0);
await save({...payload,public_profile:{display_name:'Test artisan',contact:'public phone',contact_public:true}});
check((await db.query('SELECT * FROM marketplace_products')).rows[0].public_profile.contact,'public phone');
for (const bad of [{title:''},{final_price:0},{status:'invented'},{category:'invented'},{tags:[]},{title:null}]) {
 await assert.rejects(save({...payload,...bad})); count++;
}
check((await db.query('SELECT kalasetu_delete($1,$2) AS deleted',[payload.product_id,other])).rows[0].deleted,false);
check((await db.query('SELECT kalasetu_delete($1,$2) AS deleted',[payload.product_id,owner])).rows[0].deleted,true);
check((await db.query('SELECT kalasetu_delete($1,$2) AS deleted',[payload.product_id,owner])).rows[0].deleted,true);
await db.exec('RESET ROLE');
check((await db.query("SELECT title FROM products WHERE id='00000000-0000-0000-0000-000000000099'")).rows[0].title,'Legacy');
console.log(`${count} PostgreSQL migration, ownership, privacy and retry checks passed.`);
await db.close();
