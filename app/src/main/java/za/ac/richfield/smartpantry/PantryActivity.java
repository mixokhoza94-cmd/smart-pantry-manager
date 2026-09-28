package za.ac.richfield.smartpantry;
import android.app.*;import android.content.*;import android.os.Bundle;import android.view.*;import android.widget.*;
import java.util.*;

public class PantryActivity extends Activity {
 private PantryDb db;private LinearLayout root;private ListView list;
 @Override public void onCreate(Bundle b){super.onCreate(b);db=new PantryDb(this);root=Ui.page(this,"My Pantry");Ui.nav(this,root);Ui.button(this,root,"Add ingredient",v->Ui.open(this,EditIngredientActivity.class));list=new ListView(this);root.addView(list,new LinearLayout.LayoutParams(-1,0,1));}
 @Override protected void onResume(){super.onResume();List<PantryDb.Item> data=db.items();list.setAdapter(new ItemAdapter(this,data));list.setOnItemClickListener((p,v,pos,id)->{Intent i=new Intent(this,EditIngredientActivity.class);i.putExtra("id",data.get(pos).id);startActivity(i);});}
 static class ItemAdapter extends BaseAdapter {private Activity a;private List<PantryDb.Item> data;ItemAdapter(Activity a,List<PantryDb.Item> d){this.a=a;data=d;}
  public int getCount(){return data.size();}public Object getItem(int i){return data.get(i);}public long getItemId(int i){return data.get(i).id;}
  public View getView(int i,View old,android.view.ViewGroup parent){TextView t=old instanceof TextView?(TextView)old:new TextView(a);PantryDb.Item x=data.get(i);t.setText(x.name+"  -  "+x.qty+" "+x.unit+(x.expiry==null||x.expiry.isEmpty()?"":"  |  expires "+x.expiry));t.setTextSize(17);t.setPadding(12,20,12,20);return t;}
 }
}
