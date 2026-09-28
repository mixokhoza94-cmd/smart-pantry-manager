"""End-to-end checks against the installed debug app on an Android emulator."""
import pathlib, re, subprocess, time, xml.etree.ElementTree as ET
OUT=pathlib.Path('evidence'); OUT.mkdir(exist_ok=True)
PKG='za.ac.richfield.smartpantry'
def adb(*args):
    return subprocess.check_output(['adb',*args],stderr=subprocess.STDOUT).decode(errors='replace')
def nodes():
    adb('shell','uiautomator','dump','/sdcard/window.xml')
    xml=adb('shell','cat','/sdcard/window.xml')
    return list(ET.fromstring(xml).iter('node'))
def tap_node(n):
    x1,y1,x2,y2=map(int,re.findall(r'\d+',n.attrib['bounds']))
    adb('shell','input','tap',str((x1+x2)//2),str((y1+y2)//2));time.sleep(.7)
def click(label):
    for _ in range(4):
        for n in nodes():
            if n.attrib.get('text','').casefold()==label.casefold():tap_node(n);return
        time.sleep(.5)
    raise AssertionError('Control missing: '+label)
def expect(text,present=True):
    found=any(text.casefold() in n.attrib.get('text','').casefold() for n in nodes())
    assert found==present, f'Expected {text!r} present={present}'
def snap(name):
    (OUT/(name+'.png')).write_bytes(subprocess.check_output(['adb','exec-out','screencap','-p']))
    (OUT/(name+'.xml')).write_text(adb('shell','cat','/sdcard/window.xml'))
def enter(index,value):
    fields=[n for n in nodes() if n.attrib.get('class')=='android.widget.EditText']
    tap_node(fields[index]);adb('shell','input','keyevent','123')
    adb('shell','input','keyevent',*(['67']*40))
    if value:adb('shell','input','text',value)
    adb('shell','input','keyevent','4');time.sleep(.4)
def add(name,qty,unit='piece'):
    click('Pantry');click('Add ingredient');enter(0,name);enter(1,qty)
    if unit!='piece':
        spinner=next(n for n in nodes() if n.attrib.get('class')=='android.widget.Spinner')
        tap_node(spinner);click(unit)
    snap('add_'+name);click('Save');expect(name)
def recipes():click('Recipes')
try:
    adb('install','-r','app/build/outputs/apk/debug/app-debug.apk')
    adb('shell','pm','clear',PKG)
    adb('shell','am','start','-W','-n',PKG+'/.PantryActivity');time.sleep(2)
    expect('My Pantry');snap('01_pantry_empty')
    recipes();expect('No recipes match');snap('02_no_matches')
    add('bread','2');recipes();expect('Tomato Toast',False)
    add('tomatoes','1');recipes();expect('Tomato Toast');snap('03_strict_match')
    click('Tomato Toast');expect('Method');expect('tomato');snap('04_recipe_detail')
    click('Pantry');expect('bread');expect('tomatoes');snap('05_pantry_list')
    tomato=next(n for n in nodes() if n.attrib.get('text','').startswith('tomatoes  -'))
    tap_node(tomato);enter(1,'0.5');snap('06_edit_quantity');click('Save')
    recipes();expect('Tomato Toast',False);snap('07_insufficient_quantity')
    click('Pantry');tomato=next(n for n in nodes() if n.attrib.get('text','').startswith('tomatoes  -'))
    tap_node(tomato);enter(1,'1');click('Save')
    adb('shell','am','force-stop',PKG);adb('shell','am','start','-W','-n',PKG+'/.PantryActivity')
    expect('tomatoes');expect('bread');snap('08_persistence_after_restart')
    recipes();expect('Tomato Toast')
    click('Pantry');tomato=next(n for n in nodes() if n.attrib.get('text','').startswith('tomatoes  -'))
    tap_node(tomato);click('Delete');expect('Delete this ingredient?');snap('09_delete_confirmation');click('Delete')
    expect('tomatoes',False);recipes();expect('Tomato Toast',False);snap('10_after_delete')
    click('Pantry');click('Add ingredient');enter(0,'rice');enter(1,'0');click('Save');expect('Add Ingredient');snap('11_validation_zero')
    enter(1,'0.05');spinner=next(n for n in nodes() if n.attrib.get('class')=='android.widget.Spinner');tap_node(spinner);click('kg');click('Save')
    add('tomato','1');recipes();expect('Tomato Rice',False)
    click('Pantry');rice=next(n for n in nodes() if n.attrib.get('text','').startswith('rice  -'))
    tap_node(rice);enter(1,'0.08');click('Save');recipes();expect('Tomato Rice');snap('12_unit_conversion')
    click('Settings');expect('Show expiring soon reminder');snap('13_settings');click('Show expiring soon reminder');expect('Reminders are off.')
    adb('shell','am','force-stop',PKG);adb('shell','am','start','-W','-n',PKG+'/.PantryActivity');click('Settings');expect('Reminders are off.')
    (OUT/'result.txt').write_text('PASS: launch, empty state, create/read/update/delete, strict matching, plural normalisation, insufficient quantities, kg/g conversion, restart persistence, zero validation, recipe detail and settings persistence.\n')
except Exception:
    snap('failure')
    (OUT/'logcat.txt').write_text(adb('logcat','-d','-t','1500'))
    raise
