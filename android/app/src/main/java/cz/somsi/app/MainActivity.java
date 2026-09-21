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
    static final int GOLD=Color.rgb(242,174,43), GOLD_LIGHT=Color.rgb(255,196,75), GOLD_DARK=Color.rgb(184,125,18);
    static final int DARK=Color.rgb(5,11,15), PANEL=Color.rgb(16,24,29), PANEL2=Color.rgb(23,33,39);
    static final int TEXT=Color.WHITE, MUTED=Color.rgb(190,198,203), GREEN=Color.rgb(91,214,112), RED=Color.rgb(239,78,58);
    static final String WEB="https://stavbysomsi.cz/", PHONE="+420736771754";

    FrameLayout root; SomsiView view; DB db; android.content.SharedPreferences pref; Bitmap hero;
    int visits,active,done,planned,selectedNav=0; LinearLayout bottomNav;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(DARK);
        getWindow().setNavigationBarColor(DARK);
        if(Build.VERSION.SDK_INT>=29) getWindow().setNavigationBarContrastEnforced(false);
        if(Build.VERSION.SDK_INT>=30) getWindow().setDecorFitsSystemWindows(false);
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
        scroll.setPadding(0,0,0,dp(96));
        scroll.addView(view,new ScrollView.LayoutParams(-1,dp(1400)));
        root.addView(scroll,new FrameLayout.LayoutParams(-1,-1));

        bottomNav=new LinearLayout(this); bottomNav.setOrientation(LinearLayout.HORIZONTAL); bottomNav.setGravity(Gravity.CENTER); bottomNav.setBackgroundColor(Color.rgb(7,13,17));
        bottomNav.setPadding(dp(3),dp(4),dp(3),dp(4));
        String[][] items={{"⌂","Domů"},{"▣","Zakázky"},{"＋","Nový záznam"},{"●","Chat"},{"☰","Menu"}};
        for(int i=0;i<items.length;i++){
            final int ix=i; LinearLayout cell=new LinearLayout(this); cell.setOrientation(LinearLayout.VERTICAL); cell.setGravity(Gravity.CENTER); cell.setPadding(0,dp(2),0,dp(1));
            TextView icon=new TextView(this); icon.setText(items[i][0]); icon.setTextSize(i==2?22:20); icon.setGravity(Gravity.CENTER); icon.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
            TextView label=new TextView(this); label.setText(items[i][1]); label.setTextSize(i==2?9:10); label.setGravity(Gravity.CENTER); label.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
            cell.addView(icon,new LinearLayout.LayoutParams(-1,dp(27))); cell.addView(label,new LinearLayout.LayoutParams(-1,dp(22)));
            cell.setOnClickListener(v->{ selectedNav=ix; updateNav(); if(ix==0)show("Domů"); else if(ix==1)show("Zakázky"); else if(ix==2)newRecord(); else if(ix==3){show("Chat");chatDialog();} else menu(); });
            bottomNav.addView(cell,new LinearLayout.LayoutParams(0,dp(62),1));
        }
        root.addView(bottomNav,new FrameLayout.LayoutParams(-1,dp(70),Gravity.BOTTOM));
        bottomNav.setOnApplyWindowInsetsListener((v,insets)->{
            int bottom=0;
            if(Build.VERSION.SDK_INT>=30) bottom=insets.getInsets(WindowInsets.Type.navigationBars()).bottom;
            else bottom=insets.getSystemWindowInsetBottom();
            ViewGroup.LayoutParams lp=v.getLayoutParams(); lp.height=dp(70)+bottom; v.setLayoutParams(lp);
            v.setPadding(dp(3),dp(4),dp(3),bottom+dp(4));
            return insets;
        });
        setContentView(root); updateNav();
    }
    void updateNav(){
        if(bottomNav==null)return;
        for(int i=0;i<bottomNav.getChildCount();i++){
            LinearLayout cell=(LinearLayout)bottomNav.getChildAt(i); int col=i==selectedNav?GOLD_LIGHT:MUTED;
            ((TextView)cell.getChildAt(0)).setTextColor(col); ((TextView)cell.getChildAt(1)).setTextColor(col);
            if(i==selectedNav){GradientDrawable bg=new GradientDrawable();bg.setColor(Color.rgb(25,35,41));bg.setCornerRadius(dp(14));cell.setBackground(bg);}else cell.setBackgroundColor(Color.TRANSPARENT);
        }
    }
    void show(String x){view.sec=x;view.invalidate();}
    void newRecord(){visits++;active++;db.addRecord("Nový záznam","Vytvořen v aplikaci");save();Toast.makeText(this,"Nový záznam uložen",Toast.LENGTH_SHORT).show();view.invalidate();}
    void openWeb(){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(WEB)));}catch(Exception e){Toast.makeText(this,"Web nelze otevřít",Toast.LENGTH_SHORT).show();}}
    void call(){try{startActivity(new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+PHONE)));}catch(Exception e){Toast.makeText(this,"Telefon není dostupný",Toast.LENGTH_SHORT).show();}}
    void camera(){try{startActivity(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));}catch(Exception e){Toast.makeText(this,"Fotoaparát není dostupný",Toast.LENGTH_SHORT).show();}}
    void menu(){
        final String[] items={"Zakázky","Fotodokumentace","Stavební deník","Úkoly","Docházka","Materiál","Dokumenty","Adresář","Otevřít web SOMSI","Zavolat SOMSI"};
        new AlertDialog.Builder(this).setTitle("SOMSI – Menu").setItems(items,(d,w)->{if(w==8)openWeb();else if(w==9)call();else if(w==1)camera();else show(items[w]);}).show();
    }
    void chatDialog(){
        final EditText e=new EditText(this);e.setHint("Napište zprávu…");e.setMinLines(3);e.setGravity(Gravity.TOP);
        LinearLayout box=new LinearLayout(this);box.setPadding(dp(18),dp(4),dp(18),0);box.addView(e,new LinearLayout.LayoutParams(-1,-2));
        new AlertDialog.Builder(this).setTitle("SOMSI Chat").setMessage("Zpráva se uloží do místní databáze telefonu.").setView(box).setNegativeButton("Zrušit",null).setPositiveButton("Odeslat",(d,w)->{String msg=e.getText().toString().trim();if(!msg.isEmpty()){db.addMessage("Já",msg);Toast.makeText(this,"Zpráva uložena",Toast.LENGTH_SHORT).show();view.invalidate();}}).show();
    }

    class SomsiView extends View{
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);RectF r=new RectF();String sec="Domů";float den;
        String[] names={"Zakázky","Fotodokumentace","Stavební deník","Úkoly","Docházka","Materiál","Dokumenty","Adresář","Chat"};
        SomsiView(Context c){super(c);den=getResources().getDisplayMetrics().density;setFocusable(true);}
        float W(){return getWidth()/den;}
        void text(Canvas c,String s,float x,float y,float z,int col,boolean b){p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(col);p.setTextSize(dp(z));p.setTypeface(Typeface.create("sans",b?Typeface.BOLD:Typeface.NORMAL));c.drawText(s,dp(x),dp(y),p);}
        void center(Canvas c,String s,float x,float y,float z,int col,boolean b){p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(col);p.setTextSize(dp(z));p.setTypeface(Typeface.create("sans",b?Typeface.BOLD:Typeface.NORMAL));c.drawText(s,dp(x)-p.measureText(s)/2,dp(y),p);}
        void box(Canvas c,float l,float t,float rr,float bb,int col,float rad){p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(col);r.set(dp(l),dp(t),dp(rr),dp(bb));c.drawRoundRect(r,dp(rad),dp(rad),p);}
        void stroke(Canvas c,float l,float t,float rr,float bb,int col,float rad,float sw){p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(sw));p.setColor(col);r.set(dp(l),dp(t),dp(rr),dp(bb));c.drawRoundRect(r,dp(rad),dp(rad),p);p.setStyle(Paint.Style.FILL);}
        void line(Canvas c,float x1,float y1,float x2,float y2,int col,float sw){p.setShader(null);p.setColor(col);p.setStrokeWidth(dp(sw));c.drawLine(dp(x1),dp(y1),dp(x2),dp(y2),p);}
        @Override protected void onDraw(Canvas c){c.drawColor(DARK);if(sec.equals("Domů"))home(c);else page(c);}

        void logo(Canvas c,float cx,float top){
            box(c,cx-132,top-8,cx+132,top+143,0x66050B0F,22);
            p.setColor(GOLD);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(7));p.setStrokeCap(Paint.Cap.SQUARE);
            Path roof=new Path();roof.moveTo(dp(cx-78),dp(top+45));roof.lineTo(dp(cx),dp(top));roof.lineTo(dp(cx+78),dp(top+45));c.drawPath(roof,p);c.drawLine(dp(cx-42),dp(top+27),dp(cx-42),dp(top+82),p);p.setStyle(Paint.Style.FILL);p.setStrokeCap(Paint.Cap.BUTT);
            center(c,"SOMSI",cx,top+73,38,TEXT,true);center(c,"stavební práce",cx,top+101,18,TEXT,false);center(c,"Od základu po střechu",cx,top+129,19,GOLD_LIGHT,true);
        }
        void home(Canvas c){float w=W();
            if(hero!=null){float scale=Math.max(getWidth()/(float)hero.getWidth(),dp(405)/(float)hero.getHeight());float bw=hero.getWidth()*scale,bh=hero.getHeight()*scale;c.save();c.clipRect(0,0,getWidth(),dp(405));c.drawBitmap(hero,null,new RectF((getWidth()-bw)/2,0,(getWidth()+bw)/2,bh),p);c.restore();p.setColor(0x28000000);c.drawRect(0,0,getWidth(),dp(405),p);}else{p.setColor(PANEL2);c.drawRect(0,0,getWidth(),dp(405),p);}
            text(c,"☰",18,48,32,TEXT,false);
            box(c,w-89,17,w-16,59,0xB0050B0F,14);center(c,"WEB",w-52.5f,45,12,GOLD_LIGHT,true);
            logo(c,w/2,43);
            text(c,"KVALITA",w-116,150,9,MUTED,true);text(c,"ZKUŠENOSTI",w-116,171,9,MUTED,true);text(c,"SPOLEHLIVOST",w-116,192,9,MUTED,true);
            text(c,"Stavíme",w-116,276,21,TEXT,true);text(c,"Vaše plány",w-116,302,21,TEXT,true);text(c,"do reality",w-116,328,21,TEXT,true);line(c,w-116,343,w-28,343,GOLD,2);
            stats(c);grid(c);callPanel(c);webPanel(c);contactStrip(c);
        }
        void stats(Canvas c){float gap=7,l=10,top=422,cw=(W()-20-gap*3)/4;String[] n={"Návštěvy","Zakázky","Dokončeno","Plán"};String[] v={""+visits,""+active,""+done,""+planned};for(int i=0;i<4;i++){float x=l+i*(cw+gap);box(c,x,top,x+cw,top+96,PANEL,15);center(c,v[i],x+cw/2,top+39,19,TEXT,true);center(c,n[i],x+cw/2,top+65,9,MUTED,false);if(i<2)center(c,i==0?"+12% ↗":"+8% ↗",x+cw/2,top+86,8,GREEN,true);}}
        void drawTileIcon(Canvas c,float x,float y,int i,int col){p.setColor(col);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(2.5f));p.setStrokeCap(Paint.Cap.ROUND);float cx=dp(x),cy=dp(y);if(i==0){c.drawRect(cx-dp(9),cy-dp(7),cx+dp(9),cy+dp(7),p);c.drawLine(cx-dp(5),cy-dp(11),cx+dp(5),cy-dp(11),p);}else if(i==1){c.drawCircle(cx,cy,dp(9),p);c.drawLine(cx-dp(9),cy+dp(7),cx+dp(9),cy+dp(7),p);}else if(i==2){c.drawRect(cx-dp(8),cy-dp(10),cx+dp(8),cy+dp(10),p);c.drawLine(cx-dp(5),cy-dp(3),cx+dp(5),cy-dp(3),p);c.drawLine(cx-dp(5),cy+dp(2),cx+dp(5),cy+dp(2),p);}else if(i==3){c.drawLine(cx-dp(9),cy, cx-dp(2),cy+dp(7),p);c.drawLine(cx-dp(2),cy+dp(7),cx+dp(10),cy-dp(8),p);}else if(i==4){c.drawCircle(cx,cy-dp(4),dp(4),p);c.drawArc(new RectF(cx-dp(10),cy+dp(1),cx+dp(10),cy+dp(13)),180,180,false,p);}else if(i==5){Path q=new Path();q.moveTo(cx,cy-dp(10));q.lineTo(cx+dp(10),cy);q.lineTo(cx,cy+dp(10));q.lineTo(cx-dp(10),cy);q.close();c.drawPath(q,p);}else if(i==6){c.drawRect(cx-dp(8),cy-dp(10),cx+dp(8),cy+dp(10),p);c.drawLine(cx-dp(5),cy-dp(3),cx+dp(5),cy-dp(3),p);c.drawLine(cx-dp(5),cy+dp(2),cx+dp(5),cy+dp(2),p);}else if(i==7){c.drawCircle(cx,cy-dp(5),dp(5),p);c.drawRoundRect(new RectF(cx-dp(10),cy+dp(1),cx+dp(10),cy+dp(11)),dp(4),dp(4),p);}else{c.drawCircle(cx,cy,dp(9),p);c.drawCircle(cx-dp(4),cy,dp(1.2f),p);c.drawCircle(cx,cy,dp(1.2f),p);c.drawCircle(cx+dp(4),cy,dp(1.2f),p);}p.setStyle(Paint.Style.FILL);p.setStrokeCap(Paint.Cap.BUTT);}
        void iconTile(Canvas c,float x,float y,float cw,float ch,int i){int bg=i==0?GOLD_DARK:PANEL;box(c,x,y,x+cw,y+ch,bg,16);box(c,x+9,y+10,x+47,y+48,i==0?Color.rgb(250,199,88):Color.rgb(40,54,61),12);drawTileIcon(c,x+28,y+29,i,i==0?Color.BLACK:GOLD_LIGHT);text(c,names[i],x+56,y+39,10.5f,TEXT,true);text(c,"›",x+cw-20,y+42,22,GOLD_LIGHT,true);if(i==8){box(c,x+cw-31,y+7,x+cw-7,y+31,RED,13);center(c,"3",x+cw-19,y+24,9,TEXT,true);}}
        void grid(Canvas c){float gap=8,l=10,top=535,cw=(W()-20-gap*2)/3,ch=96;for(int i=0;i<9;i++){int row=i/3,col=i%3;float x=l+col*(cw+gap),y=top+row*(ch+gap);iconTile(c,x,y,cw,ch,i);}}
        void callPanel(Canvas c){float y=847,w=W();box(c,10,y,w-10,y+88,Color.rgb(15,23,28),15);stroke(c,10,y,w-10,y+88,GOLD_LIGHT,15,1.5f);text(c,"☎",25,y+53,28,GOLD_LIGHT,true);text(c,"Potřebujete konzultaci?",66,y+34,13,TEXT,true);text(c,"Ozvěte se nám!",66,y+57,12,MUTED,false);box(c,w-130,y+18,w-23,y+70,GOLD_LIGHT,11);center(c,"Zavolat",w-76.5f,y+50,12,Color.BLACK,true);}
        void webPanel(Canvas c){float y=950,w=W();box(c,10,y,w-10,y+79,PANEL2,14);text(c,"◎",24,y+46,24,GOLD_LIGHT,true);text(c,"stavbysomsi.cz",61,y+31,13,TEXT,true);text(c,"Oficiální web • realizace • služby • kontakt",61,y+53,9,MUTED,false);box(c,w-111,y+16,w-24,y+61,Color.rgb(41,56,63),10);center(c,"OTEVŘÍT",w-67.5f,y+44,9,GOLD_LIGHT,true);}
        void contactStrip(Canvas c){float y=1045,w=W();box(c,10,y,w-10,y+93,PANEL,14);text(c,"⌖",25,y+38,23,GOLD_LIGHT,true);text(c,"Horní Studenec 29",59,y+31,11,TEXT,true);text(c,"Pardubický kraj • Královéhradecký kraj • Vysočina",59,y+54,8.5f,MUTED,false);text(c,"☎  +420 736 771 754",59,y+78,10,GOLD_LIGHT,true);}

        void page(Canvas c){float w=W();box(c,0,0,w,70,Color.rgb(8,14,18),0);text(c,"‹",17,46,33,GOLD_LIGHT,true);text(c,sec,60,43,21,TEXT,true);text(c,"SOMSI",w-80,42,16,GOLD_LIGHT,true);box(c,12,87,w-12,300,PANEL,16);sectionTitle(c,sec.toUpperCase());
            if(sec.equals("Zakázky")){text(c,"86",28,157,34,TEXT,true);text(c,"Aktivní zakázka",112,122,14,TEXT,true);text(c,"Novostavba – Horní Studenec",112,150,12,MUTED,false);text(c,"Dokončení: 30. 10. 2026",112,178,11,GOLD_LIGHT,true);stroke(c,28,205,w-28,270,GOLD,10,1);text(c,"+ NOVÁ ZAKÁZKA",43,244,11,GOLD_LIGHT,true);}
            else if(sec.equals("Fotodokumentace")){text(c,"Realizace SOMSI",28,127,18,TEXT,true);if(hero!=null)c.drawBitmap(hero,null,new RectF(dp(28),dp(145),dp(w-28),dp(270)),p);text(c,"Fotografie lze rozšířit o další realizace.",28,292,10,MUTED,false);}
            else if(sec.equals("Stavební deník")){text(c,"Dnešní zápis",28,133,18,TEXT,true);text(c,"Práce pokračují podle harmonogramu.",28,165,11,MUTED,false);text(c,"Materiál  •  počasí  •  poznámky",28,198,11,GOLD_LIGHT,true);text(c,"Nový zápis lze přidat tlačítkem +.",28,233,10,MUTED,false);}
            else if(sec.equals("Úkoly")){text(c,"✓  Zkontrolovat materiál",28,137,13,TEXT,true);text(c,"✓  Dokončit fotodokumentaci",28,173,13,TEXT,true);text(c,"○  Připravit předání stavby",28,209,13,MUTED,true);}
            else if(sec.equals("Docházka")){text(c,"Přítomni dnes",28,137,17,TEXT,true);text(c,"12 pracovníků",28,173,28,TEXT,true);text(c,"Evidence se ukládá do zařízení.",28,215,10,GOLD_LIGHT,true);}
            else if(sec.equals("Materiál")){text(c,"Cihly",28,137,14,TEXT,true);text(c,"1 240 ks",w-105,137,12,GOLD_LIGHT,true);text(c,"Izolace",28,177,14,TEXT,true);text(c,"84 m²",w-105,177,12,GOLD_LIGHT,true);text(c,"Cement",28,217,14,TEXT,true);text(c,"160 pytlů",w-105,217,12,GOLD_LIGHT,true);}
            else if(sec.equals("Dokumenty")){text(c,"Smlouvy a nabídky",28,137,15,TEXT,true);text(c,"Rozpočty a předávací protokoly",28,172,11,MUTED,false);text(c,"Dokumenty jsou připravené pro další rozšíření.",28,215,10,GOLD_LIGHT,true);}
            else if(sec.equals("Adresář")){text(c,"Petr SOMSI",28,137,15,TEXT,true);text(c,"+420 736 771 754",28,164,11,GOLD_LIGHT,true);text(c,"Zdeněk",28,205,15,TEXT,true);text(c,"Kontakty uložené v aplikaci",28,232,10,MUTED,false);}
            else if(sec.equals("Chat")){List<String> ms=db.messages();int yy=137;for(String m:ms){text(c,m,28,yy,11,TEXT,false);yy+=30;if(yy>246)break;}stroke(c,28,255,w-28,300,GOLD,10,1);text(c,"Napsat zprávu…",43,284,11,MUTED,false);}
            else {text(c,"Sekce "+sec,28,137,21,TEXT,true);text(c,"Funkce je připravena pro práci v telefonu.",28,172,11,MUTED,false);}
            text(c,"‹  Zpět na úvod",28,345,11,MUTED,false);
        }
        void sectionTitle(Canvas c,String s){text(c,s,28,111,11,MUTED,true);}
        @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;float x=e.getX()/den,y=e.getY()/den,w=W();
            if(sec.equals("Domů")){if(y<80&&x<80){menu();return true;}if(y<80&&x>w-110){openWeb();return true;}if(y>=535&&y<839){int row=(int)((y-535)/104),col=(int)(x/(w/3));int i=row*3+col;if(i>=0&&i<9){if(i==8){show("Chat");chatDialog();}else show(names[i]);}return true;}if(y>=847&&y<940&&x>w-150){call();return true;}if(y>=950&&y<1035){openWeb();return true;}if(y>=1045&&y<1145&&x>35&&x<290){call();return true;}}
            else if(y<75){show("Domů");selectedNav=0;updateNav();return true;}return true;}
    }

    static class DB extends SQLiteOpenHelper{
        DB(Context c){super(c,"somsi.db",null,3);}
        public void onCreate(SQLiteDatabase d){d.execSQL("CREATE TABLE records(id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT,detail TEXT,created INTEGER)");d.execSQL("CREATE TABLE messages(id INTEGER PRIMARY KEY AUTOINCREMENT,author TEXT,msg TEXT,created INTEGER)");d.execSQL("INSERT INTO messages(author,msg,created) VALUES('SOMSI','Vítejte v interním chatu.',strftime('%s','now'))");}
        public void onUpgrade(SQLiteDatabase d,int a,int b){if(a<3){try{d.execSQL("ALTER TABLE messages ADD COLUMN created INTEGER DEFAULT 0");}catch(Exception ignored){}}}
        void addRecord(String t,String x){ContentValues v=new ContentValues();v.put("title",t);v.put("detail",x);v.put("created",System.currentTimeMillis());getWritableDatabase().insert("records",null,v);}
        void addMessage(String a,String m){ContentValues v=new ContentValues();v.put("author",a);v.put("msg",m);v.put("created",System.currentTimeMillis());getWritableDatabase().insert("messages",null,v);}
        List<String> messages(){List<String> out=new ArrayList<>();Cursor c=getReadableDatabase().rawQuery("SELECT author,msg FROM messages ORDER BY id DESC LIMIT 6",null);while(c.moveToNext())out.add(c.getString(0)+": "+c.getString(1));c.close();return out;}
    }
}
