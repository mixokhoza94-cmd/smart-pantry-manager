"""Run the production Java matcher against every seeded recipe using JDK 17.

Only PantryDb's data-holder classes are extracted for compilation without Android.
This tests matching, not SQLite or the Activity UI (covered by ci_smoke.py).
"""
import pathlib
import re
import subprocess
import tempfile

ROOT = pathlib.Path(__file__).resolve().parent
JAVA = ROOT / 'app/src/main/java/za/ac/richfield/smartpantry'
db = (JAVA / 'PantryDb.java').read_text()
models = db[db.index(' public static class Item'):db.index(' public PantryDb(Context')]
seeds = re.findall(r'addRecipe\(db,"([^"]+)","([^"]+)","([^"]+)"\);', db)
assert len(seeds) == 18, f'Expected 18 seeded recipes, got {len(seeds)}'
seed_lines = []
for name, steps, encoded in seeds:
    seed_lines.append(f'checkRecipe("{name}", "{encoded}");')
harness = r'''
package za.ac.richfield.smartpantry;
import java.util.*;
public class MatchingChecks {
 static int checks;
 static void check(boolean condition, String message) {
  checks++; if (!condition) throw new AssertionError(message);
 }
 static PantryDb.Item item(String n, double q, String u) {
  return new PantryDb.Item(1,n,q,u,"");
 }
 static PantryDb.Recipe recipe(String n, double q, String u) {
  PantryDb.Recipe r=new PantryDb.Recipe(1,"test","");
  r.needs.add(new PantryDb.Need(n,q,u));return r;
 }
 static void checkRecipe(String title, String encoded) {
  PantryDb.Recipe r=new PantryDb.Recipe(1,title,"");
  List<PantryDb.Item> full=new ArrayList<>();
  for(String part:encoded.split(";")) {
   String[] p=part.split("\\|");double q=Double.parseDouble(p[1]);
   r.needs.add(new PantryDb.Need(p[0],q,p[2]));full.add(item(p[0],q,p[2]));
  }
  check(Matcher.qualifies(r,full),title+": exact quantities");
  check(!Matcher.qualifies(r,new ArrayList<>()),title+": empty pantry");
  for(int i=0;i<full.size();i++) {
   List<PantryDb.Item> missing=new ArrayList<>(full);missing.remove(i);
   check(!Matcher.qualifies(r,missing),title+": missing ingredient "+i);
   List<PantryDb.Item> shortfall=new ArrayList<>(full);
   PantryDb.Item original=full.get(i);
   shortfall.set(i,item(original.name,original.qty/2,original.unit));
   check(!Matcher.qualifies(r,shortfall),title+": insufficient ingredient "+i);
  }
 }
 public static void main(String[] args) {
 SEED_CALLS
  check(Matcher.qualifies(recipe("tomato",1,"piece"),Arrays.asList(item("  TOMATOES  ",1,"piece"))),"case spaces plural");
  check(Matcher.qualifies(recipe("rice",75,"g"),Arrays.asList(item("rice",0.05,"kg"),item("rice",25,"g"))),"sum mixed mass units");
  check(!Matcher.qualifies(recipe("rice",75,"g"),Arrays.asList(item("rice",0.05,"kg"))),"mass shortfall");
  check(Matcher.qualifies(recipe("milk",200,"ml"),Arrays.asList(item("milk",0.2,"l"))),"volume conversion");
  check(!Matcher.qualifies(recipe("milk",200,"ml"),Arrays.asList(item("milk",200,"g"))),"reject mass for volume");
  check(!Matcher.qualifies(recipe("tomato",1,"piece"),Arrays.asList(item("tomato",1000,"g"))),"reject mass for count");
  check(Matcher.qualifies(recipe("bread",2,"piece"),Arrays.asList(item("bread",1,"piece"),item("bread",1,"piece"))),"sum duplicate pantry rows");
  check(!Matcher.qualifies(recipe("bread",2,"piece"),Arrays.asList(item("bread",1,"piece"),item("rice",100,"g"))),"unrelated item cannot fill shortage");
  check(Matcher.name("potatoes").equals(Matcher.name("potato")),"potato plural");
  check(Matcher.name("berries").equals(Matcher.name("berry")),"berry plural");
  System.out.println("PASS: "+checks+" matching assertions across 18 seeded recipes and unit/name edge cases.");
 }
}
'''.replace('SEED_CALLS', '\n'.join(seed_lines))
with tempfile.TemporaryDirectory() as directory:
    tmp = pathlib.Path(directory)
    (tmp / 'PantryDb.java').write_text('package za.ac.richfield.smartpantry;\nimport java.util.*;\npublic class PantryDb {\n'+models+'\n}')
    (tmp / 'MatchingChecks.java').write_text(harness)
    subprocess.run(['javac', '-d', str(tmp), str(tmp/'PantryDb.java'), str(JAVA/'Matcher.java'), str(tmp/'MatchingChecks.java')], check=True)
    subprocess.run(['java', '-cp', str(tmp), 'za.ac.richfield.smartpantry.MatchingChecks'], check=True)
