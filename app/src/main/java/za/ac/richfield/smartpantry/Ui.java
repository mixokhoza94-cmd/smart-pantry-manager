package za.ac.richfield.smartpantry;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.View;
import android.widget.*;

final class Ui {
 private Ui() {}
 static LinearLayout page(Activity a, String title) {
  LinearLayout root = new LinearLayout(a); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(20,20,20,16);
  TextView heading = new TextView(a); heading.setText(title); heading.setTextSize(24); heading.setTypeface(null,Typeface.BOLD); root.addView(heading);
  a.setContentView(root); return root;
 }
 static void nav(Activity a, LinearLayout root) {
  LinearLayout row = new LinearLayout(a);
  button(a,row,"Pantry",v -> open(a,PantryActivity.class));
  button(a,row,"Recipes",v -> open(a,SuggestedActivity.class));
  button(a,row,"Settings",v -> open(a,SettingsActivity.class));
  root.addView(row);
 }
 static Button button(Activity a, LinearLayout root, String label, View.OnClickListener action) {
  Button b = new Button(a); b.setText(label); b.setOnClickListener(action); root.addView(b); return b;
 }
 static TextView text(Activity a, LinearLayout root, String value) {
  TextView t = new TextView(a); t.setText(value); t.setTextSize(16); t.setPadding(0,12,0,12); root.addView(t); return t;
 }
 static void open(Activity a, Class<?> destination) { a.startActivity(new Intent(a,destination)); }
 static EditText field(Activity a, LinearLayout root, String hint) {
  EditText e = new EditText(a); e.setHint(hint); root.addView(e); return e;
 }
}
