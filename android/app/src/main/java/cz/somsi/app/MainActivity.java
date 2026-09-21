package cz.somsi.app;

import android.app.*;
import android.os.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    static final int GOLD=Color.rgb(242,174,43), GOLD_DARK=Color.rgb(184,125,18), DARK=Color.rgb(6,12,16), PANEL=Color.rgb(17,25,30), PANEL2=Color.rgb(25,34,40), TEXT=Color.WHITE, MUTED=Color.rgb(190,198,203), GREEN=Color.rgb(91,214,112), RED=Color.rgb(239,78,58);
    static final String WEB="https://stavbysomsi.cz/", PHONE="+420736771754";
    FrameLayout root; SomsiView view; DB db; android.content.SharedPreferences pref;
    Bitmap hero; int visits,active,done,planned; int selectedNav=0;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(5,10,14));
        getWindow().setNavigationBarColor(Color.rgb(5,10,14));
        getWindow().getDecorView().setSystemUiVisibility(0);
        db=new DB(this); pref=getSharedPreferences("somsi",0);
        visits=pref.getInt("v",1248); active=pref.getInt("a",86); done=pref.getInt("d",67); planned=pref.getInt("p",12);
        hero=BitmapFactory.decodeResource(getResources(),R.drawable.realizace_hlavni);
        build();
    }
    int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    void save(){pref.edit().putInt("v",visits).putInt("a",active).putInt("d",done).putInt("p",planned).apply();}

    void build(){
        root=new FrameLayout(this); root.setBackgroundColor(DARK);
        view=new SomsiView(this);
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true); scroll.setClipToPadding(false);
        scroll.addView(view,new ScrollView.LayoutParams(-1,dp(1335)));
        root.addView(scroll,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout nav=new LinearLayout(this); nav.setOrientation(LinearLayout.HORIZONTAL); nav.setGravity(Gravity.CENTER); nav.setPadding(0,dp(3),0,dp(3)); nav.setBackgroundColor(Color.rgb(7,13,17));
        String[][] items={{"⌂","Domů"},{"▣","Zakázky"},{"＋","Nový záznam"},{"●","Chat"},{"☰","Menu"}};
        for(int i=0;i<items.length;i++){
            final int ix=i; LinearLayout cell=new LinearLayout(this); cell.setOrientation(LinearLayout.VERTICAL); cell.setGravity(Gravity.CENTER); cell.setPadding(0,dp(3),0,dp(2));
            TextView icon=new TextView(this); icon.setText(items[i][0]); icon.setTextSize(i==2?22:19); icon.setGravity(Gravity.CENTER); icon.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
            TextView label=new TextView(this); label.setText(items[i][1]); label.setTextSize(i==2?10:9); label.setGravity(Gravity.CENTER); label.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
            cell.addView(icon,new LinearLayout.LayoutParams(-1,dp(27))); cell.addView(label,new LinearLayout.LayoutParams(-1,dp(22)));
            cell.setOnClickListener(v->{
                selectedNav=ix; updateNav(nav);
                if(ix==0) show("Domů"); else if(ix==1) show("Zakázky"); else if(ix==2) newRecord(); else if(ix==3){show("Chat"); chatDialog();} else menu();
            });
            nav.addView(cell,new LinearLayout.LayoutParams(0,dp(62),1));
        }
        root.addView(nav,new FrameLayout.LayoutParams(-1,dp(70),Gravity.BOTTOM));
        setContentView(root); updateNav(nav);
    }
    void updateNav(LinearLayout nav){
        for(int i=0;i<nav.getChildCount();i++){
            LinearLayout cell=(LinearLayout)nav.getChildAt(i); int col=i==selectedNav?GOLD:MUTED;
            ((TextView)cell.getChildAt(0)).setTextColor(col); ((TextView)cell.getChildAt(1)).setTextColor(col);
            if(i==selectedNav){GradientDrawable bg=new GradientDrawable();bg.setColor(Color.rgb(25,34,40));bg.setCornerRadius(dp(12));cell.setBackground(bg);}else cell.setBackgroundColor(Color.TRANSPARENT);
        }
    }
    void show(String x){view.sec=x; view.invalidate();}
    void newRecord(){visits++; active++; db.addRecord("Nový záznam","Vytvořen v aplikaci"); save(); Toast.makeText(this,"Nový záznam uložen do databáze",Toast.LENGTH_SHORT).show(); view.invalidate();}
    void openWeb(){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(WEB)));}catch(Exception e){Toast.makeText(this,"Web nelze otevřít",Toast.LENGTH_SHORT).show();}}
    void call(){try{startActivity(new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+PHONE)));}catch(Exception e){Toast.makeText(this,"Telefon není dostupný",Toast.LENGTH_SHORT).show();}}
    void camera(){try{startActivity(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));}catch(Exception e){Toast.makeText(this,"Fotoaparát není dostupný",Toast.LENGTH_SHORT).show();}}
    void menu(){
        final String[] items={"Zakázky","Fotodokumentace","Stavební deník","Úkoly","Docházka","Materiál","Dokumenty","Adresář","Otevřít web SOMSI","Zavolat SOMSI"};
        new AlertDialog.Builder(this).setTitle("SOMSI – Menu").setItems(items,(d,w)->{ if(w==8)openWeb(); else if(w==9)call(); else if(w==1)camera(); else show(items[w]); }).show();
    }
    void chatDialog(){
        final EditText e=new EditText(this); e.setHint("Napište zprávu…"); e.setMinLines(3); e.setGravity(Gravity.TOP);
        LinearLayout box=new LinearLayout(this); box.setPadding(dp(18),dp(4),dp(18),0); box.addView(e,new LinearLayout.LayoutParams(-1,-2));
        new AlertDialog.Builder(this).setTitle("SOMSI Chat").setMessage("Zpráva se uloží do místní databáze telefonu.").setView(box).setNegativeButton("Zrušit",null).setPositiveButton("Odeslat",(d,w)->{String msg=e.getText().toString().trim();if(!msg.isEmpty()){db.addMessage("Já",msg);Toast.makeText(this,"Zpráva uložena",Toast.LENGTH_SHORT).show();view.invalidate();}}).show();
    }

    class SomsiView extends View{
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG); RectF r=new RectF(); String sec="Domů"; float den;
        String[] names={"Zakázky","Fotodokumentace","Stavební deník","Úkoly","Docházka","Materiál","Dokumenty","Adresář","Chat"};
        String[] icons={"▣","◉","▤","✓","●","◆","▤","♟","●"};
        SomsiView(Context c){super(c);den=getResources().getDisplayMetrics().density;setFocusable(true);}
        float W(){return getWidth()/den;}
        void text(Canvas c,String s,float x,float y,float z,int col,boolean b){p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(col);p.setTextSize(dp(z));p.setTypeface(Typeface.create("sans",b?Typeface.BOLD:Typeface.NORMAL));c.drawText(s,dp(x),dp(y),p);}
        void center(Canvas c,String s,float x,float y,float z,int col,boolean b){p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(col);p.setTextSize(dp(z));p.setTypeface(Typeface.create("sans",b?Typeface.BOLD:Typeface.NORMAL));c.drawText(s,dp(x)-p.measureText(s)/2,dp(y),p);}
        void box(Canvas c,float l,float t,float rr,float bb,int col,float rad){p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(col);r.set(dp(l),dp(t),dp(rr),dp(bb));c.drawRoundRect(r,dp(rad),dp(rad),p);}
        void stroke(Canvas c,float l,float t,float rr,float bb,int col,float rad,float sw){p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(sw));p.setColor(col);r.set(dp(l),dp(t),dp(rr),dp(bb));c.drawRoundRect(r,dp(rad),dp(rad),p);p.setStyle(Paint.Style.FILL);}
        void line(Canvas c,float x1,float y1,float x2,float y2,int col,float sw){p.setShader(null);p.setColor(col);p.setStrokeWidth(dp(sw));c.drawLine(dp(x1),dp(y1),dp(x2),dp(y2),p);}
        @Override protected void onDraw(Canvas c){c.drawColor(DARK); if(sec.equals("Domů"))home(c); else page(c);}

        void logo(Canvas c,float cx,float top){
            p.setColor(GOLD);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(5));p.setStrokeCap(Paint.Cap.SQUARE);
            Path roof=new Path();roof.moveTo(dp(cx-31),dp(top+25));roof.lineTo(dp(cx),dp(top));roof.lineTo(dp(cx+31),dp(top+25));c.drawPath(roof,p);c.drawLine(dp(cx-17),dp(top+15),dp(cx-17),dp(top+47),p);p.setStyle(Paint.Style.FILL);p.setStrokeCap(Paint.Cap.BUTT);
            center(c,"SOMSI",cx,top+56,31,TEXT,true); center(c,"stavební práce",cx,top+79,15,TEXT,false); center(c,"Od základu po střechu",cx,top+108,18,GOLD,true);
        }
        void home(Canvas c){float w=W();
            if(hero!=null){float scale=Math.max(getWidth()/(float)hero.getWidth(),dp(355)/(float)hero.getHeight());float bw=hero.getWidth()*scale,bh=hero.getHeight()*scale;c.save();c.clipRect(0,0,getWidth(),dp(355));c.drawBitmap(hero,null,new RectF((getWidth()-bw)/2,0,(getWidth()+bw)/2,bh),p);c.restore();p.setColor(0x52000000);c.drawRect(0,0,getWidth(),dp(355),p);}
            else{p.setColor(Color.rgb(35,44,48));c.drawRect(0,0,getWidth(),dp(355),p);}
            text(c,"☰",17,39,30,TEXT,false); stroke(c,w-68,14,w-16,54,GOLD,14,1.5f);center(c,"WEB",w-42,39,11,GOLD,true);
            logo(c,w/2,25);
            text(c,"KVALITA",w-116,124,9,MUTED,false);text(c,"ZKUŠENOSTI",w-116,144,9,MUTED,false);text(c,"SPOLEHLIVOST",w-116,164,9,MUTED,false);
            text(c,"Stavíme",w-116,242,20,TEXT,true);text(c,"Vaše plány",w-116,267,20,TEXT,true);text(c,"do reality",w-116,292,20,TEXT,true);line(c,w-116,306,w-30,306,GOLD,2);
            stats(c);grid(c);callPanel(c);webPanel(c);contactStrip(c);
        }
        void stats(Canvas c){float gap=6,l=10,top=372,cw=(W()-20-gap*3)/4;String[] n={"Návštěvy","Zakázky","Dokončeno","Plán"};String[] v={""+visits,""+active,""+done,""+planned};for(int i=0;i<4;i++){float x=l+i*(cw+gap);box(c,x,top,x+cw,top+92,PANEL,14);center(c,v[i],x+cw/2,top+38,18,TEXT,true);center(c,n[i],x+cw/2,top+62,9,MUTED,false);if(i<2)center(c,i==0?"+12% ↗":"+8% ↗",x+cw/2,top+82,8,GREEN,true);}}
        void iconTile(Canvas c,float x,float y,float cw,float ch,int i){int bg=i==0?GOLD_DARK:PANEL;box(c,x,y,x+cw,y+ch,bg,15);box(c,x+9,y+10,x+42,y+43,i==0?Color.rgb(250,199,88):Color.rgb(40,54,61),11);center(c,icons[i],x+25.5f,y+34,17,i==0?Color.BLACK:GOLD,true);text(c,names[i],x+51,y+38,10,TEXT,true);text(c,"›",x+cw-21,y+41,20,GOLD,true);if(i==8){box(c,x+cw-30,y+7,x+cw-7,y+30,RED,12);center(c,"3",x+cw-18.5f,y+23,9,TEXT,true);}}
        void grid(Canvas c){float gap=7,l=10,top=481,cw=(W()-20-gap*2)/3,ch=91;for(int i=0;i<9;i++){int row=i/3,col=i%3;float x=l+col*(cw+gap),y=top+row*(ch+gap);iconTile(c,x,y,cw,ch,i);}}
        void callPanel(Canvas c){float y=784,w=W();box(c,10,y,w-10,y+86,Color.rgb(15,23,28),15);stroke(c,10,y,w-10,y+86,GOLD,15,1.2f);text(c,"☎",25,y+51,26,GOLD,true);text(c,"Potřebujete konzultaci?",62,y+33,13,TEXT,true);text(c,"Ozvěte se nám!",62,y+55,12,MUTED,false);box(c,w-125,y+17,w-24,y+68,GOLD,11);center(c,"Zavolat",w-74.5f,y+49,12,Color.BLACK,true);}
        void webPanel(Canvas c){float y=884,w=W();box(c,10,y,w-10,y+76,PANEL2,14);text(c,"◉",24,y+45,21,GOLD,true);text(c,"stavbysomsi.cz",59,y+30,13,TEXT,true);text(c,"Oficiální web • realizace • služby • kontakt",59,y+52,9,MUTED,false);box(c,w-108,y+15,w-25,y+59,Color.rgb(41,56,63),10);center(c,"OTEVŘÍT",w-66.5f,y+42,9,GOLD,true);}
        void contactStrip(Canvas c){float y=974,w=W();box(c,10,y,w-10,y+90,PANEL,14);text(c,"⌖",25,y+37,21,GOLD,true);text(c,"Horní Studenec 29",57,y+31,11,TEXT,true);text(c,"Pardubický kraj • Královéhradecký kraj • Vysočina",57,y+54,8,MUTED,false);text(c,"☎  +420 736 771 754",57,y+76,10,GOLD,true);}

        void page(Canvas c){float w=W();
            box(c,0,0,w,66,Color.rgb(8,14,18),0);text(c,"‹",17,43,31,GOLD,true);text(c,sec,58,40,21,TEXT,true);text(c,"SOMSI",w-76,39,16,GOLD,true);
            box(c,12,83,w-12,285,PANEL,16);
            if(sec.equals("Zakázky")){sectionTitle(c,"AKTIVNÍ ZAKÁZKY");text(c,"86",28,145,32,TEXT,true);text(c,"Novostavba – Horní Studenec",112,107,13,TEXT,true);text(c,"Fasáda a zateplení",112,134,11,MUTED,false);text(c,"Dokončení: 30. 10. 2026",112,161,10,GOLD,true);stroke(c,28,192,w-28,258,GOLD,10,1);text(c,"+ NOVÁ ZAKÁZKA",43,231,11,GOLD,true);}
            else if(sec.equals("Fotodokumentace")){sectionTitle(c,"FOTODOKUMENTACE");text(c,"Fotografie realizací",28,125,18,TEXT,true);c.drawBitmap(hero,null,new RectF(dp(28),dp(145),dp(w-28),dp(255)),p);text(c,"Aktuální fotografie ze staveb SOMSI",28,276,10,MUTED,false);}
            else if(sec.equals("Stavební deník")){sectionTitle(c,"STAVEBNÍ DENÍK");text(c,"Dnes",28,128,18,TEXT,true);text(c,"Práce pokračují podle harmonogramu.",28,158,11,MUTED,false);text(c,"Materiál  •  počasí  •  poznámky",28,190,11,GOLD,true);text(c,"Nový zápis lze přidat tlačítkem + dole.",28,225,10,MUTED,false);}
            else if(sec.equals("Úkoly")){sectionTitle(c,"ÚKOLY");text(c,"✓  Zkontrolovat materiál",28,130,13,TEXT,true);text(c,"✓  Dokončit fotodokumentaci",28,163,13,TEXT,true);text(c,"○  Připravit předání stavby",28,196,13,MUTED,true);}
            else if(sec.equals("Docházka")){sectionTitle(c,"DOCHÁZKA");text(c,"Přítomni dnes",28,130,17,TEXT,true);text(c,"12 pracovníků",28,162,28,TEXT,true);text(c,"Evidence se ukládá do zařízení.",28,205,10,GOLD,true);}
            else if(sec.equals("Materiál")){sectionTitle(c,"MATERIÁL");text(c,"Cihly",28,132,14,TEXT,true);text(c,"1 240 ks",w-105,132,12,GOLD,true);text(c,"Izolace",28,170,14,TEXT,true);text(c,"84 m²",w-105,170,12,GOLD,true);text(c,"Cement",28,208,14,TEXT,true);text(c,"160 pytlů",w-105,208,12,GOLD,true);}
            else if(sec.equals("Dokumenty")){sectionTitle(c,"DOKUMENTY");text(c,"Smlouvy a nabídky",28,132,15,TEXT,true);text(c,"Rozpočty a předávací protokoly",28,166,11,MUTED,false);text(c,"Dokumenty jsou připravené pro další rozšíření.",28,208,10,GOLD,true);}
            else if(sec.equals("Adresář")){sectionTitle(c,"ADRESÁŘ");text(c,"Petr SOMSI",28,132,15,TEXT,true);text(c,"+420 736 771 754",28,157,11,GOLD,true);text(c,"Zdeněk",28,196,15,TEXT,true);text(c,"Kontakty uložené v aplikaci",28,220,10,MUTED,false);}
            else if(sec.equals("Chat")){sectionTitle(c,"TEAM CHAT");List<String> ms=db.messages();int y=132;for(String m:ms){text(c,m,28,y,11,TEXT,false);y+=28;if(y>225)break;}stroke(c,28,238,w-28,285,GOLD,10,1);text(c,"Napsat zprávu…",43,267,11,MUTED,false);}
            else {sectionTitle(c,"PŘEHLED");text(c,"Sekce "+sec,28,132,21,TEXT,true);text(c,"Funkce je připravena pro práci v telefonu.",28,165,11,MUTED,false);}
            text(c,"‹  Zpět na úvod",28,330,11,MUTED,false);
        }
        void sectionTitle(Canvas c,String s){text(c,s,28,110,11,MUTED,true);}
        @Override public boolean onTouchEvent(MotionEvent e){
            if(e.getAction()!=MotionEvent.ACTION_UP)return true;float x=e.getX()/den,y=e.getY()/den,w=W();
            if(sec.equals("Domů")){
                if(y<75 && x<80){menu();return true;}
                if(y<75 && x>w-90){openWeb();return true;}
                if(y>=481&&y<782){int row=(int)((y-481)/98),col=(int)(x/(w/3));int i=row*3+col;if(i>=0&&i<9){if(i==8){show("Chat");chatDialog();}else show(names[i]);}return true;}
                if(y>=784&&y<870&&x>w-145){call();return true;}
                if(y>=884&&y<968){openWeb();return true;}
                if(y>=974&&y<1070&&x>35&&x<260){call();return true;}
            } else if(y<70){show("Domů");selectedNav=0;invalidate();return true;}
            return true;
        }
    }

    static class DB extends SQLiteOpenHelper{
        DB(Context c){super(c,"somsi.db",null,2);}
        public void onCreate(SQLiteDatabase d){d.execSQL("CREATE TABLE records(id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT,detail TEXT,created INTEGER)");d.execSQL("CREATE TABLE messages(id INTEGER PRIMARY KEY AUTOINCREMENT,author TEXT,msg TEXT,created INTEGER)");d.execSQL("INSERT INTO messages(author,msg,created) VALUES('SOMSI','Vítejte v interním chatu.',strftime('%s','now'))");}
        public void onUpgrade(SQLiteDatabase d,int a,int b){if(a<2){} }
        void addRecord(String t,String x){ContentValues v=new ContentValues();v.put("title",t);v.put("detail",x);v.put("created",System.currentTimeMillis());getWritableDatabase().insert("records",null,v);}
        void addMessage(String a,String m){ContentValues v=new ContentValues();v.put("author",a);v.put("msg",m);v.put("created",System.currentTimeMillis());getWritableDatabase().insert("messages",null,v);}
        List<String> messages(){List<String> out=new ArrayList<>();Cursor c=getReadableDatabase().rawQuery("SELECT author,msg FROM messages ORDER BY id DESC LIMIT 6",null);while(c.moveToNext())out.add(c.getString(0)+": "+c.getString(1));c.close();return out;}
    }
}
