package cz.somsi.app;

import android.app.*;
import android.os.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    static final int GOLD=Color.rgb(232,169,46), DARK=Color.rgb(7,13,17), PANEL=Color.rgb(20,28,33), PANEL2=Color.rgb(28,38,44), TEXT=Color.WHITE, MUTED=Color.rgb(184,194,199), GREEN=Color.rgb(93,213,116), RED=Color.rgb(235,76,55);
    static final String WEB="https://stavbysomsi.cz/", PHONE="+420736771754", PHOTO="https://stavbysomsi.cz/realizace-hlavni.jpg";
    FrameLayout root; SomsiView view; DB db; Bitmap hero; android.content.SharedPreferences pref;
    int visits,active,done,planned;

    @Override public void onCreate(Bundle b){ super.onCreate(b); db=new DB(this); pref=getSharedPreferences("somsi",0); visits=pref.getInt("v",1248); active=pref.getInt("a",86); done=pref.getInt("d",67); planned=pref.getInt("p",12); build(); loadHero(); }
    int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    void save(){pref.edit().putInt("v",visits).putInt("a",active).putInt("d",done).putInt("p",planned).apply();}

    void build(){
        root=new FrameLayout(this); view=new SomsiView(this); ScrollView s=new ScrollView(this); s.setFillViewport(true); s.addView(view,new ScrollView.LayoutParams(-1,dp(1280))); root.addView(s,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout nav=new LinearLayout(this); nav.setGravity(Gravity.CENTER); nav.setBackgroundColor(Color.rgb(8,14,18));
        String[] n={"⌂\nDomů","▣\nZakázky","＋\nNový","●\nChat","☰\nMenu"};
        for(String z:n){ TextView t=new TextView(this); t.setText(z); t.setTextColor(TEXT); t.setTextSize(11); t.setGravity(Gravity.CENTER); t.setTypeface(Typeface.DEFAULT,Typeface.BOLD); t.setPadding(0,dp(7),0,0); t.setOnClickListener(v->{String x=((TextView)v).getText().toString(); if(x.contains("Domů"))show("Domů"); else if(x.contains("Zakázky"))show("Zakázky"); else if(x.contains("Chat"))show("Chat"); else if(x.contains("Nový"))newRecord(); else menu();}); nav.addView(t,new LinearLayout.LayoutParams(0,dp(70),1)); }
        root.addView(nav,new FrameLayout.LayoutParams(-1,dp(70),Gravity.BOTTOM)); setContentView(root);
    }
    void loadHero(){ Executors.newSingleThreadExecutor().execute(()->{ try{ HttpURLConnection c=(HttpURLConnection)new URL(PHOTO+"?v=20260921").openConnection(); c.setConnectTimeout(8000);c.setReadTimeout(12000);c.connect(); InputStream in=c.getInputStream(); Bitmap b=BitmapFactory.decodeStream(in);in.close();c.disconnect(); runOnUiThread(()->{hero=b;view.invalidate();}); }catch(Exception ignored){} }); }
    void show(String x){view.sec=x;view.invalidate();}
    void newRecord(){visits++;active++;db.addRecord("Nový záznam","Vytvořen v aplikaci");save();Toast.makeText(this,"Nový záznam uložen do databáze",Toast.LENGTH_SHORT).show();view.invalidate();}
    void openWeb(){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(WEB)));}catch(Exception e){Toast.makeText(this,"Web nelze otevřít",Toast.LENGTH_SHORT).show();}}
    void call(){startActivity(new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+PHONE)));}
    void camera(){try{startActivity(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));}catch(Exception e){Toast.makeText(this,"Fotoaparát není dostupný",Toast.LENGTH_SHORT).show();}}
    void menu(){ final String[] items={"Adresář","Fotodokumentace","Stavební deník","Úkoly","Docházka","Materiál","Dokumenty","Otevřít web SOMSI","Nastavení"}; new AlertDialog.Builder(this).setTitle("SOMSI – Menu").setItems(items,(d,w)->{if(w==1)camera(); else if(w==7)openWeb(); else show(items[w]);}).show(); }
    void chatDialog(){
        final EditText e=new EditText(this); e.setHint("Napište zprávu…"); e.setMinLines(3); e.setGravity(Gravity.TOP); LinearLayout box=new LinearLayout(this);box.setPadding(dp(18),dp(4),dp(18),0);box.addView(e,new LinearLayout.LayoutParams(-1,-2));
        new AlertDialog.Builder(this).setTitle("SOMSI Chat").setMessage("Zpráva se uloží do místní databáze telefonu.").setView(box).setNegativeButton("Zrušit",null).setPositiveButton("Odeslat",(d,w)->{String msg=e.getText().toString().trim();if(!msg.isEmpty()){db.addMessage("Já",msg);Toast.makeText(this,"Zpráva uložena",Toast.LENGTH_SHORT).show();view.invalidate();}}).show();
    }

    class SomsiView extends View{
        Paint p=new Paint(3); String sec="Domů"; RectF r=new RectF(); float den;
        String[] names={"Zakázky","Fotodokumentace","Stavební deník","Úkoly","Docházka","Materiál","Dokumenty","Adresář","Chat"};
        String[] icons={"▣","▣","▤","✓","●","◆","▤","♟","●"};
        SomsiView(Context c){super(c);den=getResources().getDisplayMetrics().density;setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        float W(){return getWidth()/den;}
        void text(Canvas c,String s,float x,float y,float z,int col,boolean b){p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(col);p.setTextSize(dp(z));p.setTypeface(Typeface.create("sans",b?Typeface.BOLD:Typeface.NORMAL));c.drawText(s,dp(x),dp(y),p);}
        void box(Canvas c,float l,float t,float rr,float bb,int col,float rad){p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(col);r.set(dp(l),dp(t),dp(rr),dp(bb));c.drawRoundRect(r,dp(rad),dp(rad),p);}
        void line(Canvas c,float x1,float y1,float x2,float y2,int col,float sw){p.setShader(null);p.setColor(col);p.setStrokeWidth(dp(sw));c.drawLine(dp(x1),dp(y1),dp(x2),dp(y2),p);}
        @Override protected void onDraw(Canvas c){c.drawColor(DARK); if(sec.equals("Domů"))home(c);else page(c);}

        void home(Canvas c){float w=W();
            if(hero!=null){p.setShader(new BitmapShader(hero,Shader.TileMode.CLAMP,Shader.TileMode.CLAMP));float scale=Math.max(getWidth()/(float)hero.getWidth(),dp(300)/(float)hero.getHeight());r.set(0,0,getWidth(),dp(300));c.save();c.clipRect(r);float bw=hero.getWidth()*scale,bh=hero.getHeight()*scale;c.drawBitmap(hero,null,new RectF((getWidth()-bw)/2,0,(getWidth()+bw)/2,bh),p);c.restore();p.setShader(null);p.setColor(0xB0000000);c.drawRect(0,0,getWidth(),dp(300),p);}else{p.setColor(Color.rgb(35,44,48));c.drawRect(0,0,getWidth(),dp(300),p);}
            text(c,"☰",16,42,29,TEXT,false); text(c,"SOMSI",68,56,29,TEXT,true);text(c,"stavební práce",70,80,15,TEXT,false);text(c,"Od základu po střechu",62,114,18,GOLD,true);
            text(c,"KVALITA",w-103,53,9,MUTED,false);text(c,"ZKUŠENOSTI",w-103,73,9,MUTED,false);text(c,"SPOLEHLIVOST",w-103,93,9,MUTED,false);
            text(c,"Stavíme",w-112,181,18,TEXT,true);text(c,"Vaše plány",w-112,204,18,TEXT,true);text(c,"do reality",w-112,227,18,TEXT,true);
            stats(c);grid(c);callPanel(c);webPanel(c);
        }
        void stats(Canvas c){float gap=5,l=10,top=315,cw=(W()-20-gap*3)/4;String[] name={"Návštěvy","Zakázky","Dokončeno","Plán"};String[] val={""+visits,""+active,""+done,""+planned};for(int i=0;i<4;i++){float x=l+i*(cw+gap);box(c,x,top,x+cw,top+88,PANEL,12);text(c,val[i],x+cw/2-17,top+37,18,TEXT,true);text(c,name[i],x+cw/2-27,top+60,9,TEXT,false);if(i<2)text(c,i==0?"+12%":"+8%",x+cw/2-12,top+78,8,GREEN,true);}}
        void grid(Canvas c){float gap=6,l=10,top=417,cw=(W()-20-gap*2)/3,ch=88;for(int i=0;i<9;i++){int row=i/3,col=i%3;float x=l+col*(cw+gap),y=top+row*(ch+gap);int bg=i==0?Color.rgb(170,118,28):PANEL;box(c,x,y,x+cw,y+ch,bg,14);box(c,x+8,y+9,x+38,y+39,i==0?Color.rgb(250,196,83):Color.rgb(44,57,64),10);text(c,icons[i],x+15,y+31,17,i==0?Color.BLACK:GOLD,true);text(c,names[i],x+45,y+35,10,TEXT,true);text(c,"›",x+cw-19,y+39,19,GOLD,true);if(i==8){box(c,x+cw-27,y+6,x+cw-6,y+27,RED,11);text(c,"3",x+cw-20,y+20,8,TEXT,true);}}}
        void callPanel(Canvas c){float y=716,w=W();box(c,10,y,w-10,y+82,Color.rgb(17,25,29),14);line(c,10,y,w-10,y,GOLD,1);text(c,"☎",23,y+48,25,GOLD,true);text(c,"Potřebujete konzultaci?",58,y+32,13,TEXT,true);text(c,"Ozvěte se nám!",58,y+54,12,MUTED,false);box(c,w-108,y+17,w-25,y+65,GOLD,10);text(c,"Zavolat",w-94,y+47,12,Color.BLACK,true);}
        void webPanel(Canvas c){float y=814,w=W();box(c,10,y,w-10,y+70,PANEL2,13);text(c,"🌐",23,y+43,20,GOLD,true);text(c,"Web stavbysomsi.cz",58,y+30,13,TEXT,true);text(c,"Aktuální realizace, služby a kontakt",58,y+51,10,MUTED,false);box(c,w-102,y+14,w-25,y+57,Color.rgb(41,55,62),10);text(c,"OTEVŘÍT",w-94,y+40,9,GOLD,true);}

        void page(Canvas c){float w=W(); text(c,"‹",16,43,30,GOLD,true);text(c,sec,55,40,22,TEXT,true);text(c,"SOMSI",w-70,40,16,GOLD,true);box(c,12,65,w-12,235,PANEL,16);
            if(sec.equals("Zakázky")){text(c,"AKTIVNÍ ZAKÁZKY",28,98,11,MUTED,true);text(c,"86",28,140,30,TEXT,true);text(c,"Novostavba – Horní Studenec",112,103,13,TEXT,true);text(c,"Fasáda a zateplení",112,129,11,MUTED,false);text(c,"Dokončení: 30. 10. 2026",112,155,10,GOLD,true);text(c,"+ NOVÁ ZAKÁZKA",28,205,11,GOLD,true);}
            else if(sec.equals("Fotodokumentace")){text(c,"FOTODOKUMENTACE",28,98,11,MUTED,true);text(c,"Fotografie realizací",28,126,18,TEXT,true);if(hero!=null)c.drawBitmap(hero,null,new RectF(dp(28),dp(142),dp(w-28),dp(225)),p);text(c,"Zdroj: aktuální web SOMSI",28,253,10,MUTED,false);}
            else if(sec.equals("Stavební deník")){text(c,"STAVEBNÍ DENÍK",28,98,11,MUTED,true);text(c,"Dnes",28,129,17,TEXT,true);text(c,"Práce pokračují podle harmonogramu.",28,154,11,MUTED,false);text(c,"Materiál · počasí · poznámky",28,183,11,GOLD,true);}
            else if(sec.equals("Úkoly")){text(c,"ÚKOLY",28,98,11,MUTED,true);text(c,"✓ Zkontrolovat materiál",28,132,13,TEXT,true);text(c,"✓ Dokončit fotodokumentaci",28,162,13,TEXT,true);text(c,"○ Připravit předání",28,192,13,MUTED,true);}
            else if(sec.equals("Docházka")){text(c,"DOCHÁZKA",28,98,11,MUTED,true);text(c,"Přítomni dnes",28,132,27,TEXT,true);text(c,"12 pracovníků",28,159,11,MUTED,false);text(c,"Evidence se ukládá do zařízení.",28,194,10,GOLD,true);}
            else if(sec.equals("Materiál")){text(c,"MATERIÁL",28,98,11,MUTED,true);text(c,"Cihly",28,130,14,TEXT,true);text(c,"Sklad 1 240 ks",190,130,12,GOLD,true);text(c,"Izolace",28,165,14,TEXT,true);text(c,"Sklad 84 m²",190,165,12,GOLD,true);}
            else if(sec.equals("Dokumenty")){text(c,"DOKUMENTY",28,98,11,MUTED,true);text(c,"Smlouvy a nabídky",28,132,15,TEXT,true);text(c,"Rozpočty a předávací protokoly",28,163,11,MUTED,false);}
            else if(sec.equals("Adresář")){text(c,"ADRESÁŘ",28,98,11,MUTED,true);text(c,"Petr SOMSI",28,132,15,TEXT,true);text(c,PHONE,28,153,11,MUTED,false);text(c,"Zdeněk",28,186,15,TEXT,true);text(c,"Kontakt uložen v aplikaci",28,207,11,MUTED,false);}
            else if(sec.equals("Chat")){text(c,"TEAM CHAT",28,98,11,MUTED,true);List<String> ms=db.messages();int y=130;for(String m:ms){text(c,m,28,y,11,TEXT,false);y+=27;if(y>205)break;}box(c,28,250,w-28,305,Color.rgb(33,43,48),10);text(c,"Napsat zprávu…",43,283,11,MUTED,false);}
            else {text(c,"PŘEHLED",28,98,11,MUTED,true);text(c,"Sekce "+sec,28,138,21,TEXT,true);text(c,"Funkce je připravena pro práci v telefonu.",28,166,11,MUTED,false);}
            text(c,"Klepni na ‹ pro návrat",28,350,11,MUTED,false);
        }
        @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;float x=e.getX()/den,y=e.getY()/den;if(sec.equals("Domů")){if(y>=417&&y<700){int row=(int)((y-417)/94),col=(int)(x/(W()/3));int i=row*3+col;if(i>=0&&i<9){if(i==1)show("Fotodokumentace");else if(i==8)show("Chat");else show(names[i]);}}else if(y>=716&&y<800&&x>W()-130)MainActivity.this.call();else if(y>=814&&y<900)openWeb();}else if(y<60)show("Domů");else if(sec.equals("Chat")&&y>245&&y<330)chatDialog();return true;}
    }

    static class DB extends SQLiteOpenHelper{
        DB(Context c){super(c,"somsi.db",null,1);}
        public void onCreate(SQLiteDatabase d){d.execSQL("CREATE TABLE records(id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT,detail TEXT,created INTEGER)");d.execSQL("CREATE TABLE messages(id INTEGER PRIMARY KEY AUTOINCREMENT,author TEXT,msg TEXT,created INTEGER)");d.execSQL("INSERT INTO messages(author,msg,created) VALUES('SOMSI','Vítejte v interním chatu.',strftime('%s','now'))");}
        public void onUpgrade(SQLiteDatabase d,int a,int b){}
        void addRecord(String t,String x){ContentValues v=new ContentValues();v.put("title",t);v.put("detail",x);v.put("created",System.currentTimeMillis());getWritableDatabase().insert("records",null,v);}
        void addMessage(String a,String m){ContentValues v=new ContentValues();v.put("author",a);v.put("msg",m);v.put("created",System.currentTimeMillis());getWritableDatabase().insert("messages",null,v);}
        List<String> messages(){List<String> out=new ArrayList<>();Cursor c=getReadableDatabase().rawQuery("SELECT author,msg FROM messages ORDER BY id DESC LIMIT 6",null);while(c.moveToNext())out.add(c.getString(0)+": "+c.getString(1));c.close();return out;}
    }
}
