package za.ac.richfield.smartpantry;
import android.app.*;import android.content.*;import android.os.Bundle;import android.view.*;import android.widget.*;
import java.util.*;

public class SuggestedActivity extends Activity {
 private PantryDb db;private LinearLayout root;
 @Override public void onCreate(Bundle b){super.onCreate(b);db=new PantryDb(this);root=Ui.page(this,"Suggested Recipes");Ui.nav(this,root);}
 @Override protected void onResume(){super.onResume();while(root.getChildCount()>2)root.removeViewAt(2);List<PantryDb.Recipe> matches=new ArrayList<>();List<PantryDb.Item> pantry=db.items();for(PantryDb.Recipe r:db.recipes())if(Matcher.qualifies(r,pantry))matches.add(r);
  if(matches.isEmpty()){Ui.text(this,root,"No recipes match your pantry yet - add more ingredients.");return;}
  ListView list=new ListView(this);root.addView(list,new LinearLayout.LayoutParams(-1,0,1));list.setAdapter(new BaseAdapter(){public int getCount(){return matches.size();}public Object getItem(int i){return matches.get(i);}public long getItemId(int i){return matches.get(i).id;}public View getView(int i,View old,ViewGroup p){TextView t=old instanceof TextView?(TextView)old:new TextView(SuggestedActivity.this);t.setText(matches.get(i).name);t.setTextSize(18);t.setPadding(12,22,12,22);return t;}});
  list.setOnItemClickListener((p,v,pos,id)->{Intent i=new Intent(this,RecipeDetailActivity.class);i.putExtra("recipe_id",matches.get(pos).id);startActivity(i);});}
}
