package za.ac.richfield.smartpantry;
import android.app.*;import android.os.Bundle;import android.widget.*;
public class RecipeDetailActivity extends Activity {
 @Override public void onCreate(Bundle b){super.onCreate(b);PantryDb.Recipe r=new PantryDb(this).recipe(getIntent().getLongExtra("recipe_id",-1));LinearLayout root=Ui.page(this,r==null?"Recipe unavailable":r.name);Ui.nav(this,root);if(r==null)return;Ui.text(this,root,"Ingredients");for(PantryDb.Need n:r.needs)Ui.text(this,root,"• "+n.qty+" "+n.unit+" "+n.name);Ui.text(this,root,"Method");Ui.text(this,root,r.steps);}
}
