package za.ac.richfield.smartpantry;
import android.app.*;import android.os.Bundle;import android.text.InputType;import android.widget.*;
import java.text.*;import java.util.*;

public class EditIngredientActivity extends Activity {
 @Override public void onCreate(Bundle b){super.onCreate(b);PantryDb db=new PantryDb(this);long id=getIntent().getLongExtra("id",-1);PantryDb.Item existing=id<0?null:db.item(id);LinearLayout root=Ui.page(this,id<0?"Add Ingredient":"Edit Ingredient");
  EditText name=Ui.field(this,root,"Ingredient name");EditText qty=Ui.field(this,root,"Quantity (positive number)");qty.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);Spinner unit=new Spinner(this);String[] units={"piece","g","kg","ml","l"};unit.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,units));root.addView(unit);EditText expiry=Ui.field(this,root,"Expiry date YYYY-MM-DD (optional)");
  if(existing!=null){name.setText(existing.name);qty.setText(String.valueOf(existing.qty));unit.setSelection(Arrays.asList(units).indexOf(existing.unit));expiry.setText(existing.expiry);}
  Ui.button(this,root,"Save",v->{String n=name.getText().toString().trim(),q=qty.getText().toString().trim(),e=expiry.getText().toString().trim();if(n.isEmpty()){name.setError("Enter a name");return;}double value;try{value=Double.parseDouble(q);}catch(NumberFormatException ex){qty.setError("Enter a valid quantity");return;}if(!Double.isFinite(value)||value<=0){qty.setError("Quantity must be greater than zero");return;}
   if(!e.isEmpty()){try{SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd",Locale.ROOT);f.setLenient(false);if(!e.equals(f.format(f.parse(e))))throw new ParseException(e,0);}catch(ParseException ex){expiry.setError("Use a real date in YYYY-MM-DD format");return;}}
   db.save(id,n,value,(String)unit.getSelectedItem(),e);finish();});
  if(existing!=null)Ui.button(this,root,"Delete",v->new AlertDialog.Builder(this).setMessage("Delete this ingredient?").setNegativeButton("Cancel",null).setPositiveButton("Delete",(d,w)->{db.delete(id);finish();}).show());
 }
}
