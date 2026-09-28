package za.ac.richfield.smartpantry;
import java.util.*;

public final class Matcher {
 private Matcher() {}
 public static String name(String raw) {
  String n=raw.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+"," ");
  if(n.endsWith("ies") && n.length()>3)return n.substring(0,n.length()-3)+"y";
  if(n.endsWith("oes"))return n.substring(0,n.length()-2);
  if(n.endsWith("s") && !n.endsWith("ss") && n.length()>3)return n.substring(0,n.length()-1);
  return n;
 }
 private static String dimension(String unit) {switch(unit){case "kg":case "g":return "mass";case "l":case "ml":return "volume";default:return "piece";}}
 private static double base(double qty,String unit) {return (unit.equals("kg")||unit.equals("l"))?qty*1000:qty;}
 public static boolean qualifies(PantryDb.Recipe recipe,List<PantryDb.Item> pantry) {
  for(PantryDb.Need need:recipe.needs) {
   double available=0;
   for(PantryDb.Item item:pantry) if(name(item.name).equals(name(need.name)) && dimension(item.unit).equals(dimension(need.unit))) available+=base(item.qty,item.unit);
   if(available+0.000001<base(need.qty,need.unit))return false;
  }
  return true;
 }
}
