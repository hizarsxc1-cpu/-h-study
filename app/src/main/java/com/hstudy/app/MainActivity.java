package com.hstudy.app;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Typeface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.view.*;
import android.widget.*;
import android.text.InputType;
import java.util.*;

public class MainActivity extends Activity {
    interface Saver { void save(String[] values); }
    private static final int BG=Color.rgb(11,11,18), CARD=Color.rgb(25,24,38), PURPLE=Color.rgb(167,139,250), WHITE=Color.WHITE, MUTED=Color.rgb(164,160,181);
    private DB db; private LinearLayout root, content, nav; private String page="Home";
    private final String[] tabs={"Home","Tasks","Schedule","Notes","Goals","Stats","Settings"};
    private final String[] icons={"⌂","✓","▦","▤","◎","▥","⚙"};

    @Override public void onCreate(Bundle b){super.onCreate(b); getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);db=new DB(this);show("Home"); if(Build.VERSION.SDK_INT>=33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=android.content.pm.PackageManager.PERMISSION_GRANTED) requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},7);}
    private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private LinearLayout vertical(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
    private LinearLayout horizontal(){LinearLayout l=new LinearLayout(this);l.setOrientation(0);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    private void pad(View v,int a){v.setPadding(dp(a),dp(a),dp(a),dp(a));}
    private void show(String p){page=p;root=vertical();root.setBackgroundColor(BG);setContentView(root);
        LinearLayout header=vertical();header.setPadding(dp(20),dp(22),dp(20),dp(12));
        TextView brand=text("H STUDY  ✦",14,PURPLE,true);header.addView(brand);
        TextView title=text(p.equals("Home")?"Good day, student 👋":p,27,WHITE,true);LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(-1,-2);tp.topMargin=dp(8);header.addView(title,tp);
        TextView sub=text(p.equals("Home")?"Your Smart Study Companion":"Stay organized. Keep moving forward.",13,MUTED,false);header.addView(sub);
        root.addView(header);
        ScrollView scroll=new ScrollView(this);content=vertical();content.setPadding(dp(18),dp(8),dp(18),dp(18));scroll.addView(content);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        render();
        HorizontalScrollView hsv=new HorizontalScrollView(this);hsv.setHorizontalScrollBarEnabled(false);nav=horizontal();nav.setPadding(dp(8),dp(10),dp(8),dp(10));nav.setBackgroundColor(Color.rgb(17,16,27));
        for(int i=0;i<tabs.length;i++){final String tab=tabs[i];LinearLayout item=vertical();item.setGravity(Gravity.CENTER);item.setPadding(dp(10),dp(2),dp(10),dp(2));TextView ic=text(icons[i],20,page.equals(tab)?PURPLE:MUTED,true);ic.setGravity(Gravity.CENTER);item.addView(ic);TextView label=text(tab,10,page.equals(tab)?WHITE:MUTED,page.equals(tab));label.setGravity(Gravity.CENTER);item.addView(label);item.setOnClickListener(v->show(tab));nav.addView(item,new LinearLayout.LayoutParams(dp(62),-2));}
        hsv.addView(nav);root.addView(hsv);
    }
    private void section(String title,String hint){TextView t=text(title,17,WHITE,true);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(14);lp.bottomMargin=dp(8);content.addView(t,lp);if(hint!=null){content.addView(text(hint,12,MUTED,false));}}
    private void card(View v){LinearLayout box=vertical();box.setBackground(shape(CARD,18));box.setPadding(dp(15),dp(14),dp(15),dp(14));box.addView(v);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(10);content.addView(box,lp);}
    private void action(String label,Runnable r){Button b=new Button(this);b.setText(label);b.setTextColor(WHITE);b.setAllCaps(false);b.setBackground(shape(PURPLE,14));b.setOnClickListener(v->r.run());LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(50));lp.topMargin=dp(8);lp.bottomMargin=dp(8);content.addView(b,lp);}
    private void render(){switch(page){case "Home":home();break;case "Tasks":tasks();break;case "Schedule":schedule();break;case "Notes":notes();break;case "Goals":goals();break;case "Stats":stats();break;default:settings();}}
    private void home(){int total=db.count("tasks"),done=db.countWhere("tasks","done=1"),pending=total-done;
        LinearLayout hero=vertical();hero.setBackground(shape(Color.rgb(57,43,91),20));hero.setPadding(dp(18),dp(18),dp(18),dp(18));hero.addView(text("YOUR STUDY JOURNEY",11,Color.rgb(220,205,255),true));TextView big=text(done+" / "+total,32,WHITE,true);hero.addView(big);hero.addView(text("tasks completed  •  "+pending+" remaining",13,Color.rgb(226,216,255),false));card(hero);
        LinearLayout stats=horizontal();stats.addView(metric("Tasks",String.valueOf(total)),new LinearLayout.LayoutParams(0,-2,1));stats.addView(metric("Done",String.valueOf(done)),new LinearLayout.LayoutParams(0,-2,1));stats.addView(metric("Notes",String.valueOf(db.count("notes"))),new LinearLayout.LayoutParams(0,-2,1));card(stats);
        section("Quick access","A little progress every day adds up.");action("+ Add a task",()->dialogTask());action("+ Add a note",()->dialogNote());
        section("Upcoming tasks","Your next things to finish.");listItems("tasks","No tasks yet. Add your first task.",false);
        section("Today's schedule","Keep your day on track.");listItems("schedule","No schedule added yet.",false);
    }
    private View metric(String label,String value){LinearLayout l=vertical();l.setPadding(dp(4),dp(4),dp(4),dp(4));l.addView(text(value,23,PURPLE,true));l.addView(text(label,11,MUTED,false));return l;}
    private void tasks(){section("My tasks","Tap Done when you finish a task.");action("+ New task",()->dialogTask());listItems("tasks","Nothing here yet. Add a task to get started.",true);}
    private void schedule(){section("Class schedule","Add your class, day, and time.");action("+ Add schedule",()->dialogSchedule());listItems("schedule","Your schedule is empty.",true);}
    private void notes(){section("Subject notes","Save important things in one place.");action("+ Create note",()->dialogNote());listItems("notes","No notes yet. Create your first note.",true);}
    private void goals(){section("Study goals","Set a target and track your progress.");action("+ Add goal",()->dialogGoal());listItems("goals","No goals yet. Add a goal to stay motivated.",true);}
    private void stats(){int total=db.count("tasks"),done=db.countWhere("tasks","done=1");section("Your progress","Small steps, strong results.");card(metric("Completed tasks",done+" / "+total));card(metric("Completion rate",total==0?"0%":(done*100/total)+"%"));card(metric("Saved notes",String.valueOf(db.count("notes"))));card(metric("Class schedule entries",String.valueOf(db.count("schedule"))));card(metric("Study goals",String.valueOf(db.count("goals"))));}
    private void settings(){section("App preferences","Simple tools for a focused study life.");card(text("H STUDY",22,WHITE,true));card(text("Version 1.0.0\nYour Smart Study Companion\n\nYour tasks, notes, schedules, and goals are stored locally on this device.",14,MUTED,false));action("Refresh dashboard",()->show("Home"));action("About H STUDY",()->new AlertDialog.Builder(this).setTitle("H STUDY").setMessage("Made to help you study smarter and stay organized.").setPositiveButton("OK",null).show());}
    private void listItems(String table,String empty,boolean controls){Cursor c=db.get(table);if(c.getCount()==0){card(text(empty,13,MUTED,false));c.close();return;}while(c.moveToNext()){final int id=c.getInt(c.getColumnIndexOrThrow("_id"));String title=c.getString(c.getColumnIndexOrThrow("title"));String detail="";try{if(table.equals("tasks"))detail=c.getString(c.getColumnIndexOrThrow("subject"))+"  •  "+c.getString(c.getColumnIndexOrThrow("due"));else if(table.equals("schedule"))detail=c.getString(c.getColumnIndexOrThrow("day"))+"  •  "+c.getString(c.getColumnIndexOrThrow("time"));else if(table.equals("notes"))detail=c.getString(c.getColumnIndexOrThrow("body"));else if(table.equals("goals"))detail="Target: "+c.getString(c.getColumnIndexOrThrow("target"));}catch(Exception ignored){}
        LinearLayout row=vertical();row.addView(text(title,15,WHITE,true));if(detail!=null&&!detail.trim().isEmpty())row.addView(text(detail,12,MUTED,false));
        if(controls){LinearLayout buttons=horizontal();if(table.equals("tasks")){int isDone=c.getInt(c.getColumnIndexOrThrow("done"));Button doneBtn=small(isDone==1?"Completed ✓":"Mark done",()->{db.done(id,isDone==0);show(page);});buttons.addView(doneBtn);}Button edit=small("Edit",()->editItem(table,id));buttons.addView(edit);Button del=small("Delete",()->new AlertDialog.Builder(this).setMessage("Delete this item?").setNegativeButton("Cancel",null).setPositiveButton("Delete",(d,w)->{db.delete(table,id);show(page);}).show());buttons.addView(del);row.addView(buttons);}
        card(row);
    }c.close();}
    private void editItem(String table,int id){Cursor c=db.getOne(table,id);if(!c.moveToFirst()){c.close();return;}String[] labels=table.equals("tasks")?new String[]{"Task name","Subject","Due date"}:table.equals("schedule")?new String[]{"Class / subject","Day","Time"}:table.equals("notes")?new String[]{"Note title","Note content"}:new String[]{"Goal name","Target"};String[] cols=table.equals("tasks")?new String[]{"title","subject","due"}:table.equals("schedule")?new String[]{"title","day","time"}:table.equals("notes")?new String[]{"title","body"}:new String[]{"title","target"};EditText[] fields=new EditText[cols.length];LinearLayout box=vertical();for(int i=0;i<cols.length;i++){fields[i]=field(labels[i],table.equals("notes")&&i==1);fields[i].setText(c.getString(c.getColumnIndexOrThrow(cols[i])));box.addView(fields[i]);}c.close();new AlertDialog.Builder(this).setTitle("Edit item").setView(box).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{String[] values=new String[fields.length];for(int i=0;i<fields.length;i++)values[i]=fields[i].getText().toString().trim();if(!values[0].isEmpty()){db.update(table,id,values);show(page);}}).show();}
    private Button small(String label,Runnable r){Button b=new Button(this);b.setText(label);b.setTextSize(11);b.setAllCaps(false);b.setTextColor(WHITE);b.setBackground(shape(Color.rgb(65,54,91),12));b.setOnClickListener(v->r.run());return b;}
    private EditText field(String hint,boolean multiline){EditText e=new EditText(this);e.setSingleLine(!multiline);e.setTextColor(WHITE);e.setHintTextColor(MUTED);e.setHint(hint);e.setTextSize(14);e.setPadding(dp(12),dp(9),dp(12),dp(9));e.setBackground(shape(Color.rgb(39,37,54),12));if(multiline){e.setMinLines(3);e.setGravity(Gravity.TOP);}return e;}
    private void dialog(String title,EditText[] fields,String[] hints,Saver save){LinearLayout box=vertical();box.setPadding(dp(8),dp(6),dp(8),dp(2));for(int i=0;i<fields.length;i++){fields[i]=field(hints[i],i==fields.length-1&&title.equals("New note"));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(9);box.addView(fields[i],lp);}new AlertDialog.Builder(this).setTitle(title).setView(box).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{String[] values=new String[fields.length];for(int i=0;i<fields.length;i++)values[i]=fields[i].getText().toString().trim();if(values[0].isEmpty()){Toast.makeText(this,"Please enter a title",Toast.LENGTH_SHORT).show();return;}save.save(values);show(page);}).show();}
    private void dialogTask(){EditText[] f=new EditText[3];dialog("New task",f,new String[]{"Task name","Subject (optional)","Due date (e.g. 12 Nov)"},v->db.insert("tasks",v));}
    private void dialogSchedule(){EditText[] f=new EditText[3];dialog("New schedule",f,new String[]{"Class / subject","Day (e.g. Monday)","Time (e.g. 07:30 - 09:00)"},v->{long id=db.insert("schedule",v);scheduleReminder(v[0],v[1],v[2],(int)id);});}
    private void scheduleReminder(String title,String day,String time,int id){try{String[] parts=time.trim().substring(0,5).split(":");int hour=Integer.parseInt(parts[0]),minute=Integer.parseInt(parts[1]);if(hour>23||minute>59)return;String[] names={"sunday","monday","tuesday","wednesday","thursday","friday","saturday"};int target=Calendar.MONDAY;String d=day.toLowerCase(Locale.ROOT);for(int i=0;i<names.length;i++)if(d.startsWith(names[i]))target=i+1;Calendar c=Calendar.getInstance();c.set(Calendar.HOUR_OF_DAY,hour);c.set(Calendar.MINUTE,minute);c.set(Calendar.SECOND,0);c.set(Calendar.MILLISECOND,0);int delta=(target-c.get(Calendar.DAY_OF_WEEK)+7)%7;if(delta==0&&!c.after(Calendar.getInstance()))delta=7;c.add(Calendar.DAY_OF_YEAR,delta);Intent intent=new Intent(this,ReminderReceiver.class);intent.putExtra("title","Time for "+title+" ("+time+")");int flags=PendingIntent.FLAG_UPDATE_CURRENT|(Build.VERSION.SDK_INT>=23?PendingIntent.FLAG_IMMUTABLE:0);PendingIntent pi=PendingIntent.getBroadcast(this,id,intent,flags);AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);am.setInexactRepeating(AlarmManager.RTC_WAKEUP,c.getTimeInMillis(),AlarmManager.INTERVAL_DAY*7,pi);}catch(Exception ignored){Toast.makeText(this,"Reminder needs a time like 07:30",Toast.LENGTH_LONG).show();}}
    private void dialogNote(){EditText[] f=new EditText[2];dialog("New note",f,new String[]{"Note title","Write your note here..."},v->db.insert("notes",v));}
    private void dialogGoal(){EditText[] f=new EditText[2];dialog("New study goal",f,new String[]{"Goal name","Target (e.g. 5 chapters / week)"},v->db.insert("goals",v));}
    static class DB extends SQLiteOpenHelper{
        DB(Context c){super(c,"hstudy.db",null,1);}
        public void onCreate(SQLiteDatabase d){d.execSQL("CREATE TABLE tasks (_id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT,subject TEXT,due TEXT,done INTEGER DEFAULT 0)");d.execSQL("CREATE TABLE schedule (_id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT,day TEXT,time TEXT)");d.execSQL("CREATE TABLE notes (_id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT,body TEXT)");d.execSQL("CREATE TABLE goals (_id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT,target TEXT)");}
        public void onUpgrade(SQLiteDatabase d,int o,int n){}
        Cursor get(String t){return getReadableDatabase().rawQuery("SELECT * FROM "+t+" ORDER BY _id DESC",null);}
        Cursor getOne(String t,int id){return getReadableDatabase().rawQuery("SELECT * FROM "+t+" WHERE _id=?",new String[]{String.valueOf(id)});}
        void update(String t,int id,String[] v){ContentValues x=new ContentValues();x.put("title",v[0]);if(t.equals("tasks")){x.put("subject",v[1]);x.put("due",v[2]);}else if(t.equals("schedule")){x.put("day",v[1]);x.put("time",v[2]);}else if(t.equals("notes"))x.put("body",v[1]);else x.put("target",v[1]);getWritableDatabase().update(t,x,"_id=?",new String[]{String.valueOf(id)});}
        int count(String t){Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM "+t,null);c.moveToFirst();int n=c.getInt(0);c.close();return n;}
        int countWhere(String t,String w){Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM "+t+" WHERE "+w,null);c.moveToFirst();int n=c.getInt(0);c.close();return n;}
        long insert(String t,String[] v){ContentValues x=new ContentValues();x.put("title",v[0]);if(t.equals("tasks")){x.put("subject",v[1]);x.put("due",v[2]);}else if(t.equals("schedule")){x.put("day",v[1]);x.put("time",v[2]);}else if(t.equals("notes"))x.put("body",v[1]);else x.put("target",v[1]);return getWritableDatabase().insert(t,null,x);}
        void done(int id,boolean done){ContentValues x=new ContentValues();x.put("done",done?1:0);getWritableDatabase().update("tasks",x,"_id=?",new String[]{String.valueOf(id)});}
        void delete(String t,int id){getWritableDatabase().delete(t,"_id=?",new String[]{String.valueOf(id)});}
    }
}