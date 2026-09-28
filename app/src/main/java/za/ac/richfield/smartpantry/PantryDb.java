package za.ac.richfield.smartpantry;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import java.util.*;

public class PantryDb extends SQLiteOpenHelper {
 public static class Item { public long id; public String name, unit, expiry; public double qty;
  Item(long id,String n,double q,String u,String e) {this.id=id;name=n;qty=q;unit=u;expiry=e;} }
 public static class Need { public String name, unit; public double qty;
  Need(String n,double q,String u) {name=n;qty=q;unit=u;} }
 public static class Recipe { public long id; public String name, steps; public List<Need> needs=new ArrayList<>();
  Recipe(long i,String n,String s) {id=i;name=n;steps=s;} }
 public PantryDb(Context c) { super(c,"pantry.db",null,1); }
 @Override public void onCreate(SQLiteDatabase db) {
  db.execSQL("CREATE TABLE pantry(_id INTEGER PRIMARY KEY, name TEXT NOT NULL, qty REAL NOT NULL CHECK(qty>0), unit TEXT NOT NULL, expiry TEXT)");
  db.execSQL("CREATE TABLE recipe(_id INTEGER PRIMARY KEY, name TEXT NOT NULL, steps TEXT NOT NULL)");
  db.execSQL("CREATE TABLE recipe_ingredient(_id INTEGER PRIMARY KEY, recipe_id INTEGER NOT NULL, name TEXT NOT NULL, qty REAL NOT NULL, unit TEXT NOT NULL, FOREIGN KEY(recipe_id) REFERENCES recipe(_id))");
  seed(db);
 }
 @Override public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion) { throw new IllegalStateException("Migration required"); }
 private void seed(SQLiteDatabase db) {
  addRecipe(db,"Tomato Toast","Toast bread. Slice tomato and place it on the toast.","bread|2|piece;tomato|1|piece");
  addRecipe(db,"Cheese Toast","Toast bread and top with cheese.","bread|2|piece;cheese|40|g");
  addRecipe(db,"Egg Toast","Cook eggs and serve on toasted bread.","egg|2|piece;bread|2|piece");
  addRecipe(db,"Scrambled Eggs","Beat eggs with milk and gently cook.","egg|2|piece;milk|30|ml");
  addRecipe(db,"Tomato Omelette","Beat eggs, add chopped tomato, then cook.","egg|2|piece;tomato|1|piece");
  addRecipe(db,"Cheese Omelette","Beat eggs, add cheese, then cook.","egg|2|piece;cheese|30|g");
  addRecipe(db,"Banana Oats","Cook oats in milk and add sliced banana.","oats|60|g;milk|200|ml;banana|1|piece");
  addRecipe(db,"Apple Oats","Cook oats in milk and add chopped apple.","oats|60|g;milk|200|ml;apple|1|piece");
  addRecipe(db,"Yoghurt Banana Bowl","Slice banana and serve with yoghurt.","yoghurt|150|g;banana|1|piece");
  addRecipe(db,"Yoghurt Apple Bowl","Chop apple and serve with yoghurt.","yoghurt|150|g;apple|1|piece");
  addRecipe(db,"Tomato Rice","Cook rice and stir through chopped tomato.","rice|75|g;tomato|1|piece");
  addRecipe(db,"Egg Fried Rice","Cook rice, then stir fry with beaten egg.","rice|75|g;egg|1|piece");
  addRecipe(db,"Bean Rice Bowl","Cook rice and warm beans. Combine.","rice|75|g;beans|150|g");
  addRecipe(db,"Tomato Pasta","Cook pasta and simmer chopped tomato. Combine.","pasta|80|g;tomato|2|piece");
  addRecipe(db,"Cheese Pasta","Cook pasta and mix with grated cheese.","pasta|80|g;cheese|40|g");
  addRecipe(db,"Bean Toast","Warm beans and spoon over toast.","beans|150|g;bread|2|piece");
  addRecipe(db,"Potato Egg Hash","Cook diced potato until tender and add egg.","potato|2|piece;egg|1|piece");
  addRecipe(db,"Carrot Potato Soup","Simmer diced potato and carrot in measured water until soft.","potato|2|piece;carrot|2|piece;water|500|ml");
 }
 private void addRecipe(SQLiteDatabase db,String name,String steps,String encoded) {
  ContentValues r=new ContentValues(); r.put("name",name);r.put("steps",steps);long id=db.insertOrThrow("recipe",null,r);
  for(String part:encoded.split(";")) {String[] p=part.split("\\|");ContentValues n=new ContentValues();n.put("recipe_id",id);n.put("name",p[0]);n.put("qty",Double.parseDouble(p[1]));n.put("unit",p[2]);db.insertOrThrow("recipe_ingredient",null,n);}
 }
 public List<Item> items() {List<Item> out=new ArrayList<>(); try(Cursor c=getReadableDatabase().rawQuery("SELECT _id,name,qty,unit,expiry FROM pantry ORDER BY name",null)) {while(c.moveToNext())out.add(new Item(c.getLong(0),c.getString(1),c.getDouble(2),c.getString(3),c.getString(4)));}return out;}
 public Item item(long id) {try(Cursor c=getReadableDatabase().rawQuery("SELECT _id,name,qty,unit,expiry FROM pantry WHERE _id=?",new String[]{String.valueOf(id)})){if(c.moveToFirst())return new Item(c.getLong(0),c.getString(1),c.getDouble(2),c.getString(3),c.getString(4));}return null;}
 public void save(long id,String name,double qty,String unit,String expiry) {ContentValues v=new ContentValues();v.put("name",name.trim());v.put("qty",qty);v.put("unit",unit);v.put("expiry",expiry);if(id<0)getWritableDatabase().insertOrThrow("pantry",null,v);else getWritableDatabase().update("pantry",v,"_id=?",new String[]{String.valueOf(id)});}
 public void delete(long id) {getWritableDatabase().delete("pantry","_id=?",new String[]{String.valueOf(id)});}
 public Recipe recipe(long id) {Recipe r=null;try(Cursor c=getReadableDatabase().rawQuery("SELECT _id,name,steps FROM recipe WHERE _id=?",new String[]{String.valueOf(id)})){if(c.moveToFirst())r=new Recipe(c.getLong(0),c.getString(1),c.getString(2));}if(r!=null)loadNeeds(r);return r;}
 public List<Recipe> recipes() {List<Recipe> out=new ArrayList<>();try(Cursor c=getReadableDatabase().rawQuery("SELECT _id,name,steps FROM recipe ORDER BY name",null)){while(c.moveToNext())out.add(new Recipe(c.getLong(0),c.getString(1),c.getString(2)));}for(Recipe r:out)loadNeeds(r);return out;}
 private void loadNeeds(Recipe r) {try(Cursor c=getReadableDatabase().rawQuery("SELECT name,qty,unit FROM recipe_ingredient WHERE recipe_id=?",new String[]{String.valueOf(r.id)})){while(c.moveToNext())r.needs.add(new Need(c.getString(0),c.getDouble(1),c.getString(2)));}}
}
